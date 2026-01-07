package etape3.equipements.coffee_machine.connections.ports;

import java.util.concurrent.TimeUnit;

import etape3.equipements.coffee_machine.interfaces.CoffeeMachinePushImplementationI;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineCompoundSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineModeSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineSensorDataI;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineStateSensorData;
import etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData;
import etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;

/**
 * The class <code>CoffeeMachineSensorDataOutboundPort</code> implements the 
 * outbound port for the {@code CoffeeMachineSensorDataCI} component data 
 * interface, and as such must implement the
 * {@code CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI} pull 
 * interface as well as the method {@code receive} from the 
 * {@code DataRequiredCI.PushCI} push interface to call the appropriate client 
 * component method to receive data pushed from the server component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This outbound port is used by the controller component to:
 * <ul>
 * <li>Pull sensor data from the coffee machine (pull protocol)</li>
 * <li>Receive sensor data pushed by the coffee machine (push protocol)</li>
 * <li>Start/stop the push sensor mechanism</li>
 * </ul>
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
public class CoffeeMachineSensorDataOutboundPort 
extends AbstractDataOutboundPort
implements CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI {

    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------
    
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    
    /**
     * create the outbound port.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code owner != null}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param owner			component that owns this port.
     * @throws Exception 	<i>to do</i>.
     */
    public CoffeeMachineSensorDataOutboundPort(ComponentI owner) 
    throws Exception {
        super(DataRequiredCI.PullCI.class,
              DataRequiredCI.PushCI.class, 
              owner);
    }

    /**
     * create the outbound port.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code uri != null && !uri.isEmpty()}
     * pre	{@code owner != null}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param uri			unique identifier of the port.
     * @param owner			component that owns this port.
     * @throws Exception 	<i>to do</i>.
     */
    public CoffeeMachineSensorDataOutboundPort(String uri, ComponentI owner) 
    throws Exception {
        super(uri, 
              CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI.class,
              DataRequiredCI.PushCI.class, 
              owner);
    }

    // -------------------------------------------------------------------------
    // Methods - Pull Protocol
    // -------------------------------------------------------------------------

    /**
     * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#statePullSensor()
     */
    @Override
    public CoffeeMachineStateSensorData statePullSensor() throws Exception {
        return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) 
                this.getConnector()).statePullSensor();
    }

    /**
     * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#modePullSensor()
     */
    @Override
    public CoffeeMachineModeSensorData modePullSensor() throws Exception {
        return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) 
                this.getConnector()).modePullSensor();
    }

    /**
     * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#temperaturePullSensor()
     */
    @Override
    public TemperatureSensorData temperaturePullSensor() throws Exception {
        return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) 
                this.getConnector()).temperaturePullSensor();
    }

    /**
     * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#waterLevelPullSensor()
     */
    @Override
    public WaterLevelSensorData waterLevelPullSensor() throws Exception {
        return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) 
                this.getConnector()).waterLevelPullSensor();
    }

    /**
     * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#startSensorDataPush(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void startSensorDataPush(long controlPeriod, TimeUnit tu) 
    throws Exception {
        ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) 
                this.getConnector()).startSensorDataPush(controlPeriod, tu);
    }

    // -------------------------------------------------------------------------
    // Methods - Push Protocol
    // -------------------------------------------------------------------------

    /**
     * receive data pushed by the coffee machine and dispatch to the appropriate
     * handler method in the controller component.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * This method is called when the coffee machine pushes sensor data to the
     * controller. It identifies the type of data received and calls the
     * appropriate processing method in the owner component (the controller).
     * </p>
     * <p>
     * The method handles three types of sensor data:
     * <ul>
     * <li><b>CoffeeMachineStateSensorData:</b> State changes (ON/OFF/HEATING).
     *     This is typically sent when the machine is switched on or off to
     *     trigger the start/stop of the control loop in push mode.</li>
     * <li><b>CoffeeMachineCompoundSensorData:</b> Complete sensor readings
     *     including state, mode, temperature, and water level. This is sent
     *     periodically during the push control mode.</li>
     * <li><b>Individual sensor data:</b> Single sensor readings (mode, 
     *     temperature, water level) if needed for specific purposes.</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code d != null}
     * pre	{@code d instanceof CoffeeMachineSensorDataI}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param d				the sensor data received from the coffee machine.
     * @throws Exception	<i>to do</i>.
     */
    @Override
    public void receive(DataRequiredCI.DataI d) throws Exception {
        assert d instanceof CoffeeMachineSensorDataI :
            new BCMException("d instanceof CoffeeMachineSensorDataI");

        // Case 1: State sensor data - typically sent on/off to start/stop control
        if (d instanceof CoffeeMachineStateSensorData) {
            this.getOwner().runTask(
                o -> {
					try {
						((CoffeeMachinePushImplementationI)o)
						    .processCoffeeMachineState(
						        ((CoffeeMachineStateSensorData)d).getMeasure().getData());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
        } 
        // Case 2: Compound sensor data - all sensors together (most common in push mode)
        else if (d instanceof CoffeeMachineCompoundSensorData) {
            CoffeeMachineCompoundSensorData compoundData = (CoffeeMachineCompoundSensorData)d;
            this.getOwner().runTask(
                o -> {
					try {
						((CoffeeMachinePushImplementationI)o)
						    .processCoffeeMachineData(
						        compoundData.getState(),
						        compoundData.getMode(),
						        compoundData.getTemperature(),
						        compoundData.getWaterLevel());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
        }
        // Case 3: Individual sensor data (mode, temperature, water level)
        else if (d instanceof CoffeeMachineModeSensorData) {
            this.getOwner().runTask(
                o -> {
					try {
						((CoffeeMachinePushImplementationI)o)
						    .processCoffeeMachineMode(
						        ((CoffeeMachineModeSensorData)d).getMeasure().getData());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
        }
        else if (d instanceof TemperatureSensorData) {
            this.getOwner().runTask(
                o -> {
					try {
						((CoffeeMachinePushImplementationI)o)
						    .processCoffeeMachineTemperature(
						        ((TemperatureSensorData)d));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
        }
        else if (d instanceof WaterLevelSensorData) {
            this.getOwner().runTask(
                o -> {
					try {
						((CoffeeMachinePushImplementationI)o)
						    .processCoffeeMachineWaterLevel(
						        ((WaterLevelSensorData)d));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
        }
        else {
            throw new BCMException("Unknown coffee machine sensor data: " + d);
        }
    }
}
// -----------------------------------------------------------------------------