package etape3.equipements.fan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import etape1.equipements.fan.connections.connectors.FanUserConnector;
import etape1.equipements.fan.connections.ports.FanOutboundPort;
import etape1.equipements.fan.interfaces.FanImplementationI.FanMode;
import etape1.equipements.fan.interfaces.FanImplementationI.FanState;
import etape1.equipements.fan.interfaces.FanUserCI;

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

import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
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
 * The class <code>FanTesterCyPhy</code> implements the cyber-physical component
 * performing tests for the class <code>FanCyPhy</code> as a BCM4Java-CyPhy
 * component.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * The har dryer tester component is meant to make sure that the component
 * methods work according to its expectation. It implements the following
 * execution mode:
 * </p>
 * <ul>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * fanInboundPortURI != null && !fanInboundPortURI.isEmpty()
 * }
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()
 * }
 * invariant	{@code
 * X_RELATIVE_POSITION >= 0
 * }
 * invariant	{@code
 * Y_RELATIVE_POSITION >= 0
 * }
 * </pre>
 * 
 * <p>
 * Created on : 2023-09-19
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = { FanUserCI.class })
public class FanTesterCyPhy extends AbstractCyPhyComponent {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;

	/**
	 * standard reflection, inbound port URI for the {@code FanUnitTesterCyPhy}
	 * component.
	 */
	public static final String REFLECTION_INBOUND_PORT_URI = "hair-dryer-unit-tester-RIP-URI";

	/** outbound port connecting to the fan component. */
	protected FanOutboundPort hdop;
	/** URI of the fan inbound port to connect to. */
	protected String fanInboundPortURI;

	// Execution/Simulation

	/** one thread for the method execute. */
	protected static int NUMBER_OF_STANDARD_THREADS = 1;
	/** one thread to schedule this component test actions. */
	protected static int NUMBER_OF_SCHEDULABLE_THREADS = 1;

	/** collector of test statistics. */
	protected TestsStatistics statistics;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * hdt != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param hdt instance to be tested.
	 * @return true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean implementationInvariants(FanTesterCyPhy hdt) {
		assert hdt != null : new PreconditionException("hdt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				hdt.fanInboundPortURI != null && !hdt.fanInboundPortURI.isEmpty(), FanTesterCyPhy.class, hdt,
				"hdt.fanInboundPortURI != null && " + "!hdt.fanInboundPortURI.isEmpty()");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @return true if the static invariants are observed, false otherwise.
	 */
	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty(), FanTesterCyPhy.class,
				"REFLECTION_INBOUND_PORT_URI != null && " + "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(X_RELATIVE_POSITION >= 0, FanTesterCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(Y_RELATIVE_POSITION >= 0, FanTesterCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * hdt != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param hdt instance to be tested.
	 * @return true if the invariants are observed, false otherwise.
	 */
	protected static boolean invariants(FanTesterCyPhy hdt) {
		assert hdt != null : new PreconditionException("hdt != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution for manual tests (no test scenario and no simulation)

	/**
	 * create a fan tester component manual tests without test scenario or
	 * simulation.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * !(this instanceof ComponentInterface)
	 * }
	 * pre	{@code
	 * fanInboundPortURI != null && !fanInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * getCurrentExecutionMode().isStandard()
	 * }
	 * </pre>
	 *
	 * @param fanInboundPortURI URI of the fan inbound port to connect to.
	 * @throws Exception <i>to do</i>.
	 */
	protected FanTesterCyPhy(String fanInboundPortURI) throws Exception {
		super(REFLECTION_INBOUND_PORT_URI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS);

		this.initialise(fanInboundPortURI);
	}

	// Test execution with test scenario

	/**
	 * create a fan tester component for tests (unit or integration) with a test
	 * scenario but no simulation.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * !(this instanceof ComponentInterface)
	 * }
	 * pre	{@code
	 * fanInboundPortURI != null && !fanInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * executionMode != null && !executionMode.isStandard()
	 * }
	 * pre	{@code
	 * testScenario != null
	 * }
	 * post	{@code
	 * getExecutionMode().equals(executionMode)
	 * }
	 * </pre>
	 *
	 * @param fanInboundPortURI URI of the fan inbound port to connect to.
	 * @param executionMode     execution mode for the next run.
	 * @param testScenario      test scenario to be executed.
	 * @throws Exception <i>to do</i>.
	 */
	protected FanTesterCyPhy(String fanInboundPortURI, ExecutionMode executionMode, TestScenario testScenario)
			throws Exception {
		super(REFLECTION_INBOUND_PORT_URI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS,
				AssertionChecking.assertTrueAndReturnOrThrow(executionMode != null && !executionMode.isStandard(),
						executionMode,
						() -> new PreconditionException(
								"currentExecutionMode != null && " + "!currentExecutionMode.isStandard()")),
				AssertionChecking.assertTrueAndReturnOrThrow(testScenario != null, testScenario.getClockURI(),
						() -> new PreconditionException("testScenario != null")),
				testScenario);

		this.initialise(fanInboundPortURI);
	}

	/**
	 * initialise a fan tester component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * fanInboundPortURI != null && !fanInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param fanInboundPortURI URI of the fan inbound port to connect to.
	 * @throws Exception <i>to do</i>.
	 */
	protected void initialise(String fanInboundPortURI) throws Exception {
		this.fanInboundPortURI = fanInboundPortURI;
		this.hdop = new FanOutboundPort(this);
		this.hdop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Fan tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		if (this.getExecutionMode().isTestWithoutSimulation()) {
			this.statistics = new TestsStatistics();
		}

		assert FanTesterCyPhy.implementationInvariants(this)
				: new ImplementationInvariantException("FanTester.implementationInvariants(this)");
		assert FanTesterCyPhy.invariants(this) : new InvariantException("FanTester.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Test action methods
	// -------------------------------------------------------------------------

	/**
	 * turn on the fan; method to be used in test scenario.
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
	 * @throws Exception <i>to do</i>.
	 */
	public void turnOnFan() throws Exception {
		this.hdop.turnOn();
	}

	/**
	 * turn off the fan; method to be used in test scenario.
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
	 * @throws Exception <i>to do</i>.
	 */
	public void turnOffFan() throws Exception {
		this.hdop.turnOff();
	}

	/**
	 * set the fan low; method to be used in test scenario.
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
	 * @throws Exception <i>to do</i>.
	 */
	public void setLowFan() throws Exception {
		this.hdop.setLow();
	}
	
	
	public void setMediumFan() throws Exception {
		this.hdop.setMedium();
	}

	/**
	 * set the fan high; method to be used in test scenario.
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
	 * @throws Exception <i>to do</i>.
	 */
	public void setHighFan() throws Exception {
		this.hdop.setHigh();
	}

	// -------------------------------------------------------------------------
	// Tests implementations
	// -------------------------------------------------------------------------

	/**
	 * test of the {@code getState} method when the fan is off.
	 * 
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * Gherkin specification:
	 * </p>
	 * 
	 * <pre>
	 * Feature: Getting the state of the fan
	 * 
	 *   Scenario: getting the state when off
	 *     Given the fan has not been used yet
	 *     When I test the state of the fan
	 *     Then the fan is in its initial state
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
	public void testGetState() {
		this.logMessage("Feature: Getting the state of the fan");
		this.logMessage("  Scenario: getting the state when off");
		this.logMessage("    Given the fan has not been used yet");
		FanState result = null;
		try {
			this.logMessage("    When I test the state of the fan");
			result = this.hdop.getState();
			this.logMessage("    Then the fan is in its initial state");
			if (!FanCyPhy.INITIAL_STATE.equals(result)) {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " + result);
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test of the {@code getMode} method when the fan is off.
	 * 
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * Gherkin specification:
	 * </p>
	 * 
	 * <pre>
	 * Feature: Getting the mode of the fan
	 * 
	 *   Scenario: getting the state when off
	 *     Given the fan is off
	 *     When I test the mode of the fan
	 *     Then the fan is in its initial mode
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
	public void testGetMode() {
		this.logMessage("Feature: Getting the mode of the fan");
		this.logMessage("  Scenario: getting the mode when off");
		this.logMessage("    Given the fan is off");
		FanState sResult = null;
		try {
			sResult = this.hdop.getState();
			if (!FanState.OFF.equals(sResult)) {
				this.statistics.failedCondition();
				this.logMessage("     but was: " + sResult);
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		FanMode mResult = null;
		try {
			this.logMessage("    When I test the mode of the fan");
			mResult = this.hdop.getMode();
			this.logMessage("    Then the fan is in its initial mode");
			if (!FanCyPhy.INITIAL_MODE.equals(mResult)) {
				this.logMessage("     but was: " + mResult);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test turning on and off the heir dryer.
	 * 
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * Gherkin specification:
	 * </p>
	 * 
	 * <pre>
	 * Feature: turning the fan on and off
	 * 
	 *   Scenario: turning on when off
	 *     Given the fan is off
	 *     When the fan is turned on
	 *     Then the fan is on
	 *     And the fan is low
	 * 
	 *   Scenario: turning on when on
	 *     Given the fan is on
	 *     When the fan is turned on
	 *     Then a precondition exception is thrown
	 * 
	 *   Scenario: turning off when on
	 *     Given the fan is on
	 *     When the fan is turned off
	 *     Then the fan is off
	 * 
	 *   Scenario: turning off when off
	 *     Given the fan is off
	 *     When the fan is turned off
	 *     Then a precondition exception is thrown
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
	public void testTurnOnOff() {
		this.logMessage("Feature: turning the fan on and off");
		this.logMessage("  Scenario: turning on when off");
		FanState resultState = null;
		FanMode resultMode = null;
		try {
			this.logMessage("    Given the fan is off");
			resultState = this.hdop.getState();
			if (!FanState.OFF.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
			this.logMessage("    When the fan is turned on");
			this.hdop.turnOn();
			this.logMessage("    Then the fan is on");
			resultState = this.hdop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
			this.logMessage("    And the fan is in mode low");
			resultMode = this.hdop.getMode();
			if (!FanMode.LOW.equals(resultMode)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning on when on");
		this.logMessage("    Given the fan is on");
		try {
			resultState = this.hdop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.logMessage("    When the fan is turned on");
		this.logMessage("    Then a precondition exception is thrown");
		boolean old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.turnOn();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {

		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning off when on");
		this.logMessage("    Given the fan is on");
		try {
			resultState = this.hdop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.logMessage("    When the fan is turned off");
		try {
			this.hdop.turnOff();
			this.logMessage("    Then the fan is off");
			resultState = this.hdop.getState();
			if (!FanState.OFF.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning off when off");
		this.logMessage("    Given the fan is off");
		try {
			resultState = this.hdop.getState();
			if (!FanState.OFF.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.logMessage("    When the fan is turned off");
		this.logMessage("    Then a precondition exception is thrown");
		old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.turnOff();
			this.logMessage("     but the precondition exception was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {

		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching mode of the fan.
	 * 
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * Gherkin specification:
	 * </p>
	 * 
	 * <pre>
	 * Feature: switching the fan low and high.
	 * 
	 *   Scenario: set the fan high from low
	 *     Given the fan is on
	 *     And the fan is low
	 *     When the fan is set high
	 *     Then the fan is on
	 *     And  the fan is high
	 * 
	 *   Scenario: set the fan high from high
	 *     Given the fan is on
	 *     And the fan is high
	 *     When the fan is set high
	 *     Then an exception is thrown
	 * 
	 *   Scenario: set the fan low from high
	 *     Given the fan is on
	 *     And the fan is high
	 *     When the fan is set low
	 *     Then the fan is on
	 *     And the fan is low
	 * 
	 *   Scenario: set the fan low from low
	 *     Given the fan is on
	 *     And the fan is low
	 *     When the fan is set low
	 *     Then an exception is thrown
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
	public void testSetLowHigh() {
		this.logMessage("Feature: switching the fan low and high.");
		this.logMessage("  Scenario: set the fan high from low");
		this.logMessage("    Given the fan is on");
		FanState resultState = null;
		FanMode resultMode = null;
		try {
			this.hdop.turnOn();
			resultState = this.hdop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    And the fan is low");
			resultMode = this.hdop.getMode();
			if (!FanMode.LOW.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    When the fan is set high");
			this.logMessage("    Then the fan is on");
			this.hdop.setHigh();
			resultState = this.hdop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    And  the fan is high");
			resultMode = this.hdop.getMode();
			if (!FanMode.HIGH.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: set the fan high from high");
		this.logMessage("    Given the fan is on");
		this.logMessage("    And the fan is high");
		this.logMessage("    When the fan is set high");
		this.logMessage("    Then a precondition exception is thrown");
		boolean old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.setHigh();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {

		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: set the fan low from high");
		this.logMessage("    Given the fan is on");
		this.logMessage("    And the fan is high");
		this.logMessage("    When the fan is set low");
		try {
			this.hdop.setLow();
			this.logMessage("    Then the fan is on");
			resultState = this.hdop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    And the fan is low");
			resultMode = this.hdop.getMode();
			if (!FanMode.LOW.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: set the fan low from low");
		this.logMessage("    Given the fan is on");
		this.logMessage("    And the fan is low");
		this.logMessage("    When the fan is set low");
		this.logMessage("    Then a precondition exception is thrown");
		old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.setLow();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {

		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		// turn off at the end of the tests
		try {
			this.hdop.turnOff();
		} catch (Throwable e) {
			assertTrue(false);
		}
	}

	/**
	 * run all unit tests.
	 * 
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * The tests are run in the following order:
	 * </p>
	 * <ol>
	 * <li>{@code testGetState}</li>
	 * <li>{@code testGetMode}</li>
	 * <li>{@code testTurnOnOff(}</li>
	 * <li>{@code testSetLowHigh}</li>
	 * </ol>
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
	 */
	protected void runAllUnitTests() {
		this.testGetState();
		this.testGetMode();
		this.testTurnOnOff();
		this.testSetLowHigh();

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
			this.doPortConnection(this.hdop.getPortURI(), fanInboundPortURI, FanUserConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception {
		this.traceMessage("Fan Unit Tester begins execution.\n");

		switch (this.getExecutionMode()) {
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI, this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI, this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
			this.statistics = new TestsStatistics();
			this.traceMessage("Fan Unit Tester starts the tests.\n");
			this.runAllUnitTests();
			this.traceMessage("Fan Unit Tester ends.\n");
			break;
		default:
		}
		this.traceMessage("Fan Unit Tester ends execution.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.hdop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.hdop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
