package etape3.equipements.laundry;

import etape1.equipements.laundry.connectors.LaundryUserConnector;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;
import etape1.equipements.laundry.interfaces.LaundryUserCI;
import etape1.equipements.laundry.ports.LaundryUserOutboundPort;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestsStatistics;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryTesterCyPhy</code> implements a component performing
 * tests for the class <code>LaundryCyPhy</code> as a BCM component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This tester component connects to the LaundryCyPhy through the user interface
 * and performs basic unit tests on the laundry machine operations.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
@RequiredInterfaces(required={LaundryUserCI.class})
public class LaundryTesterCyPhy
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static boolean VERBOSE = false;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	public static final String REFLECTION_INBOUND_PORT_URI =
		"laundry-tester-RIP-URI";

	protected String laundryUserInboundPortURI;
	protected LaundryUserOutboundPort luOP;

	protected static int NUMBER_OF_STANDARD_THREADS = 1;
	protected static int NUMBER_OF_SCHEDULABLE_THREADS = 1;

	protected TestsStatistics statistics;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean implementationInvariants(
		LaundryTesterCyPhy lt
	)
	{
		assert lt != null : new PreconditionException("lt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				lt.laundryUserInboundPortURI != null
					&& !lt.laundryUserInboundPortURI.isEmpty(),
				LaundryTesterCyPhy.class, lt,
				"lt.laundryUserInboundPortURI != null && "
					+ "!lt.laundryUserInboundPortURI.isEmpty()");
		return ret;
	}

	protected static boolean invariants(LaundryTesterCyPhy lt) {
		assert lt != null : new PreconditionException("lt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkInvariant(
				X_RELATIVE_POSITION >= 0,
				LaundryTesterCyPhy.class, lt,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(
				Y_RELATIVE_POSITION >= 0,
				LaundryTesterCyPhy.class, lt,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected LaundryTesterCyPhy(
		String laundryUserInboundPortURI
	) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.initialise(laundryUserInboundPortURI);
	}

	protected LaundryTesterCyPhy(
		String laundryUserInboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario
	) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				executionMode != null && !executionMode.isStandard(),
				executionMode,
				() -> new PreconditionException(
					"executionMode != null && "
					+ "!executionMode.isStandard()")),
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null,
				testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario);

		this.initialise(laundryUserInboundPortURI);
	}

	protected void initialise(String laundryUserInboundPortURI)
	throws Exception
	{
		this.laundryUserInboundPortURI = laundryUserInboundPortURI;
		this.luOP = new LaundryUserOutboundPort(this);
		this.luOP.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Laundry tester component");
			this.tracer.get().setRelativePosition(
				X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert LaundryTesterCyPhy.implementationInvariants(this)
			: new ImplementationInvariantException(
					"LaundryTesterCyPhy.implementationInvariants(this)");
		assert LaundryTesterCyPhy.invariants(this)
			: new InvariantException(
					"LaundryTesterCyPhy.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Test action helper methods
	// -------------------------------------------------------------------------

	public LaundryUserOutboundPort getLuOP() {
		return this.luOP;
	}

	// -------------------------------------------------------------------------
	// Tests
	// -------------------------------------------------------------------------

	protected void testOff() {
		this.logMessage("Feature: getting the state of the laundry machine");
		this.logMessage("  Scenario: getting the state when off");
		this.logMessage("    Given the laundry is initialised");
		try {
			this.logMessage("    When I test the state of the laundry");
			LaundryState state = this.luOP.getState();
			if (state == LaundryState.OFF) {
				this.logMessage("    Then the state is OFF");
			} else {
				this.logMessage("     but was: " + state);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but exception: " + e);
		}
		this.statistics.updateStatistics();
	}

	protected void testSwitchOnSwitchOff() {
		this.logMessage("Feature: switching on and off the laundry");

		this.logMessage("  Scenario: switching on when off");
		try {
			this.luOP.turnOn();
			LaundryState state = this.luOP.getState();
			if (state == LaundryState.ON) {
				this.logMessage("    Then the state is ON");
			} else {
				this.logMessage("     but was: " + state);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but exception: " + e);
		}
		this.statistics.updateStatistics();

		this.logMessage("  Scenario: switching off when on");
		try {
			this.luOP.turnOff();
			LaundryState state = this.luOP.getState();
			if (state == LaundryState.OFF) {
				this.logMessage("    Then the state is OFF");
			} else {
				this.logMessage("     but was: " + state);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but exception: " + e);
		}
		this.statistics.updateStatistics();
	}

	protected void testWashCycle() {
		this.logMessage("Feature: wash cycle operations");

		this.logMessage("  Scenario: start and cancel wash");
		try {
			this.luOP.turnOn();

			this.luOP.setWhiteMode();
			LaundryWashMode mode = this.luOP.getWashMode();
			if (mode == LaundryWashMode.WHITE) {
				this.logMessage("    Mode set to WHITE");
			} else {
				this.logMessage("     but mode was: " + mode);
				this.statistics.incorrectResult();
			}

			this.luOP.setWashTemperature(
				new Measure<Double>(60.0, MeasurementUnit.CELSIUS));
			this.luOP.setSpinSpeed(SpinSpeed.RPM_1200);

			this.luOP.startWash();
			boolean running = this.luOP.isRunning();
			if (running) {
				this.logMessage("    Wash cycle started");
			} else {
				this.logMessage("     but was not running");
				this.statistics.incorrectResult();
			}

			this.luOP.cancelWash();
			running = this.luOP.isRunning();
			if (!running) {
				this.logMessage("    Wash cycle cancelled");
			} else {
				this.logMessage("     but was still running");
				this.statistics.incorrectResult();
			}

			this.luOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but exception: " + e);
		}
		this.statistics.updateStatistics();
	}

	protected void testModes() {
		this.logMessage("Feature: setting wash modes");

		try {
			this.luOP.turnOn();

			// Test DELICATE
			this.luOP.setDelicateMode();
			if (this.luOP.getWashMode() == LaundryWashMode.DELICATE) {
				this.logMessage("    DELICATE mode set");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     DELICATE mode failed");
			}
			this.statistics.updateStatistics();

			// Test COLOR
			this.luOP.setColorMode();
			if (this.luOP.getWashMode() == LaundryWashMode.COLOR) {
				this.logMessage("    COLOR mode set");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     COLOR mode failed");
			}
			this.statistics.updateStatistics();

			// Test INTENSIVE
			this.luOP.setIntensiveMode();
			if (this.luOP.getWashMode() == LaundryWashMode.INTENSIVE) {
				this.logMessage("    INTENSIVE mode set");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     INTENSIVE mode failed");
			}
			this.statistics.updateStatistics();

			this.luOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but exception: " + e);
			this.statistics.updateStatistics();
		}
	}

	protected void runAllUnitTests() {
		this.testOff();
		this.testSwitchOnSwitchOff();
		this.testWashCycle();
		this.testModes();
		this.statistics.statisticsReport(this);
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			this.doPortConnection(
				this.luOP.getPortURI(),
				this.laundryUserInboundPortURI,
				LaundryUserConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	@Override
	public synchronized void execute() throws Exception {
		this.traceMessage("Laundry Tester begins execution.\n");

		switch (this.getExecutionMode()) {
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(
				ClocksServer.STANDARD_INBOUNDPORT_URI, this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
				ClocksServer.STANDARD_INBOUNDPORT_URI, this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
			this.statistics = new TestsStatistics();
			this.traceMessage("Laundry Tester starts the tests.\n");
			this.runAllUnitTests();
			this.traceMessage("Laundry Tester ends.\n");
			break;
		default:
		}

		this.traceMessage("Laundry Tester ends execution.\n");
	}

	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.luOP.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.luOP.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
