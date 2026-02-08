package etape2.equipments.generator.mil;



import java.util.ArrayList;
import java.util.Map;

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

import java.util.concurrent.TimeUnit;

import etape1.equipments.generator.Generator;
import etape1.equipments.generator.GeneratorImplementationI.State;
import etape2.GlobalSimulationConfigurationI;
import etape2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import etape2.equipments.generator.mil.events.Refill;
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorFuelModel</code> implements the simulation model
 * for the fuel consumption of the generator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code Start},
 *   {@code Stop},
 *   {@code Refill},
 *   {@code GeneratorRequiredPowerChanged}</li>
 * <li>Exported events:
 *   {@code TankEmpty},
 *   {@code TankNoLongerEmpty} </li>
 * <li>Imported variables:
 *   name = {@code generatorOutputPower}, type = {@code Double}</li>
 * <li>Exported variables: none</li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code !currentLevel.isInitialised() || (currentLevel.getValue() >= 0.0 && currentLevel.getValue() < maxCapacity)}
 * invariant	{@code !lastDerivative.isInitialised() || (lastDerivative.getValue() <= 0.0)}
 * invariant	{@code standardLevelQuantum > 0.0}
 * invariant	{@code Math.abs(currentLevelQuantum) >= 0.0 && Math.abs(currentLevelQuantum <= standardLevelQuantum)}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code TimeUnit.HOURS.equals(GlobalSimulationConfigurationI.TIME_UNIT)}
 * </pre>
 * 
 * <p>Created on : 2025-10-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		imported = {Start.class, Stop.class, Refill.class,
					GeneratorRequiredPowerChanged.class},
		exported = {TankEmpty.class, TankNoLongerEmpty.class})
@ModelImportedVariable(name = "generatorOutputPower", type = Double.class)
//-----------------------------------------------------------------------------
public class			GeneratorFuelModel
extends		AtomicHIOA
implements	GeneratorStateManagementI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>EventToBeEmittedNext</code> defines the action to
	 * be taken when the fuel tank has just become empty or when a refill of the
	 * tank has just been done.
	 * 
	 * <p>Created on : 2025-10-24</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	protected static enum	EventToBeEmittedNext
	{
		TANK_EMPTY,
		TANK_NO_LONGER_EMPTY;
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean			VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean			DEBUG = false;
	/** when comparing floating point values, use this tolerance to get
	 *  the result of the comparison.										*/
	protected static final double	TOLERANCE  = 1.0e-08;

	/** single model URI.													*/
	public static final String		URI = GeneratorFuelModel.class.getSimpleName();

	/**	name of the run parameter for the capacity of the tank in
	 *  {@code MeasurementUnit.LITERS}.										*/
	public static final String	CAPACITY_RP_NAME = "CAPACITY";
	/**	name of the run parameter for the initial fuel level of the tank
	 *  in {@code MeasurementUnit.LITERS}.									*/
	public static final String	INITIAL_LEVEL_RP_NAME = "INITIAL_LEVEL";
	/**	name of the run parameter for the minimum fuel consumption of the
	 *  generator in {@code MeasurementUnit.LITERS_PER_HOUR}.				*/
	public static final String	MIN_FUEL_CONSUMPTION_RP_NAME =
												"MIN_FUEL_CONSUMPTION_RP_NAME";
	/**	name of the run parameter for the maximum fuel consumption of the
	 *  generator in {@code MeasurementUnit.LITERS_PER_HOUR}.				*/
	public static final String	MAX_FUEL_CONSUMPTION_RP_NAME =
												"MAX_FUEL_CONSUMPTION_RP_NAME";
	/**	name of the run parameter for the maximum out power of the generator
	 *  in {@code MeasurementUnit.WATTS}.									*/
	public static final String	MAX_OUT_POWER_RP_NAME = "MAX_OUT_POWER_RP_NAME";
	/**	name of the run parameter for the initialisation of the
	 *  fuel level quantum in {@code MeasurementUnit.LITERS}.				*/
	public static final String	LEVEL_QUANTUM_RP_NAME = "LEVEL_QUANTUM";
	
	/** maximum capacity of the tank in {@code MeasurementUnit.LITERS}.		*/
	protected double	maxCapacity;
	/** initial fuel level of the tank in {@code MeasurementUnit.LITERS}.	*/
	protected double	initialLevel;
	/**	minimum fuel consumption of the generator in
	 *  {@code MeasurementUnit.LITERS_PER_HOUR}.							*/
	protected double	minFuelConsumption;
	/**	maximum fuel consumption of the generator in
	 *  {@code MeasurementUnit.LITERS_PER_HOUR}.							*/
	protected double	maxFuelConsumption;
	/** total maximal out power provided when producing power in
	 *  {@code MeasurementUnit.WATTS}.	 									*/
	protected double	totalMaximumOutputPower;

	/** current state of the generator.										*/
	protected State		currentState;
	/** the event to be emitted at the next output or null if none.			*/
	protected EventToBeEmittedNext	toBeEmitted;

	/** current power delivered to the electric circuit by the generator
	 *  in {@code MeasurementUnit.AMPERES}.									*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>	generatorOutputPower;

	/** in the QSS numerical integration algorithm, standard increment or
	 *  decrement in the fuel level between two successive updates of the
	 *  tank level in {@code MeasurementUnit.LITERS}.						*/
	protected double	standardLevelQuantum;
	/** in the QSS numerical integration algorithm, increment or decrement
	 *  in the fuel level to be used for the next update of the tank
	 *  level in {@code MeasurementUnit.LITERS}.							*/
	protected double	currentLevelQuantum;
	/** current level of the tank in {@code MeasurementUnit.LITERS}.		*/
	@InternalVariable(type = Double.class)
	protected Value<Double>	currentLevel = new Value<>(this);
	/** the value of the fuel level derivative at the last internal event.	*/
	@InternalVariable(type = Double.class)
	protected Value<Double>	lastDerivative = new Value<>(this);

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
		GeneratorFuelModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= GeneratorFuelModel.staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentLevel.isInitialised() ||
					(instance.currentLevel.getValue() >= 0.0 &&
						instance.currentLevel.getValue() < instance.maxCapacity),
				GeneratorFuelModel.class, instance,
				"!currentLevel.isInitialised() || (currentLevel.getValue() >= "
				+ "0.0 && currentLevel.getValue() < maxCapacity)");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.lastDerivative.isInitialised() ||
								(instance.lastDerivative.getValue() <= 0.0),
				GeneratorFuelModel.class, instance,
				"!lastDerivative.isInitialised() || "
				+ "(lastDerivative.getValue() <= 0.0)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.standardLevelQuantum >= 0.0,
				GeneratorFuelModel.class, instance,
				"standardLevelQuantum > 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				Math.abs(instance.currentLevelQuantum) >= 0.0 &&
						Math.abs(instance.currentLevelQuantum) <=
												instance.standardLevelQuantum,
				GeneratorFuelModel.class, instance,
				"currentLevelQuantum >= 0.0 && currentLevelQuantum <= "
				+ "standardLevelQuantum");
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
	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				TimeUnit.HOURS.equals(GlobalSimulationConfigurationI.TIME_UNIT),
				GeneratorFuelModel.class,
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
	protected static boolean	invariants(GeneratorFuelModel instance)
	{
		assert	instance != null :
			new NeoSim4JavaException(
					"Precondition violation: instance != null");

		boolean ret = true;
		ret &= GeneratorFuelModel.staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic hybrid input/output model with the given URI (if null,
	 * one will be generated) and to be run by the given simulator using the
	 * given time unit for its clock.
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
	public				GeneratorFuelModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert	GeneratorFuelModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorFuelModel.implementationInvariants(this)");
		assert	GeneratorFuelModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorFuelModel.invariants(this)");
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
		String capacityName = ModelI.createRunParameterName(this.getURI(),
													CAPACITY_RP_NAME);
		String initialLevelName = ModelI.createRunParameterName(this.getURI(),
													INITIAL_LEVEL_RP_NAME);
		String minFuelConsumptionName =
								  ModelI.createRunParameterName(this.getURI(),
													MIN_FUEL_CONSUMPTION_RP_NAME);
		String maxFuelConsumptionName =
								  ModelI.createRunParameterName(this.getURI(),
										  			MAX_FUEL_CONSUMPTION_RP_NAME);
		String maxOutPowerName = ModelI.createRunParameterName(this.getURI(),
													MAX_OUT_POWER_RP_NAME);
		String quantumLevelName = ModelI.createRunParameterName(this.getURI(),
													LEVEL_QUANTUM_RP_NAME);

		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(initialLevelName) :
				new MissingRunParameterException(initialLevelName);
		assert	simParams.containsKey(initialLevelName) :
				new MissingRunParameterException(capacityName);
		assert	simParams.containsKey(minFuelConsumptionName) :
				new MissingRunParameterException(minFuelConsumptionName);
		assert	simParams.containsKey(maxFuelConsumptionName) :
				new MissingRunParameterException(maxFuelConsumptionName);
		assert	simParams.containsKey(maxOutPowerName) :
				new MissingRunParameterException(maxOutPowerName);
		assert	simParams.containsKey(quantumLevelName) :
				new MissingRunParameterException(quantumLevelName);

		this.maxCapacity = (double) simParams.get(capacityName);
		this.initialLevel = (double) simParams.get(initialLevelName);
		this.minFuelConsumption = (double) simParams.get(minFuelConsumptionName);
		this.maxFuelConsumption = (double) simParams.get(maxFuelConsumptionName);
		this.totalMaximumOutputPower = (double) simParams.get(maxOutPowerName);
		this.standardLevelQuantum = (double) simParams.get(quantumLevelName);

		// tracing
		if (DEBUG) {
			this.logMessage("setSimulationRunParameters done!");
		}
	}

	/**
	 * initialise the model.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The model is initialised so that the batteries are in IDLE state and it
	 * is at full capacity.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code currentState = State.IDLE}
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.currentState = State.OFF;
		this.toBeEmitted = null;
		this.currentLevelQuantum = this.standardLevelQuantum;

		// Invariant checking
		assert	GeneratorFuelModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorFuelModel.implementationInvariants(this)");
		assert	GeneratorFuelModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: "
						+ "GeneratorFuelModel.invariants(this)");

		super.initialiseState(initialTime);

		// tracing
		if (DEBUG) {
			this.logMessage("initialiseState currentState = "
							+ this.currentState);
		}
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
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		// at each call, the method tries to initialise the variables held by
		// the model and return the number of variables that were initialised
		// and the number that could not be initialised (because variables
		// imported from other models they depend upon are not yet initialised)
		int numberOfNewlyInitialisedVariables = 0;
		int numberOfStillNotInitialisedVariables = 0;

		// the variables lastDerivative and currentLevel are internal, so they
		// can be immediately initialised
		if (!this.lastDerivative.isInitialised()) {
			this.lastDerivative.initialise(0.0);
			numberOfNewlyInitialisedVariables++;
			// tracing
			if (DEBUG) {
				this.logMessage(
						"fixpointInitialiseVariables lastDerivative = "
						+ this.lastDerivative.getValue());
			}
		}
		if (!this.currentLevel.isInitialised()) {
			this.currentLevel.initialise(this.initialLevel);
			numberOfNewlyInitialisedVariables++;
			// tracing
			if (DEBUG) {
				this.logMessage(
						"fixpointInitialiseVariables currentLevel = "
						+ this.currentLevel.getValue());
			}
		}

		// Invariant checking
		assert	GeneratorFuelModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorFuelModel.implementationInvariants(this)");
		assert	GeneratorFuelModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorFuelModel."
						+ "invariants(this)");

		// the two counters are returned and aggregated among the different
		// execution of fixpointInitialiseVariables in the different models
		// if the total numbers gives 0 still not initialised variable, then
		// the fix point has been reached, but if there are still variables not
		// initialised but some have been initialised during the run (i.e.,
		// numberOfNewlyInitialisedVariables > 0) then the method
		// fixpointInitialiseVariables must be rerun on all models until all
		// variables have been initialised
		return new Pair<Integer, Integer>(numberOfNewlyInitialisedVariables,
										  numberOfStillNotInitialisedVariables);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorStateManagementI#getGeneratorState()
	 */
	@Override
	public State		getGeneratorState()
	{
		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorStateManagementI#setGeneratorState(fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI.State)
	 */
	@Override
	public void			setGeneratorState(State newState)
	{
		this.currentState = newState;
	}

	/**
	 * refill the tank.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code currentState.equals(State.OFF) || currentState.equals(State.TANK_EMPTY)}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param quantity	the quantity to be added up to the tank capacity.
	 */
	public void			refill(double quantity)
	{
		assert	this.currentState.equals(State.OFF) ||
									this.currentState.equals(State.TANK_EMPTY) :
				new NeoSim4JavaException(
						"currentState.equals(State.OFF) || currentState."
						+ "equals(State.TANK_EMPTY)");

		double old = this.currentLevel.getValue();
		double newValue = old + quantity;
		if (newValue > this.maxCapacity) {
			newValue = this.maxCapacity;
		}
		this.currentLevel.setNewValue(newValue, currentStateTime);
		if (this.currentState.equals(State.TANK_EMPTY)) {
			this.currentState = State.OFF;
			this.toBeEmitted = EventToBeEmittedNext.TANK_NO_LONGER_EMPTY;
		}
	}

	/**
	 * compute the duration required to consume {@code quantum} at the
	 * {@code derivative} rhythm.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @param quantum		a state quantum to consume during the next step.
	 * @param derivative	the derivative for the consumption of the state.
	 * @return				the duration required to consume {@code quantum} at the {@code derivative} rhythm.
	 */
	protected Duration	computeStepDuration(
		double quantum,
		double derivative
		)
	{
		Duration ret = null;
		double absDerivative = Math.abs(derivative);
		if (absDerivative < TOLERANCE) {
			ret = Duration.INFINITY;
		} else {
			ret = new Duration(Math.abs(quantum)/absDerivative,
							   this.getSimulatedTimeUnit());
		}

		// tracing
		if (DEBUG) {
			this.logMessage("computeStepDuration for quantum " + quantum
							+ " and derivative " + derivative
							+ " returns " + ret);
		}

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		Duration ret = null;
		if (this.toBeEmitted != null) {
			// the crossing of a constraint on the continuous state triggers an
			// immediate internal event that will emit an external event to
			// notify the users
			ret = Duration.zero(this.getSimulatedTimeUnit());
		} else if (this.currentState.equals(State.PRODUCING) ||
										this.currentState.equals(State.IDLE)) {
			// when producing or idle, the time to the next internal event is
			// the time required for the fuel level to change by the amount
			// defined by currentLevelQuantum given the current level derivative
			ret = this.computeStepDuration(this.currentLevelQuantum,
										   this.lastDerivative.getValue());
		} else {
			// when the state is OFF or TANK_EMPTY, there is no future internal
			// event, hence the time advance is infinity
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
			if (this.toBeEmitted.equals(EventToBeEmittedNext.TANK_EMPTY)) {
				ret.add(new TankEmpty(this.getTimeOfNextEvent()));
			} else {
				assert	this.toBeEmitted.equals(
								EventToBeEmittedNext.TANK_NO_LONGER_EMPTY) :
						new NeoSim4JavaException(
								"toBeEmitted.equals(EventToBeEmittedNext."
								+ "TANK_NO_LONGER_EMPTY)");
				ret.add(new TankNoLongerEmpty(this.getTimeOfNextEvent()));
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
	 * compute the current derivative of the fuel level in
	 * {@code MeasurementUnit.LITERS}/h. 
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The generator has a minimal and a maximal fuel consumption (derivative
	 * of the fuel level) corresponding to the idle state or the minimal power
	 * production for the former and the maximal power production for the
	 * latter. The model assumes that between these two ends, the fuel
	 * consumption grows linearly, proportionally to the power production. The
	 * result is always negative or 0, as the fuel level goes down while
	 * producing.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return <= 0.0}
	 * </pre>
	 *
	 * @return	the current derivative of the fuel level.
	 */
	protected double	computeFuelLevelDerivative()
	{
		switch (this.currentState) {
		case PRODUCING:
			double coef = this.generatorOutputPower.getValue() *
								Generator.OUTPUT_AC_TENSION.getData()/
											this.totalMaximumOutputPower;
			double range = this.maxFuelConsumption - this.minFuelConsumption;
			return -(this.minFuelConsumption + coef * range);
		case IDLE:
			return -this.minFuelConsumption;
		case OFF:
		case TANK_EMPTY:
			return 0.0;
		default:
			throw new NeoSim4JavaException("unknown generator state");
		}
	}

	/**
	 * compute a new state when a transition occurs, internal or external after
	 * having executed the effect of the external event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		computeNextState()
	{
		// tracing
		if (DEBUG) {
			this.logMessage(
					"compute next state (beginning) at "
					+ this.getCurrentStateTime()
					+ ": currentState = " + this.currentState
					+ ", currentLevel = " + this.currentLevel
					+ ", generatorOutputPower = " + this.generatorOutputPower);
		}

		// previous events to be emitted have been emitted, if any
		assert	toBeEmitted == null :
				new NeoSim4JavaException("toBeEmitted == null");

		// First, update the current level of the fuel tank using the level
		// quantum chosen at the preceding evaluation, and also possibly the
		// state, when an internal event TANK_EMPTY occurs. Next, update the
		// derivative of the fuel level and choose the next level quantum to
		// be applied. Finally, update the output power given the current state
		if (!(this.currentState.equals(State.OFF) ||
								this.currentState.equals(State.TANK_EMPTY))) {

			// update the level given the level quantum chosen at the previous
			// evaluation
			double newLevel =
					this.currentLevel.getValue() + this.currentLevelQuantum;
			this.currentLevel.setNewValue(newLevel,
										  this.getCurrentStateTime());

			// update the state given the constraints on the fuel level
			switch (this.currentState) {
			case PRODUCING:
				if (newLevel < TOLERANCE) {
					// The tank is considered empty, so set the level to 0 and
					// the generator goes to the state TANK_EMPTY and prepares
					// to emit the event TANK_EMPTY to notify its user that it
					// can no longer produce until refilled.
					this.currentLevel.setNewValue(0.0,
												  this.getCurrentStateTime());
					this.currentState = State.TANK_EMPTY;
					this.toBeEmitted = EventToBeEmittedNext.TANK_EMPTY;
				} else {
					if (this.generatorOutputPower.getValue() <= TOLERANCE) {
						// if no power is still needed by the user, then the
						// generator goes to idle state
						this.currentState = State.IDLE;
					} else {
						// otherwise remain in the state PRODUCING
						this.currentState = State.PRODUCING;
					}
				}
				break;
			case IDLE:
				if (newLevel < TOLERANCE) {
					// the tank is empty, so set the level to 0 and go to
					// the state OFF
					this.currentLevel.setNewValue(0.0,
												  this.getCurrentStateTime());
					this.currentState = State.TANK_EMPTY;
					this.toBeEmitted = EventToBeEmittedNext.TANK_EMPTY;
				} else {
					if (this.generatorOutputPower.getValue() > TOLERANCE) {
						// if power is again needed
						this.currentState = State.PRODUCING;
					} else {
						// otherwise remain in the state IDLE
						this.currentState = State.IDLE;
					}
				}
				break;
			case OFF:
			case TANK_EMPTY:
				// excluded by the if
				throw new NeoSim4JavaException("must never happen!");
			default:
				throw new NeoSim4JavaException("unknown generator state!");
			}

			// update the current level quantum for the next transition
			switch (this.currentState) {
			case PRODUCING:
			case IDLE:
				if (newLevel >= this.standardLevelQuantum) {
					// newLevel is high enough to drop by the standard quantum
					// during the next step
					this.currentLevelQuantum = -this.standardLevelQuantum;
				} else {
					// otherwise, the tank will become empty during the next
					// step, hence choose the current level as the temporary
					// quantum for the next step
					this.currentLevelQuantum = -newLevel;
				}
				break;
			case OFF:
			case TANK_EMPTY:
				// empty or off, the generator does not consume fuel
				this.currentLevelQuantum = 0.0;
				break;
			default:
				throw new NeoSim4JavaException("unknown generator state!");
			}
		} else {
			this.currentLevel.setNewValue(this.currentLevel.getValue(),
										  this.getCurrentStateTime());
		}

		// update the last derivative to the one at the current time
		this.lastDerivative.setNewValue(
				this.computeFuelLevelDerivative(), 
				this.getCurrentStateTime());

		// Invariant checking
		assert	GeneratorFuelModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorFuelModel.implementationInvariants(this)");
		assert	GeneratorFuelModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorFuelModel."
						+ "invariants(this)");

		// tracing
		if (DEBUG) {
			this.logMessage(
					"compute next state (end) at "
					+ this.getCurrentStateTime()
					+ ": currentState = " + this.currentState
					+ ", currentLevel = " + this.currentLevel
					+ ", level quantum = " + this.currentLevelQuantum
					+ ", generatorOutputPower = " + this.generatorOutputPower);
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		this.computeNextState();

		// tracing
		if (DEBUG) {
			this.logMessage(
					"userDefinedInternalTransition at "
					+ this.getCurrentStateTime()
					+ ": currentState = " + this.currentState
					+ ", currentLevel = " + this.currentLevel
					+ ", level quantum = " + this.currentLevelQuantum
					+ ", generatorOutputPower = " + this.generatorOutputPower);
		} else if (VERBOSE) {
			this.logMessage(
					"userDefinedInternalTransition at "
					+ this.getCurrentStateTime()
					+ ": currentState = " + this.currentState
					+ "; currentLevel = " + this.currentLevel);
		}

		// Invariant checking
		assert	GeneratorFuelModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorFuelModel.implementationInvariants(this)");
		assert	GeneratorFuelModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorFuelModel."
						+ "invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void		userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		if (!this.currentState.equals(State.OFF) &&
								!this.currentState.equals(State.TANK_EMPTY)) {
			// update the current level given the elapsed time and the derivative
			// at the last internal event
			this.currentLevel.setNewValue(
				this.currentLevel.getValue() +
						this.lastDerivative.getValue() *
											elapsedTime.getSimulatedDuration(),
				this.getCurrentStateTime());
		} else {
			this.currentLevel.setNewValue(this.currentLevel.getValue(),
										  this.getCurrentStateTime());
		}

		// as the transition is computed immediately, it must not change the
		// level that has already been updated and it will recompute these
		// values from the new state after executing the event
		this.currentLevelQuantum = 0.0;
		this.lastDerivative.setNewValue(0.0, this.getCurrentStateTime());

		EventI e = this.getStoredEventAndReset().remove(0);

		// execute the event, which will change the state of the batteries
		e.executeOn(this);

		// complete the update of the state of the model
		this.computeNextState();

		// tracing
		if (DEBUG) {
			this.logMessage(
					"userDefinedExternalTransition at "
					+ this.getCurrentStateTime()
					+ ": currentState = " + this.currentState
					+ ", currentLevel = " + this.currentLevel
					+ ", level quantum = " + this.currentLevelQuantum
					+ ", generatorOutputPower = " + this.generatorOutputPower);
		} else if (VERBOSE) {
			this.logMessage(
					"userDefinedExternalTransition at "
					+ this.getCurrentStateTime()
					+ " on event " + e
					+ ": currentState = " + this.currentState
					+ ", currentLevel = " + this.currentLevel);
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
					+ ": currentState = " + this.currentState
					+ ", currentLevel = " + this.currentLevel);
		}

		super.endSimulation(endTime);
	}
}
// -----------------------------------------------------------------------------
