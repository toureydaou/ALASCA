package etape1.equipments.solar_panel;

import fr.sorbonne_u.exceptions.AssertionChecking;
import physical_data.Measure;
import physical_data.MeasurementUnit;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelImplementationI</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code POWER_UNIT != null}
 * </pre>
 * 
 * <p>Created on : 2025-09-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		SolarPanelImplementationI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** measurement unit for power used in this appliance.					*/
	public static final MeasurementUnit	POWER_UNIT = MeasurementUnit.WATTS;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return			true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				POWER_UNIT != null,
				SolarPanelImplementationI.class,
				"POWER_UNIT != null");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Signature and default methods
	// -------------------------------------------------------------------------

	/**
	 * return the number of square meters in the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0}
	 * </pre>
	 *
	 * @return				the number of square meters in the solar panel.
	 * @throws Exception	<i>to do</i>.
	 */
	public int				getNumberOfSquareMeters() throws Exception;

	/**
	 * return the nominal power production capacity of the solar panels in the
	 * power unit used by the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(POWER_UNIT)}
	 * </pre>
	 *
	 * @return				the nominal power production capacity of the solar panels in the power unit used by the solar panel.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	getNominalPowerProductionCapacity()
	throws Exception;

	/**
	 * return the current power production capacity of the solar panels in the
	 * power unit used by the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getMeasure().getMeasurementUnit().equals(POWER_UNIT)}
	 * post	{@code return.getMeasure().getData() >= 0.0 && return.getMeasure().getData() <= getNominalPowerProductionCapacity().getData()}	// no postcondition.
	 * </pre>
	 *
	 * @return				the current power production capacity of the solar panels in the power unit used by the solar panel.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	getCurrentPowerProductionLevel()
	throws Exception;
}
// -----------------------------------------------------------------------------
