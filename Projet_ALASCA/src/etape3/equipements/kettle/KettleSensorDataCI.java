package etape3.equipements.kettle;

import java.util.concurrent.TimeUnit;

import etape3.equipements.kettle.sensor_data.KettleModeSensorData;
import etape3.equipements.kettle.sensor_data.KettleStateSensorData;
import etape3.equipements.kettle.sensor_data.KettleTemperatureSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component data interface <code>KettleSensorDataCI</code> declares the
 * pull and push interfaces to get and receive sensor data from the kettle
 * component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * In the pull protocol, the consumer component (controller) has the initiative
 * to call the producer (kettle) to get the data. In the push protocol, the
 * producer pushes data to the consumer at regular intervals.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public interface KettleSensorDataCI
extends		DataOfferedCI,
			DataRequiredCI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The component interface <code>KettleSensorCI</code> declares the common
	 * services used in pull mode to get sensor data from the kettle.
	 */
	public static interface KettleSensorCI
	extends		OfferedCI,
				RequiredCI
	{
		/**
		 * Return the current state of the kettle.
		 *
		 * @return				the current state sensor data.
		 * @throws Exception	<i>to do</i>.
		 */
		public KettleStateSensorData statePullSensor() throws Exception;

		/**
		 * Return the current mode of the kettle.
		 *
		 * @return				the current mode sensor data.
		 * @throws Exception	<i>to do</i>.
		 */
		public KettleModeSensorData modePullSensor() throws Exception;

		/**
		 * Return the current water temperature of the kettle.
		 *
		 * @return				the current temperature sensor data.
		 * @throws Exception	<i>to do</i>.
		 */
		public KettleTemperatureSensorData temperaturePullSensor()
		throws Exception;

		/**
		 * Start a sequence of sensor data pushes with the given period.
		 *
		 * @param controlPeriod	period at which the pushes must be made.
		 * @param tu			time unit in which {@code controlPeriod} is expressed.
		 * @throws Exception	<i>to do</i>.
		 */
		public void startSensorDataPush(
			long controlPeriod,
			TimeUnit tu
		) throws Exception;
	}

	/**
	 * The interface <code>KettleSensorRequiredPullCI</code> is the pull
	 * interface that a client component must require.
	 */
	public static interface KettleSensorRequiredPullCI
	extends		DataRequiredCI.PullCI,
				KettleSensorCI
	{
	}

	/**
	 * The interface <code>KettleSensorOfferedPullCI</code> is the pull
	 * interface that a server component must offer.
	 */
	public static interface KettleSensorOfferedPullCI
	extends		DataOfferedCI.PullCI,
				KettleSensorCI
	{
	}
}
// -----------------------------------------------------------------------------
