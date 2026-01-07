package fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil;

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

import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The interface <code>HairDryerSimulationConfigurationI</code> defines
 * configuration parameters for the hair dryer simulator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The hair dryer simulator assumes that the hair dryer uses
 * {@code MeasurementUnit.WATTS} as its power unit and that its tension is
 * 220.0 volts. It also assumes that the power unit used by the electric meter
 * is {@code }.
 * </p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MeasurementUnit.AMPERES.equals(ElectricMeterImplementationI.POWER_UNIT)}
 * invariant	{@code MeasurementUnit.VOLTS.equals(ElectricMeterImplementationI.TENSION_UNIT)}
 * invariant	{@code (new Measure<Double>(220.0, ElectricMeterImplementationI.TENSION_UNIT)).equals(ElectricMeter.TENSION)}
 * invariant	{@code MeasurementUnit.WATTS.equals(HairDryerImplementationI.POWER_UNIT)}
 * invariant	{@code ElectricMeterImplementationI.TENSION_UNIT.equals(HairDryerImplementationI.TENSION_UNIT)}
 * invariant	{@code ElectricMeter.TENSION.equals(HairDryer.TENSION)}
 * invariant	{@code TIME_UNIT != null}
 * </pre>
 * 
 * <p>Created on : 2025-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		HairDryerSimulationConfigurationI
{
	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** time unit used in the heater simulator.								*/
	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= ElectricMeterImplementationI.staticInvariants();
		ret &= HairDryerImplementationI.staticInvariants();
		ret &= HairDryer.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.AMPERES.equals(
									ElectricMeterImplementationI.POWER_UNIT),
				HairDryerSimulationConfigurationI.class,
				"MeasurementUnit.AMPERES.equals("
				+ "ElectricMeterImplementationI.POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.VOLTS.equals(
									ElectricMeterImplementationI.TENSION_UNIT),
				HairDryerSimulationConfigurationI.class,
				"MeasurementUnit.VOLTS.equals("
				+ "ElectricMeterImplementationI.TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				(new Measure<Double>(220.0,
									 ElectricMeterImplementationI.TENSION_UNIT)).
						equals(ElectricMeter.TENSION),
				HairDryerSimulationConfigurationI.class,
				"(new Measure<Double>(220.0, ElectricMeterImplementationI."
				+ "TENSION_UNIT)).equals(ElectricMeter.TENSION)");
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.WATTS.equals(HairDryerImplementationI.POWER_UNIT),
				HairDryerSimulationConfigurationI.class,
				"MeasurementUnit.WATTS.equals(HairDryerImplementationI.POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				ElectricMeterImplementationI.TENSION_UNIT.equals(
										HairDryerImplementationI.TENSION_UNIT),
				HairDryerSimulationConfigurationI.class,
				"ElectricMeterImplementationI.TENSION_UNIT.equals("
				+ "HairDryerImplementationI.TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				ElectricMeter.TENSION.equals(HairDryer.TENSION),
				HairDryerSimulationConfigurationI.class,
				"ElectricMeter.TENSION.equals(HairDryer.TENSION)");
		ret &= AssertionChecking.checkStaticInvariant(
				TIME_UNIT != null,
				HairDryerSimulationConfigurationI.class,
				"TIME_UNIT != null");
		return ret;
	}
}
// -----------------------------------------------------------------------------
