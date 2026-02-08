package etape4;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape2.equipments.batteries.mil.BatteriesSimulationConfiguration;
import etape2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
import etape3.CoordinatorComponent;
import etape3.equipements.coffee_machine.CoffeeMachineController;
import etape3.equipements.coffee_machine.CoffeeMachineController.ControlMode;
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
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.models.time.TimeUtils;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

/**
 * The class <code>CVMSILIntegration</code> defines the SIL integration test
 * for Etape 4: Energy management control loop with SIL simulation.
 *
 * <p><strong>Description</strong></p>
 * <p>
 * This CVM reuses the Etape 3 SIL simulation architecture
 * (ComponentSimulationArchitectures, GlobalSupervisor, CoordinatorComponent)
 * with controllers and sensors/actuators activated for CoffeeMachine and
 * Kettle. It uses HEMEnergyManager instead of HEMCyPhy to enable the
 * energy management control loop.
 * </p>
 *
 * <p><strong>Execution mode:</strong>
 * {@code ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION}</p>
 *
 * <p><strong>Test Scenario (24h simulated, accelerated x360):</strong></p>
 * <ul>
 * <li>06:30 - Kettle morning startup (ON, MAX, startHeating)</li>
 * <li>07:00 - Coffee Machine startup (ON, fillWater, MAX)</li>
 * <li>08:02 - Fan cycle (ON, HIGH, LOW, OFF at 09:00)</li>
 * <li>09:02 - Laundry cycle (ON, OFF at 10:02)</li>
 * <li>10:05 - Kettle morning OFF</li>
 * <li>11:00 - Coffee Machine ECO mode</li>
 * <li>16:00 - Coffee Machine afternoon MAX</li>
 * <li>18:00 - Kettle evening cycle</li>
 * <li>20:00 - Coffee Machine OFF</li>
 * <li>22:00 - Kettle evening OFF</li>
 * </ul>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public class CVMSILIntegration extends AbstractCVM {

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** Delay before starting test scenarios (ms) */
	public static long DELAY_TO_START = 8000L;

	/** Duration of the sleep at end before exiting JVM (ms) */
	public static long END_SLEEP_DURATION = 100000L;

	/** Time unit for simulation duration */
	public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;

	/** Start time of the simulation in simulated logical time */
	public static Time SIMULATION_START_TIME =
			new Time(0.0, SIMULATION_TIME_UNIT);

	/** Duration of the simulation in simulated time (24 hours) */
	public static Duration SIMULATION_DURATION =
			new Duration(24.0, SIMULATION_TIME_UNIT);

	/** Acceleration factor: 360x means 24h simulated = 240s real */
	public static double ACCELERATION_FACTOR = 360.0;

	/** Duration of the execution (ms) */
	public static long EXECUTION_DURATION =
			DELAY_TO_START +
				TimeUnit.NANOSECONDS.toMillis(
						TimeUtils.toNanos(
								SIMULATION_DURATION.getSimulatedDuration() /
										ACCELERATION_FACTOR,
								SIMULATION_DURATION.getTimeUnit()));

	/** Execution mode: SIL simulation */
	public static ExecutionMode GLOBAL_EXECUTION_MODE =
			ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION;

	/** Clock URI for time synchronization */
	public static String CLOCK_URI = "etape4-sil-integration-clock";

	/** Start instant for test scenarios */
	public static Instant START_INSTANT =
			Instant.parse("2026-02-07T06:00:00.00Z");

	/** Number of solar panel square meters */
	public static final int NB_OF_SQUARE_METERS = 10;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean implementationInvariants(CVMSILIntegration cvm) {
		assert cvm != null : new PreconditionException("cvm != null");
		return true;
	}

	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
			CLOCK_URI != null && !CLOCK_URI.isEmpty(),
			CVMSILIntegration.class,
			"CLOCK_URI != null && !CLOCK_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
			DELAY_TO_START >= 0,
			CVMSILIntegration.class,
			"DELAY_TO_START >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
			ACCELERATION_FACTOR > 0.0,
			CVMSILIntegration.class,
			"ACCELERATION_FACTOR > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
			START_INSTANT != null,
			CVMSILIntegration.class,
			"START_INSTANT != null");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	public CVMSILIntegration() throws Exception {
		super();

		// Trace window positions
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		HEMCyPhy.VERBOSE = true;
		HEMCyPhy.X_RELATIVE_POSITION = 0;
		HEMCyPhy.Y_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.VERBOSE = true;
		ElectricMeterCyPhy.X_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.Y_RELATIVE_POSITION = 0;
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
		HEMEnergyManager.VERBOSE = true;
		HEMEnergyManager.CONTROL_LOOP_VERBOSE = true;
	}

	// -------------------------------------------------------------------------
	// CVM Life-cycle
	// -------------------------------------------------------------------------

	@Override
	public void deploy() throws Exception {
		assert staticInvariants() : "Static invariants not satisfied!";

		TestScenarioWithSimulation testScenario = createTestScenario();

		long unixEpochStartTimeInMillis =
				System.currentTimeMillis() + DELAY_TO_START;

		// Clock server with simulation support
		AbstractComponent.createComponent(
			ClocksServerWithSimulation.class.getCanonicalName(),
			new Object[]{
				CLOCK_URI,
				TimeUnit.MILLISECONDS.toNanos(unixEpochStartTimeInMillis),
				START_INSTANT,
				ACCELERATION_FACTOR,
				DELAY_TO_START,
				SIMULATION_START_TIME,
				SIMULATION_DURATION
			});

		// Global Supervisor (creates and manages simulation architecture)
		AbstractComponent.createComponent(
			GlobalSupervisor.class.getCanonicalName(),
			new Object[]{
				testScenario,
				GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI
			});

		// Coordinator Component (coordinates simulation across components)
		AbstractComponent.createComponent(
			CoordinatorComponent.class.getCanonicalName(),
			new Object[]{});

		// HEM Energy Manager (with control loop, replaces HEMCyPhy)
		AbstractComponent.createComponent(
			HEMEnergyManager.class.getCanonicalName(),
			new Object[]{
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				testScenario
			});
		
		

		// Energy Sources
		AbstractComponent.createComponent(
				BatteriesCyPhy.class.getCanonicalName(),
				new Object[]{
						BatteriesCyPhy.REFLECTION_INBOUND_PORT_URI,
						BatteriesCyPhy.STANDARD_INBOUND_PORT_URI,
						BatteriesSimulationConfiguration.
												NUMBER_OF_PARALLEL_CELLS,
						BatteriesSimulationConfiguration.
												NUMBER_OF_CELL_GROUPS_IN_SERIES,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario,
						BatteriesCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR});

			AbstractComponent.createComponent(
				SolarPanelCyPhy.class.getCanonicalName(),
				new Object[]{
						SolarPanelCyPhy.REFLECTION_INBOUND_PORT_URI,
						SolarPanelCyPhy.STANDARD_INBOUND_PORT_URI,
						SolarPanelSimulationConfigurationI.NB_SQUARE_METERS,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario,
						BatteriesCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR
				});

			AbstractComponent.createComponent(
				GeneratorCyPhy.class.getCanonicalName(),
				new Object[]{
						GeneratorCyPhy.STANDARD_INBOUND_PORT_URI,
						GeneratorCyPhy.MAX_POWER,
						GeneratorCyPhy.TANK_CAPACITY,
						GeneratorCyPhy.MIN_FUEL_CONSUMPTION,
						GeneratorCyPhy.MAX_FUEL_CONSUMPTION,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario,
						GeneratorCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR});



		// Electric Meter (SIL constructor)
		AbstractComponent.createComponent(
			ElectricMeterCyPhy.class.getCanonicalName(),
			new Object[]{
				ElectricMeterCyPhy.REFLECTION_INBOUND_PORT_URI,
				ElectricMeterCyPhy.ELECTRIC_METER_INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				testScenario,
				ElectricMeterCyPhy.LOCAL_ARCHITECTURE_URI,
				ACCELERATION_FACTOR
			});

		// Fan (SIL constructor)
		AbstractComponent.createComponent(
			FanCyPhy.class.getCanonicalName(),
			new Object[]{
				FanCyPhy.REFLECTION_INBOUND_PORT_URI,
				FanCyPhy.INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				testScenario,
				FanCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
				ACCELERATION_FACTOR
			});

		// Fan Tester (uses INTEGRATION_TEST mode, not SIL - same as etape3)
		AbstractComponent.createComponent(
			FanTesterCyPhy.class.getCanonicalName(),
			new Object[]{
				FanCyPhy.INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST,
				testScenario
			});

		// Coffee Machine (SIL constructor with sensors/actuators)
		AbstractComponent.createComponent(
			CoffeeMachineCyPhy.class.getCanonicalName(),
			new Object[]{
				true,
				CoffeeMachineCyPhy.REFLECTION_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.SENSOR_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.ACTUATOR_INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				testScenario,
				CoffeeMachineCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
				ACCELERATION_FACTOR
			});

		// Coffee Machine Controller (SIL mode with sensors/actuators)
		AbstractComponent.createComponent(
			CoffeeMachineController.class.getCanonicalName(),
			new Object[]{
				CoffeeMachineCyPhy.SENSOR_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.ACTUATOR_INBOUND_PORT_URI,
				CoffeeMachineController.STANDARD_TEMPERATURE_HYSTERESIS,
				CoffeeMachineController.STANDARD_CONTROL_PERIOD,
				ControlMode.PULL,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				ACCELERATION_FACTOR
			});

		// Coffee Machine Tester (uses INTEGRATION_TEST mode)
		AbstractComponent.createComponent(
			CoffeeMachineTesterCyPhy.class.getCanonicalName(),
			new Object[]{
				CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
				CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST,
				testScenario
			});

		// Laundry (SIL constructor with sensors/actuators)
		AbstractComponent.createComponent(
			LaundryCyPhy.class.getCanonicalName(),
			new Object[]{
				true,
				LaundryCyPhy.REFLECTION_INBOUND_PORT_URI,
				LaundryCyPhy.USER_INBOUND_PORT_URI,
				LaundryCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
				LaundryCyPhy.SENSOR_INBOUND_PORT_URI,
				LaundryCyPhy.ACTUATOR_INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				testScenario,
				LaundryCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
				ACCELERATION_FACTOR
			});

		// Laundry Controller (SIL mode)
		AbstractComponent.createComponent(
			LaundryController.class.getCanonicalName(),
			new Object[]{
				LaundryCyPhy.SENSOR_INBOUND_PORT_URI,
				LaundryCyPhy.ACTUATOR_INBOUND_PORT_URI,
				LaundryController.STANDARD_CONTROL_PERIOD,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				ACCELERATION_FACTOR
			});

		// Laundry Tester (uses INTEGRATION_TEST mode)
		AbstractComponent.createComponent(
			LaundryTesterCyPhy.class.getCanonicalName(),
			new Object[]{
				LaundryCyPhy.USER_INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST,
				testScenario
			});

		// Kettle (SIL constructor with sensors/actuators)
		AbstractComponent.createComponent(
			KettleCyPhy.class.getCanonicalName(),
			new Object[]{
				true,
				KettleCyPhy.REFLECTION_INBOUND_PORT_URI,
				KettleCyPhy.USER_INBOUND_PORT_URI,
				KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
				KettleCyPhy.SENSOR_INBOUND_PORT_URI,
				KettleCyPhy.ACTUATOR_INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				testScenario,
				KettleCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
				ACCELERATION_FACTOR
			});

		// Kettle Controller (SIL mode with sensors/actuators)
		AbstractComponent.createComponent(
			KettleController.class.getCanonicalName(),
			new Object[]{
				KettleCyPhy.SENSOR_INBOUND_PORT_URI,
				KettleCyPhy.ACTUATOR_INBOUND_PORT_URI,
				KettleController.STANDARD_HYSTERESIS,
				KettleController.STANDARD_CONTROL_PERIOD,
				KettleController.ControlMode.PULL,
				ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
				ACCELERATION_FACTOR
			});

		// Kettle Tester (uses INTEGRATION_TEST mode)
		AbstractComponent.createComponent(
			KettleTesterCyPhy.class.getCanonicalName(),
			new Object[]{
				KettleCyPhy.USER_INBOUND_PORT_URI,
				KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
				ExecutionMode.INTEGRATION_TEST,
				testScenario
			});

		super.deploy();
		assert implementationInvariants(this);
	}

	// -------------------------------------------------------------------------
	// Test Scenario
	// -------------------------------------------------------------------------

	/**
	 * Create the test scenario for Etape 4 SIL integration test.
	 * Reuses the same daily scenario as Etape 3 SIL integration test.
	 *
	 * @return test scenario with simulation
	 * @throws Exception if scenario creation fails
	 */
	protected TestScenarioWithSimulation createTestScenario() throws Exception {
		long d = TimeUnit.NANOSECONDS.toSeconds(
				TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = START_INSTANT.plusSeconds(d);

		// Coffee machine: 07:00-11:00 MAX, 11:00-15:00 ECO, 16:00-20:00 MAX
		Instant cmSwitchOn       = Instant.parse("2026-02-07T07:00:00.00Z");
		Instant cmFillWater1     = Instant.parse("2026-02-07T07:05:00.00Z");
		Instant cmSetMaxMode1    = Instant.parse("2026-02-07T07:10:00.00Z");
		Instant cmMakeExpresso1  = Instant.parse("2026-02-07T07:30:00.00Z");
		Instant cmServeCoffee1   = Instant.parse("2026-02-07T08:00:00.00Z");
		Instant cmMakeExpresso2  = Instant.parse("2026-02-07T09:30:00.00Z");
		Instant cmServeCoffee2   = Instant.parse("2026-02-07T10:00:00.00Z");
		Instant cmSetEcoMode     = Instant.parse("2026-02-07T11:00:00.00Z");
		Instant cmSetMaxMode2    = Instant.parse("2026-02-07T16:00:00.00Z");
		Instant cmFillWater2     = Instant.parse("2026-02-07T16:15:00.00Z");
		Instant cmMakeExpresso3  = Instant.parse("2026-02-07T16:30:00.00Z");
		Instant cmServeCoffee3   = Instant.parse("2026-02-07T17:00:00.00Z");
		Instant cmMakeExpresso4  = Instant.parse("2026-02-07T18:30:00.00Z");
		Instant cmServeCoffee4   = Instant.parse("2026-02-07T19:00:00.00Z");
		Instant cmSwitchOff      = Instant.parse("2026-02-07T20:00:00.00Z");

		// Kettle morning (06:30-10:05)
		Instant kettleSwitchOn1     = Instant.parse("2026-02-07T06:30:00.00Z");
		Instant kettleSetMaxMode1   = Instant.parse("2026-02-07T06:35:00.00Z");
		Instant kettleStartHeating1 = Instant.parse("2026-02-07T06:40:00.00Z");
		Instant kettleSwitchOff1    = Instant.parse("2026-02-07T10:05:00.00Z");

		// Fan cycle (08:02-09:00)
		Instant fanTurnOn   = Instant.parse("2026-02-07T08:02:00.00Z");
		Instant fanSetHigh  = Instant.parse("2026-02-07T08:05:00.00Z");
		Instant fanSetLow   = Instant.parse("2026-02-07T08:30:00.00Z");
		Instant fanTurnOff  = Instant.parse("2026-02-07T09:00:00.00Z");

		// Laundry (09:02-10:02)
		Instant laundrySwitchOn  = Instant.parse("2026-02-07T09:02:00.00Z");
		Instant laundrySwitchOff = Instant.parse("2026-02-07T10:02:00.00Z");

		// Kettle evening (18:00-22:00)
		Instant kettleSwitchOn2      = Instant.parse("2026-02-07T18:00:00.00Z");
		Instant kettleSetMaxMode2    = Instant.parse("2026-02-07T18:05:00.00Z");
		Instant kettleStartHeating2  = Instant.parse("2026-02-07T18:10:00.00Z");
		Instant kettleSwitchOff2     = Instant.parse("2026-02-07T22:00:00.00Z");

		return new TestScenarioWithSimulation(
			CLOCK_URI,
			START_INSTANT,
			endInstant,
			GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI,
			new Time(0.0, TimeUnit.HOURS),
			(ts, simParams) -> { },
			new TestStepI[] {
				// ===== 06:30 - Kettle morning startup =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleSwitchOn1,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 06:35
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleSetMaxMode1,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().setMode(KettleMode.MAX);
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 06:40 - startHeating (safe: controller may have already started)
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleStartHeating1,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().startHeating();
					} catch (Exception e) {
						System.out.println("[TEST] Kettle already heating (controller started it)");
					}
					}),

				// ===== 07:00 - Coffee Machine startup =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmSwitchOn,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 07:05
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmFillWater1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().fillWater();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 07:10
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmSetMaxMode1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setMaxMode();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 07:30 - Make expresso (morning batch 1)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmMakeExpresso1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 08:00 - Serve coffee =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmServeCoffee1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 08:02 - Fan on =====
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					fanTurnOn,
					owner -> { try {
						((FanTesterCyPhy)owner).turnOnFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 08:05
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					fanSetHigh,
					owner -> { try {
						((FanTesterCyPhy)owner).setHighFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 08:30
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					fanSetLow,
					owner -> { try {
						((FanTesterCyPhy)owner).setLowFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 09:00 - Fan off =====
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					fanTurnOff,
					owner -> { try {
						((FanTesterCyPhy)owner).turnOffFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 09:02 - Laundry on =====
				new TestStep(CLOCK_URI,
					LaundryTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					laundrySwitchOn,
					owner -> { try {
						((LaundryTesterCyPhy)owner).getLuOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// 09:30 - Make expresso (morning batch 2)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmMakeExpresso2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 10:00 - Serve coffee =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmServeCoffee2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 10:02 - Laundry off =====
				new TestStep(CLOCK_URI,
					LaundryTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					laundrySwitchOff,
					owner -> { try {
						((LaundryTesterCyPhy)owner).getLuOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 10:05 - Kettle off =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleSwitchOff1,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 11:00 - Coffee Machine: switch to ECO mode =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmSetEcoMode,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setEcoMode();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 16:00 - Coffee Machine: afternoon MAX mode =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmSetMaxMode2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setMaxMode();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 16:15 - Fill water
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmFillWater2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().fillWater();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 16:30 - Make expresso (afternoon batch 1)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmMakeExpresso3,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 17:00 - Serve coffee
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmServeCoffee3,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 18:00 - Kettle evening cycle =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleSwitchOn2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 18:05
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleSetMaxMode2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().setMode(KettleMode.MAX);
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 18:10 - startHeating (safe: controller may have already started)
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleStartHeating2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().startHeating();
					} catch (Exception e) {
						System.out.println("[TEST] Kettle already heating (controller started it)");
					}
					}),

				// 18:30 - Make expresso (afternoon batch 2)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmMakeExpresso4,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 19:00 - Serve coffee
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmServeCoffee4,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 20:00 - Coffee Machine: shutdown =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					cmSwitchOff,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 22:00 - Kettle end evening =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleSwitchOff2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					})
			});
	}

	// -------------------------------------------------------------------------
	// Execution
	// -------------------------------------------------------------------------

	public static void main(String[] args) {
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		try {
			CVMSILIntegration cvm = new CVMSILIntegration();
			cvm.startStandardLifeCycle(EXECUTION_DURATION);
			Thread.sleep(END_SLEEP_DURATION);
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
