package etape1.equipements.coffee_machine.interfaces;

import physical_data.Measure;

public interface CoffeeMachineTemperatureI {

	public Measure<Double> getTemperature() throws Exception;
}
