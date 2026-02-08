package etape2.equipments.laundry.mil;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;
import fr.sorbonne_u.devs_simulation.models.time.Time;

/**
 * The interface <code>LaundryOperationI</code> defines the operations that
 * a laundry machine simulation model must provide to handle simulation events.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface LaundryOperationI {
	/**
	 * set the state of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code s != null}
	 * post	{@code getState() == s}
	 * </pre>
	 *
	 * @param s	the new state to be set.
	 */
	public void			setState(LaundryState s);

	/**
	 * return the current state of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current state of the laundry machine.
	 */
	public LaundryState	getState();

	/**
	 * set the wash mode of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code m != null}
	 * post	{@code getWashMode() == m}
	 * </pre>
	 *
	 * @param m	the new wash mode to be set.
	 */
	public void			setWashMode(LaundryWashMode m);

	/**
	 * return the current wash mode of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current wash mode of the laundry machine.
	 */
	public LaundryWashMode	getWashMode();

	/**
	 * set the state and wash mode of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code s != null && m != null}
	 * post	{@code getState() == s && getWashMode() == m}
	 * </pre>
	 *
	 * @param s	the new state to be set.
	 * @param m	the new wash mode to be set.
	 */
	public void setStateMode(LaundryState s, LaundryWashMode m);

	/**
	 * set the current washing power of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code newPower >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newPower	the new power in watts to be set on the laundry machine.
	 * @param t			time at which the new power is set.
	 */
	public void setCurrentWashingPower(double newPower, Time t);

	/**
	 * set the current water level in the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code newLevel >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newLevel	the new water level in liters.
	 * @param t			time at which the new level is set.
	 */
	public void setCurrentWaterLevel(double newLevel, Time t);

	/**
	 * set the current wash temperature of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code newTemp >= 0.0}
	 * post	{@code true}	// no postcondition.
	 */
	public void setCurrentWashTemperature(double newTemp, Time t);

	/**
	 * set the spin speed of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code speed != null}
	 * post	{@code getSpinSpeed() == speed}
	 * </pre>
	 *
	 * @param speed	the new spin speed to be set.
	 */
	public void setSpinSpeed(SpinSpeed speed);

	/**
	 * return the current spin speed of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current spin speed of the laundry machine.
	 */
	public SpinSpeed getSpinSpeed();
}
