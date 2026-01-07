package etape1.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;

public interface CoffeeMachineInternalControlI  extends CoffeeMachineTemperatureI{
	
	public boolean heating() throws Exception;
	
	public void startHeating() throws Exception;
	
	public void stopHeating() throws Exception;

	@Override
	public Measure<Double> getTemperature() throws Exception;
	

}
