package etape3.equipements.laundry.sensor_data;

import java.time.Instant;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;

// -----------------------------------------------------------------------------
/**
 * The class <code>WashProgressSensorData</code> represents the wash progress
 * sensor data of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This class encapsulates the current progress of a wash cycle including
 * water temperature, water level, spin speed, and estimated remaining time.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code waterTemperature >= 0.0}
 * invariant	{@code waterLevel >= 0.0}
 * invariant	{@code spinSpeed != null}
 * invariant	{@code timestamp != null}
 * </pre>
 */
public class WashProgressSensorData
implements LaundrySensorDataI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The current water temperature in degrees Celsius. */
	protected final double waterTemperature;
	/** The current water level in liters. */
	protected final double waterLevel;
	/** The current spin speed. */
	protected final SpinSpeed spinSpeed;
	/** The current power consumption in watts. */
	protected final double powerConsumption;
	/** The timestamp when this data was created. */
	protected final Instant timestamp;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a new wash progress sensor data.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code waterTemperature >= 0.0}
	 * pre	{@code waterLevel >= 0.0}
	 * pre	{@code spinSpeed != null}
	 * pre	{@code powerConsumption >= 0.0}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param waterTemperature	the current water temperature.
	 * @param waterLevel		the current water level.
	 * @param spinSpeed			the current spin speed.
	 * @param powerConsumption	the current power consumption.
	 */
	public WashProgressSensorData(
		double waterTemperature,
		double waterLevel,
		SpinSpeed spinSpeed,
		double powerConsumption)
	{
		super();
		assert waterTemperature >= 0.0;
		assert waterLevel >= 0.0;
		assert spinSpeed != null;
		assert powerConsumption >= 0.0;

		this.waterTemperature = waterTemperature;
		this.waterLevel = waterLevel;
		this.spinSpeed = spinSpeed;
		this.powerConsumption = powerConsumption;
		this.timestamp = Instant.now();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * Get the current water temperature.
	 *
	 * @return the water temperature in degrees Celsius.
	 */
	public double getWaterTemperature()
	{
		return this.waterTemperature;
	}

	/**
	 * Get the current water level.
	 *
	 * @return the water level in liters.
	 */
	public double getWaterLevel()
	{
		return this.waterLevel;
	}

	/**
	 * Get the current spin speed.
	 *
	 * @return the spin speed.
	 */
	public SpinSpeed getSpinSpeed()
	{
		return this.spinSpeed;
	}

	/**
	 * Get the current power consumption.
	 *
	 * @return the power consumption in watts.
	 */
	public double getPowerConsumption()
	{
		return this.powerConsumption;
	}

	/**
	 * Get the timestamp when this data was created.
	 *
	 * @return the timestamp.
	 */
	public Instant getTimestamp()
	{
		return this.timestamp;
	}

	@Override
	public String toString()
	{
		return "WashProgressSensorData[waterTemp=" + this.waterTemperature +
			   "C, waterLevel=" + this.waterLevel +
			   "L, spinSpeed=" + this.spinSpeed +
			   ", power=" + this.powerConsumption +
			   "W, timestamp=" + this.timestamp + "]";
	}
}
// -----------------------------------------------------------------------------
