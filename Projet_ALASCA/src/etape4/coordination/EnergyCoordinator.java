package etape4.coordination;

import etape4.coordination.EnergyStateModel.EnergySnapshot;

/**
 * The interface <code>EnergyCoordinator</code> defines the coordination contract
 * for energy management controllers.
 *
 * <p>
 * This interface implements the "controller collaboration" pattern where:
 * <ul>
 * <li>HEM controller makes high-level decisions (which equipment to control)</li>
 * <li>Equipment controllers query the shared energy state before acting</li>
 * <li>All decisions are driven by a common understanding of energy balance</li>
 * </ul>
 * </p>
 *
 * <p>
 * Controllers (both HEM and individual equipment) use this interface to:
 * <ol>
 * <li>Query the current energy state (snapshot)</li>
 * <li>Check if actions (e.g., suspend, increase power) are allowed</li>
 * <li>Notify other controllers when they take action</li>
 * </ol>
 * </p>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public interface EnergyCoordinator {

	// -------------------------------------------------------------------------
	// Data Access
	// -------------------------------------------------------------------------

	/**
	 * Get the current energy state snapshot.
	 *
	 * <p>
	 * This is the single source of truth for all energy decisions.
	 * The snapshot provides a consistent view of all energy metrics.
	 * </p>
	 *
	 * @return current energy snapshot
	 */
	EnergySnapshot getEnergySnapshot();

	/**
	 * Get the underlying energy state model (for advanced access).
	 *
	 * @return energy state model
	 */
	EnergyStateModel getEnergyStateModel();

	// -------------------------------------------------------------------------
	// Coordination Decisions
	// -------------------------------------------------------------------------

	/**
	 * Check if an equipment can be turned on without causing deficit.
	 *
	 * <p>
	 * This allows equipment controllers to make smart decisions about
	 * when to activate or increase power.
	 * </p>
	 *
	 * @param requiredPowerWatts power needed in Watts
	 * @return true if turning on is allowed (surplus > required power)
	 */
	boolean canTurnOn(double requiredPowerWatts);

	/**
	 * Check if an equipment should suspend due to energy deficit.
	 *
	 * <p>
	 * Equipment can query whether the HEM has determined that it should
	 * suspend. This supports proactive suspension by equipment.
	 * </p>
	 *
	 * @param equipmentId equipment identifier
	 * @return true if equipment should suspend
	 */
	boolean shouldSuspend(String equipmentId);

	/**
	 * Check if an equipment can resume operation.
	 *
	 * <p>
	 * Equipment that has been suspended can check if it's now safe to resume.
	 * </p>
	 *
	 * @param equipmentId equipment identifier
	 * @param requiredPowerWatts power needed to resume in Watts
	 * @return true if equipment can resume
	 */
	boolean canResume(String equipmentId, double requiredPowerWatts);

	/**
	 * Check if the system is in critical deficit (very low margin).
	 *
	 * @return true if system is in critical state
	 */
	boolean isCriticalDeficit();

	/**
	 * Check if the system is in surplus state.
	 *
	 * @return true if system has surplus production
	 */
	boolean isSurplus();

	/**
	 * Get the recommended action for the current energy state.
	 *
	 * <p>
	 * This provides guidance to controllers on what actions to take:
	 * <ul>
	 * <li>"NONE" - system balanced, no action needed</li>
	 * <li>"SUSPEND" - need to suspend some equipment (deficit)</li>
	 * <li>"RESUME" - can resume some equipment (surplus)</li>
	 * <li>"GENERATOR_START" - critical deficit, start generator</li>
	 * <li>"GENERATOR_STOP" - surplus available, stop generator</li>
	 * <li>"BATTERY_CHARGE" - sufficient surplus, charge batteries</li>
	 * <li>"BATTERY_DISCHARGE" - deficit and battery available</li>
	 * </ul>
	 * </p>
	 *
	 * @return recommended action string
	 */
	String getRecommendedAction();

	// -------------------------------------------------------------------------
	// Collaboration Notifications
	// -------------------------------------------------------------------------

	/**
	 * Notify coordinator that an equipment has changed its power consumption.
	 *
	 * <p>
	 * When equipment changes mode or state, it notifies the coordinator
	 * so that the energy state model can be updated.
	 * </p>
	 *
	 * @param equipmentId equipment identifier
	 * @param oldConsumptionWatts previous consumption in Watts
	 * @param newConsumptionWatts new consumption in Watts
	 */
	void notifyConsumptionChange(String equipmentId, double oldConsumptionWatts, double newConsumptionWatts);

	/**
	 * Notify coordinator that an energy source has changed production.
	 *
	 * @param sourceId source identifier (e.g., "SOLAR", "GENERATOR")
	 * @param productionWatts production in Watts
	 */
	void notifyProductionChange(String sourceId, double productionWatts);

	/**
	 * Notify coordinator that equipment has been suspended.
	 *
	 * @param equipmentId equipment identifier
	 */
	void notifySuspended(String equipmentId);

	/**
	 * Notify coordinator that equipment has been resumed.
	 *
	 * @param equipmentId equipment identifier
	 */
	void notifyResumed(String equipmentId);

	// -------------------------------------------------------------------------
	// Thresholds and Configuration
	// -------------------------------------------------------------------------

	/**
	 * Get the action threshold for deficit/surplus detection.
	 *
	 * <p>
	 * The control loop only acts when the balance exceeds this threshold
	 * to avoid oscillating between actions.
	 * </p>
	 *
	 * @return threshold in Amperes
	 */
	double getActionThreshold();

	/**
	 * Get the threshold for starting the generator.
	 *
	 * @return threshold in Amperes
	 */
	double getGeneratorStartThreshold();

	/**
	 * Get the battery charge level threshold for starting charging.
	 *
	 * @return threshold (0.0 to 1.0)
	 */
	double getBatteryChargeThreshold();
}
