package etape1.equipements.hem.connections;

import etape1.bases.AdjustableCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.exceptions.PreconditionException;

public class CoffeeMachineConnector extends AbstractConnector implements AdjustableCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/**
	 * modes will be defined by five power levels, including a power level of 0.0
	 * watts; note that modes go from 1 (0.0 watts) to 6 (2000.0 watts).
	 */
	public static final int MAX_MODE = 3;
	/**
	 * the minimum admissible temperature from which the heater should be resumed in
	 * priority after being suspended to save energy.
	 */
	public static final double MIN_ADMISSIBLE_TEMP_THE = 0.0;

	/**
	 * the minimum admissible temperature from which the heater should be resumed in
	 * priority after being suspended to save energy.
	 */
	public static final double MIN_ADMISSIBLE_TEMP_CAFE = 0.0;

	/**
	 * the minimum admissible temperature from which the heater should be resumed in
	 * priority after being suspended to save energy.
	 */
	public static final double MAX_ADMISSIBLE_TEMP_THE = 100.0;

	/**
	 * the minimum admissible temperature from which the heater should be resumed in
	 * priority after being suspended to save energy.
	 */
	public static final double MAX_ADMISSIBLE_TEMP_CAFE = 90.0;
	/**
	 * the maximal admissible difference between the target and the current
	 * temperature from which the heater should be resumed in priority after being
	 * suspended to save energy.
	 */
	public static final double MAX_ADMISSIBLE_DELTA = 10.0;

	/** the current mode of the heater. */
	protected int currentMode;
	/** true if the heater has been suspended, false otherwise. */
	protected boolean isSuspended;
	
	public CoffeeMachineConnector() {
		this.currentMode = MAX_MODE;
		this.isSuspended = false;
	}

	@Override
	public int maxMode() throws Exception {
		return MAX_MODE;
	}

	@Override
	public boolean upMode() throws Exception {
		this.currentMode++;
		return true;
	}

	@Override
	public boolean downMode() throws Exception {
		this.currentMode--;
		return true;
	}

	@Override
	public boolean setMode(int modeIndex) throws Exception {
		this.currentMode = modeIndex;
		return true;
	}

	@Override
	public int currentMode() throws Exception {
		
		return this.currentMode;
	}

	@Override
	public double getModeConsumption(int modeIndex) throws Exception {

		return 0.0;
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
			// this.computeAndSetNewPowerLevel(this.currentMode);
			this.isSuspended = false;
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	@Override
	public double emergency() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
