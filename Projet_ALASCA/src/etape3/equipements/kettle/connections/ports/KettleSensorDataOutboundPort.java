package etape3.equipements.kettle.connections.ports;

import java.util.concurrent.TimeUnit;

import etape3.equipements.kettle.KettlePushImplementationI;
import etape3.equipements.kettle.KettleSensorDataCI;
import etape3.equipements.kettle.sensor_data.KettleCompoundSensorData;
import etape3.equipements.kettle.sensor_data.KettleModeSensorData;
import etape3.equipements.kettle.sensor_data.KettleSensorDataI;
import etape3.equipements.kettle.sensor_data.KettleStateSensorData;
import etape3.equipements.kettle.sensor_data.KettleTemperatureSensorData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleSensorDataOutboundPort</code> implements the outbound
 * port for the {@code KettleSensorDataCI} component data interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This outbound port is used by the controller component to pull sensor data
 * from the kettle and to receive sensor data pushed by the kettle.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleSensorDataOutboundPort
extends		AbstractDataOutboundPort
implements	KettleSensorDataCI.KettleSensorRequiredPullCI
{
	private static final long serialVersionUID = 1L;

	public KettleSensorDataOutboundPort(ComponentI owner)
	throws Exception
	{
		super(DataRequiredCI.PullCI.class,
			  DataRequiredCI.PushCI.class,
			  owner);
	}

	public KettleSensorDataOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri,
			  KettleSensorDataCI.KettleSensorRequiredPullCI.class,
			  DataRequiredCI.PushCI.class,
			  owner);
	}

	// -------------------------------------------------------------------------
	// Methods - Pull Protocol
	// -------------------------------------------------------------------------

	@Override
	public KettleStateSensorData statePullSensor() throws Exception {
		return ((KettleSensorDataCI.KettleSensorRequiredPullCI)
				this.getConnector()).statePullSensor();
	}

	@Override
	public KettleModeSensorData modePullSensor() throws Exception {
		return ((KettleSensorDataCI.KettleSensorRequiredPullCI)
				this.getConnector()).modePullSensor();
	}

	@Override
	public KettleTemperatureSensorData temperaturePullSensor()
	throws Exception {
		return ((KettleSensorDataCI.KettleSensorRequiredPullCI)
				this.getConnector()).temperaturePullSensor();
	}

	@Override
	public void startSensorDataPush(long controlPeriod, TimeUnit tu)
	throws Exception {
		((KettleSensorDataCI.KettleSensorRequiredPullCI)
				this.getConnector()).startSensorDataPush(controlPeriod, tu);
	}

	// -------------------------------------------------------------------------
	// Methods - Push Protocol
	// -------------------------------------------------------------------------

	@Override
	public void receive(DataRequiredCI.DataI d) throws Exception {
		assert d instanceof KettleSensorDataI :
			new BCMException("d instanceof KettleSensorDataI");

		if (d instanceof KettleStateSensorData) {
			this.getOwner().runTask(
				o -> {
					try {
						((KettlePushImplementationI)o)
							.processKettleState(
								((KettleStateSensorData)d).getState());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
		}
		else if (d instanceof KettleCompoundSensorData) {
			KettleCompoundSensorData compoundData =
				(KettleCompoundSensorData)d;
			this.getOwner().runTask(
				o -> {
					try {
						((KettlePushImplementationI)o)
							.processKettleState(
								compoundData.getState().getState());
						((KettlePushImplementationI)o)
							.processKettleTemperature(
								compoundData.getTemperature());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
		}
		else if (d instanceof KettleTemperatureSensorData) {
			this.getOwner().runTask(
				o -> {
					try {
						((KettlePushImplementationI)o)
							.processKettleTemperature(
								(KettleTemperatureSensorData)d);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
		}
		else {
			throw new BCMException("Unknown kettle sensor data: " + d);
		}
	}
}
// -----------------------------------------------------------------------------
