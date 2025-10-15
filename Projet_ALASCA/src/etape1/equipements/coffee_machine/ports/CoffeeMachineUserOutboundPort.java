package etape1.equipements.coffee_machine.ports;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserCI;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class CoffeeMachineUserOutboundPort extends AbstractOutboundPort implements CoffeeMachineUserCI {

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
	public CoffeeMachineUserOutboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineUserCI.class, owner);
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
	public CoffeeMachineUserOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineUserCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		((CoffeeMachineUserCI) this.getConnector()).turnOn();

	}

	@Override
	public void turnOff() throws Exception {
		((CoffeeMachineUserCI) this.getConnector()).turnOff();

	}

	@Override
	public CoffeeMachineState getState() throws Exception {
		return ((CoffeeMachineUserCI) this.getConnector()).getState();
	}

	@Override
	public CoffeeMachineMode getMode() throws Exception {
		return ((CoffeeMachineUserCI) this.getConnector()).getMode();
	}

	@Override
	public void setExpresso() throws Exception {
		((CoffeeMachineUserCI) this.getConnector()).setExpresso();

	}

	@Override
	public void setThe() throws Exception {
		((CoffeeMachineUserCI) this.getConnector()).setThe();

	}


	@Override
	public boolean on() throws Exception {
		return ((CoffeeMachineUserCI) this.getConnector()).on();
	}

	@Override
	public void setEcoMode() throws Exception {
		((CoffeeMachineUserCI) this.getConnector()).setEcoMode();
		
	}

}
