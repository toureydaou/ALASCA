package etape1.equipements.coffee_machine.interfaces;

public interface CoffeeMachineInternalControlI {
	
	public boolean heating() throws Exception;
	
	public void startHeating() throws Exception;
	
	public void stopHeating() throws Exception;
	
	

}
