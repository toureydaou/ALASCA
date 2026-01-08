package etape1.equipements.coffee_machine.interfaces;

public interface CoffeeMachineUserI extends CoffeeMachineImplementationI {
	
	public void fillWater() throws Exception;
	
	public void turnOn() throws Exception;
	
	public void turnOff() throws Exception;
	
	public void setSuspendMode() throws Exception;

	public void makeExpresso() throws Exception;
	
	public void setMaxMode() throws Exception;
	
	public void setEcoMode() throws Exception;
	
	public void setNormalMode() throws Exception;

	void serveCoffee() throws Exception;
}
