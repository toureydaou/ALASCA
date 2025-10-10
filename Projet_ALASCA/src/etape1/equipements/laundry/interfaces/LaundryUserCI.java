package etape1.equipements.laundry.interfaces;



import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface LaundryUserCI extends  OfferedCI, RequiredCI, LaundryUserI {
	

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception;

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception;

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#setHigh()
	 */
	@Override
	public void			setDryMode() throws Exception;

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#setLow()
	 */
	@Override
	public void	 setWashMode() throws Exception;
	
	@Override
	public void setLaundryWashModeWhite() throws Exception;
	
	@Override
	public void setLaundryWashModeColor() throws Exception;



	@Override
	public void setTemperature(WashTemperatures temp) throws Exception;
}
