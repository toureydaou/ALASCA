package etape3.equipements.kettle;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape3.equipements.kettle.sensor_data.KettleTemperatureSensorData;

// -----------------------------------------------------------------------------
/**
 * The interface <code>KettlePushImplementationI</code> declares the signatures
 * of the methods to be implemented by the kettle controller to receive sensor
 * data from the kettle in push mode.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface must be implemented by the controller component that receives
 * push notifications from the kettle. The controller uses these methods to
 * process state changes and temperature updates.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public interface KettlePushImplementationI
{
	/**
	 * Receive and process the kettle state, starting the control loop if the
	 * state has changed from {@code OFF} to {@code ON} or {@code HEATING}.
	 *
	 * @param kettleState	kettle state received from the kettle component.
	 */
	public void processKettleState(KettleState kettleState);

	/**
	 * Receive and process the water temperature from the kettle.
	 *
	 * @param temperature	temperature data received from the kettle component.
	 */
	public void processKettleTemperature(
		KettleTemperatureSensorData temperature);
}
// -----------------------------------------------------------------------------
