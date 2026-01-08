package etape3.equipements.coffee_machine.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability.
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or
// data to be ensured and,  more generally, to use and operate it in the
// same conditions as regards security.
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape1.equipements.coffee_machine.Constants;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape2.equipments.coffeemachine.mil.events.CoffeeMachineEventI;
import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.Heat;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI.VariableValue;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>CoffeeMachineTemperatureSILModel</code> defines a SIL model
 * that simulates the evolution of the water temperature in the coffee machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The derivative of temperature is calculated according to the formula:
 * dT/dt = (Heating_Power) / (Water_Mass * Water_Thermal_Capacity) - (Thermal_Losses)
 * where thermal losses follow Newton's law of cooling.
 * </p>
 *
 * <ul>
 * <li>Imported events:
 *   {@code SwitchOnCoffeeMachine},
 *   {@code SwitchOffCoffeeMachine},
 *   {@code Heat}, {@code DoNotHeat},
 *   {@code MakeCoffee}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables:
 *   name = {@code currentHeatingPower}, type = {@code Double}
 *   name = {@code currentWaterLevel}, type = {@code Double}</li>
 * <li>Exported variables: none</li>
 * </ul>
 *
 * <p><strong>Implementation Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code currentState != null}
 * </pre>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
 * invariant	{@code WATER_SPECIFIC_HEAT_CAPACITY > 0.0}
 * invariant	{@code INSULATION_CONSTANT > 0.0}
 * invariant	{@code AMBIENT_TEMPERATURE >= 0.0}
 * </pre>
 *
 * <p>Created on : 2025-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(imported = {SwitchOnCoffeeMachine.class,
								 SwitchOffCoffeeMachine.class,
								 Heat.class,
								 DoNotHeat.class,
								 MakeCoffee.class})
@ModelImportedVariable(name = "currentHeatingPower", type = Double.class)
@ModelImportedVariable(name = "currentWaterLevel", type = Double.class)
public class			CoffeeMachineTemperatureSILModel
extends		AtomicHIOA
implements	SIL_CoffeeMachineOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = CoffeeMachineTemperatureSILModel.class.
															getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** specific heat capacity of water in J/kg/°C (or Kelvin).			*/
	protected static double		WATER_SPECIFIC_HEAT_CAPACITY =
										Constants.WATER_THERMAL_CAPACITY;

	/** thermal loss factor (insulation). Higher value means better
	 *  insulation.															*/
	protected static double		INSULATION_CONSTANT = 1000.0;

	/** ambient temperature (if not imported from a room model).			*/
	protected static double		AMBIENT_TEMPERATURE = 20.0;

	/** evaluation step for the differential equation (in hours).			*/
	protected static double		STEP = 60.0/3600.0;  // 1 minute = 1/60 hour

	/** current state of the coffee machine (similar to electricity model).*/
	protected CoffeeMachineState	currentState = CoffeeMachineState.ON;

	/** integration step as a duration, including the time unit.			*/
	protected final Duration		integrationStep;

	/** accumulator to compute the mean temperature for the simulation
	 *  report.																*/
	protected double				temperatureAcc;
	/** the simulation time of start used to compute the mean temperature.	*/
	protected Time					start;
	/** the mean temperature over the simulation duration for the
	 *  simulation report.													*/
	protected double				meanTemperature;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current heating power (imported from electricity model).			*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>		currentHeatingPower;

	/** current water level in kg (imported from electricity model).		*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>		currentWaterLevel;

	/** current water temperature (internal variable).						*/
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double>	currentWaterTemperature =
												new DerivableValue<>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				CoffeeMachineTemperatureSILModel.class,
				"URI != null && !URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				WATER_SPECIFIC_HEAT_CAPACITY > 0.0,
				CoffeeMachineTemperatureSILModel.class,
				"WATER_SPECIFIC_HEAT_CAPACITY > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				INSULATION_CONSTANT > 0.0,
				CoffeeMachineTemperatureSILModel.class,
				"INSULATION_CONSTANT > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				AMBIENT_TEMPERATURE >= 0.0,
				CoffeeMachineTemperatureSILModel.class,
				"AMBIENT_TEMPERATURE >= 0.0");
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		CoffeeMachineTemperatureSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.currentState != null,
					CoffeeMachineTemperatureSILModel.class,
					instance,
					"currentState != null");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(
		CoffeeMachineTemperatureSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a coffee machine temperature SIL model instance.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine == null || !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine == null || simulationEngine instanceof AtomicEngine}
	 * post	{@code !isDebugModeOn()}
	 * post	{@code getURI() != null && !getURI().isEmpty()}
	 * post	{@code uri == null || getURI().equals(uri)}
	 * post	{@code getSimulatedTimeUnit().equals(simulatedTimeUnit)}
	 * post	{@code getSimulationEngine().equals(simulationEngine)}
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do</i>.
	 */
	public				CoffeeMachineTemperatureSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	CoffeeMachineTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.implementationInvariants(this)");
		assert	CoffeeMachineTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * compute the derivative of temperature (dT/dt) at time t.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code currentTemp != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param currentTemp	the current temperature.
	 * @return				the derivative (variation in °C per hour).
	 */
	protected double	computeDerivatives(Double currentTemp)
	{
		// Get water mass (in kg, assuming 1L = 1kg)
		double waterMass = this.currentWaterLevel.getValue();

		// Safety: if no water, no heating
		if (waterMass <= 0.001) {
			return 0.0;
		}

		double heatingContribution = 0.0;

		// Heating contribution (gain)
		if (this.currentState == CoffeeMachineState.HEATING) {
			double power = this.currentHeatingPower.getValue(); // In Watts (Joules/sec)

			// Formula: P = m * Cp * (dT/dt) => dT/dt = P / (m * Cp)
			// This gives °C/s, so multiply by 3600 to convert to °C/h (deltaT is in hours)
			heatingContribution =
					power / (waterMass * WATER_SPECIFIC_HEAT_CAPACITY) * 60.0;

			// Physical limit: reduce heating power as we approach boiling point
			// to prevent overshoot due to discrete time steps
			if (currentTemp > 95.0) {
				// Gradual reduction from 95°C to 100°C
				// At 95°C: factor = 1.0 (full power)
				// At 97.5°C: factor = 0.5 (half power)
				// At 100°C: factor = 0.0 (no heating)
				double reductionFactor = Math.max(0.0, (100.0 - currentTemp) / 5.0);
				heatingContribution *= reductionFactor;
			}

			// Hard limit: no heating above 100°C
			if (currentTemp >= 100.0) {
				heatingContribution = 0.0;
			}
		}

		// Cooling contribution (loss to environment)
		// Newton's law of cooling: k * (T_ext - T_water)
		// Simplified with insulation constant.
		double coolingContribution =
				(AMBIENT_TEMPERATURE - currentTemp) / INSULATION_CONSTANT;

		return heatingContribution + coolingContribution;
	}

	/**
	 * compute the new temperature after a time deltaT via the Euler method.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code deltaT >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param deltaT	time delta in hours (from simulation).
	 * @return			the new temperature.
	 */
	protected double	computeNewTemperature(double deltaT)
	{
		Time t = this.currentWaterTemperature.getTime();
		double oldTemp = this.currentWaterTemperature.evaluateAt(t);
		double newTemp;

		if (deltaT > 0.0001) { // TEMPERATURE_UPDATE_TOLERANCE
			// T(t+dt) = T(t) + T'(t) * dt
			// deltaT is in hours, derivative is in °C per hour
			double derivative = this.currentWaterTemperature.getFirstDerivative();
			newTemp = oldTemp + derivative * deltaT;
		} else {
			newTemp = oldTemp;
		}

		// Physical limit: water cannot exceed boiling point at atmospheric pressure
		// Hard cap at 100°C (boiling point of water)
		if (newTemp > 100.0) {
			newTemp = 100.0;
		}

		// accumulate the temperature*time to compute the mean temperature
		this.temperatureAcc += ((oldTemp + newTemp) / 2.0) * deltaT;

		return newTemp;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setState(etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState)
	 */
	@Override
	public void			setState(CoffeeMachineState s)
	{
		this.currentState = s;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#getState()
	 */
	@Override
	public CoffeeMachineState	getState()
	{
		return this.currentState;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setMode(etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode)
	 */
	@Override
	public void			setMode(
		etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode m
		)
	{
		// Nothing to do here, mode doesn't directly affect temperature
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#getMode()
	 */
	@Override
	public etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode	getMode()
	{
		// Not managed in this model
		return null;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setCurrentHeatingPower(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentHeatingPower(double newPower, Time t)
	{
		// Nothing to do here, power is imported from electricity model
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setCurrentWaterLevel(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentWaterLevel(double newLevel, Time t)
	{
		// Nothing to do here, water level is imported from electricity model
	}

	/**
	 * For software-in-the-loop tests with simulation, return the current value
	 * of the {@code currentWaterTemperature} variable.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current value of the {@code currentWaterTemperature} variable.
	 */
	public VariableValue<Double>	getCurrentTemperature()
	{
		return new VariableValue<Double>(
							this.currentWaterTemperature.getValue(),
							this.currentWaterTemperature.getTime());
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

		// this gets the reference on the owner component which is required
		// to have simulation models able to make the component perform some
		// operations or tasks or to get the value of variables held by the
		// component when necessary.
		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			// by the following, all of the logging will appear in the owner
			// component logger
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.start = initialTime;
		this.temperatureAcc = 0.0;
		this.meanTemperature = 0.0;

		if (VERBOSE) {
			this.logMessage("simulation begins. Water Temp: " + AMBIENT_TEMPERATURE);
		}

		assert	CoffeeMachineTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.implementationInvariants(this)");
		assert	CoffeeMachineTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables()
	{
		Pair<Integer, Integer> ret;

		if (!this.currentWaterTemperature.isInitialised()) {
			
			double derivative = this.computeDerivatives(AMBIENT_TEMPERATURE);
			
			// Initially, temperature is ambient temperature
			this.currentWaterTemperature.initialise(AMBIENT_TEMPERATURE, derivative);

			StringBuffer sb = new StringBuffer("new temperature: ");
			sb.append(this.currentWaterTemperature.getValue());
			sb.append(" °C at ");
			sb.append(this.currentWaterTemperature.getTime());
			sb.append(" seconds.\n");
			this.logMessage(sb.toString());

			ret = new Pair<>(1, 0);
		} else {
			ret = new Pair<>(0, 0);
		}

		assert	CoffeeMachineTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.implementationInvariants(this)");
		assert	CoffeeMachineTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		if (this.currentState == CoffeeMachineState.OFF) {
			// When the machine is OFF, no simulation is needed
			return Duration.INFINITY;
		} else {
			// When the machine is ON or HEATING, perform a continuous simulation
			return this.integrationStep;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// recompute the current temperature
		// Note: accumulation for mean temperature is now done inside computeNewTemperature()
		double newTemp = this.computeNewTemperature(elapsedTime.getSimulatedDuration());
		// compute the derivative
		double newDerivative = this.computeDerivatives(newTemp);
		// set the new temperature value and derivative
		Time t = this.getCurrentStateTime();
		this.currentWaterTemperature.setNewValue(newTemp, newDerivative, t);

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer();
			sb.append(t.getSimulatedTime());
			sb.append("|");
			sb.append(newTemp);
			sb.append("|");
			sb.append(this.currentState);
			sb.append("|");
			sb.append(this.currentHeatingPower.getValue());
			sb.append("|");
			sb.append(this.currentWaterLevel.getValue());
			sb.append("\n");
			this.logMessage(sb.toString());
		}

		assert	CoffeeMachineTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.implementationInvariants(this)");
		assert	CoffeeMachineTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the coffee machine model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof CoffeeMachineEventI;

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("execute the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		// First, update the temperature (i.e., the value of the continuous
		// variable) until the current time.
		// Note: accumulation for mean temperature is done inside computeNewTemperature()
		double newTemp = this.computeNewTemperature(elapsedTime.getSimulatedDuration());

		// Then, execute the event on this model, causing the appropriate
		// transition (e.g., state change).
		ce.executeOn(this);

		// Next, compute the new derivative based on the new state
		double newDerivative = this.computeDerivatives(newTemp);

		// Finally, set the new temperature value and derivative
		if (elapsedTime.getSimulatedDuration() > 0.0001) { // TEMPERATURE_UPDATE_TOLERANCE
			this.currentWaterTemperature.setNewValue(
					newTemp,
					newDerivative,
					new Time(this.getCurrentStateTime().getSimulatedTime(),
							 this.getSimulatedTimeUnit()));
		}

		assert	CoffeeMachineTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.implementationInvariants(this)");
		assert	CoffeeMachineTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineTemperatureModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		this.meanTemperature =
				this.temperatureAcc/
					endTime.subtract(this.start).getSimulatedDuration();

		if (VERBOSE) {
			this.logMessage("simulation ends.");
			this.logMessage(new CoffeeMachineTemperatureReport(URI, meanTemperature).printout(" "));
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>CoffeeMachineTemperatureReport</code> implements the
	 * simulation report for the <code>CoffeeMachineTemperatureModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Created on : 2025-01-07</p>
	 *
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		CoffeeMachineTemperatureReport
	implements	SimulationReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	meanTemperature; // in Celsius

		public			CoffeeMachineTemperatureReport(
			String modelURI,
			double meanTemperature
			)
		{
			super();
			this.modelURI = modelURI;
			this.meanTemperature = meanTemperature;
		}

		@Override
		public String	getModelURI()
		{
			return this.modelURI;
		}

		public String	printout(String indent)
		{
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("mean temperature = ");
			ret.append(this.meanTemperature);
			ret.append(" °C.\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport()
	{
		return new CoffeeMachineTemperatureReport(this.getURI(), this.meanTemperature);
	}

	@Override
	public void setStateMode(CoffeeMachineState on, CoffeeMachineMode normal) {
		// TODO Auto-generated method stub
		
	}
}
// -----------------------------------------------------------------------------
