package etape3.equipements.laundry.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;
import etape2.equipments.laundry.mil.LaundryOperationI;
import etape2.equipments.laundry.mil.LaundrySimulationConfigurationI;
import etape2.equipments.laundry.mil.events.CancelWash;
import etape2.equipments.laundry.mil.events.LaundryEventI;
import etape2.equipments.laundry.mil.events.SetColorModeLaundry;
import etape2.equipments.laundry.mil.events.SetDelicateModeLaundry;
import etape2.equipments.laundry.mil.events.SetIntensiveModeLaundry;
import etape2.equipments.laundry.mil.events.SetPowerLaundry;
import etape2.equipments.laundry.mil.events.SetSpinSpeed;
import etape2.equipments.laundry.mil.events.SetWashTemperature;
import etape2.equipments.laundry.mil.events.SetWhiteModeLaundry;
import etape2.equipments.laundry.mil.events.StartWash;
import etape2.equipments.laundry.mil.events.SwitchOffLaundry;
import etape2.equipments.laundry.mil.events.SwitchOnLaundry;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryStateSILModel</code> defines a simulation model
 * tracking the state changes on a laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The model receives events from the laundry component (corresponding to calls
 * to operations on the laundry in this component), keeps track of the current
 * state of the laundry in the simulation and then emits the received events
 * again towards another model simulating the electricity consumption of the
 * laundry given its current operating state.
 * </p>
 * <p>
 * This model becomes necessary in a SIL simulation because the electricity
 * model must be put in the electric meter component to share variables with
 * other electricity models, so this state model will serve as a bridge between
 * the models put in the laundry component and its electricity model put in the
 * electric meter component.
 * </p>
 *
 * <ul>
 * <li>Imported events: {@code SwitchOnLaundry}, {@code SwitchOffLaundry},
 * {@code StartWash}, {@code CancelWash}, {@code SetDelicateModeLaundry},
 * {@code SetColorModeLaundry}, {@code SetWhiteModeLaundry},
 * {@code SetIntensiveModeLaundry}, {@code SetWashTemperature},
 * {@code SetSpinSpeed}, {@code SetPowerLaundry}</li>
 * <li>Exported events: same as imported</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables: none</li>
 * </ul>
 *
 * <p>Created on : 2026-02-06</p>
 */
// -----------------------------------------------------------------------------
@ModelExternalEvents(
	imported = {
		SwitchOnLaundry.class, SwitchOffLaundry.class,
		StartWash.class, CancelWash.class,
		SetDelicateModeLaundry.class, SetColorModeLaundry.class,
		SetWhiteModeLaundry.class, SetIntensiveModeLaundry.class,
		SetWashTemperature.class, SetSpinSpeed.class,
		SetPowerLaundry.class
	},
	exported = {
		SwitchOnLaundry.class, SwitchOffLaundry.class,
		StartWash.class, CancelWash.class,
		SetDelicateModeLaundry.class, SetColorModeLaundry.class,
		SetWhiteModeLaundry.class, SetIntensiveModeLaundry.class,
		SetWashTemperature.class, SetSpinSpeed.class,
		SetPowerLaundry.class
	}
)
// -----------------------------------------------------------------------------
public class LaundryStateSILModel
extends		AtomicModel
implements	LaundryOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model. */
	public static boolean VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model. */
	public static boolean DEBUG = false;

	/** URI for an instance model; works as long as only one instance is
	 *  created. */
	public static final String URI = LaundryStateSILModel.class.getSimpleName();

	/** current state of the laundry machine. */
	protected LaundryState currentState = LaundryState.OFF;
	/** current wash mode of the laundry machine. */
	protected LaundryWashMode currentWashMode = LaundryWashMode.COLOR;
	/** current spin speed of the laundry machine. */
	protected SpinSpeed currentSpinSpeed = SpinSpeed.RPM_1000;
	/** last received event or null if none. */
	protected EventI lastReceived;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				LaundryStateSILModel.class,
				"URI != null && !URI.isEmpty()");
		return ret;
	}

	protected static boolean invariants(LaundryStateSILModel instance) {
		assert instance != null :
			new NeoSim4JavaException("Precondition violation: instance != null");
		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public LaundryStateSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		if (VERBOSE || DEBUG) {
			this.getSimulationEngine().setLogger(new StandardLogger());
		}

		assert LaundryStateSILModel.invariants(this) :
			new NeoSim4JavaException("LaundryStateSILModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods from LaundryOperationI
	// -------------------------------------------------------------------------

	@Override
	public void setState(LaundryState s) {
		this.currentState = s;
	}

	@Override
	public LaundryState getState() {
		return this.currentState;
	}

	@Override
	public void setWashMode(LaundryWashMode m) {
		this.currentWashMode = m;
	}

	@Override
	public LaundryWashMode getWashMode() {
		return this.currentWashMode;
	}

	@Override
	public void setStateMode(LaundryState s, LaundryWashMode m) {
		this.currentState = s;
		this.currentWashMode = m;
	}

	@Override
	public void setCurrentWashingPower(double newPower, Time t) {
		// nothing to do in the state model
	}

	@Override
	public void setCurrentWaterLevel(double newLevel, Time t) {
		// nothing to do in the state model
	}

	@Override
	public void setCurrentWashTemperature(double newTemp, Time t) {
		// nothing to do in the state model
	}

	@Override
	public void setSpinSpeed(SpinSpeed speed) {
		this.currentSpinSpeed = speed;
	}

	@Override
	public SpinSpeed getSpinSpeed() {
		return this.currentSpinSpeed;
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.lastReceived = null;
		this.currentState = LaundryState.OFF;
		this.currentWashMode = LaundryWashMode.COLOR;
		this.currentSpinSpeed = SpinSpeed.RPM_1000;

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}
	}

	@Override
	public ArrayList<EventI> output() {
		assert this.lastReceived != null :
			new NeoSim4JavaException("lastReceived != null");

		ArrayList<EventI> ret = new ArrayList<EventI>();
		ret.add(this.lastReceived);
		this.lastReceived = null;
		return ret;
	}

	@Override
	public Duration timeAdvance() {
		if (this.lastReceived != null) {
			return Duration.zero(this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert currentEvents != null && currentEvents.size() == 1 :
			new NeoSim4JavaException(
					"currentEvents != null && currentEvents.size() == 1");

		this.lastReceived = currentEvents.get(0);

		if (VERBOSE) {
			StringBuffer message = new StringBuffer(this.uri);
			message.append(" executes the external event ");
			message.append(this.lastReceived);
			this.logMessage(message.toString());
		}
	}

	@Override
	public void endSimulation(Time endTime) {
		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	@Override
	public void setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(
				AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			this.getSimulationEngine().setLogger(
					AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
}
// -----------------------------------------------------------------------------
