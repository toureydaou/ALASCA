package etape3.equipements.kettle.connections.connectors;

import java.util.concurrent.TimeUnit;

import etape3.equipements.kettle.KettleSensorDataCI;
import etape3.equipements.kettle.sensor_data.KettleModeSensorData;
import etape3.equipements.kettle.sensor_data.KettleStateSensorData;
import etape3.equipements.kettle.sensor_data.KettleTemperatureSensorData;
import fr.sorbonne_u.components.connectors.DataConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleSensorDataConnector</code> implements the connector
 * for the {@code KettleSensorDataCI} data interface.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleSensorDataConnector
extends		DataConnector
implements	KettleSensorDataCI.KettleSensorRequiredPullCI
{
	@Override
	public KettleStateSensorData statePullSensor() throws Exception {
		return ((KettleSensorDataCI.KettleSensorOfferedPullCI)
				this.offering).statePullSensor();
	}

	@Override
	public KettleModeSensorData modePullSensor() throws Exception {
		return ((KettleSensorDataCI.KettleSensorOfferedPullCI)
				this.offering).modePullSensor();
	}

	@Override
	public KettleTemperatureSensorData temperaturePullSensor()
	throws Exception {
		return ((KettleSensorDataCI.KettleSensorOfferedPullCI)
				this.offering).temperaturePullSensor();
	}

	@Override
	public void startSensorDataPush(long controlPeriod, TimeUnit tu)
	throws Exception {
		((KettleSensorDataCI.KettleSensorOfferedPullCI)
				this.offering).startSensorDataPush(controlPeriod, tu);
	}
}
// -----------------------------------------------------------------------------
