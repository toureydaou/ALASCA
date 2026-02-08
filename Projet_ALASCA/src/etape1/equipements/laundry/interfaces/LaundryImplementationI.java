package etape1.equipements.laundry.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The interface <code>LaundryImplementationI</code> defines the signatures of
 * the laundry machine services and the state data access.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The interface is used to define the common functionalities to access the
 * laundry machine state and modes, used across the user and the control
 * interfaces.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface LaundryImplementationI {
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>LaundryState</code> describes the operation
	 * states of the laundry machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Created on : 2026-01-08</p>
	 *
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	LaundryState
	{
		/** laundry machine is off.											*/
		OFF,
		/** laundry machine is on, ready to start.							*/
		ON,
		/** laundry machine is washing.										*/
		WASHING,
		/** laundry machine is rinsing.										*/
		RINSING,
		/** laundry machine is spinning (essorage).							*/
		SPINNING,
		/** laundry machine is drying (séchage).							*/
		DRYING
	}

	/**
	 * The enumeration <code>LaundryWashMode</code> describes the wash
	 * modes of the laundry machine.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * The laundry machine can operate in different wash modes:
	 * <ul>
	 * <li>WHITE: for white clothes (linge blanc)</li>
	 * <li>COLOR: for colored clothes (linge de couleur)</li>
	 * <li>DELICATE: for delicate fabrics (linge délicat)</li>
	 * <li>INTENSIVE: for heavily soiled clothes (linge très sale)</li>
	 * </ul>
	 * </p>
	 *
	 * <p>Created on : 2026-01-08</p>
	 *
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	LaundryWashMode
	{
		/** white clothes wash mode.										*/
		WHITE,
		/** colored clothes wash mode.										*/
		COLOR,
		/** delicate fabrics wash mode.										*/
		DELICATE,
		/** intensive wash mode for heavily soiled clothes.				*/
		INTENSIVE
	}

	/**
	 * The enumeration <code>WashTemperature</code> describes the available
	 * wash temperatures.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Created on : 2026-01-08</p>
	 *
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	WashTemperature
	{
		T_30,	// 30°C
		T_40,	// 40°C
		T_50,	// 50°C
		T_60,	// 60°C
		T_70,	// 70°C
		T_80,	// 80°C
		T_90	// 90°C
	}

	/**
	 * The enumeration <code>SpinSpeed</code> describes the available
	 * spin speeds (vitesse d'essorage).
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>Created on : 2026-01-08</p>
	 *
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	SpinSpeed
	{
		/** 400 RPM (tours/minute) */
		RPM_400,
		/** 600 RPM */
		RPM_600,
		/** 800 RPM */
		RPM_800,
		/** 1000 RPM */
		RPM_1000,
		/** 1200 RPM */
		RPM_1200,
		/** 1400 RPM */
		RPM_1400
	}

	// -------------------------------------------------------------------------
	// Component services signatures
	// -------------------------------------------------------------------------

	/**
	 * return true if the laundry machine is currently running (washing,
	 * rinsing, spinning, or drying).
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the laundry machine is running.
	 * @throws Exception 	<i>to do</i>.
	 */
	public boolean		isRunning() throws Exception;

	/**
	 * return the current state of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the current state of the laundry machine.
	 * @throws Exception 	<i>to do</i>.
	 */
	public LaundryState	getState() throws Exception;

	/**
	 * return the current wash mode of the laundry machine.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the current wash mode.
	 * @throws Exception 	<i>to do</i>.
	 */
	public LaundryWashMode	getWashMode() throws Exception;


	/**
	 * return the current spin speed setting.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null}
	 * </pre>
	 *
	 * @return				the current spin speed.
	 * @throws Exception 	<i>to do</i>.
	 */
	public SpinSpeed	getSpinSpeed() throws Exception;
	

	public Measure<Double> getWashTemperature() throws Exception;

	/**
	 * return true if a delayed start has been set.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if a delayed start is programmed.
	 * @throws Exception 	<i>to do</i>.
	 */
	public boolean		isDelayedStartSet() throws Exception;

	/**
	 * return the delayed start time in seconds from the time it was set.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code isDelayedStartSet()}
	 * post	{@code ret >= 0}
	 * </pre>
	 *
	 * @return				the delayed start time in seconds.
	 * @throws Exception 	<i>to do</i>.
	 */
	public long			getDelayedStartTime() throws Exception;
}
// -----------------------------------------------------------------------------
