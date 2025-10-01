package fr.sorbonne_u.components.hem2025e1.equipments.solar_panel;

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
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025.tests_utils.TestsStatistics;
import fr.sorbonne_u.components.hem2025e1.equipments.solar_panel.connections.SolarPanelConnector;
import fr.sorbonne_u.components.hem2025e1.equipments.solar_panel.connections.SolarPanelOutboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelUnitTester</code> implements a component testing
 * the {@code SolarPanel}. 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code solarPanelInboundPortURI != null && !solarPanelInboundPortURI.isEmpty()}
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-09-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {SolarPanelCI.class})
public class			SolarPanelUnitTester
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

	/** outboud port connecting to the {@code SolarPanel} component.		*/
	protected final SolarPanelOutboundPort	outboundPort;
	/** URI of the solar panel inbound port offering the {@code SolarPanelCI}
	 *  component interface.												*/
	protected final String					solarPanelInboundPortURI;

	/** collector of test statistics.										*/
	protected TestsStatistics				statistics;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code solarPanelInboundPortURI != null && !solarPanelInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param solarPanelInboundPortURI	URI of the solar panel inbound port offering the {@code SolarPanelCI} component interface.
	 * @throws Exception 
	 */
	protected			SolarPanelUnitTester(
		String solarPanelInboundPortURI
		) throws Exception
	{
		super(1, 0);

		// Preconditions checking
		assert	solarPanelInboundPortURI != null &&
										!solarPanelInboundPortURI.isEmpty() :
			new PreconditionException(
					"solarPanelInboundPortURI != null && "
					+ "!solarPanelInboundPortURI.isEmpty()");

		this.solarPanelInboundPortURI = solarPanelInboundPortURI;
		this.outboundPort = new SolarPanelOutboundPort(this);
		this.outboundPort.publishPort();
		this.statistics = new TestsStatistics();

		if (VERBOSE) {
			this.tracer.get().setTitle("Solar Panel tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * testing the power production.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: solar panel number of square meters
	 * 
	 *   Scenario: getting the solar panel number of square meters
	 *     Given a solar panel of a fixed number of square meters
	 *     When I ask for the number of square meters
	 *     Then I get the fixed number of square meters
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
	protected static void	testNumberOfSquareMeters(
		AbstractComponent tester,
		SolarPanelOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: solar panel number of square meters");
		tester.logMessage("  Scenario: getting the solar panel number of square meters");
		tester.logMessage("    Given a solar panel of a fixed number of square meters");
		tester.logMessage("    When I ask for the number of square meters");
		tester.logMessage("    Then I get the fixed number of square meters");
		try {
			outboundPort.getNumberOfSquareMeters();
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * testing the power production.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: getting the solar panel power production capacities
	 * 
	 *   Scenario: getting the nominal power production capacity
	 *     Given a solar panel of a fixed number of square meters
	 *     When I ask for the nominal capacity
	 *     Then I get the standard nominal capacity for the fixed number of square meters
	 * 
	 *   Scenario: getting the current power production capacity
	 *     Given a solar panel
	 *     When I ask for the current power production capacity
	 *     Then I get the test value for current power production capacity
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
	protected static void	testTotalPowerProductionCapacities(
		AbstractComponent tester,
		SolarPanelOutboundPort outboundPort,
		TestsStatistics statistics
		) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: getting the solar panel power production capacities");
		tester.logMessage("  Scenario: getting the nominal power production capacity for a solar panel of a fixed number of square meters");
		tester.logMessage("    Given a solar panel of a fixed number of square meters");
		int iResult = -1;
		try {
			iResult = outboundPort.getNumberOfSquareMeters();
		} catch (Throwable e) {
			statistics.failedCondition();
			tester.logMessage("     but the exception " + e + " has been raised");
		}
		tester.logMessage("    When I ask for the nominal capacity");
		tester.logMessage("    Then I get the standard nominal capacity for the fixed number of square meters");
		Measure<Double> result = null;
		try {
			result = outboundPort.getNominalPowerProductionCapacity();
			if (result == null ||
					!result.equals(new Measure<Double>(
							iResult * SolarPanel.CAPACITY_PER_SQUARE_METER.getData(),
							SolarPanel.CAPACITY_PER_SQUARE_METER.
													getMeasurementUnit()))) {
				tester.logMessage("      but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();

		tester.logMessage("  Scenario: getting the current power production capacity");
		tester.logMessage("    Given a solar panel");
		tester.logMessage("    When I ask for the current power production capacity");
		tester.logMessage("    Then I get the test value for current power production capacity");
		SignalData<Double> sResult = null;
		try {
			sResult = outboundPort.getCurrentPowerProductionLevel();
			if (sResult == null || !sResult.getMeasure().equals(
									SolarPanel.FAKE_CURRENT_POWER_PRODUCTION)) {
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
	public static void	runAllTests(
			AbstractComponent tester,
			SolarPanelOutboundPort outboundPort,
			TestsStatistics statistics
			) throws Exception
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	outboundPort != null && outboundPort.connected() :
				new PreconditionException(
						"outboundPort != null && outboundPort.connected()");
		assert	statistics != null:
				new PreconditionException("statistics != null");

		testNumberOfSquareMeters(tester, outboundPort, statistics);
		testTotalPowerProductionCapacities(tester, outboundPort, statistics);
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
					outboundPort.getPortURI(),
					this.solarPanelInboundPortURI,
					SolarPanelConnector.class.getCanonicalName());
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
		testTotalPowerProductionCapacities(
									this, this.outboundPort, this.statistics);
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
