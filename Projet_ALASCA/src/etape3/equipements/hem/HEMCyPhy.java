package etape3.equipements.hem;

import java.util.HashMap;

import etape1.bases.AdjustableCI;
import etape1.bases.RegistrationCI;
import etape1.bases.generator.ConnectorGenerator;
import etape1.bases.parser.ConnectorAdapterInfo;
import etape1.bases.parser.ConnectorAdapterParserXML;
import etape1.equipements.coffee_machine.CoffeeMachine;
import etape1.equipements.hem.ports.AdjustableOutboundPort;
import etape1.equipements.registration.ports.RegistrationI;
import etape1.equipements.registration.ports.RegistrationInboundPort;
import etape1.equipments.batteries.BatteriesCI;
import etape1.equipments.batteries.connections.BatteriesOutboundPort;
import etape1.equipments.generator.GeneratorCI;
import etape1.equipments.generator.connections.GeneratorOutboundPort;
import etape1.equipments.meter.ElectricMeterCI;
import etape1.equipments.meter.ElectricMeterUnitTester;
import etape1.equipments.meter.connections.ElectricMeterConnector;
import etape1.equipments.meter.connections.ElectricMeterOutboundPort;
import etape1.equipments.solar_panel.SolarPanelCI;
import etape1.equipments.solar_panel.connections.SolarPanelOutboundPort;
import etape3.equipements.meter.ElectricMeterCyPhy;

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
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
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
 * The class <code>HEM</code> implements the basis for a household energy
 * management component.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * As is, this component is only a very limited starting point for the actual
 * component. The given code is there only to ease the understanding of the
 * objectives, but most of it must be replaced to get the correct code.
 * Especially, no registration of the components representing the appliances is
 * given.
 * </p>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * true
 * }	// no more invariant
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
 * Created on : 2021-09-09
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = { AdjustableCI.class, ElectricMeterCI.class, BatteriesCI.class, SolarPanelCI.class,
		GeneratorCI.class })
@OfferedInterfaces(offered = { RegistrationCI.class })
public class HEMCyPhy extends AbstractComponent implements RegistrationI {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = false;
	/** when true, methods trace their actions. */
	public static boolean DEBUG = false;
	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;
	/**
	 * standard reflection, inbound port URI for the {@code HEMCyPhy} component.
	 */
	public static final String REFLECTION_INBOUND_PORT_URI = "hem-RIP-URI";

	// HEM registration URI for equipements
	public static final String REGISTRATION_COFFEE_INBOUND_PORT_URI = "HEM-REGISTRATION-COFFEE-INBOUND-PORT-URI";
	public static final String REGISTRATION_LAUNDRY_INBOUND_PORT_URI = "HEM-REGISTRATION-LAUNDRY-INBOUND-PORT-URI";
	public static final String REGISTRATION_KETTLE_INBOUND_PORT_URI = "HEM-REGISTRATION-KETTLE-INBOUND-PORT-URI";

	// HashMap to register equipements URI and connector class names
	protected HashMap<String, String> equipementsRegitered;

	// HashMap to store the outbound port for each registered equipment
	protected HashMap<String, AdjustableOutboundPort> equipmentPorts;

	// Registration port for coffee machine
	protected RegistrationInboundPort rcip;

	// Registration port for laundry
	protected RegistrationInboundPort rlip;

	// Registration port for kettle
	protected RegistrationInboundPort rkip;

	/** port to connect to the electric meter. */
	protected ElectricMeterOutboundPort meterop;

	/** port to connect to the batteries. */
	protected BatteriesOutboundPort batteriesop;
	/** port to connect to the solar panel. */
	protected SolarPanelOutboundPort solarPanelop;
	/** port to connect to the generator. */
	protected GeneratorOutboundPort generatorop;

	/**
	 * when true, manage the heater in a customised way, otherwise let it register
	 * itself as an adjustable appliance.
	 */
	protected boolean isPreFirstStep;
	

	// Execution/Simulation

	/** one thread for the method execute. */
	protected static int NUMBER_OF_STANDARD_THREADS = 1;
	/** one thread to schedule this component test actions. */
	protected static int NUMBER_OF_SCHEDULABLE_THREADS = 1;

	protected ExecutionMode executionMode;
	protected TestScenario testScenario;
	
	protected TestsStatistics statistics;
	
	public static final int COFFEE_MACHINE_TEST_DELAY = 5;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
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
	public static boolean staticImplementationInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticImplementationInvariant(NUMBER_OF_STANDARD_THREADS >= 0, HEMCyPhy.class,
				"NUMBER_OF_STANDARD_THREADS >= 0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(NUMBER_OF_SCHEDULABLE_THREADS >= 0, HEMCyPhy.class,
				"NUMBER_OF_SCHEDULABLE_THREADS");
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * hem != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param hem instance to be tested.
	 * @return true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean implementationInvariants(HEMCyPhy hem) {
		assert hem != null : new PreconditionException("hem != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
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
		ret &= AssertionChecking.checkStaticInvariant(X_RELATIVE_POSITION >= 0, HEMCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(Y_RELATIVE_POSITION >= 0, HEMCyPhy.class,
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
	 * hem != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param hem instance to be tested.
	 * @return true if the invariants are observed, false otherwise.
	 */
	protected static boolean invariants(HEMCyPhy hem) {
		assert hem != null : new PreconditionException("hem != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution for manual tests (no test scenario and no simulation)

	/**
	 * create a household energy manager component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * !(this instanceof ComponentInterface)
	 * }
	 * post	{@code
	 * getCurrentExecutionMode().isStandard()
	 * }
	 * </pre>
	 *
	 */
	protected HEMCyPhy() {
		// 1 standard thread to execute the method execute and 1 schedulable
		// thread that is used to perform the tests
		super(NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS);

		// by default, consider this execution as one in the pre-first step
		// and manage the heater in a customised way.
		this.isPreFirstStep = true;

		this.executionMode = ExecutionMode.STANDARD;
		this.testScenario = null;

		assert HEMCyPhy.implementationInvariants(this)
				: new ImplementationInvariantException("HEMCyPhy.implementationInvariants(this)");
		assert HEMCyPhy.invariants(this) : new InvariantException("HEMCyPhy.invariants(this)");
	}

	// Test execution with test scenario

	/**
	 * create a household energy manager component.
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
	 * executionMode != null && (executionMode.isIntegrationTest() || executionMode.isSILIntegrationTest())
	 * }
	 * pre	{@code
	 * testScenario != null
	 * }
	 * post	{@code
	 * getCurrentExecutionMode().equals(executionMode)
	 * }
	 * </pre>
	 *
	 * @param executionMode execution mode for the next run.
	 * @param testScenario  test scenario to be executed.
	 * @throws Exception <i>to do</i>.
	 */
	protected HEMCyPhy(ExecutionMode executionMode, TestScenario testScenario) throws Exception {
		// 1 standard thread to execute the method execute and 1 schedulable
		// thread that is used to perform the tests
		super(REFLECTION_INBOUND_PORT_URI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS);

		assert executionMode != null && (executionMode.isIntegrationTest() || executionMode.isSILIntegrationTest())
				: new PreconditionException("executionMode != null && (executionMode." + "isIntegrationTest() || "
						+ "executionMode.isSILIntegrationTest())");
		assert testScenario != null : new PreconditionException("testScenario != null");

		this.executionMode = executionMode;
		this.testScenario = testScenario;
		
		try {
			this.rcip = new RegistrationInboundPort(REGISTRATION_COFFEE_INBOUND_PORT_URI, this);
			this.rcip.publishPort();

			this.rlip = new RegistrationInboundPort(REGISTRATION_LAUNDRY_INBOUND_PORT_URI, this);
			this.rlip.publishPort();

			this.rkip = new RegistrationInboundPort(REGISTRATION_KETTLE_INBOUND_PORT_URI, this);
			this.rkip.publishPort();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// by default, consider this execution as one in the pre-first step
		// and manage the heater in a customised way.
		this.isPreFirstStep = false;
		
		this.equipementsRegitered = new HashMap<String, String>();
		this.equipmentPorts = new HashMap<String, AdjustableOutboundPort>();

		if (VERBOSE) {
			this.tracer.get().setTitle("Home Energy Manager component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		assert HEMCyPhy.implementationInvariants(this)
				: new ImplementationInvariantException("HEMCyPhy.implementationInvariants(this)");
		assert HEMCyPhy.invariants(this) : new InvariantException("HEMCyPhy.invariants(this)");
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
			this.meterop = new ElectricMeterOutboundPort(this);
			this.meterop.publishPort();
			this.doPortConnection(this.meterop.getPortURI(), ElectricMeterCyPhy.ELECTRIC_METER_INBOUND_PORT_URI,
					ElectricMeterConnector.class.getCanonicalName());
//			this.batteriesop = new BatteriesOutboundPort(this);
//			this.batteriesop.publishPort();
//			this.doPortConnection(
//					batteriesop.getPortURI(),
//					Batteries.STANDARD_INBOUND_PORT_URI,
//					BatteriesConnector.class.getCanonicalName());
//			this.solarPanelop = new SolarPanelOutboundPort(this);
//			this.solarPanelop.publishPort();
//			this.doPortConnection(
//					this.solarPanelop.getPortURI(),
//					SolarPanel.STANDARD_INBOUND_PORT_URI,
//					SolarPanelConnector.class.getCanonicalName());
//			this.generatorop = new GeneratorOutboundPort(this);
//			this.generatorop.publishPort();
//			this.doPortConnection(
//					this.generatorop.getPortURI(),
//					Generator.STANDARD_INBOUND_PORT_URI,
//					GeneratorConnector.class.getCanonicalName());
			
			
		

			
		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception {
		this.traceMessage("HEM begins execution.\n");

		switch (this.executionMode) {
		case STANDARD:
		case UNIT_TEST:
		case UNIT_TEST_WITH_SIL_SIMULATION:
			throw new BCMException("No unit test for HEM!");
		case INTEGRATION_TEST:
			this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI, this.testScenario.getClockURI());
			this.executeTestScenario(this.testScenario);
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock(ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI, this.testScenario.getClockURI());
//			this.executeTestScenario(this.testScenario);
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		default:
		}
		this.traceMessage("HEM ends execution.\n");

//		if (this.performTest) {
//			this.logMessage("Electric meter tests start.");
//			this.testMeter();
//			this.logMessage("Electric meter tests end.");
//			this.logMessage("Batteries tests start.");
//			this.testBatteries();
//			this.logMessage("Batteries tests end.");
//			this.logMessage("Solar Panel tests start.");
//			this.testSolarPanel();
//			this.logMessage("Solar Panel tests end.");
//			this.logMessage("Generator tests start.");
//			this.testGenerator();
//			this.logMessage("Generator tests end.");
//			if (this.isPreFirstStep) {
//				this.scheduleTestHeater();
//			}
//		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void finalise() throws Exception {
		this.doPortDisconnection(this.meterop.getPortURI());
//		this.doPortDisconnection(this.batteriesop.getPortURI());
//		this.doPortDisconnection(this.solarPanelop.getPortURI());
//		this.doPortDisconnection(this.generatorop.getPortURI());
		
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.meterop.unpublishPort();
//			this.batteriesop.unpublishPort();
//			this.solarPanelop.unpublishPort();
//			this.generatorop.unpublishPort();
			
			for (AdjustableOutboundPort port : this.equipmentPorts.values()) {
				if (port.connected()) {
					this.doPortDisconnection(port.getPortURI());
				}
				port.unpublishPort();
			}
			this.rcip.unpublishPort();
			this.rlip.unpublishPort();
			this.rkip.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * test the {@code ElectricMeter} component.
	 * 
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * Calls the test methods defined in {@code ElectricMeterUnitTester}.
	 * </p>
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
	public void testMeter() throws Exception {
		ElectricMeterUnitTester.runAllTests(this, this.meterop, new TestsStatistics());
	}

	/**
	 * test the {@code Batteries} component.
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
	 *
	 */
	public void testBatteries() throws Exception {
		//BatteriesUnitTester.runAllTests(this, this.batteriesop, new TestsStatistics());
	}

	/**
	 * test the {@code SolarPanel} component.
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
	 *
	 */
	public void testSolarPanel() throws Exception {
		//SolarPanelUnitTester.runAllTests(this, this.solarPanelop, new TestsStatistics());
	}

	/**
	 * test the {@code Generator} component.
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
	 *
	 */
	public void testGenerator() throws Exception {
		//GeneratorUnitTester.runAllTests(this, this.generatorop, new TestsStatistics());
	}

	/**
	 * test the heater.
	 * 
	 * <p>
	 * <strong>Gherkin specification</strong>
	 * </p>
	 * 
	 * <pre>
	 * Feature: adjustable appliance mode management
	 *   Scenario: getting the max mode index
	 *     Given the heater has just been turned on
	 *     When I call maxMode()
	 *     Then the result is its max mode index
	 *   Scenario: getting the current mode index
	 *     Given the heater has just been turned on
	 *     When I call currentMode()
	 *     Then the current mode is its max mode
	 *   Scenario: going down one mode index
	 *     Given the heater is turned on
	 *     And the current mode index is the max mode index
	 *     When I call downMode()
	 *     Then the method returns true
	 *     And the current mode is its max mode minus one
	 *   Scenario: going up one mode index
	 *     Given the heater is turned on
	 *     And the current mode index is the max mode index minus one
	 *     When I call upMode()
	 *     Then the method returns true
	 *     And the current mode is its max mode
	 *   Scenario: setting the mode index
	 *     Given the heater is turned on
	 *     And the mode index 1 is legitimate
	 *     When I call setMode(1)
	 *     Then the method returns true
	 *     And the current mode is 1
	 * Feature: Getting the power consumption given a mode
	 *   Scenario: getting the power consumption of the maximum mode
	 *     Given the heater is turned on
	 *     When I get the power consumption of the maximum mode
	 *     Then the result is the maximum power consumption of the heater
	 * Feature: suspending and resuming
	 *   Scenario: checking if suspended when not
	 *     Given the heater is turned on
	 *     And it has not been suspended yet
	 *     When I check if suspended
	 *     Then it is not
	 *   Scenario: suspending
	 *     Given the heater is turned on
	 *     And it is not suspended
	 *     When I call suspend()
	 *     Then the method returns true
	 *     And the heater is suspended
	 *   Scenario: going down one mode index when suspended
	 *     Given the heater is turned on
	 *     And the heater is suspended
	 *     When I call downMode()
	 *     Then a precondition exception is thrown
	 *   Scenario: going up one mode index when suspended
	 *     Given the heater is turned on
	 *     And the heater is suspended
	 *     When I call upMode()
	 *     Then a precondition exception is thrown
	 *   Scenario: going up one mode index when suspended
	 *     Given the heater is turned on
	 *     And the heater is suspended
	 *     When I call upMode()
	 *     Then a precondition exception is thrown
	 *   Scenario: getting the current mode when suspended
	 *     Given the heater is turned on
	 *     And the heater is suspended
	 *     When I get the current mode
	 *     Then a precondition exception is thrown
	 *   Scenario: checking the emergency
	 *     Given the heater is turned on
	 *     And it has just been suspended
	 *     When I call emergency()
	 *     Then the emergency is between 0.0 and 1.0
	 *   Scenario: resuming
	 *     Given the heater is turned on
	 *     And it is suspended
	 *     When I call resume()
	 *     Then the method returns true
	 *     And the heater is not suspended
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
	 * @throws Exception <i>to do</i>.
	 */
	public void testCoffeeMachine() throws Exception {
		this.logMessage("Coffee tests start.");
		this.statistics = new TestsStatistics();
		try {
			// Get the coffee machine port from the registered equipment
			AdjustableOutboundPort coffeePort = this.equipmentPorts.get(CoffeeMachine.COFFEE_MACHINE_CONNECTOR_NAME);

			if (coffeePort == null) {
				this.logMessage("ERROR: Coffee machine not registered in equipmentPorts!");
				this.logMessage("Available equipment UIDs: " + this.equipmentPorts.keySet());
				return;
			}

			this.logMessage("\n*** Testing coffee machine adjustable modes ***\n");

			// ---------------------------------------------------------------------
			// Scenario: Getting the maximum mode index
			// ---------------------------------------------------------------------
			this.logMessage("Testing maxMode():");
			int maxMode = coffeePort.maxMode();
			this.logMessage("  - maxMode() returned: " + maxMode);

			if (maxMode <= 0) {
				this.logMessage("  -> Incorrect: maximum mode should be > 0.");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			// ---------------------------------------------------------------------
			// Scenario: Getting the current mode index
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting currentMode():");
			int currentMode = coffeePort.currentMode();
			this.logMessage("  - currentMode() returned: " + currentMode);

			if (currentMode < 0 || currentMode > maxMode) {
				this.logMessage("  -> Incorrect: current mode is out of valid range.");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			// ---------------------------------------------------------------------
			// Scenario: Increasing the mode index
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting upMode():");
			if (currentMode == maxMode) {
				this.logMessage("  - Already at maximum mode, cannot increase.");
				this.statistics.updateStatistics();
			} else {
				boolean upSuccess = coffeePort.upMode();
				int newMode = coffeePort.currentMode();
				this.logMessage("  - upMode() returned: " + upSuccess);
				this.logMessage("  - New mode is: " + newMode);

				if (!upSuccess) {
					this.logMessage("  -> Incorrect: upMode() should return true when current < max.");
					this.statistics.incorrectResult();
				}
				if (newMode != currentMode + 1) {
					this.logMessage("  -> Incorrect: new mode should be previous mode + 1.");
					this.statistics.incorrectResult();
				}
				this.statistics.updateStatistics();

				currentMode = newMode; // update for next tests
			}

			// ---------------------------------------------------------------------
			// Scenario: Decreasing the mode index
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting downMode():");
			if (currentMode == 0) {
				this.logMessage("  - Already at minimum mode, cannot decrease.");
				this.statistics.updateStatistics();
			} else {
				boolean downSuccess = coffeePort.downMode();
				int newMode = coffeePort.currentMode();
				this.logMessage("  - downMode() returned: " + downSuccess);
				this.logMessage("  - New mode is: " + newMode);

				if (!downSuccess) {
					this.logMessage("  -> Incorrect: downMode() should return true when current > 0.");
					this.statistics.incorrectResult();
				}
				if (newMode != currentMode - 1) {
					this.logMessage("  -> Incorrect: new mode should be previous mode - 1.");
					this.statistics.incorrectResult();
				}
				this.statistics.updateStatistics();

				currentMode = newMode;
			}

			// ---------------------------------------------------------------------
			// Scenario: Getting consumption of the maximum mode
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting getModeConsumption():");
			double maxConsumption = coffeePort.getModeConsumption(maxMode);
			this.logMessage("  - getModeConsumption(" + maxMode + ") returned: " + maxConsumption);

			if (maxConsumption <= 0) {
				this.logMessage("  -> Incorrect: consumption for max mode must be > 0.");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			// ---------------------------------------------------------------------
			// Scenario: Suspending
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting suspend():");
			boolean suspendSuccess = coffeePort.suspend();
			this.logMessage("  - suspend() returned: " + suspendSuccess);

			if (!suspendSuccess) {
				this.logMessage("  -> Incorrect: suspend() should return true.");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();

			// ---------------------------------------------------------------------
			// Scenario: Calling upMode() while suspended
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting upMode() while suspended (should fail):");
			try {
				coffeePort.upMode();
				this.logMessage("  -> Incorrect: upMode() should throw an exception while suspended.");
				this.statistics.incorrectResult();
			} catch (AssertionError | Exception e) {
				this.logMessage("  - Correct: upMode() threw an exception as expected.");
			}
			this.statistics.updateStatistics();

			// ---------------------------------------------------------------------
			// Scenario: Calling currentMode() while suspended
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting currentMode() while suspended (should fail):");
			try {
				coffeePort.currentMode();
				this.logMessage("  -> Incorrect: currentMode() should throw an exception while suspended.");
				this.statistics.incorrectResult();
			} catch (AssertionError | Exception e) {
				this.logMessage("  - Correct: currentMode() threw an exception as expected.");
			}
			this.statistics.updateStatistics();

			// ---------------------------------------------------------------------
			// Scenario: Resuming
			// ---------------------------------------------------------------------
			this.logMessage("\nTesting resume():");
			boolean resumeSuccess = coffeePort.resume();
			this.logMessage("  - resume() returned: " + resumeSuccess);

			if (!resumeSuccess) {
				this.logMessage("  -> Incorrect: resume() should return true.");
				this.statistics.incorrectResult();
			}
			this.statistics.updateStatistics();
			
			this.statistics.statisticsReport(this);

			this.logMessage("\n*** Coffee machine tests completed ***\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean registered(String uid) throws Exception {
		return this.equipementsRegitered.containsKey(uid);
	}

	@Override
	public boolean register(String uid, String controlPortURI, String xmlControlAdapter) throws Exception {
		this.logMessage("Registering equipment: " + uid);
		if (this.registered(uid))
			return false;

		// Create a new outbound port for this equipment
		AdjustableOutboundPort equipmentPort = new AdjustableOutboundPort(this);
		equipmentPort.publishPort();

		// Parse the XML descriptor and generate the connector
		ConnectorAdapterInfo infos = ConnectorAdapterParserXML.parse(xmlControlAdapter);
		Class<?> connectorGenerated = ConnectorGenerator.generate(infos, uid);

		this.logMessage("Connector generated for " + uid + ": " + connectorGenerated.getCanonicalName());

		// Connect the equipment using the generated connector
		this.doPortConnection(equipmentPort.getPortURI(), controlPortURI, connectorGenerated.getCanonicalName());

		// Store the connector class name and the port for this equipment
		this.equipementsRegitered.put(uid, connectorGenerated.getCanonicalName());
		this.equipmentPorts.put(uid, equipmentPort);

		this.logMessage("Equipment " + uid + " registered successfully");

		return true;
	}

	@Override
	public void unregister(String uid) throws Exception {
		this.logMessage("Unregistering equipment: " + uid);

		if (!this.registered(uid)) {
			this.logMessage("Equipment " + uid + " is not registered");
			return;
		}

		// Get the port for this equipment
		AdjustableOutboundPort equipmentPort = this.equipmentPorts.get(uid);

		if (equipmentPort != null) {
			// Disconnect and unpublish the port
			if (equipmentPort.connected()) {
				this.doPortDisconnection(equipmentPort.getPortURI());
			}
			equipmentPort.unpublishPort();
		}

		// Remove from the maps
		this.equipementsRegitered.remove(uid);
		this.equipmentPorts.remove(uid);

		this.logMessage("Equipment " + uid + " unregistered successfully");
		
	}

	/**
	 * test the {@code Heater} component, in cooperation with the
	 * {@code HeaterTester} component.
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
//	protected void		scheduleTestHeater()
//	{
//		// Test for the heater
//		Instant heaterTestStart =
//				this.ac.getStartInstant().plusSeconds(
//							(HeaterUnitTester.SWITCH_ON_DELAY +
//											HeaterUnitTester.SWITCH_OFF_DELAY)/2);
//		this.traceMessage("HEM schedules the heater test.\n");
//		long delay = this.ac.nanoDelayUntilInstant(heaterTestStart);
//
//		// schedule the switch on heater in one second
//		this.scheduleTaskOnComponent(
//				new AbstractComponent.AbstractTask() {
//					@Override
//					public void run() {
//						try {
//							testHeater();
//						} catch (Throwable e) {
//							throw new BCMRuntimeException(e) ;
//						}
//					}
//				}, delay, TimeUnit.NANOSECONDS);
//	}
}
// -----------------------------------------------------------------------------
