package etape1.equipements.laundry;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape1.CVMIntegrationTest;
import etape1.equipements.laundry.connectors.LaundryUserConnector;
import etape1.equipements.laundry.interfaces.LaundryExternalControlJava4CI;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;
import etape1.equipements.laundry.interfaces.LaundryUserCI;
import etape1.equipements.laundry.ports.LaundryUserOutboundPort;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import tests_utils.TestsStatistics;

@RequiredInterfaces(required = { LaundryUserCI.class, ClocksServerCI.class })
@OfferedInterfaces(offered = { LaundryExternalControlJava4CI.class })
public class LaundryUnitTester extends AbstractComponent {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the laundry port for external control. */
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "LAUNDRY-TEST-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Laundry is switched on.
	 */
	public static final int SWITCH_ON_DELAY = 4;
	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Laundry is switched off.
	 */
	public static final int SWITCH_OFF_DELAY = 65;

	public static final int START_WASH_WHITE_MODE_DELAY = 25;
	
	public static final int START_WASH_COLOR_MODE_DELAY = 35;

	public static final int START_WASH_INTENSIVE_MODE_DELAY = 45;
	
	public static final int START_WASH_DELICATE_MODE_DELAY = 55;

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;

	/**
	 * true if the component must perform unit tests, otherwise it executes
	 * integration tests actions.
	 */
	protected final boolean isUnitTest;
	/** URI of the user component interface inbound port. */
	protected String laundryUserInboundPortURI;

	/** user component interface outbound port. */
	protected LaundryUserOutboundPort luop;

	/** collector of test statistics. */
	protected TestsStatistics statistics;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a Laundry test component.
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param isUnitTest true if the component must perform unit tests, otherwise it
	 *                   executes integration tests actions.
	 * @throws Exception <i>to do</i>.
	 */

	protected LaundryUnitTester(boolean isUnitTest) throws Exception {
		this(isUnitTest, Laundry.USER_INBOUND_PORT_URI);
	}

	/**
	 * create a Laundry test component.
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param isUnitTest                           true if the component must
	 *                                             perform unit tests, otherwise it
	 *                                             executes integration tests
	 *                                             actions.
	 * @param laundryUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected LaundryUnitTester(boolean isUnitTest, String laundryUserInboundPortURI) throws Exception {
		super(1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(laundryUserInboundPortURI);
	}

	/**
	 * create a Laundry test component.
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param isUnitTest                           true if the component must
	 *                                             perform unit tests, otherwise it
	 *                                             executes integration tests
	 *                                             actions.
	 * @param reflectionInboundPortURI             URI of the reflection inbound
	 *                                             port of the component.
	 * @param laundryUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected LaundryUnitTester(boolean isUnitTest, String reflectionInboundPortURI,
			String laundryUserInboundPortURI)
			throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(laundryUserInboundPortURI);
	}

	/**
	 * initialise a Laundry test component.
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param laundryUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected void initialise(String laundryUserInboundPortURI)
			throws Exception {
		this.laundryUserInboundPortURI = laundryUserInboundPortURI;
		this.luop = new LaundryUserOutboundPort(this);
		this.luop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Laundry tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * test getting the state of the Laundry.
	 *
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 *
	 * <p>
	 * Gherkin specification
	 * </p>
	 * <p>
	 * </p>
	 *
	 * <pre>
	 * Feature: getting the state of the Laundry
	 *   Scenario: getting the state of the Laundry when off
	 *     Given the Laundry is initialised
	 *     And the Laundry has not been used yet
	 *     When I test the state of the Laundry
	 *     Then the state of the Laundry is OFF
	 * </pre>
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testOff() {
		this.logMessage("Feature: getting the state of the Laundry");
		this.logMessage("  Scenario: getting the state of the Laundry when off");
		this.logMessage("    Given the Laundry is initialised");
		this.logMessage("    And the Laundry has not been used yet");
		try {
			this.logMessage("    When I test the state of the Laundry");
			if (this.luop.getState() == LaundryState.OFF) {
				this.logMessage("    Then the state of the Laundry is OFF");
			} else {
				this.logMessage("     but was: " + this.luop.getState());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			System.out.println(e.fillInStackTrace());
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting the state of the Laundry.
	 *
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 *
	 * <p>
	 * Gherkin specification
	 * </p>
	 * <p>
	 * </p>
	 *
	 * <pre>
	 * Feature: getting the state of the Laundry
	 *   Scenario: getting the state of the Laundry when on
	 *     Given the Laundry is initialised
	 *     And the Laundry has been used
	 *     When I test the state of the Laundry
	 *     Then the state of the Laundry is ON
	 * </pre>
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testOn() {
		this.logMessage("Feature: getting the state of the Laundry");
		this.logMessage("  Scenario: getting the state of the Laundry when on");
		this.logMessage("    Given the Laundry is initialised");
		this.logMessage("    And the Laundry has been used ");
		try {
			this.logMessage("    When I turn on the Laundry");
			this.luop.turnOn();
			this.logMessage("    When I test the state of the Laundry");
			if (this.luop.getState() == LaundryState.ON) {
				this.logMessage("    Then the state of the Laundry is ON");
			} else {
				this.logMessage("     but was: " + this.luop.getState());
				this.statistics.incorrectResult();
			}
			this.luop.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching on and off the laundry.
	 *
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 *
	 * <pre>
	 * Feature: turning on and off the laundry
	 *   Scenario: turning on the laundry when off
	 *     Given the laundry is initialised
	 *     And the laundry has not been used yet
	 *     When I turn on the laundry
	 *     Then the state of the laundry is ON
	 *   Scenario: turning off the laundry when on
	 *     Given the laundry is initialised
	 *     And the laundry is on
	 *     When I turn off the laundry
	 *     Then the state of the laundry is OFF
	 * </pre>
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testTurnOnTurnOff() {
		this.logMessage("Feature: turning on and off the laundry");

		this.logMessage("  Scenario: turning on the laundry when off");
		this.logMessage("    Given the laundry is initialised");
		this.logMessage("    And the laundry has not been used yet");
		LaundryState result;
		try {
			this.logMessage("    When I turn on the laundry");
			this.luop.turnOn();
			result = this.luop.getState();
			if (result == LaundryState.ON) {
				this.logMessage("    Then the state of the laundry is ON");
			} else {
				this.logMessage("     but was: " + result);
				this.statistics.incorrectResult();
			}
		} catch (Exception e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning off the laundry when on");
		this.logMessage("    Given the laundry is initialised");
		this.logMessage("    And the laundry is on");
		try {
			this.logMessage("    When I turn off the laundry");
			this.luop.turnOff();
			result = this.luop.getState();
			if (result == LaundryState.OFF) {
				this.logMessage("    Then the state of the laundry is OFF");
			} else {
				this.logMessage("     but was: " + result);
				this.statistics.incorrectResult();
			}
		} catch (Exception e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test starting a wash in WHITE mode.
	 *
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 *
	 * <p>
	 * Gherkin specification
	 * </p>
	 * <p>
	 * </p>
	 *
	 * <pre>
	 * Feature: start a wash in WHITE mode
	 *   Scenario: starting a wash in WHITE mode
	 *     Given the Laundry is initialised
	 *     And the WHITE mode is set
	 *     When I start a wash
	 *     Then the mode of the Laundry is WHITE
	 *     And the Laundry is running
	 * </pre>
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 */

	protected void testStartWashInWhiteMode() {
		this.logMessage("Feature: start a wash in WHITE mode");
		this.logMessage("	Given the Laundry is initialised");
		this.logMessage("	And the WHITE mode is set");
		this.logMessage("	When I start a wash");
		this.logMessage("	Then the mode of the Laundry is WHITE");
		this.logMessage("	And the Laundry is running");
		try {

			this.logMessage("    When I turn on the laundry");
			this.luop.turnOn();
			if (this.luop.getState() == LaundryState.ON) {
				this.logMessage("    Then the state of the laundry is ON");
			} else {
				this.logMessage("     but was: " + this.luop.getState());
				this.statistics.incorrectResult();
			}

			this.logMessage("    When I set the laundry to WHITE mode");
			this.luop.setWhiteMode();
			if (this.luop.getWashMode() == LaundryWashMode.WHITE) {
				this.logMessage("    Then the mode of the laundry is WHITE");
			} else {
				this.logMessage(" but was " + this.luop.getWashMode());
				this.statistics.incorrectResult();
			}

			this.logMessage("    When I start a wash");
			this.luop.startWash();
			if (this.luop.isRunning()) {
				this.logMessage("    Then the laundry is running");
			} else {
				this.logMessage(" but was not running");
				this.statistics.incorrectResult();
			}

			this.logMessage("    When I cancel the wash");
			this.luop.cancelWash();

			this.luop.turnOff();
		} catch (Exception e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();

	}

	/**
	 * test starting a wash in INTENSIVE mode.
	 *
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 *
	 * <p>
	 * Gherkin specification
	 * </p>
	 * <p>
	 * </p>
	 *
	 * <pre>
	 * Feature: start a wash in INTENSIVE mode
	 *   Scenario: starting a wash in INTENSIVE mode
	 *     Given the Laundry is initialised
	 *     And the INTENSIVE mode is set
	 *     When I start a wash
	 *     Then the mode of the Laundry is INTENSIVE
	 *     And the Laundry is running
	 * </pre>
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testStartWashInIntensiveMode() {
		this.logMessage("Feature: start a wash in INTENSIVE mode");
		this.logMessage("	Given the Laundry is initialised");
		this.logMessage("	And the INTENSIVE mode is set");
		this.logMessage("	When I start a wash");
		this.logMessage("	Then the mode of the Laundry is INTENSIVE");
		this.logMessage("	And the Laundry is running");
		try {

			this.logMessage("    When I turn on the laundry");
			this.luop.turnOn();
			if (this.luop.getState() == LaundryState.ON) {
				this.logMessage("    Then the state of the laundry is ON");
			} else {
				this.logMessage("     but was: " + this.luop.getState());
				this.statistics.incorrectResult();
			}

			this.logMessage("    When I set the laundry to INTENSIVE mode");
			this.luop.setIntensiveMode();
			if (this.luop.getWashMode() == LaundryWashMode.INTENSIVE) {
				this.logMessage("    Then the mode of the laundry is INTENSIVE");
			} else {
				this.logMessage(" but was " + this.luop.getWashMode());
				this.statistics.incorrectResult();
			}

			this.logMessage("    When I start a wash");
			this.luop.startWash();
			if (this.luop.isRunning()) {
				this.logMessage("    Then the laundry is running");
			} else {
				this.logMessage(" but was not running");
				this.statistics.incorrectResult();
			}

			this.logMessage("    When I cancel the wash");
			this.luop.cancelWash();

			this.luop.turnOff();

		} catch (Exception e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();

	}

	/**
	 * test setting wash temperature and spin speed.
	 *
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 *
	 * <pre>
	 * Feature: setting wash temperature and spin speed
	 *   Scenario: setting wash temperature to 60 degrees
	 *     Given the laundry is on
	 *     When I set the wash temperature to 60 degrees
	 *     Then the wash temperature is 60 degrees
	 *   Scenario: setting spin speed to 1200 RPM
	 *     Given the laundry is on
	 *     When I set the spin speed to 1200 RPM
	 *     Then the spin speed is 1200 RPM
	 * </pre>
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testSetTemperatureAndSpinSpeed() {
		this.logMessage("Feature: setting wash temperature and spin speed");

		this.logMessage("  Scenario: setting wash temperature to 60 degrees");
		this.logMessage("    Given the laundry is on");
		try {
			this.luop.turnOn();
			this.logMessage("    When I set the wash temperature to 60 degrees");
			Measure<Double> temp60 = new Measure<Double>(60.0, MeasurementUnit.CELSIUS);
			this.luop.setWashTemperature(temp60);
			Measure<Double> currentTemp = this.luop.getWashTemperature();
			if (currentTemp != null && currentTemp.getData() == 60.0) {
				this.logMessage("    Then the wash temperature is 60 degrees");
			} else {
				this.logMessage("     but was: " + (currentTemp != null ? currentTemp.getData() : "null"));
				this.statistics.incorrectResult();
			}

			this.logMessage("  Scenario: setting spin speed to 1200 RPM");
			this.logMessage("    Given the laundry is on");
			this.logMessage("    When I set the spin speed to 1200 RPM");
			this.luop.setSpinSpeed(SpinSpeed.RPM_1200);
			SpinSpeed currentSpeed = this.luop.getSpinSpeed();
			if (currentSpeed == SpinSpeed.RPM_1200) {
				this.logMessage("    Then the spin speed is 1200 RPM");
			} else {
				this.logMessage("     but was: " + currentSpeed);
				this.statistics.incorrectResult();
			}

			this.luop.turnOff();
		} catch (Exception e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	protected void runAllUnitTests() {
		this.testOff();
		this.testOn();
		this.testTurnOnTurnOff();
		this.testStartWashInWhiteMode();
		this.testStartWashInIntensiveMode();
		this.testSetTemperatureAndSpinSpeed();

		this.statistics.statisticsReport(this);

	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			this.doPortConnection(this.luop.getPortURI(), this.laundryUserInboundPortURI,
					LaundryUserConnector.class.getCanonicalName());

		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	public synchronized void execute() throws Exception {
		if (this.isUnitTest) {
			this.runAllUnitTests();
		} else {
			System.out.println("Lancement script test intégration (LaundryTest)");
			ClocksServerOutboundPort clocksServerOutboundPort = new ClocksServerOutboundPort(this);
			clocksServerOutboundPort.publishPort();

			this.doPortConnection(clocksServerOutboundPort.getPortURI(), ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			this.traceMessage("Laundry tester gets the clock.\n");
			AcceleratedClock ac = clocksServerOutboundPort.getClock(CVMIntegrationTest.CLOCK_URI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort = null;

			Instant startInstant = ac.getStartInstant();
			Instant laundrySwitchOn = startInstant.plusSeconds(SWITCH_ON_DELAY);
			Instant laundrySwitchOff = startInstant.plusSeconds(SWITCH_OFF_DELAY);
			Instant laundryStartWashWhiteMode = startInstant.plusSeconds(START_WASH_WHITE_MODE_DELAY);
			Instant laundryStartWashColorMode = startInstant.plusSeconds(START_WASH_COLOR_MODE_DELAY);
			Instant laundryStartWashIntensiveMode = startInstant.plusSeconds(START_WASH_INTENSIVE_MODE_DELAY);
			Instant laundryStartWashDelicateMode = startInstant.plusSeconds(START_WASH_DELICATE_MODE_DELAY);
			
			this.traceMessage("Laundry tester waits until start.\n");
			ac.waitUntilStart();
			this.traceMessage("Laundry tester schedules switch on and off.\n");
			long delayToSwitchOn = ac.nanoDelayUntilInstant(laundrySwitchOn);
			long delayToSwitchOff = ac.nanoDelayUntilInstant(laundrySwitchOff);
			long delayToStartWashWhiteMode = ac.nanoDelayUntilInstant(laundryStartWashWhiteMode);
			long delayToStartWashColorMode = ac.nanoDelayUntilInstant(laundryStartWashColorMode);
			long delayToStartWashIntensiveMode = ac.nanoDelayUntilInstant(laundryStartWashIntensiveMode);
			long delayToStartWashDelicateMode = ac.nanoDelayUntilInstant(laundryStartWashDelicateMode);

			// This is to avoid mixing the 'this' of the task object with the 'this'
			// representing the component object in the code of the next methods run
			AbstractComponent o = this;

			// schedule the switch on Laundry
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry switches on.\n");
						luop.turnOn();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToSwitchOn, TimeUnit.NANOSECONDS);
			
			


			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry starts wash in WHITE mode.\n");
						luop.setWhiteMode();
						luop.setWashTemperature(new Measure<Double>(60.0, MeasurementUnit.CELSIUS));
						luop.setSpinSpeed(SpinSpeed.RPM_1000);
						luop.startWash();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				
			}, delayToStartWashWhiteMode, TimeUnit.NANOSECONDS);
			
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry starts wash in COLOR mode.\n");
						luop.cancelWash();
						luop.setColorMode();
						luop.setWashTemperature(new Measure<Double>(30.0, MeasurementUnit.CELSIUS));
						luop.setSpinSpeed(SpinSpeed.RPM_800);
						luop.startWash();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToStartWashColorMode, TimeUnit.NANOSECONDS);


			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry cancels current wash and starts wash in INTENSIVE mode.\n");
						luop.cancelWash();
						luop.setIntensiveMode();
						luop.setWashTemperature(new Measure<Double>(90.0, MeasurementUnit.CELSIUS));
						luop.setSpinSpeed(SpinSpeed.RPM_1400);
						luop.startWash();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToStartWashIntensiveMode, TimeUnit.NANOSECONDS);
			
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry cancels current wash and starts wash in DELICATE mode.\n");
						luop.cancelWash();
						luop.setDelicateMode();
						luop.setWashTemperature(new Measure<Double>(40.0, MeasurementUnit.CELSIUS));
						luop.setSpinSpeed(SpinSpeed.RPM_600);
						luop.startWash();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToStartWashDelicateMode, TimeUnit.NANOSECONDS);



			// schedule the switch off Laundry
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry cancels wash and switches off.\n");
						luop.cancelWash();
						luop.turnOff();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToSwitchOff, TimeUnit.NANOSECONDS);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.luop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.luop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

}
