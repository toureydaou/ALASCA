package etape1.equipements.laundry.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The interface <code>LaundryUserI</code> defines the user-level services
 * of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface gathers all the services that a user can directly call on
 * the laundry machine component.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface LaundryUserI extends LaundryImplementationI {

	// -------------------------------------------------------------------------
	// Power control
	// -------------------------------------------------------------------------

	/**
	 * turn on the laundry machine.
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
	 * turn off the laundry machine.
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
	// Wash cycle control
	// -------------------------------------------------------------------------

	/**
	 * start the wash cycle with current settings.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * post	{@code getState() == LaundryState.WASHING}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			startWash() throws Exception;

	/**
	 * cancel the current wash cycle.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isRunning()}
	 * post	{@code getState() == LaundryState.ON}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			cancelWash() throws Exception;

	// -------------------------------------------------------------------------
	// Settings
	// -------------------------------------------------------------------------

	/**
	 * set the wash mode to WHITE.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * pre	{@code !isRunning()}
	 * post	{@code getWashMode() == LaundryWashMode.WHITE}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setWhiteMode() throws Exception;

	/**
	 * set the wash mode to COLOR.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * pre	{@code !isRunning()}
	 * post	{@code getWashMode() == LaundryWashMode.COLOR}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setColorMode() throws Exception;

	/**
	 * set the wash mode to DELICATE.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * pre	{@code !isRunning()}
	 * post	{@code getWashMode() == LaundryWashMode.DELICATE}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setDelicateMode() throws Exception;

	/**
	 * set the wash mode to INTENSIVE.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * pre	{@code !isRunning()}
	 * post	{@code getWashMode() == LaundryWashMode.INTENSIVE}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setIntensiveMode() throws Exception;

	/**
	 * set the wash temperature.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * pre	{@code !isRunning()}
	 * pre	{@code temp != null}
	 * post	{@code getWashTemperature() == temp}
	 * </pre>
	 *
	 * @param temp			the wash temperature to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setWashTemperature(Measure<Double>  temp) throws Exception;

	/**
	 * set the spin speed.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * pre	{@code !isRunning()}
	 * pre	{@code speed != null}
	 * post	{@code getSpinSpeed() == speed}
	 * </pre>
	 *
	 * @param speed			the spin speed to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setSpinSpeed(SpinSpeed speed) throws Exception;
}
// -----------------------------------------------------------------------------
