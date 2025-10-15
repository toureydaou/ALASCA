package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import etape1.equipements.kettle.interfaces.KettleExternalControlI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class KettleExternalControlInboundPort extends AbstractInboundPort implements KettleExternalControlCI {

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
	 * owner instanceof KettleExternalControlI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public KettleExternalControlInboundPort(ComponentI owner) throws Exception {
		this(KettleExternalControlCI.class, owner);
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
	 * owner instanceof KettleExternalControlI
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
	public KettleExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		this(uri, KettleExternalControlCI.class, owner);
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
	 * implementedInterface != null && KettleExternalControlCI.class.isAssignableFrom(implementedInterface)
	 * }
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof KettleExternalControlI
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
	public KettleExternalControlInboundPort(Class<? extends OfferedCI> implementedInterface, ComponentI owner)
			throws Exception {
		super(implementedInterface, owner);

		assert implementedInterface != null && KettleExternalControlCI.class.isAssignableFrom(implementedInterface)
				: new PreconditionException("implementedInterface != null && "
						+ "KettleExternalControlCI.class.isAssignableFrom(" + "implementedInterface)");
		assert owner instanceof KettleExternalControlI
				: new PreconditionException("owner instanceof KettleExternalControlI");
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
	 * implementedInterface != null && KettleExternalControlCI.class.isAssignableFrom(implementedInterface)
	 * }
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof KettleExternalControlI
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
	public KettleExternalControlInboundPort(String uri, Class<? extends OfferedCI> implementedInterface,
			ComponentI owner) throws Exception {
		super(uri, implementedInterface, owner);

		assert implementedInterface != null && KettleExternalControlCI.class.isAssignableFrom(implementedInterface)
				: new PreconditionException("implementedInterface != null && "
						+ "KettleExternalControlCI.class.isAssignableFrom(" + "implementedInterface)");
		assert owner instanceof KettleExternalControlI
				: new PreconditionException("owner instanceof KettleExternalControlI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -----------------------

	@Override
	public KettleState getState() throws Exception {

		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getState());
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getKettleMode());
	}

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(o -> {
			((KettleExternalControlI) o).turnOn();
			return null;
		});

	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(o -> {
			((KettleExternalControlI) o).turnOff();
			return null;
		});

	}

	@Override
	public void setTemperature() throws Exception {
		this.getOwner().handleRequest(o -> {
			((KettleExternalControlI) o).setTemperature();
			return null;
		});
		
	}

}
