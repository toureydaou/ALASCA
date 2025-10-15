package etape1.equipements.coffee_machine.ports;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;

public class CoffeeMachineExternalControlInboundPort extends AbstractInboundPort
		implements CoffeeMachineExternalControlCI {

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
	public CoffeeMachineExternalControlInboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineExternalControlCI.class, owner);
		assert owner instanceof CoffeeMachineExternalControlI
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
	public CoffeeMachineExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineExternalControlCI.class, owner);
		assert owner instanceof CoffeeMachineExternalControlI
				: new PreconditionException("owner instanceof CoffeeMachineImplementationI");
	}

	public CoffeeMachineExternalControlInboundPort(String uri, Class<? extends OfferedCI> implementedInterface,
			ComponentI owner) throws Exception {
		super(uri, implementedInterface, owner);

		assert implementedInterface != null
				&& CoffeeMachineExternalControlCI.class.isAssignableFrom(implementedInterface)
				: new PreconditionException("implementedInterface != null && "
						+ "CoffeeMachineExternalControlCI.class.isAssignableFrom(" + "implementedInterface)");
		//assert owner instanceof CoffeeMachineExternalControlI
			//	: new PreconditionException("owner instanceof CoffeeMachineExternalControlI");
	}

	@Override
	public Measure<Double> getTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((CoffeeMachineExternalControlI) o).getTemperature();
		});
	}

	@Override
	public Measure<Double> getPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((CoffeeMachineExternalControlI) o).getPowerLevel();
		});
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((CoffeeMachineExternalControlI) o).getMaxPowerLevel();
		});
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineExternalControlI) o).setCurrentPowerLevel(powerLevel);
			return null;
		});
	}

	@Override
	public void setEcoMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineExternalControlI) o).setEcoMode();
			return null;
		});

	}

}
