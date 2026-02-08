package etape1.equipements.kettle;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import tests_utils.TestsStatistics;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape1.CVMIntegrationTest;
import etape1.equipements.kettle.connections.connectors.KettleUserConnector;
import etape1.equipements.kettle.connections.ports.KettleUserOutboundPort;
import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape1.equipements.kettle.interfaces.KettleUserCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleUnitTester</code> implements a component performing
 * unit tests for the water heater (chauffe-eau) component.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = { KettleUserCI.class, ClocksServerCI.class })
public class KettleUnitTester extends AbstractComponent {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static final int SWITCH_ON_DELAY = 2;
	public static final int START_HEATING_DELAY = 4;
	public static final int STOP_HEATING_DELAY = 7;
	public static final int SWITCH_OFF_DELAY = 35;

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;

	protected final boolean isUnitTest;
	protected String kettleUserInboundPortURI;
	protected KettleUserOutboundPort hop;
	protected TestsStatistics statistics;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected KettleUnitTester(boolean isUnitTest) throws Exception {
		this(isUnitTest, Kettle.USER_INBOUND_PORT_URI);
	}

	protected KettleUnitTester(boolean isUnitTest, String kettleUserInboundPortURI) throws Exception {
		super(1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(kettleUserInboundPortURI);
	}

	protected void initialise(String kettleUserInboundPortURI) throws Exception {
		this.kettleUserInboundPortURI = kettleUserInboundPortURI;
		this.hop = new KettleUserOutboundPort(this);
		this.hop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Kettle tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();
	}

	// -------------------------------------------------------------------------
	// Test methods
	// -------------------------------------------------------------------------

	/**
	 * Test initial state of the water heater.
	 *
	 * <pre>
	 * Feature: getting the state of the water heater
	 *   Scenario: initial state is OFF
	 *     Given the water heater is initialised
	 *     When I test the state
	 *     Then the state is OFF
	 * </pre>
	 */
	protected void testInitialState() {
		this.logMessage("Feature: getting the state of the water heater");
		this.logMessage("  Scenario: initial state is OFF");
		this.logMessage("    Given the water heater is initialised");
		try {
			this.logMessage("    When I test the state");
			if (this.hop.getState() == KettleState.OFF) {
				this.logMessage("    Then the state is OFF");
			} else {
				this.logMessage("     but was: " + this.hop.getState());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();
	}

	/**
	 * Test turning on and off the water heater.
	 *
	 * <pre>
	 * Feature: turning on and off the water heater
	 *   Scenario: turning on then off
	 *     Given the water heater is OFF
	 *     When I turn it on
	 *     Then the state is ON
	 *     When I turn it off
	 *     Then the state is OFF
	 * </pre>
	 */
	protected void testTurnOnTurnOff() {
		this.logMessage("Feature: turning on and off the water heater");
		this.logMessage("  Scenario: turning on then off");
		try {
			this.logMessage("    Given the water heater is OFF");
			this.logMessage("    When I turn it on");
			this.hop.turnOn();
			if (this.hop.getState() == KettleState.ON) {
				this.logMessage("    Then the state is ON");
			} else {
				this.logMessage("     but was: " + this.hop.getState());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();

		try {
			this.logMessage("    When I turn it off");
			this.hop.turnOff();
			if (this.hop.getState() == KettleState.OFF) {
				this.logMessage("    Then the state is OFF");
			} else {
				this.logMessage("     but was: " + this.hop.getState());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();
	}

	/**
	 * Test start/stop heating cycle.
	 *
	 * <pre>
	 * Feature: heating cycle
	 *   Scenario: start and stop heating
	 *     Given the water heater is ON
	 *     When I start heating
	 *     Then the state is HEATING
	 *     When I stop heating
	 *     Then the state is ON
	 * </pre>
	 */
	protected void testStartStopHeating() {
		this.logMessage("Feature: heating cycle");
		this.logMessage("  Scenario: start and stop heating");
		try {
			this.logMessage("    Given the water heater is ON");
			this.hop.turnOn();

			this.logMessage("    When I start heating");
			this.hop.startHeating();
			if (this.hop.getState() == KettleState.HEATING) {
				this.logMessage("    Then the state is HEATING");
			} else {
				this.logMessage("     but was: " + this.hop.getState());
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			if (this.hop.isHeating()) {
				this.logMessage("    And isHeating() returns true");
			} else {
				this.logMessage("     but isHeating() returned false");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			this.logMessage("    When I stop heating");
			this.hop.stopHeating();
			if (this.hop.getState() == KettleState.ON) {
				this.logMessage("    Then the state is ON");
			} else {
				this.logMessage("     but was: " + this.hop.getState());
				this.statistics.incorrectResult();
			}

			this.hop.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();
	}

	/**
	 * Test setting target temperature.
	 *
	 * <pre>
	 * Feature: setting target temperature
	 *   Scenario: set temperature to 55C then 70C
	 *     Given the water heater is ON
	 *     When I set target temperature to 55C
	 *     Then the target temperature is 55C
	 *     When I set target temperature to 70C
	 *     Then the target temperature is 70C
	 * </pre>
	 */
	protected void testSetTargetTemperature() {
		this.logMessage("Feature: setting target temperature");
		this.logMessage("  Scenario: set temperature to 55C then 70C");
		try {
			this.logMessage("    Given the water heater is ON");
			this.hop.turnOn();

			this.logMessage("    When I set target temperature to 55C");
			this.hop.setTargetTemperature(
					new Measure<Double>(55.0, MeasurementUnit.CELSIUS));
			double target = this.hop.getTargetTemperature().getData();
			if (target == 55.0) {
				this.logMessage("    Then the target temperature is 55C");
			} else {
				this.logMessage("     but was: " + target);
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			this.logMessage("    When I set target temperature to 70C");
			this.hop.setTargetTemperature(
					new Measure<Double>(70.0, MeasurementUnit.CELSIUS));
			target = this.hop.getTargetTemperature().getData();
			if (target == 70.0) {
				this.logMessage("    Then the target temperature is 70C");
			} else {
				this.logMessage("     but was: " + target);
				this.statistics.incorrectResult();
			}

			this.hop.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();
	}

	/**
	 * Test mode switching.
	 *
	 * <pre>
	 * Feature: mode switching
	 *   Scenario: switch through ECO, NORMAL, MAX modes
	 *     Given the water heater is ON (default mode NORMAL)
	 *     When I set mode to ECO
	 *     Then the mode is ECO
	 *     When I set mode to MAX
	 *     Then the mode is MAX
	 *     When I set mode to NORMAL
	 *     Then the mode is NORMAL
	 * </pre>
	 */
	protected void testModeSwitch() {
		this.logMessage("Feature: mode switching");
		this.logMessage("  Scenario: switch through ECO, NORMAL, MAX modes");
		try {
			this.logMessage("    Given the water heater is ON (default mode NORMAL)");
			this.hop.turnOn();

			if (this.hop.getKettleMode() == KettleMode.NORMAL) {
				this.logMessage("    Default mode is NORMAL");
			} else {
				this.logMessage("     but default mode was: " + this.hop.getKettleMode());
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			this.logMessage("    When I set mode to ECO");
			this.hop.setMode(KettleMode.ECO);
			if (this.hop.getKettleMode() == KettleMode.ECO) {
				this.logMessage("    Then the mode is ECO");
			} else {
				this.logMessage("     but was: " + this.hop.getKettleMode());
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			this.logMessage("    When I set mode to MAX");
			this.hop.setMode(KettleMode.MAX);
			if (this.hop.getKettleMode() == KettleMode.MAX) {
				this.logMessage("    Then the mode is MAX");
			} else {
				this.logMessage("     but was: " + this.hop.getKettleMode());
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			this.logMessage("    When I set mode to NORMAL");
			this.hop.setMode(KettleMode.NORMAL);
			if (this.hop.getKettleMode() == KettleMode.NORMAL) {
				this.logMessage("    Then the mode is NORMAL");
			} else {
				this.logMessage("     but was: " + this.hop.getKettleMode());
				this.statistics.incorrectResult();
			}

			this.hop.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();
	}

	/**
	 * Test suspend and resume by HEM.
	 *
	 * <pre>
	 * Feature: suspend and resume
	 *   Scenario: HEM suspends and resumes the water heater
	 *     Given the water heater is ON in MAX mode
	 *     When HEM suspends it
	 *     Then isSuspended is true and mode is SUSPEND
	 *     When HEM resumes it
	 *     Then isSuspended is false and mode is restored to MAX
	 * </pre>
	 */
	protected void testSuspendResume() {
		this.logMessage("Feature: suspend and resume");
		this.logMessage("  Scenario: HEM suspends and resumes the water heater");
		try {
			this.logMessage("    Given the water heater is ON in MAX mode");
			this.hop.turnOn();
			this.hop.setMode(KettleMode.MAX);

			this.logMessage("    When HEM suspends it");
			this.hop.suspend();
			if (this.hop.isSuspended()) {
				this.logMessage("    Then isSuspended is true");
			} else {
				this.logMessage("     but isSuspended() returned false");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			if (this.hop.getKettleMode() == KettleMode.SUSPEND) {
				this.logMessage("    And mode is SUSPEND");
			} else {
				this.logMessage("     but mode was: " + this.hop.getKettleMode());
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			this.logMessage("    When HEM resumes it");
			this.hop.resume();
			if (!this.hop.isSuspended()) {
				this.logMessage("    Then isSuspended is false");
			} else {
				this.logMessage("     but isSuspended() returned true");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			if (this.hop.getKettleMode() == KettleMode.MAX) {
				this.logMessage("    And mode is restored to MAX");
			} else {
				this.logMessage("     but mode was: " + this.hop.getKettleMode());
				this.statistics.incorrectResult();
			}

			this.hop.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();
	}

	/**
	 * Test power level operations.
	 *
	 * <pre>
	 * Feature: power level management
	 *   Scenario: get and set power levels
	 *     Given the water heater is ON
	 *     Then max power level is 3000W
	 *     And current power level matches NORMAL mode (2000W)
	 * </pre>
	 */
	protected void testPowerLevels() {
		this.logMessage("Feature: power level management");
		this.logMessage("  Scenario: get and set power levels");
		try {
			this.logMessage("    Given the water heater is ON");
			this.hop.turnOn();

			double maxPower = this.hop.getMaxPowerLevel().getData();
			if (maxPower == KettleImplementationI.MAX_POWER_LEVEL) {
				this.logMessage("    Then max power level is " + maxPower + "W");
			} else {
				this.logMessage("     but max power was: " + maxPower);
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			double currentPower = this.hop.getCurrentPowerLevel().getData();
			if (currentPower == KettleImplementationI.NORMAL_MODE_POWER) {
				this.logMessage("    And current power level is " + currentPower + "W (NORMAL mode)");
			} else {
				this.logMessage("     but current power was: " + currentPower);
				this.statistics.incorrectResult();
			}

			this.hop.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.statistics.updateStatistics();
	}

	protected void runAllUnitTests() {
		this.testInitialState();
		this.testTurnOnTurnOff();
		this.testStartStopHeating();
		this.testSetTargetTemperature();
		this.testModeSwitch();
		this.testSuspendResume();
		this.testPowerLevels();

		this.statistics.statisticsReport(this);
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();
		try {
			this.doPortConnection(this.hop.getPortURI(), this.kettleUserInboundPortURI,
					KettleUserConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	@Override
	public synchronized void execute() throws Exception {
		if (this.isUnitTest) {
			this.runAllUnitTests();
		} else {
			ClocksServerOutboundPort clocksServerOutboundPort = new ClocksServerOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			this.traceMessage("Kettle tester gets the clock.\n");
			AcceleratedClock ac = clocksServerOutboundPort.getClock(CVMIntegrationTest.CLOCK_URI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort = null;

			Instant startInstant = ac.getStartInstant();
			Instant kettleSwitchOn = startInstant.plusSeconds(SWITCH_ON_DELAY);
			Instant kettleStartHeating = startInstant.plusSeconds(START_HEATING_DELAY);
			Instant kettleStopHeating = startInstant.plusSeconds(STOP_HEATING_DELAY);
			Instant kettleSwitchOff = startInstant.plusSeconds(SWITCH_OFF_DELAY);
			this.traceMessage("Kettle tester waits until start.\n");
			ac.waitUntilStart();
			this.traceMessage("Kettle tester schedules operations.\n");

			long delayToSwitchOn = ac.nanoDelayUntilInstant(kettleSwitchOn);
			long delayToStartHeating = ac.nanoDelayUntilInstant(kettleStartHeating);
			long delayToStopHeating = ac.nanoDelayUntilInstant(kettleStopHeating);
			long delayToSwitchOff = ac.nanoDelayUntilInstant(kettleSwitchOff);

			AbstractComponent o = this;

			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Kettle switches on.\n");
						hop.turnOn();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToSwitchOn, TimeUnit.NANOSECONDS);

			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Kettle starts heating.\n");
						hop.startHeating();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToStartHeating, TimeUnit.NANOSECONDS);

			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Kettle stops heating.\n");
						hop.stopHeating();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToStopHeating, TimeUnit.NANOSECONDS);

			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Kettle switches off.\n");
						hop.turnOff();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToSwitchOff, TimeUnit.NANOSECONDS);
		}
	}

	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.hop.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.hop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
