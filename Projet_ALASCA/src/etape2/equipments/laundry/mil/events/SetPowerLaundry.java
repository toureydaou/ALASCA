package etape2.equipments.laundry.mil.events;

import etape1.equipements.laundry.Laundry;
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

import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The class <code>SetPowerLaundry</code> defines the simulation event of the
 * laundry machine power being set to some level (in watts).
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
public class			SetPowerLaundry
extends		ES_Event
implements	LaundryEventI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>PowerValue</code> represent a power value to be passed
	 * as an {@code EventInformationI} when creating a {@code SetPowerLaundry}
	 * event.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p><strong>Implementation Invariants</strong></p>
	 *
	 * <pre>
	 * invariant	{@code power >= 0.0 && power <= MAX_POWER}
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
	public static class	PowerValue
	implements	EventInformationI
	{
		private static final long serialVersionUID = 1L;
		/* a power in watts.												*/
		protected final double	power;

		/**
		 * create an instance of {@code PowerValue}.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code power >= 0.0 && power <= MAX_POWER}
		 * post	{@code getPower() == power}
		 * </pre>
		 *
		 * @param power	the power in watts to put in this container.
		 */
		public			PowerValue(double power)
		{
			super();

			assert	power >= 0.0 &&
						power <= Laundry.MAX_POWER_IN_WATTS.getData() :
					new NeoSim4JavaException(
							"power >= 0.0 && power <= "
							+ "MAX_POWER");

			this.power = power;
		}

		/**
		 * return the power value in watts.
		 *
		 * <p><strong>Contract</strong></p>
		 *
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code return >= 0.0 && return <= MAX_POWER}
		 * </pre>
		 *
		 * @return	the power value in watts.
		 */
		public double	getPower()	{ return this.power; }

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String	toString()
		{
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append('[');
			sb.append(this.power);
			sb.append(']');
			return sb.toString();
		}
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** the power value to be set on the laundry machine when the event will be
	 *  executed.															*/
	protected final PowerValue	powerValue;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a {@code SetPowerLaundry} event which content is a
	 * {@code PowerValue}.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code content != null && content instanceof PowerValue}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code content == null || getEventInformation().equals(content)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time at which the event must be executed in simulated time.
	 * @param content			the power value to be set on the laundry machine when the event will be executed.
	 */
	public				SetPowerLaundry(
		Time timeOfOccurrence,
		EventInformationI content
		)
	{
		super(timeOfOccurrence, content);

		assert	content != null && content instanceof PowerValue :
				new NeoSim4JavaException(
						"Precondition violation: event content is null or"
						+ " not a PowerValue " + content);

		this.powerValue = (PowerValue) content;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.events.ES_Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		// if many laundry events occur at the same time, the SetPowerLaundry one
		// will be executed first except for SwitchOnLaundry ones.
		if (e instanceof SwitchOffLaundry) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI)
	 */
	@Override
	public void			executeOn(AtomicModelI model)
	{
		assert	model instanceof LaundryOperationI :
				new NeoSim4JavaException(
						"Precondition violation: model instanceof "
						+ "LaundryOperationI");

		LaundryOperationI laundry = (LaundryOperationI)model;
		assert	laundry.getState() == LaundryState.ON
				|| laundry.getState() == LaundryState.WASHING
				|| laundry.getState() == LaundryState.RINSING
				|| laundry.getState() == LaundryState.SPINNING
				|| laundry.getState() == LaundryState.DRYING :
				new NeoSim4JavaException(
						"model not in the right state, should be "
						+ "ON, WASHING, RINSING, SPINNING or DRYING but is " + laundry.getState());
		laundry.setCurrentWashingPower(this.powerValue.getPower(), this.getTimeOfOccurrence());
	}
}
// -----------------------------------------------------------------------------
