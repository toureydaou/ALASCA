package etape1.equipements.kettle;

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
import tests_utils.TestsStatistics;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import etape1.CVMIntegrationTest;
import etape1.equipements.kettle.connections.connectors.KettleExternalControlConnector;
import etape1.equipements.kettle.connections.connectors.KettleUserConnector;
import etape1.equipements.kettle.connections.ports.KettleExternalControlOutboundPort;
import etape1.equipements.kettle.connections.ports.KettleUserOutboundPort;
import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape1.equipements.kettle.interfaces.KettleUserCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleUnitTester</code> implements a component performing
 * unit tests for the class <code>Kettle</code> as a BCM component.
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
 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty()
 * }
 * invariant	{@code
 * KettleInternalControlInboundPortURI != null && !KettleInternalControlInboundPortURI.isEmpty()
 * }
 * invariant	{@code
 * KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty()
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
@RequiredInterfaces(required = { KettleUserCI.class,
		// KettleInternalControlCI.class,
		KettleExternalControlCI.class, ClocksServerCI.class })
public class KettleUnitTester extends AbstractComponent {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Kettle is switched on.
	 */
	public static final int SWITCH_ON_DELAY = 2;
	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Kettle is switched off.
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
	protected String KettleUserInboundPortURI;
	/** URI of the internal control component interface inbound port. */
	protected String KettleInternalControlInboundPortURI;
	/** URI of the external control component interface inbound port. */
	protected String KettleExternalControlInboundPortURI;

	/** user component interface inbound port. */
	protected KettleUserOutboundPort hop;
	/** internal control component interface inbound port. */
	// protected KettleInternalControlOutboundPort hicop;
	/** external control component interface inbound port. */
	protected KettleExternalControlOutboundPort hecop;

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
	protected static boolean implementationInvariants(KettleUnitTester ht) {
		assert ht != null : new PreconditionException("ht != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				ht.KettleUserInboundPortURI != null && !ht.KettleUserInboundPortURI.isEmpty(),
				KettleUnitTester.class, ht,
				"ht.KettleUserInboundPortURI != null && " + "!ht.KettleUserInboundPortURI.isEmpty()");

		/*
		 * ret &= AssertionChecking.checkImplementationInvariant(
		 * ht.KettleExternalControlInboundPortURI != null &&
		 * !ht.KettleExternalControlInboundPortURI.isEmpty(), KettleUnitTester.class,
		 * ht, "ht.KettleExternalControlInboundPortURI != null &&" +
		 * "!ht.KettleExternalControlInboundPortURI.isEmpty()");
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
	protected static boolean invariants(KettleUnitTester ht) {
		assert ht != null : new PreconditionException("ht != null");

		boolean ret = true;
		ret &= AssertionChecking.checkInvariant(X_RELATIVE_POSITION >= 0, KettleUnitTester.class, ht,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(Y_RELATIVE_POSITION >= 0, KettleUnitTester.class, ht,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a Kettle test component.
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
	protected KettleUnitTester(boolean isUnitTest) throws Exception {
		this(isUnitTest, Kettle.USER_INBOUND_PORT_URI, Kettle.EXTERNAL_CONTROL_INBOUND_PORT_URI);
	}

	/**
	 * create a Kettle test component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleInternalControlInboundPortURI != null && !KettleInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty()
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
	 * @param KettleUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @param KettleInternalControlInboundPortURI URI of the internal control
	 *                                             component interface inbound port.
	 * @param KettleExternalControlInboundPortURI URI of the external control
	 *                                             component interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected KettleUnitTester(boolean isUnitTest, String KettleUserInboundPortURI,
			String KettleExternalControlInboundPortURI) throws Exception {
		super(1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(KettleUserInboundPortURI, KettleExternalControlInboundPortURI);
	}

	/**
	 * create a Kettle test component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleInternalControlInboundPortURI != null && !KettleInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty()
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
	 * @param KettleUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @param KettleInternalControlInboundPortURI URI of the internal control
	 *                                             component interface inbound port.
	 * @param KettleExternalControlInboundPortURI URI of the external control
	 *                                             component interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected KettleUnitTester(boolean isUnitTest, String reflectionInboundPortURI, String KettleUserInboundPortURI,
			String KettleExternalControlInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.isUnitTest = isUnitTest;
		this.initialise(KettleUserInboundPortURI, KettleExternalControlInboundPortURI);
	}

	/**
	 * initialise a Kettle test component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleInternalControlInboundPortURI != null && !KettleInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param KettleUserInboundPortURI            URI of the user component
	 *                                             interface inbound port.
	 * @param KettleInternalControlInboundPortURI URI of the internal control
	 *                                             component interface inbound port.
	 * @param KettleExternalControlInboundPortURI URI of the external control
	 *                                             component interface inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected void initialise(String KettleUserInboundPortURI, String KettleExternalControlInboundPortURI)
			throws Exception {
		this.KettleUserInboundPortURI = KettleUserInboundPortURI;
		this.hop = new KettleUserOutboundPort(this);
		this.hop.publishPort();

		this.KettleExternalControlInboundPortURI = KettleExternalControlInboundPortURI;
		this.hecop = new KettleExternalControlOutboundPort(this);
		this.hecop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Kettle tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert KettleUnitTester.implementationInvariants(this)
				: new ImplementationInvariantException("KettleTester.implementationInvariants(this)");
		assert KettleUnitTester.invariants(this) : new InvariantException("KettleTester.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * test getting the state of the Kettle.
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
	 * Feature: getting the state of the Kettle
	 *   Scenario: getting the state of the Kettle when off
	 *     Given the Kettle is initialised
	 *     And the Kettle has not been used yet
	 *     When I test the state of the Kettle
	 *     Then the state of the Kettle is off
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
		this.logMessage("Feature: getting the state of the Kettle");
		this.logMessage("  Scenario: getting the state of the Kettle when off");
		this.logMessage("    Given the Kettle is initialised");
		this.logMessage("    And the Kettle has not been used yet");
		try {
			this.logMessage("    When I test the state of the Kettle");
			this.hop.turnOn();
			if (this.hop.getState() == KettleState.ON) {
				this.logMessage("    Then the state of the Kettle is off");
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
	 * test getting the state of the Kettle.
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
	 * Feature: getting the state of the Kettle
	 *   Scenario: getting the state of the Kettle when off
	 *     Given the Kettle is initialised
	 *     And the Kettle has not been used yet
	 *     When I test the state of the Kettle
	 *     Then the state of the Kettle is off
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
	protected void testPartialMode() {
		this.logMessage("Feature: getting the state of the Kettle");
		this.logMessage("  Scenario: getting the mode of the Kettle when it is Total");
		this.logMessage("    Given the Kettle is initialised");
		this.logMessage("    And the Kettle has not been used yet");
		try {
			this.logMessage("    When I test the state of the Kettle");
			this.hop.setPartialMode();
			if (this.hop.getKettleMode() == KettleMode.PARTIAL) {
				this.logMessage("    Then the mode of the Kettle is Partial");
			} else {
				this.logMessage("     but was: total");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching on and off the Kettle.
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
	 * Feature: switching on and off the Kettle
	 *   Scenario: switching on the Kettle when off
	 *     Given the Kettle is initialised
	 *     And the Kettle has not been used yet
	 *     When I switch on the Kettle
	 *     Then the state of the Kettle is on
	 *   Scenario: switching off the Kettle when on
	 *     Given the Kettle is initialised
	 *     And the Kettle is on
	 *     When I switch off the Kettle
	 *     Then the state of the Kettle is off
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
		this.logMessage("Feature: switching on and off the Kettle");

		this.logMessage("  Scenario: switching on the Kettle when off");
		this.logMessage("    Given the Kettle is initialised");
		this.logMessage("    And the Kettle has not been used yet");

		try {
			this.logMessage("    When I switch on the Kettle");

			this.hop.turnOn();
			if (this.hop.getState() == KettleState.ON) {
				this.logMessage("    Then the state of the Kettle is on");
			} else {
				this.logMessage("     but was: off");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: switching off the Kettle when on");
		this.logMessage("    Given the Kettle is initialised");
		this.logMessage("    And the Kettle is on");
		try {
			this.logMessage("    When I switch off the Kettle");
			this.hop.turnOff();
			if (this.hop.getState() == KettleState.OFF) {
				this.logMessage("    Then the state of the Kettle is off");
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
		this.testPartialMode();
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
			this.doPortConnection(this.hop.getPortURI(), this.KettleUserInboundPortURI,
					KettleUserConnector.class.getCanonicalName());
			/*
			 * this.doPortConnection(this.hicop.getPortURI(),
			 * KettleInternalControlInboundPortURI,
			 * KettleInternalControlConnector.class.getCanonicalName());
			 */

			this.doPortConnection(this.hecop.getPortURI(), KettleExternalControlInboundPortURI,
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
		if (this.isUnitTest) {
			this.runAllUnitTests();
		} else {
			ClocksServerOutboundPort clocksServerOutboundPort = new ClocksServerOutboundPort(this);
			clocksServerOutboundPort.publishPort();
			this.doPortConnection(clocksServerOutboundPort.getPortURI(), ClocksServer.STANDARD_INBOUNDPORT_URI,
					ClocksServerConnector.class.getCanonicalName());
			this.traceMessage("Kettle tester gets the clock.\n");
			AcceleratedClock ac = clocksServerOutboundPort.getClock(CVMIntegrationTest.CLOCK_URI);
			this.doPortDisconnection(clocksServerOutboundPort.getPortURI());
			clocksServerOutboundPort.unpublishPort();
			clocksServerOutboundPort = null;

			Instant startInstant = ac.getStartInstant();
			Instant KettleSwitchOn = startInstant.plusSeconds(SWITCH_ON_DELAY);
			Instant KettleSwitchOff = startInstant.plusSeconds(SWITCH_OFF_DELAY);
			this.traceMessage("Kettle tester waits until start.\n");
			ac.waitUntilStart();
			this.traceMessage("Kettle tester schedules switch on and off.\n");
			long delayToSwitchOn = ac.nanoDelayUntilInstant(KettleSwitchOn);
			long delayToSwitchOff = ac.nanoDelayUntilInstant(KettleSwitchOff);

			// This is to avoid mixing the 'this' of the task object with the 'this'
			// representing the component object in the code of the next methods run
			AbstractComponent o = this;

			// schedule the switch on Kettle
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

			// to be completed with a more covering scenario

			// schedule the switch off Kettle
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
