package fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events;

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

import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.shredzone.commons.suncalc.SunTimes;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.DeterministicSunRiseAndSetModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.AstronomicalSunRiseAndSetModel;

// -----------------------------------------------------------------------------
/**
 * The class <code>Sunrise</code> implements the type of internal scheduled
 * events of the {@code SunRiseAndSetModel} that trigger the effect of a sunrise
 * on this model.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This event is used internally by models that trigger sun set and sun rise
 * for the solar panel power production simulator. It is made a schedulable
 * event to ease the chaining of events between sunrises and sunsets,
 * generating each others through the method {@code generateNewEvents} that
 * schedulable events feature.
 * </p>
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
public class			InternalSunriseEvent
extends		ES_Event
implements	InternalSunRiseAndSetEventI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a sunrise event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code zdtOccurrence != null && getEventInformation().equals(zdtOccurrence)}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the created event.
	 * @param zdtOccurrence		the time time of occurrence in {@code ZoneDateTime} format.
	 */
	public				InternalSunriseEvent(
		Time timeOfOccurrence,
		CurrentZonedDateTime zdtOccurrence
		)
	{
		super(timeOfOccurrence, zdtOccurrence);

		// Preconditions checking
		assert	zdtOccurrence != null &&
							getEventInformation().equals(zdtOccurrence) :
				new AssertionError(
						"Precondition violation: zdtOccurrence != null && "
						+ "getEventInformation().equals(zdtOccurrence)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.events.ES_Event#generateNewEvents(fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model)
	 */
	@Override
	public Set<ES_EventI>	generateNewEvents(AtomicES_Model model)
	{
		assert	model instanceof AstronomicalSunRiseAndSetModel ||
						model instanceof DeterministicSunRiseAndSetModel :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "SunRiseAndSetModel \\ model instanceof "
						+ "DeterministicSunRiseAndSetModel");

		// A sunrise event generates the next sunset event

		CurrentZonedDateTime current =
							(CurrentZonedDateTime) this.getEventInformation();
		ZonedDateTime sunSetTime = null;
		if (model instanceof AstronomicalSunRiseAndSetModel) {
			Position latitude = ((AstronomicalSunRiseAndSetModel)model).getLatitude();
			Position longitude = ((AstronomicalSunRiseAndSetModel)model).getLongitude();
			SunTimes st = SunTimes.compute()
				// as we are at sunrise, add 15 minutes to be sure that the next
				// event is a sunset and not a sunrise
            	.on(current.getCurrent().plusMinutes(15))
            	.latitude(latitude.getDegree(),
            			  latitude.getMinutes(),
            			  latitude.getSeconds())
            	.longitude(longitude.getDegree(),
            			   longitude.getMinutes(),
            			   longitude.getSeconds())
            	.execute();
			ZonedDateTime sunRiseTime = st.getRise();
			sunSetTime = st.getSet();

			assert	sunRiseTime.compareTo(sunSetTime) > 0 :
					new AssertionError(
							"Just after sunrise, the next sunset must happen "
							+ "before the next sunrise!");
		} else {
			sunSetTime =
				ZonedDateTime.ofInstant(
						SolarPanelSimulationConfigurationI.getSunset(
										current.getCurrent().toInstant()),
										current.getCurrent().getZone());
		}

		double d = java.time.Duration.
					between(current.getCurrent(), sunSetTime).abs().getSeconds();
		TimeUnit simulationTimeUnit = this.getTimeOfOccurrence().getTimeUnit();
		double delay = d/TimeUnit.SECONDS.convert(1, simulationTimeUnit);
		Time t = this.getTimeOfOccurrence().add(
									new Duration(delay, simulationTimeUnit));
		Set<ES_EventI> s = new HashSet<>();
		s.add(new InternalSunsetEvent(t, new CurrentZonedDateTime(sunSetTime)));
		return s;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.InternalSunRiseAndSetEventI#generateExternalEvent()
	 */
	public	ExternalSolarPanelEventI	generateExternalEvent()
	{
		return new SunriseEvent(this.getTimeOfOccurrence(),
								this.getEventInformation());
	}
}
// -----------------------------------------------------------------------------
