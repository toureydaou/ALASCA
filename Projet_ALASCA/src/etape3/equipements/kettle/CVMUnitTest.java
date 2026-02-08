package etape3.equipements.kettle;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

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
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMUnitTest</code> performs unit tests for the kettle
 * component with clock synchronisation but no simulation.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class CVMUnitTest extends AbstractCVM {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static long DELAY_TO_START = 10000L;
	public static long END_SLEEP_DURATION = 1000000L;

	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	public static Time SIMULATION_START_TIME =
		new Time(0.0, SIMULATION_TIME_UNIT);
	public static Duration SIMULATION_DURATION =
		new Duration(6.0, SIMULATION_TIME_UNIT);
	public static double ACCELERATION_FACTOR = 3600.0;
	public static long EXECUTION_DURATION =
			DELAY_TO_START +
				TimeUnit.NANOSECONDS.toMillis(
						TimeUtils.toNanos(
								SIMULATION_DURATION.getSimulatedDuration()/
													ACCELERATION_FACTOR,
								SIMULATION_DURATION.getTimeUnit()));

	public static ExecutionMode KETTLE_EXECUTION_MODE =
											ExecutionMode.UNIT_TEST;

	public static ExecutionMode KETTLE_TESTER_EXECUTION_MODE =
											ExecutionMode.UNIT_TEST;

	public static String CLOCK_URI = "kettle-test-clock";
	public static String START_INSTANT = "2026-10-08T00:00:00.00Z";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public CVMUnitTest() throws Exception {
		KettleTesterCyPhy.VERBOSE = true;
		KettleTesterCyPhy.X_RELATIVE_POSITION = 0;
		KettleTesterCyPhy.Y_RELATIVE_POSITION = 1;
		KettleCyPhy.VERBOSE = true;
		KettleCyPhy.X_RELATIVE_POSITION = 1;
		KettleCyPhy.Y_RELATIVE_POSITION = 1;
		KettleController.VERBOSE = true;
		KettleController.X_RELATIVE_POSITION = 2;
		KettleController.Y_RELATIVE_POSITION = 1;
	}

	// -------------------------------------------------------------------------
	// CVM life-cycle
	// -------------------------------------------------------------------------

	@Override
	public void deploy() throws Exception {
		if (KETTLE_EXECUTION_MODE.isStandard()) {

			AbstractComponent.createComponent(
					KettleCyPhy.class.getCanonicalName(),
					new Object[]{false});

			AbstractComponent.createComponent(
					KettleTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						KettleCyPhy.USER_INBOUND_PORT_URI,
						KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI
						});

		} else if (KETTLE_EXECUTION_MODE.isTestWithoutSimulation()) {

			long current = System.currentTimeMillis();
			long unixEpochStartTimeInMillis = current + DELAY_TO_START;
			Instant	startInstant = Instant.parse(START_INSTANT);
			TestScenario testScenario = unitTestScenario();

			AbstractComponent.createComponent(
					KettleCyPhy.class.getCanonicalName(),
					new Object[]{
							false,
						KETTLE_EXECUTION_MODE,
						testScenario.getClockURI()
						});

			AbstractComponent.createComponent(
					KettleTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						KettleCyPhy.USER_INBOUND_PORT_URI,
						KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						KETTLE_TESTER_EXECUTION_MODE,
						testScenario
						});

			AbstractComponent.createComponent(
					ClocksServer.class.getCanonicalName(),
					new Object[]{
							CLOCK_URI,
							TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
							startInstant,
							ACCELERATION_FACTOR
					});

		} else {
			assert KETTLE_EXECUTION_MODE.isSimulationTest();

			long current = System.currentTimeMillis();
			long unixEpochStartTimeInMillis = current + DELAY_TO_START;
			Instant	startInstant = Instant.parse(START_INSTANT);
			TestScenario testScenario = unitTestScenarioWithSimulation();

			AbstractComponent.createComponent(
					KettleCyPhy.class.getCanonicalName(),
					new Object[]{
							false,
						KettleCyPhy.REFLECTION_INBOUND_PORT_URI,
						KettleCyPhy.USER_INBOUND_PORT_URI,
						KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						KettleCyPhy.SENSOR_INBOUND_PORT_URI,
						KettleCyPhy.ACTUATOR_INBOUND_PORT_URI,
						KETTLE_EXECUTION_MODE,
						testScenario,
						KettleCyPhy.UNIT_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR
						});

			AbstractComponent.createComponent(
					KettleController.class.getCanonicalName(),
					new Object[]{
						KettleCyPhy.SENSOR_INBOUND_PORT_URI,
						KettleCyPhy.ACTUATOR_INBOUND_PORT_URI,
						KettleController.STANDARD_HYSTERESIS,
						KettleController.STANDARD_CONTROL_PERIOD,
						KettleController.ControlMode.PULL,
						KETTLE_EXECUTION_MODE,
						ACCELERATION_FACTOR
						});

			AbstractComponent.createComponent(
					KettleTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						KettleCyPhy.USER_INBOUND_PORT_URI,
						KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						KETTLE_TESTER_EXECUTION_MODE,
						testScenario
						});

			AbstractComponent.createComponent(
					ClocksServerWithSimulation.class.getCanonicalName(),
					new Object[]{
							CLOCK_URI,
							TimeUnit.MILLISECONDS.toNanos(
											 		unixEpochStartTimeInMillis),
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

	public static TestScenario unitTestScenario() throws VerboseException {
		Instant startInstant = Instant.parse(START_INSTANT);
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = startInstant.plusSeconds(d);

		Instant switchOnInstant = startInstant.plusSeconds(300);
		Instant setMaxModeInstant = startInstant.plusSeconds(3900);
		Instant startHeatingInstant = startInstant.plusSeconds(7500);
		Instant stopHeatingInstant = startInstant.plusSeconds(11100);
		Instant switchOffInstant = startInstant.plusSeconds(d - 300);

		return new TestScenario(
			CLOCK_URI,
			startInstant,
			endInstant,
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOnInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().turnOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					setMaxModeInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().setMode(
								KettleMode.MAX);
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					startHeatingInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().startHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					stopHeatingInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().stopHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOffInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().turnOff();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					})
			});
	}

	public static TestScenarioWithSimulation unitTestScenarioWithSimulation()
	throws VerboseException {
		Instant startInstant = Instant.parse(START_INSTANT);
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = startInstant.plusSeconds(d);

		Instant switchOnInstant = startInstant.plusSeconds(300);
		Instant setMaxModeInstant = startInstant.plusSeconds(600);
		Instant startHeatingInstant = startInstant.plusSeconds(1200);
		Instant switchOffInstant = startInstant.plusSeconds(d - 300);

		return new TestScenarioWithSimulation(
			CLOCK_URI,
			startInstant,
			endInstant,
			"global-archi",
			SIMULATION_START_TIME,
			(ts, simParams) -> { },
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOnInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().turnOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					setMaxModeInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().setMode(
								KettleMode.MAX);
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					startHeatingInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().startHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					}),
				new TestStep(
					CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOffInstant,
					owner -> {
						try {
							((KettleTesterCyPhy)owner).getKuOP().turnOff();
						} catch (Exception e) {
							throw new BCMRuntimeException(e);
						}
					})
			});
	}
}
// -----------------------------------------------------------------------------
