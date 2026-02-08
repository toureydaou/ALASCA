package etape3.equipements.kettle.sensor_data;

import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleCompoundSensorData</code> implements a compound sensor
 * data containing all sensor readings from the kettle component sent to the
 * controller component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This compound sensor data aggregates all individual sensor data from the
 * kettle into a single object for push mode transmission. It includes:
 * <ul>
 * <li>State: OFF, ON, or HEATING</li>
 * <li>Mode: SUSPEND, ECO, NORMAL, or MAX</li>
 * <li>Temperature: current water temperature in degrees Celsius</li>
 * </ul>
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleCompoundSensorData
implements KettleSensorDataI
{
	private static final long serialVersionUID = 1L;

	/** The state sensor data. */
	protected final KettleStateSensorData state;
	/** The mode sensor data. */
	protected final KettleModeSensorData mode;
	/** The temperature sensor data. */
	protected final KettleTemperatureSensorData temperature;

	public KettleCompoundSensorData(
		KettleStateSensorData state,
		KettleModeSensorData mode,
		KettleTemperatureSensorData temperature
	) {
		assert state != null :
			new PreconditionException("state != null");
		assert mode != null :
			new PreconditionException("mode != null");
		assert temperature != null :
			new PreconditionException("temperature != null");

		this.state = state;
		this.mode = mode;
		this.temperature = temperature;
	}

	public KettleStateSensorData getState()
	{
		return this.state;
	}

	public KettleModeSensorData getMode()
	{
		return this.mode;
	}

	public KettleTemperatureSensorData getTemperature()
	{
		return this.temperature;
	}

	@Override
	public String toString()
	{
		return "KettleCompoundSensorData[state=" + this.state +
			   ", mode=" + this.mode +
			   ", temperature=" + this.temperature + "]";
	}
}
// -----------------------------------------------------------------------------
