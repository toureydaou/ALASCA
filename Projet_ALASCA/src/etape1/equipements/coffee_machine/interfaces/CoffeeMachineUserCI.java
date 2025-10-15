package etape1.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface CoffeeMachineUserCI extends CoffeeMachineUserI, OfferedCI, RequiredCI {
	
	public void turnOn() throws Exception; 
	
	public void turnOff() throws Exception;
	

}
