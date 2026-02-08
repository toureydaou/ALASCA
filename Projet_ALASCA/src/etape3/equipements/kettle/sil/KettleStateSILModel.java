package etape3.equipements.kettle.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape2.equipments.kettle.mil.KettleOperationI;
import etape2.equipments.kettle.mil.KettleSimulationConfigurationI;
import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.KettleEventI;
import etape2.equipments.kettle.mil.events.SetEcoModeKettle;
import etape2.equipments.kettle.mil.events.SetMaxModeKettle;
import etape2.equipments.kettle.mil.events.SetNormalModeKettle;
import etape2.equipments.kettle.mil.events.SetPowerKettle;
import etape2.equipments.kettle.mil.events.SetSuspendedModeKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
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
 * The class <code>KettleStateSILModel</code> defines a simulation model
 * tracking the state changes on a kettle (water heater).
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The model receives events from the kettle component (corresponding to calls
 * to operations on the kettle in this component), keeps track of the current
 * state of the kettle in the simulation and then emits the received events
 * again towards another model simulating the electricity consumption of the
 * kettle given its current operating state.
 * </p>
 * <p>
 * This model becomes necessary in a SIL simulation because the electricity
 * model must be put in the electric meter component to share variables with
 * other electricity models, so this state model will serve as a bridge between
 * the models put in the kettle component and its electricity model put in the
 * electric meter component.
 * </p>
 *
 * <ul>
 * <li>Imported events: {@code SwitchOnKettle}, {@code SwitchOffKettle},
 * {@code HeatKettle}, {@code DoNotHeatKettle}, {@code SetEcoModeKettle},
 * {@code SetNormalModeKettle}, {@code SetMaxModeKettle},
 * {@code SetSuspendedModeKettle}, {@code SetPowerKettle}</li>
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
		SwitchOnKettle.class, SwitchOffKettle.class,
		HeatKettle.class, DoNotHeatKettle.class,
		SetEcoModeKettle.class, SetNormalModeKettle.class,
		SetMaxModeKettle.class, SetSuspendedModeKettle.class,
		SetPowerKettle.class
	},
	exported = {
		SwitchOnKettle.class, SwitchOffKettle.class,
		HeatKettle.class, DoNotHeatKettle.class,
		SetEcoModeKettle.class, SetNormalModeKettle.class,
		SetMaxModeKettle.class, SetSuspendedModeKettle.class,
		SetPowerKettle.class
	}
)
// -----------------------------------------------------------------------------
public class KettleStateSILModel
extends		AtomicModel
implements	KettleOperationI
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
	public static final String URI = KettleStateSILModel.class.getSimpleName();

	/** current state of the kettle. */
	protected KettleState currentState = KettleState.OFF;
	/** current mode of the kettle. */
	protected KettleMode currentMode = KettleMode.NORMAL;
	/** last received event or null if none. */
	protected EventI lastReceived;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= KettleSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				KettleStateSILModel.class,
				"URI != null && !URI.isEmpty()");
		return ret;
	}

	protected static boolean invariants(KettleStateSILModel instance) {
		assert instance != null :
			new NeoSim4JavaException("Precondition violation: instance != null");
		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public KettleStateSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		if (VERBOSE || DEBUG) {
			this.getSimulationEngine().setLogger(new StandardLogger());
		}

		assert KettleStateSILModel.invariants(this) :
			new NeoSim4JavaException("KettleStateSILModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods from KettleOperationI
	// -------------------------------------------------------------------------

	@Override
	public void setState(KettleState s) {
		this.currentState = s;
	}

	@Override
	public KettleState getState() {
		return this.currentState;
	}

	@Override
	public void setMode(KettleMode m) {
		this.currentMode = m;
	}

	@Override
	public KettleMode getMode() {
		return this.currentMode;
	}

	@Override
	public void setCurrentHeatingPower(double newPower, Time t) {
		// nothing to do in the state model
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.lastReceived = null;
		this.currentState = KettleState.OFF;
		this.currentMode = KettleMode.NORMAL;

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
