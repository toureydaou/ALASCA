package etape3.equipements.kettle.sensor_data;

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;

// -----------------------------------------------------------------------------
/**
 * The interface <code>KettleSensorDataI</code> defines a common super-type
 * for kettle sensor data classes.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface extends the standard data interfaces from BCM4Java to mark
 * all sensor data classes that are specific to the kettle component.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public interface KettleSensorDataI
extends DataOfferedCI.DataI,
        DataRequiredCI.DataI {
}
// -----------------------------------------------------------------------------
