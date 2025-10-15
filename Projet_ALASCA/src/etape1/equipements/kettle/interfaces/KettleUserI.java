package etape1.equipements.kettle.interfaces;

public interface KettleUserI extends KettleExternalControlI {
	

	/**
	 * set the Kettle in high mode.
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
	public void			setTotalMode() throws Exception;
	
	/**
	 * set the Kettle in high mode.
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
	public void			setPartialMode() throws Exception;



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
	
}
