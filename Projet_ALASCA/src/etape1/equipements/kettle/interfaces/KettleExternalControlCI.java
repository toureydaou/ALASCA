package etape1.equipements.kettle.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;



public interface KettleExternalControlCI extends KettleExternalControlI,  RequiredCI, OfferedCI {
	
	public void turnOn() throws Exception;
	
	public void turnOff() throws Exception;
	
}
