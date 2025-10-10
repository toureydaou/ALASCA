package etape1.equipements.fan.interfaces;


public interface FanImplementationI {
	// -------------------------------------------------------------------------
		// Inner interfaces and types
		// -------------------------------------------------------------------------

		/**
		 * The enumeration <code>FanState</code> describes the operation
		 * states of the hair dryer.
		 *
		 * <p><strong>Description</strong></p>
		 * 
		 * <p>Created on : 2021-09-09</p>
		 * 
		 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
		 */
		public static enum	FanState
		{
			/** hair dryer is on.												*/
			ON,
			/** hair dryer is off.												*/
			OFF
		}

		/**
		 * The enumeration <code>FanMode</code> describes the operation
		 * modes of the hair dryer.
		 *
		 * <p><strong>Description</strong></p>
		 * 
		 * <p>
		 * The hair dryer can be either in <code>LOW</code> mode (warm and slow) or
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

		// -------------------------------------------------------------------------
		// Component services signatures
		// -------------------------------------------------------------------------

		/**
		 * return the current state of the hair dryer.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				the current state of the hair dryer.
		 * @throws Exception 	<i>to do</i>.
		 */
		public FanState	getState() throws Exception;

		/**
		 * return the current operation mode of the hair dryer.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				the current state of the hair dryer.
		 * @throws Exception 	<i>to do</i>.
		 */
		public FanMode	getMode() throws Exception;

		/**
		 * turn on the hair dryer, put in the low temperature and slow fan mode.
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
		 * turn off the hair dryer.
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
		 * set the hair dryer in high mode.
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
		 * set the hair dryer in low mode.
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
		 * set the hair dryer in low mode.
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
