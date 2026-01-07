package etape3.equipements.coffee_machine.connections.ports;

import etape3.equipements.coffee_machine.CoffeeMachineCyPhy;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineActuatorCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

public class CoffeeMachineActuatorInboundPort extends AbstractInboundPort implements CoffeeMachineActuatorCI {

	private static final long serialVersionUID = 1L;

	public CoffeeMachineActuatorInboundPort(ComponentI owner) throws Exception {
		super(CoffeeMachineActuatorCI.class, owner);
		assert owner instanceof CoffeeMachineCyPhy;
	}

	public CoffeeMachineActuatorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CoffeeMachineActuatorCI.class, owner);
		assert owner instanceof CoffeeMachineCyPhy;
	}

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).turnOn();
			return null;
		});
	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).turnOff();
			return null;
		});
	}

	@Override
	public void startHeating() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).startHeating();
			return null;
		});
	}

	@Override
	public void stopHeating() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).stopHeating();
			return null;
		});
	}

	@Override
	public void setSuspendMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).setSuspendMode();
			return null;
		});
	}

	@Override
	public void setEcoMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).setEcoMode();
			return null;
		});
	}

	@Override
	public void setNormalMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).setNormalMode();
			return null;
		});
	}

	@Override
	public void setMaxMode() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).setMaxMode();
			return null;
		});
	}

	@Override
	public void makeExpresso() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).makeExpresso();
			return null;
		});
	}

	@Override
	public void fillWater() throws Exception {
		this.getOwner().handleRequest(o -> {
			((CoffeeMachineCyPhy) o).fillWater();
			return null;
		});
	}
}