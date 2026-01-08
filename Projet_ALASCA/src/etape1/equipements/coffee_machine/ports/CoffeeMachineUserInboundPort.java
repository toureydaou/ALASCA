package etape1.equipements.coffee_machine.ports;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class CoffeeMachineUserInboundPort extends AbstractInboundPort implements CoffeeMachineUserCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port instance.
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
	 * owner instanceof CoffeeMachineImplementationI
	 * }
	 * post	{@code
	 * true
	 * }	// no more postcondition.
	 * </pre>
	 *
	 * @param owner component owning the port.
	 * @throws Exception <i>to do</i>.
	 */
	public CoffeeMachineUserInboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineUserCI.class, owner);
		assert owner instanceof CoffeeMachineUserI
				: new PreconditionException("owner instanceof CoffeeMachineImplementationI");
	}

	/**
	 * create an inbound port instance.
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
	 * owner instanceof CoffeeMachineImplementationI
	 * }
	 * post	{@code
	 * true
	 * }	// no more postcondition.
	 * </pre>
	 *
	 * @param uri   URI of the port.
	 * @param owner component owning the port.
	 * @throws Exception <i>to do</i>.
	 */
	public CoffeeMachineUserInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineUserCI.class, owner);
		assert owner instanceof CoffeeMachineUserI
				: new PreconditionException("owner instanceof CoffeeMachineImplementationI");
	}

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).turnOn();
			return null;
		});

	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).turnOff();
			return null;
		});

	}

	@Override
	public CoffeeMachineState getState() throws Exception {
		return this.getOwner().handleRequest(o -> ((CoffeeMachineUserI) o).getState());
	}

	@Override
	public CoffeeMachineMode getMode() throws Exception {
		return this.getOwner().handleRequest(o -> ((CoffeeMachineUserI) o).getMode());
	}


	@Override
	public boolean on() throws Exception {
		return this.getOwner().handleRequest(o -> ((CoffeeMachineUserI) o).on());
	}

	@Override
	public void setEcoMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).setEcoMode();
			return null;
		});
		
	}

	@Override
	public void fillWater() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).fillWater();
			return null;
		});
	}

	@Override
	public void setSuspendMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).setSuspendMode();
			return null;
		});
		
	}

	@Override
	public void makeExpresso() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).makeExpresso();
			return null;
		});
		
	}

	@Override
	public void setNormalMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).setNormalMode();
			return null;
		});
		
	}

	@Override
	public void setMaxMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).setMaxMode();
			return null;
		});
		
	}

	@Override
	public void serveCoffee() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineUserI) o).serveCoffee();
			return null;
		});
		
	}

}
