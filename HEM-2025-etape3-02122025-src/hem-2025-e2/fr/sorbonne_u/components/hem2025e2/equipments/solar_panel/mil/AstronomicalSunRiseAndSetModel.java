package fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil;

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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.shredzone.commons.suncalc.SunTimes;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.InternalSunRiseAndSetEventI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.InternalSunriseEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.InternalSunsetEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunriseEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunsetEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SolarPanelEventI.CurrentZonedDateTime;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SolarPanelEventI.Position;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>AstronomicalSunRiseAndSetModel</code> models the sunrises
 * and sunsets as events which times of occurrence are computed from actual
 * positions on earth and actual days.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation model uses astronomical computations to get the sunrise and
 * sunset times in order tp trigger events imported by other solar panel
 * simulation models.
 * </p>
 * 
 * <ul>
 * <li>Imported events: none</li>
 * <li>Exported events: {@code SunriseEvent}, {@code SunsetEventSunset}</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables: none</li>
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
 * <p>Created on : 2025-10-03</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(exported = {SunriseEvent.class, SunsetEvent.class})
//-----------------------------------------------------------------------------
public class			AstronomicalSunRiseAndSetModel
extends		AtomicES_Model
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
	public static final String	URI = "astronomical-sun-rise-and-set-model";

	/**	latitude of the solar panel in degrees, minutes and seconds.		*/
	protected Position	latitude;
	/**	longitude of the solar panel in degrees, minutes and seconds.		*/
	protected Position	longitude;
	/** start time of the simulation in {@code Instant} format.				*/
	protected Instant	startInstant;
	/** time zone of the solar panel.										*/
	protected ZoneId	zoneId;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic simulation model with the given URI (if null, one will
	 * be generated) and to be run by the given simulator using the given time
	 * unit for its clock.
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
	 * @param uri					unique identifier of the model.
	 * @param simulatedTimeUnit		time unit used for the simulation clock.
	 * @param simulationEngine		simulation engine enacting the model.
	 */
	public				AstronomicalSunRiseAndSetModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the latitude of the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the latitude of the solar panel.
	 */
	public Position		getLatitude()
	{
		return this.latitude;
	}
	
	/**
	 * return the longitude of the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the longitude of the solar panel.
	 */
	public Position		getLongitude()
	{
		return this.longitude;
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
									SunRiseAndSetModelI.LATITUDE_RP_NAME);
		String longitudeName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunRiseAndSetModelI.LONGITUDE_RP_NAME);
		String startInstantName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunRiseAndSetModelI.START_INSTANT_RP_NAME);
		String zoneIdName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunRiseAndSetModelI.ZONE_ID_RP_NAME);

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
		
		this.latitude = (Position) simParams.get(latitudeName);
		this.longitude = (Position) simParams.get(longitudeName);
		this.startInstant = (Instant) simParams.get(startInstantName);
		this.zoneId = (ZoneId) simParams.get(zoneIdName);

		if (DEBUG) {
			this.logMessage("setSimulationRunParameters");
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		ZonedDateTime start =
				ZonedDateTime.ofInstant(this.startInstant, this.zoneId);
		SunTimes st = SunTimes.compute()
				.on(start)
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
			// next event is sunrise
			long d = java.time.Duration.
								between(start, sunRiseTime).abs().getSeconds();
			TimeUnit simulationTimeUnit = initialTime.getTimeUnit();
			double delay = TimeUnit.SECONDS.convert(d, simulationTimeUnit);
			Time t = initialTime.add(new Duration(delay, simulationTimeUnit));
			this.scheduleEvent(
					new InternalSunriseEvent(t, new CurrentZonedDateTime(sunSetTime)));
			if (DEBUG) {
				this.logMessage("initialiseState sunrise planned for "
								+ sunRiseTime + " at " + t);
			}
		} else {
			// next event is sunset
			double d = java.time.Duration.
							between(start, sunSetTime).abs().getSeconds();
			TimeUnit simulationTimeUnit = initialTime.getTimeUnit();
			double delay = d/TimeUnit.SECONDS.convert(1, simulationTimeUnit);
			Time t = initialTime.add(new Duration(delay, simulationTimeUnit));
			this.scheduleEvent(
					new InternalSunsetEvent(t, new CurrentZonedDateTime(sunSetTime)));
			if (DEBUG) {
				this.logMessage("initialiseState sunset planned for "
								+ sunSetTime + " at " + t);
			}
		}

		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent = this.currentStateTime.add(this.nextTimeAdvance);
		
		if (VERBOSE) {
			this.logMessage("initialiseState first event: "
							+ this.eventList.peek());
		}
	}

	
	/**
	 * @see fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		// Preconditions checking
		assert	this.eventList.peek() != null &&
					this.eventList.peek() instanceof InternalSunRiseAndSetEventI :
				new NeoSim4JavaException(
						"Precondition violation: eventList.peek() != null "
						+ "&& this.eventList.peek() instanceof "
						+ "InternalSunRiseAndSetEventI");

		// propagate the new state as exported events

		ArrayList<EventI> ret = new ArrayList<EventI>();
		InternalSunRiseAndSetEventI e =
				(InternalSunRiseAndSetEventI) this.eventList.peek();
		ret.add(e.generateExternalEvent());

		// Postconditions checking
		assert	this.eventList.peek() != null &&
				this.eventList.peek() instanceof InternalSunRiseAndSetEventI :
			new NeoSim4JavaException(
					"Precondition violation: eventList.peek() != null "
					+ "&& this.eventList.peek() instanceof "
					+ "InternalSunRiseAndSetEventI");
		
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		if (VERBOSE) {
			this.logMessage("userDefinedInternalTransition " 
							+ this.eventList.peek());
		}

		super.userDefinedInternalTransition(elapsedTime);
	}
}
// -----------------------------------------------------------------------------
