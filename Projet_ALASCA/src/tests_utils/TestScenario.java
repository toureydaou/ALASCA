package tests_utils;

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

import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

// -----------------------------------------------------------------------------
/**
 * The class <code>TestScenario</code> implements a test scenario to be executed
 * by simulators defined in the DEVS simulation framework NeoSim4Java. 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A test scenario describe a simulation run were some of the actions taken by
 * simulation models are imposed by the scenario. Hence, at the heart of a test
 * scenario, there is a sequence of test steps performed by a set of simulation
 * models included in the simulator and identified by their URI. Each test step
 * is defined by the class {@code SimulationTestStep}.
 * </p>
 * <p>
 * The time management used in test steps and test scenarios is based on two
 * time lines. Test steps are planned using the class {@code Instant}. A test
 * scenario has a start instant and an end instant. The time of occurrence of a
 * test step is an instant between the start and end instants. This time line of
 * instants is mapped to the time line of the simulation clock represented by
 * the class {@code Time} of NeoSim4Java. The test scenario aligns the two time
 * lines by having as input the start instant, the end instant and the start
 * time in simulated time. The end time in simulated time as well as the
 * simulation duration are deduced from former input parameters and the
 * alignment of the two time lines.
 * </p>
 * <p>
 * Each test step represents an action to be taken by some simulation model as
 * an internal transition, perhaps exporting some external events. Steps are
 * provided by an array of {@code SimulationTestStep} that must be ordered by
 * ascending instants of occurrence. To help constructing complex scenarios
 * intertwining actions performed by different simulation models, the array
 * of steps contains all of the actions in a scenario, including all simulation
 * models the scenario will involve. However, the execution of the test scenario
 * makes advances per simulation models.
 * </p>
 * <p>
 * As a test scenario is a simulation run, some set up may be necessary, mainly
 * to define simulation run parameters for the simulation models appearing in
 * the simulator. {@code  TestScenario} proposes two set up types of functions,
 * among which one must be provide at creation time:
 * </p>
 * <ul>
 * <li>a function of two parameters taking as arguments the reference to the
 *   simulator and the test scenario about to be executed, and</li>
 * <li>a function of three parameters taking the same two arguments plus
 *   a map of run parameters already created and that must be initialised
 *   on the simulator models.</li>
 * </ul>
 * <p>
 * Optional beginning and ending messages can also be provided. To execute a
 * test scenario, the standard DEVS protocol methods in models having steps to
 * performed call this class methods. First, in the class implementing the
 * simulation run, the simulator must be constructed as usual and then the
 * set up function provided by the test scenario must be called. Then, models
 * that do not perform steps defined in the scenario executes as usual. Models
 * that must perform steps defined in the scenario uses the following methods
 * of this class:
 * </p>
 * <ul>
 * <li>The methods {@code setUpSimulator} are called to set up the simulation
 *   run parameters and other configuration options.</li>
 * <li>The methods {@code getStartTime()}, to retrieve the simulated start time,
 *   and {@code getEndTime()}, to retrieve the simulated end time, to start the
 *   simulation by calling the chosen start method of NeoSim4Java.</li>
 * <li>Models having to perform test steps can retrieve the time of their
 *   next step by calling the methods {@code simulatedTimeOfNextStep} or
 *   {@code simulatedDelayToNextStep}, typically in their method
 *   {@code timeAdvance} to know whether their next internal transition in their
 *   next test step or another internal transition.</li>
 * <li>When a model must execute a test step, the method {@code generateOutput}
 *   is called in their {@code output} method to generate and export the
 *   external events of the test step.</li>
 * <li>Next, the method {@code performInternalTransition} is called by their
 *   method {@code userDefinedInternalTransition} to perform the internal
 *   transition of the test step.</li>
 * <li>Finally, the method {@code advanceToNextStep} is called to advance the
 *   test scenario for that model to its next test step. The method
 *   {@code scenarioTerminated} allows to check if the scenario is terminated
 *   for the model.</li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code !(setUp2 != null && setUp3 != null) && !(setUp2 == null && setUp3 == null)}
 * invariant	{@code simulationTestSteps != null && simulationTestSteps.length > 0}
 * invariant	{@code performingAtomicModelsURIs != null && performingAtomicModelsURIs.size() > 0}
 * invariant	{@code nextSteps != null}
 * invariant	{@code nextSteps.keySet().stream().allMatch(uri -> performingAtomicModelsURIs.contains(uri))}
 * invariant	{@code nextSteps.values().stream().allMatch(index -> index >= 0 && index <= instance.simulationTestSteps.length)}
 * invariant	{@code startInstant != null}
 * invariant	{@code endInstant != null}
 * invariant	{@code startInstant.isBefore(endInstant)}
 * invariant	{@code startTime}
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
public class			TestScenario
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The functional interface <code>TriConsumer</code> defines the type of
	 * functions of three arguments that returns no result.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2025-10-31</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	@FunctionalInterface
	public static interface		TriConsumer<T, U, V>
	{
		/**
		 * apply the function with the given arguments.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param t	first argument.
		 * @param u	second argument.
		 * @param v	third argument.
		 */
		public void		accept(T t, U u, V v);
	}
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, trace the test scenario.									*/
	public static boolean					VERBOSE = true;
	/** when true, print debugging information on the test scenario.		*/
	public static boolean					DEBUG = true;

	/** message to be print at the beginning of the scenario when
	 *  {@code VERBOSE} is true.											*/
	protected final String					beginningMessage;
	/** message to be print at the end of the scenario when {@code VERBOSE}
	 *  is true.															*/
	protected final String					endingMessage;

	/** perform the necessary set up for the scenario, before starting the
	 *  simulation run, typically to set configuration and run parameters
	 *  that are added to the third argument.	*/
	protected final TriConsumer<SimulatorI, TestScenario, Map<String, Object>>
											setUp3;
	/** perform the necessary set up for the scenario, before starting the
	 *  simulation run, typically to set configuration and run parameters.	*/
	protected final BiConsumer<SimulatorI, TestScenario>	setUp2;
	/** the steps in the test scenario.										*/
	protected final SimulationTestStep[]	simulationTestSteps;
	/** URIs of the simulation models performing test actions in this
	 *  scenario.														 	*/
	protected final Set<String>				performingAtomicModelsURIs;

	/** indexes if the next steps for every simulation models.				*/
	protected final Map<String, Integer>	nextSteps;
	/** start instant of the simulation run.								*/
	protected final Instant					startInstant;
	/** end instant of the simulation run.									*/
	protected final Instant					endInstant;
	/** start time of the simulation run in simulated time.					*/
	protected final Time					startTime;

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
	protected static boolean	implementationInvariants(TestScenario instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nextSteps.keySet().stream().allMatch(
					uri -> instance.performingAtomicModelsURIs.contains(uri)),
				TestScenario.class, instance,
				"nextSteps.keySet().stream().allMatch("
				+ "uri -> performingAtomicModelsURIs.contains(uri)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nextSteps.values().stream().allMatch(
					index -> index >= 0 &&
								index <= instance.simulationTestSteps.length),
				TestScenario.class, instance,
				"nextSteps.values().stream().allMatch("
				+ "index -> index >= 0 && index <= simulationTestSteps.length)");
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
	protected static boolean	invariants(TestScenario instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a test scenario with the given set up and simulated test steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code startInstant != null}
	 * pre	{@code endInstant != null}
	 * pre	{@code startInstant.isBefore(endInstant)}
	 * pre	{@code startTime != null}
	 * pre	{@code setUp2 != null}
	 * pre	{@code simulationTestSteps != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param startInstant			start instant of the simulation run.
	 * @param endInstant			end instant of the simulation run.
	 * @param startTime				start time of the simulation run in simulated time.
	 * @param setUp2				function that performs the initial set up for this test scenario.
	 * @param simulationTestSteps	simulation steps in the scenario.
	 */
	public				TestScenario(
		Instant startInstant,
		Instant endInstant,
		Time startTime,
		BiConsumer<SimulatorI, TestScenario> setUp2,
		SimulationTestStep[] simulationTestSteps
		)
	{
		this(null, null, startInstant, endInstant, startTime, setUp2, null,
			 simulationTestSteps);
	}

	/**
	 * create a test scenario with the given set up and simulated test steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code startInstant != null}
	 * pre	{@code endInstant != null}
	 * pre	{@code startInstant.isBefore(endInstant)}
	 * pre	{@code startTime != null}
	 * pre	{@code setUp2 != null}
	 * pre	{@code simulationTestSteps != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param beginingMessage		message to be output on sysout at the beginning of the scenario.
	 * @param endingMessage			message to be output on sysout at the end of the scenario.
	 * @param startInstant			start instant of the simulation run.
	 * @param endInstant			end instant of the simulation run.
	 * @param startTime				start time of the simulation run in simulated time.
	 * @param setUp2				function that performs the initial set up for this test scenario.
	 * @param simulationTestSteps	simulation steps in the scenario.
	 * @throws Exception			<i>to do</i>.
	 */
	public				TestScenario(
		String beginingMessage,
		String endingMessage,
		Instant startInstant,
		Instant endInstant,
		Time startTime,
		BiConsumer<SimulatorI, TestScenario> setUp2,
		SimulationTestStep[] simulationTestSteps
		)
	{
		this(beginingMessage, endingMessage,
			 startInstant, endInstant, startTime, setUp2, null,
			 simulationTestSteps);
	}

	/**
	 * create a test scenario with the given set up and simulated test steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code startInstant != null}
	 * pre	{@code endInstant != null}
	 * pre	{@code startInstant.isBefore(endInstant)}
	 * pre	{@code startTime != null}
	 * pre	{@code setUp3 != null}
	 * pre	{@code simulationTestSteps != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param startInstant			start instant of the simulation run.
	 * @param endInstant			end instant of the simulation run.
	 * @param startTime				start time of the simulation run in simulated time.
	 * @param setUp3				function that performs the initial set up for this test scenario from an initial map of run parameters.
	 * @param simulationTestSteps	simulation steps in the scenario.
	 */
	public				TestScenario(
		Instant startInstant,
		Instant endInstant,
		Time startTime,
		TriConsumer<SimulatorI, TestScenario, Map<String, Object>> setUp3,
		SimulationTestStep[] simulationTestSteps
		)
	{
		this(null, null, startInstant, endInstant, startTime, null, setUp3,
			 simulationTestSteps);
	}

	/**
	 * create a test scenario with the given set up and simulated test steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code startInstant != null}
	 * pre	{@code endInstant != null}
	 * pre	{@code startInstant.isBefore(endInstant)}
	 * pre	{@code startTime != null}
	 * pre	{@code setUp3 != null}
	 * pre	{@code simulationTestSteps != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param beginingMessage		message to be output on sysout at the beginning of the scenario.
	 * @param endingMessage			message to be output on sysout at the end of the scenario.
	 * @param startInstant			start instant of the simulation run.
	 * @param endInstant			end instant of the simulation run.
	 * @param startTime				start time of the simulation run in simulated time.
	 * @param setUp3				function that performs the initial set up for this test scenario from an initial map of run parameters.
	 * @param simulationTestSteps	simulation steps in the scenario.
	 */
	public				TestScenario(
		String beginingMessage,
		String endingMessage,
		Instant startInstant,
		Instant endInstant,
		Time startTime,
		TriConsumer<SimulatorI, TestScenario, Map<String, Object>> setUp3,
		SimulationTestStep[] simulationTestSteps
		)
	{
		this(beginingMessage, endingMessage,
			 startInstant, endInstant, startTime, null, setUp3,
			 simulationTestSteps);
	}

	/**
	 * create a test scenario with the given messages, set up and simulated
	 * test steps.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code startInstant != null}
	 * pre	{@code endInstant != null}
	 * pre	{@code startInstant.isBefore(endInstant)}
	 * pre	{@code startTime != null}
	 * pre	{@code !(setUp2 != null && setUp3 != null) && !(setUp2 == null && setUp3 == null)}
	 * pre	{@code simulationTestSteps != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param beginingMessage		message to be output on sysout at the beginning of the scenario.
	 * @param endingMessage			message to be output on sysout at the end of the scenario.
	 * @param startInstant			start instant of the simulation run.
	 * @param endInstant			end instant of the simulation run.
	 * @param startTime				start time of the simulation run in simulated time.
	 * @param setUp2				function that performs the initial set up for this test scenario.
	 * @param setUp3				function that performs the initial set up for this test scenario from an initial map of run parameters.
	 * @param simulationTestSteps	simulation steps in the scenario.
	 */
	public				TestScenario(
		String beginingMessage,
		String endingMessage,
		Instant startInstant,
		Instant endInstant,
		Time startTime,
		BiConsumer<SimulatorI, TestScenario> setUp2,
		TriConsumer<SimulatorI, TestScenario, Map<String, Object>> setUp3,
		SimulationTestStep[] simulationTestSteps
		)
	{
		// Preconditions checking
		assert	startInstant != null :
				new PreconditionException("startInstant != null");
		assert	endInstant != null :
				new PreconditionException("endInstant != null");
		assert	startInstant.isBefore(endInstant) :
				new PreconditionException("startInstant.isBefore(endInstant)");
		assert	startTime != null :
				new PreconditionException("startTime != null");
		assert	simulationTestSteps != null :
				new PreconditionException("simulationTestSteps != null");
		assert	!(setUp2 != null && setUp3 != null) &&
										!(setUp2 == null && setUp3 == null) :
				new PreconditionException(
						"!(setUp2 != null && setUp3 != null) && "
						+ "!(setUp2 == null && setUp3 == null)");
		assert	ordered(simulationTestSteps) :
				new PreconditionException("ordered(simulationTestSteps)");

		this.beginningMessage = beginingMessage;
		this.endingMessage = endingMessage;
		this.startInstant = startInstant;
		this.endInstant = endInstant;
		this.startTime = startTime;
		this.setUp2 = setUp2;
		this.setUp3 = setUp3;
		this.simulationTestSteps = simulationTestSteps;

		this.performingAtomicModelsURIs = new HashSet<>();
		this.nextSteps = new HashMap<>();
		for (int i = 0 ; i < simulationTestSteps.length ; i++) {
			String uri = simulationTestSteps[i].getPerformingAtomicModelURI();
			this.performingAtomicModelsURIs.add(uri);
			if (!this.nextSteps.containsKey(uri)) {
				// the first step in which the model with this URI appears in
				this.nextSteps.put(uri, i);
			}
		}

		// Invariant checking
		assert	TestScenario.implementationInvariants(this) :
				new ImplementationInvariantException(
						"TestScenario.implementationInvariants(this)");
		assert	TestScenario.invariants(this) :
				new InvariantException("TestScenario.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the steps in {@code simulationTestSteps} appear in
	 * increasing order of time of occurrence.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param simulationTestSteps	simulation steps in a scenario.
	 * @return						true if the steps in {@code simulationTestSteps} appear in increasing order of time of occurrence.
	 */
	public static boolean	ordered(SimulationTestStep[] simulationTestSteps)
	{
		assert	simulationTestSteps != null :
				new PreconditionException("simulationTestSteps != null");

		if (simulationTestSteps.length > 1) {
			boolean ret = true;
			Instant old = simulationTestSteps[0].getTimeOfOccurrence();
			for (int i = 1 ; ret && i < simulationTestSteps.length ; i++) {
				Instant current = simulationTestSteps[i].getTimeOfOccurrence();
				ret &= old.isBefore(current) || old.equals(current);
				old = current;
			}
			return ret;
		} else {
			return true;
		}
	}

	/**
	 * return the start instant of this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the start instant of this test scenario.
	 */
	public Instant		getStartInstant()
	{
		return this.startInstant;
	}

	/**
	 * return the end instant of this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the end instant of this test scenario.
	 */
	public Instant		getEndInstant()
	{
		return this.endInstant;
	}

	/**
	 * return the simulated start time of this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the simulated start time of this test scenario.
	 */
	public Time			getStartTime()
	{
		return this.startTime;
	}

	/**
	 * return the simulated end time of this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the simulated end time of this test scenario.
	 */
	public Time			getEndTime()
	{
		return this.startTime.add(
					TimeUtils.betweenInDuration(startInstant,
												endInstant,
												this.startTime.getTimeUnit()));
	}

	/**
	 * return true if {@code uri} is the URI of an atomic model performing some
	 * action in this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri	an URI.
	 * @return		true if {@code uri} is the URI of an atomic model performing some actions in this test scenario.
	 */
	public boolean		atomicModelAppearsIn(String uri)
	{
		assert	uri != null && !uri.isEmpty() :
				new PreconditionException("uri != null && !uri.isEmpty()");

		return this.performingAtomicModelsURIs.contains(uri);
	}

	/**
	 * run the set up for this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code se != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param se							the simulation engine that will run the simulation.
	 * @throws MissingRunParameterException if an expected run parameter does not appear in {@code simParams} (including {@code simParams} being {@code null}).
	 */
	public void			setUpSimulator(SimulatorI se)
	throws MissingRunParameterException
	{
		assert	se != null : new PreconditionException("se != null");
		
		if (VERBOSE) {
			System.out.println(this.beginningMessage);
		}
		this.setUp2.accept(se, this);
	}

	/**
	 * run the set up for this test scenario from an initial map of run
	 * parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code se != null}
	 * pre	{@code SignatureMethodParameterSpec != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param se							the simulation engine that will run the simulation.
	 * @param simParams						map from parameters names to their values.
	 * @throws MissingRunParameterException if an expected run parameter does not appear in {@code simParams} (including {@code simParams} being {@code null}).
	 */
	public void			setUpSimulator(
		SimulatorI se,
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		assert	se != null : new PreconditionException("se != null");

		assert	this.setUp3 != null :
				new BCMException("setUp3 != null");

		if (VERBOSE) {
			System.out.println(this.beginningMessage);
		}
		this.setUp3.accept(se, this, simParams);
	}

	/**
	 * return the delay until the next test step for {@code m} in simulated time.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code occurrence != null && (this.getStartInstant().isBefore(occurrence) || this.getStartInstant().equals(occurrence))}
	 * post	{@code return != null && getStartTime().lessThanOrEqual(return)}
	 * </pre>
	 *
	 * @param occurrence	an instant in the simulation.
	 * @return				the simulated time corresponding to {@code occurrence}.
	 */
	protected Time		toSimulatedTime(Instant occurrence)
	{
		assert	occurrence != null &&
					(this.getStartInstant().isBefore(occurrence)
							|| this.getStartInstant().equals(occurrence)) :
				new PreconditionException(
						"occurrence != null && (this.getStartInstant()."
						+ "isBefore(occurrence) || this.getStartInstant()."
						+ "equals(occurrence))");

		Duration d =
				TimeUtils.betweenInDuration(this.startInstant,
											occurrence,
											this.getStartTime().getTimeUnit());

		return this.startTime.add(d);
	}

	/**
	 * return the simulated time at which the next test step for {@code m}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	an atomic model performing actions in the current test scenario.
	 * @return	the time at which the next test step for {@code m} in simulated time.
	 */
	public Time			simulatedTimeOfNextStep(AtomicModel m)
	{
		if (!this.scenarioTerminated(m)) {
			return this.toSimulatedTime(this.getInstantOfNextStep(m));
		} else {
			return Time.INFINITY;
		}
	}

	/**
	 * return the delay until the next test step for {@code m} in simulated time.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	an atomic model performing actions in the current test scenario.
	 * @return	the delay until the next test step for {@code m} in simulated time.
	 */
	public Duration		simulatedDelayToNextStep(AtomicModel m)
	{
		assert	m != null && this.atomicModelAppearsIn(m.getURI()) :
				new PreconditionException(
						"m != null && atomicModelAppearsIn(m.getURI())");

		if (!this.scenarioTerminated(m)) {
			return this.simulatedTimeOfNextStep(m).
											subtract(m.getCurrentStateTime());
		} else {
			return Duration.INFINITY;
		}
	}

	/**
	 * return the instant of occurrence of the next step in this scenario for
	 * {@code m}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	the atomic model on which the internal transition must be executed.
	 * @return	the instant of occurrence of the next step in this scenario for {@code m}.
	 */
	public Instant		getInstantOfNextStep(AtomicModel m)
	{
		assert	m != null && this.atomicModelAppearsIn(m.getURI()) :
				new PreconditionException(
						"m != null && atomicModelAppearsIn(m.getURI())");

		return this.simulationTestSteps[this.nextSteps.get(m.getURI())].
														getTimeOfOccurrence();
	}

	/**
	 * generate the events to be output by the atomic simulation model {@code m}
	 * at the time {@code m.getTimeOfNextEvent()} in this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m		the atomic model on which the internal transition must be executed.
	 * @return		a list of events to be output by the simulation model.
	 */
	public ArrayList<EventI>	generateOutput(AtomicModel m)
	{
		assert	m != null && this.atomicModelAppearsIn(m.getURI()) :
				new PreconditionException(
						"m != null && atomicModelAppearsIn(m.getURI())");

		return this.simulationTestSteps[this.nextSteps.get(m.getURI())].
															generateOutput(m);
	}

	/**
	 * perform the next internal transition for the atomic model {@code m} in
	 * this test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m			the atomic model on which the internal transition must be executed.
	 */
	public void			performInternalTransition(AtomicModel m)
	{
		assert	m != null && this.atomicModelAppearsIn(m.getURI()) :
				new PreconditionException(
						"m != null && atomicModelAppearsIn(m.getURI())");

		this.simulationTestSteps[this.nextSteps.get(m.getURI())].
												performInternalTransition(m);
	}

	/**
	 * return the index of the next step in the scenario that the model
	 * {@code m} must perform or an index out of the range of the simulated
	 * steps in this tests scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	an atomic model that must perform an internal transition in this scenario.
	 * @return	the index of the next step in the scenario that the model m must perform.
	 */
	protected int		findNextStepIndex(AtomicModel m)
	{
		assert	m != null && this.atomicModelAppearsIn(m.getURI()) :
				new PreconditionException(
						"m != null && atomicModelAppearsIn(m.getURI())");

		int ret = this.nextSteps.get(m.getURI()) + 1;
		for (int i = ret ; i < this.simulationTestSteps.length ; i++) {
			if (!m.getURI().equals(
					this.simulationTestSteps[i].getPerformingAtomicModelURI())) {
				ret++;
			} else {
				break;
			}
		}
		return ret;
	}

	/**
	 * return true if {@code m} has not terminated its actions in this scenario
	 * yet.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	the atomic model on which the internal transition must be executed.
	 * @return	true if {@code m} has not terminated its actions in this scenario yet.
	 */
	public boolean		scenarioTerminated(AtomicModel m)
	{
		assert	m != null && this.atomicModelAppearsIn(m.getURI()) :
				new PreconditionException(
						"m != null && atomicModelAppearsIn(m.getURI())");

		return this.nextSteps.get(m.getURI()) >=
											this.simulationTestSteps.length;
	}

	/**
	 * advance to the next test step in this scenario for {@code m}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && atomicModelAppearsIn(m.getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	an atomic model that must perform an internal transition in this scenario.
	 */
	public void			advanceToNextStep(AtomicModel m)
	{
		assert	m != null && this.atomicModelAppearsIn(m.getURI()) :
				new PreconditionException(
						"m != null && atomicModelAppearsIn(m.getURI())");

		this.nextSteps.put(m.getURI(), this.findNextStepIndex(m));

		// Invariant checking
		assert	TestScenario.implementationInvariants(this) :
				new ImplementationInvariantException(
						"TestScenario.implementationInvariants(this)");
		assert	TestScenario.invariants(this) :
				new InvariantException("TestScenario.invariants(this)");
	}
}
// -----------------------------------------------------------------------------
