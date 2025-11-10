package etape1.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import physical_data.Measure;

public interface CoffeeMachineInternalControlCI extends CoffeeMachineInternalControlI, OfferedCI, RequiredCI {
	
	public boolean heating() throws Exception;
	
	public void startHeating() throws Exception;
	
	public void stopHeating() throws Exception;
	
	@Override
	public Measure<Double> getTemperature() throws Exception;
	
	

}
