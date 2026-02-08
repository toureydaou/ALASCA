package etape2.equipments.kettle.mil;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The interface <code>KettleOperationI</code> defines the operations that
 * events can call on the kettle (water heater) simulation models.
 *
 * <p>Created on : 2026-02-06</p>
 */
public interface KettleOperationI {

	/**
	 * set the state of the kettle.
	 *
	 * @param s	the new state to be set.
	 */
	public void			setState(KettleState s);

	/**
	 * return the current state of the kettle.
	 *
	 * @return	the current state of the kettle.
	 */
	public KettleState	getState();

	/**
	 * set the mode of the kettle.
	 *
	 * @param m	the new mode to be set.
	 */
	public void			setMode(KettleMode m);

	/**
	 * return the current mode of the kettle.
	 *
	 * @return	the current mode of the kettle.
	 */
	public KettleMode	getMode();

	/**
	 * set the current heating power of the kettle.
	 *
	 * @param newPower	the new power in watts.
	 * @param t			time at which the new power is set.
	 */
	public void			setCurrentHeatingPower(double newPower, Time t);
}
// -----------------------------------------------------------------------------
