package etape3.equipements.laundry;

import etape1.equipements.laundry.interfaces.LaundryImplementationI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>LaundryInternalControlI</code> defines the signatures of
 * the services offered by the laundry machine to its controller.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface extends {@code LaundryImplementationI} to add internal control
 * methods that allow a controller component to start and stop wash cycles,
 * and to change wash parameters.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 */
public interface LaundryInternalControlI
extends		LaundryImplementationI
{
	/**
	 * Return true if the laundry machine is currently on (not OFF).
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}
	 * post	{@code true}
	 * </pre>
	 *
	 * @return				true if the laundry machine is on.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean isOn() throws Exception;

	/**
	 * Return true if the laundry machine is currently washing (in WASHING,
	 * RINSING, SPINNING, or DRYING state).
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isOn()}
	 * post	{@code true}
	 * </pre>
	 *
	 * @return				true if the laundry machine is washing.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean isWashing() throws Exception;

	/**
	 * Start the wash cycle.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isOn()}
	 * pre	{@code !isWashing()}
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
	 * pre	{@code isOn()}
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
	 * pre	{@code spinSpeed != null}
	 * pre	{@code isOn()}
	 * post	{@code getSpinSpeed() == spinSpeed}
	 * </pre>
	 *
	 * @param spinSpeed	the spin speed to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setSpinSpeed(SpinSpeed spinSpeed) throws Exception;

	/**
	 * Get the current water level in liters.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isOn()}
	 * post	{@code return >= 0.0}
	 * </pre>
	 *
	 * @return				the current water level in liters.
	 * @throws Exception	<i>to do</i>.
	 */
	public double getWaterLevel() throws Exception;

	/**
	 * Get the current power consumption in watts.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}
	 * post	{@code return >= 0.0}
	 * </pre>
	 *
	 * @return				the current power consumption in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public double getPowerConsumption() throws Exception;
}
// -----------------------------------------------------------------------------
