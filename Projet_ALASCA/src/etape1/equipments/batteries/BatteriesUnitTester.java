package etape1.equipments.batteries;

import etape1.equipments.batteries.connections.BatteriesConnector;
import etape1.equipments.batteries.connections.BatteriesOutboundPort;

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
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;
import physical_data.SignalData;
import tests_utils.TestsStatistics;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesUnitTester</code> implements a tester component for
 * the component {@code Batteries}.
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
@RequiredInterfaces(required = {BatteriesCI.class})
public class			BatteriesUnitTester
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

	/** URI of the inbound port of the {@code Batteries} component under
	 *  test.																*/
	protected String				inboundPortURI;
	/** outbound port connected to the {@code Batteries} component under
	 *  test.																*/
	protected BatteriesOutboundPort	outboundPort;

	/** collector of test statistics.										*/
	protected TestsStatistics			statistics;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inboundPortURI	URI of the inbound port of the {@code Batteries} component under test.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			BatteriesUnitTester(String inboundPortURI) throws Exception
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

		this.statistics = new TestsStatistics();
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
			if (result == null || !result.equals(Batteries.CAPACITY_PER_UNIT)) {
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
			if (sResult == null || !sResult.getMeasure().equals(
											Batteries.FAKE_CURRENT_CAPACITY)) {
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
			if (sResult == null || !sResult.getMeasure().equals(Batteries.FAKE_CHARGE_LEVEL)) {
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
	 *     When one unit of batteries are charging
	 *     When I get the power consumption of the batteries
	 *     Then I get the input power consumption for one unit of batteries
	 * 
	 *   Scenario: stop charging when charging
	 *     Given a batteries charging
	 *     When I stop charging
	 *     Then the batteries are not charging
	 * 
	 *   Scenario: getting the power consumption when not charging
	 *     When batteries are not charging
	 *     When I get the power consumption of the batteries
	 *     Then a precondition exception is thrown
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
		tester.logMessage("    When one unit of batteries are charging");
		tester.logMessage("    When I get the power consumption of the batteries");
		tester.logMessage("    Then I get the input power consumption for one unit of batteries");
		try {
			sResult = outboundPort.getCurrentPowerConsumption();
			if (!sResult.getMeasure().equals(Batteries.IN_POWER_PER_CELL)) {
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
		tester.logMessage("    Then a precondition exception is thrown");
		old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			outboundPort.getCurrentPowerConsumption();
			tester.logMessage("      but it was not!");
			statistics.incorrectResult();
		} catch (Throwable e) {
		} finally {
			BCMException.VERBOSE = old;
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
		runAllTests(this, this.outboundPort, this.statistics);
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
