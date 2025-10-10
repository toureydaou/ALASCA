package etape1.equipements.laundry;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
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
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import physical_data.SignalData;
import tests_utils.TestsStatistics;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape1.CVMIntegrationTest;
import etape1.equipements.laundry.connections.connectors.LaundryExternalControlConnector;
import etape1.equipements.laundry.connections.connectors.LaundryUserConnector;
import etape1.equipements.laundry.connections.ports.LaundryExternalControlOutboundPort;
import etape1.equipements.laundry.connections.ports.LaundryUserOutboundPort;
import etape1.equipements.laundry.interfaces.LaundryExternalControlCI;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryUserCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryUnitTester</code> implements a component performing
 * unit tests for the class <code>Laundry</code> as a BCM component.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
 * }
 * invariant	{@code
 * LaundryInternalControlInboundPortURI != null && !LaundryInternalControlInboundPortURI.isEmpty()
 * }
 * invariant	{@code
 * LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty()
 * }
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * X_RELATIVE_POSITION >= 0
 * }
 * invariant	{@code
 * Y_RELATIVE_POSITION >= 0
 * }
 * </pre>
 * 
 * <p>
 * Created on : 2021-09-13
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = { LaundryUserCI.class,
		// LaundryInternalControlCI.class,
		LaundryExternalControlCI.class, ClocksServerCI.class })
public class LaundryUnitTester extends AbstractComponent {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Laundry is switched on.
	 */
	public static final int SWITCH_ON_DELAY = 2;
	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Laundry is switched off.
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
	protected String LaundryUserInboundPortURI;
	/** URI of the internal control component interface inbound port. */
	protected String LaundryInternalControlInboundPortURI;
	/** URI of the external control component interface inbound port. */
	protected String LaundryExternalControlInboundPortURI;

	/** user component interface inbound port. */
	protected LaundryUserOutboundPort hop;
	/** internal control component interface inbound port. */
	// protected LaundryInternalControlOutboundPort hicop;
	/** external control component interface inbound port. */
	protected LaundryExternalControlOutboundPort hecop;

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
	 * ht != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param ht instance to be tested.
	 * @return true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean implementationInvariants(LaundryUnitTester ht) {
		assert ht != null : new PreconditionException("ht != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				ht.LaundryUserInboundPortURI != null && !ht.LaundryUserInboundPortURI.isEmpty(),
				LaundryUnitTester.class, ht,
				"ht.LaundryUserInboundPortURI != null && " + "!ht.LaundryUserInboundPortURI.isEmpty()");

		/*
		 * ret &= AssertionChecking.checkImplementationInvariant(
		 * ht.LaundryExternalControlInboundPortURI != null &&
		 * !ht.LaundryExternalControlInboundPortURI.isEmpty(), LaundryUnitTester.class,
		 * ht, "ht.LaundryExternalControlInboundPortURI != null &&" +
		 * "!ht.LaundryExternalControlInboundPortURI.isEmpty()");
		 */
		return ret;
	}

	/**
	 * return true if the invariants is observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * ht != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param ht instance to be tested.
	 * @return true if the invariants are observed, false otherwise.
	 */
	protected static boolean invariants(LaundryUnitTester ht) {
		assert ht != null : new PreconditionException("ht != null");

		boolean ret = true;
		ret &= AssertionChecking.checkInvariant(X_RELATIVE_POSITION >= 0, LaundryUnitTester.class, ht,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(Y_RELATIVE_POSITION >= 0, LaundryUnitTester.class, ht,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

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
		this(isUnitTest, Laundry.USER_INBOUND_PORT_URI, Laundry.EXTERNAL_CONTROL_INBOUND_PORT_URI);
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
	protected LaundryUnitTester(boolean isUnitTest, String LaundryUserInboundPortURI,
			String LaundryExternalControlInboundPortURI) throws Exception {
		super(1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(LaundryUserInboundPortURI, LaundryExternalControlInboundPortURI);
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
	protected LaundryUnitTester(boolean isUnitTest, String reflectionInboundPortURI, String LaundryUserInboundPortURI,
			String LaundryExternalControlInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(LaundryUserInboundPortURI, LaundryExternalControlInboundPortURI);
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
	protected void initialise(String LaundryUserInboundPortURI, String LaundryExternalControlInboundPortURI)
			throws Exception {
		this.LaundryUserInboundPortURI = LaundryUserInboundPortURI;
		this.hop = new LaundryUserOutboundPort(this);
		this.hop.publishPort();

		this.LaundryExternalControlInboundPortURI = LaundryExternalControlInboundPortURI;
		this.hecop = new LaundryExternalControlOutboundPort(this);
		this.hecop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Laundry tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert LaundryUnitTester.implementationInvariants(this)
				: new ImplementationInvariantException("LaundryTester.implementationInvariants(this)");
		assert LaundryUnitTester.invariants(this) : new InvariantException("LaundryTester.invariants(this)");
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
	 *     Then the state of the Laundry is off
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
			this.hop.turnOn();
			if (this.hop.getState() == LaundryState.ON) {
				this.logMessage("    Then the state of the Laundry is off");
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
	 *     Then the state of the Laundry is off
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
	protected void testDryMode() {
		this.logMessage("Feature: getting the state of the Laundry");
		this.logMessage("  Scenario: getting the mode of the Laundry when it is wash");
		this.logMessage("    Given the Laundry is initialised");
		this.logMessage("    And the Laundry has not been used yet");
		try {
			this.logMessage("    When I test the state of the Laundry");
			this.hop.setDryMode();
			if (this.hop.getLaundryMode() == LaundryMode.DRY) {
				this.logMessage("    Then the mode of the Laundry is dry");
			} else {
				this.logMessage("     but was: wash");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching on and off the Laundry.
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
	 * Feature: switching on and off the Laundry
	 *   Scenario: switching on the Laundry when off
	 *     Given the Laundry is initialised
	 *     And the Laundry has not been used yet
	 *     When I switch on the Laundry
	 *     Then the state of the Laundry is on
	 *   Scenario: switching off the Laundry when on
	 *     Given the Laundry is initialised
	 *     And the Laundry is on
	 *     When I switch off the Laundry
	 *     Then the state of the Laundry is off
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
		this.logMessage("Feature: switching on and off the Laundry");

		this.logMessage("  Scenario: switching on the Laundry when off");
		this.logMessage("    Given the Laundry is initialised");
		this.logMessage("    And the Laundry has not been used yet");

		try {
			this.logMessage("    When I switch on the Laundry");

			this.hop.turnOn();
			if (this.hop.getState() == LaundryState.ON) {
				this.logMessage("    Then the state of the Laundry is on");
			} else {
				this.logMessage("     but was: off");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: switching off the Laundry when on");
		this.logMessage("    Given the Laundry is initialised");
		this.logMessage("    And the Laundry is on");
		try {
			this.logMessage("    When I switch off the Laundry");
			this.hop.turnOff();
			if (this.hop.getState() == LaundryState.OFF) {
				this.logMessage("    Then the state of the Laundry is off");
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
		this.testSwitchOnSwitchOff();
		this.testOff();
		this.testDryMode();
		// this.testTargetTemperature();
		// this.testCurrentTemperature();
		// this.testPowerLevel();

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
			this.doPortConnection(this.hop.getPortURI(), this.LaundryUserInboundPortURI,
					LaundryUserConnector.class.getCanonicalName());
			/*
			 * this.doPortConnection(this.hicop.getPortURI(),
			 * LaundryInternalControlInboundPortURI,
			 * LaundryInternalControlConnector.class.getCanonicalName());
			 */

			this.doPortConnection(this.hecop.getPortURI(), LaundryExternalControlInboundPortURI,
					LaundryExternalControlConnector.class.getCanonicalName());

		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception {
		if (this.isUnitTest) {
			this.runAllUnitTests();
		} else {
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
			Instant LaundrySwitchOn = startInstant.plusSeconds(SWITCH_ON_DELAY);
			Instant LaundrySwitchOff = startInstant.plusSeconds(SWITCH_OFF_DELAY);
			this.traceMessage("Laundry tester waits until start.\n");
			ac.waitUntilStart();
			this.traceMessage("Laundry tester schedules switch on and off.\n");
			long delayToSwitchOn = ac.nanoDelayUntilInstant(LaundrySwitchOn);
			long delayToSwitchOff = ac.nanoDelayUntilInstant(LaundrySwitchOff);

			// This is to avoid mixing the 'this' of the task object with the 'this'
			// representing the component object in the code of the next methods run
			AbstractComponent o = this;

			// schedule the switch on Laundry
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry switches on.\n");
						hop.turnOn();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}, delayToSwitchOn, TimeUnit.NANOSECONDS);

			// to be completed with a more covering scenario

			// schedule the switch off Laundry
			this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						o.traceMessage("Laundry switches off.\n");
						hop.turnOff();
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
		this.doPortDisconnection(this.hop.getPortURI());
		// this.doPortDisconnection(this.hicop.getPortURI());
		this.doPortDisconnection(this.hecop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.hop.unpublishPort();
			// this.hicop.unpublishPort();
			this.hecop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
