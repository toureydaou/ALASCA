package etape3.equipements.laundry.connections.connectors;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape3.equipements.laundry.LaundryActuatorCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryActuatorConnector</code> implements the connector
 * for the {@code LaundryActuatorCI} component interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This connector mediates between the outbound and inbound actuator ports.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class LaundryActuatorConnector
extends		AbstractConnector
implements	LaundryActuatorCI
{
	@Override
	public void startWash() throws Exception {
		((LaundryActuatorCI) this.offering).startWash();
	}

	@Override
	public void stopWash() throws Exception {
		((LaundryActuatorCI) this.offering).stopWash();
	}

	@Override
	public void setWashMode(LaundryWashMode mode) throws Exception {
		((LaundryActuatorCI) this.offering).setWashMode(mode);
	}

	@Override
	public void setWashTemperature(double temperature) throws Exception {
		((LaundryActuatorCI) this.offering).setWashTemperature(temperature);
	}

	@Override
	public void setSpinSpeed(int spinSpeed) throws Exception {
		((LaundryActuatorCI) this.offering).setSpinSpeed(spinSpeed);
	}
}
// -----------------------------------------------------------------------------
