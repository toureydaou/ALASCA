package etape3.equipements.laundry;

import java.util.concurrent.TimeUnit;

import etape3.equipements.laundry.sensor_data.LaundryStateSensorData;
import etape3.equipements.laundry.sensor_data.WashProgressSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component data interface <code>LaundrySensorDataCI</code> declares the pull
 * and the push interfaces to get and receive sensor data from the laundry
 * component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * BCM4Java data oriented interfaces provide means to communicate between
 * components to exchange data rather than calling services. To achieve this,
 * the standard BCM4Java {@code DataOfferedCI} and {@code DataRequiredCI}
 * defines two protocols for the exchange of data: a pull protocol and a push
 * protocol.
 * </p>
 * <p>
 * In the pull protocol, the consumer component has the initiative to call the
 * producer to get the data. In the push protocol, the producer component has
 * the initiative to push the data to the consumer.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 */
public interface LaundrySensorDataCI
extends		DataOfferedCI,
			DataRequiredCI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The component interface <code>LaundrySensorCI</code> declares the common
	 * services used in pull mode to get the sensor data from the laundry.
	 */
	public static interface LaundrySensorCI
	extends		OfferedCI,
				RequiredCI
	{
		// ---------------------------------------------------------------------
		// Methods
		// ---------------------------------------------------------------------

		/**
		 * Return the current state of the laundry machine.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code true}
		 * post	{@code return != null}
		 * </pre>
		 *
		 * @return				the current state sensor data.
		 * @throws Exception	<i>to do</i>.
		 */
		public LaundryStateSensorData statePullSensor() throws Exception;

		/**
		 * Return the current wash progress data.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code isOn()}
		 * post	{@code return != null}
		 * </pre>
		 *
		 * @return				the current wash progress sensor data.
		 * @throws Exception	<i>to do</i>.
		 */
		public WashProgressSensorData washProgressPullSensor() throws Exception;

		/**
		 * Return the current water temperature.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code isOn()}
		 * post	{@code return >= 0.0}
		 * </pre>
		 *
		 * @return				the current water temperature in degrees Celsius.
		 * @throws Exception	<i>to do</i>.
		 */
		public double waterTemperaturePullSensor() throws Exception;

		/**
		 * Return the current water level.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code isOn()}
		 * post	{@code return >= 0.0}
		 * </pre>
		 *
		 * @return				the current water level in liters.
		 * @throws Exception	<i>to do</i>.
		 */
		public double waterLevelPullSensor() throws Exception;

		/**
		 * Start a sequence of wash progress pushes with the given period.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code controlPeriod > 0}
		 * pre	{@code tu != null}
		 * post	{@code true}
		 * </pre>
		 *
		 * @param controlPeriod	period at which the pushes must be made.
		 * @param tu			time unit in which {@code controlPeriod} is expressed.
		 * @throws Exception	<i>to do</i>.
		 */
		public void startWashProgressPushSensor(
			long controlPeriod,
			TimeUnit tu
		) throws Exception;

		/**
		 * Stop the sequence of wash progress pushes.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code true}
		 * post	{@code true}
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public void stopWashProgressPushSensor() throws Exception;
	}

	/**
	 * The interface <code>LaundrySensorRequiredPullCI</code> is the pull
	 * interface that a client component must require to call the outbound port.
	 */
	public static interface LaundrySensorRequiredPullCI
	extends		DataRequiredCI.PullCI,
				LaundrySensorCI
	{
	}

	/**
	 * The interface <code>LaundrySensorOfferedPullCI</code> is the pull
	 * interface that a server component must offer to be called by the inbound
	 * port.
	 */
	public static interface LaundrySensorOfferedPullCI
	extends		DataOfferedCI.PullCI,
				LaundrySensorCI
	{
	}
}
// -----------------------------------------------------------------------------
