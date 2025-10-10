package etape1.equipements.laundry.interfaces;


public interface LaundryImplementationI {
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
		public static enum	LaundryState
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
		public static enum	LaundryMode
		{
			/** low mode is just warm and the fan is slower.					*/
			WASH,			
			DRY
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
		public LaundryState	getState() throws Exception;

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
		public LaundryMode	getLaundryMode() throws Exception;
		
		


		
		
		
		
}
