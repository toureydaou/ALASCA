package etape1.equipements.laundry.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The interface <code>LaundryExternalControlI</code> defines the external
 * control services (HEM) of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface gathers the services that the Home Energy Manager (HEM) can
 * call to control and monitor the laundry machine. These services are a subset
 * of the user services, focused on energy management and remote control.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface LaundryExternalControlI extends LaundryImplementationI {
	
	public void setMode(int mode) throws Exception;
	

	// -------------------------------------------------------------------------
	// Power control
	// -------------------------------------------------------------------------

	/**
	 * turn on the laundry machine remotely.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.OFF}
	 * post	{@code getState() == LaundryState.ON}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			turnOn() throws Exception;

	/**
	 * turn off the laundry machine remotely.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() != LaundryState.OFF}
	 * pre	{@code !isRunning()}
	 * post	{@code getState() == LaundryState.OFF}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			turnOff() throws Exception;

	// -------------------------------------------------------------------------
	// Energy management
	// -------------------------------------------------------------------------

	/**
	 * suspend the laundry machine operation (for energy management).
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code true}	// machine paused
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			suspend() throws Exception;

	/**
	 * resume the laundry machine operation after suspension.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// machine was suspended
	 * post	{@code isRunning()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			resume() throws Exception;

	/**
	 * get the maximum power level of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null && ret.getData() > 0.0}
	 * </pre>
	 *
	 * @return				the maximum power consumption as a Measure in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	getMaxPowerLevel() throws Exception;

	/**
	 * get the current power level of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null && ret.getData() >= 0.0}
	 * </pre>
	 *
	 * @return				the current power consumption as a Measure in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	
	
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception;
	
	public Measure<Double>	getCurrentPowerLevel() throws Exception;
	
	public Measure<Double> getCurrentWashTemperature() throws Exception;
}
// -----------------------------------------------------------------------------
