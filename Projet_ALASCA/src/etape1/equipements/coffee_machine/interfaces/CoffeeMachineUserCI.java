package etape1.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface CoffeeMachineUserCI extends CoffeeMachineUserI, OfferedCI, RequiredCI {

	@Override
	public void fillWater() throws Exception;

	@Override
	public void turnOn() throws Exception;

	@Override
	public void turnOff() throws Exception;

	@Override
	public void setSuspendMode() throws Exception;

	@Override
	public void makeExpresso() throws Exception;

	@Override
	public void setEcoMode() throws Exception;

	@Override
	public void setNormalMode() throws Exception;
	
	@Override
	public void setMaxMode() throws Exception;

}
