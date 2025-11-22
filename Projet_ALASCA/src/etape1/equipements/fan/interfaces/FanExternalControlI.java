package etape1.equipements.fan.interfaces;


import physical_data.Measure;

public interface FanExternalControlI  {
	
	public Measure<Double> getPowerLevel() throws Exception;
	
	public Measure<Double> getMaxPowerLevel() throws Exception;
	
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception;
	
	public void setMode(int mode) throws Exception;
	
	

}
