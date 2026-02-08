package etape1.equipements.kettle.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The interface <code>KettleExternalControlI</code> defines the external
 * control operations for the water heater, used by HEM for energy management.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface provides operations that the HEM can use to control the
 * water heater's power consumption, including turning it on/off, suspending
 * and resuming operation, and querying/setting power levels.
 * </p>
 *
 * <p>Created on : 2023-09-18</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface KettleExternalControlI extends KettleImplementationI {

	/**
	 * turn on the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == KettleState.OFF}
	 * post	{@code getState() == KettleState.ON}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void turnOn() throws Exception;

	/**
	 * turn off the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == KettleState.ON}
	 * post	{@code getState() == KettleState.OFF}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void turnOff() throws Exception;

	/**
	 * suspend the water heater operation (HEM energy management).
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() != KettleState.OFF}
	 * pre	{@code !isSuspended()}
	 * post	{@code isSuspended()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void suspend() throws Exception;

	/**
	 * resume the water heater operation after suspension.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isSuspended()}
	 * post	{@code !isSuspended()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void resume() throws Exception;

	/**
	 * return true if the water heater is currently suspended.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the water heater is suspended.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean isSuspended() throws Exception;

	/**
	 * return the maximum power level of the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null && ret.getData() > 0.0}
	 * </pre>
	 *
	 * @return				the maximum power level.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double> getMaxPowerLevel() throws Exception;

	/**
	 * return the current power level of the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null && ret.getData() >= 0.0}
	 * </pre>
	 *
	 * @return				the current power level.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double> getCurrentPowerLevel() throws Exception;

	/**
	 * set the current power level of the water heater.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code powerLevel != null && powerLevel.getData() >= 0.0}
	 * pre	{@code powerLevel.getData() <= getMaxPowerLevel().getData()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param powerLevel	the power level to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception;
}
