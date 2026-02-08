package etape4.equipements.hem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import etape1.equipments.batteries.connections.BatteriesOutboundPort;
import etape1.equipments.generator.connections.GeneratorOutboundPort;
import etape1.equipments.meter.connections.ElectricMeterOutboundPort;
import etape1.equipments.solar_panel.connections.SolarPanelOutboundPort;
import etape4.coordination.EnergyStateModel;
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
 * <li>Start/stop generator when needed</li>
 * <li>Charge/discharge batteries</li>
 * </ul>
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

	/** Battery charge level threshold to start charging */
	public static final double BATTERY_CHARGE_THRESHOLD = 0.9;

	/** Battery charge level threshold for discharging */
	public static final double BATTERY_DISCHARGE_THRESHOLD = 0.5;

	// -------------------------------------------------------------------------
	// Variables
	// -------------------------------------------------------------------------

	/** Reference to owner component */
	private final AbstractComponent owner;

	/** Port to read meter data */
	private final ElectricMeterOutboundPort meterop;

	/** Port to generator */
	private final GeneratorOutboundPort generatorop;

	/** Port to batteries */
	private final BatteriesOutboundPort batteriesop;

	/** Port to solar panel */
	private final SolarPanelOutboundPort solarPanelop;

	/** Equipment registry */
	private final EquipmentRegistry registry;

	/** Shared energy state model (data-centered coordination) */
	private final EnergyStateModel energyStateModel;

	/** Whether to enable verbose logging */
	private final boolean verbose;

	/** Whether the generator is currently running */
	private boolean generatorRunning;

	/** Whether the batteries are currently charging */
	private boolean batteriesCharging;

	/** Flag to stop the task gracefully */
	private volatile boolean stopped = false;

	/** Set to false after first generator error to suppress repeated logs */
	private boolean generatorAvailable = true;

	/** Set to false after first battery error to suppress repeated logs */
	private boolean batteriesAvailable = true;

	/** Set to false after first solar panel error to suppress repeated logs */
	private boolean solarPanelAvailable = true;

	// -------------------------------------------------------------------------
	// Cumulative tracking for final report
	// -------------------------------------------------------------------------

	/** Total cumulative consumption in Watt-seconds */
	private double cumulativeConsumptionWs = 0.0;

	/** Total cumulative production in Watt-seconds */
	private double cumulativeProductionWs = 0.0;

	/** Per-equipment cumulative consumption in Watt-seconds */
	private final Map<String, Double> equipmentConsumptionWs = new HashMap<>();

	/** Per-equipment peak consumption in Watts */
	private final Map<String, Double> equipmentPeakConsumptionW = new HashMap<>();

	/** Per-source cumulative production in Watt-seconds */
	private final Map<String, Double> sourceProductionWs = new HashMap<>();

	/** Number of times each equipment was suspended */
	private final Map<String, Integer> equipmentSuspendCount = new HashMap<>();

	/** Number of control loop iterations */
	private int iterationCount = 0;

	/** Timestamp of first run (ms) */
	private long firstRunTimeMs = 0;

	/** Timestamp of last run (ms) */
	private long lastRunTimeMs = 0;

	/** Control loop period in seconds (for energy integration) */
	private double controlPeriodSeconds = 10.0;

	/** Earliest time (System.currentTimeMillis) when the task should start running */
	private long readyTimeMs = 0;

	/** Simulated generator production (Watts) when running in INTEGRATION_TEST mode.
	 *  In this mode, GeneratorCyPhy.currentPowerProduction() always returns 0.0W
	 *  because no SIL simulation is running, so we use a fixed estimate. */
	private static final double SIMULATED_GENERATOR_PRODUCTION_W = 4500.0;

	/** Default solar production (Watts) when the solar panel component is unavailable.
	 *  SolarPanelCyPhy returns FAKE_CURRENT_POWER_PRODUCTION=250W in test mode,
	 *  but getClock() may NPE before the clock is initialized. */
	private static final double DEFAULT_SOLAR_PRODUCTION_W = 250.0;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	public EnergyControlLoopTask(
		AbstractComponent owner,
		ElectricMeterOutboundPort meterop,
		GeneratorOutboundPort generatorop,
		BatteriesOutboundPort batteriesop,
		SolarPanelOutboundPort solarPanelop,
		EquipmentRegistry registry,
		EnergyStateModel energyStateModel,
		boolean verbose
	) {
		this.owner = owner;
		this.meterop = meterop;
		this.generatorop = generatorop;
		this.batteriesop = batteriesop;
		this.solarPanelop = solarPanelop;
		this.registry = registry;
		this.energyStateModel = energyStateModel;
		this.verbose = verbose;
		this.generatorRunning = false;
		this.batteriesCharging = false;
	}

	// -------------------------------------------------------------------------
	// Logging
	// -------------------------------------------------------------------------

	private void log(String message) {
		System.out.println(message);
		owner.logMessage(message);
	}

	// -------------------------------------------------------------------------
	// Control
	// -------------------------------------------------------------------------

	/**
	 * Set the control loop period for energy integration calculations.
	 */
	public void setControlPeriodSeconds(double seconds) {
		this.controlPeriodSeconds = seconds;
	}

	/**
	 * Set the earliest time when this task should start running.
	 * Iterations before this time are silently skipped.
	 *
	 * @param readyTimeMs earliest run time in System.currentTimeMillis
	 */
	public void setReadyTimeMs(long readyTimeMs) {
		this.readyTimeMs = readyTimeMs;
	}

	/**
	 * Stop this task gracefully. After calling this, subsequent run() calls
	 * will return immediately without accessing any ports.
	 * Prints the final energy report.
	 */
	public void stop() {
		this.stopped = true;
		printFinalReport();
	}

	/**
	 * Print the final energy management report.
	 */
	private void printFinalReport() {
		if (iterationCount == 0) return;

		double durationSeconds = (lastRunTimeMs - firstRunTimeMs) / 1000.0 + controlPeriodSeconds;
		double durationMinutes = durationSeconds / 60.0;

		// Convert Watt-seconds to Watt-hours
		double totalConsumptionWh = cumulativeConsumptionWs / 3600.0;
		double totalProductionWh = cumulativeProductionWs / 3600.0;
		double avgConsumptionW = cumulativeConsumptionWs / durationSeconds;
		double avgProductionW = cumulativeProductionWs / durationSeconds;

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("=======================================================================\n");
		sb.append("                    BILAN ENERGETIQUE FINAL                             \n");
		sb.append("=======================================================================\n");
		sb.append(String.format("  Duree de controle : %.1fs (%.1f min), %d iterations\n",
			durationSeconds, durationMinutes, iterationCount));
		sb.append("-----------------------------------------------------------------------\n");
		sb.append("  CONSOMMATION TOTALE\n");
		sb.append(String.format("    Energie consommee  : %10.2f Wh\n", totalConsumptionWh));
		sb.append(String.format("    Puissance moyenne  : %10.2f W  (%.2f A)\n",
			avgConsumptionW, avgConsumptionW / 220.0));
		sb.append("\n");
		sb.append("  PRODUCTION TOTALE\n");
		sb.append(String.format("    Energie produite   : %10.2f Wh\n", totalProductionWh));
		sb.append(String.format("    Puissance moyenne  : %10.2f W  (%.2f A)\n",
			avgProductionW, avgProductionW / 220.0));
		sb.append("\n");
		sb.append(String.format("  BILAN NET            : %+10.2f Wh (%s)\n",
			totalProductionWh - totalConsumptionWh,
			totalProductionWh >= totalConsumptionWh ? "EXCEDENT" : "DEFICIT"));
		sb.append("-----------------------------------------------------------------------\n");

		// Per-equipment consumption
		sb.append("  CONSOMMATION PAR EQUIPEMENT\n");
		if (equipmentConsumptionWs.isEmpty()) {
			sb.append("    (aucun equipement enregistre)\n");
		}
		for (Map.Entry<String, Double> entry : equipmentConsumptionWs.entrySet()) {
			String uid = entry.getKey();
			double eqWh = entry.getValue() / 3600.0;
			double peakW = equipmentPeakConsumptionW.getOrDefault(uid, 0.0);
			int suspensions = equipmentSuspendCount.getOrDefault(uid, 0);
			sb.append(String.format("    %-22s: %8.2f Wh (pic: %.0fW, suspensions: %d)\n",
				uid, eqWh, peakW, suspensions));
		}
		sb.append("-----------------------------------------------------------------------\n");

		// Per-source production
		sb.append("  PRODUCTION PAR SOURCE\n");
		if (sourceProductionWs.isEmpty()) {
			sb.append("    (aucune source de production detectee)\n");
		}
		for (Map.Entry<String, Double> entry : sourceProductionWs.entrySet()) {
			String source = entry.getKey();
			double srcWh = entry.getValue() / 3600.0;
			sb.append(String.format("    %-22s: %8.2f Wh\n", source, srcWh));
		}
		sb.append("=======================================================================\n");

		System.out.println(sb.toString());
	}

	// -------------------------------------------------------------------------
	// Task Execution
	// -------------------------------------------------------------------------

	@Override
	public void run() {
		if (this.stopped) {
			return;
		}
		// Skip iterations before the simulation clock is ready
		if (readyTimeMs > 0 && System.currentTimeMillis() < readyTimeMs) {
			return;
		}
		try {
			long now = System.currentTimeMillis();
			if (firstRunTimeMs == 0) firstRunTimeMs = now;
			lastRunTimeMs = now;
			iterationCount++;

			// 1. Read current state from meter
			SignalData<Double> consumption = meterop.getCurrentConsumption();
			SignalData<Double> production = meterop.getCurrentProduction();

			double consumptionAmperes = consumption.getMeasure().getData();
			double productionAmperes = production.getMeasure().getData();

			// Convert to Watts (220V system)
			double consumptionWatts = consumptionAmperes * 220.0;
			double productionWatts = productionAmperes * 220.0;

			// 2. If meter returns 0 (integration test without simulation),
			//    estimate consumption from registered equipment states
			//    and estimate production from energy sources
			if (consumptionAmperes == 0.0 && productionAmperes == 0.0) {
				consumptionWatts = estimateConsumptionFromEquipment() * 220.0;
				productionWatts = estimateProductionFromSources() * 220.0;
				consumptionAmperes = consumptionWatts / 220.0;
				productionAmperes = productionWatts / 220.0;
			}

			// 3. Accumulate energy for final report (Watt-seconds)
			cumulativeConsumptionWs += consumptionWatts * controlPeriodSeconds;
			cumulativeProductionWs += productionWatts * controlPeriodSeconds;

			// 4. Update shared energy state model (data-centered coordination)
			energyStateModel.setConsumption(consumptionWatts);
			if (batteriesAvailable) {
				try {
					if (batteriesop != null && batteriesop.connected()) {
						SignalData<Double> batteryCharge = batteriesop.chargeLevel();
						double chargeValue = batteryCharge.getMeasure().getData();
						energyStateModel.setBatteryState(chargeValue, batteriesCharging);
					}
				} catch (Exception e) {
					batteriesAvailable = false;
					log("[CONTROL LOOP] Batteries unavailable: " + e.getMessage());
				}
			}

			// 5. Calculate deficit/surplus (positive = deficit, negative = surplus)
			double balance = consumptionAmperes - productionAmperes;

			if (verbose) {
				log(String.format(
					"[CONTROL LOOP] Consumption: %.2fA, Production: %.2fA, Balance: %.2fA (gen=%s, bat=%s)",
					consumptionAmperes, productionAmperes, balance,
					generatorRunning ? "ON" : "OFF",
					batteriesCharging ? "CHARGING" : "IDLE"));
			}

			// 6. Make decision based on balance
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

				// Track per-equipment consumption (Watt-seconds)
				equipmentConsumptionWs.merge(eq.uid, watts * controlPeriodSeconds, Double::sum);
				equipmentPeakConsumptionW.merge(eq.uid, watts, Math::max);

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

	/**
	 * Estimate total production from energy sources (solar + generator).
	 *
	 * In INTEGRATION_TEST mode (no SIL simulation), energy source components
	 * may not be fully operational:
	 * - SolarPanelCyPhy.getCurrentPowerProductionLevel() may NPE if clock not ready
	 * - GeneratorCyPhy.currentPowerProduction() always returns 0.0W (no simulation)
	 * So we use simulated/default values when component queries fail or return 0.
	 */
	private double estimateProductionFromSources() {
		double totalWatts = 0.0;

		// Solar panel production
		if (solarPanelAvailable) {
			try {
				if (solarPanelop != null && solarPanelop.connected()) {
					SignalData<Double> solarProd = solarPanelop.getCurrentPowerProductionLevel();
					if (solarProd != null) {
						double solarW = solarProd.getMeasure().getData();
						totalWatts += solarW;
						sourceProductionWs.merge("Panneau Solaire", solarW * controlPeriodSeconds, Double::sum);
					}
				}
			} catch (Exception e) {
				// In INTEGRATION_TEST mode, the solar panel's getClock() may NPE
				// before initialization. Use a default production value.
				solarPanelAvailable = false;
				double solarW = DEFAULT_SOLAR_PRODUCTION_W;
				totalWatts += solarW;
				sourceProductionWs.merge("Panneau Solaire (estimé)", solarW * controlPeriodSeconds, Double::sum);
				log(String.format(
					"[CONTROL LOOP]   Solar panel query failed (%s), using default: %.0fW",
					e.getMessage(), solarW));
			}
		} else {
			// Solar panel marked unavailable but still producing (default estimate)
			double solarW = DEFAULT_SOLAR_PRODUCTION_W;
			totalWatts += solarW;
			sourceProductionWs.merge("Panneau Solaire (estimé)", solarW * controlPeriodSeconds, Double::sum);
		}

		// Generator production (if running)
		if (generatorRunning) {
			// In INTEGRATION_TEST mode, GeneratorCyPhy.currentPowerProduction()
			// always returns 0.0W because no SIL model is running.
			// Use a simulated production value instead.
			double genW = 0.0;
			try {
				if (generatorop != null && generatorop.connected()) {
					SignalData<Double> genProd = generatorop.currentPowerProduction();
					if (genProd != null) {
						genW = genProd.getMeasure().getData();
					}
				}
			} catch (Exception e) {
				// Ignore query errors
			}
			// If component returns 0 (INTEGRATION_TEST mode), use simulated value
			if (genW < 1.0) {
				genW = SIMULATED_GENERATOR_PRODUCTION_W;
			}
			totalWatts += genW;
			sourceProductionWs.merge("Generateur", genW * controlPeriodSeconds, Double::sum);
		}

		double totalAmperes = totalWatts / 220.0;
		if (verbose && totalAmperes > 0) {
			log(String.format(
				"[CONTROL LOOP]   Total estimated production: %.2fW (%.2fA)",
				totalWatts, totalAmperes));
		}

		return totalAmperes;
	}

	// -------------------------------------------------------------------------
	// Control Strategies
	// -------------------------------------------------------------------------

	/**
	 * Handle deficit situation (consumption > production).
	 *
	 * Strategy:
	 * 1. Suspend equipment by priority (low priority first)
	 * 2. If still insufficient, start generator
	 * 3. If batteries > 50%, discharge to help
	 */
	private void handleDeficit(double deficit, double consumptionAmperes, double productionAmperes) throws Exception {
		log(String.format(
			"[CONTROL LOOP] DEFICIT detected: %.2fA (consumption: %.2fA, production: %.2fA)",
			deficit, consumptionAmperes, productionAmperes));

		// Step 1: Suspend equipment by priority (low priority first)
		List<EquipmentInfo> suspendable = registry.getSuspendableEquipment();
		double remaining = deficit;
		int suspendedCount = 0;

		if (!suspendable.isEmpty()) {
			log(String.format("[CONTROL LOOP] Found %d suspendable equipment", suspendable.size()));

			for (EquipmentInfo eq : suspendable) {
				if (remaining <= ACTION_THRESHOLD) break;

				try {
					// Query current mode from port (eq.currentMode may be uninitialized at 0)
					int mode = eq.port.currentMode();
					eq.currentMode = mode;
					double currentConsumption = eq.getModeConsumption(mode);
					double currentIntensity = currentConsumption / 220.0;

					log(String.format(
						"[CONTROL LOOP] Attempting to suspend %s (priority=%d, mode=%d, consumption=%.2fA)",
						eq.uid, eq.priority, mode, currentIntensity));

					boolean success = eq.port.suspend();
					if (success) {
						eq.suspended = true;
						eq.priorSuspendMode = mode;
						remaining -= currentIntensity;
						suspendedCount++;
						equipmentSuspendCount.merge(eq.uid, 1, Integer::sum);

						log(String.format(
							"[CONTROL LOOP] SUSPENDED %s, remaining deficit: %.2fA",
							eq.uid, remaining));
					} else {
						log(String.format(
							"[CONTROL LOOP] Failed to suspend %s", eq.uid));
					}
				} catch (Exception e) {
					log(String.format(
						"[CONTROL LOOP] Error suspending %s: %s", eq.uid, e.getMessage()));
				}
			}
		}

		// Step 2: Start generator if deficit still large
		if (remaining > GENERATOR_START_THRESHOLD && !generatorRunning) {
			boolean started = false;
			// Try to start the actual generator component
			if (generatorAvailable) {
				try {
					if (generatorop != null && generatorop.connected()) {
						generatorop.startGenerator();
						started = true;
						log("[CONTROL LOOP] GENERATOR STARTED (component) to cover deficit");
					}
				} catch (Exception e) {
					generatorAvailable = false;
					log(String.format(
						"[CONTROL LOOP] Generator component unavailable (%s), using simulated mode",
						e.getMessage()));
					// Even if the component fails, we can still "start" the generator
					// in simulated mode for the control loop's production estimation
					started = true;
				}
			} else {
				// Generator component unavailable but we can simulate it
				started = true;
			}
			if (started) {
				generatorRunning = true;
				energyStateModel.setGeneratorRunning(true, SIMULATED_GENERATOR_PRODUCTION_W);
				log(String.format(
					"[CONTROL LOOP] GENERATOR RUNNING (production: %.0fW = %.2fA)",
					SIMULATED_GENERATOR_PRODUCTION_W, SIMULATED_GENERATOR_PRODUCTION_W / 220.0));
			}
		}

		// Step 3: Check batteries for discharge
		if (remaining > ACTION_THRESHOLD && batteriesAvailable) {
			try {
				if (batteriesop != null && batteriesop.connected()) {
					double chargeLevel = batteriesop.chargeLevel().getMeasure().getData();
					if (chargeLevel > BATTERY_DISCHARGE_THRESHOLD) {
						log(String.format(
							"[CONTROL LOOP] Battery charge level: %.1f%% - available for discharge",
							chargeLevel * 100));
					}
				}
			} catch (Exception e) {
				// Ignore battery read errors
			}
		}

		log(String.format(
			"[CONTROL LOOP] Deficit handling complete: suspended %d, remaining deficit: %.2fA, generator: %s",
			suspendedCount, remaining, generatorRunning ? "ON" : "OFF"));
	}

	/**
	 * Handle surplus situation (production > consumption).
	 *
	 * Strategy:
	 * 1. Stop generator if running
	 * 2. Resume suspended equipment by urgency (high emergency first)
	 * 3. Charge batteries with remaining surplus
	 */
	private void handleSurplus(double surplus, double consumptionAmperes, double productionAmperes) throws Exception {
		log(String.format(
			"[CONTROL LOOP] SURPLUS detected: %.2fA (consumption: %.2fA, production: %.2fA)",
			surplus, consumptionAmperes, productionAmperes));

		double remaining = surplus;

		// Step 1: Stop generator if running
		// When stopping the generator, the surplus shrinks by the generator's
		// production output. Adjust 'remaining' accordingly.
		if (generatorRunning) {
			double generatorProductionA = SIMULATED_GENERATOR_PRODUCTION_W / 220.0;
			if (generatorAvailable) {
				try {
					if (generatorop != null && generatorop.connected()) {
						generatorop.stopGenerator();
						log("[CONTROL LOOP] GENERATOR STOPPED (component)");
					}
				} catch (Exception e) {
					log("[CONTROL LOOP] Error stopping generator component: " + e.getMessage());
				}
			}
			generatorRunning = false;
			energyStateModel.setGeneratorRunning(false, 0.0);
			remaining -= generatorProductionA;
			if (remaining < 0) remaining = 0;
			log(String.format(
				"[CONTROL LOOP] GENERATOR STOPPED, remaining surplus after removing generator production: %.2fA",
				remaining));
		}

		// Step 2: Resume suspended equipment by urgency
		List<EquipmentInfo> suspended = registry.getSuspendedEquipment();

		if (!suspended.isEmpty()) {
			log(String.format("[CONTROL LOOP] Found %d suspended equipment", suspended.size()));

			// Sort by emergency (highest urgency first)
			suspended.sort((e1, e2) -> {
				try {
					return Double.compare(e2.emergency(), e1.emergency());
				} catch (Exception ex) {
					return 0;
				}
			});

			int resumedCount = 0;
			for (EquipmentInfo eq : suspended) {
				if (remaining <= ACTION_THRESHOLD) break;

				try {
					// Ensure priorSuspendMode is valid (> 0)
					int resumeMode = eq.priorSuspendMode > 0
						? eq.priorSuspendMode : eq.port.currentMode();
					double resumeConsumption = eq.getModeConsumption(resumeMode);
					double resumeIntensity = resumeConsumption / 220.0;

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

					boolean success = eq.port.resume();
					if (success) {
						eq.suspended = false;
						eq.currentMode = eq.priorSuspendMode;
						remaining -= resumeIntensity;
						resumedCount++;

						log(String.format(
							"[CONTROL LOOP] RESUMED %s, remaining surplus: %.2fA",
							eq.uid, remaining));
					} else {
						log(String.format(
							"[CONTROL LOOP] Failed to resume %s", eq.uid));
					}
				} catch (Exception e) {
					log(String.format(
						"[CONTROL LOOP] Error resuming %s: %s", eq.uid, e.getMessage()));
				}
			}

			log(String.format("[CONTROL LOOP] Resumed %d equipment", resumedCount));
		}

		// Step 3: Charge batteries with remaining surplus
		if (remaining > ACTION_THRESHOLD && batteriesAvailable) {
			try {
				if (batteriesop != null && batteriesop.connected()) {
					double chargeLevel = batteriesop.chargeLevel().getMeasure().getData();
					if (chargeLevel < BATTERY_CHARGE_THRESHOLD && !batteriesCharging) {
						batteriesop.startCharging();
						batteriesCharging = true;
						// Update shared energy state
						energyStateModel.setBatteryState(chargeLevel, true);
						log(String.format(
							"[CONTROL LOOP] BATTERY CHARGING started (level: %.1f%%, surplus: %.2fA)",
							chargeLevel * 100, remaining));
					} else if (chargeLevel >= BATTERY_CHARGE_THRESHOLD && batteriesCharging) {
						batteriesop.stopCharging();
						batteriesCharging = false;
						// Update shared energy state
						energyStateModel.setBatteryState(chargeLevel, false);
						log(String.format(
							"[CONTROL LOOP] BATTERY CHARGING stopped (level: %.1f%% - full)",
							chargeLevel * 100));
					}
				}
			} catch (Exception e) {
				log("[CONTROL LOOP] Error managing batteries: " + e.getMessage());
			}
		}
	}
}
