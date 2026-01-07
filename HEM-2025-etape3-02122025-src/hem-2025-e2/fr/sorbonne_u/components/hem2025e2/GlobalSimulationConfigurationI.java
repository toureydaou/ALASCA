package fr.sorbonne_u.components.hem2025e2;

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
import java.time.Instant;
import java.time.ZoneId;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterSimulationConfigurationI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SolarPanelEventI.Position;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The class <code>GlobalSimulationConfigurationI</code> defines
 * configuration parameters for the household energy management simulator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The simulation models assumes that the power unit used by the electric meter
 * is {@code MeasurementUnit.AMPERES} and that the tension of appliances is
 * 220.0 volts for all of them.
 * </p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MeasurementUnit.AMPERES.equals(ElectricMeterImplementationI.POWER_UNIT)}
 * invariant	{@code MeasurementUnit.VOLTS.equals(ElectricMeterImplementationI.TENSION_UNIT)}
 * invariant	{@code (new Measure<Double>(220.0, MeasurementUnit.VOLTS)).equals(ElectricMeter.TENSION)}
 * invariant	{@code TIME_UNIT != null}
 * </pre>
 * 
 * <p>Created on : 2025-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		GlobalSimulationConfigurationI
{
	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** time unit used in the heater simulator.								*/
	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;

	/** a start {@code Instant} for the simulation, corresponding to the
	 *  start time for the simulation clock.								*/
	public static Instant		START_INSTANT =
									Instant.parse("2025-10-20T08:00:00.00Z");
	/** the end instant used in the test scenarios.							*/
	public static Instant		END_INSTANT =
									Instant.parse("2025-10-21T08:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}.												*/
	public static Time			START_TIME = new Time(0.0, TimeUnit.HOURS);

	/** acceleration factor for the real time simulation; with a factor 2.0,
	 *  the simulation runs two times faster than real time i.e., a run that
	 *  is supposed to take 10 seconds in real time will take 5 seconds to
	 *  execute.															*/
	public static double 		ACCELERATION_FACTOR = 1200.0;

	// Batteries configuration

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
	public static double		BATTERIES_LEVEL_INTEGRATION_QUANTUM = 300.0;

	// Generator configuration

	/** capacity of the TANK in {@code MeasurementUnit.LITERS}.	*/
	public static double		TANK_CAPACITY = 40.0;
	/** initial FUEL level of the TANK in {@code MeasurementUnit.LITERS}.	*/
	public static double		INITIAL_TANK_LEVEL = 15.0;
	/** in the QSS integration algorithm, the standard level quantum between
	 *  successive points computations.										*/
	public static double		FUEL_LEVEL_INTEGRATION_QUANTUM = 0.1;

	// Solar panel configuration

	/** latitude of the solar panel.										*/
	public static Position		LATITUDE = new Position(48, 51, 24.0);
	/** longitude of the solar panel.										*/
	public static Position		LONGITUDE = new Position(2, 21, 6.0);
	/** time zone of the solar panel.										*/
	public static ZoneId		ZONE = ZoneId.of("Europe/Paris");
	/** Sun azimuth from the solar panel, in degrees, north-based; this is
	 *  the direction along the horizon, measured from north to east (or
	 *  example, {@code 0.0} means north, {@code 135.0} means southeast,
	 *  {@code 270.0} means west.
	*/
	public static double		ORIENTATION = 190.0;
	/** slope of the solar panel.											*/
	public static double		SLOPE = 45.0;
	/**	available number of square meters of solar panel.					*/
	public static int			NB_SQUARE_METERS = 5;
	/** when using the deterministic sunrise and sunset model, the hour of
	 *  sunrise each day.													*/
	public static int			DETERMINISTIC_SUNRISE_HOUR = 6;
	/** when using the deterministic sunrise and sunset model, the hour of
	 *  sunset each day.													*/
	public static int			DETERMINISTIC_SUNSET_HOUR = 18;

	/** delay between successive updates of the sun intensity.				*/
	public static double		SUN_INTENSITY_MODEL_STEP = 0.1;
	/** delay between successive updates of the sun intensity.				*/
	public static double		SOLAR_PANEL_POWER_MODEL_STEP = 0.1;

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
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.AMPERES.equals(
									ElectricMeterImplementationI.POWER_UNIT),
				HeaterSimulationConfigurationI.class,
				"MeasurementUnit.AMPERES.equals("
				+ "ElectricMeterImplementationI.POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.VOLTS.equals(
									ElectricMeterImplementationI.TENSION_UNIT),
				HeaterSimulationConfigurationI.class,
				"MeasurementUnit.VOLTS.equals("
				+ "ElectricMeterImplementationI.TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				(new Measure<Double>(220.0, MeasurementUnit.VOLTS)).
												equals(ElectricMeter.TENSION),
				HeaterSimulationConfigurationI.class,
				"(new Measure<Double>(220.0, MeasurementUnit.VOLTS))."
				+ "equals(ElectricMeter.TENSION)");
		ret &= AssertionChecking.checkStaticInvariant(
				TIME_UNIT != null,
				HeaterSimulationConfigurationI.class,
				"TIME_UNIT != null");
		return ret;
	}
}
// -----------------------------------------------------------------------------
