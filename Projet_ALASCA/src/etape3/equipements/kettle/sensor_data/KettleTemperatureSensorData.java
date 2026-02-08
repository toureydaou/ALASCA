package etape3.equipements.kettle.sensor_data;

import java.time.Instant;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleTemperatureSensorData</code> represents the water
 * temperature sensor data of the kettle (water heater).
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This class encapsulates the current water temperature of the kettle
 * in degrees Celsius.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleTemperatureSensorData
implements KettleSensorDataI
{
	private static final long serialVersionUID = 1L;

	/** The current water temperature in degrees Celsius. */
	protected final double temperature;
	/** The timestamp when this data was created. */
	protected final Instant timestamp;

	public KettleTemperatureSensorData(double temperature)
	{
		super();

		this.temperature = temperature;
		this.timestamp = Instant.now();
	}

	public double getTemperature()
	{
		return this.temperature;
	}

	public Instant getTimestamp()
	{
		return this.timestamp;
	}

	@Override
	public String toString()
	{
		return "KettleTemperatureSensorData[temperature=" + this.temperature +
			   "C, timestamp=" + this.timestamp + "]";
	}
}
// -----------------------------------------------------------------------------
