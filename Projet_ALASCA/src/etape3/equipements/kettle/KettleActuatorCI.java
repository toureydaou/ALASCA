package etape3.equipements.kettle;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>KettleActuatorCI</code> declares the actuator
 * operations that can be performed on the kettle by a controller.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface allows external components (like a controller) to control
 * the kettle's operation: starting/stopping heating and setting the power mode.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public interface KettleActuatorCI
extends		OfferedCI,
			RequiredCI
{
	/**
	 * Start heating the water.
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void startHeating() throws Exception;

	/**
	 * Stop heating the water.
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void stopHeating() throws Exception;

	/**
	 * Set the power mode of the kettle.
	 *
	 * @param mode			the mode to set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void setMode(KettleMode mode) throws Exception;
}
// -----------------------------------------------------------------------------
