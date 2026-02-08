package etape4.equipments.batteries;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
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
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import etape1.equipments.batteries.Batteries;
import etape1.equipments.batteries.BatteriesCI;
import etape1.equipments.batteries.connections.BatteriesConnector;
import etape1.equipments.batteries.connections.BatteriesOutboundPort;
import etape2.equipments.batteries.mil.BatteriesSimulationConfiguration;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestsStatistics;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesCyPhyUnitTester</code> implements a tester component
 * for the component {@code Batteries}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-09-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@RequiredInterfaces(required = {BatteriesCI.class})
// -----------------------------------------------------------------------------
public class			BatteriesCyPhyUnitTester
extends		AbstractCyPhyComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** standard reflection, inbound port URI for the
	 *  {@code BatteriesCyPhyUnitTester} component.							*/
	public static final String	REFLECTION_INBOUND_PORT_URI =
											"batteries-unit-tester-RIP-URI";

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** URI of the inbound port of the {@code Batteries} component under
	 *  test.																*/
	protected String					inboundPortURI;
	/** outbound port connected to the {@code Batteries} component under
	 *  test.																*/
	protected BatteriesOutboundPort		outboundPort;

	/** collector of test statistics.										*/
	protected TestsStatistics			statistics;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @param inboundPortURI	URI of the inbound port of the {@code Batteries} component under test.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			BatteriesCyPhyUnitTester(String inboundPortURI)
	throws Exception
	{
		super(1, 0);

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		this.inboundPortURI = inboundPortURI;
		this.outboundPort = new BatteriesOutboundPort(this);
		this.outboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Batteries tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// Test execution with test scenario

	/**
	 * create a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code testScenario != null}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param inboundPortURI	URI of the inbound port of the {@code BatteriesCyPhy} component under test.
	 * @param executionMode		execution mode for the next run.
	 * @param testScenario		test scenario to be executed.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			BatteriesCyPhyUnitTester(
		String inboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  1,
			  1,
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

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		this.inboundPortURI = inboundPortURI;
		this.outboundPort = new BatteriesOutboundPort(this);
		this.outboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Batteries tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * testing the total capacity retrieval methods.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * 
	 * <pre>
	 * Feature: getting the batteries total capacities
	 * 
	 *   Scenario: getting the nominal capacity
	 *     Given a standard batteries with one batteries unit
	 *     When I ask for the nominal capacity
	 *     Then I get the standard nominal capacity for one unit
	 * 
	 *   Scenario: getting the current capacity
	 *     Given a batteries
	 *     When I ask for the current capacity
	 *     Then I get the test value for current capacity
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code outboundPort != null && outboundPort.connected()}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param outboundPort	outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 * @throws Exception 	<i>to do</i>.
	 */
	protected static void	testTotalCapacities(
		AbstractComponent tester,
		BatteriesOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: getting the batteries total capacities");
		tester.logMessage("  Scenario: getting the nominal capacity");
		tester.logMessage("    Given a batteries with one batteries unit");
		tester.logMessage("    When I ask for the nominal capacity");
		tester.logMessage("    Then I get the standard nominal capacity for one unit");
		Measure<Double> result = null;
		try {
			result = outboundPort.nominalCapacity();
			if (result == null ||
					!result.equals(BatteriesSimulationConfiguration.
														NOMINAL_CAPACITY)) {
				tester.logMessage("      but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the current capacity");
		tester.logMessage("    Given a batteries");
		tester.logMessage("    When I ask for the current capacity");
		tester.logMessage("    Then I get the test value for current capacity");
		SignalData<Double> sResult = null;
		try {
			sResult = outboundPort.currentCapacity();
			if (sResult == null ||
					!sResult.getMeasure().equals(
						BatteriesSimulationConfiguration.NOMINAL_CAPACITY)) {
				tester.logMessage("      but was: " + sResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * testing the total capacity retrieval methods.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: getting the batteries charging state at beginning
	 * 
	 *   Scenario: getting the charging state when just initialised
	 *     Given a standard Batteries just initialised component
	 *     When I ask for the charging state
	 *     Then I get false
	 * 
	 *   Scenario: getting the discharging state when just initialised
	 *     Given a standard Batteries just initialised component
	 *     When I ask for the discharging state
	 *     Then I get false
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code outboundPort != null && outboundPort.connected()}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param outboundPort	outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 * @throws Exception 	<i>to do</i>.
	 */
	protected static void	testChargingStateAtBeginning(
		AbstractComponent tester,
		BatteriesOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: getting the batteries charging state at beginning");
		tester.logMessage("  Scenario: getting the charging state when just initialised");
		tester.logMessage("    Given a standard Batteries just initialised component");
		tester.logMessage("    When I ask for the charging state");
		tester.logMessage("    Then I get false");
		boolean result;
		try {
			result = outboundPort.areCharging();
			if (result) {
				tester.logMessage("      but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the discharging state when just initialised");
		tester.logMessage("    Given a standard Batteries just initialised component");
		tester.logMessage("    When I ask for the discharging state");
		tester.logMessage("    Then I get false");
		try {
			result = outboundPort.areDischarging();
			if (result) {
				tester.logMessage("      but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * testing the batteries charge level at beginning.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: getting the batteries charge level at beginning
	 * 
	 *   Scenario: getting the charge level when just initialised");
	 *     Given a standard Batteries just initialised component");
	 *     When I ask for the chargevel");
	 *     Then I get the test value for charge level");
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code outboundPort != null && outboundPort.connected()}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param outboundPort	outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 * @throws Exception 	<i>to do</i>.
	 */
	protected static void	testChargingLevelAtBeginning(
		AbstractComponent tester,
		BatteriesOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: getting the batteries charge level at beginning");
		tester.logMessage("  Scenario: getting the charge level when just initialised");
		tester.logMessage("    Given a standard Batteries just initialised component");
		tester.logMessage("    When I ask for the chargevel");
		tester.logMessage("    Then I get the test value for charge level");
		SignalData<Double> sResult = null;
		try {
			sResult = outboundPort.chargeLevel();
			if (sResult == null ||
					!sResult.getMeasure().getData().equals(
								BatteriesSimulationConfiguration.
											INITIAL_BATTERIES_LEVEL_RATIO)) {
				tester.logMessage("      but was: " + sResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * testing the batteries charge level at beginning.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: testing start and stop charging
	 * 
	 * Feature: testing start and stop charging
	 * 
	 *   Scenario: stop charging when not charging
	 *     Given a batteries not charging
	 *     When I stop charging
	 *     Then a precondition exception is thrown
	 * 
	 *   Scenario: start charging when not charging
	 *     Given a batteries not charging
	 *     And a batteries not full
	 *     When I start charging
	 *     Then the batteries are charging
	 * 
	 *   Scenario: start charging when charging
	 *     Given a batteries charging
	 *     When I start charging
	 *     Then a precondition exception is thrown
	 * 
	 *   Scenario: getting the power consumption when charging for a standard settings
	 *     When the batteries are charging
	 *     When I get the power consumption of the batteries
	 *     Then I get the input power consumption for the batteries
	 * 
	 *   Scenario: stop charging when charging
	 *     Given a batteries charging
	 *     When I stop charging
	 *     Then the batteries are not charging
	 * 
	 *   Scenario: getting the power consumption when not charging
	 *     When batteries are not charging
	 *     When I get the power consumption of the batteries
	 *     Then I get a 0 input power consumption
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code outboundPort != null && outboundPort.connected()}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param outboundPort	outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 * @throws Exception 	<i>to do</i>.
	 */
	protected static void	testStartAndStopCharging(
		AbstractComponent tester,
		BatteriesOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: testing start and stop charging");
		tester.logMessage("  Scenario: stop charging when not charging");
		tester.logMessage("    Given a batteries not charging");
		boolean bResult = true;
		try {
			bResult = outboundPort.areCharging();
			if (bResult) {
				tester.logMessage("     but was: " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I stop charging");
		tester.logMessage("    Then a precondition exception is thrown");
		boolean old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			outboundPort.stopCharging();
			tester.logMessage("      but it was not!");
			statistics.incorrectResult();
		} catch (Throwable e) {
		} finally {
			BCMException.VERBOSE = old;
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: start charging when not charging");
		tester.logMessage("    Given a batteries not charging");
		bResult = true;
		try {
			bResult = outboundPort.areCharging();
			if (bResult) {
				tester.logMessage("     but was: " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    And a batteries not full");
		SignalData<Double> sResult = null;
		try {
			sResult = outboundPort.chargeLevel();
			if (sResult == null || sResult.getMeasure().getData() == 1.0) {
				tester.logMessage("     but was: " + sResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I start charging");
		tester.logMessage("    Then the batteries are charging");
		try {
			outboundPort.startCharging();
		} catch (Throwable e) {
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			bResult = outboundPort.areCharging();
			if (!bResult) {
				tester.logMessage("     but was: " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: start charging when charging");
		tester.logMessage("    Given a batteries charging");
		bResult = false;
		try {
			bResult = outboundPort.areCharging();
			if (!bResult) {
				tester.logMessage("     but was: " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I start charging");
		tester.logMessage("    Then a precondition exception is thrown");
		old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			outboundPort.startCharging();
			tester.logMessage("      but it was not!");
			statistics.incorrectResult();
		} catch (Throwable e) {
		} finally {
			BCMException.VERBOSE = old;
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the power consumption when charging for a standard settings");
		tester.logMessage("    When the batteries are charging");
		tester.logMessage("    When I get the power consumption of the batteries");
		tester.logMessage("    Then I get the input power consumption for the batteries");
		try {
			sResult = outboundPort.getCurrentPowerConsumption();
			if (!sResult.getMeasure().equals(
							BatteriesSimulationConfiguration.TOTAL_IN_POWER)) {
				tester.logMessage("     but was: " + sResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: stop charging when charging");
		tester.logMessage("    Given a batteries charging");
		try {
			bResult = outboundPort.areCharging();
			if (!bResult) {
				tester.logMessage("     but was: " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I stop charging");
		tester.logMessage("    Then the batteries are not charging");
		try {
			outboundPort.stopCharging();
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			bResult = outboundPort.areCharging();
			if (bResult) {
				tester.logMessage("     but was: " + bResult);
				statistics.incorrectResult();;
			}
		} catch (Throwable e) {
			statistics.incorrectResult();;
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the power consumption when not charging");
		tester.logMessage("    When batteries are not charging");
		try {
			bResult = outboundPort.areCharging();
			if (bResult) {
				tester.logMessage("     but was: " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I get the power consumption of the batteries");
		tester.logMessage("    Then I get a 0 input power consumption");
		try {
			sResult = outboundPort.getCurrentPowerConsumption();
			if (!sResult.getMeasure().equals(
					new Measure<Double>(
							0.0,
							Batteries.IN_POWER_PER_CELL.getMeasurementUnit()))) {
				tester.logMessage("     but was: " + sResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * run all tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code outboundPort != null && outboundPort.connected()}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param outboundPort	outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 * @throws Exception 	<i>to do</i>.
	 */
	public static void		runAllTests(
		AbstractComponent tester,
		BatteriesOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		testTotalCapacities(tester, outboundPort, statistics);
		testChargingStateAtBeginning(tester, outboundPort, statistics);
		testChargingLevelAtBeginning(tester, outboundPort, statistics);
		testStartAndStopCharging(tester, outboundPort, statistics);

		statistics.statisticsReport(tester);
	}

	// -------------------------------------------------------------------------
	// Test methods
	// -------------------------------------------------------------------------

	protected void	startCharging() throws Exception
	{
		this.logMessage("start charging.");
		this.outboundPort.startCharging();
	}

	protected void	stopCharging() throws Exception
	{
		this.logMessage("stop charging.");
		this.outboundPort.stopCharging();
	}

	protected boolean	areCharging() throws Exception
	{
		boolean ret = this.outboundPort.areCharging();
		this.logMessage("areCharging returns " + ret + ".");
		return ret;
	}

	protected boolean	areDischarging() throws Exception
	{
		boolean ret = this.outboundPort.areDischarging();
		this.logMessage("areDischarging returns " + ret + ".");
		return ret;
	}

	protected SignalData<Double>	chargeLevel() throws Exception
	{
		SignalData<Double> ret = this.outboundPort.chargeLevel();
		this.logMessage("chargeLevel returns " + ret + ".");
		return ret;
	}

	protected SignalData<Double>	getCurrentPowerConsumption()
	throws Exception
	{
		SignalData<Double> ret = this.outboundPort.getCurrentPowerConsumption();
		this.logMessage("getCurrentPowerConsumption returns " + ret + ".");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
					this.outboundPort.getPortURI(),
					this.inboundPortURI,
					BatteriesConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.traceMessage("BatteriesCyPhy Unit Tester begins execution.\n");

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
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
			this.statistics = new TestsStatistics();
			this.traceMessage("BatteriesCyPhy Unit Tester starts the tests.\n");
			runAllTests(this, this.outboundPort, this.statistics);
			this.traceMessage("BatteriesCyPhy Unit Tester ends.\n");
			break;
		default:
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.outboundPort.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.outboundPort.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
