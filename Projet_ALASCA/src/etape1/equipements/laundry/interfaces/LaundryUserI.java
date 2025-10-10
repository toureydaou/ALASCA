package etape1.equipements.laundry.interfaces;

public interface LaundryUserI extends LaundryExternalControlI, LaundryWashModeI, LaundryWashTemperatureI {
	

	/**
	 * set the hair dryer in high mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getState() == FanState.ON}
	 * pre	{@code getMode() == FanMode.LOW}
	 * post	{@code getMode() == FanMode.HIGH}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setWashMode() throws Exception;
	
	/**
	 * set the hair dryer in high mode.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getState() == FanState.ON}
	 * pre	{@code getMode() == FanMode.LOW}
	 * post	{@code getMode() == FanMode.HIGH}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setDryMode() throws Exception;



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
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI.equipments.Fan.FanImplementationI#setLow()
	 */
	@Override
	public void			setLaundryWashModeWhite() throws Exception;
	
	@Override
	public void			setLaundryWashModeColor() throws Exception;
	
	@Override
	public void setTemperature(WashTemperatures temp) throws Exception;
}
