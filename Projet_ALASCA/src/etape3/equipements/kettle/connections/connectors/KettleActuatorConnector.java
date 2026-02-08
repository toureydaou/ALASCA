package etape3.equipements.kettle.connections.connectors;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape3.equipements.kettle.KettleActuatorCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleActuatorConnector</code> implements the connector
 * for the {@code KettleActuatorCI} component interface.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleActuatorConnector
extends		AbstractConnector
implements	KettleActuatorCI
{
	@Override
	public void startHeating() throws Exception {
		((KettleActuatorCI)this.offering).startHeating();
	}

	@Override
	public void stopHeating() throws Exception {
		((KettleActuatorCI)this.offering).stopHeating();
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		((KettleActuatorCI)this.offering).setMode(mode);
	}
}
// -----------------------------------------------------------------------------
