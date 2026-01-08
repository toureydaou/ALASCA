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
	public boolean on() throws Exception {
		return ((CoffeeMachineUserCI) this.offering).on();
	}

	@Override
	public void setEcoMode() throws Exception {
		((CoffeeMachineUserCI) this.offering).setEcoMode();

	}

	@Override
	public void fillWater() throws Exception {
		((CoffeeMachineUserCI) this.offering).fillWater();

	}

	@Override
	public void setSuspendMode() throws Exception {
		((CoffeeMachineUserCI) this.offering).setSuspendMode();

	}

	@Override
	public void makeExpresso() throws Exception {
		((CoffeeMachineUserCI) this.offering).makeExpresso();

	}

	@Override
	public void setNormalMode() throws Exception {
		((CoffeeMachineUserCI) this.offering).setNormalMode();

	}

	@Override
	public void setMaxMode() throws Exception {
		((CoffeeMachineUserCI) this.offering).setMaxMode();

	}

	@Override
	public void serveCoffee() throws Exception {
		((CoffeeMachineUserCI) this.offering).serveCoffee();
	}

}
