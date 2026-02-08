package etape3.equipements.kettle.connections.ports;

import java.util.concurrent.TimeUnit;

import etape3.equipements.kettle.KettleCyPhy;
import etape3.equipements.kettle.KettleSensorDataCI;
import etape3.equipements.kettle.sensor_data.KettleCompoundSensorData;
import etape3.equipements.kettle.sensor_data.KettleModeSensorData;
import etape3.equipements.kettle.sensor_data.KettleStateSensorData;
import etape3.equipements.kettle.sensor_data.KettleTemperatureSensorData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleSensorDataInboundPort</code> implements the inbound
 * port for the {@code KettleSensorDataCI} component data interface.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleSensorDataInboundPort
extends		AbstractDataInboundPort
implements	KettleSensorDataCI.KettleSensorOfferedPullCI
{
	private static final long serialVersionUID = 1L;

	public KettleSensorDataInboundPort(ComponentI owner)
	throws Exception
	{
		super(KettleSensorDataCI.KettleSensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class,
			  owner);
		assert owner instanceof KettleCyPhy;
	}

	public KettleSensorDataInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri,
			  KettleSensorDataCI.KettleSensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class,
			  owner);
		assert owner instanceof KettleCyPhy;
	}

	@Override
	public KettleStateSensorData statePullSensor() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((KettleCyPhy)o).statePullSensor());
	}

	@Override
	public KettleModeSensorData modePullSensor() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((KettleCyPhy)o).modePullSensor());
	}

	@Override
	public KettleTemperatureSensorData temperaturePullSensor()
	throws Exception {
		return this.getOwner().handleRequest(
			o -> ((KettleCyPhy)o).temperaturePullSensor());
	}

	@Override
	public void startSensorDataPush(long controlPeriod, TimeUnit tu)
	throws Exception {
		this.getOwner().handleRequest(
			o -> {
				((KettleCyPhy)o).startSensorDataPush(controlPeriod, tu);
				return null;
			});
	}

	@Override
	public DataI get() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((KettleCyPhy)o).sensorDataCompound());
	}
}
// -----------------------------------------------------------------------------
