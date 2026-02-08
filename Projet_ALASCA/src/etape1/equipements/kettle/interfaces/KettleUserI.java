package etape1.equipements.kettle.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The interface <code>KettleUserI</code> defines the user-facing operations
 * for the water heater (chauffe-eau) component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface provides all operations available to the user, including
 * turning the water heater on/off, starting/stopping heating, setting the
 * target temperature, and choosing the power mode.
 * </p>
 *
 * <p>Created on : 2023-09-18</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface KettleUserI extends KettleExternalControlI {

	/**
	 * start heating the water to reach the target temperature.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == KettleState.ON}
	 * pre	{@code getKettleMode() != KettleMode.SUSPEND}
	 * post	{@code getState() == KettleState.HEATING}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void startHeating() throws Exception;

	/**
	 * stop heating the water manually.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == KettleState.HEATING}
	 * post	{@code getState() == KettleState.ON}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void stopHeating() throws Exception;

	/**
	 * set the target temperature for the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() != KettleState.OFF}
	 * pre	{@code temperature != null}
	 * pre	{@code temperature.getData() >= MIN_TARGET_TEMPERATURE}
	 * pre	{@code temperature.getData() <= MAX_TARGET_TEMPERATURE}
	 * post	{@code getTargetTemperature().getData() == temperature.getData()}
	 * </pre>
	 *
	 * @param temperature	the target temperature to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setTargetTemperature(Measure<Double> temperature) throws Exception;

	/**
	 * set the power mode of the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() != KettleState.OFF}
	 * pre	{@code mode != null}
	 * post	{@code getKettleMode() == mode}
	 * </pre>
	 *
	 * @param mode	the power mode to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setMode(KettleImplementationI.KettleMode mode) throws Exception;

	/**
	 * @see etape1.equipements.kettle.interfaces.KettleExternalControlI#turnOn()
	 */
	@Override
	public void turnOn() throws Exception;

	/**
	 * @see etape1.equipements.kettle.interfaces.KettleExternalControlI#turnOff()
	 */
	@Override
	public void turnOff() throws Exception;
}
