package etape1.equipements.laundry.interfaces;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;

public interface LaundryExternalControlJava4CI extends LaundryExternalControlCI {
	
	public LaundryState	getStateJava4() throws Exception;

	public LaundryMode	getLaundryModeJava4() throws Exception;
	
	public void turnOnJava4() throws Exception;

	public void turnOffJava4() throws Exception;
}
