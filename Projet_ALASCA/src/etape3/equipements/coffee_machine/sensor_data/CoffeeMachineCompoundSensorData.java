package etape3.equipements.coffee_machine.sensor_data;

import fr.sorbonne_u.exceptions.PreconditionException;
/*
**
* The class <code>CoffeeMachineCompoundSensorData</code> implements a compound
* sensor data containing all sensor readings from the {@code CoffeeMachineCyPhy}
* component sent to the {@code CoffeeMachineController} component.
*
* <p><strong>Description</strong></p>
* 
* <p>
* This compound sensor data aggregates all the individual sensor data from the
* coffee machine into a single object that can be transmitted in one push
* operation. This is particularly useful for the push control mode where all
* sensor readings need to be sent together to the controller.
* </p>
* <p>
* The compound data includes:
* <ul>
* <li>State: OFF, ON, or HEATING</li>
* <li>Mode: SUSPEND, ECO, NORMAL, or MAX</li>
* <li>Temperature: current water temperature in °C</li>
* <li>Water level: current water level in liters</li>
* </ul>
* </p>
* 
* <p><strong>Implementation Invariants</strong></p>
* 
* <pre>
* invariant	{@code getState() != null}
* invariant	{@code getMode() != null}
* invariant	{@code getTemperature() != null}
* invariant	{@code getWaterLevel() != null}
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
public class CoffeeMachineCompoundSensorData 
implements CoffeeMachineSensorDataI {
   
   private static final long serialVersionUID = 1L;
   
   /** the state sensor data.												*/
   protected final CoffeeMachineStateSensorData state;
   /** the mode sensor data.												*/
   protected final CoffeeMachineModeSensorData mode;
   /** the temperature sensor data.										*/
   protected final TemperatureSensorData temperature;
   /** the water level sensor data.										*/
   protected final WaterLevelSensorData waterLevel;
   
   /**
    * create a compound sensor data from individual coffee machine sensor data.
    * 
    * <p><strong>Contract</strong></p>
    * 
    * <pre>
    * pre	{@code state != null}
    * pre	{@code mode != null}
    * pre	{@code temperature != null}
    * pre	{@code waterLevel != null}
    * post	{@code getState().equals(state)}
    * post	{@code getMode().equals(mode)}
    * post	{@code getTemperature().equals(temperature)}
    * post	{@code getWaterLevel().equals(waterLevel)}
    * </pre>
    *
    * @param state			the state sensor data.
    * @param mode			the mode sensor data.
    * @param temperature	the temperature sensor data.
    * @param waterLevel	the water level sensor data.
    * @throws Exception	<i>to do</i>.
    */
   public CoffeeMachineCompoundSensorData(
       CoffeeMachineStateSensorData state,
       CoffeeMachineModeSensorData mode,
       TemperatureSensorData temperature,
       WaterLevelSensorData waterLevel
   ) throws Exception {
       assert state != null :
           new PreconditionException("state != null");
       assert mode != null :
           new PreconditionException("mode != null");
       assert temperature != null :
           new PreconditionException("temperature != null");
       assert waterLevel != null :
           new PreconditionException("waterLevel != null");
       
       this.state = state;
       this.mode = mode;
       this.temperature = temperature;
       this.waterLevel = waterLevel;
   }
   
   /**
    * return the state sensor data.
    * 
    * <p><strong>Contract</strong></p>
    * 
    * <pre>
    * pre	{@code true}	// no precondition.
    * post	{@code return != null}
    * </pre>
    *
    * @return	the state sensor data.
    */
   public CoffeeMachineStateSensorData getState() {
       return this.state;
   }
   
   /**
    * return the mode sensor data.
    * 
    * <p><strong>Contract</strong></p>
    * 
    * <pre>
    * pre	{@code true}	// no precondition.
    * post	{@code return != null}
    * </pre>
    *
    * @return	the mode sensor data.
    */
   public CoffeeMachineModeSensorData getMode() {
       return this.mode;
   }
   
   /**
    * return the temperature sensor data.
    * 
    * <p><strong>Contract</strong></p>
    * 
    * <pre>
    * pre	{@code true}	// no precondition.
    * post	{@code return != null}
    * </pre>
    *
    * @return	the temperature sensor data.
    */
   public TemperatureSensorData getTemperature() {
       return this.temperature;
   }
   
   /**
    * return the water level sensor data.
    * 
    * <p><strong>Contract</strong></p>
    * 
    * <pre>
    * pre	{@code true}	// no precondition.
    * post	{@code return != null}
    * </pre>
    *
    * @return	the water level sensor data.
    */
   public WaterLevelSensorData getWaterLevel() {
       return this.waterLevel;
   }
   
   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
       StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
       sb.append('[');
       sb.append("state=");
       sb.append(this.state);
       sb.append(", mode=");
       sb.append(this.mode);
       sb.append(", temperature=");
       sb.append(this.temperature);
       sb.append(", waterLevel=");
       sb.append(this.waterLevel);
       sb.append(']');
       return sb.toString();
   }
}