package fr.sorbonne_u.components.hem2025e1.equipments.batteries;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.AssertionChecking;

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
		ret &= AssertionChecking.checkInvariant(
				POWER_UNIT != null,
				BatteriesImplementationI.class, instance,
				"POWER_UNIT != null");
		ret &= AssertionChecking.checkInvariant(
				TENSION_UNIT != null,
				BatteriesImplementationI.class, instance,
				"TENSION_UNIT != null");
		ret &= AssertionChecking.checkInvariant(
				CAPACITY_UNIT != null,
				BatteriesImplementationI.class, instance,
				"!(areCharging() && areDischarging())");
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
