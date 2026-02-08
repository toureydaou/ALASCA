package etape3.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape3.equipements.kettle.KettleActuatorCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleActuatorOutboundPort</code> implements the outbound
 * port for the {@code KettleActuatorCI} component interface.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleActuatorOutboundPort
extends		AbstractOutboundPort
implements	KettleActuatorCI
{
	private static final long serialVersionUID = 1L;

	public KettleActuatorOutboundPort(ComponentI owner)
	throws Exception
	{
		super(KettleActuatorCI.class, owner);
	}

	public KettleActuatorOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, KettleActuatorCI.class, owner);
	}

	@Override
	public void startHeating() throws Exception {
		((KettleActuatorCI)this.getConnector()).startHeating();
	}

	@Override
	public void stopHeating() throws Exception {
		((KettleActuatorCI)this.getConnector()).stopHeating();
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		((KettleActuatorCI)this.getConnector()).setMode(mode);
	}
}
// -----------------------------------------------------------------------------
