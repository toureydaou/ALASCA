package etape3.equipements.coffee_machine.sensor_data;

import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;
import fr.sorbonne_u.exceptions.PreconditionException;


/**
 * The class <code>TemperatureSensorData</code> implements a sensor data
 * containing the most recent water temperature of the {@code CoffeeMachineCyPhy} 
 * component sent to the {@code CoffeeMachineController} component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The water temperature is a critical parameter for the coffee machine operation.
 * It determines the quality of the coffee and the energy consumption. The
 * temperature varies between 20°C (ambient) and 100°C (maximum).
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
public class TemperatureSensorData 
extends SignalData<Double>
implements CoffeeMachineSensorDataI {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * create a temperature sensor data with the standard timestamper.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code temperature != null}
     * post	{@code isSingle()}
     * post	{@code getMeasure().equals(temperature)}
     * </pre>
     *
     * @param temperature	the current water temperature.
     * @throws Exception	<i>to do</i>.
     */
    public TemperatureSensorData(TimedMeasure<Double> temperature) 
    throws Exception {
        super(temperature);
        
        assert temperature != null :
            new PreconditionException("temperature != null");
    }
}
