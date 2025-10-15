package etape1.equipements.coffee_machine.connectors;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;

public class CoffeeMachineInternalConnector extends AbstractConnector implements CoffeeMachineInternalControlCI {
	
	
	

	@Override
	public boolean heating() throws Exception {
		return ((CoffeeMachineInternalControlCI) this.offering).heating();
	}

	@Override
	public void startHeating() throws Exception {
		((CoffeeMachineInternalControlCI) this.offering).startHeating();

	}

	@Override
	public void stopHeating() throws Exception {
		((CoffeeMachineInternalControlCI) this.offering).stopHeating();

	}

}
