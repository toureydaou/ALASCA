package etape3.equipements.laundry.connections.ports;

import java.util.concurrent.TimeUnit;

import etape3.equipements.laundry.LaundryPushImplementationI;
import etape3.equipements.laundry.LaundrySensorDataCI;
import etape3.equipements.laundry.sensor_data.LaundryStateSensorData;
import etape3.equipements.laundry.sensor_data.LaundrySensorDataI;
import etape3.equipements.laundry.sensor_data.WashProgressSensorData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundrySensorDataOutboundPort</code> implements the outbound
 * port for the {@code LaundrySensorDataCI} component data interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This outbound port is used by the controller component to:
 * <ul>
 * <li>Pull sensor data from the laundry machine (pull protocol)</li>
 * <li>Receive sensor data pushed by the laundry machine (push protocol)</li>
 * </ul>
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class LaundrySensorDataOutboundPort
extends		AbstractDataOutboundPort
implements	LaundrySensorDataCI.LaundrySensorRequiredPullCI
{
	private static final long serialVersionUID = 1L;

	public LaundrySensorDataOutboundPort(ComponentI owner)
	throws Exception
	{
		super(DataRequiredCI.PullCI.class,
			  DataRequiredCI.PushCI.class,
			  owner);
	}

	public LaundrySensorDataOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri,
			  LaundrySensorDataCI.LaundrySensorRequiredPullCI.class,
			  DataRequiredCI.PushCI.class,
			  owner);
	}

	// -------------------------------------------------------------------------
	// Methods - Pull Protocol
	// -------------------------------------------------------------------------

	@Override
	public LaundryStateSensorData statePullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.getConnector()).statePullSensor();
	}

	@Override
	public WashProgressSensorData washProgressPullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.getConnector()).washProgressPullSensor();
	}

	@Override
	public double waterTemperaturePullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.getConnector()).waterTemperaturePullSensor();
	}

	@Override
	public double waterLevelPullSensor() throws Exception {
		return ((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.getConnector()).waterLevelPullSensor();
	}

	@Override
	public void startWashProgressPushSensor(long controlPeriod, TimeUnit tu)
	throws Exception
	{
		((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.getConnector()).startWashProgressPushSensor(
						controlPeriod, tu);
	}

	@Override
	public void stopWashProgressPushSensor() throws Exception {
		((LaundrySensorDataCI.LaundrySensorRequiredPullCI)
				this.getConnector()).stopWashProgressPushSensor();
	}

	// -------------------------------------------------------------------------
	// Methods - Push Protocol
	// -------------------------------------------------------------------------

	@Override
	public void receive(DataRequiredCI.DataI d) throws Exception {
		assert d instanceof LaundrySensorDataI :
			new BCMException("d instanceof LaundrySensorDataI");

		if (d instanceof LaundryStateSensorData) {
			LaundryStateSensorData stateData = (LaundryStateSensorData) d;
			this.getOwner().runTask(
				o -> {
					try {
						((LaundryPushImplementationI)o)
							.processLaundryState(stateData.getState());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
		}
		else if (d instanceof WashProgressSensorData) {
			WashProgressSensorData progressData = (WashProgressSensorData) d;
			this.getOwner().runTask(
				o -> {
					try {
						((LaundryPushImplementationI)o)
							.processWashProgress(progressData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
		}
		else {
			throw new BCMException("Unknown laundry sensor data: " + d);
		}
	}
}
// -----------------------------------------------------------------------------
