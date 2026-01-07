package etape3.equipements.coffee_machine.connections.ports;

import etape3.equipements.coffee_machine.CoffeeMachineCyPhy;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineActuatorCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

public class CoffeeMachineActuatorOutboundPort extends AbstractOutboundPort implements CoffeeMachineActuatorCI {

	private static final long serialVersionUID = 1L;

	public CoffeeMachineActuatorOutboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineActuatorCI.class, owner);

	}

	public CoffeeMachineActuatorOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineActuatorCI.class, owner);
		assert owner instanceof CoffeeMachineCyPhy;
	}

	@Override
	public void turnOn() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).turnOn();

	}

	@Override
	public void turnOff() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).turnOff();

	}

	@Override
	public void startHeating() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).startHeating();

	}

	@Override
	public void stopHeating() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).stopHeating();

	}

	@Override
	public void setSuspendMode() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).setSuspendMode();

	}

	@Override
	public void setEcoMode() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).setEcoMode();

	}

	@Override
	public void setNormalMode() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).setNormalMode();

	}

	@Override
	public void setMaxMode() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).setMaxMode();

	}

	@Override
	public void makeExpresso() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).makeExpresso();

	}

	@Override
	public void fillWater() throws Exception {
		((CoffeeMachineActuatorCI) this.getConnector()).fillWater();

	}

}
