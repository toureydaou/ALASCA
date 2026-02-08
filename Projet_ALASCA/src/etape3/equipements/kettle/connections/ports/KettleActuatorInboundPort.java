package etape3.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape3.equipements.kettle.KettleActuatorCI;
import etape3.equipements.kettle.KettleCyPhy;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleActuatorInboundPort</code> implements the inbound port
 * for the {@code KettleActuatorCI} component interface.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleActuatorInboundPort
extends		AbstractInboundPort
implements	KettleActuatorCI
{
	private static final long serialVersionUID = 1L;

	public KettleActuatorInboundPort(ComponentI owner)
	throws Exception
	{
		super(KettleActuatorCI.class, owner);
		assert owner instanceof KettleCyPhy;
	}

	public KettleActuatorInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, KettleActuatorCI.class, owner);
		assert owner instanceof KettleCyPhy;
	}

	@Override
	public void startHeating() throws Exception {
		this.getOwner().handleRequest(o -> {
			((KettleCyPhy)o).startHeating();
			return null;
		});
	}

	@Override
	public void stopHeating() throws Exception {
		this.getOwner().handleRequest(o -> {
			((KettleCyPhy)o).stopHeating();
			return null;
		});
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		this.getOwner().handleRequest(o -> {
			((KettleCyPhy)o).setMode(mode);
			return null;
		});
	}
}
// -----------------------------------------------------------------------------
