package etape1.equipements.kettle.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;

// -----------------------------------------------------------------------------
/**
 * The interface <code>KettleImplementationI</code> defines the base types
 * and state accessors for the water heater (chauffe-eau) component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The water heater is a "non-programmable" appliance that heats water in a
 * tank to a target temperature. It can be controlled remotely to modulate
 * its maximum power consumption. The temperature evolution follows a
 * differential equation (simulated in later stages).
 * </p>
 *
 * <p>Created on : 2023-09-18</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface KettleImplementationI {

	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>KettleState</code> describes the operation
	 * states of the water heater.
	 */
	public static enum KettleState {
		/** Water heater is off. */
		OFF,
		/** Water heater is on, in standby (maintaining temperature or idle). */
		ON,
		/** Water heater is actively heating water to reach target temperature. */
		HEATING
	}

	/**
	 * The enumeration <code>KettleMode</code> describes the power modes
	 * of the water heater for HEM energy management.
	 */
	public static enum KettleMode {
		/** Suspended by HEM, no power consumption (0W). */
		SUSPEND,
		/** Economy mode, slow heating (1000W). */
		ECO,
		/** Normal mode, standard heating (2000W). */
		NORMAL,
		/** Maximum mode, fast heating (3000W). */
		MAX
	}

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** Temperature measurement unit. */
	public static final MeasurementUnit TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;

	/** Power measurement unit. */
	public static final MeasurementUnit POWER_UNIT = MeasurementUnit.WATTS;

	/** Minimum target temperature in Celsius. */
	public static final double MIN_TARGET_TEMPERATURE = 30.0;

	/** Maximum target temperature in Celsius. */
	public static final double MAX_TARGET_TEMPERATURE = 80.0;

	/** Default target temperature in Celsius. */
	public static final double DEFAULT_TARGET_TEMPERATURE = 55.0;

	/** Tank capacity in liters. */
	public static final double TANK_CAPACITY_LITERS = 200.0;

	/** Power consumption in SUSPEND mode (W). */
	public static final double SUSPEND_MODE_POWER = 0.0;

	/** Power consumption in ECO mode (W). */
	public static final double ECO_MODE_POWER = 1000.0;

	/** Power consumption in NORMAL mode (W). */
	public static final double NORMAL_MODE_POWER = 2000.0;

	/** Power consumption in MAX mode (W). */
	public static final double MAX_MODE_POWER = 3000.0;

	/** Maximum power level (W). */
	public static final double MAX_POWER_LEVEL = 3000.0;

	/** Machine voltage (V). */
	public static final double MACHINE_VOLTAGE = 220.0;

	// -------------------------------------------------------------------------
	// Component services signatures
	// -------------------------------------------------------------------------

	/**
	 * return the current state of the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the current state of the water heater.
	 * @throws Exception 	<i>to do</i>.
	 */
	public KettleState getState() throws Exception;

	/**
	 * return the current power mode of the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() != KettleState.OFF}
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the current power mode of the water heater.
	 * @throws Exception 	<i>to do</i>.
	 */
	public KettleMode getKettleMode() throws Exception;

	/**
	 * return the target temperature of the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the target temperature.
	 * @throws Exception 	<i>to do</i>.
	 */
	public Measure<Double> getTargetTemperature() throws Exception;

	/**
	 * return the current water temperature (fake value without simulation).
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the current water temperature.
	 * @throws Exception 	<i>to do</i>.
	 */
	public Measure<Double> getCurrentTemperature() throws Exception;

	/**
	 * return true if the water heater is actively heating.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the water heater is heating.
	 * @throws Exception 	<i>to do</i>.
	 */
	public boolean isHeating() throws Exception;
}
