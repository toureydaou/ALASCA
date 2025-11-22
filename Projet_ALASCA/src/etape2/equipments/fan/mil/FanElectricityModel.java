package etape2.equipments.fan.mil;

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

import etape1.equipements.fan.Fan;
import etape1.equipements.fan.interfaces.FanImplementationI.FanMode;
import etape1.equipements.fan.interfaces.FanImplementationI.FanState;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape2.GlobalReportI;
import etape2.equipments.fan.mil.events.FanEventI;
import etape2.equipments.fan.mil.events.SetHighModeFan;
import etape2.equipments.fan.mil.events.SetLowModeFan;
import etape2.equipments.fan.mil.events.SetMediumModeFan;
import etape2.equipments.fan.mil.events.SwitchOffFan;
import etape2.equipments.fan.mil.events.SwitchOnFan;
import etape2.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>FanElectricityModel</code> defines a simulation
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
 * external events ({@code SwitchOnFan},
 * {@code SwitchOffFan}, {@code Heat} and {@code DoNotHeat}). The
 * power level is set through the external event {@code SetPowerFan}
 * that has a parameter defining the required power level. The electric power
 * consumption is stored in the exported variable {@code currentIntensity}.
 * </p>
 * <p>
 * Initially, the mode is in state {@code State.OFF} and the electric power
 * consumption at 0.0.
 * </p>
 * 
 * <ul>
 * <li>Imported events: {@code SwitchOnFan},
 * {@code SwitchOffFan}, {@code SetPowerFan}, {@code Heat},
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
@ModelExternalEvents(imported = { SwitchOnFan.class, SwitchOffFan.class,
		SetHighModeFan.class, SetMediumModeFan.class, SetLowModeFan.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentHeatingPower", type = Double.class)
@ModelExportedVariable(name = "currentWaterLevel", type = Double.class)
//-----------------------------------------------------------------------------
public class FanElectricityModel extends AtomicHIOA {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created. */
	public static final String URI = FanElectricityModel.class.getSimpleName();
	/** when true, leaves a trace of the execution of the model. */
	public static boolean VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model. */
	public static boolean DEBUG = false;

	/** current state of the heater. */
	protected FanState currentState = FanState.OFF;
	protected FanMode currentMode = FanMode.LOW;

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
	protected double lowModeConsumption;
	/**
	 * power consumption in the HIGH mode in the power unit defined by the hair
	 * dryer.
	 */
	protected double mediumModeConsumption;

	/**
	 * power consumption in the HIGH mode in the power unit defined by the hair
	 * dryer.
	 */
	protected double highModeConsumption;

	

	/** total consumption of the heater during the simulation in kwh. */
	protected double totalConsumption;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	
	/** current intensity in the power unit used by the electric meter. */
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentIntensity = new Value<Double>(this);


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
	protected static boolean implementationInvariants(FanElectricityModel instance) {
		assert instance != null : new NeoSim4JavaException("Precondition violation: " + "instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(instance.currentState != null,
				FanElectricityModel.class, instance, "currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(instance.totalConsumption >= 0.0,
				FanElectricityModel.class, instance, "totalConsumption >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentIntensity.isInitialised() || instance.currentIntensity.getValue() >= 0.0,
				FanElectricityModel.class, instance,
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
		// ret &= FanSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(URI != null && !URI.isEmpty(),
				FanElectricityModel.class, "URI != null && !URI.isEmpty()");
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
	protected static boolean invariants(FanElectricityModel instance) {
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
	public FanElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine)
			throws Exception {

		super(uri, simulatedTimeUnit, simulationEngine);

		this.lowModeConsumption = Fan.LOW_POWER_IN_WATTS.getData();
		this.mediumModeConsumption = Fan.MEDIUM_POWER_IN_WATTS.getData();
		this.highModeConsumption = Fan.HIGH_POWER_IN_WATTS.getData();
		
		

		this.getSimulationEngine().setLogger(new StandardLogger());

		assert FanElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("FanElectricityModel.implementationInvariants(this)");
		assert FanElectricityModel.invariants(this)
				: new NeoSim4JavaException("FanElectricityModel.invariants(this)");
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
	public void setState(FanState s, Time t) {
		FanState old = this.currentState;
		this.currentState = s;
		if (old != s) {
			this.consumptionHasChanged = true;
		}

		assert FanElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("FanElectricityModel.implementationInvariants(this)");
		assert FanElectricityModel.invariants(this)
				: new NeoSim4JavaException("FanElectricityModel.invariants(this)");
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
	public FanState getState() {
		return this.currentState;
	}

	public void setStateMode(FanState s, FanMode m) {
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
	public FanMode getMode() {
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


	

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.currentState = FanState.OFF;
		this.currentMode = FanMode.LOW;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}

		assert FanElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("FanElectricityModel.implementationInvariants(this)");
		assert FanElectricityModel.invariants(this)
				: new NeoSim4JavaException("FanElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#initialiseVariables()
	 */
	@Override
	public void			initialiseVariables()
	{
		super.initialiseVariables();

		// initially, the fan is off, so its consumption is zero.
		this.currentIntensity.initialise(0.0);

		assert	FanElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HairDryerElectricityModel.implementationInvariants("
						+ "this)");
		assert	FanElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HairDryerElectricityModel.invariants(this)");
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

		assert FanElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("FanElectricityModel.implementationInvariants(this)");
		assert FanElectricityModel.invariants(this)
				: new NeoSim4JavaException("FanElectricityModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();

		if (this.currentState == FanState.ON) {
			switch (this.currentMode) {
			case LOW:
				this.currentIntensity.setNewValue(this.lowModeConsumption / Fan.VOLTAGE.getData(), t);
				break;
			case MEDIUM:
				this.currentIntensity.setNewValue(this.mediumModeConsumption / Fan.VOLTAGE.getData(), t);
				break;
			case HIGH:
				this.currentIntensity.setNewValue(this.highModeConsumption / Fan.VOLTAGE.getData(), t);
				break;
			
			}
		} else {
			this.currentIntensity.setNewValue(0.0, t);
		}

		if (VERBOSE) {
			StringBuffer message =
					new StringBuffer("executes an internal transition ");
			message.append("with current consumption ");
			message.append(this.currentIntensity.getValue());
			message.append(" ");
			message.append(ElectricMeterImplementationI.POWER_UNIT);
			message.append(" at ");
			message.append(this.currentIntensity.getTime());
			this.logMessage(message.toString());
		}

		assert FanElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("FanElectricityModel.implementationInvariants(this)");
		assert FanElectricityModel.invariants(this)
				: new NeoSim4JavaException("FanElectricityModel.invariants(this)");
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
		assert ce instanceof FanEventI;

		// compute the total consumption for the simulation report.
		this.totalConsumption += Electricity.computeConsumption(elapsedTime,
				Fan.VOLTAGE.getData() * this.currentIntensity.getValue());

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

		assert FanElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("FanElectricityModel.implementationInvariants(this)");
		assert FanElectricityModel.invariants(this)
				: new NeoSim4JavaException("FanElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void endSimulation(Time endTime) {
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption += Electricity.computeConsumption(d,
				Fan.VOLTAGE.getData() * this.currentIntensity.getValue());

		if (VERBOSE) {
			this.logMessage("simulation ends.");
			this.logMessage(new FanElectricityReport(this.uri, this.totalConsumption).printout(" "));
		}
		super.endSimulation(endTime);
	}
	
	// -------------------------------------------------------------------------
		// Optional DEVS simulation protocol: simulation run parameters
		// -------------------------------------------------------------------------

		/** run parameter name for {@code LOW_MODE_CONSUMPTION}.				*/
		public static final String		LOW_MODE_CONSUMPTION_RPNAME =
													URI + ":LOW_MODE_CONSUMPTION";
		
		/** run parameter name for {@code MEDIUM_MODE_CONSUMPTION}.				*/
		public static final String		MEDIUM_MODE_CONSUMPTION_RPNAME =
													URI + ":MEDIUM_MODE_CONSUMPTION";
		/** run parameter name for {@code HIGH_MODE_CONSUMPTION}.				*/
		public static final String		HIGH_MODE_CONSUMPTION_RPNAME =
													URI + ":HIGH_MODE_CONSUMPTION";
		/** run parameter name for {@code TENSION}.								*/
		public static final String		TENSION_RPNAME = URI + ":TENSION";

		/**
		 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(Map)
		 */
		@Override
		public void			setSimulationRunParameters(
			Map<String, Object> simParams
			) throws MissingRunParameterException
		{
			super.setSimulationRunParameters(simParams);

			String lowName =
				ModelI.createRunParameterName(getURI(),
											  LOW_MODE_CONSUMPTION_RPNAME);
			if (simParams.containsKey(lowName)) {
				this.lowModeConsumption = (double) simParams.get(lowName);
			}
			String mediumName =
					ModelI.createRunParameterName(getURI(),
												  HIGH_MODE_CONSUMPTION_RPNAME);
				if (simParams.containsKey(mediumName)) {
					this.highModeConsumption = (double) simParams.get(mediumName);
				}
			String highName =
				ModelI.createRunParameterName(getURI(),
											  HIGH_MODE_CONSUMPTION_RPNAME);
			if (simParams.containsKey(highName)) {
				this.highModeConsumption = (double) simParams.get(highName);
			}

			assert	FanElectricityModel.implementationInvariants(this) :
					new NeoSim4JavaException(
							"HairDryerElectricityModel.implementationInvariants("
							+ "this)");
			assert	FanElectricityModel.invariants(this) :
					new NeoSim4JavaException(
							"HairDryerElectricityModel.invariants(this)");
		}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>FanElectricityReport</code> implements the
	 * simulation report for the <code>FanElectricityModel</code>.
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
	public static class FanElectricityReport implements SimulationReportI, GlobalReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalConsumption; // in kwh

		public FanElectricityReport(String modelURI, double totalConsumption) {
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
		return new FanElectricityReport(this.getURI(), this.totalConsumption);
	}
}
// -----------------------------------------------------------------------------
