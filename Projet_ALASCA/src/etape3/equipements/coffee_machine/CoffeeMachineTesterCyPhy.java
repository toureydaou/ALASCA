package etape3.equipements.coffee_machine;

import etape1.equipements.coffee_machine.connectors.CoffeeMachineExternalControlConnector;
import etape1.equipements.coffee_machine.connectors.CoffeeMachineInternalConnector;
import etape1.equipements.coffee_machine.connectors.CoffeeMachineUserConnector;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserCI;
import etape1.equipements.coffee_machine.ports.CoffeeMachineExternalControlOutboundPort;
import etape1.equipements.coffee_machine.ports.CoffeeMachineInternalOutboundPort;
import etape1.equipements.coffee_machine.ports.CoffeeMachineUserOutboundPort;
import fr.sorbonne_u.alasca.physical_data.Measure;
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
 * The class <code>CoffeeMachineTesterCyPhy</code> implements a component performing
 * tests for the class <code>CoffeeMachineCyPhy</code> as a BCM component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p><strong>Implementation Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code coffeeMachineUserInboundPortURI != null && !coffeeMachineUserInboundPortURI.isEmpty()}
 * invariant	{@code coffeeMachineInternalControlInboundPortURI != null && !coffeeMachineInternalControlInboundPortURI.isEmpty()}
 * invariant	{@code coffeeMachineExternalControlInboundPortURI != null && !coffeeMachineExternalControlInboundPortURI.isEmpty()}
 * </pre>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 *
 * <p>Created on : 2026-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required={CoffeeMachineUserCI.class,
							  CoffeeMachineInternalControlCI.class,
							  CoffeeMachineExternalControlJava4CI.class})
public class CoffeeMachineTesterCyPhy extends AbstractCyPhyComponent {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int Y_RELATIVE_POSITION = 0;

	/** standard reflection, inbound port URI for the CoffeeMachineUnitTesterCyPhy component. */
	public static final String REFLECTION_INBOUND_PORT_URI = "coffee-machine-unit-tester-RIP-URI";

	/** URI of the user component interface inbound port.					*/
	protected String coffeeMachineUserInboundPortURI;
	/** URI of the internal control component interface inbound port.		*/
	protected String coffeeMachineInternalControlInboundPortURI;
	/** URI of the external control component interface inbound port.		*/
	protected String coffeeMachineExternalControlInboundPortURI;

	/** user component interface outbound port.								*/
	protected CoffeeMachineUserOutboundPort cmUserOP;
	/** internal control component interface outbound port.					*/
	protected CoffeeMachineInternalOutboundPort cmInternalOP;
	/** external control component interface outbound port.					*/
	protected CoffeeMachineExternalControlOutboundPort cmExternalOP;

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
	 * pre	{@code cmt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param cmt	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean implementationInvariants(CoffeeMachineTesterCyPhy cmt) {
		assert cmt != null : new PreconditionException("cmt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				cmt.coffeeMachineUserInboundPortURI != null &&
									!cmt.coffeeMachineUserInboundPortURI.isEmpty(),
				CoffeeMachineTesterCyPhy.class, cmt,
				"cmt.coffeeMachineUserInboundPortURI != null && "
							+ "!cmt.coffeeMachineUserInboundPortURI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
				cmt.coffeeMachineInternalControlInboundPortURI != null &&
							!cmt.coffeeMachineInternalControlInboundPortURI.isEmpty(),
				CoffeeMachineTesterCyPhy.class, cmt,
				"cmt.coffeeMachineInternalControlInboundPortURI != null && "
						+ "!cmt.coffeeMachineInternalControlInboundPortURI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
				cmt.coffeeMachineExternalControlInboundPortURI != null &&
							!cmt.coffeeMachineExternalControlInboundPortURI.isEmpty(),
				CoffeeMachineTesterCyPhy.class, cmt,
				"cmt.coffeeMachineExternalControlInboundPortURI != null &&"
						+ "!cmt.coffeeMachineExternalControlInboundPortURI.isEmpty()");
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
				CoffeeMachineTesterCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				CoffeeMachineTesterCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants is observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code cmt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param cmt	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean invariants(CoffeeMachineTesterCyPhy cmt) {
		assert cmt != null : new PreconditionException("cmt != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution for manual tests (no test scenario and no simulation)

	/**
	 * create a coffee machine unit tester component manual tests without  test scenario
	 * or simulation.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code getCurrentExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected CoffeeMachineTesterCyPhy(
		String coffeeMachineUserInboundPortURI,
		String coffeeMachineInternalControlInboundPortURI,
		String coffeeMachineExternalControlInboundPortURI
		) throws Exception {
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.initialise(coffeeMachineUserInboundPortURI,
						coffeeMachineInternalControlInboundPortURI,
						coffeeMachineExternalControlInboundPortURI);
	}

	// Test execution with test scenario but no simulation

	/**
	 * create a coffee machine unit tester component manual tests with test scenario but
	 * no simulation (for this component).
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code coffeeMachineUserInboundPortURI != null && !coffeeMachineUserInboundPortURI.isEmpty()}
	 * pre	{@code coffeeMachineInternalControlInboundPortURI != null && !coffeeMachineInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code coffeeMachineExternalControlInboundPortURI != null && !coffeeMachineExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && !executionMode.isStandard()}
	 * pre	{@code testScenario != null}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param coffeeMachineUserInboundPortURI				URI of the user component interface inbound port.
	 * @param coffeeMachineInternalControlInboundPortURI	URI of the internal control component interface inbound port.
	 * @param coffeeMachineExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @throws Exception										<i>to do</i>.
	 */
	protected CoffeeMachineTesterCyPhy(
		String coffeeMachineUserInboundPortURI,
		String coffeeMachineInternalControlInboundPortURI,
		String coffeeMachineExternalControlInboundPortURI,
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

		this.initialise(coffeeMachineUserInboundPortURI,
						coffeeMachineInternalControlInboundPortURI,
						coffeeMachineExternalControlInboundPortURI);
	}

	/**
	 * initialise a coffee machine test component.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code coffeeMachineUserInboundPortURI != null && !coffeeMachineUserInboundPortURI.isEmpty()}
	 * pre	{@code coffeeMachineInternalControlInboundPortURI != null && !coffeeMachineInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code coffeeMachineExternalControlInboundPortURI != null && !coffeeMachineExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param coffeeMachineUserInboundPortURI				URI of the user component interface inbound port.
	 * @param coffeeMachineInternalControlInboundPortURI	URI of the internal control component interface inbound port.
	 * @param coffeeMachineExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @throws Exception										<i>to do</i>.
	 */
	protected void initialise(
		String coffeeMachineUserInboundPortURI,
		String coffeeMachineInternalControlInboundPortURI,
		String coffeeMachineExternalControlInboundPortURI
		) throws Exception {
		this.coffeeMachineUserInboundPortURI = coffeeMachineUserInboundPortURI;
		this.cmUserOP = new CoffeeMachineUserOutboundPort(this);
		this.cmUserOP.publishPort();
		this.coffeeMachineInternalControlInboundPortURI = coffeeMachineInternalControlInboundPortURI;
		this.cmInternalOP = new CoffeeMachineInternalOutboundPort(this);
		this.cmInternalOP.publishPort();
		this.coffeeMachineExternalControlInboundPortURI = coffeeMachineExternalControlInboundPortURI;
		this.cmExternalOP = new CoffeeMachineExternalControlOutboundPort(this);
		this.cmExternalOP.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("CoffeeMachine tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert CoffeeMachineTesterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"CoffeeMachineTester.implementationInvariants(this)");
		assert CoffeeMachineTesterCyPhy.invariants(this) :
				new InvariantException("CoffeeMachineTester.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Test action helper methods
	// -------------------------------------------------------------------------

	/**
	 * return the coffee machine user outbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the coffee machine user outbound port.
	 */
	public CoffeeMachineUserOutboundPort getCmUserOP() {
		return this.cmUserOP;
	}

	/**
	 * return the coffee machine internal control outbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the coffee machine internal control outbound port.
	 */
	public CoffeeMachineInternalOutboundPort getCmInternalOP() {
		return this.cmInternalOP;
	}

	/**
	 * return the coffee machine external control outbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the coffee machine external control outbound port.
	 */
	public CoffeeMachineExternalControlOutboundPort getCmExternalOP() {
		return this.cmExternalOP;
	}

	// -------------------------------------------------------------------------
	// Tests implementations
	// -------------------------------------------------------------------------

	/**
	 * test getting the state of the coffee machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: getting the state of the coffee machine
	 *   Scenario: getting the state of the coffee machine when off
	 *     Given the coffee machine is initialised
	 *     And the coffee machine has not been used yet
	 *     When I test the state of the coffee machine
	 *     Then the state of the coffee machine is off
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
		this.logMessage("Feature: getting the state of the coffee machine");
		this.logMessage("  Scenario: getting the state of the coffee machine when off");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine has not been used yet");
		try {
			this.logMessage("    When I test the state of the coffee machine");
			boolean result = !this.cmUserOP.on();
			if (result) {
				this.logMessage("    Then the state of the coffee machine is off");
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
	 * test switching on and off the coffee machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: switching on and off the coffee machine
	 *   Scenario: switching on the coffee machine when off
	 *     Given the coffee machine is initialised
	 *     And the coffee machine has not been used yet
	 *     When I switch on the coffee machine
	 *     Then the state of the coffee machine is on
	 *   Scenario: switching off the coffee machine when on
	 *     Given the coffee machine is initialised
	 *     And the coffee machine is on
	 *     When I switch off the coffee machine
	 *     Then the state of the coffee machine is off
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
		this.logMessage("Feature: switching on and off the coffee machine");

		this.logMessage("  Scenario: switching on the coffee machine when off");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine has not been used yet");
		boolean result;
		try {
			this.logMessage("    When I switch on the coffee machine");
			this.cmUserOP.turnOn();
			result = this.cmUserOP.on();
			if (result) {
				this.logMessage("    Then the state of the coffee machine is on");
			} else {
				this.logMessage("     but was: off");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: switching off the coffee machine when on");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine is on");
		try {
			this.logMessage("    When I switch off the coffee machine");
			this.cmUserOP.turnOff();
			result = !this.cmUserOP.on();
			if (result) {
				this.logMessage("    Then the state of the coffee machine is off");
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
	 * test heating operations of the coffee machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: heating control of the coffee machine
	 *   Scenario: starting heating when coffee machine is on
	 *     Given the coffee machine is initialised
	 *     And the coffee machine is on
	 *     And the coffee machine is not heating
	 *     When I start heating
	 *     Then the coffee machine is heating
	 *   Scenario: stopping heating when coffee machine is heating
	 *     Given the coffee machine is initialised
	 *     And the coffee machine is on
	 *     And the coffee machine is heating
	 *     When I stop heating
	 *     Then the coffee machine is not heating
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
		this.logMessage("Feature: heating control of the coffee machine");

		this.logMessage("  Scenario: starting heating when coffee machine is on");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine is on");
		this.logMessage("    And the coffee machine is not heating");
		boolean result;
		try {
			this.cmUserOP.turnOn();
			result = this.cmUserOP.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I start heating");
			this.cmInternalOP.startHeating();
			result = this.cmInternalOP.heating();
			if (result) {
				this.logMessage("    Then the coffee machine is heating");
			} else {
				this.logMessage("     but was: not heating");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: stopping heating when coffee machine is heating");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine is on");
		this.logMessage("    And the coffee machine is heating");
		try {
			this.logMessage("    When I stop heating");
			this.cmInternalOP.stopHeating();
			result = !this.cmInternalOP.heating();
			if (result) {
				this.logMessage("    Then the coffee machine is not heating");
			} else {
				this.logMessage("     but was: heating");
				this.statistics.incorrectResult();
			}
			this.cmUserOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting the temperature of the coffee machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: getting the temperature of the coffee machine
	 *   Scenario: getting the temperature when on
	 *     Given the coffee machine is initialised
	 *     And the coffee machine is on
	 *     When I get the temperature of the coffee machine
	 *     Then the temperature is between min and max temperature
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
		this.logMessage("Feature: getting the temperature of the coffee machine");

		this.logMessage("  Scenario: getting the temperature when on");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine is on");
		boolean result;
		Measure<Double> temperature = null;
		try {
			this.cmUserOP.turnOn();
			result = this.cmUserOP.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I get the temperature of the coffee machine");
			temperature = this.cmInternalOP.getTemperature();
			if (temperature.getData() >= CoffeeMachineCyPhy.MIN_TEMPERATURE.getData() &&
				temperature.getData() <= CoffeeMachineCyPhy.MAX_TEMPERATURE.getData() &&
				temperature.getMeasurementUnit().equals(CoffeeMachineCyPhy.TEMPERATURE_UNIT)) {
				this.logMessage("    Then the temperature is between min and max temperature: " + temperature.getData());
			} else {
				this.logMessage("     but was: " + temperature.getData());
				this.statistics.incorrectResult();
			}
			this.cmUserOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting and setting the power level of the coffee machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: getting and setting the power level of the coffee machine
	 *   Scenario: getting the maximum power level
	 *     Given the coffee machine is initialised
	 *     When I get the maximum power level
	 *     Then the result is the coffee machine maximum power level
	 *   Scenario: setting the power level
	 *     Given the coffee machine is initialised
	 *     And the coffee machine is on
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
		this.logMessage("Feature: getting and setting the power level of the coffee machine");

		this.logMessage("  Scenario: getting the maximum power level");
		this.logMessage("    Given the coffee machine is initialised");
		Measure<Double> powerLevel = null;
		try {
			this.logMessage("    When I get the maximum power level");
			powerLevel = this.cmExternalOP.getMaxPowerLevel();
			if (powerLevel.getData() == CoffeeMachineCyPhy.HIGH_POWER_IN_WATTS.getData() &&
				powerLevel.getMeasurementUnit().equals(CoffeeMachineCyPhy.HIGH_POWER_IN_WATTS.getMeasurementUnit())) {
				this.logMessage("    Then the result is the coffee machine maximum power level");
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
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine is on");
		boolean result;
		try {
			this.cmUserOP.turnOn();
			result = this.cmUserOP.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I set the current power level to a given level");
			double testPower = CoffeeMachineCyPhy.NORMAL_POWER_IN_WATTS.getData();
			this.cmExternalOP.setCurrentPowerLevel(new Measure<Double>(testPower, CoffeeMachineCyPhy.POWER_UNIT));
			powerLevel = this.cmExternalOP.getPowerLevel();
			if (powerLevel.getData() == testPower &&
				powerLevel.getMeasurementUnit().equals(CoffeeMachineCyPhy.POWER_UNIT)) {
				this.logMessage("    Then the current power level is the given power level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " + powerLevel.getData());
			}
			this.cmUserOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test mode operations of the coffee machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: setting modes of the coffee machine
	 *   Scenario: setting suspend mode
	 *     Given the coffee machine is initialised
	 *     And the coffee machine is on
	 *     When I set suspend mode
	 *     Then the coffee machine mode is SUSPEND
	 *   Scenario: setting eco mode
	 *     Given the coffee machine is on
	 *     When I set eco mode
	 *     Then the coffee machine mode is ECO
	 *   Scenario: setting normal mode
	 *     Given the coffee machine is on
	 *     When I set normal mode
	 *     Then the coffee machine mode is NORMAL
	 *   Scenario: setting max mode
	 *     Given the coffee machine is on
	 *     When I set max mode
	 *     Then the coffee machine mode is MAX
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
		this.logMessage("Feature: setting modes of the coffee machine");

		this.logMessage("  Scenario: setting suspend mode");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine is on");
		boolean result;
		try {
			this.cmUserOP.turnOn();
			result = this.cmUserOP.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I set suspend mode");
			this.cmUserOP.setSuspendMode();
			CoffeeMachineMode mode = this.cmUserOP.getMode();
			if (mode == CoffeeMachineMode.SUSPEND) {
				this.logMessage("    Then the coffee machine mode is SUSPEND");
			} else {
				this.logMessage("     but was: " + mode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting eco mode");
		this.logMessage("    Given the coffee machine is on");
		try {
			this.logMessage("    When I set eco mode");
			this.cmUserOP.setEcoMode();
			CoffeeMachineMode mode = this.cmUserOP.getMode();
			if (mode == CoffeeMachineMode.ECO) {
				this.logMessage("    Then the coffee machine mode is ECO");
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
		this.logMessage("    Given the coffee machine is on");
		try {
			this.logMessage("    When I set normal mode");
			this.cmUserOP.setNormalMode();
			CoffeeMachineMode mode = this.cmUserOP.getMode();
			if (mode == CoffeeMachineMode.NORMAL) {
				this.logMessage("    Then the coffee machine mode is NORMAL");
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
		this.logMessage("    Given the coffee machine is on");
		try {
			this.logMessage("    When I set max mode");
			this.cmUserOP.setMaxMode();
			CoffeeMachineMode mode = this.cmUserOP.getMode();
			if (mode == CoffeeMachineMode.MAX) {
				this.logMessage("    Then the coffee machine mode is MAX");
			} else {
				this.logMessage("     but was: " + mode);
				this.statistics.incorrectResult();
			}
			this.cmUserOP.turnOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test coffee making operations.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Gherkin specification</p>
	 * <pre>
	 * Feature: making coffee and filling water
	 *   Scenario: making an expresso
	 *     Given the coffee machine is initialised
	 *     And the coffee machine is on
	 *     When I make an expresso
	 *     Then the operation completes successfully
	 *   Scenario: filling water tank
	 *     Given the coffee machine is on
	 *     When I fill water
	 *     Then the operation completes successfully
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
	protected void testCoffeeMaking() {
		this.logMessage("Feature: making coffee and filling water");

		this.logMessage("  Scenario: filling water tank");
		this.logMessage("    Given the coffee machine is initialised");
		this.logMessage("    And the coffee machine is on");
		boolean result;
		try {
			this.cmUserOP.turnOn();
			result = this.cmUserOP.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I fill water");
			this.cmUserOP.fillWater();
			this.logMessage("    Then the operation completes successfully");
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: making an expresso");
		this.logMessage("    Given the coffee machine is on");
		try {
			this.logMessage("    When I make an expresso");
			this.cmUserOP.makeExpresso();
			this.logMessage("    Then the operation completes successfully");
			this.cmUserOP.turnOff();
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
		this.testPowerLevel();
		this.testModes();
		this.testCoffeeMaking();

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
					this.cmUserOP.getPortURI(),
					this.coffeeMachineUserInboundPortURI,
					CoffeeMachineUserConnector.class.getCanonicalName());
			this.doPortConnection(
					this.cmInternalOP.getPortURI(),
					coffeeMachineInternalControlInboundPortURI,
					CoffeeMachineInternalConnector.class.getCanonicalName());
			this.doPortConnection(
					this.cmExternalOP.getPortURI(),
					coffeeMachineExternalControlInboundPortURI,
					CoffeeMachineExternalControlConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception {
		this.traceMessage("CoffeeMachine Unit Tester begins execution.\n");

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
			this.traceMessage("CoffeeMachine Unit Tester starts the tests.\n");
			this.runAllUnitTests();
			this.traceMessage("CoffeeMachine Unit Tester ends.\n");
			break;
		default:
		}

		this.traceMessage("CoffeeMachine Unit Tester ends execution.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.cmUserOP.getPortURI());
		this.doPortDisconnection(this.cmInternalOP.getPortURI());
		this.doPortDisconnection(this.cmExternalOP.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.cmUserOP.unpublishPort();
			this.cmInternalOP.unpublishPort();
			this.cmExternalOP.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
