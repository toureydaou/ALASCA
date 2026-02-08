package etape4;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape3.equipements.coffee_machine.CoffeeMachineController;
import etape3.equipements.coffee_machine.CoffeeMachineCyPhy;
import etape3.equipements.coffee_machine.CoffeeMachineTesterCyPhy;
import etape3.equipements.fan.FanCyPhy;
import etape3.equipements.fan.FanTesterCyPhy;
import etape3.equipements.hem.HEMCyPhy;
import etape3.equipements.kettle.KettleController;
import etape3.equipements.kettle.KettleCyPhy;
import etape3.equipements.kettle.KettleTesterCyPhy;
import etape3.equipements.laundry.LaundryController;
import etape3.equipements.laundry.LaundryCyPhy;
import etape3.equipements.laundry.LaundryTesterCyPhy;
import etape3.equipements.meter.ElectricMeterCyPhy;
import etape4.equipements.hem.HEMEnergyManager;
import etape4.equipments.batteries.BatteriesCyPhy;
import etape4.equipments.generator.GeneratorCyPhy;
import etape4.equipments.solar_panel.SolarPanelCyPhy;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

/**
 * The class <code>CVMIntegrationTest</code> implements the rich integration
 * test for Etape 4: Energy management control loop.
 *
 * Equipment: 3 CoffeeMachines (HEM-controlled), 2 Kettles (HEM-controlled),
 * 1 Fan (uncontrolled, consumption-only).
 * Energy sources: Generator, Batteries, SolarPanel.
 *
 * <p><strong>Test Scenario (~100s real-time):</strong></p>
 * <ul>
 * <li>Phase 1 (t=0-10s): Coffee-1 and Fan-1 activated</li>
 * <li>Phase 2 (t=10-30s): Coffee-2 and Kettle-1 activated</li>
 * <li>Phase 3 (t=30-50s): Coffee-3 and Fan-2 activated</li>
 * <li>Phase 4 (t=50-70s): Equipment deactivation</li>
 * <li>Phase 5 (t=70-100s): Second wave with Coffee-1 only</li>
 * </ul>
 *
 * <p><strong>Note on Fans:</strong> Fans are NOT registered with HEM.
 * They simply consume power and report to ElectricMeter. Only CoffeeMachine
 * and Kettle are controlled by HEM.</p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public class CVMIntegrationTest extends AbstractCVM {

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** Delay before starting test scenarios (ms) */
	public static long DELAY_TO_START = 8000L;

	/** Duration to sleep at end before exiting JVM (ms) */
	public static long END_SLEEP_DURATION = 120000L;

	/** Execution mode for this test */
	public static ExecutionMode GLOBAL_EXECUTION_MODE = ExecutionMode.INTEGRATION_TEST;

	/** Clock URI for time synchronization */
	public static String CLOCK_URI = "etape4-integration-test-clock";

	/** Start instant for test scenarios */
	public static Instant START_INSTANT = Instant.parse("2026-02-06T08:00:00.00Z");

	/** End instant for test scenarios (100 seconds after start) */
	public static Instant END_INSTANT = START_INSTANT.plusSeconds(100);

	/** Acceleration factor (1.0 = real-time) */
	public static double ACCELERATION_FACTOR = 1.0;

	/** Number of solar panel square meters */
	public static final int NB_OF_SQUARE_METERS = 10;

	// -------------------------------------------------------------------------
	// Tester Reflection URIs (unique per instance)
	// -------------------------------------------------------------------------

	public static final String CM_TESTER_1_RIP = "CM-TESTER-1-RIP";
	public static final String CM_TESTER_2_RIP = "CM-TESTER-2-RIP";
	public static final String CM_TESTER_3_RIP = "CM-TESTER-3-RIP";
	public static final String KETTLE_TESTER_1_RIP = "KETTLE-TESTER-1-RIP";
	/** Fan tester uses its own static REFLECTION_INBOUND_PORT_URI */
	public static final String FAN_TESTER_RIP = FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI;

	// HEM Reflection URI
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
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		HEMCyPhy.VERBOSE = true;
		HEMCyPhy.X_RELATIVE_POSITION = 0;
		HEMCyPhy.Y_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.VERBOSE = true;
		ElectricMeterCyPhy.X_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.Y_RELATIVE_POSITION = 0;
		BatteriesCyPhy.VERBOSE = true;
		BatteriesCyPhy.X_RELATIVE_POSITION = 1;
		BatteriesCyPhy.Y_RELATIVE_POSITION = 1;
		SolarPanelCyPhy.VERBOSE = true;
		SolarPanelCyPhy.X_RELATIVE_POSITION = 2;
		SolarPanelCyPhy.Y_RELATIVE_POSITION = 1;
		GeneratorCyPhy.VERBOSE = true;
		GeneratorCyPhy.X_RELATIVE_POSITION = 3;
		GeneratorCyPhy.Y_RELATIVE_POSITION = 1;
		FanTesterCyPhy.VERBOSE = true;
		FanTesterCyPhy.X_RELATIVE_POSITION = 0;
		FanTesterCyPhy.Y_RELATIVE_POSITION = 2;
		FanCyPhy.VERBOSE = true;
		FanCyPhy.X_RELATIVE_POSITION = 1;
		FanCyPhy.Y_RELATIVE_POSITION = 2;
		CoffeeMachineTesterCyPhy.VERBOSE = true;
		CoffeeMachineTesterCyPhy.X_RELATIVE_POSITION = 0;
		CoffeeMachineTesterCyPhy.Y_RELATIVE_POSITION = 3;
		CoffeeMachineCyPhy.VERBOSE = true;
		CoffeeMachineCyPhy.X_RELATIVE_POSITION = 1;
		CoffeeMachineCyPhy.Y_RELATIVE_POSITION = 3;
		CoffeeMachineController.VERBOSE = true;
		CoffeeMachineController.X_RELATIVE_POSITION = 2;
		CoffeeMachineController.Y_RELATIVE_POSITION = 3;
		LaundryTesterCyPhy.VERBOSE = true;
		LaundryTesterCyPhy.X_RELATIVE_POSITION = 0;
		LaundryTesterCyPhy.Y_RELATIVE_POSITION = 4;
		LaundryCyPhy.VERBOSE = true;
		LaundryCyPhy.X_RELATIVE_POSITION = 1;
		LaundryCyPhy.Y_RELATIVE_POSITION = 4;
		LaundryController.VERBOSE = true;
		LaundryController.X_RELATIVE_POSITION = 2;
		LaundryController.Y_RELATIVE_POSITION = 4;
		KettleTesterCyPhy.VERBOSE = true;
		KettleTesterCyPhy.X_RELATIVE_POSITION = 0;
		KettleTesterCyPhy.Y_RELATIVE_POSITION = 5;
		KettleCyPhy.VERBOSE = true;
		KettleCyPhy.X_RELATIVE_POSITION = 1;
		KettleCyPhy.Y_RELATIVE_POSITION = 5;
		KettleController.VERBOSE = true;
		KettleController.X_RELATIVE_POSITION = 2;
		KettleController.Y_RELATIVE_POSITION = 5;
		ClocksServer.VERBOSE = true;
		HEMEnergyManager.VERBOSE = true;
		HEMEnergyManager.CONTROL_LOOP_VERBOSE = true;
		CoffeeMachineCyPhy.VERBOSE = true;
		ElectricMeterCyPhy.VERBOSE = true;
		FanCyPhy.VERBOSE = true;
		KettleCyPhy.VERBOSE = true;
		BatteriesCyPhy.VERBOSE = true;
		GeneratorCyPhy.VERBOSE = true;
		SolarPanelCyPhy.VERBOSE = true;
	}

	@Override
	public void deploy() throws Exception {
		assert staticInvariants() : "Static invariants not satisfied!";

		TestScenario testScenario = this.createTestScenario();

		// Clock server
		long unixEpochStartTimeInMillis = System.currentTimeMillis() + DELAY_TO_START;
		AbstractComponent.createComponent(
			ClocksServer.class.getCanonicalName(),
			new Object[]{
				CLOCK_URI,
				TimeUnit.MILLISECONDS.toNanos(unixEpochStartTimeInMillis),
				START_INSTANT,
				ACCELERATION_FACTOR
			});

		// Electric Meter
		AbstractComponent.createComponent(
			ElectricMeterCyPhy.class.getCanonicalName(),
			new Object[]{ GLOBAL_EXECUTION_MODE, CLOCK_URI });

		// Energy Sources
		AbstractComponent.createComponent(
			BatteriesCyPhy.class.getCanonicalName(),
			new Object[]{ GLOBAL_EXECUTION_MODE, CLOCK_URI });

		AbstractComponent.createComponent(
			SolarPanelCyPhy.class.getCanonicalName(),
			new Object[]{ NB_OF_SQUARE_METERS, GLOBAL_EXECUTION_MODE, CLOCK_URI });

		AbstractComponent.createComponent(
			GeneratorCyPhy.class.getCanonicalName(),
			new Object[]{ GLOBAL_EXECUTION_MODE, CLOCK_URI });

		// Coffee Machines (3 instances, HEM-controlled)
		for (int i = 1; i <= 3; i++) {
			AbstractComponent.createComponent(
				CoffeeMachineCyPhy.class.getCanonicalName(),
				new Object[]{ i, true, GLOBAL_EXECUTION_MODE, CLOCK_URI });
		}

		// Coffee Machine Testers (3 instances, with unique reflection URIs)
		String[] cmTesterRIPs = { CM_TESTER_1_RIP, CM_TESTER_2_RIP, CM_TESTER_3_RIP };
		for (int i = 1; i <= 3; i++) {
			AbstractComponent.createComponent(
				CoffeeMachineTesterCyPhy.class.getCanonicalName(),
				new Object[]{
					cmTesterRIPs[i-1],
					CoffeeMachineCyPhy.USER_INBOUND_PORT_URI + "-" + i,
					CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI + "-" + i,
					CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI + "-" + i,
					GLOBAL_EXECUTION_MODE,
					testScenario
				});
		}

		// Kettle (single instance, HEM-controlled)
		AbstractComponent.createComponent(
			KettleCyPhy.class.getCanonicalName(),
			new Object[]{ 1, true, GLOBAL_EXECUTION_MODE, CLOCK_URI });

		// Kettle Tester (single instance)
		AbstractComponent.createComponent(
			KettleTesterCyPhy.class.getCanonicalName(),
			new Object[]{
				KETTLE_TESTER_1_RIP,
				KettleCyPhy.USER_INBOUND_PORT_URI + "-1",
				KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI + "-1",
				GLOBAL_EXECUTION_MODE,
				testScenario
			});

		// Fan (single instance, NOT HEM-controlled, consumption-only)
		AbstractComponent.createComponent(
			FanCyPhy.class.getCanonicalName(),
			new Object[]{ GLOBAL_EXECUTION_MODE });

		// Fan Tester (single instance)
		AbstractComponent.createComponent(
			FanTesterCyPhy.class.getCanonicalName(),
			new Object[]{
				FanCyPhy.INBOUND_PORT_URI,
				GLOBAL_EXECUTION_MODE,
				testScenario
			});

		// HEM Energy Manager (controls only Coffee and Kettle)
		AbstractComponent.createComponent(
			HEMEnergyManager.class.getCanonicalName(),
			new Object[]{ GLOBAL_EXECUTION_MODE, testScenario });

		super.deploy();
		assert implementationInvariants(this);
	}

	// -------------------------------------------------------------------------
	// Test Scenario
	// -------------------------------------------------------------------------

	/**
	 * Create the test scenario for Etape 4.
	 *
	 * The HEM controls only CoffeeMachine and Kettle based on deficit/surplus.
	 * Fans operate independently and contribute to total consumption.
	 *
	 * @return test scenario
	 */
	protected TestScenario createTestScenario() {
		TestStepI[] steps = new TestStepI[] {

			// ===================================================================
			// Phase 1: Coffee-1 and Fan-1 Activation (t=3-5s)
			// ===================================================================

			// t=3s: Coffee-1 ON
			new TestStep(CLOCK_URI, CM_TESTER_1_RIP,
				START_INSTANT.plusSeconds(3),
				owner -> { try {
					System.out.println("\n[TEST t=3s] Coffee-1 turning ON...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOn();
					System.out.println("[TEST t=3s] Coffee-1 ON (mode=SUSPEND, 3W)");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// t=4s: Coffee-1 set MAX
			new TestStep(CLOCK_URI, CM_TESTER_1_RIP,
				START_INSTANT.plusSeconds(4),
				owner -> { try {
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().setMaxMode();
					System.out.println("[TEST t=4s] Coffee-1 set to MAX (1500W = 6.8A)");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// t=5s: Fan ON (independent of HEM)
			new TestStep(CLOCK_URI, FAN_TESTER_RIP,
				START_INSTANT.plusSeconds(5),
				owner -> { try {
					System.out.println("[TEST t=5s] Fan turning ON...");
					((FanTesterCyPhy) owner).turnOnFan();
					System.out.println("[TEST t=5s] Fan ON LOW (200W = 0.9A)");
					System.out.println("[TEST] Total estimated consumption: ~7.7A (no production)");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// CONTROL LOOP #1 fires at t=10s:
			// → HEM detects ~6.8A deficit from Coffee-1
			// → Suspends Coffee-1 (priority 6)
			// → Fan-1 continues independently

			// ===================================================================
			// Phase 2: Coffee-2 and Kettle-1 Activation (t=13-16s)
			// ===================================================================

			// t=13s: Coffee-2 ON + MAX
			new TestStep(CLOCK_URI, CM_TESTER_2_RIP,
				START_INSTANT.plusSeconds(13),
				owner -> { try {
					System.out.println("\n[TEST t=13s] Coffee-2 turning ON...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOn();
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			new TestStep(CLOCK_URI, CM_TESTER_2_RIP,
				START_INSTANT.plusSeconds(14),
				owner -> { try {
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().setMaxMode();
					System.out.println("[TEST t=14s] Coffee-2 set to MAX (1500W = 6.8A)");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// t=15s: Kettle-1 ON + MAX
			new TestStep(CLOCK_URI, KETTLE_TESTER_1_RIP,
				START_INSTANT.plusSeconds(15),
				owner -> { try {
					System.out.println("[TEST t=15s] Kettle-1 turning ON...");
					((KettleTesterCyPhy) owner).getKuOP().turnOn();
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			new TestStep(CLOCK_URI, KETTLE_TESTER_1_RIP,
				START_INSTANT.plusSeconds(16),
				owner -> { try {
					((KettleTesterCyPhy) owner).getKuOP().setMode(KettleMode.MAX);
					System.out.println("[TEST t=16s] Kettle-1 set to MAX (3000W = 13.6A)");
					System.out.println("[TEST] Total new HEM-controlled consumption: ~13.6A");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// CONTROL LOOP #2 fires at t=20s:
			// → HEM detects ~13.6A deficit (Coffee-2 + Kettle-1)
			// → Suspends Coffee-2 (prio 6), then Kettle-1 (prio 5)
			// → Remaining deficit > 1A → starts generator

			// ===================================================================
			// Phase 3: Coffee-3 Activation (t=26-27s)
			// ===================================================================

			// t=26s: Coffee-3 ON + ECO
			new TestStep(CLOCK_URI, CM_TESTER_3_RIP,
				START_INSTANT.plusSeconds(26),
				owner -> { try {
					System.out.println("[TEST t=26s] Coffee-3 turning ON...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOn();
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			new TestStep(CLOCK_URI, CM_TESTER_3_RIP,
				START_INSTANT.plusSeconds(27),
				owner -> { try {
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().setEcoMode();
					System.out.println("[TEST t=27s] Coffee-3 set to ECO (700W = 3.2A)");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// CONTROL LOOP #3 fires at t=30s:
			// → HEM detects ~3.2A deficit from Coffee-3
			// → Suspends Coffee-3 (priority 6)
			// → Fan continues independently

			// ===================================================================
			// CHECKPOINT 1 (t=43s): All HEM-controlled equipment suspended
			// ===================================================================

			new TestStep(CLOCK_URI, HEM_REFLECTION_URI,
				START_INSTANT.plusSeconds(43),
				owner -> {
					System.out.println("*******************************************\n" +
						"  CHECKPOINT 1 (t=43s)                                   \n" +
						"  HEM-CONTROLLED Equipment (Suspended):                  \n" +
						"    Coffee-1 (prio 6) → SUSPENDED                        \n" +
						"    Coffee-2 (prio 6) → SUSPENDED                        \n" +
						"    Coffee-3 (prio 6) → SUSPENDED                        \n" +
						"    Kettle-1 (prio 5) → SUSPENDED                        \n" +
						"                                                         \n" +
						"  INDEPENDENT Equipment (Running):                       \n" +
						"    Fan (ON, LOW, 200W)                                  \n" +
						"                                                         \n" +
						"  Generator: RUNNING (started due to large deficit)      \n" +
						"  Check [CONTROL LOOP] logs above for confirmation       \n" );
				}),

			// ===================================================================
			// Phase 4: Equipment Deactivation (t=50-67s)
			// ===================================================================

			// t=50s: Kettle-1 OFF
			new TestStep(CLOCK_URI, KETTLE_TESTER_1_RIP,
				START_INSTANT.plusSeconds(50),
				owner -> { try {
					System.out.println("\n[TEST t=50s] Kettle-1 turning OFF (unregistering from HEM)...");
					((KettleTesterCyPhy) owner).getKuOP().turnOff();
					System.out.println("[TEST t=50s] Kettle-1 OFF and unregistered");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// t=51s: Coffee-1 OFF
			new TestStep(CLOCK_URI, CM_TESTER_1_RIP,
				START_INSTANT.plusSeconds(51),
				owner -> { try {
					System.out.println("[TEST t=51s] Coffee-1 turning OFF (unregistering from HEM)...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOff();
					System.out.println("[TEST t=51s] Coffee-1 OFF and unregistered");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// t=55s: Coffee-2 OFF
			new TestStep(CLOCK_URI, CM_TESTER_2_RIP,
				START_INSTANT.plusSeconds(55),
				owner -> { try {
					System.out.println("[TEST t=55s] Coffee-2 turning OFF (unregistering from HEM)...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOff();
					System.out.println("[TEST t=55s] Coffee-2 OFF and unregistered");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// t=56s: Fan OFF (independent)
			new TestStep(CLOCK_URI, FAN_TESTER_RIP,
				START_INSTANT.plusSeconds(56),
				owner -> { try {
					System.out.println("[TEST t=56s] Fan turning OFF (independent)...");
					((FanTesterCyPhy) owner).turnOffFan();
					System.out.println("[TEST t=56s] Fan OFF");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// t=65s: Coffee-3 OFF
			new TestStep(CLOCK_URI, CM_TESTER_3_RIP,
				START_INSTANT.plusSeconds(65),
				owner -> { try {
					System.out.println("\n[TEST t=65s] Coffee-3 turning OFF...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOff();
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// ===================================================================
			// Phase 5: Second Wave (t=73-85s) - Coffee-1 only
			// ===================================================================

			// t=73s: Coffee-1 ON again + NORMAL mode
			new TestStep(CLOCK_URI, CM_TESTER_1_RIP,
				START_INSTANT.plusSeconds(73),
				owner -> { try {
					System.out.println("\n[TEST t=73s] Coffee-1 turning ON again (second wave)...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOn();
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			new TestStep(CLOCK_URI, CM_TESTER_1_RIP,
				START_INSTANT.plusSeconds(74),
				owner -> { try {
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().setNormalMode();
					System.out.println("[TEST t=74s] Coffee-1 set to NORMAL (1000W = 4.5A)");
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// CONTROL LOOP fires at t=80s:
			// → HEM detects ~4.5A deficit → suspends Coffee-1

			// t=85s: Coffee-1 OFF (final cleanup)
			new TestStep(CLOCK_URI, CM_TESTER_1_RIP,
				START_INSTANT.plusSeconds(85),
				owner -> { try {
					System.out.println("[TEST t=85s] Coffee-1 turning OFF (final cleanup)...");
					((CoffeeMachineTesterCyPhy) owner).getCmUserOP().turnOff();
				} catch (Exception e) { throw new BCMRuntimeException(e); }
				}),

			// ===================================================================
			// FINAL CHECKPOINT (t=90s)
			// ===================================================================

			new TestStep(CLOCK_URI, HEM_REFLECTION_URI,
				START_INSTANT.plusSeconds(90),
				owner -> {
					System.out.println("*******************************************\n" +
						"  FINAL CHECKPOINT (t=90s) - TEST COMPLETE               " +
						"  Test Summary:                                          " +
						"                                                         " +
						"  1. HEM Control (CoffeeMachine & Kettle):               " +
						"     - Registered and suspended equipment                " +
						"     - Controlled by priority                            " +
						"     - Generator activated on large deficit              " +
						"                                                         " +
						"  2. Independent Equipment (Fans):                       " +
						"     - NOT registered with HEM                           " +
						"     - Consumed power independently                      " +
						"     - Contributed to total ElectricMeter reading        " +
						"                                                         " +
						"  3. Control Loop Behavior:                              " +
						"     - Prioritized suspension of HEM equipment           " +
						"     - Generator start/stop                              " +
						"                                                         " +
						"  Review [CONTROL LOOP] logs for detailed actions        " );
				})
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
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
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
