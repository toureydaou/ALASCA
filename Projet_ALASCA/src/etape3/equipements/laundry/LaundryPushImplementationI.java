package etape3.equipements.laundry;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape3.equipements.laundry.sensor_data.WashProgressSensorData;

// -----------------------------------------------------------------------------
/**
 * The interface <code>LaundryPushImplementationI</code> declares the signatures
 * of the methods to be implemented by the laundry controller to receive the
 * sensor data from the laundry in push mode.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface must be implemented by the controller component that receives
 * push notifications from the laundry machine. The controller uses these
 * methods to process state changes and wash progress updates.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 */
public interface LaundryPushImplementationI
{
	/**
	 * Receive and process the laundry state coming from the laundry component,
	 * starting the control loop if the state has changed from {@code OFF} to
	 * {@code ON}.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code laundryState != null}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param laundryState	laundry state to be received from the laundry component.
	 */
	public void processLaundryState(LaundryState laundryState);

	/**
	 * Receive and process the wash progress data pushed from the laundry.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code washProgress != null}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param washProgress	wash progress data to be received from the laundry.
	 */
	public void processWashProgress(WashProgressSensorData washProgress);

	/**
	 * Receive and process a wash cycle completion notification.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param success	true if the wash cycle completed successfully.
	 */
	public void processWashCycleComplete(boolean success);
}
// -----------------------------------------------------------------------------
