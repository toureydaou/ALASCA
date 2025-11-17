package etape1.equipements.coffee_machine.ports;

import etape1.equipements.coffee_machine.CoffeeMachine;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import fr.sorbonne_u.components.ComponentI;
import physical_data.Measure;

public class CoffeeMachineExternalControlJava4InboundPort extends CoffeeMachineExternalControlInboundPort
		implements CoffeeMachineExternalControlJava4CI {

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
	public CoffeeMachineExternalControlJava4InboundPort(ComponentI owner) throws Exception {
		super(owner);
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
	public CoffeeMachineExternalControlJava4InboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineExternalControlJava4CI.class, owner);
	}

	@Override
	public double getTemperatureJava4() throws Exception {
		return this.getTemperature().getData();
	}

	@Override
	public double getPowerLevelJava4() throws Exception {
		return this.getPowerLevel().getData();
	}

	@Override
	public double getMaxPowerLevelJava4() throws Exception {
		return this.getMaxPowerLevel().getData();
	}

	@Override
	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception {
		this.setCurrentPowerLevel(new Measure<Double>(powerLevel, CoffeeMachine.POWER_UNIT));

	}

	@Override
	public void setModeJava4(int mode) throws Exception {
		this.setMode(mode);
		
	}



}
