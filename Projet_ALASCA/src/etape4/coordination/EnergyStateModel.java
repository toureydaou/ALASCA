package etape4.coordination;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * The class <code>EnergyStateModel</code> implements a shared data model for
 * energy management coordination.
 *
 * <p>
 * This component implements the "data-centered coordination" pattern where:
 * <ul>
 * <li>All energy readings (production and consumption) are centralized</li>
 * <li>The energy balance is computed from a single source of truth</li>
 * <li>All controllers (HEM, equipment controllers) access the same data</li>
 * <li>Decisions are made collaboratively based on shared state</li>
 * </ul>
 * </p>
 *
 * <p>
 * The model tracks:
 * <ul>
 * <li>Total consumption from all equipment</li>
 * <li>Total production from all sources (solar, generator, batteries)</li>
 * <li>Energy balance (production - consumption)</li>
 * <li>Critical metrics (generator status, battery charge, etc.)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Thread-safety: All data is protected by read-write locks to support
 * concurrent access from multiple controllers.
 * </p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public class EnergyStateModel {

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** Unit for all power measurements (Watts) */
	protected static final String POWER_UNIT = "W";

	/** Unit for all current measurements (Amperes) */
	protected static final String CURRENT_UNIT = "A";

	/** Voltage in the system (220V) */
	protected static final double SYSTEM_VOLTAGE = 220.0;

	// -------------------------------------------------------------------------
	// Data Structure
	// -------------------------------------------------------------------------

	/**
	 * Immutable snapshot of the current energy state.
	 * Allows consistent reading of multiple values without locks.
	 */
	public static class EnergySnapshot {
		/** Timestamp when snapshot was taken */
		public final long timestamp;

		/** Total power consumption in Watts */
		public final double totalConsumptionWatts;

		/** Total power production in Watts */
		public final double totalProductionWatts;

		/** Energy balance in Watts (positive = surplus, negative = deficit) */
		public final double balanceWatts;

		/** Energy balance in Amperes */
		public final double balanceAmperes;

		/** Current consumption in Amperes */
		public final double consumptionAmperes;

		/** Current production in Amperes */
		public final double productionAmperes;

		/** Whether generator is currently running */
		public final boolean generatorRunning;

		/** Current battery charge level (0.0 to 1.0) */
		public final double batteryChargeLevel;

		/** Whether batteries are currently charging */
		public final boolean batteriesCharging;

		/** Solar panel production in Watts */
		public final double solarProductionWatts;

		public EnergySnapshot(
			double totalConsumptionWatts,
			double totalProductionWatts,
			boolean generatorRunning,
			double batteryChargeLevel,
			boolean batteriesCharging,
			double solarProductionWatts
		) {
			this.timestamp = System.currentTimeMillis();
			this.totalConsumptionWatts = totalConsumptionWatts;
			this.totalProductionWatts = totalProductionWatts;
			this.balanceWatts = totalProductionWatts - totalConsumptionWatts;
			this.consumptionAmperes = totalConsumptionWatts / SYSTEM_VOLTAGE;
			this.productionAmperes = totalProductionWatts / SYSTEM_VOLTAGE;
			this.balanceAmperes = this.balanceWatts / SYSTEM_VOLTAGE;
			this.generatorRunning = generatorRunning;
			this.batteryChargeLevel = batteryChargeLevel;
			this.batteriesCharging = batteriesCharging;
			this.solarProductionWatts = solarProductionWatts;
		}

		@Override
		public String toString() {
			return String.format(
				"EnergySnapshot{consumption=%.0fW(%.2fA), production=%.0fW(%.2fA), balance=%.2fA, " +
				"gen=%s, bat=%.0f%%(charging=%s), solar=%.0fW}",
				totalConsumptionWatts, consumptionAmperes,
				totalProductionWatts, productionAmperes,
				balanceAmperes,
				generatorRunning ? "ON" : "OFF",
				batteryChargeLevel * 100,
				batteriesCharging ? "yes" : "no",
				solarProductionWatts);
		}
	}

	// -------------------------------------------------------------------------
	// Fields
	// -------------------------------------------------------------------------

	/** Lock for thread-safe access to state */
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/** Latest energy snapshot */
	private AtomicReference<EnergySnapshot> currentSnapshot;

	/** Total consumption in Watts */
	private double totalConsumptionWatts;

	/** Total production in Watts */
	private double totalProductionWatts;

	/** Solar panel production in Watts */
	private double solarProductionWatts;

	/** Generator status */
	private boolean generatorRunning;

	/** Battery state */
	private double batteryChargeLevel; // 0.0 to 1.0
	private boolean batteriesCharging;

	/** Listeners for state changes */
	private volatile EnergyStateListener listener;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Create a new EnergyStateModel.
	 */
	public EnergyStateModel() {
		this.totalConsumptionWatts = 0.0;
		this.totalProductionWatts = 0.0;
		this.solarProductionWatts = 0.0;
		this.generatorRunning = false;
		this.batteryChargeLevel = 0.5; // Start at 50%
		this.batteriesCharging = false;
		this.currentSnapshot = new AtomicReference<>(
			new EnergySnapshot(0.0, 0.0, false, 0.5, false, 0.0)
		);
	}

	// -------------------------------------------------------------------------
	// Data Update Methods
	// -------------------------------------------------------------------------

	/**
	 * Update total consumption from equipment.
	 *
	 * @param watts power consumption in Watts
	 */
	public void setConsumption(double watts) {
		lock.writeLock().lock();
		try {
			this.totalConsumptionWatts = Math.max(0.0, watts);
			updateSnapshot();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Update solar panel production.
	 *
	 * @param watts power production in Watts
	 */
	public void setSolarProduction(double watts) {
		lock.writeLock().lock();
		try {
			this.solarProductionWatts = Math.max(0.0, watts);
			this.totalProductionWatts = this.solarProductionWatts; // Base production
			updateSnapshot();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Update generator status.
	 *
	 * @param running true if generator is running
	 * @param generatorProduction power production in Watts (if running)
	 */
	public void setGeneratorRunning(boolean running, double generatorProduction) {
		lock.writeLock().lock();
		try {
			this.generatorRunning = running;
			if (running) {
				this.totalProductionWatts = this.solarProductionWatts + Math.max(0.0, generatorProduction);
			} else {
				this.totalProductionWatts = this.solarProductionWatts;
			}
			updateSnapshot();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Update battery state.
	 *
	 * @param batteryCharge current charge level (0.0 to 1.0)
	 * @param charging true if currently charging
	 */
	public void setBatteryState(double batteryCharge, boolean charging) {
		lock.writeLock().lock();
		try {
			this.batteryChargeLevel = Math.max(0.0, Math.min(1.0, batteryCharge));
			this.batteriesCharging = charging;
			updateSnapshot();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Set a listener for state changes.
	 *
	 * @param listener the listener
	 */
	public void setStateListener(EnergyStateListener listener) {
		this.listener = listener;
	}

	// -------------------------------------------------------------------------
	// Data Query Methods
	// -------------------------------------------------------------------------

	/**
	 * Get a consistent snapshot of the current energy state.
	 *
	 * @return snapshot of energy state
	 */
	public EnergySnapshot getSnapshot() {
		lock.readLock().lock();
		try {
			return currentSnapshot.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get current total consumption in Watts.
	 *
	 * @return consumption in Watts
	 */
	public double getConsumptionWatts() {
		lock.readLock().lock();
		try {
			return totalConsumptionWatts;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get current total consumption in Amperes.
	 *
	 * @return consumption in Amperes
	 */
	public double getConsumptionAmperes() {
		lock.readLock().lock();
		try {
			return totalConsumptionWatts / SYSTEM_VOLTAGE;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get current total production in Watts.
	 *
	 * @return production in Watts
	 */
	public double getProductionWatts() {
		lock.readLock().lock();
		try {
			return totalProductionWatts;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get current total production in Amperes.
	 *
	 * @return production in Amperes
	 */
	public double getProductionAmperes() {
		lock.readLock().lock();
		try {
			return totalProductionWatts / SYSTEM_VOLTAGE;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get current energy balance.
	 *
	 * @return balance in Amperes (positive = surplus, negative = deficit)
	 */
	public double getBalanceAmperes() {
		lock.readLock().lock();
		try {
			return (totalProductionWatts - totalConsumptionWatts) / SYSTEM_VOLTAGE;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get whether generator is running.
	 *
	 * @return true if generator is running
	 */
	public boolean isGeneratorRunning() {
		lock.readLock().lock();
		try {
			return generatorRunning;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get battery charge level.
	 *
	 * @return charge level (0.0 to 1.0)
	 */
	public double getBatteryChargeLevel() {
		lock.readLock().lock();
		try {
			return batteryChargeLevel;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get whether batteries are charging.
	 *
	 * @return true if charging
	 */
	public boolean areBatteriesCharging() {
		lock.readLock().lock();
		try {
			return batteriesCharging;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get solar production in Watts.
	 *
	 * @return solar production in Watts
	 */
	public double getSolarProductionWatts() {
		lock.readLock().lock();
		try {
			return solarProductionWatts;
		} finally {
			lock.readLock().unlock();
		}
	}

	// -------------------------------------------------------------------------
	// Private Methods
	// -------------------------------------------------------------------------

	/**
	 * Update the snapshot based on current state.
	 * Must be called while holding the write lock.
	 */
	private void updateSnapshot() {
		EnergySnapshot oldSnapshot = currentSnapshot.get();
		EnergySnapshot newSnapshot = new EnergySnapshot(
			totalConsumptionWatts,
			totalProductionWatts,
			generatorRunning,
			batteryChargeLevel,
			batteriesCharging,
			solarProductionWatts
		);
		currentSnapshot.set(newSnapshot);

		// Notify listener of significant changes
		if (listener != null && oldSnapshot != null) {
			double oldBalance = oldSnapshot.balanceWatts;
			double newBalance = newSnapshot.balanceWatts;

			// Notify on balance transitions (deficit/surplus/balanced)
			if ((oldBalance < 0 && newBalance >= 0) ||
				(oldBalance >= 0 && newBalance < 0)) {
				listener.onBalanceChange(newSnapshot);
			}

			// Notify on generator state changes
			if (oldSnapshot.generatorRunning != newSnapshot.generatorRunning) {
				listener.onGeneratorStatusChange(newSnapshot);
			}

			// Notify on battery charging state changes
			if (oldSnapshot.batteriesCharging != newSnapshot.batteriesCharging) {
				listener.onBatteryChargingChange(newSnapshot);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Listener Interface
	// -------------------------------------------------------------------------

	/**
	 * Interface for listening to energy state changes.
	 */
	public interface EnergyStateListener {
		/**
		 * Called when energy balance transitions (deficit/surplus/balanced).
		 *
		 * @param snapshot current energy snapshot
		 */
		void onBalanceChange(EnergySnapshot snapshot);

		/**
		 * Called when generator status changes.
		 *
		 * @param snapshot current energy snapshot
		 */
		void onGeneratorStatusChange(EnergySnapshot snapshot);

		/**
		 * Called when battery charging status changes.
		 *
		 * @param snapshot current energy snapshot
		 */
		void onBatteryChargingChange(EnergySnapshot snapshot);
	}
}
