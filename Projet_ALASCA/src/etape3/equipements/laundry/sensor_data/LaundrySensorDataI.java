package etape3.equipements.laundry.sensor_data;

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>LaundrySensorDataI</code> defines a common super-type for
 * laundry sensor data classes.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface is used to type the sensor data exchanged between the laundry
 * component and its controller. All specific sensor data classes must implement
 * this interface.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 */
public interface LaundrySensorDataI
extends		DataOfferedCI.DataI,
			DataRequiredCI.DataI
{
}
// -----------------------------------------------------------------------------
