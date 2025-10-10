package etape1.equipements.laundry.interfaces;


import physical_data.Measure;
import physical_data.MeasurementUnit;

public interface LaundryWashTemperatureI {
	
	
	public static enum WashTemperatures {
		T_30, T_40, T_50,T_60,T_70,T_80,T_90
	}

	public static final MeasurementUnit	TEMPERATURE_UNIT =
			MeasurementUnit.CELSIUS;
	
	public Measure<Double> getCurrentTemperature() throws Exception;
	
	public void setTemperature(WashTemperatures temp) throws Exception;
	
	

}
