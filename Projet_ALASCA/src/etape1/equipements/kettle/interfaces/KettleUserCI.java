package etape1.equipements.kettle.interfaces;



import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface KettleUserCI extends  OfferedCI, RequiredCI, KettleUserI {
	

	/**
	 * @see etape1.equipements.kettle.interfaces.KettleImplementationI.equipments.Fan.FanImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception;

	/**
	 * @see etape1.equipements.kettle.interfaces.KettleImplementationI.equipments.Fan.FanImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception;

	/**
	 * @see etape1.equipements.kettle.interfaces.KettleImplementationI.equipments.Fan.FanImplementationI#setHigh()
	 */
	@Override
	public void			setTotalMode() throws Exception;

	/**
	 * @see etape1.equipements.kettle.interfaces.KettleImplementationI.equipments.Fan.FanImplementationI#setLow()
	 */
	@Override
	public void	 setPartialMode() throws Exception;


}
