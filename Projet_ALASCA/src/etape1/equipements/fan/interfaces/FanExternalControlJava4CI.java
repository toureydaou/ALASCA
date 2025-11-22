package etape1.equipements.fan.interfaces;

public interface FanExternalControlJava4CI extends FanExternalControlCI {
	public double getTemperatureJava4() throws Exception;

	public double getPowerLevelJava4() throws Exception;

	public double getMaxPowerLevelJava4() throws Exception;

	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception;
	
	public void setModeJava4(int mode) throws Exception;

	
}
