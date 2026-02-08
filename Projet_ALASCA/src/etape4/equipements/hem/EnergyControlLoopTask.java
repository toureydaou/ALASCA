package etape4.equipements.hem;

import java.util.List;

import etape1.equipments.meter.connections.ElectricMeterOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import etape4.equipements.hem.EquipmentRegistry.EquipmentInfo;

/**
 * The class <code>EnergyControlLoopTask</code> implements the periodic energy
 * management control algorithm.
 *
 * <p>
 * This task runs at fixed intervals to:
 * <ul>
 * <li>Read current production and consumption from ElectricMeter</li>
 * <li>Calculate deficit or surplus</li>
 * <li>Suspend equipment when production < consumption</li>
 * <li>Resume equipment when production > consumption</li>
 * <li>Start generator if equipment suspension insufficient</li>
 * </ul>
 * </p>
 *
 * <p>
 * Thread-safe via EquipmentRegistry's internal locking.
 * </p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public class EnergyControlLoopTask extends AbstractComponent.AbstractTask {

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** Threshold for action (only act if deficit/surplus > threshold in Amperes) */
	public static final double ACTION_THRESHOLD = 0.5;

	/** Threshold for starting generator (Amperes) */
	public static final double GENERATOR_START_THRESHOLD = 1.0;

	// -------------------------------------------------------------------------
	// Variables
	// -------------------------------------------------------------------------

	/** Reference to owner component */
	private final AbstractComponent owner;

	/** Port to read meter data */
	private final ElectricMeterOutboundPort meterop;

	/** Equipment registry */
	private final EquipmentRegistry registry;

	/** Whether to enable verbose logging */
	private final boolean verbose;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Create a new energy control loop task.
	 *
	 * @param owner    owner component
	 * @param meterop  port to electric meter
	 * @param registry equipment registry
	 * @param verbose  enable verbose logging
	 */
	public EnergyControlLoopTask(
		AbstractComponent owner,
		ElectricMeterOutboundPort meterop,
		EquipmentRegistry registry,
		boolean verbose
	) {
		this.owner = owner;
		this.meterop = meterop;
		this.registry = registry;
		this.verbose = verbose;
	}

	// -------------------------------------------------------------------------
	// Logging
	// -------------------------------------------------------------------------

	private void log(String message) {
		System.out.println(message);
		owner.logMessage(message);
	}

	// -------------------------------------------------------------------------
	// Task Execution
	// -------------------------------------------------------------------------

	@Override
	public void run() {
		try {
			// 1. Read current state from meter
			SignalData<Double> consumption = meterop.getCurrentConsumption();
			SignalData<Double> production = meterop.getCurrentProduction();

			double consumptionAmperes = consumption.getMeasure().getData();
			double productionAmperes = production.getMeasure().getData();

			// 2. If meter returns 0 (integration test without simulation),
			//    estimate consumption from registered equipment states
			if (consumptionAmperes == 0.0 && productionAmperes == 0.0) {
				consumptionAmperes = estimateConsumptionFromEquipment();
				// Production stays at 0 (no generator/solar in this test)
			}

			// 3. Calculate deficit/surplus (positive = deficit, negative = surplus)
			double balance = consumptionAmperes - productionAmperes;

			if (verbose) {
				log(String.format(
					"[CONTROL LOOP] Consumption: %.2fA, Production: %.2fA, Balance: %.2fA",
					consumptionAmperes, productionAmperes, balance));
			}

			// 4. Make decision based on balance
			if (balance > ACTION_THRESHOLD) {
				handleDeficit(balance, consumptionAmperes, productionAmperes);
			} else if (balance < -ACTION_THRESHOLD) {
				handleSurplus(-balance, consumptionAmperes, productionAmperes);
			} else {
				if (verbose) {
					log("[CONTROL LOOP] System balanced, no action needed");
				}
			}

		} catch (Exception e) {
			log("[CONTROL LOOP ERROR] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Estimate total consumption by querying registered equipment.
	 *
	 * <p>
	 * Used in INTEGRATION_TEST mode (without simulation) where the
	 * ElectricMeter always returns 0. Queries each equipment's current
	 * mode via the AdjustableCI port.
	 * </p>
	 *
	 * @return estimated consumption in Amperes
	 */
	private double estimateConsumptionFromEquipment() {
		double totalAmperes = 0.0;
		List<EquipmentInfo> allEquipment = registry.getAllEquipment();

		for (EquipmentInfo eq : allEquipment) {
			if (eq.suspended) continue;
			try {
				int mode = eq.port.currentMode();
				eq.currentMode = mode;
				double watts = eq.port.getModeConsumption(mode);
				double amperes = watts / 220.0;
				totalAmperes += amperes;

				if (verbose) {
					log(String.format(
						"[CONTROL LOOP]   Equipment %s: mode=%d, consumption=%.2fW (%.2fA)",
						eq.uid, mode, watts, amperes));
				}
			} catch (Exception e) {
				if (verbose) {
					log(String.format(
						"[CONTROL LOOP]   Error reading %s: %s", eq.uid, e.getMessage()));
				}
			}
		}

		if (verbose) {
			log(String.format(
				"[CONTROL LOOP]   Total estimated consumption: %.2fA", totalAmperes));
		}

		return totalAmperes;
	}

	// -------------------------------------------------------------------------
	// Control Strategies
	// -------------------------------------------------------------------------

	/**
	 * Handle deficit situation (consumption > production).
	 *
	 * <p>
	 * Strategy:
	 * <ol>
	 * <li>Suspend equipment by priority (low priority first)</li>
	 * <li>If still insufficient, start generator (future)</li>
	 * </ol>
	 * </p>
	 *
	 * @param deficit            deficit amount in Amperes
	 * @param consumptionAmperes current consumption
	 * @param productionAmperes  current production
	 * @throws Exception if control action fails
	 */
	private void handleDeficit(double deficit, double consumptionAmperes, double productionAmperes) throws Exception {
		log(String.format(
			"[CONTROL LOOP] DEFICIT detected: %.2fA (consumption: %.2fA, production: %.2fA)",
			deficit, consumptionAmperes, productionAmperes));

		// Get suspendable equipment sorted by priority (low priority first)
		List<EquipmentInfo> suspendable = registry.getSuspendableEquipment();

		if (suspendable.isEmpty()) {
			log("[CONTROL LOOP] No equipment available to suspend");
			// TODO Phase 4: Start generator if available
			return;
		}

		log(String.format("[CONTROL LOOP] Found %d suspendable equipment", suspendable.size()));

		double remaining = deficit;
		int suspendedCount = 0;

		for (EquipmentInfo eq : suspendable) {
			if (remaining <= ACTION_THRESHOLD) {
				break; // Deficit covered
			}

			try {
				// Estimate power savings from suspending this equipment
				// Note: getModeConsumption returns Watts, need to convert to Amperes
				double currentConsumption = eq.getModeConsumption(eq.currentMode);
				double currentIntensity = currentConsumption / 220.0; // I = P / V

				log(String.format(
					"[CONTROL LOOP] Attempting to suspend %s (priority=%d, consumption=%.2fA)",
					eq.uid, eq.priority, currentIntensity));

				// Suspend equipment
				boolean success = eq.port.suspend();

				if (success) {
					eq.suspended = true;
					eq.priorSuspendMode = eq.currentMode;
					remaining -= currentIntensity;
					suspendedCount++;

					log(String.format(
						"[CONTROL LOOP] ✓ Suspended %s, remaining deficit: %.2fA",
						eq.uid, remaining));
				} else {
					log(String.format(
						"[CONTROL LOOP] ✗ Failed to suspend %s", eq.uid));
				}
			} catch (Exception e) {
				log(String.format(
					"[CONTROL LOOP] Error suspending %s: %s", eq.uid, e.getMessage()));
			}
		}

		log(String.format(
			"[CONTROL LOOP] Suspended %d equipment, remaining deficit: %.2fA",
			suspendedCount, remaining));

		// If still in deficit after suspending all available equipment
		if (remaining > GENERATOR_START_THRESHOLD) {
			log(String.format(
				"[CONTROL LOOP] Large deficit remains (%.2fA), generator start needed (not implemented yet)",
				remaining));
			// TODO Phase 4: Start generator
		}
	}

	/**
	 * Handle surplus situation (production > consumption).
	 *
	 * <p>
	 * Strategy:
	 * <ol>
	 * <li>Stop generator if running (future)</li>
	 * <li>Resume suspended equipment by urgency (high emergency first)</li>
	 * <li>Charge batteries with remaining surplus (future)</li>
	 * </ol>
	 * </p>
	 *
	 * @param surplus            surplus amount in Amperes
	 * @param consumptionAmperes current consumption
	 * @param productionAmperes  current production
	 * @throws Exception if control action fails
	 */
	private void handleSurplus(double surplus, double consumptionAmperes, double productionAmperes) throws Exception {
		log(String.format(
			"[CONTROL LOOP] SURPLUS detected: %.2fA (consumption: %.2fA, production: %.2fA)",
			surplus, consumptionAmperes, productionAmperes));

		// TODO Phase 4: Stop generator if running

		// Get suspended equipment
		List<EquipmentInfo> suspended = registry.getSuspendedEquipment();

		if (suspended.isEmpty()) {
			log("[CONTROL LOOP] No suspended equipment to resume");
			// TODO Phase 4: Charge batteries
			return;
		}

		log(String.format("[CONTROL LOOP] Found %d suspended equipment", suspended.size()));

		// Sort by emergency (highest urgency first)
		suspended.sort((e1, e2) -> {
			try {
				return Double.compare(e2.emergency(), e1.emergency());
			} catch (Exception ex) {
				return 0;
			}
		});

		double remaining = surplus;
		int resumedCount = 0;

		for (EquipmentInfo eq : suspended) {
			if (remaining <= ACTION_THRESHOLD) {
				break; // Surplus exhausted
			}

			try {
				// Estimate power needed to resume this equipment
				double resumeConsumption = eq.getModeConsumption(eq.priorSuspendMode);
				double resumeIntensity = resumeConsumption / 220.0;

				// Check if we have enough surplus to resume
				if (resumeIntensity > remaining) {
					log(String.format(
						"[CONTROL LOOP] Insufficient surplus to resume %s (needs %.2fA, have %.2fA)",
						eq.uid, resumeIntensity, remaining));
					continue;
				}

				double urgency = eq.emergency();
				log(String.format(
					"[CONTROL LOOP] Attempting to resume %s (urgency=%.2f, consumption=%.2fA)",
					eq.uid, urgency, resumeIntensity));

				// Resume equipment
				boolean success = eq.port.resume();

				if (success) {
					eq.suspended = false;
					eq.currentMode = eq.priorSuspendMode;
					remaining -= resumeIntensity;
					resumedCount++;

					log(String.format(
						"[CONTROL LOOP] ✓ Resumed %s, remaining surplus: %.2fA",
						eq.uid, remaining));
				} else {
					log(String.format(
						"[CONTROL LOOP] ✗ Failed to resume %s", eq.uid));
				}
			} catch (Exception e) {
				log(String.format(
					"[CONTROL LOOP] Error resuming %s: %s", eq.uid, e.getMessage()));
			}
		}

		log(String.format(
			"[CONTROL LOOP] Resumed %d equipment, remaining surplus: %.2fA",
			resumedCount, remaining));

		// TODO Phase 4: Charge batteries with remaining surplus
		if (remaining > ACTION_THRESHOLD) {
			log(String.format(
				"[CONTROL LOOP] Surplus remains (%.2fA), battery charging possible (not implemented yet)",
				remaining));
		}
	}
}
