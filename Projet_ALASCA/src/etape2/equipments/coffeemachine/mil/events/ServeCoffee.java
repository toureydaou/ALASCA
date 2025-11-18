package etape2.equipments.coffeemachine.mil.events;

import etape1.equipements.coffee_machine.CoffeeMachine;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape2.equipments.coffeemachine.mil.CoffeeMachineElectricityModel;
import etape2.equipments.coffeemachine.mil.CoffeeMachineTemperatureModel;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

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
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The class <code>Heat</code> defines the simulation event of the coffeeMachine
 * starting to heat.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * true
 * }	// no more invariant
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * true
 * }	// no more invariant
 * </pre>
 * 
 * <p>
 * Created on : 2023-10-02
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class ServeCoffee extends Event implements CoffeeMachineEventI {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>Heat</code> event.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * timeOfOccurrence != null
	 * }
	 * post	{@code
	 * getTimeOfOccurrence().equals(timeOfOccurrence)
	 * }
	 * post	{@code
	 * getEventInformation() == null
	 * }
	 * </pre>
	 *
	 * @param timeOfOccurrence time of occurrence of the event.
	 */
	public ServeCoffee(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean hasPriorityOver(EventI e) {
		// if many coffeeMachine events occur at the same time, the Heat one will be
		// executed after SwitchOnCoffeeMachine and DoNotHeat ones but before
		// SwitchOffCoffeeMachine.
		if (e instanceof SwitchOnCoffeeMachine || e instanceof DoNotHeat) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI)
	 */
	@Override
	public void executeOn(AtomicModelI model) {
		assert model instanceof CoffeeMachineElectricityModel || model instanceof CoffeeMachineTemperatureModel
				: new NeoSim4JavaException("Precondition violation: model instanceof "
						+ "CoffeeMachineElectricityModel || " + "model instanceof CoffeeMachineTemperatureModel");

		if (model instanceof CoffeeMachineElectricityModel) {
			CoffeeMachineElectricityModel coffeeMachine = (CoffeeMachineElectricityModel) model;
			assert coffeeMachine.getState() == CoffeeMachineState.ON
					&& coffeeMachine.getCurrentWaterLevel().getValue() > 0.1
					: new NeoSim4JavaException("model not in the right state, should be "
							+ "CoffeeMachineElectricityModel.State.ON but is " + coffeeMachine.getState());
			coffeeMachine.setState(CoffeeMachineState.ON, this.getTimeOfOccurrence());
			double newWaterLevel = coffeeMachine.getCurrentWaterLevel().getValue() - CoffeeMachine.CUP_OF_CAFE_CAPACITY.getData();
			coffeeMachine.setCurrentWaterLevel(newWaterLevel, timeOfOccurrence);
			
		} 
	}
}
// -----------------------------------------------------------------------------
