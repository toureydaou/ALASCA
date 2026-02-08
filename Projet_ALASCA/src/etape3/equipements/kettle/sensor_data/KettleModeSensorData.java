package etape3.equipements.kettle.sensor_data;

import java.time.Instant;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleModeSensorData</code> represents the mode sensor data
 * of the kettle (water heater).
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This class encapsulates the current power mode of the kettle
 * (SUSPEND, ECO, NORMAL, MAX).
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleModeSensorData
implements KettleSensorDataI
{
	private static final long serialVersionUID = 1L;

	/** The current mode of the kettle. */
	protected final KettleMode mode;
	/** The timestamp when this data was created. */
	protected final Instant timestamp;

	public KettleModeSensorData(KettleMode mode)
	{
		super();
		assert mode != null;

		this.mode = mode;
		this.timestamp = Instant.now();
	}

	public KettleMode getMode()
	{
		return this.mode;
	}

	public Instant getTimestamp()
	{
		return this.timestamp;
	}

	@Override
	public String toString()
	{
		return "KettleModeSensorData[mode=" + this.mode +
			   ", timestamp=" + this.timestamp + "]";
	}
}
// -----------------------------------------------------------------------------
