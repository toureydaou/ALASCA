package etape3.equipements.coffee_machine.sensor_data;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;


/**
 * The class <code>CoffeeMachineStateSensorData</code> implements a sensor data
 * containing the most recent state of the {@code CoffeeMachineCyPhy} component
 * sent to the {@code CoffeeMachineController} component.
 *
 * <p><strong>Description</strong></p>
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
public class CoffeeMachineStateSensorData 
extends SignalData<CoffeeMachineState>
implements CoffeeMachineSensorDataI {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * create a coffee machine state sensor data.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code state != null}
     * post	{@code isSingle()}
     * post	{@code getTimestamp().equals(Instant.ofEpochMilli(System.currentTimeMillis())}
     * post	{@code getTimeReference().equals(getStandardTimestamper())}
     * post	{@code getMeasure().getData().equals(state)}
     * </pre>
     *
     * @param state			the state of the coffee machine.
     * @throws Exception	<i>to do</i>.
     */
    public CoffeeMachineStateSensorData(CoffeeMachineState state) 
    throws Exception {
        super(AssertionChecking.assertTrueAndReturnOrThrow(
                state != null,
                new Measure<CoffeeMachineState>(state),
                () -> new PreconditionException("state != null")));
    }
}