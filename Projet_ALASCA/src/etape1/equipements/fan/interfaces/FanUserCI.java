package etape1.equipements.fan.interfaces;



import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface FanUserCI extends OfferedCI, RequiredCI, FanImplementationI {
	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#getState()
	 */
	@Override
	public FanState	getState() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public FanMode	getMode() throws Exception;

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
	public void			setHigh() throws Exception;

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#setHigh()
	 */
	@Override
	public void			setMedium() throws Exception;

	
	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception;
}
