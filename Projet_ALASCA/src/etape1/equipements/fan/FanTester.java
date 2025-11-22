package etape1.equipements.fan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import etape1.CVMIntegrationTest;
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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import tests_utils.TestsStatistics;

// -----------------------------------------------------------------------------
/**
 * The class <code>FanTester</code> implements a component performing
 * tests for the class <code>Fan</code> as a BCM4Java component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code FanInboundPortURI != null && !FanInboundPortURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {FanUserCI.class, ClocksServerCI.class})
public class			FanTester
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/* when true, the component performs a unit test.						*/
	protected final boolean				isUnitTest;
	/* outbound port connecting to the fan component.				*/
	protected FanOutboundPort		fop;
	/* URI of the fan inbound port to connect to.					*/
	protected String					FanInboundPortURI;

	/** collector of test statistics.										*/
	protected TestsStatistics			statistics;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hdt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hdt	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(FanTester hdt)
	{
		assert	hdt != null : new PreconditionException("hdt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				hdt.FanInboundPortURI != null &&
										!hdt.FanInboundPortURI.isEmpty(),
				FanTester.class, hdt,
				"hdt.FanInboundPortURI != null && "
								+ "!hdt.FanInboundPortURI.isEmpty()");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hdt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hdt	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(FanTester hdt)
	{
		assert	hdt != null : new PreconditionException("hdt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkInvariant(
				X_RELATIVE_POSITION >= 0,
				FanTester.class, hdt,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(
				Y_RELATIVE_POSITION >= 0,
				FanTester.class, hdt,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a fan tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param isUnitTest	when true, the component performs a unit test.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			FanTester(boolean isUnitTest) throws Exception
	{
		this(isUnitTest, Fan.INBOUND_PORT_URI);
	}

	/**
	 * create a fan tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code FanInboundPortURI != null && !FanInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param isUnitTest				when true, the component performs a unit test.
	 * @param FanInboundPortURI	URI of the fan inbound port to connect to.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			FanTester(
		boolean isUnitTest,
		String FanInboundPortURI
		) throws Exception
	{
		super(1, 0);

		assert	FanInboundPortURI != null &&
										!FanInboundPortURI.isEmpty() :
				new PreconditionException(
						"FanInboundPortURI != null && "
						+ "!FanInboundPortURI.isEmpty()");

		this.isUnitTest = isUnitTest;
		this.initialise(FanInboundPortURI);
	}

	/**
	 * create a fan tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code FanInboundPortURI != null && !FanInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param isUnitTest				when true, the component performs a unit test.
	 * @param FanInboundPortURI	URI of the fan inbound port to connect to.
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			FanTester(
		boolean isUnitTest,
		String FanInboundPortURI,
		String reflectionInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);

		this.isUnitTest = isUnitTest;
		this.initialise(FanInboundPortURI);
	}

	/**
	 * initialise a fan tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code FanInboundPortURI != null && !FanInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param FanInboundPortURI	URI of the fan inbound port to connect to.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		initialise(
		String FanInboundPortURI
		) throws Exception
	{
		this.FanInboundPortURI = FanInboundPortURI;
		this.fop = new FanOutboundPort(this);
		this.fop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Fan tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert	implementationInvariants(this) :
				new ImplementationInvariantException(
						"FanTester.implementationInvariants(this)");
		assert	invariants(this) :
				new InvariantException("FanTester.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * test of the {@code getState} method when the fan is off.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: Getting the state of the fan
	 * 
	 *   Scenario: getting the state when off
	 *     Given the fan is initialised and never been used yet
	 *     When the fan has not been used yet
	 *     Then the fan is off
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
	public void			testGetState()
	{
		this.logMessage("Feature: Getting the state of the fan");
		this.logMessage("  Scenario: getting the state when off");
		this.logMessage("    Given the fan is initialised");
		this.logMessage("    And the fan has not been used yet");
		FanState result = null;
		try {
			this.logMessage("    When I test the state of the fan");
			result = this.fop.getState();
			this.logMessage("    Then the state of the fan is off");
			if (!FanState.OFF.equals(result)) {
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
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: Getting the mode of the fan
	 * 
	 *   Scenario: getting the mode when off
	 *     Given the fan is initialised and never been used yet
	 *     When the method getMode is called
	 *     Then the result is that the fan is in low mode
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
	public void			testGetMode()
	{
		this.logMessage("Feature: Getting the mode of the fan");
		this.logMessage("  Scenario: getting the mode when off");
		this.logMessage("    Given the fan is initialised");
		FanMode result = null;
		try {
			this.logMessage("    When the fan has not been used yet");
			result = this.fop.getMode();
			this.logMessage("    Then the fan is low");
			if (!FanMode.LOW.equals(result)) {
				this.logMessage("     but was: " + result);	
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
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
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
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			testTurnOnOff()
	{
		this.logMessage("Feature: turning the fan on and off");
		this.logMessage("  Scenario: turning on when off");
		FanState resultState = null;
		FanMode resultMode = null;
		try {
			this.logMessage("    Given the fan is off");
			resultState = this.fop.getState();
			if (!FanState.OFF.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
			this.logMessage("    When the fan is turned on");
			this.fop.turnOn();
			this.logMessage("    Then the fan is on");
			resultState = this.fop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
			this.logMessage("    And the fan is in mode low");
			resultMode = this.fop.getMode();
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
			resultState = this.fop.getState();
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
			this.fop.turnOn();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch(Throwable e) {
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning off when on");
		this.logMessage("    Given the fan is on");
		try {
			resultState = this.fop.getState();
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
			this.fop.turnOff();
			this.logMessage("    Then the fan is off");
			resultState = this.fop.getState();
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
			resultState = this.fop.getState();
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
			this.fop.turnOff();
			this.logMessage("     but the precondition exception was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching mode of the fan.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
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
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			testSetLowHigh()
	{
		this.logMessage("Feature: switching the fan low and high.");
		this.logMessage("  Scenario: set the fan high from low");
		this.logMessage("    Given the fan is on");
		FanState resultState = null;
		FanMode resultMode = null;
		try {
			this.fop.turnOn();
			resultState = this.fop.getState();
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
			resultMode = this.fop.getMode();
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
			this.fop.setHigh();
			resultState = this.fop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
			
		}
		try {
			this.logMessage("    And  the fan is high");
			resultMode = this.fop.getMode();
			if (!FanMode.HIGH.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
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
			this.fop.setHigh();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: set the fan low from high");
		this.logMessage("    Given the fan is on");
		this.logMessage("    And the fan is high");
		this.logMessage("    When the fan is set low");
		try {
			this.fop.setLow();
			this.logMessage("    Then the fan is on");
			resultState = this.fop.getState();
			if (!FanState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
			
		}
		try {
			this.logMessage("    And the fan is low");
			resultMode = this.fop.getMode();
			if (!FanMode.LOW.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
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
			this.fop.setLow();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		// turn off at the end of the tests
		try {
			this.fop.turnOff();
		} catch (Throwable e) {
			assertTrue(false);
		}
	}

	/**
	 * run all unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>The tests are run in the following order:</p>
	 * <ol>
	 * <li>{@code testGetState}</li>
	 * <li>{@code testGetMode}</li>
	 * <li>{@code testTurnOnOff(}</li>
	 * <li>{@code testSetLowHigh}</li>
	 * </ol>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 */
	protected void			runAllUnitTests()
	{
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
	public synchronized void	start()
	throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
							this.fop.getPortURI(),
							FanInboundPortURI,
							FanUserConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception
	{
		if (!this.isUnitTest) {
			ClocksServerOutboundPort clocksServerOutboundPort =
											new ClocksServerOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(
					clocksServerOutboundPort.getPortURI(),
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			this.traceMessage("Fan Tester gets the clock.\n");
			AcceleratedClock ac =
					clocksServerOutboundPort.getClock(
										CVMIntegrationTest.CLOCK_URI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort = null;

			this.traceMessage("Fan Tester waits until start.\n");
			ac.waitUntilStart();
		}
		this.traceMessage("Fan Tester starts the tests.\n");
		this.runAllUnitTests();
		this.traceMessage("Fan Tester ends.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.fop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.fop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
