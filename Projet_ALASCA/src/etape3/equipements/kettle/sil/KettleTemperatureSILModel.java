package etape3.equipements.kettle.sil;

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

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.KettleEventI;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
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
 * The class <code>KettleTemperatureSILModel</code> defines a SIL model
 * that simulates the evolution of the water temperature in the kettle
 * (water heater).
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
 *   {@code SwitchOnKettle},
 *   {@code SwitchOffKettle},
 *   {@code HeatKettle}, {@code DoNotHeatKettle}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables:
 *   name = {@code currentHeatingPower}, type = {@code Double}</li>
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
 * <p>Created on : 2026-02-06</p>
 */
@ModelExternalEvents(imported = {SwitchOnKettle.class,
								 SwitchOffKettle.class,
								 HeatKettle.class,
								 DoNotHeatKettle.class})
@ModelImportedVariable(name = "currentHeatingPower", type = Double.class)
public class			KettleTemperatureSILModel
extends		AtomicHIOA
implements	SIL_KettleOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = KettleTemperatureSILModel.class.
															getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** specific heat capacity of water in J/kg/C (or Kelvin).			*/
	protected static double		WATER_SPECIFIC_HEAT_CAPACITY = 4200.0;

	/** water mass in kg (200L tank, 1L = 1kg).							*/
	protected static double		WATER_MASS = 200.0;

	/** thermal loss factor (insulation). Higher value means better
	 *  insulation. For a 200L insulated water heater, this must be much
	 *  high to allow temperature to rise
	 *  properly (otherwise cooling dominates heating).						*/
	protected static double		INSULATION_CONSTANT = 100000.0;

	/** ambient temperature (if not imported from a room model).			*/
	protected static double		AMBIENT_TEMPERATURE = 20.0;

	/** default heating power in Watts (NORMAL mode).						*/
	protected static double		NORMAL_MODE_POWER = 2000.0;

	/** evaluation step for the differential equation (in hours).			*/
	protected static double		STEP = 60.0/3600.0;  // 1 minute = 1/60 hour

	/** current state of the kettle.										*/
	protected KettleState		currentState = KettleState.OFF;

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

	/** current heating power (imported from electricity model).
	 *  MAY BE NULL in integration test mode where the electricity model
	 *  is in ElectricMeterCyPhy, not KettleCyPhy.							*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>		currentHeatingPower;

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
				KettleTemperatureSILModel.class,
				"URI != null && !URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				WATER_SPECIFIC_HEAT_CAPACITY > 0.0,
				KettleTemperatureSILModel.class,
				"WATER_SPECIFIC_HEAT_CAPACITY > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				INSULATION_CONSTANT > 0.0,
				KettleTemperatureSILModel.class,
				"INSULATION_CONSTANT > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				AMBIENT_TEMPERATURE >= 0.0,
				KettleTemperatureSILModel.class,
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
		KettleTemperatureSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.currentState != null,
					KettleTemperatureSILModel.class,
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
		KettleTemperatureSILModel instance
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
	 * create a kettle temperature SIL model instance.
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
	public				KettleTemperatureSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	KettleTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.implementationInvariants(this)");
		assert	KettleTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.invariants(this)");
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
	 * @return				the derivative (variation in degrees C per second).
	 */
	protected double	computeDerivatives(Double currentTemp)
	{
		double heatingContribution = 0.0;

		// Heating contribution (gain)
		// Only heat when state is HEATING (requires explicit heat command)
		if (this.currentState == KettleState.HEATING) {
			// In integration test mode, imported variables may be null
			// because the electricity model is in ElectricMeterCyPhy
			double power = (this.currentHeatingPower != null && this.currentHeatingPower.isInitialised())
					? this.currentHeatingPower.getValue()
					: NORMAL_MODE_POWER; // Default: NORMAL mode power in Watts

			// Formula: P = m * Cp * (dT/dt) => dT/dt = P / (m * Cp)
			// This gives degrees C/s (derivative in seconds)
			heatingContribution = power / (WATER_MASS * WATER_SPECIFIC_HEAT_CAPACITY);

			// Physical limit: reduce heating power as we approach boiling point
			if (currentTemp > 95.0) {
				double reductionFactor = Math.max(0.0, (100.0 - currentTemp) / 5.0);
				heatingContribution *= reductionFactor;
			}

			// Hard limit: no heating above 100 degrees C
			if (currentTemp >= 100.0) {
				heatingContribution = 0.0;
			}
		}

		// Cooling contribution (loss to environment)
		// Newton's law of cooling: k * (T_ext - T_water)
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
			// deltaT is in hours, derivative is in degrees C/s
			// Multiply by 3600 to convert hours to seconds (same as etape2)
			double derivative = this.currentWaterTemperature.getFirstDerivative();
			newTemp = oldTemp + derivative * deltaT * 3600;
		} else {
			newTemp = oldTemp;
		}

		// Physical limit: water cannot exceed boiling point
		if (newTemp > 100.0) {
			newTemp = 100.0;
		}

		// accumulate the temperature*time to compute the mean temperature
		this.temperatureAcc += ((oldTemp + newTemp) / 2.0) * deltaT;

		return newTemp;
	}

	/**
	 * @see etape2.equipments.kettle.mil.KettleOperationI#setState(etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState)
	 */
	@Override
	public void			setState(KettleState s)
	{
		this.currentState = s;
	}

	/**
	 * @see etape2.equipments.kettle.mil.KettleOperationI#getState()
	 */
	@Override
	public KettleState	getState()
	{
		return this.currentState;
	}

	/**
	 * @see etape2.equipments.kettle.mil.KettleOperationI#setMode(etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode)
	 */
	@Override
	public void			setMode(KettleMode m)
	{
		// Nothing to do here, mode doesn't directly affect temperature
	}

	/**
	 * @see etape2.equipments.kettle.mil.KettleOperationI#getMode()
	 */
	@Override
	public KettleMode	getMode()
	{
		// Not managed in this model
		return null;
	}

	/**
	 * @see etape2.equipments.kettle.mil.KettleOperationI#setCurrentHeatingPower(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentHeatingPower(double newPower, Time t)
	{
		// Nothing to do here, power is imported from electricity model
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

		assert	KettleTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.implementationInvariants(this)");
		assert	KettleTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.invariants(this)");
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
		int justInitialised = 0;
		int notInitialisedYet = 0;

		// Check if imported variable is bound (it may be null in integration
		// test mode where the electricity model is in a different component)
		boolean importedVarsReady =
			(this.currentHeatingPower != null && this.currentHeatingPower.isInitialised());

		// In integration test mode, imported variable may be null because the
		// electricity model is in a different component (ElectricMeterCyPhy).
		// In this case, we can still initialize with default values.
		boolean importedVarsNull =
			(this.currentHeatingPower == null);

		if (!this.currentWaterTemperature.isInitialised() &&
			(importedVarsReady || importedVarsNull)) {
			// Initialize with default derivative (no heating)
			double derivative = this.computeDerivatives(AMBIENT_TEMPERATURE);

			// Initially, temperature is ambient temperature
			this.currentWaterTemperature.initialise(AMBIENT_TEMPERATURE, derivative);

			StringBuffer sb = new StringBuffer("new temperature: ");
			sb.append(this.currentWaterTemperature.getValue());
			sb.append(" degrees C at ");
			sb.append(this.currentWaterTemperature.getTime());
			sb.append(" seconds.");
			sb.append("\n");
			this.logMessage(sb.toString());

			justInitialised++;
		} else if (!this.currentWaterTemperature.isInitialised()) {
			// If the imported variable is not initialised and the current
			// temperature either, then say one more variable has not been
			// initialised yet at this execution, forcing another execution
			// to reach the fix point.
			notInitialisedYet++;
		}

		assert	KettleTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.implementationInvariants(this)");
		assert	KettleTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.invariants(this)");

		return new Pair<>(justInitialised, notInitialisedYet);
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
		if (this.currentState == KettleState.OFF) {
			// When the kettle is OFF, no simulation is needed
			return Duration.INFINITY;
		} else {
			// When the kettle is ON or HEATING, perform a continuous simulation
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
			sb.append(this.currentHeatingPower != null && this.currentHeatingPower.isInitialised()
					? this.currentHeatingPower.getValue() : "N/A");
			sb.append("\n");
			this.logMessage(sb.toString());
		}

		assert	KettleTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.implementationInvariants(this)");
		assert	KettleTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.invariants(this)");
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
		// and for the kettle model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof KettleEventI;

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("execute the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		// First, update the temperature (i.e., the value of the continuous
		// variable) until the current time.
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

		assert	KettleTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.implementationInvariants(this)");
		assert	KettleTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"KettleTemperatureSILModel.invariants(this)");
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
			this.logMessage(new KettleTemperatureReport(URI, meanTemperature).printout(" "));
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>KettleTemperatureReport</code> implements the
	 * simulation report for the <code>KettleTemperatureSILModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Created on : 2026-02-06</p>
	 */
	public static class		KettleTemperatureReport
	implements	SimulationReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	meanTemperature; // in Celsius

		public			KettleTemperatureReport(
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
			ret.append(" degrees C.\n");
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
		return new KettleTemperatureReport(this.getURI(), this.meanTemperature);
	}
}
// -----------------------------------------------------------------------------
