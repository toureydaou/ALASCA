package etape4;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape3.equipements.coffee_machine.CoffeeMachineCyPhy;
import etape3.equipements.coffee_machine.CoffeeMachineTesterCyPhy;
import etape3.equipements.meter.ElectricMeterCyPhy;
import etape4.equipements.hem.HEMEnergyManager;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

/**
 * The class <code>CVMIntegrationTest</code> implements the integration test for
 * Etape 4 Phase 1: Basic energy management control loop.
 *
 * <p>
 * <strong>Test Objective:</strong> Verify that the HEM Energy Manager can detect
 * deficits and suspend equipment appropriately.
 * </p>
 *
 * <p>
 * <strong>Test Setup:</strong>
 * <ul>
 * <li>1 Coffee Machine</li>
 * <li>1 Electric Meter</li>
 * <li>1 HEM Energy Manager with control loop</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Test Scenario:</strong>
 * <ol>
 * <li>Turn on coffee machine (creates consumption)</li>
 * <li>Wait for control loop to detect deficit and suspend coffee machine</li>
 * <li>Verify suspension occurred</li>
 * </ol>
 * </p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public class CVMIntegrationTest extends AbstractCVM {

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** Delay before starting test scenarios (ms) */
	public static long DELAY_TO_START = 3000L;

	/** Duration to sleep at end before exiting JVM (ms) */
	public static long END_SLEEP_DURATION = 80000L;  // 80s to cover 60s scenario + margin

	/** Execution mode for this test */
	public static ExecutionMode GLOBAL_EXECUTION_MODE = ExecutionMode.INTEGRATION_TEST;

	/** Clock URI for time synchronization */
	public static String CLOCK_URI = "etape4-integration-test-clock";

	/** Start instant for test scenarios */
	public static Instant START_INSTANT = Instant.parse("2026-02-06T08:00:00.00Z");

	/** End instant for test scenarios (60 seconds after start) */
	public static Instant END_INSTANT = START_INSTANT.plusSeconds(60);

	/** Acceleration factor for real-time tests (no acceleration for simple tests) */
	public static double ACCELERATION_FACTOR = 1.0;

	// -------------------------------------------------------------------------
	// Component URIs
	// -------------------------------------------------------------------------

	// Coffee Machine
	public static final String COFFEE_MACHINE_1_REFLECTION_URI = "COFFEE-1-RIP";
	public static final String COFFEE_MACHINE_1_USER_URI = "COFFEE-1-USER";
	public static final String COFFEE_MACHINE_1_INTERNAL_URI = "COFFEE-1-INTERNAL";
	public static final String COFFEE_MACHINE_1_EXTERNAL_URI = "COFFEE-1-EXTERNAL";

	// Electric Meter
	public static final String ELECTRIC_METER_REFLECTION_URI = "METER-RIP";

	// HEM Energy Manager (uses default from HEMCyPhy)
	public static final String HEM_REFLECTION_URI = "hem-RIP-URI";

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean implementationInvariants(CVMIntegrationTest cvm) {
		assert cvm != null : new PreconditionException("cvm != null");
		return true;
	}

	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
			CLOCK_URI != null && !CLOCK_URI.isEmpty(),
			CVMIntegrationTest.class,
			"CLOCK_URI != null && !CLOCK_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
			DELAY_TO_START >= 0,
			CVMIntegrationTest.class,
			"DELAY_TO_START >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
			START_INSTANT != null,
			CVMIntegrationTest.class,
			"START_INSTANT != null");
		return ret;
	}

	// -------------------------------------------------------------------------
	// CVM Life-cycle
	// -------------------------------------------------------------------------

	public CVMIntegrationTest() throws Exception {
		super();
	}

	@Override
	public void deploy() throws Exception {
		assert staticInvariants() : "Static invariants not satisfied!";

		// Enable verbose logging
		HEMEnergyManager.VERBOSE = true;
		HEMEnergyManager.CONTROL_LOOP_VERBOSE = true;
		CoffeeMachineCyPhy.VERBOSE = true;
		ElectricMeterCyPhy.VERBOSE = true;

		// Create test scenario
		TestScenario testScenario = this.createTestScenario();

		// Create components
		this.createClockServer();
		this.createElectricMeter(testScenario);
		this.createCoffeeMachine1(testScenario);
		this.createHEM(testScenario);

		// Deploy
		super.deploy();

		assert implementationInvariants(this);
	}

	// -------------------------------------------------------------------------
	// Component Creation
	// -------------------------------------------------------------------------

	/**
	 * Create the clocks server.
	 *
	 * @throws Exception if creation fails
	 */
	protected void createClockServer() throws Exception {
		// Calculate Unix epoch start time in nanoseconds
		long unixEpochStartTimeInMillis = System.currentTimeMillis() + DELAY_TO_START;

		AbstractComponent.createComponent(
			ClocksServer.class.getCanonicalName(),
			new Object[] {
				CLOCK_URI,
				TimeUnit.MILLISECONDS.toNanos(unixEpochStartTimeInMillis),
				START_INSTANT,
				ACCELERATION_FACTOR
			});
	}

	/**
	 * Create the electric meter component.
	 *
	 * @param testScenario test scenario
	 * @throws Exception if creation fails
	 */
	protected void createElectricMeter(TestScenario testScenario) throws Exception {
		AbstractComponent.createComponent(
			ElectricMeterCyPhy.class.getCanonicalName(),
			new Object[] {
				GLOBAL_EXECUTION_MODE,
				CLOCK_URI
			});
	}

	/**
	 * Create coffee machine instance 1.
	 *
	 * @param testScenario test scenario
	 * @throws Exception if creation fails
	 */
	protected void createCoffeeMachine1(TestScenario testScenario) throws Exception {
		// Create coffee machine CyPhy component
		AbstractComponent.createComponent(
			CoffeeMachineCyPhy.class.getCanonicalName(),
			new Object[] {
				true,  // isIntegrationTestMode
				GLOBAL_EXECUTION_MODE,
				CLOCK_URI
			});

		// Create coffee machine tester
		AbstractComponent.createComponent(
			CoffeeMachineTesterCyPhy.class.getCanonicalName(),
			new Object[] {
				CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
				GLOBAL_EXECUTION_MODE,
				testScenario
			});
	}

	/**
	 * Create the HEM Energy Manager component.
	 *
	 * @param testScenario test scenario
	 * @throws Exception if creation fails
	 */
	protected void createHEM(TestScenario testScenario) throws Exception {
		AbstractComponent.createComponent(
			HEMEnergyManager.class.getCanonicalName(),
			new Object[] {
				GLOBAL_EXECUTION_MODE,
				testScenario
			});
	}

	// -------------------------------------------------------------------------
	// Test Scenario
	// -------------------------------------------------------------------------

	/**
	 * Create the test scenario for Phase 1.
	 *
	 * <p>
	 * Scenario:
	 * <ul>
	 * <li>t=10s: Turn on coffee machine (creates consumption)</li>
	 * <li>t=60s: Control loop should have detected deficit and suspended</li>
	 * </ul>
	 * </p>
	 *
	 * @return test scenario
	 */
	protected TestScenario createTestScenario() {
		TestStepI[] steps = new TestStepI[] {
			// Step 1: Turn on coffee machine at t=10s and set to MAX power
			new TestStep(
				CLOCK_URI,
				CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
				START_INSTANT.plusSeconds(10),
				owner -> {
					try {
						CoffeeMachineTesterCyPhy tester = (CoffeeMachineTesterCyPhy) owner;
						tester.getCmUserOP().turnOn();
						System.out.println("[TEST] Coffee machine turned ON");
						// Set power level to MAX (1500W) to create significant consumption
						tester.getCmExternalOP().setCurrentPowerLevel(
							new fr.sorbonne_u.alasca.physical_data.Measure<Double>(
								1500.0,
								fr.sorbonne_u.alasca.physical_data.MeasurementUnit.WATTS));
						System.out.println("[TEST] Coffee machine power set to MAX (1500W = ~6.8A)");
					} catch (Exception e) {
						System.out.println("[TEST ERROR] Failed to turn on coffee machine: " + e.getMessage());
						e.printStackTrace();
					}
				}
			),

			// Step 2: Check status at t=40s (after control loop should have acted)
			new TestStep(
				CLOCK_URI,
				HEM_REFLECTION_URI,
				START_INSTANT.plusSeconds(40),
				owner -> {
					System.out.println("[TEST] ========================================");
					System.out.println("[TEST] Test checkpoint: Control loop should have suspended coffee machine");
					System.out.println("[TEST] Check logs above for suspension confirmation");
					System.out.println("[TEST] ========================================");
				}
			),

			// Step 3: Final summary at end
			new TestStep(
				CLOCK_URI,
				HEM_REFLECTION_URI,
				END_INSTANT.minusSeconds(5),
				owner -> {
					System.out.println("\n[TEST] ========================================");
					System.out.println("[TEST] PHASE 1 TEST COMPLETE");
					System.out.println("[TEST] Expected behavior:");
					System.out.println("[TEST] 1. Coffee machine turned ON at t=10s");
					System.out.println("[TEST] 2. Control loop detected deficit (no production, some consumption)");
					System.out.println("[TEST] 3. Coffee machine suspended by control loop");
					System.out.println("[TEST] Review logs for 'SUSPENDED' messages");
					System.out.println("[TEST] ========================================\n");
				}
			)
		};

		return new TestScenario(CLOCK_URI, START_INSTANT, END_INSTANT, steps);
	}

	// -------------------------------------------------------------------------
	// Execution
	// -------------------------------------------------------------------------

	@Override
	public void finalise() throws Exception {
		super.finalise();
	}

	@Override
	public void shutdown() throws Exception {
		super.shutdown();
	}

	public static void main(String[] args) {
		try {
			CVMIntegrationTest cvm = new CVMIntegrationTest();
			cvm.startStandardLifeCycle(DELAY_TO_START + END_SLEEP_DURATION);
			Thread.sleep(END_SLEEP_DURATION);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
