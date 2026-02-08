package etape1.equipements.fan.interfaces;

import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;

public interface FanImplementationI {
	// -------------------------------------------------------------------------
		// Inner interfaces and types
		// -------------------------------------------------------------------------

		/**
		 * The enumeration <code>FanState</code> describes the operation
		 * states of the fan.
		 *
		 * <p><strong>Description</strong></p>
		 * 
		 * <p>Created on : 2021-09-09</p>
		 * 
		 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
		 */
		public static enum	FanState
		{
			/** fan is on.												*/
			ON,
			/** fan is off.												*/
			OFF
		}

		/**
		 * The enumeration <code>FanMode</code> describes the operation
		 * modes of the fan.
		 *
		 * <p><strong>Description</strong></p>
		 * 
		 * <p>
		 * The fan can be either in <code>LOW</code> mode (warm and slow) or
		 * in <code>HIGH</code> mode (hot and fast).
		 * </p>
		 * 
		 * <p>Created on : 2021-09-09</p>
		 * 
		 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
		 */
		public static enum	FanMode
		{
			/** low mode is just warm and the fan is slower.					*/
			LOW,			
			
			MEDIUM,
			/** high mode is hot and the fan turns faster.						*/
			HIGH
		}

		/** measurement unit for power used in this appliance.					*/
		public static final MeasurementUnit	POWER_UNIT = MeasurementUnit.WATTS;
		/** measurement unit for tension used in this appliance.				*/
		public static final MeasurementUnit	TENSION_UNIT = MeasurementUnit.VOLTS;
		
		
		// -------------------------------------------------------------------------
		// Component services signatures
		// -------------------------------------------------------------------------

		/**
		 * return the current state of the fan.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				the current state of the fan.
		 * @throws Exception 	<i>to do</i>.
		 */
		public FanState	getState() throws Exception;

		/**
		 * return the current operation mode of the fan.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				the current state of the fan.
		 * @throws Exception 	<i>to do</i>.
		 */
		public FanMode	getMode() throws Exception;

		/**
		 * turn on the fan, put in the low temperature and slow fan mode.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
		 * post	{@code getState() == FanState.ON}
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public void			turnOn() throws Exception;

		/**
		 * turn off the fan.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code getState() == FanState.OFF}
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public void			turnOff() throws Exception;

		/**
		 * set the fan in high mode.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code getState() == FanState.ON}
		 * pre	{@code getMode() == FanMode.LOW}
		 * post	{@code getMode() == FanMode.HIGH}
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public void			setHigh() throws Exception;

		
		/**
		 * set the fan in low mode.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code getState() == FanState.ON}
		 * pre	{@code getMode() == FanMode.HIGH}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public void			setMedium() throws Exception;
		
		/**
		 * set the fan in low mode.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code getState() == FanState.ON}
		 * pre	{@code getMode() == FanMode.HIGH}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 *
		 * @throws Exception	<i>to do</i>.
		 */
		public void			setLow() throws Exception;
}
