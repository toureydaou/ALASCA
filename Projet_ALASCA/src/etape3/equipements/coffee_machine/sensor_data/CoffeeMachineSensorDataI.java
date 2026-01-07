package etape3.equipements.coffee_machine.sensor_data;

import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;

/**
 * The interface <code>CoffeeMachineSensorDataI</code> defines a common super-type 
 * for coffee machine sensor data classes.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This interface extends the standard data interfaces from BCM4Java to mark
 * all sensor data classes that are specific to the coffee machine component.
 * It allows for type checking and polymorphic handling of different sensor
 * data types from the coffee machine.
 * </p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-01-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface CoffeeMachineSensorDataI
extends DataOfferedCI.DataI,
        DataRequiredCI.DataI {
}