package etape3.equipements.coffee_machine.sensor_data;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;


/**
 * The class <code>CoffeeMachineModeSensorData</code> implements a sensor data
 * containing the most recent mode of the {@code CoffeeMachineCyPhy} component
 * sent to the {@code CoffeeMachineController} component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The coffee machine can operate in different modes (SUSPEND, ECO, NORMAL, MAX)
 * which affect the power consumption and target temperature. This sensor data
 * allows the controller to know which mode is currently active.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
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
public class CoffeeMachineModeSensorData 
extends SignalData<CoffeeMachineMode>
implements CoffeeMachineSensorDataI {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * create a coffee machine mode sensor data.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code mode != null}
     * post	{@code isSingle()}
     * post	{@code getTimestamp().equals(Instant.ofEpochMilli(System.currentTimeMillis())}
     * post	{@code getTimeReference().equals(getStandardTimestamper())}
     * post	{@code getMeasure().getData().equals(mode)}
     * </pre>
     *
     * @param mode			the mode of the coffee machine (SUSPEND, ECO, NORMAL, MAX).
     * @throws Exception	<i>to do</i>.
     */
    public CoffeeMachineModeSensorData(CoffeeMachineMode mode) 
    throws Exception {
        super(AssertionChecking.assertTrueAndReturnOrThrow(
                mode != null,
                new Measure<CoffeeMachineMode>(mode),
                () -> new PreconditionException("mode != null")));
    }
}
