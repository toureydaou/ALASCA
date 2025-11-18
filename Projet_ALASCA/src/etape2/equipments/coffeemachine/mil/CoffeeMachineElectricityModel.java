package etape2.equipments.coffeemachine.mil;

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
import java.util.concurrent.TimeUnit;

import etape1.equipements.coffee_machine.CoffeeMachine;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape2.GlobalReportI;
import etape2.equipments.coffeemachine.mil.events.CoffeeMachineEventI;
import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.ServeCoffee;
import etape2.equipments.coffeemachine.mil.events.SetEcoModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetMaxModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetNormalModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetPowerCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import etape2.utils.Electricity;
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
 * The class <code>CoffeeMachineElectricityModel</code> defines a simulation
 * model for the electricity consumption of the heater.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * The electric power consumption (in amperes) depends upon the state and the
 * current power level i.e., {@code State.OFF => consumption == 0.0},
 * {@code State.ON => consumption == NOT_HEATING_POWER} and
 * {@code State.HEATING => consumption >= NOT_HEATING_POWER && consumption <=
 * MAX_HEATING_POWER}). The state of the heater is modified by the reception of
 * external events ({@code SwitchOnCoffeeMachine},
 * {@code SwitchOffCoffeeMachine}, {@code Heat} and {@code DoNotHeat}). The
 * power level is set through the external event {@code SetPowerCoffeeMachine}
 * that has a parameter defining the required power level. The electric power
 * consumption is stored in the exported variable {@code currentIntensity}.
 * </p>
 * <p>
 * Initially, the mode is in state {@code State.OFF} and the electric power
 * consumption at 0.0.
 * </p>
 * 
 * <ul>
 * <li>Imported events: {@code SwitchOnCoffeeMachine},
 * {@code SwitchOffCoffeeMachine}, {@code SetPowerCoffeeMachine}, {@code Heat},
 * {@code DoNotHeat}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables: name = {@code currentIntensity}, type =
 * {@code Double}</li>
 * </ul>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * NOT_HEATING_POWER >= 0.0
 * }
 * invariant	{@code
 * MAX_HEATING_POWER > NOT_HEATING_POWER
 * }
 * invariant	{@code
 * TENSION > 0.0
 * }
 * invariant	{@code
 * currentState != null
 * }
 * invariant	{@code
 * totalConsumption >= 0.0
 * }
 * invariant	{@code
 * !currentHeatingPower.isInitialised() || currentHeatingPower.getValue() >= 0.0
 * }
 * invariant	{@code
 * !currentIntensity.isInitialised() || currentIntensity.getValue() >= 0.0
 * }
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * URI != null && !URI.isEmpty()
 * }
 * invariant	{@code
 * NOT_HEATING_POWER_RUNPNAME != null && !NOT_HEATING_POWER_RUNPNAME.isEmpty()
 * }
 * invariant	{@code
 * MAX_HEATING_POWER_RUNPNAME != null && !MAX_HEATING_POWER_RUNPNAME.isEmpty()
 * }
 * invariant	{@code
 * TENSION_RUNPNAME != null && !TENSION_RUNPNAME.isEmpty()
 * }
 * </pre>
 * 
 * <p>
 * Created on : 2023-09-29
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(imported = { SwitchOnCoffeeMachine.class, SwitchOffCoffeeMachine.class,
		SetPowerCoffeeMachine.class, DoNotHeat.class, SetEcoModeCoffeeMachine.class, SetMaxModeCoffeeMachine.class,
		SetSuspendedModeCoffeeMachine.class, SetNormalModeCoffeeMachine.class, ServeCoffee.class, MakeCoffee.class, FillWaterCoffeeMachine.class })
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentHeatingPower", type = Double.class)
@ModelExportedVariable(name = "currentWaterLevel", type = Double.class)
//-----------------------------------------------------------------------------
public class CoffeeMachineElectricityModel extends AtomicHIOA {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created. */
	public static final String URI = CoffeeMachineElectricityModel.class.getSimpleName();
	/** when true, leaves a trace of the execution of the model. */
	public static boolean VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model. */
	public static boolean DEBUG = false;

	/** current state of the heater. */
	protected CoffeeMachineState currentState = CoffeeMachineState.OFF;
	protected CoffeeMachineMode currentMode = CoffeeMachineMode.NORMAL;

	/**
	 * true when the electricity consumption of the heater has changed after
	 * executing an external event; the external event changes the value of
	 * <code>currentState</code> and then an internal transition will be triggered
	 * by putting through in this variable which will update the variable
	 * <code>currentIntensity</code>.
	 */
	protected boolean consumptionHasChanged = false;

	/**
	 * power consumption in the LOW mode in the power unit defined by the hair
	 * dryer.
	 */
	protected double suspendedModeConsumption;
	/**
	 * power consumption in the HIGH mode in the power unit defined by the hair
	 * dryer.
	 */
	protected double normalModeConsumption;

	/**
	 * power consumption in the HIGH mode in the power unit defined by the hair
	 * dryer.
	 */
	protected double ecoModeConsumption;

	/**
	 * power consumption in the HIGH mode in the power unit defined by the hair
	 * dryer.
	 */
	protected double maxModeConsumption;

	protected double maxWaterCapacity;

	/** total consumption of the heater during the simulation in kwh. */
	protected double totalConsumption;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/**
	 * the current heating power between 0 and
	 * {@code CoffeeMachineElectricityModel.MAX_HEATING_POWER} in the power unit
	 * used by the heater.
	 */
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentHeatingPower = new Value<Double>(this);
	/** current intensity in the power unit used by the electric meter. */
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentIntensity = new Value<Double>(this);

	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentWaterLevel = new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @return true if the static implementation invariants are observed, false
	 *         otherwise.
	 */
	protected static boolean staticImplementationInvariants() {
		boolean ret = true;
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * instance != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param instance instance to be tested.
	 * @return true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean implementationInvariants(CoffeeMachineElectricityModel instance) {
		assert instance != null : new NeoSim4JavaException("Precondition violation: " + "instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(instance.currentState != null,
				CoffeeMachineElectricityModel.class, instance, "currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(instance.totalConsumption >= 0.0,
				CoffeeMachineElectricityModel.class, instance, "totalConsumption >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentHeatingPower.isInitialised() || instance.currentHeatingPower.getValue() >= 0.0,
				CoffeeMachineElectricityModel.class, instance,
				"!currentHeatingPower.isInitialised() || " + "currentHeatingPower.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentIntensity.isInitialised() || instance.currentIntensity.getValue() >= 0.0,
				CoffeeMachineElectricityModel.class, instance,
				"!currentIntensity.isInitialised() || " + "currentIntensity.getValue() >= 0.0");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @return true if the invariants are observed, false otherwise.
	 */
	public static boolean staticInvariants() {
		boolean ret = true;
		// ret &= CoffeeMachineSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(URI != null && !URI.isEmpty(),
				CoffeeMachineElectricityModel.class, "URI != null && !URI.isEmpty()");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * instance != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param instance instance to be tested.
	 * @return true if the invariants are observed, false otherwise.
	 */
	protected static boolean invariants(CoffeeMachineElectricityModel instance) {
		assert instance != null : new NeoSim4JavaException("Precondition violation: instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a heater MIL electricity model instance.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * uri == null || !uri.isEmpty()
	 * }
	 * pre	{@code
	 * simulatedTimeUnit != null
	 * }
	 * pre	{@code
	 * simulationEngine != null && !simulationEngine.isModelSet()
	 * }
	 * pre	{@code
	 * simulationEngine instanceof AtomicEngine
	 * }
	 * post	{@code
	 * !isDebugModeOn()
	 * }
	 * post	{@code
	 * getURI() != null && !getURI().isEmpty()
	 * }
	 * post	{@code
	 * uri == null || getURI().equals(uri)
	 * }
	 * post	{@code
	 * getSimulatedTimeUnit().equals(simulatedTimeUnit)
	 * }
	 * post	{@code
	 * getSimulationEngine().equals(simulationEngine)
	 * }
	 * </pre>
	 *
	 * @param uri               URI of the model.
	 * @param simulatedTimeUnit time unit used for the simulation time.
	 * @param simulationEngine  simulation engine to which the model is attached.
	 * @throws Exception <i>to do</i>.
	 */
	public CoffeeMachineElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine)
			throws Exception {

		super(uri, simulatedTimeUnit, simulationEngine);

		this.suspendedModeConsumption = CoffeeMachine.SUSPENDED_POWER_IN_WATTS.getData();
		this.maxModeConsumption = CoffeeMachine.HIGH_POWER_IN_WATTS.getData();
		this.normalModeConsumption = CoffeeMachine.NORMAL_POWER_IN_WATTS.getData();
		this.ecoModeConsumption = CoffeeMachine.ECO_POWER_IN_WATTS.getData();
		this.maxWaterCapacity = CoffeeMachine.WATER_CAPACITY.getData();
		

		this.getSimulationEngine().setLogger(new StandardLogger());

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * set the state of the heater.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * s != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param s the new state.
	 * @param t time at which the state {@code s} is set.
	 */
	public void setState(CoffeeMachineState s, Time t) {
		CoffeeMachineState old = this.currentState;
		this.currentState = s;
		if (old != s) {
			this.consumptionHasChanged = true;
		}

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * return the state of the heater.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * ret != null
	 * }
	 * </pre>
	 *
	 * @return the current state.
	 */
	public CoffeeMachineState getState() {
		return this.currentState;
	}

	public void setStateMode(CoffeeMachineState s, CoffeeMachineMode m) {
		this.currentState = s;
		this.currentMode = m;
	}

	/**
	 * return the current mode of the hair dryer.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return the state of the hair dryer.
	 */
	public CoffeeMachineMode getMode() {
		return this.currentMode;
	}

	/**
	 * toggle the value of the state of the model telling whether the electricity
	 * consumption level has just changed or not; when it changes after receiving an
	 * external event, an immediate internal transition is triggered to update the
	 * level of electricity consumption.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 */
	public void toggleConsumptionHasChanged() {
		if (this.consumptionHasChanged) {
			this.consumptionHasChanged = false;
		} else {
			this.consumptionHasChanged = true;
		}
	}

	public void setCurrentWaterLevel(double water, Time t) {

		assert water >= 0.0 && water <= this.maxWaterCapacity
	            : new NeoSim4JavaException("Water level must be non-negative and not exceed max capacity.");

		if (water <= this.maxWaterCapacity) {
			this.currentWaterLevel.setNewValue(water, t);
		} else {
			this.currentWaterLevel.setNewValue(this.maxWaterCapacity, t);
		}
	}

	/**
	 * set the current heating power of the heater to {@code newPower}.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * newPower >= 0.0 && newPower <= MAX_HEATING_POWER
	 * }
	 * post	{@code
	 * getCurrentHeatingPower() == newPower
	 * }
	 * </pre>
	 *
	 * @param newPower the new power in the unit used by the heater to be set on the
	 *                 heater.
	 * @param t        time at which the new power is set.
	 */
	public void setCurrentHeatingPower(double newPower, Time t) {
		assert newPower >= 0.0 && newPower <= CoffeeMachine.HIGH_POWER_IN_WATTS.getData()
				: new NeoSim4JavaException("Precondition violation: newPower >= 0.0 && "
						+ "newPower <= CoffeeMachineElectricityModel.MAX_HEATING_POWER," + " but newPower = "
						+ newPower);

		double oldPower = this.currentHeatingPower.getValue();
		this.currentHeatingPower.setNewValue(newPower, t);
		if (newPower != oldPower) {
			this.consumptionHasChanged = true;
		}

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");
	}

	public Value<Double> getCurrentWaterLevel() {
		return this.currentWaterLevel;
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.currentState = CoffeeMachineState.OFF;
		this.currentMode = CoffeeMachineMode.NORMAL;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean useFixpointInitialiseVariables() {
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		Pair<Integer, Integer> ret = null;

		if (!this.currentIntensity.isInitialised() || !this.currentHeatingPower.isInitialised() || !this.currentWaterLevel.isInitialised()) {
			// initially, the heater is off, so its consumption is zero.
			this.currentIntensity.initialise(0.0);
			this.currentHeatingPower.initialise(CoffeeMachine.HIGH_POWER_IN_WATTS.getData());
			this.currentWaterLevel.initialise(0.0);

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

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI> output() {
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration timeAdvance() {
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

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();

		if (this.currentState == CoffeeMachineState.ON) {
			switch (this.currentMode) {
			case NORMAL:
				this.currentIntensity.setNewValue(this.normalModeConsumption / CoffeeMachine.VOLTAGE.getData(), t);
				break;
			case ECO:
				this.currentIntensity.setNewValue(this.ecoModeConsumption / CoffeeMachine.VOLTAGE.getData(), t);
				break;
			case MAX:
				this.currentIntensity.setNewValue(this.maxModeConsumption / CoffeeMachine.VOLTAGE.getData(), t);
				break;
			case SUSPEND:
				this.currentIntensity.setNewValue(this.suspendedModeConsumption / CoffeeMachine.VOLTAGE.getData(), t);
				break;
			}
		} else if (this.currentState == CoffeeMachineState.HEATING) {
			this.currentIntensity.setNewValue(
					this.currentHeatingPower.getValue() / CoffeeMachine.VOLTAGE.getData(), t);
		} else {
			assert this.currentState == CoffeeMachineState.OFF;
			this.currentIntensity.setNewValue(0.0, t);
		}

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("new consumption: ");
			sb.append(this.currentIntensity.getValue());
			sb.append(" ");
			sb.append(ElectricMeterImplementationI.POWER_UNIT);
			sb.append(" at ");
			sb.append(this.currentIntensity.getTime());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		super.userDefinedExternalTransition(elapsedTime);

		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the heater model, there will be exactly one by
		// construction.
		assert currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert ce instanceof CoffeeMachineEventI;

		// compute the total consumption for the simulation report.
		this.totalConsumption += Electricity.computeConsumption(elapsedTime,
				CoffeeMachine.VOLTAGE.getData() * this.currentIntensity.getValue());

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("execute the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		// the next call will update the current state of the heater and if
		// this state has changed, it put the boolean consumptionHasChanged
		// at true, which in turn will trigger an immediate internal transition
		// to update the current intensity of the heater electricity
		// consumption.
		ce.executeOn(this);

		assert CoffeeMachineElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.implementationInvariants(this)");
		assert CoffeeMachineElectricityModel.invariants(this)
				: new NeoSim4JavaException("CoffeeMachineElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void endSimulation(Time endTime) {
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption += Electricity.computeConsumption(d,
				CoffeeMachine.VOLTAGE.getData() * this.currentIntensity.getValue());

		if (VERBOSE) {
			this.logMessage("simulation ends.");
			this.logMessage(new CoffeeMachineElectricityReport(this.uri, this.totalConsumption).printout(" "));
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
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * <strong>White-box Invariant</strong>
	 * </p>
	 * 
	 * <pre>
	 * invariant	{@code
	 * true
	 * }	// no more invariant
	 * </pre>
	 * 
	 * <p>
	 * <strong>Black-box Invariant</strong>
	 * </p>
	 * 
	 * <pre>
	 * invariant	{@code
	 * true
	 * }	// no more invariant
	 * </pre>
	 * 
	 * <p>
	 * Created on : 2023-09-29
	 * </p>
	 * 
	 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class CoffeeMachineElectricityReport implements SimulationReportI, GlobalReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalConsumption; // in kwh

		public CoffeeMachineElectricityReport(String modelURI, double totalConsumption) {
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
		}

		@Override
		public String getModelURI() {
			return this.modelURI;
		}

		@Override
		public String printout(String indent) {
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
	public SimulationReportI getFinalReport() {
		return new CoffeeMachineElectricityReport(this.getURI(), this.totalConsumption);
	}
}
// -----------------------------------------------------------------------------
