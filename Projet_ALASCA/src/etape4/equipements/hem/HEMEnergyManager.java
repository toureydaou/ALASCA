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
import etape4.coordination.EnergyCoordinator;
import etape4.coordination.EnergyStateModel;
import etape4.coordination.EnergyStateModel.EnergySnapshot;
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
public class HEMEnergyManager extends HEMCyPhy implements EnergyCoordinator {

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

	/** Shared energy state model (data-centered coordination) */
	protected EnergyStateModel energyStateModel;

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
		this.connectToEnergySources = true;
		this.equipmentRegistry = new EquipmentRegistry();
		this.energyStateModel = new EnergyStateModel();
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
		this.connectToEnergySources = true;
		this.equipmentRegistry = new EquipmentRegistry();
		this.energyStateModel = new EnergyStateModel();
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
			double accelerationFactor = 360.0;
			double realSeconds = CONTROL_PERIOD_SECONDS / accelerationFactor;
			// Compute nanos from fractional seconds (avoid truncation to 0)
			this.controlPeriodNanos =
				(long)((CONTROL_PERIOD_SECONDS * TimeUnit.SECONDS.toNanos(1))
						/ accelerationFactor);
			// Minimum 50ms to avoid scheduler issues
			if (this.controlPeriodNanos < TimeUnit.MILLISECONDS.toNanos(50)) {
				this.controlPeriodNanos = TimeUnit.MILLISECONDS.toNanos(50);
			}

			log(String.format(
				"Control loop period: %.2fs simulated (%.3fs real, acceleration=%.1fx)",
				CONTROL_PERIOD_SECONDS, realSeconds, accelerationFactor));
		} else {
			// In integration test mode without simulation, use real-time
			// Compute nanos from fractional seconds
			this.controlPeriodNanos =
				(long)(CONTROL_PERIOD_SECONDS * TimeUnit.SECONDS.toNanos(1));

			log(String.format(
				"Control loop period: %.2fs (real-time)",
				CONTROL_PERIOD_SECONDS));
		}

		// Create control task with access to energy source ports and shared state model
		this.controlTask = new EnergyControlLoopTask(
			this,
			this.meterop,
			this.generatorop,
			this.batteriesop,
			this.solarPanelop,
			this.equipmentRegistry,
			this.energyStateModel,
			CONTROL_LOOP_VERBOSE
		);
		this.controlTask.setControlPeriodSeconds(CONTROL_PERIOD_SECONDS);

		// Delay the first effective run until the simulation clock has started.
		// DELAY_TO_START is typically 8000ms; add 2s margin for initialization.
		long delayToStartMs = 10000L;
		this.controlTask.setReadyTimeMs(
			System.currentTimeMillis() + delayToStartMs);
		log(String.format(
			"Control loop will wait %dms for simulation clock to start",
			delayToStartMs));

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
			if (this.controlTask != null) {
				this.controlTask.stop();
			}
			this.controlLoopActive = false;
		}
	}

	@Override
	public synchronized void finalise() throws Exception {
		// Stop control loop BEFORE disconnecting ports
		this.stopControlLoop();
		super.finalise();
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
		// Handle "XxxGeneratedConnector" format from XML adapter registration
		// e.g., "KettleGeneratedConnector" → "Kettle"
		// e.g., "CoffeeMachineGeneratedConnector" → "CoffeeMachine"
		if (uid.endsWith("GeneratedConnector")) {
			return uid.substring(0, uid.length() - "GeneratedConnector".length());
		}
		// Try underscore first (new format: CoffeeMachine_1), then dash
		int sepIndex = uid.lastIndexOf('_');
		if (sepIndex <= 0) {
			sepIndex = uid.lastIndexOf('-');
		}
		return sepIndex > 0 ? uid.substring(0, sepIndex) : uid;
	}

	// -------------------------------------------------------------------------
	// EnergyCoordinator Implementation
	// -------------------------------------------------------------------------

	@Override
	public EnergySnapshot getEnergySnapshot() {
		return energyStateModel.getSnapshot();
	}

	@Override
	public EnergyStateModel getEnergyStateModel() {
		return energyStateModel;
	}

	@Override
	public boolean canTurnOn(double requiredPowerWatts) {
		EnergySnapshot snapshot = energyStateModel.getSnapshot();
		double surplusWatts = snapshot.balanceWatts;
		return surplusWatts > (requiredPowerWatts + 100.0); // Add 100W safety margin
	}

	@Override
	public boolean shouldSuspend(String equipmentId) {
		EnergySnapshot snapshot = energyStateModel.getSnapshot();
		return snapshot.balanceWatts < 0; // Deficit exists
	}

	@Override
	public boolean canResume(String equipmentId, double requiredPowerWatts) {
		EnergySnapshot snapshot = energyStateModel.getSnapshot();
		double surplusWatts = snapshot.balanceWatts;
		return surplusWatts > requiredPowerWatts;
	}

	@Override
	public boolean isCriticalDeficit() {
		EnergySnapshot snapshot = energyStateModel.getSnapshot();
		return snapshot.balanceAmperes < -2.0; // More than 2A deficit
	}

	@Override
	public boolean isSurplus() {
		EnergySnapshot snapshot = energyStateModel.getSnapshot();
		return snapshot.balanceWatts > 100.0; // More than 100W surplus
	}

	@Override
	public String getRecommendedAction() {
		EnergySnapshot snapshot = energyStateModel.getSnapshot();

		if (snapshot.balanceWatts > 500.0) {
			if (snapshot.batteryChargeLevel < getBatteryChargeThreshold()) {
				return "BATTERY_CHARGE";
			}
			return "RESUME";
		} else if (snapshot.balanceWatts < -500.0) {
			if (snapshot.batteryChargeLevel > 0.5) {
				return "BATTERY_DISCHARGE";
			}
			if (!snapshot.generatorRunning) {
				return "GENERATOR_START";
			}
			return "SUSPEND";
		} else if (snapshot.balanceWatts < -100.0) {
			return "SUSPEND";
		} else if (snapshot.generatorRunning && snapshot.balanceWatts > 100.0) {
			return "GENERATOR_STOP";
		}

		return "NONE";
	}

	@Override
	public void notifyConsumptionChange(String equipmentId, double oldConsumptionWatts, double newConsumptionWatts) {
		// Equipment notifies coordinator of consumption changes
		// This allows real-time updates to the energy state
		if (VERBOSE) {
			log(String.format("[COORDINATOR] Equipment %s consumption: %.0fW -> %.0fW",
				equipmentId, oldConsumptionWatts, newConsumptionWatts));
		}
	}

	@Override
	public void notifyProductionChange(String sourceId, double productionWatts) {
		// Energy sources notify coordinator of production changes
		if ("SOLAR".equals(sourceId)) {
			energyStateModel.setSolarProduction(productionWatts);
		} else if ("GENERATOR".equals(sourceId)) {
			// Generator production is already managed by control loop
		}
		if (VERBOSE) {
			log(String.format("[COORDINATOR] Production source %s: %.0fW", sourceId, productionWatts));
		}
	}

	@Override
	public void notifySuspended(String equipmentId) {
		if (VERBOSE) {
			log(String.format("[COORDINATOR] Equipment %s suspended", equipmentId));
		}
	}

	@Override
	public void notifyResumed(String equipmentId) {
		if (VERBOSE) {
			log(String.format("[COORDINATOR] Equipment %s resumed", equipmentId));
		}
	}

	@Override
	public double getActionThreshold() {
		return EnergyControlLoopTask.ACTION_THRESHOLD;
	}

	@Override
	public double getGeneratorStartThreshold() {
		return EnergyControlLoopTask.GENERATOR_START_THRESHOLD;
	}

	@Override
	public double getBatteryChargeThreshold() {
		return EnergyControlLoopTask.BATTERY_CHARGE_THRESHOLD;
	}

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean implementationInvariants(HEMEnergyManager hem) {
		assert hem != null;

		boolean ret = true;
		ret &= HEMCyPhy.implementationInvariants(hem);
		ret &= hem.equipmentRegistry != null;
		ret &= hem.energyStateModel != null;
		return ret;
	}

	protected static boolean invariants(HEMEnergyManager hem) {
		assert hem != null;

		boolean ret = true;
		ret &= HEMCyPhy.invariants(hem);
		return ret;
	}
}
