package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleExternalControlJava4CI;
import fr.sorbonne_u.components.ComponentI;

public class KettleExternalControlJava4InboundPort extends KettleExternalControlInboundPort
		implements KettleExternalControlJava4CI {

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
	 * owner instanceof KettleUserI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public KettleExternalControlJava4InboundPort(ComponentI owner) throws Exception {
		super(KettleExternalControlJava4CI.class, owner);
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
	 * owner instanceof KettleUserI
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
	public KettleExternalControlJava4InboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, KettleExternalControlJava4CI.class, owner);
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
	public KettleState getStateJava4() throws Exception {
		return this.getState();
	}

	@Override
	public KettleMode getKettleModeJava4() throws Exception {
		return this.getKettleMode();
	}

}
