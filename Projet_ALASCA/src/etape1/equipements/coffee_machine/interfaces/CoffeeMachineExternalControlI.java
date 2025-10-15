package etape1.equipements.coffee_machine.interfaces;

import physical_data.Measure;

public interface CoffeeMachineExternalControlI extends CoffeeMachineTemperatureI {
	
	public Measure<Double> getTemperature() throws Exception;
	
	public Measure<Double> getPowerLevel() throws Exception;
	
	public Measure<Double> getMaxPowerLevel() throws Exception;
	
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception;
	
	public void setEcoMode() throws Exception;

}
