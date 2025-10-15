package etape1.equipements.kettle.interfaces;

import physical_data.MeasurementUnit;

public interface KettleImplementationI {
	// -------------------------------------------------------------------------
		// Inner interfaces and types
		// -------------------------------------------------------------------------

		/**
		 * The enumeration <code>FanState</code> describes the operation
		 * states of the Kettle.
		 *
		 * <p><strong>Description</strong></p>
		 * 
		 * <p>Created on : 2021-09-09</p>
		 * 
		 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
		 */
		public static enum	KettleState
		{
			/** Kettle is on.												*/
			ON,
			/** Kettle is off.												*/
			OFF
		}

		/**
		 * The enumeration <code>FanMode</code> describes the operation
		 * modes of the Kettle.
		 *
		 * <p><strong>Description</strong></p>
		 * 
		 * <p>
		 * The Kettle can be either in <code>LOW</code> mode (warm and slow) or
		 * in <code>HIGH</code> mode (hot and fast).
		 * </p>
		 * 
		 * <p>Created on : 2021-09-09</p>
		 * 
		 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
		 */
		public static enum	KettleMode
		{
			/** low mode is just warm and the fan is slower.					*/
			TOTAL,			
			PARTIAL
		}

		// -------------------------------------------------------------------------
		// Component services signatures
		// -------------------------------------------------------------------------

		/**
		 * return the current state of the Kettle.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				the current state of the Kettle.
		 * @throws Exception 	<i>to do</i>.
		 */
		
		public static final MeasurementUnit	TEMPERATURE_UNIT =
				MeasurementUnit.CELSIUS;
		
		public KettleState	getState() throws Exception;

		/**
		 * return the current operation mode of the Kettle.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				the current state of the Kettle.
		 * @throws Exception 	<i>to do</i>.
		 */
		public KettleMode	getKettleMode() throws Exception;
		public void setTemperature() throws Exception;
		
		


		
		
		
		
}
