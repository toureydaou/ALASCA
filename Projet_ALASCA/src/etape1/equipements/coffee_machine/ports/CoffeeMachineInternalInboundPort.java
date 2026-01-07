package etape1.equipements.coffee_machine.ports;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;


public class CoffeeMachineInternalInboundPort extends AbstractInboundPort implements CoffeeMachineInternalControlCI {

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
	public CoffeeMachineInternalInboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineInternalControlCI.class, owner);
		assert owner instanceof CoffeeMachineInternalControlI
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
	public CoffeeMachineInternalInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineInternalControlCI.class, owner);
		assert owner instanceof CoffeeMachineInternalControlI
				: new PreconditionException("owner instanceof CoffeeMachineImplementationI");
	}

	@Override
	public boolean heating() throws Exception {
		return this.getOwner().handleRequest(o -> ((CoffeeMachineInternalControlI) o).heating());
	}

	@Override
	public void startHeating() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineInternalControlI) o).startHeating();
			return null;
		});

	}

	@Override
	public void stopHeating() throws Exception {
		
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineInternalControlI) o).stopHeating();
			return null;
		});

	}

	@Override
	public Measure<Double> getTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((CoffeeMachineInternalControlI) o).getTemperature();
			
		});
	}

}
