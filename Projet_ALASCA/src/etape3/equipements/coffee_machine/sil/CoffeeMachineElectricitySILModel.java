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

import etape1.equipements.coffee_machine.CoffeeMachine;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape2.GlobalReportI;
import etape2.equipments.coffeemachine.mil.events.CoffeeMachineEventI;
import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.Heat;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.ServeCoffee;
import etape2.equipments.coffeemachine.mil.events.SetEcoModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetMaxModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetNormalModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import etape2.utils.Electricity;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI.VariableValue;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
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
 * The class <code>CoffeeMachineElectricitySILModel</code> defines a SIL
 * simulation model for the electricity consumption of the coffee machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The electric power consumption depends upon the state (OFF, ON, HEATING) and
 * the current mode (SUSPEND, ECO, NORMAL, MAX). This model also tracks the
 * water level which decreases when coffee is made.
 * </p>
 *
 * <ul>
 * <li>Imported events:
 *   {@code SwitchOnCoffeeMachine},
 *   {@code SwitchOffCoffeeMachine},
 *   {@code Heat}, {@code DoNotHeat},
 *   {@code SetEcoModeCoffeeMachine}, {@code SetMaxModeCoffeeMachine},
 *   {@code SetNormalModeCoffeeMachine}, {@code SetSuspendedModeCoffeeMachine},
 *   {@code MakeCoffee}, {@code ServeCoffee},
 *   {@code FillWaterCoffeeMachine}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables:
 *   name = {@code currentIntensity}, type = {@code Double}
 *   name = {@code currentHeatingPower}, type = {@code Double}
 *   name = {@code currentWaterLevel}, type = {@code Double}</li>
 * </ul>
 *
 * <p><strong>Implementation Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code currentState != null}
 * invariant	{@code currentMode != null}
 * invariant	{@code totalConsumption >= 0.0}
 * invariant	{@code !currentIntensity.isInitialised() || currentIntensity.getValue() >= 0.0}
 * invariant	{@code !currentHeatingPower.isInitialised() || currentHeatingPower.getValue() >= 0.0}
 * invariant	{@code !currentWaterLevel.isInitialised() || currentWaterLevel.getValue() >= 0.0}
 * </pre>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
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
								 SetEcoModeCoffeeMachine.class,
								 SetMaxModeCoffeeMachine.class,
								 SetNormalModeCoffeeMachine.class,
								 SetSuspendedModeCoffeeMachine.class,
								 MakeCoffee.class,
								 ServeCoffee.class,
								 FillWaterCoffeeMachine.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentHeatingPower", type = Double.class)
@ModelExportedVariable(name = "currentWaterLevel", type = Double.class)
//-----------------------------------------------------------------------------
public class			CoffeeMachineElectricitySILModel
extends		AtomicHIOA
implements	SIL_CoffeeMachineOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = CoffeeMachineElectricitySILModel.class.
															getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** current state of the coffee machine.								*/
	protected CoffeeMachineState		currentState = CoffeeMachineState.OFF;
	/** current mode of the coffee machine.									*/
	protected CoffeeMachineMode			currentMode = CoffeeMachineMode.SUSPEND;

	/** true when the electricity consumption has changed after executing an
	 *  external event; triggers an internal transition to update the
	 *  variable <code>currentIntensity</code>.								*/
	protected boolean					consumptionHasChanged = false;

	/** power consumption in SUSPEND mode (watts).							*/
	protected double					suspendedModeConsumption;
	/** power consumption in ECO mode (watts).								*/
	protected double					ecoModeConsumption;
	/** power consumption in NORMAL mode (watts).							*/
	protected double					normalModeConsumption;
	/** power consumption in MAX mode (watts).								*/
	protected double					maxModeConsumption;
	/** maximum water capacity in liters.									*/
	protected double					maxWaterCapacity;
	/** total consumption of the coffee machine during the simulation (kwh).*/
	protected double					totalConsumption;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current heating power in watts.										*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentHeatingPower = new Value<Double>(this);

	/** current intensity in amperes.										*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentIntensity = new Value<Double>(this);

	/** current water level in liters.										*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentWaterLevel = new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static implementation invariants are observed, false otherwise.
	 */
	protected static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
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
		CoffeeMachineElectricitySILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.currentState != null,
					CoffeeMachineElectricitySILModel.class,
					instance,
					"currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.currentMode != null,
					CoffeeMachineElectricitySILModel.class,
					instance,
					"currentMode != null");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.totalConsumption >= 0.0,
					CoffeeMachineElectricitySILModel.class,
					instance,
					"totalConsumption >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
					!instance.currentIntensity.isInitialised() ||
									instance.currentIntensity.getValue() >= 0.0,
					CoffeeMachineElectricitySILModel.class,
					instance,
					"!currentIntensity.isInitialised() || "
							+ "currentIntensity.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
					!instance.currentHeatingPower.isInitialised() ||
									instance.currentHeatingPower.getValue() >= 0.0,
					CoffeeMachineElectricitySILModel.class,
					instance,
					"!currentHeatingPower.isInitialised() || "
							+ "currentHeatingPower.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
					!instance.currentWaterLevel.isInitialised() ||
									instance.currentWaterLevel.getValue() >= 0.0,
					CoffeeMachineElectricitySILModel.class,
					instance,
					"!currentWaterLevel.isInitialised() || "
							+ "currentWaterLevel.getValue() >= 0.0");
		return ret;
	}

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
				CoffeeMachineElectricitySILModel.class,
				"URI != null && !URI.isEmpty()");
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
		CoffeeMachineElectricitySILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a coffee machine SIL electricity model instance.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine != null && !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine instanceof AtomicEngine}
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
	public				CoffeeMachineElectricitySILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.suspendedModeConsumption = CoffeeMachine.SUSPENDED_POWER_IN_WATTS.getData();
		this.ecoModeConsumption = CoffeeMachine.ECO_POWER_IN_WATTS.getData();
		this.normalModeConsumption = CoffeeMachine.NORMAL_POWER_IN_WATTS.getData();
		this.maxModeConsumption = CoffeeMachine.HIGH_POWER_IN_WATTS.getData();
		this.maxWaterCapacity = CoffeeMachine.WATER_CAPACITY.getData();

		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setState(etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState)
	 */
	@Override
	public void			setState(CoffeeMachineState s)
	{
		CoffeeMachineState old = this.currentState;
		this.currentState = s;
		if (old != s) {
			this.consumptionHasChanged = true;
		}

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
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
	public void			setMode(CoffeeMachineMode m)
	{
		CoffeeMachineMode old = this.currentMode;
		this.currentMode = m;
		if (old != m) {
			this.consumptionHasChanged = true;
		}

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#getMode()
	 */
	@Override
	public CoffeeMachineMode	getMode()
	{
		return this.currentMode;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setCurrentHeatingPower(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentHeatingPower(double newPower, Time t)
	{
		assert	newPower >= 0.0 :
				new NeoSim4JavaException(
					"Precondition violation: newPower >= 0.0, but newPower = " + newPower);

		double oldPower = this.currentHeatingPower.isInitialised() ?
						  this.currentHeatingPower.getValue() : 0.0;
		this.currentHeatingPower.setNewValue(newPower, t);
		if (newPower != oldPower) {
			this.consumptionHasChanged = true;
		}

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setCurrentWaterLevel(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentWaterLevel(double newLevel, Time t)
	{
		assert	newLevel >= 0.0 :
				new NeoSim4JavaException(
					"Precondition violation: newLevel >= 0.0, but newLevel = " + newLevel);

		this.currentWaterLevel.setNewValue(newLevel, t);

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * For software-in-the-loop tests with simulation, return the current value
	 * of the {@code currentWaterLevel} variable.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current value of the {@code currentWaterLevel} variable.
	 */
	public VariableValue<Double>	getCurrentWaterLevel()
	{
		return new VariableValue<Double>(
							this.currentWaterLevel.getValue(),
							this.currentWaterLevel.getTime());
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

		this.currentState = CoffeeMachineState.OFF;
		this.currentMode = CoffeeMachineMode.SUSPEND;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
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
		Pair<Integer, Integer> ret = null;

		if (!this.currentIntensity.isInitialised()) {
			// initially, the coffee machine is off, so its consumption is zero.
			this.currentIntensity.initialise(0.0);
			this.currentHeatingPower.initialise(0.0);
			this.currentWaterLevel.initialise(this.maxWaterCapacity);

			if (VERBOSE) {
				StringBuffer sb = new StringBuffer("new consumption: ");
				sb.append(this.currentIntensity.getValue());
				sb.append(" ");
				sb.append(ElectricMeterImplementationI.POWER_UNIT);
				sb.append(" at ");
				sb.append(this.currentIntensity.getTime());
				sb.append(" seconds.");
				this.logMessage(sb.toString());
			}

			ret = new Pair<>(3, 0);
		} else {
			ret = new Pair<>(0, 0);
		}

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");

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
		Duration ret = null;

		if (this.consumptionHasChanged) {
			// When the consumption has changed, an immediate (delay = 0.0)
			// internal transition must be made to update the electricity
			// consumption.
			this.consumptionHasChanged = false;
			ret = Duration.zero(this.getSimulatedTimeUnit());
		} else {
			// As long as the state does not change, no internal transition
			// is made (delay = infinity).
			ret = Duration.INFINITY;
		}

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();
		double currentPower = 0.0;

		if (this.currentState == CoffeeMachineState.OFF) {
			currentPower = 0.0;
		} else if (this.currentState == CoffeeMachineState.HEATING) {
			// When heating, use the current heating power
			currentPower = this.currentHeatingPower.getValue();
		} else {
			// When ON but not heating, use mode-based power
			switch (this.currentMode) {
				case SUSPEND:
					currentPower = this.suspendedModeConsumption;
					break;
				case ECO:
					currentPower = this.ecoModeConsumption;
					break;
				case NORMAL:
					currentPower = this.normalModeConsumption;
					break;
				case MAX:
					currentPower = this.maxModeConsumption;
					break;
			}
		}

		// Update current intensity (I = P/V, assuming 220V)
		this.currentIntensity.setNewValue(currentPower / 220.0, t);

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("new consumption: ");
			sb.append(this.currentIntensity.getValue());
			sb.append(" A (");
			sb.append(currentPower);
			sb.append(" W) at ");
			sb.append(this.currentIntensity.getTime());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime)
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

		// compute the total consumption for the simulation report.
		this.totalConsumption +=
				Electricity.computeConsumption(
						elapsedTime,
						220.0 * this.currentIntensity.getValue());

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("execute the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		// the next call will update the current state of the coffee machine
		// and if this state has changed, it puts the boolean consumptionHasChanged
		// at true, which in turn will trigger an immediate internal transition
		// to update the current intensity of the coffee machine electricity
		// consumption.
		ce.executeOn(this);

		assert	CoffeeMachineElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert	CoffeeMachineElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption +=
				Electricity.computeConsumption(
						d,
						220.0 * this.currentIntensity.getValue());

		if (VERBOSE) {
			this.logMessage("simulation ends.");
			this.logMessage(new CoffeeMachineElectricityReport(URI, totalConsumption).printout(" "));
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>CoffeeMachineElectricityReport</code> implements the
	 * simulation report for the <code>CoffeeMachineElectricityModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Created on : 2025-01-07</p>
	 *
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		CoffeeMachineElectricityReport
	implements	SimulationReportI, GlobalReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption; // in kwh

		public			CoffeeMachineElectricityReport(
			String modelURI,
			double totalConsumption
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
		}

		@Override
		public String	getModelURI()
		{
			return this.modelURI;
		}

		@Override
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
			ret.append("total consumption in kwh = ");
			ret.append(this.totalConsumption);
			ret.append(".\n");
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
		return new CoffeeMachineElectricityReport(this.getURI(), this.totalConsumption);
	}

	@Override
	public void setStateMode(CoffeeMachineState on, CoffeeMachineMode normal) {
		// TODO Auto-generated method stub
		
	}
}
// -----------------------------------------------------------------------------
