package etape3;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

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
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import fr.sorbonne_u.components.AbstractComponent;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMIntegrationTest</code> defines the integration test
 * for the household energy management example.
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
 * invariant	{@code CLOCK_URI != null && !CLOCK_URI.isEmpty()}
 * invariant	{@code DELAY_TO_START_IN_MILLIS >= 0}
 * invariant	{@code ACCELERATION_FACTOR > 0.0}
 * invariant	{@code START_INSTANT != null}
 * </pre>
 * 
 * <p>Created on : 2021-09-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CVMIntegrationTest
extends		AbstractCVM
{
	/** delay before starting the test scenarios, leaving time to build
	 *  and initialise the components and their simulators; this delay is
	 *  estimated given the complexity of the initialisation (including the
	 *  creation of the application simulator if simulation is used). It
	 *  could need to be revised if the computer on which the application
	 *  is run is less powerful.											*/
	public static long			DELAY_TO_START = 5000L;
	/** duration of the sleep at the end of the execution before exiting
	 *  the JVM.															*/
	public static long			END_SLEEP_DURATION = 100000L;

	/** time unit in which {@code SIMULATION_DURATION} is expressed.		*/
	public static TimeUnit		SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	/** start time of the simulation, in simulated logical time, if
	 *  relevant.															*/
	public static Time 			SIMULATION_START_TIME =
										new Time(0.0, SIMULATION_TIME_UNIT);
	/** duration  of the simulation, in simulated time.						*/
	public static Duration		SIMULATION_DURATION =
										new Duration(24.0, SIMULATION_TIME_UNIT);
	/** for real time simulations, the acceleration factor applied to the
	 *  the simulated time to get the execution time of the simulations. 	*/
	public static double		ACCELERATION_FACTOR = 360.0;
	/** duration of the execution.											*/
	public static long			EXECUTION_DURATION =
			DELAY_TO_START +
				TimeUnit.NANOSECONDS.toMillis(
						TimeUtils.toNanos(
								SIMULATION_DURATION.getSimulatedDuration()/
													ACCELERATION_FACTOR,
								SIMULATION_DURATION.getTimeUnit()));

	public static ExecutionMode	GLOBAL_EXECUTION_MODE =
					ExecutionMode.INTEGRATION_TEST;
					//ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION;

	/** for unit tests and SIL simulation unit tests, a {@code Clock} is
	 *  used to get a time-triggered synchronisation of the actions of
	 *  the components in the test scenarios.								*/
	public static String		CLOCK_URI = "integration-test-clock";
	/** start instant in test scenarios, as a string to be parsed.			*/
	public static Instant		START_INSTANT =
									Instant.parse("2026-02-07T06:00:00.00Z");

	// Solar panel constants

	/** number of square meters in the test solar panel.					*/
	public static final int		NB_OF_SQUARE_METERS = 10;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cvm != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param cvm	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(CVMIntegrationTest cvm)
	{
		assert	cvm != null : new PreconditionException("cvm != null");

		boolean ret = true;
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
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
				ACCELERATION_FACTOR > 0.0,
				CVMIntegrationTest.class,
				"ACCELERATION_FACTOR > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				START_INSTANT != null,
				CVMIntegrationTest.class,
				"START_INSTANT != null");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cvm != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param cvm	instance to be tested.
	 * @return	true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(CVMIntegrationTest cvm)
	{
		assert	cvm != null : new PreconditionException("cvm != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				CVMIntegrationTest() throws Exception
	{
		// Trace and trace window positions
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		HEMCyPhy.VERBOSE = true;
		HEMCyPhy.X_RELATIVE_POSITION = 0;
		HEMCyPhy.Y_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.VERBOSE = true;
		ElectricMeterCyPhy.X_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.Y_RELATIVE_POSITION = 0;
//		BatteriesCyPhy.VERBOSE = true;
//		BatteriesCyPhy.X_RELATIVE_POSITION = 1;
//		BatteriesCyPhy.Y_RELATIVE_POSITION = 1;
//		SolarPanelCyPhy.VERBOSE = true;
//		SolarPanelCyPhy.X_RELATIVE_POSITION = 2;
//		SolarPanelCyPhy.Y_RELATIVE_POSITION = 1;
//		GeneratorCyPhy.VERBOSE = true;
//		GeneratorCyPhy.X_RELATIVE_POSITION = 3;
//		GeneratorCyPhy.Y_RELATIVE_POSITION = 1;
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

		/*
		assert	CVMIntegrationTest.implementationInvariants(this) :
				new InvariantException(
						"CVMIntegrationTest.glassBoxInvariants(this)");
		assert	CVMIntegrationTest.invariants(this) :
				new InvariantException(
						"CVMIntegrationTest.blackBoxInvariants(this)");
						*/
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		TestScenario testScenario;

		if (ExecutionMode.INTEGRATION_TEST.equals(GLOBAL_EXECUTION_MODE)) {

			testScenario = integrationWithoutSimulation();
			// start time in Unix epoch time in nanoseconds.
			long unixEpochStartTimeInMillis = 
								System.currentTimeMillis() + DELAY_TO_START;

			AbstractComponent.createComponent(
				ClocksServer.class.getCanonicalName(),
				new Object[]{
						// URI of the clock to retrieve it
						CLOCK_URI,
						// start time in Unix epoch time
						TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
						START_INSTANT,
						ACCELERATION_FACTOR});

			AbstractComponent.createComponent(
				ElectricMeterCyPhy.class.getCanonicalName(),
				new Object[]{
						ExecutionMode.INTEGRATION_TEST,
						CLOCK_URI
				});

//			AbstractComponent.createComponent(
//				Batteries.class.getCanonicalName(),
//				new Object[]{});
//
//			AbstractComponent.createComponent(
//				SolarPanel.class.getCanonicalName(),
//				new Object[]{NB_OF_SQUARE_METERS});
//
//			AbstractComponent.createComponent(
//				Generator.class.getCanonicalName(),
//				new Object[]{});

			AbstractComponent.createComponent(
				FanCyPhy.class.getCanonicalName(),
				new Object[]{ExecutionMode.INTEGRATION_TEST});
			AbstractComponent.createComponent(
				FanTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						FanCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			AbstractComponent.createComponent(
				CoffeeMachineCyPhy.class.getCanonicalName(),
				new Object[]{
						true,
						ExecutionMode.INTEGRATION_TEST,
						testScenario.getClockURI()
				});
			AbstractComponent.createComponent(
				CoffeeMachineTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			AbstractComponent.createComponent(
				LaundryCyPhy.class.getCanonicalName(),
				new Object[]{
						true,
						ExecutionMode.INTEGRATION_TEST,
						testScenario.getClockURI()
				});
			AbstractComponent.createComponent(
				LaundryTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						LaundryCyPhy.USER_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			AbstractComponent.createComponent(
				KettleCyPhy.class.getCanonicalName(),
				new Object[]{
						true,
						ExecutionMode.INTEGRATION_TEST,
						testScenario.getClockURI()
				});
			AbstractComponent.createComponent(
				KettleTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						KettleCyPhy.USER_INBOUND_PORT_URI,
						KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			AbstractComponent.createComponent(
				HEMCyPhy.class.getCanonicalName(),
				new Object[]{
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

		} else if (ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION.equals(
													GLOBAL_EXECUTION_MODE)) {

			testScenario = integrationWithSimulation();
			// start time in Unix epoch time in nanoseconds.
			long unixEpochStartTimeInMillis = 
								System.currentTimeMillis() + DELAY_TO_START;

			AbstractComponent.createComponent(
				ClocksServerWithSimulation.class.getCanonicalName(),
				new Object[]{
						// URI of the clock to retrieve it
						CLOCK_URI,
						// start time in Unix epoch time
						TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
						START_INSTANT,
						ACCELERATION_FACTOR,
						DELAY_TO_START,
						SIMULATION_START_TIME,
						SIMULATION_DURATION});

			AbstractComponent.createComponent(
				GlobalSupervisor.class.getCanonicalName(),
				new Object[]{
						testScenario,
						GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI
				});
			AbstractComponent.createComponent(
					CoordinatorComponent.class.getCanonicalName(),
					new Object[]{});


			AbstractComponent.createComponent(
				HEMCyPhy.class.getCanonicalName(),
				new Object[]{
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario
				});
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

//			AbstractComponent.createComponent(
//				Batteries.class.getCanonicalName(),
//				new Object[]{});
//
//			AbstractComponent.createComponent(
//				SolarPanel.class.getCanonicalName(),
//				new Object[]{NB_OF_SQUARE_METERS});
//
//			AbstractComponent.createComponent(
//				Generator.class.getCanonicalName(),
//				new Object[]{});

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
			AbstractComponent.createComponent(
				FanTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						FanCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

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
			AbstractComponent.createComponent(
				CoffeeMachineTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						CoffeeMachineCyPhy.USER_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						CoffeeMachineCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

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
			AbstractComponent.createComponent(
				LaundryController.class.getCanonicalName(),
				new Object[]{
						LaundryCyPhy.SENSOR_INBOUND_PORT_URI,
						LaundryCyPhy.ACTUATOR_INBOUND_PORT_URI,
						LaundryController.STANDARD_CONTROL_PERIOD,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						ACCELERATION_FACTOR
				});
			AbstractComponent.createComponent(
				LaundryTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						LaundryCyPhy.USER_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

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
			AbstractComponent.createComponent(
				KettleTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						KettleCyPhy.USER_INBOUND_PORT_URI,
						KettleCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

		}

		super.deploy();
	}

	// -------------------------------------------------------------------------
	// Executing
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		try {
			CVMIntegrationTest cvm = new CVMIntegrationTest();
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
	 * return a test scenario for the integration testing without simulation of
	 * the HEM application.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * 
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return				a test scenario for the integration testing of the HEM application.
	 * @throws Exception	<i>to do</i>.
	 */
	public static TestScenario	integrationWithoutSimulation()
	throws Exception
	{
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = START_INSTANT.plusSeconds(d);

		Instant hemTestMeter = START_INSTANT.plusSeconds(120);          // 06:02

		// Coffee machine scenario: 07:00-11:00 MAX, 11:00-15:00 ECO, 16:00-20:00 MAX
		Instant CmSwitchOn = START_INSTANT.plusSeconds(3600);             // 07:00
		Instant CmFillWater1 = START_INSTANT.plusSeconds(3900);           // 07:05
		Instant CmSetMaxMode1 = START_INSTANT.plusSeconds(4200);          // 07:10
		Instant CmMakeExpresso1 = START_INSTANT.plusSeconds(5400);        // 07:30
		Instant CmServeCoffee1 = START_INSTANT.plusSeconds(7200);         // 08:00
		Instant CmMakeExpresso2 = START_INSTANT.plusSeconds(12600);       // 09:30
		Instant CmServeCoffee2 = START_INSTANT.plusSeconds(14400);        // 10:00
		Instant CmSetEcoMode = START_INSTANT.plusSeconds(18000);          // 11:00
		Instant hemTestCoffeeMachine = START_INSTANT.plusSeconds(19800);   // 11:30
		Instant CmSetMaxMode2 = START_INSTANT.plusSeconds(36000);         // 16:00
		Instant CmFillWater2 = START_INSTANT.plusSeconds(36900);          // 16:15
		Instant CmMakeExpresso3 = START_INSTANT.plusSeconds(37800);       // 16:30
		Instant CmServeCoffee3 = START_INSTANT.plusSeconds(39600);        // 17:00
		Instant CmMakeExpresso4 = START_INSTANT.plusSeconds(45000);       // 18:30
		Instant CmServeCoffee4 = START_INSTANT.plusSeconds(46800);        // 19:00
		Instant CmSwitchOff = START_INSTANT.plusSeconds(50400);           // 20:00

		// Kettle morning cycle (06:30 - 10:05)
		Instant KettleSwitchOn = START_INSTANT.plusSeconds(1800);         // 06:30
		Instant KettleSetMaxMode = START_INSTANT.plusSeconds(2100);       // 06:35
		Instant KettleStartHeating = START_INSTANT.plusSeconds(2400);     // 06:40
		Instant KettleSwitchOff = START_INSTANT.plusSeconds(14700);       // 10:05

		// Fan cycle (08:02-09:00)
		Instant FanTurnOn = START_INSTANT.plusSeconds(7320);              // 08:02
		Instant FanSetHigh = START_INSTANT.plusSeconds(7500);             // 08:05
		Instant FanSetLow = START_INSTANT.plusSeconds(9000);              // 08:30
		Instant FanTurnOff = START_INSTANT.plusSeconds(10800);            // 09:00

		// Laundry (09:02-10:02)
		Instant LaundrySwitchOn = START_INSTANT.plusSeconds(10920);       // 09:02
		Instant LaundrySwitchOff = START_INSTANT.plusSeconds(14520);      // 10:02

		// Kettle evening cycle (18:00 - 22:00)
		Instant KettleSwitchOn2 = START_INSTANT.plusSeconds(43200);       // 18:00
		Instant KettleSetMaxMode2 = START_INSTANT.plusSeconds(43500);     // 18:05
		Instant KettleStartHeating2 = START_INSTANT.plusSeconds(43800);   // 18:10
		Instant KettleSwitchOff2 = START_INSTANT.plusSeconds(57600);      // 22:00

		return new TestScenario(
			CLOCK_URI,
			START_INSTANT,
			endInstant,
			new TestStepI[] {
				// ===== HEM test meter: 06:02 =====
				new TestStep(CLOCK_URI,
					HEMCyPhy.REFLECTION_INBOUND_PORT_URI,
					hemTestMeter,
					owner -> { try {
						((HEMCyPhy)owner).testMeter();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 06:30 - Kettle morning startup =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleSwitchOn,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 06:35
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleSetMaxMode,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().setMode(KettleMode.MAX);
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 06:40
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleStartHeating,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().startHeating();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 07:00 - Coffee Machine startup =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmSwitchOn,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 07:05
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmFillWater1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().fillWater();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 07:10
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmSetMaxMode1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setMaxMode();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 07:30 - Make expresso (morning batch 1)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmMakeExpresso1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 08:00 - Serve coffee =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmServeCoffee1,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 08:02 - Fan on =====
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					FanTurnOn,
					owner -> { try {
						((FanTesterCyPhy)owner).turnOnFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 08:05
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					FanSetHigh,
					owner -> { try {
						((FanTesterCyPhy)owner).setHighFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 08:30
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					FanSetLow,
					owner -> { try {
						((FanTesterCyPhy)owner).setLowFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 09:00 - Fan off =====
				new TestStep(CLOCK_URI,
					FanTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					FanTurnOff,
					owner -> { try {
						((FanTesterCyPhy)owner).turnOffFan();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 09:02 - Laundry on =====
				new TestStep(CLOCK_URI,
					LaundryTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					LaundrySwitchOn,
					owner -> { try {
						((LaundryTesterCyPhy)owner).getLuOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// 09:30 - Make expresso (morning batch 2)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmMakeExpresso2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 10:00 - Serve coffee =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmServeCoffee2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 10:02 - Laundry off =====
				new TestStep(CLOCK_URI,
					LaundryTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					LaundrySwitchOff,
					owner -> { try {
						((LaundryTesterCyPhy)owner).getLuOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// ===== 10:05 - Kettle off =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleSwitchOff,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 11:00 - Coffee Machine: switch to ECO mode =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmSetEcoMode,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setEcoMode();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 11:30 - HEM test CoffeeMachine =====
				new TestStep(CLOCK_URI,
					HEMCyPhy.REFLECTION_INBOUND_PORT_URI,
					hemTestCoffeeMachine,
					owner -> { try {
						((HEMCyPhy)owner).testCoffeeMachine();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 16:00 - Coffee Machine: afternoon MAX mode =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmSetMaxMode2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().setMaxMode();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 16:15 - Fill water
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmFillWater2,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().fillWater();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 16:30 - Make expresso (afternoon batch 1)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmMakeExpresso3,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 17:00 - Serve coffee
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmServeCoffee3,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 18:00 - Kettle evening cycle =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleSwitchOn2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOn();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 18:05
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleSetMaxMode2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().setMode(KettleMode.MAX);
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 18:10
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleStartHeating2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().startHeating();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// 18:30 - Make expresso (afternoon batch 2)
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmMakeExpresso4,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().makeExpresso();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),
				// 19:00 - Serve coffee
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmServeCoffee4,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().serveCoffee();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 20:00 - Coffee Machine: shutdown =====
				new TestStep(CLOCK_URI,
					CoffeeMachineTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					CmSwitchOff,
					owner -> { try {
						((CoffeeMachineTesterCyPhy)owner).getCmUserOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					}),

				// ===== 22:00 - Kettle end evening =====
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					KettleSwitchOff2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().turnOff();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
					})
			});
	}

	/**
	 * return a test scenario for the integration testing with simulation of the
	 * HEM application.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * 
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return				a test scenario for the integration testing with simulation of the HEM application.
	 * @throws Exception	<i>to do</i>.
	 */
	public static TestScenarioWithSimulation	integrationWithSimulation()
	throws Exception
	{
		// START_INSTANT = "2026-02-07T06:00:00.00Z"
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
				// 06:40
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleStartHeating1,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().startHeating();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
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
				// 18:10
				new TestStep(CLOCK_URI,
					KettleTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					kettleStartHeating2,
					owner -> { try {
						((KettleTesterCyPhy)owner).getKuOP().startHeating();
					} catch (Exception e) { throw new BCMRuntimeException(e); }
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
}
// -----------------------------------------------------------------------------
