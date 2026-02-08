package etape3.equipements.laundry.connections.ports;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape3.equipements.laundry.LaundryActuatorCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryActuatorOutboundPort</code> implements the outbound
 * port for the {@code LaundryActuatorCI} component interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This port allows external components (like a controller) to call actuator
 * methods on the laundry machine.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class LaundryActuatorOutboundPort
extends		AbstractOutboundPort
implements	LaundryActuatorCI
{
	private static final long serialVersionUID = 1L;

	public LaundryActuatorOutboundPort(ComponentI owner)
	throws Exception
	{
		super(LaundryActuatorCI.class, owner);
	}

	public LaundryActuatorOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, LaundryActuatorCI.class, owner);
	}

	@Override
	public void startWash() throws Exception {
		((LaundryActuatorCI) this.getConnector()).startWash();
	}

	@Override
	public void stopWash() throws Exception {
		((LaundryActuatorCI) this.getConnector()).stopWash();
	}

	@Override
	public void setWashMode(LaundryWashMode mode) throws Exception {
		((LaundryActuatorCI) this.getConnector()).setWashMode(mode);
	}

	@Override
	public void setWashTemperature(double temperature) throws Exception {
		((LaundryActuatorCI) this.getConnector()).setWashTemperature(temperature);
	}

	@Override
	public void setSpinSpeed(int spinSpeed) throws Exception {
		((LaundryActuatorCI) this.getConnector()).setSpinSpeed(spinSpeed);
	}
}
// -----------------------------------------------------------------------------
