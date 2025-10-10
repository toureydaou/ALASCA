package etape1.equipements.laundry.connections.ports;

import etape1.equipements.laundry.interfaces.LaundryExternalControlJava4CI;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import fr.sorbonne_u.components.ComponentI;

public class LaundryExternalControlJava4InboundPort extends LaundryExternalControlInboundPort
		implements LaundryExternalControlJava4CI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof LaundryUserI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlJava4InboundPort(ComponentI owner) throws Exception {
		super(LaundryExternalControlJava4CI.class, owner);
	}

	/**
	 * create an inbound port.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * uri != null && !uri.isEmpty()
	 * }
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof LaundryUserI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param uri   unique identifier of the port.
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlJava4InboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, LaundryExternalControlJava4CI.class, owner);
	}

	@Override
	public void turnOnJava4() throws Exception {
		this.turnOn();
		
	}

	@Override
	public void turnOffJava4() throws Exception {
		this.turnOff();
		
	}

	@Override
	public LaundryState getStateJava4() throws Exception {
		return this.getState();
	}

	@Override
	public LaundryMode getLaundryModeJava4() throws Exception {
		return this.getLaundryMode();
	}

}
