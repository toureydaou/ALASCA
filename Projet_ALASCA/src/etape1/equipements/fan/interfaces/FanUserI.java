package etape1.equipements.fan.interfaces;

import etape1.equipements.fan.interfaces.FanImplementationI.FanMode;
import etape1.equipements.fan.interfaces.FanImplementationI.FanState;

public interface FanUserI {

	public FanState getState() throws Exception;

	public FanMode getMode() throws Exception;

	public void turnOn() throws Exception;

	public void turnOff() throws Exception;

	public void setHigh() throws Exception;

	public void setMedium() throws Exception;

	public void setLow() throws Exception;
}
