package etape1.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface CoffeeMachineExternalControlCI extends CoffeeMachineExternalControlI, OfferedCI, RequiredCI {
	public Measure<Double> getTemperature() throws Exception;

	public Measure<Double> getPowerLevel() throws Exception;

	public Measure<Double> getMaxPowerLevel() throws Exception;

	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception;
	
	public void setMode(int mode) throws Exception;

	
}
