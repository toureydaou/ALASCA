package etape1.equipements.fan;



import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface FanUserCI extends OfferedCI, RequiredCI, FanImplementationI {
	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.Fan.FanImplementationI#getState()
	 */
	@Override
	public FanState	getState() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public FanMode	getMode() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.Fan.FanImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.Fan.FanImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.Fan.FanImplementationI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception;

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.Fan.FanImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception;
}
