package fr.sorbonne_u.components.hem2025e1.equipments.heater;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
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
import fr.sorbonne_u.exceptions.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The interface <code>HeaterExternalControlI</code> declares the
 * signatures of service implementations accessible to the external controller.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code POWER_UNIT != null}
 * invariant	{@code TENSION_UNIT != null}
 * invariant	{@code MAX_POWER_LEVEL != null && MAX_POWER_LEVEL.getMeasurementUnit().equals(POWER_UNIT) && MAX_POWER_LEVEL.getData() > 0.0}
 * invariant	{@code VOLTAGE != null && VOLTAGE.getMeasurementUnit().equals(TENSION_UNIT) && VOLTAGE.getData() == 220.0}
 * invariant	{@code getCurrentPowerLevel().getData() <= getMaxPowerLevel().getData()}
 * </pre>
 * 
 * <p>Created on : 2023-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		HeaterExternalControlI
extends		HeaterTemperatureI
{
	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** measurement unit for power used in this appliance.					*/
	public static final MeasurementUnit	POWER_UNIT = MeasurementUnit.WATTS;
	/** measurement unit for tension used in this appliance.				*/
	public static final MeasurementUnit	TENSION_UNIT = MeasurementUnit.VOLTS;

	/** power level of the heater when on but not heating, in the power
	 *  measurement unit used by the heater.								*/
	public static final Measure<Double>	NOT_HEATING_POWER =
											new Measure<>(2.2, POWER_UNIT);
	/** max power level of the heater, in the power measurement unit used
	 *  by the heater.														*/
	public static final Measure<Double>	MAX_POWER_LEVEL =
											new Measure<>(2200.0, POWER_UNIT);
	/** operating voltage of the heater, in the tension measurement unit
	 *  used by the heater.													*/
	public static final Measure<Double>	TENSION =
											new Measure<>(220.0, TENSION_UNIT);

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
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				POWER_UNIT != null,
				HeaterExternalControlI.class,
				"POWER_UNIT != null");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION_UNIT != null,
				HeaterExternalControlI.class,
				"TENSION_UNIT != null");
		ret &= AssertionChecking.checkStaticInvariant(
				MAX_POWER_LEVEL != null &&
					MAX_POWER_LEVEL.getMeasurementUnit().equals(POWER_UNIT) &&
					MAX_POWER_LEVEL.getData() > 0.0,
				HeaterExternalControlI.class,
				"MAX_POWER_LEVEL != null && MAX_POWER_LEVEL.getMeasurementUnit()."
				+ "equals(POWER_UNIT) && MAX_POWER_LEVEL.getData() > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION != null &&
					TENSION.getMeasurementUnit().equals(TENSION_UNIT) &&
					TENSION.getData() == 220.0,
				HeaterExternalControlI.class,
				"VOLTAGE != null && VOLTAGE.getMeasurementUnit().equals("
				+ "TENSION_UNIT) && VOLTAGE.getData() == 220.0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Signatures
	// -------------------------------------------------------------------------

	/**
	 * return the maximum power of the heater in the power unit used by the
	 * heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(POWER_UNIT)}
	 * </pre>
	 *
	 * @return				the maximum power of the heater in the power unit used by the heater.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	getMaxPowerLevel() throws Exception;

	/**
	 * set the power level of the heater; if
	 * {@code powerLevel.getData() > getMaxPowerLevel().getData()} then set the
	 * power level to the maximum.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * pre	{@code powerLevel != null && powerLevel.getData() >= 0.0 && powerLevel.getMeasurementUnit().equals(POWER_UNIT)}
	 * post	{@code powerLevel.getData() > getMaxPowerLevel().getData() || getCurrentPowerLevel().getData() == powerLevel.getData()}
	 * post	{@code powerLevel.getData() <= getMaxPowerLevel().getData() || getCurrentPowerLevel().getData() == Heater.MAX_POWER_LEVEL.getData()}
	 * </pre>
	 *
	 * @param powerLevel	the powerLevel to be set.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setCurrentPowerLevel(Measure<Double> powerLevel)
	throws Exception;

	/**
	 * return the current power level of the heater in the power unit used by
	 * the heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * post	{@code return != null && return.getMeasure().getMeasurementUnit().equals(POWER_UNIT)}
	 * post	{@code return.getMeasure().getData() >= 0.0 && return.getMeasure().getData() <= getMaxPowerLevel().getData()}
	 * </pre>
	 *
	 * @return				the current power level of the heater.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	getCurrentPowerLevel() throws Exception;
}
// -----------------------------------------------------------------------------
