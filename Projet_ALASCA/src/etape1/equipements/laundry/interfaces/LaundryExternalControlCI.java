package etape1.equipements.laundry.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;



public interface LaundryExternalControlCI extends LaundryExternalControlI,  RequiredCI, OfferedCI {
	
	public void turnOn() throws Exception;
	
	public void turnOff() throws Exception;
	
}
