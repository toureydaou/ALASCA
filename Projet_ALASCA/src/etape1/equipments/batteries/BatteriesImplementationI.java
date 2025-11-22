package etape1.equipments.batteries;

import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;
import physical_data.MeasurementUnit;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The interface <code>BatteriesImplementationI</code> defines the signatures
 * of operations that can be performed by batteries components.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code POWER_UNIT != null}
 * invariant	{@code TENSION_UNIT != null}
 * invariant	{@code CAPACITY_UNIT != null}
 * invariant	{@code STANDARD_NOMINAL_CAPACITY != null && STANDARD_NOMINAL_CAPACITY.getData() > 0.0 && STANDARD_NOMINAL_CAPACITY.getMeasurementUnit().equals(CAPACITY_UNIT)}
 * invariant	{@code !(areCharging() && areDischarging())}
 * </pre>
 * 
 * <p>Created on : 2025-09-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		BatteriesImplementationI
{
	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** measurement unit for power used in the batteries.					*/
	public static final MeasurementUnit	POWER_UNIT = MeasurementUnit.WATTS;
	/** measurement unit for tension used in the batteries.				*/
	public static final MeasurementUnit	TENSION_UNIT = MeasurementUnit.VOLTS;
	/** capacity measurement unit for the batteries.						*/
	public static final MeasurementUnit	CAPACITY_UNIT =
													MeasurementUnit.WATT_HOURS;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				POWER_UNIT != null,
				BatteriesImplementationI.class,
				"POWER_UNIT != null");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION_UNIT != null,
				BatteriesImplementationI.class,
				"TENSION_UNIT != null");
		ret &= AssertionChecking.checkStaticInvariant(
				CAPACITY_UNIT != null,
				BatteriesImplementationI.class,
				"CAPACITY_UNIT != null");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	public static boolean	invariants(BatteriesImplementationI instance)
	throws Exception
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		ret &= AssertionChecking.checkInvariant(
				!(instance.areCharging() && instance.areDischarging()),
				BatteriesImplementationI.class, instance,
				"!(areCharging() && areDischarging())");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Signature and default methods
	// -------------------------------------------------------------------------

	/**
	 * return the nominal capacity of the batteries in AmpH.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(CAPACITY_UNIT)}
	 * </pre>
	 *
	 * @return				the nominal capacity of the batteries in AmpH.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>		nominalCapacity() throws Exception;

	/**
	 * return the current capacity of the batteries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getMeasure().getData() >= 0.0 && return.getMeasure().getData() <= nominalCapacity().getData() && return.getMeasure().getMeasurementUnit().equals(CAPACITY_UNIT)}
	 * </pre>
	 *
	 * @return				the current capacity of the batteries.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	currentCapacity() throws Exception;

	/**
	 * return true if the batteries are currently charging and false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the batteries are currently charging and false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		areCharging() throws Exception;

	/**
	 * return true if the batteries are currently discharging and false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the batteries are currently charging and false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		areDischarging() throws Exception;

	/**
	 * return current charge level of the batteries in proportion of the current
	 * capacity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() >= 0.0 && return.getData() <= 1.0 && return.getMeasurementUnit().equals(MeasurementUnit.RAW)}	// no postcondition.
	 * </pre>
	 *
	 * @return				current charge level of the batteries in proportion of the current capacity.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	chargeLevel() throws Exception;

	/**
	 * return the current power consumption of the batteries when charging,
	 * otherwise they do not consume power.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code areCharging()}	// no precondition.
	 * post	{@code return != null && return.getMeasure().getData() > 0.0 && return.getMeasure().getMeasurementUnit().equals(POWER_UNIT)}
	 * </pre>
	 *
	 * @return				the current power consumed ny the batteries.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	getCurrentPowerConsumption() throws Exception;

	/**
	 * start charging the batteries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !areCharging() && !areDischarging() && chargeLevel().getMeasure().getData() < 1.0}
	 * post	{@code areCharging()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			startCharging() throws Exception;

	/**
	 * stop charging the batteries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code areCharging()}
	 * post	{@code !areCharging()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			stopCharging() throws Exception;
}
// -----------------------------------------------------------------------------
