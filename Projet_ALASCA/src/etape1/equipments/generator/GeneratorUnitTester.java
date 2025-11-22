package etape1.equipments.generator;

import etape1.equipments.generator.Generator.State;
import etape1.equipments.generator.connections.GeneratorConnector;
import etape1.equipments.generator.connections.GeneratorOutboundPort;

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
 * The class <code>GeneratorUnitTester</code> implements a unit tester component
 * for the {@code Generator} component.
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
 * <p>Created on : 2025-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {GeneratorCI.class})
public class			GeneratorUnitTester
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean			VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int				X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int				Y_RELATIVE_POSITION = 0;

	/** URI of the inbound port of the {@code Batteries} component under
	 *  test.																*/
	protected String				inboundPortURI;
	/** outbound port connected to the {@code Batteries} component under
	 *  test.																*/
	protected GeneratorOutboundPort	outboundPort;

	/** collector of test statistics.										*/
	protected TestsStatistics		statistics;

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
	protected			GeneratorUnitTester(String inboundPortURI)
	throws Exception
	{
		super(1, 0);

		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");

		this.inboundPortURI = inboundPortURI;
		this.outboundPort = new GeneratorOutboundPort(this);
		this.outboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Generator tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();
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
					GeneratorConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void				execute() throws Exception
	{
		runAllTests(this, this.outboundPort, new TestsStatistics());
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

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * testing the static properties.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: static properties of the generator
	 * 
	 *   Scenario: getting the state when off
	 *     Given a standard generator just created
	 *     When I ask for its state
	 *     Then I get that it is off
	 * 
	 *   Scenario: getting the nominal tension
	 *     Given a standard generator
	 *     When I ask for the nominal tension
	 *     Then I get the the nominal tension for a standard generator
	 * 
	 *   Scenario: getting the tank capacity
	 *     Given a standard generator
	 *     When I ask for the tank capacity
	 *     Then I get the the tank capacity for a standard generator
	 * 
	 *   Scenario: getting the maximum power production
	 *     Given a standard generator
	 *     When I ask for the maximum power production
	 *     Then I get the the maximum power production for a standard generator
	 * 
	 *   Scenario: getting the minimum fuel consumption
	 *     Given a standard generator
	 *     When I ask for the minimum fuel consumption
	 *     Then I get the the minimum fuel consumption for a standard generator
	 * 
	 *   Scenario: getting the maximum fuel consumption
	 *     Given a standard generator
	 *     When I ask for the maximum fuel consumption
	 *     Then I get the the maximum fuel consumption for a standard generator
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
	protected static void	testStaticProperties(
		AbstractComponent tester,
		GeneratorOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		tester.logMessage("Feature: static properties of the generator");
		tester.logMessage("  Scenario: getting the state when off");
		tester.logMessage("    Given the generator just created");
		tester.logMessage("    When I ask for its state");
		tester.logMessage("    Then I get that it is off");
		State bResult;
		try {
			bResult = outboundPort.getState();
			if (!State.OFF.equals(bResult)) {
				tester.logMessage("        but was " + bResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the nominal tension");
		tester.logMessage("    Given a standard generator");
		tester.logMessage("    When I ask for the nominal tension");
		tester.logMessage("    Then I get the the nominal tension for a standard generator");
		Measure<Double> result = null;
		try {
			result = outboundPort.nominalOutputTension();
			if (result == null || !result.equals(Generator.OUTPUT_AC_TENSION)) {
				tester.logMessage("        but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the tank capacity");
		tester.logMessage("    Given a standard generator");
		tester.logMessage("    When I ask for the tank capacity");
		tester.logMessage("    Then I get the the tank capacity for a standard generator");
		result = null;
		try {
			result = outboundPort.tankCapacity();
			if (result == null || !result.equals(Generator.TANK_CAPACITY)) {
				tester.logMessage("        but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the maximum power production");
		tester.logMessage("    Given a standard generator");
		tester.logMessage("    When I ask for the maximum power production");
		tester.logMessage("    Then I get the the maximum power production for a standard generator");
		result = null;
		try {
			result = outboundPort.maxPowerProductionCapacity();
			if (result == null || !result.equals(Generator.MAX_POWER)) {
				tester.logMessage("        but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the minimum fuel consumption");
		tester.logMessage("    Given a standard generator");
		tester.logMessage("    When I ask for the minimum fuel consumption");
		tester.logMessage("    Then I get the the minimum fuel consumption for a standard generator");
		result = null;
		try {
			result = outboundPort.minFuelConsumption();
			if (result == null || !result.equals(Generator.MIN_FUEL_CONSUMPTION)) {
				tester.logMessage("        but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the maximum fuel consumption");
		tester.logMessage("    Given a standard generator");
		tester.logMessage("    When I ask for the maximum fuel consumption");
		tester.logMessage("    Then I get the the maximum fuel consumption for a standard generator");
		result = null;
		try {
			result = outboundPort.maxFuelConsumption();
			if (result == null || !result.equals(Generator.MAX_FUEL_CONSUMPTION)) {
				tester.logMessage("        but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * testing the static properties.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: dynamic properties of the generator
	 * 
	 *   Scenario: getting the current tank level
	 *     Given a standard generator
	 *     When I ask for the current tank level
	 *     Then I get the the test value for the current tank level for a standard generator
	 * 
	 *   Scenario: getting the current fuel consumption
	 *     Given a standard generator
	 *     When I ask for the current fuel consumption
	 *     Then I get the the test value for the current fuel consumption for a standard generator
	 * 
	 *   Scenario: starting the generator when it is not running
	 *     Given a generator that is off
	 *     When I start the generator
	 *     Then the generator is running
	 * 
	 *   Scenario: starting the generator when it is running
	 *     Given a generator that is running
	 *     When I start the generator
	 *     Then a precondition exception is thrown.
	 * 
	 *   Scenario: stopping the generator when it is running
	 *     Given a generator that is running
	 *     When I stop the generator
	 *     Then the generator is off
	 * 
	 *   Scenario: stopping the generator when it is not running
	 *     Given a generator that is not running
	 *     When I stop the generator
	 *     Then a precondition exception is thrown.
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
	protected static void	testDynamicProperties(
		AbstractComponent tester,
		GeneratorOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		tester.logMessage("Feature: dynamic properties of the generator");
		tester.logMessage("  Scenario: getting the current tank level");
		tester.logMessage("    Given a standard generator");
		tester.logMessage("    When I ask for the current tank level");
		tester.logMessage("    Then I get the the test value for the current tank level for a standard generator");
		SignalData<Double> sResult = null;
		try {
			sResult = outboundPort.currentTankLevel();
			if (sResult == null ||
						!sResult.getMeasure().equals(
										Generator.FAKE_CURRENT_TANK_LEVEL)) {
				tester.logMessage("        but was: " + sResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the current fuel consumption");
		tester.logMessage("    Given a standard generator");
		tester.logMessage("    When I ask for the current fuel consumption");
		tester.logMessage("    Then I get the the test value for the current fuel consumption for a standard generator");
		sResult = null;
		try {
			sResult = outboundPort.currentFuelConsumption();
			if (sResult == null ||
						!sResult.getMeasure().equals(
										Generator.FAKE_FUEL_CONSUMPTION)) {
				tester.logMessage("        but was: " + sResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: starting the generator when it is not running");
		tester.logMessage("    Given a generator that is off");
		State bResult;
		try {
			bResult = outboundPort.getState();
			if (!State.OFF.equals(bResult)) {
				tester.logMessage("        but it was " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();;
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I start the generator");
		try {
			outboundPort.startGenerator();
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    Then the generator is running");
		try {
			bResult = outboundPort.getState();
			if (!State.IDLE.equals(bResult)) {
				tester.logMessage("        but it was " + bResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		
		statistics.updateStatistics();

		tester.logMessage("  Scenario: starting the generator when it is running");
		tester.logMessage("    Given a generator that is running");
		try {
			bResult = outboundPort.getState();
			if (State.OFF.equals(bResult)) {
				tester.logMessage("        but it was " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();;
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I start the generator");
		tester.logMessage("    Then a precondition exception is thrown.");
		boolean old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			outboundPort.startGenerator();
			tester.logMessage("        but it was not!");
			statistics.incorrectResult();
		} catch (Throwable e) {
		} finally {
			BCMException.VERBOSE = old;
		}
		
		statistics.updateStatistics();

		tester.logMessage("  Scenario: stopping the generator when it is running");
		tester.logMessage("    Given a generator that is running");
		try {
			bResult = outboundPort.getState();
			if (State.OFF.equals(bResult)) {
				tester.logMessage("        but it was " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();;
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I stop the generator");
		try {
			outboundPort.stopGenerator();
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    Then the generator is off");
		try {
			bResult = outboundPort.getState();
			if (!State.OFF.equals(bResult)) {
				tester.logMessage("        but it was " + bResult);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: stopping the generator when it is not running");
		tester.logMessage("    Given a generator that is off");
		try {
			bResult = outboundPort.getState();
			if (!State.OFF.equals(bResult)) {
				tester.logMessage("        but it was " + bResult);
				statistics.failedCondition();
			}
		} catch (Throwable e) {
			statistics.failedCondition();;
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I stop the generator");
		tester.logMessage("    Then a precondition exception is thrown.");
		old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			outboundPort.stopGenerator();
			tester.logMessage("        but it was not!");
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
		GeneratorOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		testStaticProperties(tester, outboundPort, statistics);
		testDynamicProperties(tester, outboundPort, statistics);

		statistics.statisticsReport(tester);
	}
}
// -----------------------------------------------------------------------------
