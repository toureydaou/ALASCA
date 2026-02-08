package etape4.equipments.batteries.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
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

import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Map;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import etape1.equipments.batteries.Batteries.State;
import etape2.equipments.batteries.mil.BatteriesSimulationConfiguration;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import etape4.equipments.batteries.BatteriesCyPhy;
import etape4.equipments.batteries.sil.events.CurrentBatteriesLevel;
import etape4.equipments.batteries.sil.events.SIL_StartCharging;
import etape4.equipments.batteries.sil.events.SIL_StopCharging;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesStateSILModel</code> implements a simulation model
 * managing the state for the batteries.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In the batteries SIL simulation, as the batteries model simulating the power
 * consumption and production must be co-located with the electric meter models,
 * this model will be executed within the batteries component to bridge the gap
 * between the batteries component code that may change the state and the power
 * model that must keep track of these changes. This state model receives events
 * like starting and stopping the charge of the batteries, update its own state
 * tracking and resends the events to the batteries power consumption and
 * production model.
 * </p>
 * <p>
 * The batteries power consumption and production model also keeps track of the
 * charge level of the batteries, which is also an information that the
 * batteries component code needs to access. Because the component code cannot
 * access an information kept in a simulation model held by another component,
 * the charge level must be sent back to this state model in order to be
 * accessible within the batteries component. To do so, a new event called
 * {@code CurrentBatteriesLevel} is added that is exported by the power model
 * and imported by the state model. Each time the power model considers that a
 * significant change in the charge level must be forwarded to the state model,
 * it sends a {@code CurrentBatteriesLevel}, which is received and processed by
 * the state model.
 * </p>
 * <p>
 * See {@code BatteriesPowerSILModel}.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code SIL_StartCharging},
 *   {@code SIL_StopCharging},
 *   {@code CurrentBatteriesLevel}</li>
 * <li>Exported events:
 *   {@code SIL_StartCharging},
 *   {@code SIL_StopCharging}</li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-12-30</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
	imported = {SIL_StartCharging.class, SIL_StopCharging.class,
				CurrentBatteriesLevel.class},
	exported = {SIL_StartCharging.class, SIL_StopCharging.class})
//-----------------------------------------------------------------------------
public class			BatteriesStateSILModel
extends		AtomicModel
implements	BatteriesStateI,
			ModelStateAccessI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>EventToBeEmittedNext</code> defines the action to
	 * be taken when the batteries has just become empty or when they are
	 * available again after they were empty.
	 * 
	 * <p>Created on : 2025-10-24</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static enum	EventToBeEmittedNext
	{
		START_CHARGING,
		STOP_CHARGING;
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = true;

	/** single model URI.													*/
	public static final String	URI =
									BatteriesStateSILModel.class.getSimpleName();

	/** current state of the batteries.										*/
	protected State					currentState;
	/** current level of the batteries in
	 *  {@code MeasurementUnit.WATT_HOURS}.									*/
	protected double				currentLevelRatio;
	/** the event to be emitted at the next output or null if none.			*/
	protected EventToBeEmittedNext	toBeEmitted;

	protected BatteriesCyPhy		owner;

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
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticImplementationInvariants()
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
		BatteriesStateSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= BatteriesStateSILModel.staticImplementationInvariants();
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
		ret &= BatteriesSimulationConfiguration.staticInvariants();
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
		BatteriesStateSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= BatteriesStateSILModel.staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic model with the given URI (if null, one will be
	 * generated) and to be run by the given simulator using the given time unit
	 * for its clock.
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
	 * @param uri				unique identifier of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation clock.
	 * @param simulationEngine	simulation engine enacting the model.
	 */
	public				BatteriesStateSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert	BatteriesStateSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriesStateSILModel.implementationInvariants(this)");
		assert	BatteriesStateSILModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: BatteriesStateSILModel."
						+ "invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		String initialLevelName =
				ModelI.createRunParameterName(
							this.getURI(),
							BatteriesPowerSILModel.INITIAL_LEVEL_RATIO_RP_NAME);

		assert	simParams != null :
				new MissingRunParameterException("simParams != null");

		if (simParams.containsKey(initialLevelName)) {
			this.currentLevelRatio = (double) simParams.get(initialLevelName);
		} else {
			// Default to 50% charge level when parameter not provided
			// (e.g., in integration test mode where GlobalSupervisor
			// doesn't set equipment-specific run parameters)
			this.currentLevelRatio = 0.5;
		}

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
			this.owner =
					(BatteriesCyPhy) simParams.get(
							AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME);
		}

		// tracing
		if (DEBUG) {
			this.logMessage("setSimulationRunParameters done!");
		}
	}

	/**
	 * @see etape4.equipments.batteries.sil.BatteriesStateI#getBatteriesState()
	 */
	@Override
	public State		getBatteriesState()
	{
		return this.currentState;
	}

	/**
	 * @see etape4.equipments.batteries.sil.BatteriesStateI#setBatteriesState(etape1.equipments.batteries.Batteries.State)
	 */
	@Override
	public void			setBatteriesState(State newState)
	{
		State old = this.currentState;
		if (!old.equals(newState)) {
			if (newState.equals(State.CHARGING)) {
				this.toBeEmitted = EventToBeEmittedNext.START_CHARGING;
			} else {
				if (old.equals(State.CHARGING))
				this.toBeEmitted = EventToBeEmittedNext.STOP_CHARGING;
			}
			if (this.owner != null) {
				// this is true only when performing SIL simulations with
				// the owner component code executing at the same time
				this.owner.notifyState(newState);
			}
		}
		this.currentState = newState;
	}

	/**
	 * return the current batteries level as known by this model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current batteries level as known by this model.
	 */
	public double		getCurrentLevel()
	{
		return this.currentLevelRatio;
	}

	/**
	 * set the current level of the batteries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code level >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param level	the current level of the batteries.
	 */
	public void			setCurrentLevel(double level)
	{
		this.currentLevelRatio = level;
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI#getModelStateValue(java.lang.String, java.lang.String)
	 */
	@Override
	public Double		getModelStateValue(String modelURI, String name)
	throws Exception
	{
		// Preconditions checking
		assert	getURI().equals(modelURI) :
				new NeoSim4JavaException(
						"Precondition violation: getURI().equals(modelURI)");
		assert	BatteriesCyPhy.CHARGE_LEVEL.equals(name) :
				new NeoSim4JavaException(
						"BatteriesCyPhy.CHARGE_LEVEL.equals(name)");

		return (Double)this.currentLevelRatio;
	}

	/**
	 * initialise the model.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The model is initialised so that the batteries are in IDLE state.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code State.IDLE.equals(getBatteriesState())}
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.currentState = State.IDLE;
		this.toBeEmitted = null;

		// Invariant checking
		assert	BatteriesStateSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriesStateSILModel.implementationInvariants(this)");
		assert	BatteriesStateSILModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: "
						+ "BatteriesStateSILModel.invariants(this)");

		super.initialiseState(initialTime);

		// tracing
		if (DEBUG) {
			this.logMessage("BatteriesPowerModel::initialiseState "
					+ "currentState = " + this.currentState);
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		Duration ret = null;
		if (this.toBeEmitted != null) {
			ret = Duration.zero(this.getSimulatedTimeUnit());
		} else {
			ret = Duration.INFINITY;
		}

		// tracing
		if (DEBUG) {
			this.logMessage("timeAdvance in state " + this.currentState +
							" returns " + ret);
		}

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		if (this.toBeEmitted != null) {
			ArrayList<EventI> ret = new ArrayList<EventI>();
			if (toBeEmitted.equals(EventToBeEmittedNext.START_CHARGING)) {
				ret.add(new SIL_StartCharging(this.getTimeOfNextEvent()));
			} else {
				ret.add(new SIL_StopCharging(this.getTimeOfNextEvent()));
			}
			this.toBeEmitted = null;
			
			// tracing
			if (DEBUG) {
				this.logMessage("output " + ret);
			}

			return ret;
		} else {
			return super.output();
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// tracing
		if (VERBOSE || DEBUG) {
			this.logMessage(
					"userDefinedInternalTransition at "
					+ this.getCurrentStateTime()
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevelRatio);
		}

		// Invariant checking
		assert	BatteriesStateSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriesStateSILModel.implementationInvariants(this)");
		assert	BatteriesStateSILModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: BatteriesStateSILModel."
						+ "invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void		userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		EventI e = this.getStoredEventAndReset().remove(0);

		// execute the event, which will change the state of the batteries
		e.executeOn(this);

		// tracing
		if (VERBOSE || DEBUG) {
			this.logMessage(
					"userDefinedExternalTransition at "
					+ this.getCurrentStateTime()
					+ " on event " + e
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevelRatio);
		}
	}
}
// -----------------------------------------------------------------------------
