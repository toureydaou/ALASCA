package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import etape1.equipements.kettle.interfaces.KettleExternalControlI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;



public class KettleExternalControlOutboundPort extends AbstractOutboundPort implements KettleExternalControlCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
		// Constructors
		// -------------------------------------------------------------------------

		/**
		 * create an outbound port.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code owner != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param owner			component that owns this port.
		 * @throws Exception 	<i>to do</i>.
		 */
		public				KettleExternalControlOutboundPort(ComponentI owner)
		throws Exception
		{
			super(KettleExternalControlCI.class, owner);
		}

		/**
		 * create an outbound port.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code uri != null && !uri.isEmpty()}
		 * pre	{@code owner != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param uri			unique identifier of the port.
		 * @param owner			component that owns this port.
		 * @throws Exception 	<i>to do</i>.
		 */
		public				KettleExternalControlOutboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, KettleExternalControlCI.class, owner);
		}
	// -------------------------------------------------------------------------
	// Methods
	// -----------------------

	@Override
	public KettleState getState() throws Exception {

		return  ((KettleExternalControlI) this.getConnector()).getState();
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return  ((KettleExternalControlI) this.getConnector()).getKettleMode();
	}

	@Override
	public void turnOn() throws Exception {
		((KettleExternalControlI) this.getConnector()).turnOn();

	}

	@Override
	public void turnOff() throws Exception {
		((KettleExternalControlI) this.getConnector()).turnOff();

	}

	@Override
	public void setTemperature() throws Exception {
		((KettleExternalControlI) this.getConnector()).setTemperature();
		
	}

}
