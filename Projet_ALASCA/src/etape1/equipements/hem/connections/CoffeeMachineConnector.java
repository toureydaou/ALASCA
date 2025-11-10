package etape1.equipements.hem.connections;

import etape1.bases.AdjustableCI;
import etape1.equipements.coffee_machine.Constants;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.exceptions.PreconditionException;

public class CoffeeMachineConnector extends AbstractConnector implements AdjustableCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/**
	 * modes will be defined by four power levels, including a power level of 0.0
	 * watts; note that modes go from 1 (3.0 watts) to 4 (1500.0 watts).
	 */
	public static final int MIN_MODE = 1;
	public static final int MAX_MODE = 4;
	
	
	
	public static final double MAX_ADMISSIBLE_DELTA = 10.0;
	
	public static final double TARGET_TEMPERATURE = 100.0;

	/** the current mode of the heater. */
	protected int currentMode;
	/** true if the heater has been suspended, false otherwise. */
	protected boolean isSuspended;

	public CoffeeMachineConnector() {
		this.currentMode = MAX_MODE;
		this.isSuspended = false;
	}


	public double computePowerLevel(int mode) throws Exception {

		assert mode >= MIN_MODE && mode <= this.maxMode()  : new PreconditionException("mode >= MIN_MODE && mode <= MAX_MODE");
		
		switch (mode) {
			case 1:
				return Constants.SUSPENDED_MODE_POWER;
			case 2:
				return Constants.ECO_MODE_POWER;
			case 3:
				return Constants.NORMAL_MODE_POWER;
			case 4:
				return Constants.MAX_MODE_POWER;
	
			default:
				return Constants.SUSPENDED_MODE_POWER;
		}
		
		
	}

	public void setPowerLevel(double newPowerLevel) throws Exception {
		assert newPowerLevel >= 0.0 : new PreconditionException("newPowerLevel >= 0.0");

		double maxPowerLevel = ((CoffeeMachineExternalControlJava4CI) this.offering).getMaxPowerLevelJava4();

		if (newPowerLevel > maxPowerLevel) {
			newPowerLevel = maxPowerLevel;
		}
		((CoffeeMachineExternalControlJava4CI) this.offering).setCurrentPowerLevelJava4(newPowerLevel);
	}
	
	protected void		computeAndSetNewPowerLevel(int newMode) throws Exception
	{
		double newPowerLevel = this.computePowerLevel(newMode);
		this.setPowerLevel(newPowerLevel);
	}

	@Override
	public int maxMode() throws Exception {
		return MAX_MODE;
	}

	@Override
	public boolean upMode() throws Exception {
		
		assert this.currentMode() < this.maxMode()
				: new PreconditionException("this.currentMode() < this.maxMode()");

		try {
			this.currentMode++;
			this.computeAndSetNewPowerLevel(this.currentMode);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean downMode() throws Exception {
		
		assert this.currentMode() > 1
		: new PreconditionException("this.currentMode() > 1");
		
		try {
			this.currentMode--;
			this.computeAndSetNewPowerLevel(this.currentMode);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean setMode(int modeIndex) throws Exception {
		
		
		try {
			this.currentMode = modeIndex;
			this.computeAndSetNewPowerLevel(this.currentMode);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public int currentMode() throws Exception {
		return this.currentMode;
	}

	@Override
	public double getModeConsumption(int modeIndex) throws Exception {

		assert modeIndex > 0 && modeIndex <= this.maxMode()
				: new PreconditionException("modeIndex > 0 && modeIndex <= maxMode()");

		return computePowerLevel(modeIndex);
	}

	@Override
	public boolean suspended() throws Exception {
		return this.isSuspended;
	}

	@Override
	public boolean suspend() throws Exception {
		assert !this.suspended() : new PreconditionException("!suspended()");

		try {
			((CoffeeMachineExternalControlJava4CI) this.offering).setCurrentPowerLevelJava4(0.0);
			this.isSuspended = true;
		} catch (Exception e) {
			return false;
		}

		return isSuspended;
	}

	@Override
	public boolean resume() throws Exception {
		assert this.suspended() : new PreconditionException("suspended()");
		try {
			this.computeAndSetNewPowerLevel(this.currentMode);
			this.isSuspended = false;
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	@Override
	public double emergency() throws Exception {
		
		double temperature = ((CoffeeMachineExternalControlJava4CI) this.offering).getTemperatureJava4();
		
		if (temperature - TARGET_TEMPERATURE >= MAX_ADMISSIBLE_DELTA ) {
			return 1.0;
		}
		return 0.0;
	}

}
