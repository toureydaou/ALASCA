package etape3.equipements.laundry.sensor_data;

import java.time.Instant;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryStateSensorData</code> represents the state sensor data
 * of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This class encapsulates the current state of the laundry machine including
 * its operational state and wash mode.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code state != null}
 * invariant	{@code washMode != null}
 * invariant	{@code timestamp != null}
 * </pre>
 */
public class LaundryStateSensorData
implements LaundrySensorDataI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** The current state of the laundry machine. */
	protected final LaundryState state;
	/** The current wash mode. */
	protected final LaundryWashMode washMode;
	/** The timestamp when this data was created. */
	protected final Instant timestamp;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a new laundry state sensor data.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code state != null}
	 * pre	{@code washMode != null}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param state		the current state of the laundry machine.
	 * @param washMode	the current wash mode.
	 */
	public LaundryStateSensorData(LaundryState state, LaundryWashMode washMode)
	{
		super();
		assert state != null;
		assert washMode != null;

		this.state = state;
		this.washMode = washMode;
		this.timestamp = Instant.now();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * Get the current state of the laundry machine.
	 *
	 * @return the current state.
	 */
	public LaundryState getState()
	{
		return this.state;
	}

	/**
	 * Get the current wash mode.
	 *
	 * @return the current wash mode.
	 */
	public LaundryWashMode getWashMode()
	{
		return this.washMode;
	}

	/**
	 * Get the timestamp when this data was created.
	 *
	 * @return the timestamp.
	 */
	public Instant getTimestamp()
	{
		return this.timestamp;
	}

	/**
	 * Check if the laundry machine is currently washing.
	 *
	 * @return true if the machine is in a washing state.
	 */
	public boolean isWashing()
	{
		return this.state == LaundryState.WASHING ||
			   this.state == LaundryState.RINSING ||
			   this.state == LaundryState.SPINNING ||
			   this.state == LaundryState.DRYING;
	}

	@Override
	public String toString()
	{
		return "LaundryStateSensorData[state=" + this.state +
			   ", washMode=" + this.washMode +
			   ", timestamp=" + this.timestamp + "]";
	}
}
// -----------------------------------------------------------------------------
