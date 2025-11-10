package etape1.equipements.coffee_machine.interfaces;

public interface CoffeeMachineExternalControlJava4CI extends CoffeeMachineExternalControlCI {
	public double getTemperatureJava4() throws Exception;

	public double getPowerLevelJava4() throws Exception;

	public double getMaxPowerLevelJava4() throws Exception;

	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception;

	
}
