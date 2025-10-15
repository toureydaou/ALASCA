package etape1.equipements.coffee_machine.ports;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class CoffeeMachineInternalOutboundPort extends AbstractOutboundPort implements CoffeeMachineInternalControlCI {

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
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * owner != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public CoffeeMachineInternalOutboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineInternalControlCI.class, owner);
	}

	/**
	 * create an outbound port.
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
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param uri   unique identifier of the port.
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public CoffeeMachineInternalOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineInternalControlCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public boolean heating() throws Exception {
		
		return ((CoffeeMachineInternalControlCI)this.getConnector()).heating();
	}

	@Override
	public void startHeating() throws Exception {
		((CoffeeMachineInternalControlCI)this.getConnector()).startHeating();

	}

	@Override
	public void stopHeating() throws Exception {
		((CoffeeMachineInternalControlCI)this.getConnector()).stopHeating();

	}

}
