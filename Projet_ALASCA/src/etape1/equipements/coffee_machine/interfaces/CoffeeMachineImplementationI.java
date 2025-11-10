package etape1.equipements.coffee_machine.interfaces;

public interface CoffeeMachineImplementationI {

	public static enum CoffeeMachineState {
		ON, HEATING, OFF
	}

	public static enum CoffeeMachineMode {
		SUSPEND, ECO, NORMAL, MAX 
	}

	public boolean on() throws Exception;

	public CoffeeMachineState getState() throws Exception;

	public CoffeeMachineMode getMode() throws Exception;

	
}
