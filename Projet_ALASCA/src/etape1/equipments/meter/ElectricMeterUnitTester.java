package etape1.equipments.meter;

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
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;
import physical_data.SignalData;
import tests_utils.TestsStatistics;
import etape1.equipments.meter.connections.ElectricMeterConnector;
import etape1.equipments.meter.connections.ElectricMeterOutboundPort;


// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeterUnitTester</code> performs unit tests for
 * the electric meter component.
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
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required={ElectricMeterCI.class})
public class			ElectricMeterUnitTester
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;

	protected ElectricMeterOutboundPort emop;

	/** collector of tests statistics.										*/
	protected TestsStatistics			statistics;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the glass-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code em != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param em	instance to be tested.
	 * @return		true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		ElectricMeterUnitTester em
		)
	{
		assert	em != null : new PreconditionException("em != null");

		boolean ret = true;
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
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				ElectricMeterUnitTester.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				ElectricMeterUnitTester.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the black-box invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code em != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param em	instance to be tested.
	 * @return		true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(ElectricMeterUnitTester em)
	{
		assert	em != null : new PreconditionException("em != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an electric meter unit tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected			ElectricMeterUnitTester() throws Exception
	{
		super(1, 0);

		this.emop = new ElectricMeterOutboundPort(this);
		this.emop.publishPort();

		if(VERBOSE) {
			this.tracer.get().setTitle("Electric meter tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert	implementationInvariants(this) :
				new ImplementationInvariantException(
						"ElectricMeter.glassBoxInvariants(this)");
		assert	invariants(this) :
				new InvariantException("ElectricMeter.blackBoxInvariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * test getting the tension of the electric meter.
	 * 
	 * <p><strong>Gherkin specification</strong></p>
	 * 
	 * <pre>
	 * Feature: Getting the tension of the electric meter
	 *   Scenario: getting the tension
	 *     Given the electric meter has just been initialised
	 *     When I get the tension of the electric meter
	 *     Then the tension is the one defined by the electric meter
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code emop != null}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param emop			outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 */
	public static void		testGetTension(
		AbstractComponent tester,
		ElectricMeterOutboundPort emop,
		TestsStatistics statistics
		)
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	emop != null : new PreconditionException("emop != null");
		assert	statistics != null :
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: Getting the tension of the electric meter");
		tester.logMessage("  Scenario: getting the tension");
		tester.logMessage("    Given the electric meter has just been initialised");
		Measure<Double> result = null;
		try {
			tester.logMessage("    When I get the tension of the electric meter");
			result = emop.getTension();
			if (result != null &&
				result.getData() == ElectricMeter.TENSION.getData() &&
				result.getMeasurementUnit().equals(
								ElectricMeter.TENSION.getMeasurementUnit())) {
				tester.logMessage("    Then the tension is the one defined by the"
											+ " electric meter");
			} else {
				tester.logMessage("     but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * test getting the current power consumption.
	 * 
	 * <p><strong>Gherkin specification</strong></p>
	 * 
	 * <pre>
	 * Feature: Getting the current power consumption");
	 *   Scenario: getting the current power consumption");
	 *     Given the electric meter has just been initialised");
	 *     When I get the current power consumption");
	 *     Then the current power consumption is 0.0 amperes");
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code emop != null}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param tester		tester component.
	 * @param emop			outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 */
	public static void		testGetCurrentConsumption(
		AbstractComponent tester,
		ElectricMeterOutboundPort emop,
		TestsStatistics statistics
		)
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	emop != null : new PreconditionException("emop != null");
		assert	statistics != null :
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: Getting the current power consumption");
		tester.logMessage("  Scenario: getting the current power consumption");
		tester.logMessage("    Given the electric meter has just been initialised");
		SignalData<Double> result = null;
		try {
			tester.logMessage("    When I get the current power consumption");
			result = emop.getCurrentConsumption();
			if (result != null &&
				result.getMeasure().getData() == 0.0 &&
				result.getMeasure().getMeasurementUnit().equals(
									ElectricMeterImplementationI.POWER_UNIT)) {
				tester.logMessage("    Then the current power consumption is "
								  + "0.0 " +
								  ElectricMeterImplementationI.POWER_UNIT);
			} else {
				tester.logMessage("     but was: " + result);
				statistics.incorrectResult();
			}
		} catch (Throwable e) {
			statistics.incorrectResult();
			tester.logMessage("     but the exception " + e + " has been raised");
		}

		statistics.updateStatistics();
	}

	/**
	 * test getting the current power production.
	 * 
	 * <p><strong>Gherkin specification</strong></p>
	 * 
	 * <pre>
	 * Feature: Getting the current power production
	 *   Scenario: getting the current power production
	 *     Given the electric meter has just been initialised
	 *     When I get the current power production
	 *     Then the current power production is 0.0 amperes
	 * </pre>
	 *
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code emop != null}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param emop			outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 */
	public static void		testGetCurrentProduction(
		AbstractComponent tester,
		ElectricMeterOutboundPort emop,
		TestsStatistics statistics
		)
	{
		assert	tester != null : new PreconditionException("tester != null");
		assert	emop != null : new PreconditionException("emop != null");
		assert	statistics != null :
				new PreconditionException("statistics != null");

		tester.logMessage("Feature: Getting the current power production");
		tester.logMessage("  Scenario: getting the current power production");
		tester.logMessage("    Given the electric meter has just been initialised");
		SignalData<Double> result = null;
		try {
			tester.logMessage("    When I get the current power production");
			result = emop.getCurrentProduction();
			if (result != null &&
				result.getMeasure().getData() == 0.0 &&
				result.getMeasure().getMeasurementUnit().equals(
									ElectricMeterImplementationI.POWER_UNIT)) {
				tester.logMessage("    Then the current power production is "
								  + "0.0 " +
								  ElectricMeterImplementationI.POWER_UNIT);
			} else {
				tester.logMessage("     but was: " + result);
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
	 * <pre>
	 * pre	{@code tester != null}
	 * pre	{@code emop != null && emop.connected()}
	 * pre	{@code statistics != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param tester		tester component.
	 * @param emop			outbound port to be used to call the tested methods.
	 * @param statistics	collector of tests statistics.
	 */
	public static void			runAllTests(
		AbstractComponent tester,
		ElectricMeterOutboundPort emop,
		TestsStatistics statistics
		)
	{
		testGetTension(tester, emop, statistics);
		testGetCurrentConsumption(tester, emop, statistics);
		testGetCurrentProduction(tester, emop, statistics);

		statistics.statisticsReport(tester);
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
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
					this.emop.getPortURI(),
					ElectricMeter.ELECTRIC_METER_INBOUND_PORT_URI,
					ElectricMeterConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		runAllTests(this, this.emop, this.statistics);
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.emop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.emop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------
