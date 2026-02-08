package etape3.equipements.kettle.sensor_data;

import java.time.Instant;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleStateSensorData</code> represents the state sensor data
 * of the kettle (water heater).
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This class encapsulates the current state of the kettle.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleStateSensorData
implements KettleSensorDataI
{
	private static final long serialVersionUID = 1L;

	/** The current state of the kettle. */
	protected final KettleState state;
	/** The timestamp when this data was created. */
	protected final Instant timestamp;

	public KettleStateSensorData(KettleState state)
	{
		super();
		assert state != null;

		this.state = state;
		this.timestamp = Instant.now();
	}

	public KettleState getState()
	{
		return this.state;
	}

	public Instant getTimestamp()
	{
		return this.timestamp;
	}

	@Override
	public String toString()
	{
		return "KettleStateSensorData[state=" + this.state +
			   ", timestamp=" + this.timestamp + "]";
	}
}
// -----------------------------------------------------------------------------
