package etape3.equipements.coffee_machine.connections.ports;


import java.util.concurrent.TimeUnit;

import etape3.equipements.coffee_machine.CoffeeMachineCyPhy;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineModeSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineStateSensorData;
import etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData;
import etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataOfferedCI.DataI;
import fr.sorbonne_u.components.ports.AbstractDataInboundPort;


/*
* 
* The class <code>CoffeeMachineSensorDataInboundPort</code> implements the 
* inbound port for the {@code CoffeeMachineSensorDataCI} component data 
* interface, and as such must implement the
* {@code CoffeeMachineSensorDataCI.CoffeeMachineSensorOfferedPullCI} pull 
* interface as well as the method {@code get} from the 
* {@code DataOfferedCI.PullCI} pull interface it extends to pull data from 
* the server.
*
* <p><strong>Description</strong></p>
* 
* <p>
* This port handles both pull and push protocols for sensor data:
* - Pull: The controller requests data from the coffee machine
* - Push: The coffee machine sends data to the controller periodically
* </p>
 * 
 * <p>Created on : 2025-01-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class CoffeeMachineSensorDataInboundPort 
extends AbstractDataInboundPort
implements CoffeeMachineSensorDataCI.CoffeeMachineSensorOfferedPullCI {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * create an inbound port.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code owner instanceof CoffeeMachineCyPhy}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param owner			component that owns this port.
     * @throws Exception 	<i>to do</i>.
     */
    public CoffeeMachineSensorDataInboundPort(ComponentI owner) 
    throws Exception {
        super(
              CoffeeMachineSensorDataCI.CoffeeMachineSensorOfferedPullCI.class, 
              DataOfferedCI.PushCI.class,
              owner);
        assert owner instanceof CoffeeMachineCyPhy;
    }
    
    /**
     * create an inbound port.
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code owner instanceof CoffeeMachineCyPhy}
     * post	{@code true}	// no postcondition.
     * </pre>
     *
     * @param uri			unique identifier of the port.
     * @param owner			component that owns this port.
     * @throws Exception 	<i>to do</i>.
     */
    public CoffeeMachineSensorDataInboundPort(String uri, ComponentI owner) 
    throws Exception {
        super(uri, 
              CoffeeMachineSensorDataCI.CoffeeMachineSensorOfferedPullCI.class, 
              DataOfferedCI.PushCI.class,
              owner);
        assert owner instanceof CoffeeMachineCyPhy;
    }
    
    /**
     * @see etape1.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#statePullSensor()
     */
    @Override
    public CoffeeMachineStateSensorData statePullSensor() throws Exception {
        return this.getOwner().handleRequest(
            o -> ((CoffeeMachineCyPhy)o).statePullSensor());
    }
    
    /**
     * @see etape1.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#modePullSensor()
     */
    @Override
    public CoffeeMachineModeSensorData modePullSensor() throws Exception {
        return this.getOwner().handleRequest(
            o -> ((CoffeeMachineCyPhy)o).modePullSensor());
    }
    
    /**
     * @see etape1.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#temperaturePullSensor()
     */
    @Override
    public TemperatureSensorData temperaturePullSensor() throws Exception {
        return this.getOwner().handleRequest(
            o -> ((CoffeeMachineCyPhy)o).temperaturePullSensor());
    }
    
    /**
     * @see etape1.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#waterLevelPullSensor()
     */
    @Override
    public WaterLevelSensorData waterLevelPullSensor() throws Exception {
        return this.getOwner().handleRequest(
            o -> ((CoffeeMachineCyPhy)o).waterLevelPullSensor());
    }
    
    /**
     * @see etape1.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI.CoffeeMachineSensorCI#startSensorDataPush(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void startSensorDataPush(long controlPeriod, TimeUnit tu) 
    throws Exception {
        this.getOwner().handleRequest(
            o -> {
                ((CoffeeMachineCyPhy)o).startSensorDataPush(controlPeriod, tu);
                return null;
            });
    }
    

    /**
     * @see fr.sorbonne_u.components.interfaces.DataOfferedCI.PullCI#get()
     */
    @Override
    public DataI get() throws Exception {
        return this.getOwner().handleRequest(
            o -> ((CoffeeMachineCyPhy)o).sensorDataCompound());
    }
}