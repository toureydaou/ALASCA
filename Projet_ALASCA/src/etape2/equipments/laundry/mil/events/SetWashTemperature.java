package etape2.equipments.laundry.mil.events;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape2.equipments.laundry.mil.LaundryOperationI;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

// -----------------------------------------------------------------------------
/**
 * The class <code>SetWashTemperature</code> defines the simulation event
 * of setting the wash temperature on the laundry machine.
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
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class SetWashTemperature extends Event implements LaundryEventI {
	// -------------------------------------------------------------------------
	// Inner classes and types
	// -------------------------------------------------------------------------

	/**
	 * The class <code>WashTemperatureValue</code> represents the wash temperature
	 * value in the event information.
	 */
	public static class WashTemperatureValue implements EventInformationI {
		private static final long serialVersionUID = 1L;
		protected final double temperature;

		public WashTemperatureValue(double temperature) {
			this.temperature = temperature;
		}

		
		public String contentAsString() {
			return "WashTemperatureValue(" + this.temperature + ")";
		}
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>SetWashTemperature</code> event.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code temperature >= 0.0}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the event.
	 * @param temperature		the wash temperature in degrees Celsius.
	 */
	public SetWashTemperature(Time timeOfOccurrence, double temperature) {
		super(timeOfOccurrence, new WashTemperatureValue(temperature));
		assert temperature >= 0.0;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean hasPriorityOver(EventI e) {
		return false;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI)
	 */
	@Override
	public void executeOn(AtomicModelI model) {
		assert model instanceof LaundryOperationI
				: new NeoSim4JavaException("Precondition violation: model instanceof "
						+ "LaundryOperationI");

		LaundryOperationI laundry = (LaundryOperationI) model;
		assert laundry.getState() == LaundryState.ON
				: new NeoSim4JavaException("model not in the right state, should be "
						+ "LaundryState.ON but is " + laundry.getState());

		WashTemperatureValue tempValue = (WashTemperatureValue) this.getEventInformation();
		laundry.setCurrentWashTemperature(tempValue.temperature, this.getTimeOfOccurrence());
	}
}
// -----------------------------------------------------------------------------
