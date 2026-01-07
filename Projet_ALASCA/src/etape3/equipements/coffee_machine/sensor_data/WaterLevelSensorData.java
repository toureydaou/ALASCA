package etape3.equipements.coffee_machine.sensor_data;

import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>WaterLevelSensorData</code> implements a sensor data
 * containing the current water level in the reservoir of the 
 * {@code CoffeeMachineCyPhy} component sent to the 
 * {@code CoffeeMachineController} component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The water level is measured in liters and must be between 0 and the
 * maximum capacity (typically 1.8L). When the level is too low, the
 * machine should not allow coffee preparation to avoid damaging the
 * heating element.
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
public class WaterLevelSensorData 
extends SignalData<Double>
implements CoffeeMachineSensorDataI {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * create a water level sensor data with the standard timestamper.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code waterLevel != null}
     * pre	{@code waterLevel.getData() >= 0.0}
     * post	{@code isSingle()}
     * post	{@code getMeasure().equals(waterLevel)}
     * </pre>
     *
     * @param waterLevel	the current water level in liters.
     * @throws Exception	<i>to do</i>.
     */
    public WaterLevelSensorData(TimedMeasure<Double> waterLevel) 
    throws Exception {
        super(waterLevel);
        
        assert waterLevel != null :
            new PreconditionException("waterLevel != null");
        assert waterLevel.getData() >= 0.0 :
            new PreconditionException("waterLevel.getData() >= 0.0");
    }
}
