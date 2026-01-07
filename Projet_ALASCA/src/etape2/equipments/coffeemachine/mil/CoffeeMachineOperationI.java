package etape2.equipments.coffeemachine.mil;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import fr.sorbonne_u.devs_simulation.models.time.Time;

public interface CoffeeMachineOperationI {
	/**
	 * set the state of the coffee machine.
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
	public void			setState(CoffeeMachineState s);

	/**
	 * return the current state of the coffee machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current state of the coffee machine.
	 */
	public CoffeeMachineState	getState();

	/**
	 * set the mode of the coffee machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code m != null}
	 * post	{@code getMode() == m}
	 * </pre>
	 *
	 * @param m	the new mode to be set.
	 */
	public void			setMode(CoffeeMachineMode m);

	/**
	 * return the current mode of the coffee machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current mode of the coffee machine.
	 */
	public CoffeeMachineMode	getMode();

	public void setStateMode(CoffeeMachineState on, CoffeeMachineMode normal);

	/**
	 * set the current heating power of the coffee machine.
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
	public void setCurrentHeatingPower(double newPower, Time t);

	/**
	 * set the current water level in the coffee machine.
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
}
