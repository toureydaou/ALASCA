package etape3.equipements.laundry;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>LaundryActuatorCI</code> declares the signatures
 * of the actuator methods to be used with the laundry component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface defines the actions that a controller can perform on the
 * laundry machine: starting/stopping the wash cycle and changing the wash mode.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 */
public interface LaundryActuatorCI
extends		OfferedCI,
			RequiredCI
{
	/**
	 * Start the wash cycle.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isOn()}
	 * post	{@code isWashing()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void startWash() throws Exception;

	/**
	 * Stop/cancel the wash cycle.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isWashing()}
	 * post	{@code !isWashing()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void stopWash() throws Exception;

	/**
	 * Set the wash mode.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code mode != null}
	 * pre	{@code isOn()}
	 * post	{@code getWashMode() == mode}
	 * </pre>
	 *
	 * @param mode	the wash mode to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setWashMode(LaundryWashMode mode) throws Exception;

	/**
	 * Set the wash temperature.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code temperature >= 20.0 && temperature <= 90.0}
	 * pre	{@code isOn()}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param temperature	the wash temperature in degrees Celsius.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setWashTemperature(double temperature) throws Exception;

	/**
	 * Set the spin speed.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code spinSpeed >= 0 && spinSpeed <= 1600}
	 * pre	{@code isOn()}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param spinSpeed	the spin speed in RPM.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setSpinSpeed(int spinSpeed) throws Exception;
}
// -----------------------------------------------------------------------------
