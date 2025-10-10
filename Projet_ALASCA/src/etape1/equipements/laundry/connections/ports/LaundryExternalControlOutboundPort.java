package etape1.equipements.laundry.connections.ports;

import etape1.equipements.laundry.interfaces.LaundryExternalControlCI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;



public class LaundryExternalControlOutboundPort extends AbstractOutboundPort implements LaundryExternalControlCI {

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
		public				LaundryExternalControlOutboundPort(ComponentI owner)
		throws Exception
		{
			super(LaundryExternalControlCI.class, owner);
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
		public				LaundryExternalControlOutboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, LaundryExternalControlCI.class, owner);
		}
	// -------------------------------------------------------------------------
	// Methods
	// -----------------------

	@Override
	public LaundryState getState() throws Exception {

		return  ((LaundryExternalControlI) this.getConnector()).getState();
	}

	@Override
	public LaundryMode getLaundryMode() throws Exception {
		return  ((LaundryExternalControlI) this.getConnector()).getLaundryMode();
	}

	@Override
	public void turnOn() throws Exception {
		((LaundryExternalControlI) this.getConnector()).turnOn();

	}

	@Override
	public void turnOff() throws Exception {
		((LaundryExternalControlI) this.getConnector()).turnOff();

	}

}
