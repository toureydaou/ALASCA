package etape1.equipements.coffee_machine.connectors;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.connectors.AbstractConnector;

public class CoffeeMachineExternalControlConnector extends AbstractConnector implements CoffeeMachineExternalControlJava4CI {

	@Override
	public Measure<Double> getTemperature() throws Exception {
		return ((CoffeeMachineExternalControlJava4CI) this.offering).getTemperature();
	}

	@Override
	public Measure<Double> getPowerLevel() throws Exception {
		return ((CoffeeMachineExternalControlJava4CI) this.offering).getPowerLevel();
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return ((CoffeeMachineExternalControlJava4CI) this.offering).getMaxPowerLevel();
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setCurrentPowerLevel(powerLevel);
	}

	@Override
	public void setMode(int mode) throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setMode(mode);
	}

	@Override
	public double getTemperatureJava4() throws Exception {
		return ((CoffeeMachineExternalControlJava4CI) this.offering).getTemperatureJava4();
	}

	@Override
	public double getPowerLevelJava4() throws Exception {
		return ((CoffeeMachineExternalControlJava4CI) this.offering).getPowerLevelJava4();
	}

	@Override
	public double getMaxPowerLevelJava4() throws Exception {
		return ((CoffeeMachineExternalControlJava4CI) this.offering).getMaxPowerLevelJava4();
	}

	@Override
	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setCurrentPowerLevelJava4(powerLevel);
	}

	@Override
	public void setModeJava4(int mode) throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setModeJava4(mode);
	}

	@Override
	public void setSuspendMode() throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setSuspendMode();
	}

	@Override
	public void setEcoMode() throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setEcoMode();
	}

	@Override
	public void setNormalMode() throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setNormalMode();
	}

	@Override
	public void setMaxMode() throws Exception {
		((CoffeeMachineExternalControlJava4CI) this.offering).setMaxMode();
	}

	@Override
	public CoffeeMachineMode getMode() throws Exception {
		return ((CoffeeMachineExternalControlJava4CI) this.offering).getMode();
	}
}
