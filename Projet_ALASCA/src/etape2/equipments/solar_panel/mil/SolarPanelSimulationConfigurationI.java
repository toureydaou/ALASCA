package etape2.equipments.solar_panel.mil;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

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

import etape2.equipments.solar_panel.mil.events.SolarPanelEventI.Position;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

// -----------------------------------------------------------------------------
/**
 * The interface <code>SolarPanelSimulationConfigurationI</code> defines
 * configuration parameters for the solar panel simulator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MeasurementUnit.AMPERES.equals(ElectricMeterImplementationI.POWER_UNIT)}
 * invariant	{@code TIME_UNIT != null}
 * invariant	{@code LATITUDE != null}
 * invariant	{@code LONGITUDE != null}
 * invariant	{@code ZONE != null}
 * invariant	{@code (LATITUDE, LONGITUDE) in ZONE}
 * invariant	{@code ORIENTATION >= 0.0 && ORIENTATION <= 360.0}
 * invariant	{@code SLOPE >= 0.0 && SLOPE <= 90.0}
 * invariant	{@code NB_SQUARE_METERS > 0}
 * </pre>
 * 
 * <p>Created on : 2025-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		SolarPanelSimulationConfigurationI
{
	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/** time unit used in the solar panel simulator.						*/
	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;

	/** latitude of the solar panel.										*/
	public static Position			LATITUDE = new Position(48, 51, 24.0);
	/** longitude of the solar panel.										*/
	public static Position			LONGITUDE = new Position(2, 21, 6.0);
	/** time zone of the solar panel.										*/
	public static ZoneId			ZONE = ZoneId.of("Europe/Paris");
	/** Sun azimuth, in degrees, north-based; this is the direction along
	 *  the horizon, measured from north to east (or example, {@code 0.0}
	 *  means north, {@code 135.0} means southeast, {@code 270.0} means
	 *  west.
	*/
	public static double			ORIENTATION = 190.0;
	/** slope of the solar panel.											*/
	public static double			SLOPE = 45.0;
	/**	available number of square meters of solar panel.					*/
	public static int				NB_SQUARE_METERS = 25;

	
	/** when true, use the astronomical version of the sunrise and sunset
	 *  model, otherwise use the deterministic version.						*/
	public static boolean			USE_ASTRONOMICAL_MODEL = true;
	/** when using the deterministic sunrise and sunset model, the hour of
	 *  sunrise each day.													*/
	public static int				DETERMINISTIC_SUNRISE_HOUR = 6;
	/** when using the deterministic sunrise and sunset model, the hour of
	 *  sunset each day.													*/
	public static int				DETERMINISTIC_SUNSET_HOUR = 18;

	/** when true, use the stochastic version of the sun intensity model,
	 *  otherwise use the deterministic version.							*/
	public static boolean			USE_STOCHASTIC_SUN_INTENSITY_MODEL = true;

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
		/*
		ret &= AssertionChecking.checkStaticInvariant(
				TIME_UNIT != null,
				HeaterSimulationConfigurationI.class,
				"TIME_UNIT != null");
				*/
		return ret;
	}

	// -------------------------------------------------------------------------
	// Signatures and default methods
	// -------------------------------------------------------------------------

	/**
	 * extract the date from the standard format of an {@code Instant}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code i != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param i	an instant from which the date must be extracted.
	 * @return	the date of {@code i}.
	 */
	static String			extractDateString(Instant i)
	{
		assert	i != null : new NeoSim4JavaException("i != null");
		String is = i.toString();
		return is.substring(0, is.indexOf('T'));
	}

	/**
	 * return true if {@code i1} has the same date as {@code i2}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param i1	an instant.
	 * @param i2	an instant.
	 * @return		true if {@code i1} has the same date as {@code i2}.
	 */
	static boolean			sameDate(Instant i1, Instant i2)
	{
		LocalDate date1 = LocalDate.parse(extractDateString(i1));
		LocalDate date2 = LocalDate.parse(extractDateString(i2));
		return date1.equals(date2);
	}

	/**
	 * return the {@code Instant} of the sunrise at
	 * {@code DETERMINISTIC_SUNRISE_HOUR} the same day as {@code current}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code current != null}
	 * post	{@code current.get(ChronoField.YEAR) == return.get(ChronoField.YEAR)}
	 * post	{@code ChronoField.MONTH_OF_YEAR) == return.get(ChronoField.MONTH_OF_YEAR)}
	 * post	{@code current.get(ChronoField.DAY_OF_MONTH) == return.get(ChronoField.DAY_OF_MONTH)}
	 * </pre>
	 *
	 * @param current	some instant in a simulation.
	 * @return			the {@code Instant} of the sunrise at {@code DETERMINISTIC_SUNRISE_HOUR} the same day as {@code current}.
	 */
	static Instant			getSunrise(Instant current)
	{
		StringBuffer sunriseString =
				new StringBuffer(extractDateString(current));
		int threshold = 10;
		if (DETERMINISTIC_SUNRISE_HOUR < threshold) {
			sunriseString.append("T0");
			sunriseString.append(DETERMINISTIC_SUNRISE_HOUR);
		} else {
			sunriseString.append("T");
			sunriseString.append(DETERMINISTIC_SUNRISE_HOUR);
		}
		sunriseString.append(":00:00.00Z");
		Instant ret = Instant.parse(sunriseString.toString());

		assert	sameDate(current, ret) :
				new AssertionError("sameDate(current, return)");

		return ret;
	}

	/**
	 * return the {@code Instant} of the sunset at
	 * {@code DETERMINISTIC_SUNSET_HOUR} the same day as {@code current}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	some instant in a simulation.
	 * @return			the {@code Instant} of the sunset at {@code DETERMINISTIC_SUNSET_HOUR} the same day as {@code current}.
	 */
	static Instant			getSunset(Instant current)
	{
		StringBuffer sunsetString =
				new StringBuffer(extractDateString(current));
		int threshold = 10;
		if (DETERMINISTIC_SUNSET_HOUR < threshold) {
			sunsetString.append("T0");
			sunsetString.append(DETERMINISTIC_SUNSET_HOUR);
		} else {
			sunsetString.append("T");
			sunsetString.append(DETERMINISTIC_SUNSET_HOUR);
		}
		sunsetString.append(":00:00.00Z");
		Instant ret = Instant.parse(sunsetString.toString());

		assert	sameDate(current, ret) :
				new AssertionError("sameDate(current, return)");

		return ret;

	}

	/**
	 * provide a deterministic simulated value for the sun intensity using a
	 * the sine of the daylight from sunrise to sunset mapped to the interval
	 * {@code [0; Math.PI]}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code current != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	some instant during the simulation.
	 * @return			the sun intensity coefficient for {@code current}.
	 */
	static double			deterministicSunIntensityCoef(Instant current)
	{
		assert	current != null : new NeoSim4JavaException("current != null");

		Instant sunrise = getSunrise(current);
		Instant sunset = getSunset(current);

		if (current.isAfter(sunrise) && current.isBefore(sunset)) {
			double d = Duration.between(sunrise, current).getSeconds();
			double day = Duration.between(sunrise, sunset).getSeconds();
			double ret = Math.sin((d/day) * Math.PI);
			return ret;
		} else {
			return 0.0;
		}
	}
}
// -----------------------------------------------------------------------------
