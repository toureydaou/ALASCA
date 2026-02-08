package etape1.equipements.kettle.interfaces;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>KettleExternalControlJava4CI</code> extends
 * the external control interface with Java 1.4 compatible methods.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface provides methods using primitive types (int, double) instead
 * of enumerations and Measure objects for compatibility with Javassist
 * connector generation.
 * </p>
 *
 * <p>Created on : 2023-09-18</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface KettleExternalControlJava4CI
extends KettleExternalControlCI {

	/**
	 * get the current state as an integer code.
	 * 0=OFF, 1=ON, 2=HEATING
	 */
	public int getStateJava4() throws Exception;

	/**
	 * get the current mode as an integer code.
	 * 0=SUSPEND, 1=ECO, 2=NORMAL, 3=MAX
	 */
	public int getKettleModeJava4() throws Exception;

	/**
	 * get the target temperature as a double.
	 */
	public double getTargetTemperatureJava4() throws Exception;

	/**
	 * get the current water temperature as a double.
	 */
	public double getCurrentTemperatureJava4() throws Exception;

	/**
	 * get the maximum power level as a double.
	 */
	public double getMaxPowerLevelJava4() throws Exception;

	/**
	 * get the current power level as a double.
	 */
	public double getCurrentPowerLevelJava4() throws Exception;

	/**
	 * set the current power level as a double.
	 */
	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception;

	/**
	 * set the mode using an integer code.
	 * 1=ECO, 2=NORMAL, 3=MAX
	 */
	public void setModeJava4(int mode) throws Exception;
}
