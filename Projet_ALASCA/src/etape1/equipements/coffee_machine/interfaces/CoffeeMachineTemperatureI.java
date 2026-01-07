package etape1.equipements.coffee_machine.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;

public interface CoffeeMachineTemperatureI {

	public Measure<Double> getTemperature() throws Exception;
}
