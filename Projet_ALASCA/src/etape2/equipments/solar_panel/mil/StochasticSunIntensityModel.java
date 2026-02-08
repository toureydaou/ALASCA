package etape2.equipments.solar_panel.mil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

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

import org.apache.commons.math3.random.RandomDataGenerator;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import etape1.equipments.solar_panel.SolarPanel;
import etape2.equipments.solar_panel.mil.events.SolarPanelEventI;
import etape2.equipments.solar_panel.mil.events.SolarPanelEventI.Position;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>StochasticSunIntensityModel</code> implements the simulation
 * model that computes the intensity of the sun, including the cloud transparency
 * using stochastic parameters.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation model provides a solar intensity in the form of a performance
 * coefficient aggregating the azimuth of the sun compared to the orientation of
 * the solar panel and the altitude of the sun compared to the slope of the
 * solar panel. Azimuth angles are expressed north-based; this is the direction
 * along the horizon, measured from north to east (or example, {@code 0.0}
 * means north, {@code 135.0} means southeast, {@code 270.0} means west.
 * </p>
 * <p>
 * This model also introduces the effect of clouds on the sun intensity by
 * applying a random coefficient in [0, 1]. This is not a very good stochastic
 * model for sun intensity as there is no correlation between successive random
 * coefficients. In the real world, large clouds induces long period of low
 * intensity.
 * </p>
 * 
 * <ul>
 * <li>Imported events: {@code SunriseEvent}, {@code SunsetEvent}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables:
 *   name = {@code sunIntensityCoef}, type = {@code Double}</li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-10-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(imported = {SunriseEvent.class, SunsetEvent.class})
@ModelExportedVariable(name = "sunIntensityCoef", type = Double.class)
//-----------------------------------------------------------------------------
public class			StochasticSunIntensityModel
extends		AtomicHIOA
implements	SunIntensityModelI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** single model URI.													*/
	public static final String	URI = "stochastic-sun-intensity-model";

	/** computation step in hours.											*/
	public double			computationStep;

	/**	latitude of the solar panel in degrees, minutes and seconds.		*/
	protected Position		latitude;
	/**	longitude of the solar panel in degrees, minutes and seconds.		*/
	protected Position		longitude;
	/** start time of the simulation in {@code Instant} format.				*/
	protected Instant		startInstant;
	/** time zone of the solar panel.										*/
	protected ZoneId		zoneId;
	/** location in latitude and longitude represented as real in degrees.	*/
	protected double[]		location;
	/** slope of the solar panel in degrees from the ground horizontal.		*/
	protected double		slope;
	/** orientation in degrees, north-based.								*/
	protected double		orientation;

	/** random number generator to generate cloud opacity.					*/
	protected RandomDataGenerator	rgCloudTransparency;

	/** current time, in {@code ZonedTimeDate} format.						*/
	protected ZonedDateTime currentZonedDateTime;
	/** current state, day or night.										*/
	protected SunState		currentState;

	/** the intensity coefficient computed by this model.					*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double> sunIntensityCoef = new Value<Double>(this) ;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic hybrid input/output model with the given URI (if null,
	 * one will be generated) and to be run by the given simulator using the
	 * given time unit for its clock.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine != null && !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine instanceof AtomicEngine}
	 * post	{@code !isDebugModeOn()}
	 * post	{@code getURI() != null && !getURI().isEmpty()}
	 * post	{@code uri == null || getURI().equals(uri)}
	 * post	{@code getSimulatedTimeUnit().equals(simulatedTimeUnit)}
	 * post	{@code getSimulationEngine().equals(simulationEngine)}
	 * </pre>
	 *
	 * @param uri				unique identifier of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation clock.
	 * @param simulationEngine	simulation engine enacting the model.
	 */
	public				StochasticSunIntensityModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunStateManagementI#setState(fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunState)
	 */
	@Override
	public void			setState(SunState s)
	{
		this.currentState = s;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunStateManagementI#setCurrent(java.time.ZonedDateTime)
	 */
	@Override
	public void			setCurrent(ZonedDateTime newCurrent)
	{
		this.currentZonedDateTime = newCurrent;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		String latitudeName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.LATITUDE_RP_NAME);
		String longitudeName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.LONGITUDE_RP_NAME);
		String startInstantName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.START_INSTANT_RP_NAME);
		String zoneIdName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.ZONE_ID_RP_NAME);
		String slopeName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.SLOPE_RP_NAME);
		String orientationName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.ORIENTATION_RP_NAME);
		String computationStepName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.COMPUTATION_STEP_RP_NAME);

		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(latitudeName) :
				new MissingRunParameterException(latitudeName);
		assert	simParams.containsKey(longitudeName) :
				new MissingRunParameterException(longitudeName);
		assert	simParams.containsKey(startInstantName) :
				new MissingRunParameterException(startInstantName);
		assert	simParams.containsKey(zoneIdName) :
				new MissingRunParameterException(zoneIdName);
		assert	simParams.containsKey(slopeName) :
				new MissingRunParameterException(slopeName);
		assert	simParams.containsKey(orientationName) :
				new MissingRunParameterException(orientationName);
		assert	simParams.containsKey(computationStepName) :
				new MissingRunParameterException(computationStepName);

		this.latitude = (Position) simParams.get(latitudeName);
		this.longitude = (Position) simParams.get(longitudeName);
		this.startInstant = (Instant) simParams.get(startInstantName);
		this.zoneId = (ZoneId) simParams.get(zoneIdName);
		this.slope = (double) simParams.get(slopeName);
		this.orientation = (double) simParams.get(orientationName);
		this.computationStep = (double) simParams.get(computationStepName);

		double latitudeInDegrees = this.latitude.getDegree();
		latitudeInDegrees += this.latitude.getMinutes()/60.0;
		latitudeInDegrees += this.latitude.getSeconds()/3600.0;
		double longitudeInDegrees = this.longitude.getDegree();
		longitudeInDegrees += this.longitude.getMinutes()/60.0;
		longitudeInDegrees += this.longitude.getSeconds()/3600.0;
		this.location = new double[] {latitudeInDegrees, longitudeInDegrees};

		if (DEBUG) {
			this.logMessage(
					"StochasticSunIntensityModel::setSimulationRunParameters");
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		if (DEBUG) {
			this.logMessage("initialiseState initialTime " + initialTime);
		}

		// create the random number generator
		this.rgCloudTransparency = new RandomDataGenerator();

		this.currentZonedDateTime =
				ZonedDateTime.ofInstant(this.startInstant, this.zoneId);
		SunTimes st = SunTimes.compute()
				.on(this.currentZonedDateTime)
            	.latitude(latitude.getDegree(),
            			  latitude.getMinutes(),
            			  latitude.getSeconds())
            	.longitude(longitude.getDegree(),
            			   longitude.getMinutes(),
            			   longitude.getSeconds())
            	.execute();
		ZonedDateTime sunRiseTime = st.getRise();
		ZonedDateTime sunSetTime = st.getSet();

		if (sunRiseTime.compareTo(sunSetTime) <= 0) {
			// next event is sunrise, hence we are at night
			this.currentState = SunState.NIGHT;
		} else {
			// next event is sunset, hence we are during the day
			this.currentState = SunState.DAY;
		}

		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent = this.currentStateTime.add(this.nextTimeAdvance);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		if (DEBUG) {
			this.logMessage("fixpointInitialiseVariables "
							+ this.sunIntensityCoef.isInitialised());
		}

		// at each call, the method tries to initialise the variables held by
		// the model and return the number of variables that were initialised
		// and the number that could not be initialised (because variables
		// imported from other models they depend upon are not yet initialised)
		int numberOfNewlyInitialisedVariables = 0;
		int numberOfStillNotInitialisedVariables = 0;
		
		if (!this.sunIntensityCoef.isInitialised()) {
			this.sunIntensityCoef.initialise(this.computeNewIntensity());
			numberOfNewlyInitialisedVariables++;
			if (DEBUG) {
				this.logMessage("fixpointInitialiseVariables "
								+ this.sunIntensityCoef.getValue());
			}
		}

		// the two counters are returned and aggregated among the different
		// execution of fixpointInitialiseVariables in the different models
		// if the total numbers gives 0 still not initialised variable, then
		// the fix point has been reached, but if there are still variables not
		// initialised but some have been initialised during the run (i.e.,
		// numberOfNewlyInitialisedVariables > 0) then the method
		// fixpointInitialiseVariables must be rerun on all models until all
		// variables have been initialised
		return new Pair<Integer, Integer>(numberOfNewlyInitialisedVariables,
										  numberOfStillNotInitialisedVariables);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		if (DEBUG) {
			this.logMessage("timeAdvance " + this.currentState);
		}

		if (this.currentState == null || this.currentState.equals(SunState.NIGHT)) {
			return Duration.INFINITY;
		} else {
			return new Duration(this.computationStep, this.getSimulatedTimeUnit());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		long stepInSeconds = (long) (this.computationStep * 3600.0);
		this.currentZonedDateTime =
				this.currentZonedDateTime.plusSeconds(stepInSeconds);
		this.sunIntensityCoef.setNewValue(this.computeNewIntensity(),
										  this.getCurrentStateTime());

		if (VERBOSE) {
			this.logMessage(
					"userDefinedInternalTransition new sunIntensityCoef = "
					+ this.sunIntensityCoef.getValue() + " at "
					+ this.getCurrentStateTime());
		}
	}

	/**
	 * return the new sun intensity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the new sun intensity.
	 */
	protected double	computeNewIntensity()
	{
		if (this.currentState.equals(SunState.DAY)) {
			SunPosition sunPosition =
					SunPosition.compute()
						.on(this.currentZonedDateTime)
						.at(this.location)
						.timezone(this.zoneId)
						.execute();
			double azimuthalCoef =
					azimuthalPerformanceCoefficient(
									this.orientation, sunPosition.getAzimuth());		
			double altitudeCoef =
					altitudePerformance(this.slope, sunPosition.getAltitude());
			// Not a very good stochastic model for the cloud factor as there
			// is no correlation between successive values of transparency. A
			// stochastic differential equation would be a better fit.
			double cloudTransparency =
					this.rgCloudTransparency.nextUniform(0.0, 1.0);

			if (DEBUG) {
				this.logMessage("The sun position is " + sunPosition);
				this.logMessage("The azimuthal performance is " + azimuthalCoef);
				this.logMessage("The altitude performance is " + altitudeCoef);
				this.logMessage("The cloud transparency is " + cloudTransparency);
				this.logMessage(
					"The overall intensity coefficient at " + this.currentZonedDateTime
					+ " is "
					+ (azimuthalCoef * altitudeCoef * cloudTransparency)
					+ " "
					+ SolarPanel.CAPACITY_PER_SQUARE_METER.getMeasurementUnit());
			}

			return azimuthalCoef * altitudeCoef * cloudTransparency;
		} else {
			return 0.0;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{		
		super.userDefinedExternalTransition(elapsedTime);

		assert	this.currentStoredEvents.size() == 1 :
				new AssertionError("currentStoredEvents.size() == 1");

		SolarPanelEventI e =
				(SolarPanelEventI) this.getStoredEventAndReset().remove(0);

		e.executeOn(this);

		if (this.currentState.equals(SunState.NIGHT)) {
			this.sunIntensityCoef.setNewValue(0.0, this.getCurrentStateTime());
		}

		if (VERBOSE) {
			this.logMessage(
					"userDefinedExternalTransition on " + e
					+ " new sunIntensityCoef = "
					+ this.sunIntensityCoef.getValue() + " at "
					+ this.getCurrentStateTime());
		}
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * return a normalised angle between 0.0 and 360.0 degrees.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code angle >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param angle	a positive angle in degrees to be normalized.
	 * @return		a normalised angle between 0.0 and 360.0 degrees.
	 */
	protected static double	normalise(double angle)
	{
		if (angle < 0.0) {
			return angle + 360.0;
		} else {
			if (angle > 306.0) {
				return angle - 360.0;
			} else {
				return angle;
			}
		}
	}

	/**
	 * return a performance coefficient given the angle between the sun azimuth
	 * and the orientation of the panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param orientation	orientation of the solar panel.
	 * @param sunAzimuth	azimuth of the sun.
	 * @return				a performance coefficient given the angle between the sun azimuth and the orientation of the panel.
	 */
	public static double	azimuthalPerformanceCoefficient(
		double orientation,
		double sunAzimuth
		)
	{
		double ret = -0.01;
		double normalizedOrientation = normalise(orientation);
		double normalizedSunAzimuth = normalise(sunAzimuth);
		double min = normalise(normalizedOrientation - 90.0);
		double max = normalise(normalizedOrientation + 90.0);
		if (min > 180.0) {
			normalizedOrientation = normalise(normalizedOrientation + 180.0);
			normalizedSunAzimuth = normalise(normalizedSunAzimuth + 180.0);
		}
		if (normalizedSunAzimuth >= min && normalizedSunAzimuth <= max) {
			// sun is on the side of the panel
			ret = 1.0 - (Math.abs(normalizedOrientation - normalizedSunAzimuth)/90.0);
		} else {
			// sun is not on the side of the panel
			ret = 0.0;
		}
		return ret;
	}

	/**
	 * return a performance coefficient given the angle between the sun altitude
	 * and the slope of the panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param slope			the slope of the panel from the ground.
	 * @param sunAltitude	the altitude of the sun from the horizon.
	 * @return				a performance coefficient given the angle between the sun altitude and the slope of the panel.
	 */
	public static double	altitudePerformance(
		double slope,
		double sunAltitude
		)
	{
		if (sunAltitude < 0.0) {
			return 0.0;
		} else {
			double perpendicular = 90.0 - slope;
			return (90.0 - Math.abs(perpendicular - sunAltitude))/90.0;
		}
	}

	// -------------------------------------------------------------------------
	// Internal tests
	// -------------------------------------------------------------------------

	/**
	 * small tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param args command line arguments.
	 */
	public static void	main(String[] args)
	{
		double[] parisLocation = new double[] { 48.83, 2.34 };
		ZoneId parisZoneId = ZoneId.of("Europe/Paris");

		ZonedDateTime current =
				ZonedDateTime.ofInstant(
						Instant.parse("2025-03-10T12:00:00.00Z"),
						parisZoneId);
		SunTimes paris =
				SunTimes.compute()
                	.on(current)
                	.latitude(48, 51, 24.0)	// Latitude of Paris: 48 51'24" N
                	.longitude(2, 21, 6.0)	// Longitude:          2 21'06" E
                	.execute();

		System.out.println("Given a location in Paris on " + current);
		System.out.println("The sun rise at " + paris.getRise());
		System.out.println("The sun set at " + paris.getSet());

		SunPosition parisSunPosition =
				SunPosition.compute()
					.on(current)
					.at(parisLocation)
					.timezone(parisZoneId)
					.execute();
		System.out.println("The sun position is " + parisSunPosition);
		double azimuthalCoef =
				azimuthalPerformanceCoefficient(
									190.0, parisSunPosition.getAzimuth());		
		System.out.println("The azimuthal performance is " + azimuthalCoef);
		double altitudeCoef =
				altitudePerformance(45.0, parisSunPosition.getAltitude());
		System.out.println("The altitude performance is " + altitudeCoef);
		System.out.println("The optimalProduction at " + current
				+ " = " +
				(azimuthalCoef * altitudeCoef
				* SolarPanelSimulationConfigurationI.NB_SQUARE_METERS
				* SolarPanel.CAPACITY_PER_SQUARE_METER.getData())
				+ " "
				+ SolarPanel.CAPACITY_PER_SQUARE_METER.getMeasurementUnit());

		double orientation = 190.0;
		System.out.println("Given an orientation of " + orientation);
		System.out.println("azimuthal performance at 10.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 10.0));
		System.out.println("azimuthal performance at 55.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 10.0));
		System.out.println("azimuthal performance at 100.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 100.0));
		System.out.println("azimuthal performance at 145.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 145.0));
		System.out.println("azimuthal performance at 190.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 190.0));
		System.out.println("azimuthal performance at 235.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 235.0));
		System.out.println("azimuthal performance at 280.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 280.0));
		System.out.println("azimuthal performance at 325.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 325.0));
		System.out.println("azimuthal performance at 10.0 = " +
				azimuthalPerformanceCoefficient(
						orientation, 10.0));

		double slope = 45.0;
		System.out.println("Given a slope of " + slope);
		System.out.println("altitude performance at altitude 0.0 = " +
				altitudePerformance(45.0, 0.0));
		System.out.println("altitude performance at altitude 30.0 = " +
				altitudePerformance(45.0, 30.0));
		System.out.println("altitude performance at altitude 45.0 = " +
				altitudePerformance(45.0, 45.0));
		System.out.println("altitude performance at altitude 60.0 = " +
				altitudePerformance(45.0, 60.0));
	}
}
// -----------------------------------------------------------------------------
