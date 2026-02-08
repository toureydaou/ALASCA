package etape4.equipments.generator.sil;

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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import etape1.equipments.generator.GeneratorImplementationI.State;
import etape2.GlobalSimulationConfigurationI;
import etape2.equipments.generator.mil.GeneratorPowerModel;
import etape2.equipments.generator.mil.GeneratorSimulationConfiguration;
import etape2.equipments.generator.mil.GeneratorStateManagementI;
import etape2.equipments.generator.mil.TankLevelManagementI;
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import etape4.equipments.generator.sil.events.CurrentFuelConsumption;
import etape4.equipments.generator.sil.events.CurrentFuelLevel;
import etape4.equipments.generator.sil.events.CurrentPowerProduction;
import etape4.equipments.generator.sil.events.SIL_Refill;
import etape4.equipments.generator.sil.events.TimedPhysicalMeasure;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorStateSILModel</code> implements the simulation model
 * tracking the state of the generator.
 *
 * <p><strong>Description</strong></p>
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
 * invariant	{@code TimeUnit.HOURS.equals(GlobalSimulationConfigurationI.TIME_UNIT)}
 * </pre>
 * 
 * <p>Created on : 2026-01-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
	imported = {Start.class, Stop.class, SIL_Refill.class,
				TankEmpty.class, TankNoLongerEmpty.class,
				CurrentPowerProduction.class, CurrentFuelLevel.class,
				CurrentFuelConsumption.class},
	exported = {Start.class, Stop.class, SIL_Refill.class})
//-----------------------------------------------------------------------------
public class			GeneratorStateSILModel
extends		AtomicModel
implements	TankLevelManagementI,
			GeneratorStateManagementI,
			ModelStateAccessI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean			VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean			DEBUG = true;

	/** single model URI.													*/
	public static final String		URI =
									GeneratorStateSILModel.class.getSimpleName();

	/** current state of the generator.										*/
	protected State					currentState;
	/** event to be emitted towards the other models.						*/
	protected EventI				toBeEmitted;
	/** most recent value of power production.								*/
	protected TimedPhysicalMeasure	currentPowerProduction;
	/** most recent value of fuel level.									*/
	protected TimedPhysicalMeasure	currentFuelLevel;
	/** most recent value of fuel consumption.								*/
	protected TimedPhysicalMeasure	currentFuelConsumption;

	/** name to be used to retrieve the generator state using the
	 *  {@code ModelStateAccessI} protocol.									*/
	public static final String		STATE_VALUE_NAME = "state";
	/** name to be used to retrieve the current generator power production
	 *  using the {@code ModelStateAccessI} protocol.						*/
	public static final String		POWER_PRODUCTION_VALUE_NAME =
															"power-production";
	/** name to be used to retrieve the current generator fuel level
	 *  using the {@code ModelStateAccessI} protocol.						*/
	public static final String		FUEL_LEVEL_VALUE_NAME = "fuel-level";
	/** name to be used to retrieve the current generator fuel consumption
	 *  using the {@code ModelStateAccessI} protocol.						*/
	public static final String		FUEL_CONSUMPTION_VALUE_NAME =
															"fuel-consumption";

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
	 * @return		true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticImplementationInvariants() {
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
		GeneratorStateSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= GeneratorStateSILModel.staticImplementationInvariants();
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
	 * @return		true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants() {
		boolean ret = true;
		ret &= GeneratorSimulationConfiguration.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				TimeUnit.HOURS.equals(GlobalSimulationConfigurationI.TIME_UNIT),
				GeneratorPowerModel.class,
				"TimeUnit.HOURS.equals(GlobalSimulationConfigurationI.TIME_UNIT)");
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
	protected static boolean	invariants(GeneratorStateSILModel instance)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= GeneratorPowerModel.staticInvariants();
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
	public				GeneratorStateSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert	GeneratorStateSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation:"
						+ "GeneratorStateSILModel.implementationInvariants(this)");
		assert	GeneratorStateSILModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: "
						+ "GeneratorStateSILModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
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
	 * @see fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI#getModelStateValue(java.lang.String, java.lang.String)
	 */
	@Override
	public Object		getModelStateValue(String modelURI, String name)
	throws Exception
	{
		// Preconditions checking
		assert	this.uri.equals(modelURI) :
				new NeoSim4JavaException(
						"Precondition violation: getURI().equals(modelURI)");
		assert	STATE_VALUE_NAME.equals(name) ||
								POWER_PRODUCTION_VALUE_NAME.equals(name) ||
								FUEL_LEVEL_VALUE_NAME.equals(name) ||
								FUEL_CONSUMPTION_VALUE_NAME.equals(name) :
				new NeoSim4JavaException(
						"Precondition violation: STATE_VALUE_NAME.equals(name)"
						+ " || POWER_PRODUCTION_VALUE_NAME.equals(name)"
						+ " || FUEL_LEVEL_VALUE_NAME.equals(name)"
						+ " || FUEL_CONSUMPTION_VALUE_NAME.equals(name)");

		if (STATE_VALUE_NAME.equals(name)) {
			synchronized (this.currentState) {
				return this.currentState;
			}
		} else if (POWER_PRODUCTION_VALUE_NAME.equals(name)) {
			synchronized (this.currentPowerProduction) {
				return this.currentPowerProduction.clone();
			}
		} else if (FUEL_LEVEL_VALUE_NAME.equals(name)) {
			synchronized (this.currentFuelLevel) {
				return this.currentFuelLevel.clone();
			}
		} else {
			synchronized (this.currentFuelConsumption) {
				return this.currentFuelConsumption.clone();
			}
		}
	}

	/**
	 * @see etape2.equipments.generator.mil.GeneratorStateManagementI#getGeneratorState()
	 */
	@Override
	public State		getGeneratorState()
	{
		synchronized (this.currentState) {
			return this.currentState;
		}
	}

	/**
	 * @see etape2.equipments.generator.mil.GeneratorStateManagementI#setGeneratorState(etape1.equipments.generator.GeneratorImplementationI.State)
	 */
	@Override
	public void			setGeneratorState(State newState)
	{
		synchronized (this.currentState) {
			this.currentState = newState;
		}
		synchronized (this.currentFuelConsumption) {
			if (newState.equals(State.OFF)) {
				this.currentFuelConsumption =
					new TimedPhysicalMeasure(0.0,  this.getCurrentStateTime());
			}
		}
	}

	/**
	 * @see etape2.equipments.generator.mil.TankLevelManagementI#notTankEmpty()
	 */
	@Override
	public boolean		notTankEmpty()
	{
		synchronized (this.currentState) {
			return !this.currentState.equals(State.TANK_EMPTY);
		}
	}

	/**
	 * @see etape2.equipments.generator.mil.TankLevelManagementI#signalTankEmpty()
	 */
	@Override
	public void			signalTankEmpty()
	{
		synchronized (this.currentState) {
			if (!this.currentState.equals(State.OFF)) {
				this.currentState = State.TANK_EMPTY;
			}
		}
		synchronized (this.currentPowerProduction) {
			this.currentPowerProduction =
					new TimedPhysicalMeasure(0.0, this.getCurrentStateTime());
		}
	}

	/**
	 * @see etape2.equipments.generator.mil.TankLevelManagementI#signalTankNoLongerEmpty()
	 */
	@Override
	public void			signalTankNoLongerEmpty()
	{
		synchronized (this.currentState) {
			this.currentState = State.OFF;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.currentState = State.OFF;
		this.currentPowerProduction = new TimedPhysicalMeasure(0.0, initialTime);
		this.currentFuelLevel = new TimedPhysicalMeasure(0.0, initialTime);
		this.currentFuelConsumption = new TimedPhysicalMeasure(0.0, initialTime);
		
		super.initialiseState(initialTime);

		// tracing
		if (DEBUG) {
			this.logMessage("initialiseState currentState = "
							+ this.currentState);
		}
	}

	/**
	 * set the most recent value of power level.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code powerLevel != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param powerLevel	the most recent value of power level.
	 */
	public void			setNewPowerLevel(TimedPhysicalMeasure powerLevel)
	{
		assert	powerLevel != null :
				new NeoSim4JavaException(
						"Precondition violation: powerLevel != null");

		synchronized (this.currentPowerProduction) {
			this.currentPowerProduction = powerLevel;
		}

		if (powerLevel.getValue() > 0.0) {
			if (this.currentState.equals(State.IDLE)) {
				this.currentState = State.PRODUCING;
			} else {
				
			}
		} else {
			if (this.currentState.equals(State.PRODUCING)) {
				this.currentState = State.IDLE;
			} else {
				
			}
		}
	}

	/**
	 * set the most recent value of fuel level.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code fuelLevel != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param fuelLevel	the most recent value of fuel level.
	 */
	public void			setNewFuelLevel(TimedPhysicalMeasure fuelLevel)
	{
		assert	fuelLevel != null :
				new NeoSim4JavaException(
						"Precondition violation: powerLevel != null");

		synchronized (this.currentFuelLevel) {
			this.currentFuelLevel = fuelLevel;
		}
	}

	/**
	 * set the most recent value of fuel consumption.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code fuelLevel != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param fuelConsumption	the most recent value of fuel level.
	 */
	public void			setNewFuelConsumption(
		TimedPhysicalMeasure fuelConsumption
		)
	{
		assert	fuelConsumption != null :
				new NeoSim4JavaException(
						"Precondition violation: fuelConsumption != null");

		synchronized (this.currentFuelConsumption) {
			this.currentFuelConsumption = fuelConsumption;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		if (this.toBeEmitted == null) {
			return Duration.INFINITY;
		} else {
			return Duration.zero(this.getSimulatedTimeUnit());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		if (this.toBeEmitted != null) {
			ArrayList<EventI> ret = new ArrayList<>();
			ret.add(this.toBeEmitted);
			this.toBeEmitted = null;
			return ret;
		} else {
			return super.output();
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		for (int i = 0 ; i < currentEvents.size() ; i++) {
			EventI e = currentEvents.get(i);
			e.executeOn(this);
			if (e instanceof Start || e instanceof Stop
												|| e instanceof SIL_Refill) {
				this.toBeEmitted = e;
			}
		}

		// tracing
		if (DEBUG) {
			StringBuffer sb =
					new StringBuffer("userDefinedExternalTransition at ");
			sb.append(this.getCurrentStateTime());
			sb.append(" on events {");
			for (int i = 0 ; i < currentEvents.size() ; i++) {
				sb.append(currentEvents.get(i));
				if (i < currentEvents.size() -1) {
					sb.append(", ");
				}
			}
			sb.append("} giving currentState = ");
			sb.append(this.currentState);
			sb.append(", power = ");
			sb.append(this.currentPowerProduction);
			sb.append(", fuel level = ");
			sb.append(this.currentFuelLevel);
			sb.append(", fuel consumption = ");
			sb.append(this.currentFuelConsumption);
			sb.append("]");
			this.logMessage(sb.toString());
		} else if (VERBOSE) {
			StringBuffer sb =
					new StringBuffer("userDefinedExternalTransition at ");
			sb.append(this.getCurrentStateTime());
			sb.append("} giving [");
			sb.append(this.currentState);
			sb.append(", ");
			sb.append(this.currentPowerProduction.getValue());
			sb.append(", ");
			sb.append(this.currentFuelLevel.getValue());
			sb.append(", ");
			sb.append(this.currentFuelConsumption.getValue());
			sb.append("]");
			this.logMessage(sb.toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		// tracing
		if (VERBOSE) {
			this.logMessage(
					"simulation ending at " + endTime
					+ ": currentState = " + this.currentState);
		}

		super.endSimulation(endTime);
	}
}
// -----------------------------------------------------------------------------
