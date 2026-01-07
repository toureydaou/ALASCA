package etape3.equipements.coffee_machine.connections.connectors;

import etape3.equipements.coffee_machine.interfaces.CoffeeMachineActuatorCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;

public class CoffeeMachineActuatorConnector extends AbstractConnector implements CoffeeMachineActuatorCI {

	@Override
	public void turnOn() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).turnOn();

	}

	@Override
	public void turnOff() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).turnOff();

	}

	@Override
	public void startHeating() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).startHeating();

	}

	@Override
	public void stopHeating() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).stopHeating();

	}

	@Override
	public void setSuspendMode() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).setSuspendMode();

	}

	@Override
	public void setEcoMode() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).setEcoMode();

	}

	@Override
	public void setNormalMode() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).setNormalMode();

	}

	@Override
	public void setMaxMode() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).setMaxMode();
		;

	}

	@Override
	public void makeExpresso() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).makeExpresso();

	}

	@Override
	public void fillWater() throws Exception {
		((CoffeeMachineActuatorCI) this.offering).fillWater();

	}

}
