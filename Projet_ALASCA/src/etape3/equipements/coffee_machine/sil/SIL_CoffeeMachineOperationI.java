package etape3.equipements.coffee_machine.sil;

import etape2.equipments.coffeemachine.mil.CoffeeMachineOperationI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The interface <code>SIL_CoffeeMachineOperationI</code> defines the common
 * operations used by events to execute on the coffee machine SIL models.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface extends the basic coffee machine operations to support
 * Software-In-the-Loop simulation. It adds methods to set the current heating
 * power and water level at specific simulation times.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 *
 * <p>Created on : 2025-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		SIL_CoffeeMachineOperationI extends CoffeeMachineOperationI
{
	

	/**
	 * set the current heating power of the coffee machine to {@code newPower}.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code newPower >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newPower	the new power in watts to be set on the coffee machine.
	 * @param t			time at which the new power is set.
	 */
	public void			setCurrentHeatingPower(double newPower, Time t);

	/**
	 * set the current water level in the coffee machine to {@code newLevel}.
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
	public void			setCurrentWaterLevel(double newLevel, Time t);
}
// -----------------------------------------------------------------------------
