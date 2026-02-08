package etape1.equipements.fan.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface FanExternalControlCI extends FanExternalControlI, OfferedCI, RequiredCI {

	public Measure<Double> getPowerLevel() throws Exception;

	public Measure<Double> getMaxPowerLevel() throws Exception;

	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception;
	
	public void setMode(int mode) throws Exception;

	
}
