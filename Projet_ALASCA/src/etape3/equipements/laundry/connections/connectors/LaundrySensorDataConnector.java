package etape3.equipements.laundry.connections.connectors;

import java.util.concurrent.TimeUnit;

import etape3.equipements.laundry.LaundrySensorDataCI;
import etape3.equipements.laundry.sensor_data.LaundryStateSensorData;
import etape3.equipements.laundry.sensor_data.WashProgressSensorData;
import fr.sorbonne_u.components.connectors.DataConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundrySensorDataConnector</code> implements the data
 * connector for the {@code LaundrySensorDataCI} component data interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This connector forwards pull requests from the controller to the laundry
 * machine component.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class LaundrySensorDataConnector
extends		DataConnector
implements	LaundrySensorDataCI.LaundrySensorRequiredPullCI
{
	@Override
	public LaundryStateSensorData statePullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.offering).statePullSensor();
	}

	@Override
	public WashProgressSensorData washProgressPullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.offering).washProgressPullSensor();
	}

	@Override
	public double waterTemperaturePullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.offering).waterTemperaturePullSensor();
	}

	@Override
	public double waterLevelPullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.offering).waterLevelPullSensor();
	}

	@Override
	public void startWashProgressPushSensor(long controlPeriod, TimeUnit tu)
	throws Exception
	{
		((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.offering).startWashProgressPushSensor(controlPeriod, tu);
	}

	@Override
	public void stopWashProgressPushSensor() throws Exception {
		((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.offering).stopWashProgressPushSensor();
	}
}
// -----------------------------------------------------------------------------
