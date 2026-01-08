package etape3.equipements.coffee_machine;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape3.equipements.coffee_machine.CoffeeMachineController.ControlMode;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.models.time.TimeUtils;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMStandardUnitTest</code> performs unit tests for the coffee machine
 * component.
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
 * invariant	{@code true}	// no more invariant
 * </pre>
 *
 * <p>Created on : 2026-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class CVMUnitTest extends AbstractCVM {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** delay before starting the test scenarios, leaving time to build
	 *  and initialise the components and their simulators; this delay is
	 *  estimated given the complexity of the initialisation (including the
	 *  creation of the application simulator if simulation is used). It
	 *  could need to be revised if the computer on which the application
	 *  is run is less powerful.											*/
	public static long DELAY_TO_START = 10000L;
	/** duration of the sleep at the end of the execution before exiting
	 *  the JVM.															*/
	public static long END_SLEEP_DURATION = 1000000L;

	/** time unit in which {@code SIMULATION_DURATION} is expressed.		*/
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	/** start time of the simulation, in simulated logical time, if
	 *  relevant.															*/
	public static Time SIMULATION_START_TIME = new Time(0.0, SIMULATION_TIME_UNIT);
	/** duration  of the simulation, in simulated time.						*/
	public static Duration SIMULATION_DURATION = new Duration(6.0, SIMULATION_TIME_UNIT);
	/** for real time simulations, the acceleration factor applied to the
	 *  the simulated time to get the execution time of the simulations. 	*/
	public static double ACCELERATION_FACTOR = 3600.0;
	/** duration of the execution.											*/
	public static long EXECUTION_DURATION =
			DELAY_TO_START +
				TimeUnit.NANOSECONDS.toMillis(
						TimeUtils.toNanos(
								SIMULATION_DURATION.getSimulatedDuration()/
													ACCELERATION_FACTOR,
								SIMULATION_DURATION.getTimeUnit()));

	/** the execution mode for the coffee machine component, to select among
	 *  the values of the enumeration {@code ExecutionMode}.				*/
	public static ExecutionMode COFFEE_MACHINE_EXECUTION_MODE =
//			ExecutionMode.STANDARD;
										ExecutionMode.UNIT_TEST;
//											ExecutionMode.UNIT_TEST_WITH_SIL_SIMULATION;

	/** the execution mode for the coffee machine tester component, to select
	 *  among the values of the enumeration {@code ExecutionMode}.			*/
	public static ExecutionMode COFFEE_MACHINE_TESTER_EXECUTION_MODE =
//											ExecutionMode.STANDARD;
											ExecutionMode.UNIT_TEST;

	/** for unit tests and SIL simulation unit tests, a {@code Clock} is
	 *  used to get a time-triggered synchronisation of the actions of
	 *  the components in the test scenarios.								*/
	public static String CLOCK_URI = "coffee-machine-test-clock";
	/** start instant in test scenarios, as a string to be parsed.			*/
	public static String START_INSTANT = "2026-10-08T00:00:00.00Z";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public CVMUnitTest() throws Exception {
		CoffeeMachineTesterCyPhy.VERBOSE = true;
		CoffeeMachineTesterCyPhy.X_RELATIVE_POSITION = 0;
		CoffeeMachineTesterCyPhy.Y_RELATIVE_POSITION = 1;
		CoffeeMachineCyPhy.VERBOSE = true;
		CoffeeMachineCyPhy.X_RELATIVE_POSITION = 1;
		CoffeeMachineCyPhy.Y_RELATIVE_POSITION = 1;
		CoffeeMachineController.VERBOSE = true;
		CoffeeMachineController.X_RELATIVE_POSITION = 2;
		CoffeeMachineController.Y_RELATIVE_POSITION = 1;
	}

	// -------------------------------------------------------------------------
	// CVM life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void deploy() throws Exception {
		if (COFFEE_MACHINE_EXECUTION_MODE.isStandard()) {
			
			System.out.println("Mode Standard Unit Test");

			// Nota: the coffee machine controller cannot run in standard mode as the
			// coffee machine does not have the required physical sensors

			AbstractComponent.createComponent(
					CoffeeMachineCyPhy.class.getCanonicalName(),
					new Object[]{});

			AbstractComponent.createComponent(
					CoffeeMachineTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI
						});

		} else if (COFFEE_MACHINE_EXECUTION_MODE.isTestWithoutSimulation()) {
			
			System.out.println("Mode Without Simulation");

			// Nota: the coffee machine controller cannot run in test without simulation
			// mode as the coffee machine does not have the required physical sensors

			long current = System.currentTimeMillis();
			// start time of the components in Unix epoch time in milliseconds.
			long unixEpochStartTimeInMillis = current + DELAY_TO_START;
			// start instant used for time-triggered synchronisation in unit tests
			// and SIL simulation runs.
			Instant	startInstant = Instant.parse(START_INSTANT);
			// test scenario to be executed for unit tests with simulation
			TestScenario testScenario = unitTestScenario();

			AbstractComponent.createComponent(
					CoffeeMachineCyPhy.class.getCanonicalName(),
					new Object[]{
						COFFEE_MACHINE_EXECUTION_MODE,
						testScenario.getClockURI()
						});

			AbstractComponent.createComponent(
					CoffeeMachineTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						COFFEE_MACHINE_TESTER_EXECUTION_MODE,
						testScenario
						});

			AbstractComponent.createComponent(
					ClocksServer.class.getCanonicalName(),
					new Object[]{
							// URI of the clock to retrieve it
							CLOCK_URI,
							// start time in Unix epoch time
							TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
							// start instant synchronised with the start time
							startInstant,
							ACCELERATION_FACTOR
					});

		} else {
			assert COFFEE_MACHINE_EXECUTION_MODE.isSimulationTest();

			System.out.println("Mode Simulation");
			
			long current = System.currentTimeMillis();
			// start time of the components in Unix epoch time in milliseconds.
			long unixEpochStartTimeInMillis = current + DELAY_TO_START;
			// start instant used for time-triggered synchronisation in unit
			// tests and SIL simulation runs.
			Instant	startInstant = Instant.parse(START_INSTANT);
			// test scenario to be executed for unit tests with simulation
			TestScenario testScenario = unitTestScenarioWithSimulation();

			AbstractComponent.createComponent(
					CoffeeMachineCyPhy.class.getCanonicalName(),
					new Object[]{
						CoffeeMachineCyPhy.REFLECTION_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.SENSOR_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.ACTUATOR_INBOUND_PORT_URI,
						COFFEE_MACHINE_EXECUTION_MODE,
						testScenario,
						CoffeeMachineCyPhy.UNIT_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR
						});

			AbstractComponent.createComponent(
					CoffeeMachineController.class.getCanonicalName(),
					new Object[]{
						CoffeeMachineCyPhy.SENSOR_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.ACTUATOR_INBOUND_PORT_URI,
						CoffeeMachineController.STANDARD_TEMPERATURE_HYSTERESIS,
						CoffeeMachineController.STANDARD_CONTROL_PERIOD,
						ControlMode.PULL,
						COFFEE_MACHINE_EXECUTION_MODE,
						ACCELERATION_FACTOR
						});

			AbstractComponent.createComponent(
					CoffeeMachineTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						COFFEE_MACHINE_TESTER_EXECUTION_MODE,
						testScenario
						});

			AbstractComponent.createComponent(
					ClocksServerWithSimulation.class.getCanonicalName(),
					new Object[]{
							// URI of the clock to retrieve it
							CLOCK_URI,
							// start time in Unix epoch time
							TimeUnit.MILLISECONDS.toNanos(
											 		unixEpochStartTimeInMillis),
							// start instant synchronised with the start time
							startInstant,
							ACCELERATION_FACTOR,
							DELAY_TO_START,
							SIMULATION_START_TIME,
							SIMULATION_DURATION});
		}

		super.deploy();
	}

	public static void main(String[] args) {
		try {
			VerboseException.VERBOSE = true;
			VerboseException.PRINT_STACK_TRACE = true;

			CVMUnitTest cvm = new CVMUnitTest();
			cvm.startStandardLifeCycle(EXECUTION_DURATION);
			Thread.sleep(END_SLEEP_DURATION);
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------
	// Test scenarios
	// -------------------------------------------------------------------------

	/**
	 * return a test scenario without simulation for testing the coffee machine
	 * component.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * The test includes multiple steps to be executed by the coffee machine unit tester
	 * component: switching on the coffee machine, filling water, setting various modes,
	 * starting/stopping heating, making coffee, and switching it off.
	 * </p>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	a test scenario for the unit testing of the coffee machine component.
	 * @throws VerboseException	<i>to do</i>.
	 */
	public static TestScenario unitTestScenario() throws VerboseException {
		Instant startInstant = Instant.parse(START_INSTANT);
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = startInstant.plusSeconds(d);

		Instant switchOnInstant = startInstant.plusSeconds(300);
		Instant fillWaterInstant = startInstant.plusSeconds(600);
		Instant setEcoModeInstant = startInstant.plusSeconds(3900);
		Instant setNormalModeInstant = startInstant.plusSeconds(7500);
		Instant setMaxModeInstant = startInstant.plusSeconds(11100);
		Instant startHeatingInstant = startInstant.plusSeconds(14700);
		Instant makeExpressoInstant = startInstant.plusSeconds(18300);
		Instant stopHeatingInstant = startInstant.plusSeconds(20100);
		Instant switchOffInstant = startInstant.plusSeconds(d - 300);

		return new TestScenario(
			CLOCK_URI,
			startInstant,
			endInstant,
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOnInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					fillWaterInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().fillWater();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					setEcoModeInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setEcoMode();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					setNormalModeInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setNormalMode();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					setMaxModeInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setMaxMode();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					startHeatingInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmInternalOP().startHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					makeExpressoInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					stopHeatingInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmInternalOP().stopHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOffInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOff();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					})
			});
	}

	/**
	 * return a test scenario for testing with SIL simulation the coffee machine
	 * component.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * The test includes basic steps to be executed by the coffee machine unit tester
	 * component: switching on the coffee machine, filling water, making coffee, and
	 * switching it off. The controller handles the temperature regulation.
	 * </p>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	a test scenario for the unit testing of the coffee machine component.
	 * @throws VerboseException	<i>to do</i>.
	 */
	public static TestScenarioWithSimulation unitTestScenarioWithSimulation()
	throws VerboseException {
		Instant startInstant = Instant.parse(START_INSTANT);
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = startInstant.plusSeconds(d);

		Instant switchOnInstant = startInstant.plusSeconds(300);
		Instant fillWaterInstant = startInstant.plusSeconds(600);
		Instant makeExpressoInstant = startInstant.plusSeconds(d/2);
		Instant switchOffInstant = startInstant.plusSeconds(d - 300);

		return new TestScenarioWithSimulation(
			CLOCK_URI,
			startInstant,
			endInstant,
			"global-archi", // no global archi in fact
			SIMULATION_START_TIME,
			(ts, simParams) -> { },
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOnInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					fillWaterInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().fillWater();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					makeExpressoInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOffInstant,
					owner ->  {
						try {
							((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOff();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					})
				});
	}
}
// -----------------------------------------------------------------------------
