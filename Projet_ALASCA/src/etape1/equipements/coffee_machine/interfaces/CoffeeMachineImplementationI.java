package etape1.equipements.coffee_machine.interfaces;

public interface CoffeeMachineImplementationI {

	public static enum CoffeeMachineState {
		ON, HEATING, OFF
	}

	public static enum CoffeeMachineMode {
		EXPRESSO, THE, ECO_MODE
	}

	public boolean on() throws Exception;

	public CoffeeMachineState getState() throws Exception;

	public CoffeeMachineMode getMode() throws Exception;

	public void setExpresso() throws Exception;
	
	public void setEcoMode() throws Exception;

	public void setThe() throws Exception;

	
}
