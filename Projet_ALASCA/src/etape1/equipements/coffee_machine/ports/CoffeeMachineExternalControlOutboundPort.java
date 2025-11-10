package etape1.equipements.coffee_machine.ports;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import physical_data.Measure;

public class CoffeeMachineExternalControlOutboundPort extends AbstractOutboundPort
		implements CoffeeMachineExternalControlCI {

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
	public CoffeeMachineExternalControlOutboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineExternalControlCI.class, owner);
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
	public CoffeeMachineExternalControlOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineExternalControlCI.class, owner);
	}

	@Override
	public Measure<Double> getTemperature() throws Exception {
		return ((CoffeeMachineExternalControlCI)this.getConnector()).getTemperature();
	}

	@Override
	public Measure<Double> getPowerLevel() throws Exception {
		return ((CoffeeMachineExternalControlCI)this.getConnector()).getPowerLevel();
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return ((CoffeeMachineExternalControlCI)this.getConnector()).getMaxPowerLevel();
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		((CoffeeMachineExternalControlCI)this.getConnector()).setCurrentPowerLevel(powerLevel);

	}



}
