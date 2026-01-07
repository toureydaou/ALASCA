package fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events;

import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

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

import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunState;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunStateManagementI;

// -----------------------------------------------------------------------------
/**
 * The class <code>SunsetEvent</code> implements the type of events that
 * trigger the effect of a sunset on the solar panel simulation models.
 *
 * <p><strong>Description</strong></p>
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
 * <p>Created on : 2025-10-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			SunsetEvent
extends		Event
implements	ExternalSolarPanelEventI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a sunset event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code content != null && content instanceof CurrentZonedDateTime}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code getEventInformation().equals(content)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the created event.
	 * @param content			the time of occurrence in {@code ZoneDateTime} format.
	 */
	public				SunsetEvent(
		Time timeOfOccurrence,
		EventInformationI content
		)
	{
		this(timeOfOccurrence,
			 AssertionChecking.assertTrueAndReturnOrThrow(
					content != null && content instanceof CurrentZonedDateTime,
					(CurrentZonedDateTime)content,
					() -> new NeoSim4JavaException(
								"Precondition violation: content != null && "
								+ "content instanceof CurrentZonedDateTime")));
	}

	/**
	 * create a sunset event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code zdtOccurrence != null}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code getEventInformation().equals(zdtOccurrence)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the created event.
	 * @param zdtOccurrence		the time time of occurrence in {@code ZoneDateTime} format.
	 */
	public				SunsetEvent(
		Time timeOfOccurrence,
		CurrentZonedDateTime zdtOccurrence
		)
	{
		super(timeOfOccurrence, zdtOccurrence);

		// Preconditions checking
		assert	zdtOccurrence != null :
				new NeoSim4JavaException(
						"Precondition violation: zdtOccurrence != null");

		// Postconditions checking
		assert	getEventInformation().equals(zdtOccurrence) :
				new NeoSim4JavaException(
						"Postcondition violation: "
						+ "getEventInformation().equals(zdtOccurrence)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI)
	 */
	@Override
	public void			executeOn(AtomicModelI model)
	{
		// Preconditions checking
		assert	model instanceof SunStateManagementI :
				new AssertionError(
						"Precondition violation: model instanceof "
						+ "SunStateManagementI");

		((SunStateManagementI)model).setState(SunState.NIGHT);
		((SunStateManagementI)model).setCurrent(
							((CurrentZonedDateTime)this.content).getCurrent());
	}
}
// -----------------------------------------------------------------------------
