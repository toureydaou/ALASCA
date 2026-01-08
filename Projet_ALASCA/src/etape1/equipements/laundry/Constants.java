package etape1.equipements.laundry;

// -----------------------------------------------------------------------------
/**
 * The class <code>Constants</code> defines all physical and electrical
 * constants for the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This class centralizes all numerical constants used by the laundry machine
 * component, including power levels, temperatures, capacities, and physical
 * parameters.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class Constants {

	// -------------------------------------------------------------------------
	// Electrical constants
	// -------------------------------------------------------------------------

	/** Machine voltage in volts. */
	public static final double MACHINE_VOLTAGE = 220.0;

	/** Maximum power consumption in watts. */
	public static final double MAX_POWER_CONSUMPTION = 2500.0;

	/** Minimum power consumption in watts (standby/off). */
	public static final double MIN_POWER_CONSUMPTION = 0.0;

	/** Nominal power consumption in watts. */
	public static final double NOMINAL_POWER_CONSUMPTION = 2000.0;

	// -------------------------------------------------------------------------
	// Wash mode power consumption (aligned with XML descriptor)
	// -------------------------------------------------------------------------

	/** Power consumption for DELICATE mode (mode 0) in watts. */
	public static final double DELICATE_MODE_POWER = 500.0;

	/** Power consumption for COLOR mode (mode 1) in watts. */
	public static final double COLOR_MODE_POWER = 1000.0;

	/** Power consumption for WHITE mode (mode 2) in watts. */
	public static final double WHITE_MODE_POWER = 1500.0;

	/** Power consumption for INTENSIVE mode (mode 3) in watts. */
	public static final double INTENSIVE_MODE_POWER = 2500.0;

	// -------------------------------------------------------------------------
	// Temperature constants
	// -------------------------------------------------------------------------

	/** Temperature for T_30 in Celsius. */
	public static final double TEMP_30 = 30.0;

	/** Temperature for T_40 in Celsius. */
	public static final double TEMP_40 = 40.0;

	/** Temperature for T_50 in Celsius. */
	public static final double TEMP_50 = 50.0;

	/** Temperature for T_60 in Celsius. */
	public static final double TEMP_60 = 60.0;

	/** Temperature for T_70 in Celsius. */
	public static final double TEMP_70 = 70.0;

	/** Temperature for T_80 in Celsius. */
	public static final double TEMP_80 = 80.0;

	/** Temperature for T_90 in Celsius. */
	public static final double TEMP_90 = 90.0;

	/** Maximum wash temperature in Celsius. */
	public static final double MAX_WASH_TEMPERATURE = 90.0;

	/** Minimum wash temperature in Celsius. */
	public static final double MIN_WASH_TEMPERATURE = 30.0;

	// -------------------------------------------------------------------------
	// Spin speed constants
	// -------------------------------------------------------------------------

	/** Spin speed for RPM_400 in rotations per minute. */
	public static final int SPIN_400 = 400;

	/** Spin speed for RPM_600 in rotations per minute. */
	public static final int SPIN_600 = 600;

	/** Spin speed for RPM_800 in rotations per minute. */
	public static final int SPIN_800 = 800;

	/** Spin speed for RPM_1000 in rotations per minute. */
	public static final int SPIN_1000 = 1000;

	/** Spin speed for RPM_1200 in rotations per minute. */
	public static final int SPIN_1200 = 1200;

	/** Spin speed for RPM_1400 in rotations per minute. */
	public static final int SPIN_1400 = 1400;

	/** Maximum spin speed in rotations per minute. */
	public static final int MAX_SPIN_SPEED = 1400;

	/** Minimum spin speed in rotations per minute. */
	public static final int MIN_SPIN_SPEED = 400;

	// -------------------------------------------------------------------------
	// Capacity constants
	// -------------------------------------------------------------------------

	/** Drum capacity in kilograms of laundry. */
	public static final double DRUM_CAPACITY_KG = 8.0;

	/** Water capacity in liters. */
	public static final double WATER_CAPACITY_LITERS = 50.0;

	/** Initial water level in liters. */
	public static final double INITIAL_WATER_LEVEL = 0.0;

	// -------------------------------------------------------------------------
	// Physical constants
	// -------------------------------------------------------------------------

	/** Water thermal capacity in J/(kg·°C). */
	public static final int WATER_THERMAL_CAPACITY = 4186;

	/** Number of seconds in one hour. */
	public static final int HOURS_IN_SECONDS = 3600;

	// -------------------------------------------------------------------------
	// Time constants
	// -------------------------------------------------------------------------

	/** Standard wash cycle duration in seconds. */
	public static final int WASH_CYCLE_DURATION = 2400; // 40 minutes

	/** Standard rinse cycle duration in seconds. */
	public static final int RINSE_CYCLE_DURATION = 600; // 10 minutes

	/** Standard spin cycle duration in seconds. */
	public static final int SPIN_CYCLE_DURATION = 480; // 8 minutes

	/** Standard drying cycle duration in seconds. */
	public static final int DRY_CYCLE_DURATION = 1800; // 30 minutes
}
