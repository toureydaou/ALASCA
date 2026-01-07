package fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil;

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
import fr.sorbonne_u.components.hem2025e1.equipments.batteries.Batteries;
import fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesImplementationI;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The interface <code>BatteriesSimulationConfigurationI</code> defines
 * configuration parameters for the batteries simulator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MeasurementUnit.AMPERES.equals(ElectricMeterImplementationI.POWER_UNIT)}
 * invariant	{@code MeasurementUnit.VOLTS.equals(ElectricMeterImplementationI.TENSION_UNIT)}
 * invariant	{@code (new Measure<Double>(220.0, ElectricMeterImplementationI.TENSION_UNIT)).equals(ElectricMeter.TENSION)}
 * invariant	{@code MeasurementUnit.WATT_HOURS.equals(BatteriesImplementationI.CAPACITY_UNIT)}
 * invariant	{@code ElectricMeterImplementationI.TENSION_UNIT.equals(BatteriesImplementationI.TENSION_UNIT)}
 * invariant	{@code TIME_UNIT != null}
 * </pre>
 * 
 * <p>Created on : 2025-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	BatteriesSimulationConfiguration
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	public enum			BatteriesTestScenario
	{
		/** batteries go up and down, away from their limits.				*/
		CLASSICAL,
		/** batteries are charged until reaching their full capacity.		*/
		BATTERIES_FULL,
		/** batteries are discharged until they are emptied.				*/
		BATTERIES_EMPTY
	}

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** time unit used in the heater simulator.								*/
	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;
	/** number of batteries cells put in parallel to get a better
	 *  maximum output power.												*/
	public static int			NUMBER_OF_PARALLEL_CELLS = 2;
	/** number of groups of parallel batteries cells put in series to get
	 *  a better total capacity.											*/
	public static int			NUMBER_OF_CELL_GROUPS_IN_SERIES = 2;
	/** initial charge level of the batteries in
	 *  {@code MeasurementUnit.WATTS}.										*/
	public static double		INITIAL_BATTERIES_LEVEL = 11000.0;
	/** in the QSS integration algorithm, the standard level quantum between
	 *  successive points computations.										*/
	public static double		STANDARD_LEVEL_INTEGRATION_QUANTUM = 300.0;

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
		ret &= BatteriesImplementationI.staticInvariants();
		ret &= Batteries.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.AMPERES.equals(
									ElectricMeterImplementationI.POWER_UNIT),
				BatteriesSimulationConfiguration.class,
				"MeasurementUnit.AMPERES.equals("
				+ "ElectricMeterImplementationI.POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.VOLTS.equals(
									ElectricMeterImplementationI.TENSION_UNIT),
				BatteriesSimulationConfiguration.class,
				"MeasurementUnit.VOLTS.equals("
				+ "ElectricMeterImplementationI.TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				(new Measure<Double>(220.0,
									 ElectricMeterImplementationI.TENSION_UNIT)).
						equals(ElectricMeter.TENSION),
				BatteriesSimulationConfiguration.class,
				"(new Measure<Double>(220.0, ElectricMeterImplementationI."
				+ "TENSION_UNIT)).equals(ElectricMeter.TENSION)");
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.WATT_HOURS.equals(BatteriesImplementationI.CAPACITY_UNIT),
				BatteriesSimulationConfiguration.class,
				"MeasurementUnit.WATTS.equals(BatteriesImplementationI.CAPACITY_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				ElectricMeterImplementationI.TENSION_UNIT.equals(
										BatteriesImplementationI.TENSION_UNIT),
				BatteriesSimulationConfiguration.class,
				"ElectricMeterImplementationI.TENSION_UNIT.equals("
				+ "BatteriesImplementationI.TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				TIME_UNIT != null,
				BatteriesSimulationConfiguration.class,
				"TIME_UNIT != null");
		return ret;
	}
}
// -----------------------------------------------------------------------------
