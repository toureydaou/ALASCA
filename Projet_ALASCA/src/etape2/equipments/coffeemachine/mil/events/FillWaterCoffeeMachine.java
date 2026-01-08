package etape2.equipments.coffeemachine.mil.events;

import etape1.equipements.coffee_machine.CoffeeMachine;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape2.equipments.coffeemachine.mil.CoffeeMachineElectricityModel;
import etape2.equipments.coffeemachine.mil.CoffeeMachineOperationI;

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
 * The class <code>SetWaterCoffeeMachine</code> defines the simulation event of the
 * heater water being set to some level (in watts).
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
 * <p>Created on : 2023-10-12</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			FillWaterCoffeeMachine
extends		ES_Event
implements	CoffeeMachineEventI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>WaterValue</code> represent a water value to be passed
	 * as an {@code EventInformationI} when creating a {@code SetWaterCoffeeMachine}
	 * event.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Implementation Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code water >= 0.0 && water <= CoffeeMachineElectricityModel.MAX_HEATING_POWER}
	 * </pre>
	 * 
	 * <p><strong>Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2023-10-13</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	WaterValue
	implements	EventInformationI
	{
		private static final long serialVersionUID = 1L;
		/* a water in watts.												*/
		protected final double	water;

		/**
		 * create an instance of {@code WaterValue}.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code water >= 0.0 && water <= CoffeeMachineExternalControlI.MAX_POWER_LEVEL.getData()}
		 * post	{@code getWater() == water}
		 * </pre>
		 *
		 * @param water	the water in watts to put in this container.
		 */
		public			WaterValue(double water)
		{
			super();

			assert	water >= 0.0 &&
						water <= CoffeeMachine.WATER_CAPACITY.getData() :
					new NeoSim4JavaException(
							"water >= 0.0 && water <= CoffeeMachineExternalControlI."
							+ "MAX_POWER_LEVEL.getData()");

			this.water = water;
		}

		/**
		 * return the water value in watts.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code return >= 0.0 && return <= CoffeeMachineElectricityModel.MAX_HEATING_POWER}
		 * </pre>
		 *
		 * @return	the water value in watts.
		 */
		public double	getWater()	{ return this.water; }

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String	toString()
		{
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append('[');
			sb.append(this.water);
			sb.append(']');
			return sb.toString();
		}
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** the water value to be set on the heater when the event will be
	 *  executed.															*/
	protected final WaterValue	waterValue;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a {@code SetWaterCoffeeMachine} event which content is a
	 * {@code WaterValue}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code content != null && content instanceof WaterValue}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code content == null || getEventInformation().equals(content)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time at which the event must be executed in simulated time.
	 * @param content			the water value to be set on the heater when the event will be executed.
	 */
	public				FillWaterCoffeeMachine(
		Time timeOfOccurrence,
		EventInformationI content
		)
	{
		super(timeOfOccurrence, content);

		assert	content != null && content instanceof WaterValue :
				new NeoSim4JavaException(
						"Precondition violation: event content is null or"
						+ " not a WaterValue " + content);

		this.waterValue = (WaterValue) content;
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
		// if many heater events occur at the same time, the SetWaterCoffeeMachine one
		// will be executed first except for SwitchOffCoffeeMachine ones.
		if (e instanceof SwitchOffCoffeeMachine) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI)
	 */
	@Override
	public void			executeOn(AtomicModelI model)
	{
		assert model instanceof CoffeeMachineOperationI : new NeoSim4JavaException(
				"Precondition violation: model instanceof " + "CoffeeMachineOperationI");

		CoffeeMachineOperationI coffeeMachine = (CoffeeMachineOperationI) model;
		assert coffeeMachine.getState() != CoffeeMachineState.HEATING
				: new NeoSim4JavaException("model not in the right state, should not be " + "CoffeeMachineState.HEATING but is "
						+ coffeeMachine.getState());
		coffeeMachine.setCurrentWaterLevel(this.waterValue.getWater(), this.getTimeOfOccurrence());
	}
}
// -----------------------------------------------------------------------------
