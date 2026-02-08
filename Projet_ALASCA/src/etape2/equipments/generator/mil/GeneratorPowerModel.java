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
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
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
 * The class <code>GeneratorPowerModel</code> implements the simulation model
 * for the generator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation model very interesting as it is a typical example of a hybrid
 * system where not only model events (here external) can interrupt the
 * continuous evolution of the model but also two physical events caused by the
 * evolution of the continuous part of the model itself, namely the evolution of
 * the fuel level of the generator tank and of the required power from the
 * batteries. Obviously, when producing power, the fuel level cannot go under 0
 * (empty tank).
 * </p>
 * <p>
 * As such, this model gathers all of the ingredients that make the simulation
 * of hybrid systems an issue, though here the simplicity of the differential
 * equations used (linear) eases the prediction of the time at which the
 * physical event triggered transition simple and precise. The case of the
 * required power is a bit more complicated, as explained below.
 * </p>
 * <p>
 * In this implementation, the Quantised State System (QSS) approach is used to
 * perform the numerical integration of the differential equations modelling the
 * evolution of the fuel level. A standard level quantum is defined as a
 * configuration parameter. At each integration step, the algorithm adds the
 * quantum to the current fuel level and plan the next internal transition 
 * after the time required to observe a change in the fuel level of the amount
 * given by this quantum. When the fuel level is about to reach a limit, the
 * empty tank, the quantum is temporarily adapted to exactly reach the limit at
 * the next computation step, and the time required to reach the limit will be
 * used to plan the next internal transition.
 * </p>
 * <p>
 * This model also exhibit another important feature: it has external events
 * which occurrence can happen before the next internal transition is reached.
 * When this happens, the current fuel level must be updated but not of the
 * amount of the quantum but only the amount corresponding to the elapsed time
 * since the last internal event given the current evaluation of the fuel level
 * derivative (evaluated at the last internal transition). Hence, when an
 * external transition occurs, the model first update the fuel level using the
 * standard time quantum computation (the amount of level that must be added is
 * obtained by multiplying the current derivative by the elapsed time. After
 * updating the fuel level, the state of the generator is updated according to
 * the type of event that just occurred and then the simulation model state
 * is updated the entire model given the new generator state.
 * </p>
 * <p>
 * To simulate the delivery of power from the generator, the idea is that to
 * have the generator delivering energy after being started, an imported
 * variable pushes the user required from the generator. Given the required
 * power, the generator put in an exported variable the actual output power that
 * may be limited by the maximum output power of the generator. In theory, if
 * these continuous variables are evaluated step by step, when the user of the
 * generator set a new value of required power from the generator, the generator
 * model will notice that at its next computational step and update its output
 * power accordingly. Unfortunately, this does not mix well with the QSS
 * computational approach as the generator model may take a long time before
 * performing a new computational step after a change of the required power by
 * the user model, hence maybe introducing a large time gap and discrepancies
 * in the variables values between the two. Therefore, as the change in the
 * required power is voluntary, the generator model assumes that it will receive
 * an external event at the time of the required power change notifying it of
 * the shared continuous state change that just occurred, hence triggering an
 * immediate update of the output power.
 * </p>
 * <p>
 * Note that the implementation of this model is made a bit more complicated by
 * the fact that several parameters are made akin to user choice at the
 * beginning of the simulation runs, like the standard fuel level quantum, the
 * fuel tank maximum capacity, etc.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code !currentLevel.isInitialised() || (currentLevel.getValue() >= 0.0 && currentLevel.getValue() < maxCapacity)}
 * invariant	{@code !generatorOutputPower.isInitialised() || (generatorOutputPower.getValue() >= 0.0 && generatorOutputPower.getValue() <= totalMaximumOutputPower)}
 * invariant	{@code !generatorRequiredPower.isInitialised() || (generatorRequiredPower.getValue() >= 0.0)}
 * invariant	{@code (!generatorOutputPower.isInitialised() || !generatorRequiredPower.isInitialised()) || (generatorOutputPower.getValue() <= generatorRequiredPower.getValue())}
 * invariant	{@code !lastDerivative.isInitialised() || (lastDerivative.getValue() <= 0.0)}
 * invariant	{@code standardLevelQuantum > 0.0}
 * invariant	{@code Math.abs(currentLevelQuantum) >= 0.0 && Math.abs(currentLevelQuantum <= standardLevelQuantum)}
 * invariant	{@code !generatorOutputPower.isInitialised() || (!currentState.equals(State.TANK_EMPTY) || generatorOutputPower.getValue() < TOLERANCE)}
 * invariant	{@code !generatorOutputPower.isInitialised() || (!currentState.equals(State.PRODUCING) || generatorOutputPower.getValue() > 0.0)}
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
	imported = {Start.class, Stop.class, GeneratorRequiredPowerChanged.class,
				TankEmpty.class, TankNoLongerEmpty.class},
	exported = {GeneratorRequiredPowerChanged.class})
@ModelExportedVariable(name = "generatorOutputPower", type = Double.class)
@ModelImportedVariable(name = "generatorRequiredPower", type = Double.class)
//-----------------------------------------------------------------------------
public class			GeneratorPowerModel
extends		AtomicHIOA
implements	TankLevelManagementI,
			GeneratorStateManagementI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean			VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean			DEBUG = true;
	/** when comparing floating point values, use this tolerance to get
	 *  the result of the comparison.										*/
	protected static final double	TOLERANCE  = 1.0e-08;

	/** single model URI.													*/
	public static final String	URI =
									GeneratorPowerModel.class.getSimpleName();
	
	/**	name of the run parameter for the maximum out power of the generator
	 *  in {@code MeasurementUnit.WATTS}.									*/
	public static final String	MAX_OUT_POWER_RP_NAME = "MAX_OUT_POWER_RP_NAME";
	
	/** total maximal out power provided when producing power in
	 *  {@code MeasurementUnit.WATTS}.	 									*/
	protected double			totalMaximumOutputPower;

	/** current state of the generator.										*/
	protected State				currentState;
	/** when true, the model must emit an event of type
	 *  {@code SharedContinuousStateChange}.								*/
	protected boolean			signalSharedContinuousStateChange;

	/** current power required by the electric circuit from the generator
	 *  in {@code MeasurementUnit.AMPERES}; it is set by the electric
	 *  meter according to the difference between the overall current power
	 *  production and the current power consumption.						*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>		generatorRequiredPower;
	/** current power delivered to the electric circuit by the generator
	 *  in {@code MeasurementUnit.AMPERES}.									*/
	@ExportedVariable(type = Double.class)
	protected Value<Double>		generatorOutputPower = new Value<>(this);

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
		GeneratorPowerModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= GeneratorPowerModel.staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.generatorOutputPower.isInitialised() ||
					(instance.generatorOutputPower.getValue() >= 0.0 &&
						instance.generatorOutputPower.getValue() <=
											instance.totalMaximumOutputPower),
				GeneratorPowerModel.class, instance,
				"!generatorOutputPower.isInitialised() || (generatorOutputPower"
				+ ".getValue() >= 0.0 && generatorOutputPower.getValue() <= "
				+ "totalMaximumOutputPower)");
		ret &= AssertionChecking.checkImplementationInvariant(
				(instance.generatorRequiredPower == null ||
						!instance.generatorRequiredPower.isInitialised()) ||
					(instance.generatorRequiredPower.getValue() >= 0.0),
				GeneratorPowerModel.class, instance,
				"!generatorRequiredPower.isInitialised() || "
				+ "(generatorRequiredPower.getValue() >= 0.0)");
		ret &= AssertionChecking.checkImplementationInvariant(
				(!instance.generatorOutputPower.isInitialised() ||
							!instance.generatorRequiredPower.isInitialised()) ||
					(instance.generatorOutputPower.getValue() <=
									instance.generatorRequiredPower.getValue()),
				GeneratorPowerModel.class, instance,
				"(!generatorOutputPower.isInitialised() || "
				+ "!generatorRequiredPower.isInitialised()) || "
				+ "(generatorOutputPower.getValue() <= generatorRequiredPower."
				+ "getValue())");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.generatorOutputPower.isInitialised() ||
					(!instance.currentState.equals(State.PRODUCING) ||
								instance.generatorOutputPower.getValue() > 0.0),
				GeneratorPowerModel.class, instance,
				"!generatorOutputPower.isInitialised() || (!currentState."
				+ "equals(State.PRODUCING) || generatorOutputPower."
				+ "getValue() > 0.0)");
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
	protected static boolean	invariants(GeneratorPowerModel instance)
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
	public				GeneratorPowerModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert	GeneratorPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorPowerModel.implementationInvariants(this)");
		assert	GeneratorPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorPowerModel."
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
		String maxOutPowerName = ModelI.createRunParameterName(this.getURI(),
													MAX_OUT_POWER_RP_NAME);

		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(maxOutPowerName) :
				new MissingRunParameterException(maxOutPowerName);

		this.totalMaximumOutputPower = (double) simParams.get(maxOutPowerName);

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
		this.signalSharedContinuousStateChange = false;

		// Invariant checking
		assert	GeneratorPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorPowerModel.implementationInvariants(this)");
		assert	GeneratorPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: "
						+ "GeneratorPowerModel.invariants(this)");

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

		// the variables generatorOutputPower is the one that this model holds
		// and that must be initialised, but it depends upon the imported
		// variable generatorRequiredPower
		if (!this.generatorOutputPower.isInitialised()) {
			// if sunIntensityCoef has been initialised, then
			// solarPanelPowerProduction can be initialised
			if (this.generatorRequiredPower.isInitialised()) {
				double outPower = this.generatorRequiredPower.getValue();
				if (outPower > this.totalMaximumOutputPower) {
					outPower = this.totalMaximumOutputPower;
				} 
				this.generatorOutputPower.initialise(outPower);
				// and then the number of newly initialised variable is 1
				numberOfNewlyInitialisedVariables++;
				// tracing
				if (DEBUG) {
					this.logMessage(
							"fixpointInitialiseVariables batteriesOutputPower = "
							+ this.generatorOutputPower.getValue());
				}
			} else {
				// otherwise, solarPanelPowerProduction cannot be initialised
				// and the number of variables not initialised by this execution
				// is one
				numberOfStillNotInitialisedVariables++;
			}
		}

		// Invariant checking
		assert	GeneratorPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorPowerModel.implementationInvariants(this)");
		assert	GeneratorPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorPowerModel."
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
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#notTankEmpty()
	 */
	@Override
	public boolean		notTankEmpty()
	{
		return !this.currentState.equals(State.TANK_EMPTY);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#signalTankEmpty()
	 */
	@Override
	public void			signalTankEmpty()
	{
		if (!this.currentState.equals(State.OFF)) {
			this.currentState = State.TANK_EMPTY;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#signalTankNoLongerEmpty()
	 */
	@Override
	public void			signalTankNoLongerEmpty()
	{
		this.currentState = State.OFF;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		Duration ret = null;
		if (this.signalSharedContinuousStateChange) {
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
		if (this.signalSharedContinuousStateChange) {
			ArrayList<EventI> ret = new ArrayList<EventI>();
			ret.add(new GeneratorRequiredPowerChanged(this.getTimeOfNextEvent()));
			this.signalSharedContinuousStateChange = false;

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
					+ ", generatorOutputPower = " + this.generatorOutputPower
					+ ", generatorRequiredPower = " + this.generatorRequiredPower);
		}

		// update the power levels, in and out
		switch (this.currentState) {
		case PRODUCING:
			if (this.generatorRequiredPower.getValue() < TOLERANCE) {
				this.generatorOutputPower.setNewValue(
												0.0,
												this.getCurrentStateTime());
				this.currentState = State.IDLE;
			} else {
				// when producing, the user can require some power level, but the
				// generator cannot produce more than its maximum power
				if (this.generatorRequiredPower.getValue() * 
							Generator.OUTPUT_AC_TENSION.getData()
											<= this.totalMaximumOutputPower) {
					this.generatorOutputPower.setNewValue(
										this.generatorRequiredPower.getValue(),
										this.getCurrentStateTime());
				} else {
					this.generatorOutputPower.setNewValue(
								this.totalMaximumOutputPower/
										Generator.OUTPUT_AC_TENSION.getData(),
								this.getCurrentStateTime());
				}
			}
			break;
		case IDLE:
			if (this.generatorRequiredPower.getValue() >= TOLERANCE) {
				if (this.generatorRequiredPower.getValue() * 
						Generator.OUTPUT_AC_TENSION.getData()
											<= this.totalMaximumOutputPower) {
					this.generatorOutputPower.setNewValue(
									this.generatorRequiredPower.getValue(),
									this.getCurrentStateTime());
				} else {
					this.generatorOutputPower.setNewValue(
								this.totalMaximumOutputPower/
										Generator.OUTPUT_AC_TENSION.getData(),
								this.getCurrentStateTime());
				}
				this.currentState = State.PRODUCING;
			}
			break;
		case TANK_EMPTY:
		case OFF:
			this.generatorOutputPower.setNewValue(0.0,
												  this.getCurrentStateTime());
		}

		// Invariant checking
		assert	GeneratorPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorPowerModel.implementationInvariants(this)");
		assert	GeneratorPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorPowerModel."
						+ "invariants(this)");

		// tracing
		if (DEBUG) {
			this.logMessage(
				"compute next state (end) at "
				+ this.getCurrentStateTime()
				+ ": currentState = " + this.currentState
				+ ", generatorOutputPower = " + this.generatorOutputPower
				+ ", generatorRequiredPower = " + this.generatorRequiredPower);
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
				+ ", generatorOutputPower = " + this.generatorOutputPower
				+ ", generatorRequiredPower = " + this.generatorRequiredPower);
		} else if (VERBOSE) {
			this.logMessage(
				"userDefinedInternalTransition at "
				+ this.getCurrentStateTime()
				+ ": currentState = " + this.currentState);
		}


		// Invariant checking
		assert	GeneratorPowerModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "GeneratorPowerModel.implementationInvariants(this)");
		assert	GeneratorPowerModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: GeneratorPowerModel."
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

		// complete the update of the state of the model
		this.computeNextState();

		if (e instanceof GeneratorRequiredPowerChanged) {
			// a state change on the required power will trigger a state change
			// on the output power to be signalled to the fuel model
			this.signalSharedContinuousStateChange = true;
		}

		// tracing
		if (DEBUG) {
			this.logMessage(
					"userDefinedExternalTransition at "
					+ this.getCurrentStateTime()
					+ ": currentState = " + this.currentState
					+ ", generatorOutputPower = " + this.generatorOutputPower
					+ ", generatorRequiredPower = " + this.generatorRequiredPower);
		} else if (VERBOSE) {
			this.logMessage(
					"userDefinedExternalTransition at "
					+ this.getCurrentStateTime()
					+ " on event " + e
					+ ": currentState = " + this.currentState);
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
