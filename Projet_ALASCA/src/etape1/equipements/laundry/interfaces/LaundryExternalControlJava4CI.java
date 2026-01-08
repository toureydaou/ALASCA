package etape1.equipements.laundry.interfaces;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>LaundryExternalControlJava4CI</code> extends
 * the external control interface with Java 1.4 compatible methods.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface provides methods using primitive types (int, double) instead
 * of enumerations for compatibility with Java 1.4.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface LaundryExternalControlJava4CI
extends LaundryExternalControlCI {

	// -------------------------------------------------------------------------
	// Java 1.4 compatible methods (using primitives instead of enums)
	// -------------------------------------------------------------------------

	/**
	 * set the wash mode using an integer code.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code getState() == LaundryState.ON}
	 * pre	{@code !isRunning()}
	 * pre	{@code mode >= 1 && mode <= 4}  // 1=DELICATE, 2=COLOR, 3=WHITE, 4=INTENSIVE
	 * post	{@code true}	// corresponding mode set
	 * </pre>
	 *
	 * @param mode			the wash mode as integer (1-4).
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setWashModeJava4(int mode) throws Exception;


	

	/**
	 * get the current state as an integer code.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0 && ret <= 5}  // 0=OFF, 1=ON, 2=WASHING, 3=RINSING, 4=SPINNING, 5=DRYING
	 * </pre>
	 *
	 * @return				the current state as integer (0-5).
	 * @throws Exception	<i>to do</i>.
	 */
	public int			getStateJava4() throws Exception;

	/**
	 * get the current wash mode as an integer code.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0 && ret <= 3}  // 0=WHITE, 1=COLOR, 2=DELICATE, 3=INTENSIVE
	 * </pre>
	 *
	 * @return				the current wash mode as integer (0-3).
	 * @throws Exception	<i>to do</i>.
	 */
	public int			getWashModeJava4() throws Exception;

	/**
	 * get the current wash temperature as an integer code.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0 && ret <= 6}  // 0=30°C, 1=40°C, ..., 6=90°C
	 * </pre>
	 *
	 * @return				the current temperature as integer (0-6).
	 * @throws Exception	<i>to do</i>.
	 */
	public double			getWashTemperatureJava4() throws Exception;

	/**
	 * get the current spin speed as an integer code.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0 && ret <= 5}  // 0=400RPM, 1=600RPM, ..., 5=1400RPM
	 * </pre>
	 *
	 * @return				the current spin speed as integer (0-5).
	 * @throws Exception	<i>to do</i>.
	 */
	public int			getSpinSpeedJava4() throws Exception;

	/**
	 * get the current power consumption as a double.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0.0}
	 * </pre>
	 *
	 * @return				the current power consumption in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getCurrentPowerJava4() throws Exception;
	
	/*
	 * set the current power consumption as a double.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret >= 0.0}
	 * </pre>
	 *
	 * @return				the current power consumption in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public void		setCurrentPowerJava4(double power) throws Exception;

	/**
	 * get the maximum power consumption as a double.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret > 0.0}
	 * </pre>
	 *
	 * @return				the maximum power consumption in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getMaxPowerLevelJava4() throws Exception;
}
// -----------------------------------------------------------------------------
