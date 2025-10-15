package etape1.equipements.coffee_machine.connectors;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;

public class CoffeeMachineUserConnector extends AbstractConnector implements CoffeeMachineUserCI {

	@Override
	public void turnOn() throws Exception {
		((CoffeeMachineUserCI) this.offering).turnOn();

	}

	@Override
	public void turnOff() throws Exception {
		((CoffeeMachineUserCI) this.offering).turnOff();

	}

	@Override
	public CoffeeMachineState getState() throws Exception {
		
		return ((CoffeeMachineUserCI) this.offering).getState();
	}

	@Override
	public CoffeeMachineMode getMode() throws Exception {
		return ((CoffeeMachineUserCI) this.offering).getMode();
	}

	@Override
	public void setExpresso() throws Exception {
		((CoffeeMachineUserCI) this.offering).setExpresso();
		
	}

	@Override
	public void setThe() throws Exception {
		((CoffeeMachineUserCI) this.offering).setThe();
		
	}

	@Override
	public boolean on() throws Exception {
		return ((CoffeeMachineUserCI) this.offering).on();
	}

	@Override
	public void setEcoMode() throws Exception {
		((CoffeeMachineUserCI) this.offering).setEcoMode();
		
	}

}
