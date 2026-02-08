package etape4.equipements.hem;

import java.util.concurrent.TimeUnit;

import etape1.bases.AdjustableCI;
import etape1.bases.RegistrationCI;
import etape1.equipements.hem.ports.AdjustableOutboundPort;
import etape1.equipments.batteries.BatteriesCI;
import etape1.equipments.generator.GeneratorCI;
import etape1.equipments.meter.ElectricMeterCI;
import etape1.equipments.solar_panel.SolarPanelCI;
import etape3.equipements.hem.HEMCyPhy;
import etape4.control.EquipmentConstraint;
import etape4.control.PriorityConfig;
import etape4.equipements.hem.EquipmentRegistry.EquipmentInfo;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;

/**
 * The class <code>HEMEnergyManager</code> extends HEMCyPhy with an intelligent
 * energy management control loop.
 *
 * <p>
 * This component monitors production and consumption in real-time and
 * dynamically adjusts equipment operation to maintain balance:
 * <ul>
 * <li>Suspends low-priority equipment when production < consumption</li>
 * <li>Resumes equipment when production > consumption</li>
 * <li>Starts generator if equipment suspension insufficient (future)</li>
 * <li>Charges batteries during surplus (future)</li>
 * </ul>
 * </p>
 *
 * <p>
 * The control loop runs periodically (default: 30s simulated time) using
 * BCM4Java's scheduled task mechanism.
 * </p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
@RequiredInterfaces(required = { AdjustableCI.class, ElectricMeterCI.class, BatteriesCI.class,
		SolarPanelCI.class, GeneratorCI.class })
@OfferedInterfaces(offered = { RegistrationCI.class })
public class HEMEnergyManager extends HEMCyPhy {

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** Control loop period in simulated seconds */
	public static final double CONTROL_PERIOD_SECONDS = 10.0;

	/** Enable verbose control loop logging */
	public static boolean CONTROL_LOOP_VERBOSE = true;

	// -------------------------------------------------------------------------
	// Variables
	// -------------------------------------------------------------------------

	/** Equipment registry for tracking all registered equipment */
	protected EquipmentRegistry equipmentRegistry;

	/** Control loop task (scheduled periodically) */
	protected EnergyControlLoopTask controlTask;

	/** Control loop period in nanoseconds (adjusted for acceleration) */
	protected long controlPeriodNanos;

	/** Whether control loop is currently running */
	protected boolean controlLoopActive;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a HEM Energy Manager component for standard execution.
	 *
	 * @throws Exception if component creation fails
	 */
	protected HEMEnergyManager() throws Exception {
		super();
		this.equipmentRegistry = new EquipmentRegistry();
		this.controlLoopActive = false;

		assert HEMEnergyManager.implementationInvariants(this)
				: new ImplementationInvariantException("HEMEnergyManager.implementationInvariants(this)");
		assert HEMEnergyManager.invariants(this) : new InvariantException("HEMEnergyManager.invariants(this)");
	}

	/**
	 * Create a HEM Energy Manager component for integration test execution.
	 *
	 * @param executionMode execution mode for the next run
	 * @param testScenario  test scenario to be executed
	 * @throws Exception if component creation fails
	 */
	protected HEMEnergyManager(ExecutionMode executionMode, TestScenario testScenario) throws Exception {
		super(executionMode, testScenario);
		this.equipmentRegistry = new EquipmentRegistry();
		this.controlLoopActive = false;

		assert HEMEnergyManager.implementationInvariants(this)
				: new ImplementationInvariantException("HEMEnergyManager.implementationInvariants(this)");
		assert HEMEnergyManager.invariants(this) : new InvariantException("HEMEnergyManager.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component Life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		log("HEM Energy Manager starting with control loop enabled");
		log("Equipment registry: " + this.equipmentRegistry);
	}

	@Override
	public synchronized void execute() throws Exception {
		// Call parent execute (handles test scenarios)
		super.execute();

		// Start control loop if in integration test mode
		if (this.executionMode.isIntegrationTest() || this.executionMode.isSILIntegrationTest()) {
			log("Starting energy management control loop");
			this.startControlLoop();
		}
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		log("HEM Energy Manager shutting down");
		this.stopControlLoop();
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Control Loop Management
	// -------------------------------------------------------------------------

	/**
	 * Start the energy management control loop.
	 *
	 * <p>
	 * The control loop runs at fixed intervals (default: 30 seconds simulated
	 * time). For SIL simulations, the period is adjusted by the acceleration
	 * factor.
	 * </p>
	 *
	 * @throws Exception if scheduling fails
	 */
	protected void startControlLoop() throws Exception {
		if (this.controlLoopActive) {
			log("Control loop already active");
			return;
		}

		// Calculate control period (adjusted for acceleration if SIL simulation)
		if (this.executionMode.isSILIntegrationTest()) {
			// In SIL mode, adjust for acceleration factor
			// TODO: Get acceleration factor from simulation configuration
			double accelerationFactor = 3600.0; // Default: 1 hour simulated = 1 second real
			double realSeconds = CONTROL_PERIOD_SECONDS / accelerationFactor;
			this.controlPeriodNanos = TimeUnit.SECONDS.toNanos((long) realSeconds);

			log(String.format(
				"Control loop period: %.2fs simulated (%.3fs real, acceleration=%.1fx)",
				CONTROL_PERIOD_SECONDS, realSeconds, accelerationFactor));
		} else {
			// In integration test mode without simulation, use real-time
			this.controlPeriodNanos = TimeUnit.SECONDS.toNanos((long) CONTROL_PERIOD_SECONDS);

			log(String.format(
				"Control loop period: %.2fs (real-time)",
				CONTROL_PERIOD_SECONDS));
		}

		// Create control task
		this.controlTask = new EnergyControlLoopTask(
			this,
			this.meterop,
			this.equipmentRegistry,
			CONTROL_LOOP_VERBOSE
		);

		// Schedule task at fixed rate
		this.scheduleTaskAtFixedRateOnComponent(
			this.controlTask,
			this.controlPeriodNanos,  // Initial delay
			this.controlPeriodNanos,  // Period
			TimeUnit.NANOSECONDS
		);

		this.controlLoopActive = true;
		log("Control loop started successfully");
	}

	/**
	 * Stop the energy management control loop.
	 */
	protected void stopControlLoop() {
		if (this.controlLoopActive) {
			log("Stopping control loop");
			// Task will be automatically cancelled when component shuts down
			this.controlLoopActive = false;
		}
	}

	// -------------------------------------------------------------------------
	// Equipment Registration (Override)
	// -------------------------------------------------------------------------

	@Override
	public boolean register(String uid, String controlPortURI, String xmlControlAdapter) throws Exception {
		// Call parent registration (handles port creation and connection)
		boolean success = super.register(uid, controlPortURI, xmlControlAdapter);

		if (success) {
			// Extract equipment type and get priority
			String type = extractType(uid);
			int priority = PriorityConfig.getBasePriority(type);

			// Get the port created by parent
			AdjustableOutboundPort port = this.equipmentPorts.get(uid);

			// Create constraint (for now, use default always-suspendable)
			// TODO: Create specific constraints for each equipment type
			EquipmentConstraint constraint = new EquipmentConstraint.AlwaysSuspendable();

			// Create equipment info
			EquipmentInfo info = new EquipmentInfo(uid, port, priority, constraint);

			// Register in our registry
			this.equipmentRegistry.register(info);

			log(String.format(
				"Equipment %s registered in energy manager (type=%s, priority=%d)",
				uid, type, priority));

			log("Registry status: " + this.equipmentRegistry);
		}

		return success;
	}

	@Override
	public void unregister(String uid) throws Exception {
		// Unregister from our registry
		this.equipmentRegistry.unregister(uid);

		// Call parent unregistration
		super.unregister(uid);

		log(String.format("Equipment %s unregistered from energy manager", uid));
	}

	// -------------------------------------------------------------------------
	// Internal Methods
	// -------------------------------------------------------------------------

	/**
	 * Log a message to both BCM4Java tracer and stdout.
	 *
	 * @param message message to log
	 */
	protected void log(String message) {
		if (VERBOSE) {
			System.out.println("[HEM] " + message);
			this.logMessage(message);
		}
	}

	/**
	 * Extract equipment type from UID.
	 *
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>"CoffeeMachine-1" -> "CoffeeMachine"</li>
	 * <li>"Fan-3" -> "Fan"</li>
	 * </ul>
	 * </p>
	 *
	 * @param uid unique identifier
	 * @return equipment type
	 */
	private String extractType(String uid) {
		int dashIndex = uid.lastIndexOf('-');
		return dashIndex > 0 ? uid.substring(0, dashIndex) : uid;
	}

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean implementationInvariants(HEMEnergyManager hem) {
		assert hem != null;

		boolean ret = true;
		ret &= HEMCyPhy.implementationInvariants(hem);
		ret &= hem.equipmentRegistry != null;
		return ret;
	}

	protected static boolean invariants(HEMEnergyManager hem) {
		assert hem != null;

		boolean ret = true;
		ret &= HEMCyPhy.invariants(hem);
		return ret;
	}
}
