package etape2.equipments.generator.mil;



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

import etape2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import etape2.equipments.generator.mil.events.Refill;
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicHIOA;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorUnitTesterModel</code> implements a model that
 * executes unit test scenarios for the generator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This atomic HIOA simulation model is defined as solely executing test
 * scenarios (@see fr.sorbonne_u.components.hem2025.tests_utils.AbstractTestScenarioBasedAtomicHIOA)
 * for more explanations.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code TankEmpty},
 *   {@code TankNoLongerEmpty}</li>
 * <li>Exported events:
 *   {@code Start},
 *   {@code Stop},
 *   {@code Refill},
 *   {@code GeneratorRequiredPowerChanged}</li>
 * <li>Imported variables:
 *   name = {@code generatorOutputPower}, type = {@code Double}</li>
 * <li>Exported variables: none</li>
 *   name = {@code generatorRequiredPower}, type = {@code Double}</li>
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
 * <p>Created on : 2025-10-20</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		exported = {Start.class, Stop.class, Refill.class,
					GeneratorRequiredPowerChanged.class},
		imported = {TankEmpty.class, TankNoLongerEmpty.class})
@ModelImportedVariable(name = "generatorOutputPower", type = Double.class)
@ModelExportedVariable(name = "generatorRequiredPower", type = Double.class)
//-----------------------------------------------------------------------------
public class			GeneratorUnitTesterModel
extends		AbstractTestScenarioBasedAtomicHIOA
implements	TankLevelManagementI
{
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
	public static final String		URI =
								GeneratorUnitTesterModel.class.getSimpleName();
	/**	name of the run parameter for the test scenario to be executed.		*/
	public static final String		TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";
	/**	name of the run parameter for the initial fuel level of the tank
	 *  in {@code MeasurementUnit.LITERS}.									*/
	public static final String		INITIAL_LEVEL_RP_NAME = "INITIAL_LEVEL";

	/** initial fuel level of the tank in {@code MeasurementUnit.LITERS}.	*/
	protected double				initialLevel;
	/** when true, the generator tank is empty, otherwise, it is false.		*/
	protected boolean				tankEmpty;
	/** power required by the electric circuit from the generator in
	 *  {@code MeasurementUnit.AMPERES}.									*/
	@ExportedVariable(type = Double.class)
	protected Value<Double>			generatorRequiredPower = new Value<>(this);
	/** power delivered to the electric circuit by the generator in
	 *  {@code MeasurementUnit.AMPERES}.									*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			generatorOutputPower;

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
	public				GeneratorUnitTesterModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());
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
		String testScenarioName = ModelI.createRunParameterName(this.getURI(),
														TEST_SCENARIO_RP_NAME);
		String initialLevelName = ModelI.createRunParameterName(this.getURI(),
														INITIAL_LEVEL_RP_NAME);

		// Preconditions checking
		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(testScenarioName) :
				new MissingRunParameterException(testScenarioName);
		assert	simParams.containsKey(initialLevelName) :
				new MissingRunParameterException(initialLevelName);

		this.setTestScenario((TestScenarioWithSimulation)
										simParams.get(testScenarioName));
		this.initialLevel = (double) simParams.get(initialLevelName);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		if (this.initialLevel < TOLERANCE) {
			this.tankEmpty = true;
		} else {
			this.tankEmpty = false;
		}

		super.initialiseState(initialTime);
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

		// the variable generatorRequiredPower is exported and does not depend
		// upon any other variable, hence it can be immediately initialised
		if (!this.generatorRequiredPower.isInitialised()) {
			this.generatorRequiredPower.initialise(0.0);
			numberOfNewlyInitialisedVariables++;
			if (DEBUG) {
				this.logMessage(
						"fixpointInitialiseVariables generatorRequiredPower = "
						+ this.generatorRequiredPower.getValue());
			}
		}

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
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#notTankEmpty()
	 */
	@Override
	public boolean		notTankEmpty()
	{
		return !this.tankEmpty;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#signalTankEmpty()
	 */
	@Override
	public void			signalTankEmpty()
	{
		this.tankEmpty = true;
		this.generatorRequiredPower.setNewValue(0.0, this.getCurrentStateTime());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#signalTankNoLongerEmpty()
	 */
	@Override
	public void			signalTankNoLongerEmpty()
	{
		this.tankEmpty = false;
	}

	/**
	 * set a new value for the generator required power; used in test scenarios.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code v >= 0.0}
	 * pre	{@code t != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param v	new value for the generator required power.
	 * @param t	simulated time at which this new value if assigned.
	 */
	public void			setGeneratorRequiredPower(double v, Time t)
	{
		assert	v >= 0.0 :
				new NeoSim4JavaException("Precondition violation: v >= 0.0");
		assert	t != null :
				new NeoSim4JavaException("Precondition violation: t != null");
		assert	t.greaterThanOrEqual(this.generatorRequiredPower.getTime()) :
				new NeoSim4JavaException(
					"t.greaterThanOrEqual(generatorRequiredPower.getTime()))");

		this.generatorRequiredPower.setNewValue(v, t);
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicHIOA#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// tracing
		if (VERBOSE) {
			this.logMessage(
					"userDefinedInternalTransition at "
					+ this.getCurrentStateTime()
					+ ": tank empty = " + this.tankEmpty
					+ ", generatorRequiredPower " + this.generatorRequiredPower
					+ ", generatorOutputPower " + this.generatorOutputPower);
		}
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
		if (VERBOSE) {
			this.logMessage(
					"userDefinedExternalTransition at "
					+ this.getCurrentStateTime()
					+ " on event " + e
					+ ": tank empty = " + this.tankEmpty
					+ ", generatorRequiredPower = " + this.generatorRequiredPower
					+ ", generatorOutputPower = " + this.generatorOutputPower);
		}
	}
}
// -----------------------------------------------------------------------------
