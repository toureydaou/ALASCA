package etape3.equipements.laundry.connections.ports;

import java.util.concurrent.TimeUnit;

import etape3.equipements.laundry.LaundrySensorDataCI;
import etape3.equipements.laundry.sensor_data.LaundryStateSensorData;
import etape3.equipements.laundry.sensor_data.WashProgressSensorData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundrySensorDataInboundPort</code> implements the inbound
 * port for the {@code LaundrySensorDataCI} component data interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This port handles both pull and push protocols for sensor data from the
 * laundry machine.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class LaundrySensorDataInboundPort
extends		AbstractDataInboundPort
implements	LaundrySensorDataCI.LaundrySensorOfferedPullCI
{
	private static final long serialVersionUID = 1L;

	/**
	 * The interface that the owner component must implement.
	 * The owner must provide methods to get sensor data.
	 */
	public static interface LaundrySensorDataOwnerI
	{
		public LaundryStateSensorData statePullSensor() throws Exception;
		public WashProgressSensorData washProgressPullSensor() throws Exception;
		public double waterTemperaturePullSensor() throws Exception;
		public double waterLevelPullSensor() throws Exception;
		public void startWashProgressPushSensor(
				long controlPeriod, TimeUnit tu) throws Exception;
		public void stopWashProgressPushSensor() throws Exception;
	}

	public LaundrySensorDataInboundPort(ComponentI owner)
	throws Exception
	{
		super(LaundrySensorDataCI.LaundrySensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class,
			  owner);
		assert owner instanceof LaundrySensorDataOwnerI;
	}

	public LaundrySensorDataInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri,
			  LaundrySensorDataCI.LaundrySensorOfferedPullCI.class,
			  DataOfferedCI.PushCI.class,
			  owner);
		assert owner instanceof LaundrySensorDataOwnerI;
	}

	@Override
	public LaundryStateSensorData statePullSensor() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((LaundrySensorDataOwnerI)o).statePullSensor());
	}

	@Override
	public WashProgressSensorData washProgressPullSensor() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((LaundrySensorDataOwnerI)o).washProgressPullSensor());
	}

	@Override
	public double waterTemperaturePullSensor() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((LaundrySensorDataOwnerI)o).waterTemperaturePullSensor());
	}

	@Override
	public double waterLevelPullSensor() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((LaundrySensorDataOwnerI)o).waterLevelPullSensor());
	}

	@Override
	public void startWashProgressPushSensor(long controlPeriod, TimeUnit tu)
	throws Exception
	{
		this.getOwner().handleRequest(
			o -> {
				((LaundrySensorDataOwnerI)o).startWashProgressPushSensor(
						controlPeriod, tu);
				return null;
			});
	}

	@Override
	public void stopWashProgressPushSensor() throws Exception {
		this.getOwner().handleRequest(
			o -> {
				((LaundrySensorDataOwnerI)o).stopWashProgressPushSensor();
				return null;
			});
	}

	@Override
	public DataI get() throws Exception {
		return this.getOwner().handleRequest(
			o -> ((LaundrySensorDataOwnerI)o).statePullSensor());
	}
}
// -----------------------------------------------------------------------------
