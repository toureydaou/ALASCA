package etape1.equipements.coffee_machine;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape1.CVMIntegrationTest;
import etape1.equipements.coffee_machine.connectors.CoffeeMachineInternalConnector;
import etape1.equipements.coffee_machine.connectors.CoffeeMachineUserConnector;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserCI;
import etape1.equipements.coffee_machine.ports.CoffeeMachineInternalOutboundPort;
import etape1.equipements.coffee_machine.ports.CoffeeMachineUserOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import tests_utils.TestsStatistics;

@RequiredInterfaces(required = { CoffeeMachineInternalControlCI.class, CoffeeMachineUserCI.class })
@OfferedInterfaces(offered = { CoffeeMachineExternalControlJava4CI.class })
public class CoffeeMachineUnitTester extends AbstractComponent {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	
	/** URI of the heater port for internal control. */
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "COFFEE-TEST-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Coffee Machine is switched on.
	 */
	public static final int SWITCH_ON_DELAY = 2;
	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Coffee Machine is switched off.
	 */
	public static final int SWITCH_OFF_DELAY = 9;

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
	protected String coffeeMachineUserInboundPortURI;
	/** URI of the internal control component interface inbound port. */
	protected String coffeeMachineInternalControlInboundPortURI;

	/** user component interface inbound port. */
	protected CoffeeMachineUserOutboundPort cmuop;
	/** internal control component interface inbound port. */
	protected CoffeeMachineInternalOutboundPort cmiip;

	/** collector of test statistics. */
	protected TestsStatistics statistics;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a Coffee Machine test component.
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

	protected CoffeeMachineUnitTester(boolean isUnitTest) throws Exception {
		this(isUnitTest, CoffeeMachine.INTERNAL_CONTROL_INBOUND_PORT_URI, CoffeeMachine.USER_INBOUND_PORT_URI);
	}

	/**
	 * create a Coffee Machine test component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryInternalControlInboundPortURI != null && !LaundryInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty()
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
	 * @param LaundryUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @param LaundryInternalControlInboundPortURI URI of the internal control
	 *                                             component interface inbound port.
	 * @param LaundryExternalControlInboundPortURI URI of the external control
	 *                                             component interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected CoffeeMachineUnitTester(boolean isUnitTest, String coffeeMachineInternalControlInboundPortURI,
			String coffeeMachineUserInboundPortURI) throws Exception {
		super(1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(coffeeMachineInternalControlInboundPortURI, coffeeMachineUserInboundPortURI);
	}

	/**
	 * create a Coffee Machine test component.
	 * 
	 * <p>
	 * <strong>Contract</strong> Coffee Machine *
	 * 
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryInternalControlInboundPortURI != null && !LaundryInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty()
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
	 * @param LaundryUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @param LaundryInternalControlInboundPortURI URI of the internal control
	 *                                             component interface inbound port.
	 * @param LaundryExternalControlInboundPortURI URI of the external control
	 *                                             component interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected CoffeeMachineUnitTester(boolean isUnitTest, String reflectionInboundPortURI,
			String coffeeMachineInternalControlInboundPortURI, String coffeeMachineUserInboundPortURI)
			throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(coffeeMachineInternalControlInboundPortURI, coffeeMachineUserInboundPortURI);
	}

	/**
	 * initialise a Coffee Machine test component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryInternalControlInboundPortURI != null && !LaundryInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param LaundryUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @param LaundryInternalControlInboundPortURI URI of the internal control
	 *                                             component interface inbound port.
	 * @param LaundryExternalControlInboundPortURI URI of the external control
	 *                                             component interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected void initialise(String coffeeMachineInternalControlInboundPortURI, String coffeeMachineUserInboundPortURI)
			throws Exception {
		this.coffeeMachineInternalControlInboundPortURI = coffeeMachineInternalControlInboundPortURI;
		this.cmuop = new CoffeeMachineUserOutboundPort(this);
		this.cmuop.publishPort();

		this.coffeeMachineUserInboundPortURI = coffeeMachineUserInboundPortURI;
		this.cmiip = new CoffeeMachineInternalOutboundPort(this);
		this.cmiip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Coffee Machine tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		/*
		 * assert LaundryUnitTester.implementationInvariants(this) : new
		 * ImplementationInvariantException(
		 * "LaundryTester.implementationInvariants(this)"); assert
		 * LaundryUnitTester.invariants(this) : new
		 * InvariantException("LaundryTester.invariants(this)");
		 */
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * test getting the state of the Coffee Machine.
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
	 * Feature: getting the state of the Coffee Machine
	 *   Scenario: getting the state of the Coffee Machine when off
	 *     Given the Coffee Machine is initialised
	 *     And the Coffee Machine has not been used yet
	 *     When I test the state of the Coffee Machine
	 *     Then the state of the Coffee Machine is off
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
		this.logMessage("Feature: getting the state of the Coffee Machine");
		this.logMessage("  Scenario: getting the state of the Coffee Machine when off");
		this.logMessage("    Given the Coffee Machine is initialised");
		this.logMessage("    And the Coffee Machine has not been used yet");
		try {
			this.logMessage("    When I test the state of the Coffee Machine");
			this.cmuop.turnOn();
			if (this.cmuop.getState() == CoffeeMachineState.ON) {
				this.logMessage("    Then the state of the Coffee Machine is off");
			} else {
				this.logMessage("     but was: on");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching on and off the heater.
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
	 * Feature: switching on and off the heater
	 *   Scenario: switching on the heater when off
	 *     Given the heater is initialised
	 *     And the heater has not been used yet
	 *     When I switch on the heater
	 *     Then the state of the heater is on
	 *   Scenario: switching off the heater when on
	 *     Given the heater is initialised
	 *     And the heater is on
	 *     When I switch off the heater
	 *     Then the state of the heater is off
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
	protected void testSwitchOnSwitchOff() {
		this.logMessage("Feature: switching on and off the heater");

		this.logMessage("  Scenario: switching on the heater when off");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		boolean result;
		try {
			this.logMessage("    When I switch on the heater");
			this.cmuop.turnOn();
			result = this.cmuop.on();
			if (result) {
				this.logMessage("    Then the state of the heater is on");
			} else {
				this.logMessage("     but was: off");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: switching off the heater when on");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I switch off the heater");
			this.cmuop.turnOff();
			result = !this.cmuop.on();
			if (result) {
				this.logMessage("    Then the state of the heater is off");
			} else {
				this.logMessage("     but was: on");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	protected void testExpresso() {
		this.logMessage("Feature: getting the state of the Coffee Machine");
		this.logMessage("  Scenario: getting the state of the Coffee Machine when off");
		this.logMessage("    Given the Coffee Machine is initialised");
		this.logMessage("    And the Coffee Machine has not been used yet");
		try {
			this.logMessage("    When I test the state of the Coffee Machine");
			this.cmuop.turnOn();
			this.cmuop.setExpresso();
			if (this.cmuop.getMode() == CoffeeMachineMode.EXPRESSO) {
				this.logMessage("    Then the state of the Coffee Machine is off");
			} else {
				this.logMessage("     but was: on");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	protected void testThe() {
		this.logMessage("Feature: getting the state of the Coffee Machine");
		this.logMessage("  Scenario: getting the state of the Coffee Machine when off");
		this.logMessage("    Given the Coffee Machine is initialised");
		this.logMessage("    And the Coffee Machine has not been used yet");
		try {
			this.logMessage("    When I test the state of the Coffee Machine");
			this.cmuop.setThe();
			if (this.cmuop.getMode() == CoffeeMachineMode.THE) {
				this.logMessage("    Then the state of the Coffee Machine is off");
			} else {
				this.logMessage("     but was: on");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	protected void runAllUnitTests() {
		this.testOff();
		this.testSwitchOnSwitchOff();
		this.testExpresso();
		this.testThe();
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
			this.doPortConnection(this.cmuop.getPortURI(), this.coffeeMachineUserInboundPortURI,
					CoffeeMachineUserConnector.class.getCanonicalName());

			this.doPortConnection(this.cmiip.getPortURI(), this.coffeeMachineInternalControlInboundPortURI,
					CoffeeMachineInternalConnector.class.getCanonicalName());

		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	public synchronized void execute() throws Exception {
		if (this.isUnitTest) {
			this.runAllUnitTests();
		} else {
			ClocksServerOutboundPort clocksServerOutboundPort = new ClocksServerOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(clocksServerOutboundPort.getPortURI(), ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			this.traceMessage("Coffee Machine tester gets the clock.\n");
			AcceleratedClock ac = clocksServerOutboundPort.getClock(CVMIntegrationTest.CLOCK_URI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort = null;

			Instant startInstant = ac.getStartInstant();
			Instant LaundrySwitchOn = startInstant.plusSeconds(SWITCH_ON_DELAY);
			Instant LaundrySwitchOff = startInstant.plusSeconds(SWITCH_OFF_DELAY);
			this.traceMessage("Coffee Machine tester waits until start.\n");
			ac.waitUntilStart();
			this.traceMessage("Coffee Machine tester schedules switch on and off.\n");
			long delayToSwitchOn = ac.nanoDelayUntilInstant(LaundrySwitchOn);
			long delayToSwitchOff = ac.nanoDelayUntilInstant(LaundrySwitchOff);

			// This is to avoid mixing the 'this' of the task object with the 'this'
			// representing the component object in the code of the next methods run
			AbstractComponent o = this;

			// schedule the switch on Coffee Machine
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Coffee Machine switches on.\n");
						cmuop.turnOn();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToSwitchOn, TimeUnit.NANOSECONDS);

			// to be completed with a more covering scenario

			// schedule the switch off Coffee Machine
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Coffee Machine switches off.\n");
						cmuop.turnOff();
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
		this.doPortDisconnection(this.cmuop.getPortURI());
		this.doPortDisconnection(this.cmiip.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.cmuop.unpublishPort();
			this.cmiip.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

}
