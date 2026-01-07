package etape1.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;


public interface CoffeeMachineInternalControlCI extends CoffeeMachineInternalControlI, OfferedCI, RequiredCI {
	
	public boolean heating() throws Exception;
	
	public void startHeating() throws Exception;
	
	public void stopHeating() throws Exception;
	
	@Override
	public Measure<Double> getTemperature() throws Exception;
	
	

}
