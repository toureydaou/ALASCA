package etape3.equipements.coffee_machine.connections.connectors;

import java.util.concurrent.TimeUnit;

import etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineModeSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineStateSensorData;
import etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData;
import etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData;
import fr.sorbonne_u.components.connectors.DataConnector;

public class CoffeeMachineSensorDataConnector extends DataConnector
		implements CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI {

	@Override
	public CoffeeMachineStateSensorData statePullSensor() throws Exception {

		return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) this.offering).statePullSensor();
	}

	@Override
	public CoffeeMachineModeSensorData modePullSensor() throws Exception {

		return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) this.offering).modePullSensor();
	}

	@Override
	public TemperatureSensorData temperaturePullSensor() throws Exception {

		return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) this.offering).temperaturePullSensor();
	}

	@Override
	public WaterLevelSensorData waterLevelPullSensor() throws Exception {

		return ((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) this.offering).waterLevelPullSensor();
	}

	@Override
	public void startSensorDataPush(long controlPeriod, TimeUnit tu) throws Exception {

		((CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI) this.offering).startSensorDataPush(controlPeriod,
				tu);
	}

}
