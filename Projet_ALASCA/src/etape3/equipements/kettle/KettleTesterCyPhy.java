package etape3.equipements.kettle;

import etape1.equipements.kettle.connections.connectors.KettleExternalControlConnector;
import etape1.equipements.kettle.connections.connectors.KettleUserConnector;
import etape1.equipements.kettle.connections.ports.KettleExternalControlOutboundPort;
import etape1.equipements.kettle.connections.ports.KettleUserOutboundPort;
import etape1.equipements.kettle.interfaces.KettleExternalControlJava4CI;
import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape1.equipements.kettle.interfaces.KettleUserCI;
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
 * The class <code>KettleTesterCyPhy</code> implements a component performing
 * tests for the class <code>KettleCyPhy</code> as a BCM component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p><strong>Implementation Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code kettleUserInboundPortURI != null && !kettleUserInboundPortURI.isEmpty()}
 * invariant	{@code kettleExternalControlInboundPortURI != null && !kettleExternalControlInboundPortURI.isEmpty()}
 * </pre>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 *
 * <p>Created on : 2026-02-06</p>
 *
 */
@RequiredInterfaces(required={KettleUserCI.class,
							  KettleExternalControlJava4CI.class})
public class KettleTesterCyPhy extends AbstractCyPhyComponent {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int Y_RELATIVE_POSITION = 0;

	/** standard reflection, inbound port URI for the KettleTesterCyPhy component. */
	public static final String REFLECTION_INBOUND_PORT_URI = "kettle-unit-tester-RIP-URI";

	/** URI of the user component interface inbound port.					*/
	protected String kettleUserInboundPortURI;
	/** URI of the external control component interface inbound port.		*/
	protected String kettleExternalControlInboundPortURI;

	/** user component interface outbound port.								*/
	protected KettleUserOutboundPort kuOP;
	/** external control component interface outbound port.					*/
	protected KettleExternalControlOutboundPort keOP;

	// Execution/Simulation

	/** one thread for the method execute.									*/
	protected static int NUMBER_OF_STANDARD_THREADS = 1;
	/** one thread to schedule this component test actions.					*/
	protected static int NUMBER_OF_SCHEDULABLE_THREADS = 1;

	/** collector of test statistics.										*/
	protected TestsStatistics statistics;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code kt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param kt	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean implementationInvariants(KettleTesterCyPhy kt) {
		assert kt != null : new PreconditionException("kt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				kt.kettleUserInboundPortURI != null &&
									!kt.kettleUserInboundPortURI.isEmpty(),
				KettleTesterCyPhy.class, kt,
				"kt.kettleUserInboundPortURI != null && "
							+ "!kt.kettleUserInboundPortURI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
				kt.kettleExternalControlInboundPortURI != null &&
							!kt.kettleExternalControlInboundPortURI.isEmpty(),
				KettleTesterCyPhy.class, kt,
				"kt.kettleExternalControlInboundPortURI != null && "
						+ "!kt.kettleExternalControlInboundPortURI.isEmpty()");
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
	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				KettleTesterCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				KettleTesterCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code kt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param kt	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean invariants(KettleTesterCyPhy kt) {
		assert kt != null : new PreconditionException("kt != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution for manual tests (no test scenario and no simulation)

	/**
	 * create a kettle unit tester component for manual tests without test scenario
	 * or simulation.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code getCurrentExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @param kettleUserInboundPortURI				URI of the user component interface inbound port.
	 * @param kettleExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @throws Exception	<i>to do</i>.
	 */
	protected KettleTesterCyPhy(
		String kettleUserInboundPortURI,
		String kettleExternalControlInboundPortURI
		) throws Exception {
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.initialise(kettleUserInboundPortURI,
						kettleExternalControlInboundPortURI);
	}

	// Test execution with test scenario but no simulation

	/**
	 * create a kettle unit tester component with test scenario.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code kettleUserInboundPortURI != null && !kettleUserInboundPortURI.isEmpty()}
	 * pre	{@code kettleExternalControlInboundPortURI != null && !kettleExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && !executionMode.isStandard()}
	 * pre	{@code testScenario != null}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param kettleUserInboundPortURI				URI of the user component interface inbound port.
	 * @param kettleExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @param executionMode							the execution mode for this component.
	 * @param testScenario							the test scenario to execute.
	 * @throws Exception	<i>to do</i>.
	 */
	/**
	 * Constructor with custom reflection URI for multiple instances.
	 */
	protected KettleTesterCyPhy(
		String reflectionInboundPortURI,
		String kettleUserInboundPortURI,
		String kettleExternalControlInboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario
		) throws Exception {
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				executionMode != null && !executionMode.isStandard(),
				executionMode,
				() -> new PreconditionException(
								"currentExecutionMode != null && "
								+ "!currentExecutionMode.isStandard()")),
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null,
				testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario);

		this.initialise(kettleUserInboundPortURI,
						kettleExternalControlInboundPortURI);
	}

	protected KettleTesterCyPhy(
		String kettleUserInboundPortURI,
		String kettleExternalControlInboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario
		) throws Exception {
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				executionMode != null && !executionMode.isStandard(),
				executionMode,
				() -> new PreconditionException(
								"currentExecutionMode != null && "
								+ "!currentExecutionMode.isStandard()")),
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null,
				testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario);

		this.initialise(kettleUserInboundPortURI,
						kettleExternalControlInboundPortURI);
	}

	/**
	 * initialise a kettle test component.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code kettleUserInboundPortURI != null && !kettleUserInboundPortURI.isEmpty()}
	 * pre	{@code kettleExternalControlInboundPortURI != null && !kettleExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param kettleUserInboundPortURI				URI of the user component interface inbound port.
	 * @param kettleExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void initialise(
		String kettleUserInboundPortURI,
		String kettleExternalControlInboundPortURI
		) throws Exception {
		this.kettleUserInboundPortURI = kettleUserInboundPortURI;
		this.kuOP = new KettleUserOutboundPort(this);
		this.kuOP.publishPort();
		this.kettleExternalControlInboundPortURI = kettleExternalControlInboundPortURI;
		this.keOP = new KettleExternalControlOutboundPort(this);
		this.keOP.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Kettle tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert KettleTesterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"KettleTesterCyPhy.implementationInvariants(this)");
		assert KettleTesterCyPhy.invariants(this) :
				new InvariantException("KettleTesterCyPhy.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Test action helper methods
	// -------------------------------------------------------------------------

	/**
	 * return the kettle user outbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the kettle user outbound port.
	 */
	public KettleUserOutboundPort getKuOP() {
		return this.kuOP;
	}

	/**
	 * return the kettle external control outbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the kettle external control outbound port.
	 */
	public KettleExternalControlOutboundPort getKeOP() {
		return this.keOP;
	}

	// -------------------------------------------------------------------------
	// Tests implementations
	// -------------------------------------------------------------------------

	/**
	 * test getting the state of the kettle.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: getting the state of the kettle
	 *   Scenario: getting the state of the kettle when off
	 *     Given the kettle is initialised
	 *     And the kettle has not been used yet
	 *     When I test the state of the kettle
	 *     Then the state of the kettle is off
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testOff() {
		this.logMessage("Feature: getting the state of the kettle");
		this.logMessage("  Scenario: getting the state of the kettle when off");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle has not been used yet");
		try {
			this.logMessage("    When I test the state of the kettle");
			boolean result = this.kuOP.getState() == KettleState.OFF;
			if (result) {
				this.logMessage("    Then the state of the kettle is off");
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
	 * test switching on and off the kettle.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: switching on and off the kettle
	 *   Scenario: switching on the kettle when off
	 *     Given the kettle is initialised
	 *     And the kettle has not been used yet
	 *     When I switch on the kettle
	 *     Then the state of the kettle is on
	 *   Scenario: switching off the kettle when on
	 *     Given the kettle is initialised
	 *     And the kettle is on
	 *     When I switch off the kettle
	 *     Then the state of the kettle is off
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testSwitchOnSwitchOff() {
		this.logMessage("Feature: switching on and off the kettle");

		this.logMessage("  Scenario: switching on the kettle when off");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle has not been used yet");
		boolean result;
		try {
			this.logMessage("    When I switch on the kettle");
			this.kuOP.turnOn();
			result = this.kuOP.getState() != KettleState.OFF;
			if (result) {
				this.logMessage("    Then the state of the kettle is on");
			} else {
				this.logMessage("     but was: off");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: switching off the kettle when on");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle is on");
		try {
			this.logMessage("    When I switch off the kettle");
			this.kuOP.turnOff();
			result = this.kuOP.getState() == KettleState.OFF;
			if (result) {
				this.logMessage("    Then the state of the kettle is off");
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
	 * test heating operations of the kettle.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: heating control of the kettle
	 *   Scenario: starting heating when kettle is on
	 *     Given the kettle is initialised
	 *     And the kettle is on
	 *     And the kettle is not heating
	 *     When I start heating
	 *     Then the kettle is heating
	 *   Scenario: stopping heating when kettle is heating
	 *     Given the kettle is initialised
	 *     And the kettle is on
	 *     And the kettle is heating
	 *     When I stop heating
	 *     Then the kettle is not heating
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testHeating() {
		this.logMessage("Feature: heating control of the kettle");

		this.logMessage("  Scenario: starting heating when kettle is on");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle is on");
		this.logMessage("    And the kettle is not heating");
		boolean result;
		try {
			this.kuOP.turnOn();
			result = this.kuOP.getState() != KettleState.OFF;
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I start heating");
			this.kuOP.startHeating();
			result = this.kuOP.isHeating();
			if (result) {
				this.logMessage("    Then the kettle is heating");
			} else {
				this.logMessage("     but was: not heating");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: stopping heating when kettle is heating");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle is on");
		this.logMessage("    And the kettle is heating");
		try {
			this.logMessage("    When I stop heating");
			this.kuOP.stopHeating();
			result = !this.kuOP.isHeating();
			if (result) {
				this.logMessage("    Then the kettle is not heating");
			} else {
				this.logMessage("     but was: heating");
				this.statistics.incorrectResult();
			}
			this.kuOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting the temperature of the kettle.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: getting the temperature of the kettle
	 *   Scenario: getting the temperature when on
	 *     Given the kettle is initialised
	 *     And the kettle is on
	 *     When I get the temperature of the kettle
	 *     Then the temperature is between min and max target temperature
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testTemperature() {
		this.logMessage("Feature: getting the temperature of the kettle");

		this.logMessage("  Scenario: getting the temperature when on");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle is on");
		boolean result;
		Measure<Double> temperature = null;
		try {
			this.kuOP.turnOn();
			result = this.kuOP.getState() != KettleState.OFF;
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I get the temperature of the kettle");
			temperature = this.kuOP.getCurrentTemperature();
			if (temperature.getData() >= 0.0 &&
				temperature.getData() <= KettleImplementationI.MAX_TARGET_TEMPERATURE &&
				temperature.getMeasurementUnit().equals(KettleImplementationI.TEMPERATURE_UNIT)) {
				this.logMessage("    Then the temperature is between min and max target temperature: " + temperature.getData());
			} else {
				this.logMessage("     but was: " + temperature.getData());
				this.statistics.incorrectResult();
			}
			this.kuOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test mode operations of the kettle.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: setting modes of the kettle
	 *   Scenario: setting eco mode
	 *     Given the kettle is initialised
	 *     And the kettle is on
	 *     When I set eco mode
	 *     Then the kettle mode is ECO
	 *   Scenario: setting normal mode
	 *     Given the kettle is on
	 *     When I set normal mode
	 *     Then the kettle mode is NORMAL
	 *   Scenario: setting max mode
	 *     Given the kettle is on
	 *     When I set max mode
	 *     Then the kettle mode is MAX
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testModes() {
		this.logMessage("Feature: setting modes of the kettle");

		this.logMessage("  Scenario: setting eco mode");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle is on");
		boolean result;
		try {
			this.kuOP.turnOn();
			result = this.kuOP.getState() != KettleState.OFF;
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I set eco mode");
			this.kuOP.setMode(KettleMode.ECO);
			KettleMode mode = this.kuOP.getKettleMode();
			if (mode == KettleMode.ECO) {
				this.logMessage("    Then the kettle mode is ECO");
			} else {
				this.logMessage("     but was: " + mode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting normal mode");
		this.logMessage("    Given the kettle is on");
		try {
			this.logMessage("    When I set normal mode");
			this.kuOP.setMode(KettleMode.NORMAL);
			KettleMode mode = this.kuOP.getKettleMode();
			if (mode == KettleMode.NORMAL) {
				this.logMessage("    Then the kettle mode is NORMAL");
			} else {
				this.logMessage("     but was: " + mode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting max mode");
		this.logMessage("    Given the kettle is on");
		try {
			this.logMessage("    When I set max mode");
			this.kuOP.setMode(KettleMode.MAX);
			KettleMode mode = this.kuOP.getKettleMode();
			if (mode == KettleMode.MAX) {
				this.logMessage("    Then the kettle mode is MAX");
			} else {
				this.logMessage("     but was: " + mode);
				this.statistics.incorrectResult();
			}
			this.kuOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting and setting the power level of the kettle.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: getting and setting the power level of the kettle
	 *   Scenario: getting the maximum power level
	 *     Given the kettle is initialised
	 *     When I get the maximum power level
	 *     Then the result is the kettle maximum power level
	 *   Scenario: setting the power level
	 *     Given the kettle is initialised
	 *     And the kettle is on
	 *     When I set the current power level to a given level
	 *     Then the current power level is the given power level
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void testPowerLevel() {
		this.logMessage("Feature: getting and setting the power level of the kettle");

		this.logMessage("  Scenario: getting the maximum power level");
		this.logMessage("    Given the kettle is initialised");
		Measure<Double> powerLevel = null;
		try {
			this.logMessage("    When I get the maximum power level");
			powerLevel = this.keOP.getMaxPowerLevel();
			if (powerLevel.getData() == KettleImplementationI.MAX_POWER_LEVEL &&
				powerLevel.getMeasurementUnit().equals(KettleImplementationI.POWER_UNIT)) {
				this.logMessage("    Then the result is the kettle maximum power level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " + powerLevel.getData());
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting the power level");
		this.logMessage("    Given the kettle is initialised");
		this.logMessage("    And the kettle is on");
		boolean result;
		try {
			this.kuOP.turnOn();
			result = this.kuOP.getState() != KettleState.OFF;
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I set the current power level to a given level");
			double testPower = KettleImplementationI.NORMAL_MODE_POWER;
			this.keOP.setCurrentPowerLevel(
					new Measure<Double>(testPower, KettleImplementationI.POWER_UNIT));
			powerLevel = this.keOP.getCurrentPowerLevel();
			if (powerLevel.getData() == testPower &&
				powerLevel.getMeasurementUnit().equals(KettleImplementationI.POWER_UNIT)) {
				this.logMessage("    Then the current power level is the given power level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " + powerLevel.getData());
			}
			this.kuOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * run all unit tests.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void runAllUnitTests() {
		this.testOff();
		this.testSwitchOnSwitchOff();
		this.testHeating();
		this.testTemperature();
		this.testModes();
		this.testPowerLevel();

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
			this.doPortConnection(
					this.kuOP.getPortURI(),
					this.kettleUserInboundPortURI,
					KettleUserConnector.class.getCanonicalName());
			this.doPortConnection(
					this.keOP.getPortURI(),
					this.kettleExternalControlInboundPortURI,
					KettleExternalControlConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception {
		this.traceMessage("Kettle Unit Tester begins execution.\n");

		switch (this.getExecutionMode()) {
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
			this.statistics = new TestsStatistics();
			this.traceMessage("Kettle Unit Tester starts the tests.\n");
			this.runAllUnitTests();
			this.traceMessage("Kettle Unit Tester ends.\n");
			break;
		default:
		}

		this.traceMessage("Kettle Unit Tester ends execution.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.kuOP.getPortURI());
		this.doPortDisconnection(this.keOP.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.kuOP.unpublishPort();
			this.keOP.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
