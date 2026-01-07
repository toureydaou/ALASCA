package etape3.equipements.coffee_machine.interfaces;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineModeSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineStateSensorData;
import etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData;
import etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData;

/**
 * The interface <code>CoffeeMachinePushImplementationI</code> defines the 
 * methods that must be implemented by a controller component to receive and 
 * process sensor data pushed by the coffee machine.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This interface is used in the push control mode where the coffee machine
 * actively sends sensor data to the controller at regular intervals. The
 * controller component must implement these methods to handle the incoming
 * data and perform appropriate control actions.
 * </p>
 * <p>
 * The interface provides two main approaches:
 * <ol>
 * <li><b>Process state only:</b> {@code processCoffeeMachineState} is called
 *     when the machine changes state (typically on/off transitions) to 
 *     start/stop the control loop.</li>
 * <li><b>Process all sensor data:</b> {@code processCoffeeMachineData} is 
 *     called periodically with complete sensor readings to perform the control
 *     logic.</li>
 * </ol>
 * </p>
 * <p>
 * Additional methods are provided to process individual sensor data types if
 * needed for more granular control.
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
public interface CoffeeMachinePushImplementationI {
    
    /**
     * process a coffee machine state change.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * This method is called when the coffee machine pushes its state, typically
     * when it is switched on or off. The controller should use this to:
     * <ul>
     * <li>Start the control loop when state is ON or HEATING</li>
     * <li>Stop the control loop when state is OFF</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code state != null}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param state			the current state of the coffee machine.
     * @throws Exception	<i>to do</i>.
     */
    public void processCoffeeMachineState(CoffeeMachineState state) 
    throws Exception;
    
    /**
     * process complete coffee machine sensor data.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * This is the main method called periodically in push mode with all sensor
     * readings. The controller should implement the control logic here, such as:
     * <ul>
     * <li>Check if water level is sufficient</li>
     * <li>Verify temperature is in acceptable range</li>
     * <li>Adjust mode based on temperature and energy optimization</li>
     * <li>Decide whether to start/stop heating</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code state != null}
     * pre	{@code mode != null}
     * pre	{@code temperature != null}
     * pre	{@code waterLevel != null}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param state			the current state sensor data.
     * @param mode			the current mode sensor data.
     * @param temperature	the current temperature sensor data.
     * @param waterLevel	the current water level sensor data.
     * @throws Exception	<i>to do</i>.
     */
    public void processCoffeeMachineData(
        CoffeeMachineStateSensorData state,
        CoffeeMachineModeSensorData mode,
        TemperatureSensorData temperature,
        WaterLevelSensorData waterLevel
    ) throws Exception;
    
    /**
     * process a coffee machine mode change.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * This method can be used if the controller needs to react specifically to
     * mode changes (SUSPEND, ECO, NORMAL, MAX).
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code mode != null}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param mode			the current mode of the coffee machine.
     * @throws Exception	<i>to do</i>.
     */
    public void processCoffeeMachineMode(CoffeeMachineMode mode) 
    throws Exception;
    
    /**
     * process a coffee machine temperature reading.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * This method can be used if the controller needs to react specifically to
     * temperature changes, for example to trigger heating or cooling actions.
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code temperature != null}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param temperature	the current temperature sensor data.
     * @throws Exception	<i>to do</i>.
     */
    public void processCoffeeMachineTemperature(TemperatureSensorData temperature) 
    throws Exception;
    
    /**
     * process a coffee machine water level reading.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * This method can be used if the controller needs to react specifically to
     * water level changes, for example to alert when water is low or to prevent
     * coffee preparation when insufficient water is available.
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code waterLevel != null}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param waterLevel	the current water level sensor data.
     * @throws Exception	<i>to do</i>.
     */
    public void processCoffeeMachineWaterLevel(WaterLevelSensorData waterLevel) 
    throws Exception;
}
// -----------------------------------------------------------------------------