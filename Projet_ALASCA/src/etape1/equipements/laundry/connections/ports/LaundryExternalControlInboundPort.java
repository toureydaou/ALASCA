package etape1.equipements.laundry.connections.ports;

import etape1.equipements.laundry.interfaces.LaundryExternalControlCI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class LaundryExternalControlInboundPort extends AbstractInboundPort implements LaundryExternalControlCI {

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
	 * owner instanceof LaundryExternalControlI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlInboundPort(ComponentI owner) throws Exception {
		this(LaundryExternalControlCI.class, owner);
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
	 * owner instanceof LaundryExternalControlI
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
	public LaundryExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		this(uri, LaundryExternalControlCI.class, owner);
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
	 * implementedInterface != null && LaundryExternalControlCI.class.isAssignableFrom(implementedInterface)
	 * }
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof LaundryExternalControlI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param implementedInterface interface implemented by this port.
	 * @param owner                component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlInboundPort(Class<? extends OfferedCI> implementedInterface, ComponentI owner)
			throws Exception {
		super(implementedInterface, owner);

		assert implementedInterface != null && LaundryExternalControlCI.class.isAssignableFrom(implementedInterface)
				: new PreconditionException("implementedInterface != null && "
						+ "LaundryExternalControlCI.class.isAssignableFrom(" + "implementedInterface)");
		assert owner instanceof LaundryExternalControlI
				: new PreconditionException("owner instanceof LaundryExternalControlI");
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
	 * implementedInterface != null && LaundryExternalControlCI.class.isAssignableFrom(implementedInterface)
	 * }
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof LaundryExternalControlI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param uri                  unique identifier of the port.
	 * @param implementedInterface interface implemented by this port.
	 * @param owner                component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlInboundPort(String uri, Class<? extends OfferedCI> implementedInterface,
			ComponentI owner) throws Exception {
		super(uri, implementedInterface, owner);

		assert implementedInterface != null && LaundryExternalControlCI.class.isAssignableFrom(implementedInterface)
				: new PreconditionException("implementedInterface != null && "
						+ "LaundryExternalControlCI.class.isAssignableFrom(" + "implementedInterface)");
		assert owner instanceof LaundryExternalControlI
				: new PreconditionException("owner instanceof LaundryExternalControlI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -----------------------

	@Override
	public LaundryState getState() throws Exception {

		return this.getOwner().handleRequest(o -> ((LaundryExternalControlI) o).getState());
	}

	@Override
	public LaundryMode getLaundryMode() throws Exception {
		return this.getOwner().handleRequest(o -> ((LaundryExternalControlI) o).getLaundryMode());
	}

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(o -> {
			((LaundryExternalControlI) o).turnOn();
			return null;
		});

	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(o -> {
			((LaundryExternalControlI) o).turnOff();
			return null;
		});

	}

}
