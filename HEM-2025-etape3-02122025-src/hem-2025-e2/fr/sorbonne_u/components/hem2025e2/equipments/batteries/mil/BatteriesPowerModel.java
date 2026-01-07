package fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil;

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
import fr.sorbonne_u.components.hem2025e1.equipments.batteries.Batteries.State;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.BatteriesAvailable;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.BatteriesEmpty;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.BatteriesRequiredPowerChanged;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.StartCharging;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.StopCharging;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariables;
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
import java.util.ArrayList;
import java.util.Map;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesPowerModel</code> implements the simulation model
 * for the batteries.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation model very interesting as it is a typical example of a hybrid
 * system where not only model events (here external) can interrupt the
 * continuous evolution of the model but also physical events caused by the
 * evolution of the continuous part of the model itself, namely the evolution
 * of the charge level of the batteries and of the required power from the
 * batteries. Obviously, when discharging the batteries, the charge level cannot
 * go under 0. Symmetrically, when charging, the charge level cannot go over the
 * maximum capacity of the batteries. The case of the required power is a bit
 * more complicated, as explained below.
 * </p>
 * <p>
 * As such, this model gathers all of the ingredients that make the simulation
 * of hybrid systems an issue, though here the simplicity of the differential
 * equations (linear) eases the prediction of the time at which the physical
 * event triggered transition simple and precise.
 * </p>
 * <p>
 * In this implementation, the Quantised State System (QSS) approach is used to
 * perform the numerical integration of the differential equations modelling the
 * evolution of the charge level. A standard level quantum is defined as a
 * configuration parameter. At each integration step, the algorithm adds the
 * quantum to the current charge level and plan the next internal transition 
 * after the time required to observe a change in the charge level of the amount
 * given by this quantum. When the charge level is about to reach a limit (full
 * or empty), the quantum is temporarily adapted to exactly reach that limit at
 * the next computation step, and the time required to reach the limit will be
 * used to plan the next internal transition.
 * </p>
 * <p>
 * This model also exhibit another important feature: it has external events
 * which occurrence can happen before the next internal transition is reached.
 * When this happens, the current charge level must be updated but not of the
 * amount of the quantum but only the amount corresponding to the elapsed time
 * since the last internal event given the current evaluation of the charge
 * level derivative (evaluated at the last internal transition). Hence, when an
 * external transition occurs, the model first update the current charge level
 * using the standard time quantum computation (following the Euler rule, the
 * amount of level that must be added is obtained by multiplying the current
 * derivative by the elapsed time). After updating the current charge level,
 * the state of the batteries is updated according to the type of event that
 * just occurred and then the state of the entire model is updated.
 * </p>
 * <p>
 * To simulate the delivery of power from the batteries, the idea is that to
 * have the batteries delivering energy, an imported variable pushes the user
 * required power from the batteries. Given this required power, the batteries
 * put in an exported variable the actual output power, which may be limited by
 * the maximum output power of the batteries. In theory, if these continuous
 * variables are evaluated step by step, when the user of the batteries set a
 * new value of required power from the batteries, the batteries model will
 * notice that at its next computational step and update its output power
 * accordingly. Unfortunately, this does not mix well with the QSS computational
 * approach. Indeed, the batteries model have no information about the future
 * evolution of the required power, so it may have planned the next update after
 * a long time before performing a new computational step. This delay may
 * introduce a large time gap and discrepancies in the variables values between
 * the two. Therefore, as the change in the required power is voluntary, the
 * batteries model assumes that it will receive an external event at the time of
 * the required power change notifying the state change, triggering an immediate
 * update of the output power.
 * </p>
 * <p>
 * Batteries also have to be put in charge at some moment. This simulator
 * assumes that batteries can be either in charge or in use (o produce energy
 * but not both at the same time. The charging state is not automatic. The
 * beginning of the charge is triggered by an imported event of type
 * {@code StartCharging} and the end is triggered by an imported event of type
 * {@code StopCharging}. Accordingly, when the batteries become empty, an
 * exported event of type {@code BatteriesEmpty} is emitted while an event of
 * type {@code BatteriesAvailable} is emitted when the batteries can be used
 * again <i>i.e.</i>, after reaching some threshold. Currently, no event is
 * emitted to signal the state "batteries full", but this could be easily added.
 * When the batteries are charging, the model assumes that they consumes a fixed
 * electric intensity. The model exports the variable {@code batteriesInputPower}
 * that contains the power consumed by the batteries in the measurement unit
 * used by the electric meter. The constraint that the batteries cannot be
 * charging and used at the same time translates into an invariant that the
 * exported variables {@code batteriesInputPower} and
 * {@code batteriesOutputPower} cannot be both greater than 0 at the same time.
 * </p>
 * <p>
 * Note that the implementation of this model is made a bit more complicated by
 * the fact that several parameters are made akin to user choice at the
 * beginning of the simulation runs, like the standard charge level quantum, the
 * batteries maximum capacity, etc.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code StartCharging},
 *   {@code StopCharging},
 *   {@code BatteriesRequiredPowerChanged}</li>
 * <li>Exported events:
 *   {@code BatteriesEmpty},
 *   {@code BatteriesAvailable}</li>
 * <li>Imported variables:
 *   name = {@code batteriesRequiredPower}, type = {@code Double}</li>
 * <li>Exported variables:
 *   <ul>
 *   <li>name = {@code batteriesInputPower}, type = {@code Double}</li>
 *   <li>name = {@code batteriesOutputPower}, type = {@code Double}</li>
 *   </ul>
 * </li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code (!batteriesInputPower.isInitialised() || !batteriesOutputPower.isInitialised()) || !(batteriesInputPower.getValue() > 0.0 && batteriesOutputPower.getValue() > 0.0)}
 * invariant	{@code instance.batteriesRequiredPower == null || !instance.batteriesRequiredPower.isInitialised() || instance.batteriesRequiredPower.getValue() >= 0.0}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code TimeUnit.HOURS.equals(GlobalSimulationConfigurationI.TIME_UNIT)}
 * </pre>
 * 
 * <p>Created on : 2025-10-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
	imported = {StartCharging.class, StopCharging.class,
				BatteriesRequiredPowerChanged.class},
	exported = {BatteriesEmpty.class, BatteriesAvailable.class})
@ModelExportedVariables(
	{@ModelExportedVariable(name = "batteriesInputPower", type = Double.class),
	 @ModelExportedVariable(name = "batteriesOutputPower", type = Double.class)}
	)
@ModelImportedVariable(name = "batteriesRequiredPower", type = Double.class)
//-----------------------------------------------------------------------------
public class			BatteriesPowerModel
extends		AtomicHIOA
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
		BATTERIES_EMPTY,
		BATTERIES_AVAILABLE;
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
	public static final String	URI = "batteries-power-model";
	/** tension used in the batteries.										*/
	protected static double		TENSION = 220.0;
	
	/**	name of the run parameter for the capacity of the batteries in
	 *  {@code MeasurementUnit.WATT_HOURS}.									*/
	public static final String	CAPACITY_RP_NAME = "CAPACITY";
	/**	name of the run parameter for the initial charge level of the
	 *  batteries in {@code MeasurementUnit.WATT_HOURS}.					*/
	public static final String	INITIAL_LEVEL_RP_NAME = "INITIAL_LEVEL";
	/**	name of the run parameter for the input power of the batteries when
	 *  charging in {@code MeasurementUnit.WATTS}.							*/
	public static final String	IN_POWER_RP_NAME = "IN_POWER";
	/**	name of the run parameter for the maximum out power of the batteries
	 *  in {@code MeasurementUnit.WATTS}.									*/
	public static final String	MAX_OUT_POWER_RP_NAME = "MAX_OUT_POWER_RP_NAME";
	/**	name of the run parameter for the initialisation of the
	 *  level quantum in {@code MeasurementUnit.WATT_HOURS}.				*/
	public static final String	LEVEL_QUANTUM_RP_NAME = "LEVEL_QUANTUM";
	
	/** maximum capacity of the batteries in
	 *  {@code MeasurementUnit.WATT_HOURS}.									*/
	protected double				maxCapacity;
	/** initial charge level of the batteries in
	 *  {@code MeasurementUnit.WATT_HOURS}.									*/
	protected double				initialLevel;
	/** input power consumed when charging the batteries in
	 *  {@code MeasurementUnit.WATTS}.										*/
	protected double				inputPower;
	/** maximum output power the batteries can deliver in
	 *  {@code MeasurementUnit.WATTS}.	 									*/
	protected double				maximumOutputPower;

	/** current state of the batteries.										*/
	protected State					currentState;
	/** the event to be emitted at the next output or null if none.			*/
	protected EventToBeEmittedNext	toBeEmitted;

	/** current power consumed from the electric circuit to charge the
	 *  batteries in {@code MeasurementUnit.AMPERES}.						*/
	@ExportedVariable(type = Double.class)
	protected Value<Double>	batteriesInputPower = new Value<>(this);
	/** current power required by the electric circuit from the batteries
	 *  in {@code MeasurementUnit.AMPERES}; it is set by the electric
	 *  meter according to the difference between the overall current power
	 *  production and the current power consumption.						*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>	batteriesRequiredPower;
	/** current power delivered to the electric circuit by the batteries
	 *  in {@code MeasurementUnit.AMPERES}.									*/
	@ExportedVariable(type = Double.class)
	protected Value<Double>	batteriesOutputPower = new Value<>(this);

	/** in the QSS numerical integration algorithm, standard increment or
	 *  decrement in the power level between two successive updates of the
	 *  batteries level in {@code MeasurementUnit.WATT_HOURS}.				*/
	protected double	standardLevelQuantum;
	/** in the QSS numerical integration algorithm, increment or decrement
	 *  in the power level to be used for the next update of the batteries
	 *  level in {@code MeasurementUnit.WATT_HOURS}.						*/
	protected double	currentLevelQuantum;
	/** current level of the batteries in
	 *  {@code MeasurementUnit.WATT_HOURS}.									*/
	@InternalVariable(type = Double.class)
	protected Value<Double>	currentLevel = new Value<>(this);
	/** the value of the batteries level derivative at the last internal
	 *  event.															 	*/
	@InternalVariable(type = Double.class)
	protected Value<Double>	lastDerivative = new Value<>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

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
		BatteriesPowerModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				(!instance.batteriesInputPower.isInitialised() ||
							!instance.batteriesOutputPower.isInitialised()) ||
					!(instance.batteriesInputPower.getValue() > 0.0 &&
								instance.batteriesOutputPower.getValue() > 0.0),
				BatteriesPowerModel.class, instance,
				"(!batteriesInputPower.isInitialised() && !batteriesOutputPower"
				+ ".isInitialised()) || !(batteriesInputPower.getValue() > 0.0 "
				+ "&& batteriesOutputPower.getValue() > 0.0)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.batteriesRequiredPower == null ||
					!instance.batteriesRequiredPower.isInitialised() ||
						instance.batteriesRequiredPower.getValue() >= 0.0,
				BatteriesPowerModel.class, instance,
				"instance.batteriesRequiredPower == null || "
				+ "!instance.batteriesRequiredPower.isInitialised() || "
				+ "instance.batteriesRequiredPower.getValue() >= 0.0");
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
	protected static boolean	invariants(BatteriesPowerModel instance)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= BatteriesPowerModel.staticInvariants();
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
	public				BatteriesPowerModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert	BatteriesPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriePowerModel.implementationInvariants(this)");
		assert	BatteriesPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: BatteriePowerModel."
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
		String capacityName = ModelI.createRunParameterName(this.getURI(),
													CAPACITY_RP_NAME);
		String initialLevelName = ModelI.createRunParameterName(this.getURI(),
													INITIAL_LEVEL_RP_NAME);
		String inPowerName = ModelI.createRunParameterName(this.getURI(),
													IN_POWER_RP_NAME);
		String maxOutPowerName = ModelI.createRunParameterName(this.getURI(),
													MAX_OUT_POWER_RP_NAME);
		String quantumLevelName = ModelI.createRunParameterName(this.getURI(),
													LEVEL_QUANTUM_RP_NAME);

		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(capacityName) :
				new MissingRunParameterException(initialLevelName);
		assert	simParams.containsKey(initialLevelName) :
				new MissingRunParameterException(capacityName);
		assert	simParams.containsKey(inPowerName) :
				new MissingRunParameterException(inPowerName);
		assert	simParams.containsKey(maxOutPowerName) :
				new MissingRunParameterException(maxOutPowerName);
		assert	simParams.containsKey(quantumLevelName) :
				new MissingRunParameterException(quantumLevelName);

		this.maxCapacity = (double) simParams.get(capacityName);
		this.initialLevel = (double) simParams.get(initialLevelName);
		this.inputPower = (double) simParams.get(inPowerName);
		this.maximumOutputPower = (double) simParams.get(maxOutPowerName);
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
		this.currentLevelQuantum = this.standardLevelQuantum;

		// Invariant checking
		assert	BatteriesPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriePowerModel.implementationInvariants(this)");
		assert	BatteriesPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: "
						+ "BatteriePowerModel.invariants(this)");

		super.initialiseState(initialTime);

		// tracing
		if (DEBUG) {
			this.logMessage("BatteriesPowerModel::initialiseState "
					+ "currentState = " + this.currentState);
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

		// the variables batteriesInputPower is exported, so it can be
		// immediately initialised
		if (!this.batteriesInputPower.isInitialised()) {
			this.batteriesInputPower.initialise(0.0);
			numberOfNewlyInitialisedVariables++;
			// tracing
			if (DEBUG) {
				this.logMessage(
						"fixpointInitialiseVariables batteriesInputPower = "
						+ this.batteriesInputPower.getValue());
			}
		}
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
		// the variables batteriesOutputPower is the one that this model holds
		// and that must be initialised, but it depends upon the imported
		// variable batteriesRequiredPower
		if (!this.batteriesOutputPower.isInitialised()) {
			// if batteriesRequiredPower has been initialised, then
			// batteriesOutputPower can be initialised
			if (this.batteriesRequiredPower.isInitialised()) {
				double outPower = this.batteriesRequiredPower.getValue();
				if (outPower > this.maximumOutputPower) {
					outPower = this.maximumOutputPower;
				} 
				this.batteriesOutputPower.initialise(outPower);
				// and then the number of newly initialised variable is 1
				numberOfNewlyInitialisedVariables++;
				// tracing
				if (DEBUG) {
					this.logMessage(
							"fixpointInitialiseVariables batteriesOutputPower = "
							+ this.batteriesOutputPower.getValue());
				}
			} else {
				// otherwise, batteriesOutputPower cannot be initialised and the
				// number of variables not initialised by this execution is one
				numberOfStillNotInitialisedVariables++;
			}
		}

		// Invariant checking
		assert	BatteriesPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriePowerModel.implementationInvariants(this)");
		assert	BatteriesPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: BatteriePowerModel."
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
	 * return the current state of the batteries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current state of the batteries.
	 */
	public State		getBatteriesState()
	{
		return this.currentState;
	}

	/**
	 * set the new state of the batteries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code newState != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newState	the new state of the batteries.
	 */
	public void			setBatteriesState(State newState)
	{
		this.currentState = newState;
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
		} else if (!this.currentState.equals(State.IDLE)) {
			// when charging or producing the time to the next internal event
			// is the time required for the batteries level to change of the
			// amount defined by currentLevelQuantum given the current level
			// derivative
			// when producing or idle, the time to the next internal event is
			// the time required for the fuel level to change by the amount
			// defined by currentLevelQuantum given the current level derivative
			ret = this.computeStepDuration(this.currentLevelQuantum,
										   this.lastDerivative.getValue());
		} else {
			assert	State.IDLE.equals(this.currentState) :
					new NeoSim4JavaException(
							"State.IDLE.equals(this.currentState)");

			// when the state is IDLE, there is no internal event, hence the
			// time advance is infinity
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
			if (toBeEmitted.equals(EventToBeEmittedNext.BATTERIES_EMPTY)) {
				ret.add(new BatteriesEmpty(this.getTimeOfNextEvent()));
			} else {
				ret.add(new BatteriesAvailable(this.getTimeOfNextEvent()));
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
	 * compute the current derivative of the capacity level of the batteries in
	 * {@code MeasurementUnit.WATT_HOURS}/h. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current derivative of the batteries level.
	 */
	protected double	computeCurrentCapacityDerivative()
	{
		switch (this.currentState) {
		case CHARGING:
			// when charging, the level goes up based on its input power
			return this.inputPower;
		case PRODUCING:
			// when producing, the level goes down based on its current output
			// power
			return -(this.batteriesOutputPower.getValue() * TENSION);
		case EMPTY:
		case IDLE:
			// when empty or idle, the level remains the same
			return 0.0;
		default:
			throw new NeoSim4JavaException("unknown batteries state");
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
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevel
					+ " batteriesInputPower = " + this.batteriesInputPower
					+ " batteriesOutputPower = " + this.batteriesOutputPower
					+ " batteriesRequiredPower = "
					+ this.batteriesRequiredPower);
		}

		// previous events to be emitted have been emitted, if any
		assert	toBeEmitted == null :
				new NeoSim4JavaException("toBeEmitted == null");

		// First, update the current level of the batteries using the level
		// quantum chosen at the preceding evaluation, and also possibly the
		// state, when an internal event TANK_EMPTY occurs. Next, update the
		// derivative of the fuel level and choose the next level quantum to
		// be applied. Finally, update the output power given the current state
		if (!this.currentState.equals(State.EMPTY)) {

			// update the level given the current quantum
			double oldLevel = this.currentLevel.getValue();
			double newLevel =
					this.currentLevel.getValue() + this.currentLevelQuantum;
			this.currentLevel.setNewValue(newLevel,
										  this.getCurrentStateTime());

			// update the state given the constraints on the batteries level
			switch (this.currentState) {
			case CHARGING:
				if (Math.abs(this.maxCapacity - newLevel) < TOLERANCE) {
					// the batteries are full, so set the level to the maximum
					// capacity and go to the state IDLE
					this.currentLevel.setNewValue(this.maxCapacity,
												  this.getCurrentStateTime());
					this.currentState = State.IDLE;
				} else {
					// otherwise remain in the state CHARGING
					this.currentState = State.CHARGING;
				}
				if (oldLevel < TOLERANCE && newLevel > TOLERANCE) {
					// if the level is beginning to go up and the batteries
					// where empty, signal the batteries are available again
					this.toBeEmitted =
							EventToBeEmittedNext.BATTERIES_AVAILABLE;
				}
				break;
			case PRODUCING:
				if (newLevel < TOLERANCE) {
					// the batteries are empty, so set the level to 0 and go to
					// the state IDLE
					this.currentLevel.setNewValue(0.0,
												  this.getCurrentStateTime());
					this.currentState = State.EMPTY;
					this.toBeEmitted = EventToBeEmittedNext.BATTERIES_EMPTY;
				} else {
					if (this.batteriesRequiredPower.getValue() <= TOLERANCE) {
						this.currentState = State.IDLE;
					} else {
						// otherwise remain in the state PRODUCING
						this.currentState = State.PRODUCING;
					}
				}
				break;
			case IDLE:
				if (this.batteriesRequiredPower.getValue() > TOLERANCE) {
					this.currentState = State.PRODUCING;
				} else {
					// otherwise remain in the state PRODUCING
					this.currentState = State.IDLE;
				}
				break;
			case EMPTY:
				// excluded by the if
				throw new NeoSim4JavaException("must never happen!");
			default:
				throw new NeoSim4JavaException("unknown batteries state!");
			}

			// update the current level quantum for the next transition
			switch (this.currentState) {
			case CHARGING:
				if (newLevel + this.standardLevelQuantum <= this.maxCapacity) {
					this.currentLevelQuantum = this.standardLevelQuantum;
				} else {
					this.currentLevelQuantum = this.maxCapacity - newLevel;
				}
				break;
			case PRODUCING:
				if (newLevel >= this.standardLevelQuantum) {
					this.currentLevelQuantum = -this.standardLevelQuantum;
				} else {
					this.currentLevelQuantum = -newLevel;
				}
				break;
			case EMPTY:
			case IDLE:
				this.currentLevelQuantum = 0.0;
				break;
			default:
				throw new NeoSim4JavaException("unknown batteries state!");
			}
		} else {
			this.currentLevel.setNewValue(this.currentLevel.getValue(),
										  this.getCurrentStateTime());
		}

		// update the power levels, in and out
		switch (this.currentState) {
		case CHARGING:
			this.batteriesInputPower.setNewValue(this.inputPower/TENSION,
												 this.getCurrentStateTime());
			this.batteriesOutputPower.setNewValue(0.0,
												  this.getCurrentStateTime());
			break;
		case PRODUCING:
			this.batteriesInputPower.setNewValue(0.0,
												 this.getCurrentStateTime());
			if (this.batteriesRequiredPower.getValue() * TENSION <=
												this.maximumOutputPower) {
				this.batteriesOutputPower.setNewValue(
										this.batteriesRequiredPower.getValue(),
										this.getCurrentStateTime());
			} else {
				this.batteriesOutputPower.setNewValue(
										this.maximumOutputPower/TENSION,
										this.getCurrentStateTime());
			}
			break;
		case EMPTY:
		case IDLE:
			this.batteriesInputPower.setNewValue(0.0,
												 this.getCurrentStateTime());
			this.batteriesOutputPower.setNewValue(0.0,
												  this.getCurrentStateTime());
			break;
		default:
			throw new NeoSim4JavaException("unknown batteries state!");
		}

		// update the last derivative with the one at the current time
		this.lastDerivative.setNewValue(
				this.computeCurrentCapacityDerivative(), 
				this.getCurrentStateTime());

		// tracing
		if (DEBUG) {
			this.logMessage(
					"compute next state (end) at "
					+ this.getCurrentStateTime()
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevel
					+ " batteriesInputPower = "
					+ this.batteriesInputPower.getValue()
					+ " batteriesOutputPower = "
					+ this.batteriesOutputPower.getValue()
					+ " batteriesRequiredPower = "
					+ this.batteriesRequiredPower.getValue());
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
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevel
					+ " batteriesInputPower = "
					+ this.batteriesInputPower.getValue()
					+ " batteriesOutputPower = "
					+ this.batteriesOutputPower.getValue()
					+ " batteriesRequiredPower = "
					+ this.batteriesRequiredPower.getValue());
		} else if (VERBOSE) {
			this.logMessage(
					"userDefinedInternalTransition at "
					+ this.getCurrentStateTime()
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevel);
		}

		// Invariant checking
		assert	BatteriesPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriePowerModel.implementationInvariants(this)");
		assert	BatteriesPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: BatteriePowerModel."
						+ "invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void		userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		if (!(this.currentState.equals(State.IDLE) ||
									this.currentState.equals(State.EMPTY))) {
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

		// as an internal event will be triggered immediately, it must not
		// change the level that has already been updated
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
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevel
					+ " batteriesInputPower = "
					+ this.batteriesInputPower.getValue()
					+ " batteriesOutputPower = "
					+ this.batteriesOutputPower.getValue()
					+ " batteriesRequiredPower = "
					+ this.batteriesRequiredPower.getValue());
		} else if (VERBOSE) {
			this.logMessage(
					"userDefinedExternalTransition at "
					+ this.getCurrentStateTime()
					+ " on event " + e
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevel);
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
					+ " currentState = " + this.currentState
					+ " currentLevel = " + this.currentLevel);
		}
		super.endSimulation(endTime);
	}
}
// -----------------------------------------------------------------------------
