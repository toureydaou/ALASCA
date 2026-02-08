package etape4.control;

/**
 * The class <code>PriorityConfig</code> defines static priority levels for
 * different equipment types in the energy management system.
 *
 * <p>
 * Priority levels range from 1 (highest priority, least likely to suspend) to
 * 10 (lowest priority, first to suspend).
 * </p>
 *
 * <p>
 * <strong>Priority Levels:</strong>
 * </p>
 * <ul>
 * <li>1-3: Critical equipment (medical, refrigerator - future)</li>
 * <li>4: Laundry during wash cycle (cannot interrupt)</li>
 * <li>5: Coffee machine while heating</li>
 * <li>6: Coffee machine idle</li>
 * <li>7-8: Fan (by mode)</li>
 * <li>9-10: Non-essential (lighting, entertainment - future)</li>
 * </ul>
 *
 * @author Jacques Malenfant, Sorbonne Universite
 */
public class PriorityConfig {

	// -------------------------------------------------------------------------
	// Equipment Type Priorities
	// -------------------------------------------------------------------------

	/** Coffee machine base priority (idle state) */
	public static final int COFFEE_MACHINE_PRIORITY = 6;

	/** Coffee machine priority when heating (higher priority) */
	public static final int COFFEE_MACHINE_HEATING_PRIORITY = 5;

	/** Fan base priority */
	public static final int FAN_PRIORITY = 7;

	/** Laundry machine base priority (standby/off) */
	public static final int LAUNDRY_PRIORITY = 4;

	/** Laundry machine priority during wash (cannot suspend) */
	public static final int LAUNDRY_WASHING_PRIORITY = 3;

	/** Generator priority (production unit, not suspendable) */
	public static final int GENERATOR_PRIORITY = 1;

	/** Batteries priority (production unit, not suspendable) */
	public static final int BATTERIES_PRIORITY = 1;

	/** Solar panel priority (production unit, not suspendable) */
	public static final int SOLAR_PANEL_PRIORITY = 1;

	/** Electric meter priority (infrastructure, not suspendable) */
	public static final int ELECTRIC_METER_PRIORITY = 1;

	// Future equipment priorities
	public static final int REFRIGERATOR_PRIORITY = 2;
	public static final int MEDICAL_EQUIPMENT_PRIORITY = 1;
	public static final int HEATING_COOLING_PRIORITY = 3;
	public static final int LIGHTING_DECORATIVE_PRIORITY = 9;
	public static final int ENTERTAINMENT_PRIORITY = 10;

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * Get base priority for an equipment type.
	 *
	 * @param equipmentType type identifier (e.g., "CoffeeMachine")
	 * @return priority level (1-10)
	 */
	public static int getBasePriority(String equipmentType) {
		switch (equipmentType) {
			case "CoffeeMachine":
				return COFFEE_MACHINE_PRIORITY;
			case "Fan":
				return FAN_PRIORITY;
			case "LaundryMachine":
			case "Laundry":
				return LAUNDRY_PRIORITY;
			case "Generator":
				return GENERATOR_PRIORITY;
			case "Batteries":
				return BATTERIES_PRIORITY;
			case "SolarPanel":
				return SOLAR_PANEL_PRIORITY;
			case "ElectricMeter":
				return ELECTRIC_METER_PRIORITY;
			// Future equipment
			case "Refrigerator":
				return REFRIGERATOR_PRIORITY;
			case "MedicalEquipment":
				return MEDICAL_EQUIPMENT_PRIORITY;
			case "HeatingCooling":
				return HEATING_COOLING_PRIORITY;
			case "Lighting":
				return LIGHTING_DECORATIVE_PRIORITY;
			case "Entertainment":
				return ENTERTAINMENT_PRIORITY;
			default:
				// Default to medium priority for unknown equipment
				return 5;
		}
	}

	/**
	 * Check if an equipment type is suspendable (consumer equipment only).
	 *
	 * @param equipmentType type identifier
	 * @return true if equipment can be suspended
	 */
	public static boolean isSuspendable(String equipmentType) {
		switch (equipmentType) {
			// Non-suspendable: production and infrastructure
			case "Generator":
			case "Batteries":
			case "SolarPanel":
			case "ElectricMeter":
				return false;
			// Suspendable: all consumer equipment
			default:
				return true;
		}
	}

	/**
	 * Get description of priority level.
	 *
	 * @param priority priority level (1-10)
	 * @return human-readable description
	 */
	public static String getPriorityDescription(int priority) {
		if (priority <= 2) {
			return "Critical (never suspend)";
		} else if (priority <= 4) {
			return "High (suspend only in emergency)";
		} else if (priority <= 6) {
			return "Medium (suspend when needed)";
		} else if (priority <= 8) {
			return "Low (suspend readily)";
		} else {
			return "Very Low (suspend first)";
		}
	}
}
