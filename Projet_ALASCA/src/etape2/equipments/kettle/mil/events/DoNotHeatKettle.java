package etape2.equipments.kettle.mil.events;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape2.equipments.kettle.mil.KettleOperationI;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The class <code>DoNotHeatKettle</code> defines the simulation event of the
 * kettle (water heater) stopping heating.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class			DoNotHeatKettle
extends		Event
implements	KettleEventI
{
	private static final long serialVersionUID = 1L;

	public				DoNotHeatKettle(Time timeOfOccurrence)
	{
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		if (e instanceof SwitchOnKettle) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void			executeOn(AtomicModelI model)
	{
		assert	model instanceof KettleOperationI :
				new NeoSim4JavaException(
						"Precondition violation: model instanceof "
						+ "KettleOperationI");

		KettleOperationI kettle = (KettleOperationI)model;
		assert	kettle.getState() == KettleState.HEATING :
				new NeoSim4JavaException(
						"model not in the right state, should be "
						+ "KettleState.HEATING but is " + kettle.getState());
		kettle.setState(KettleState.ON);
	}
}
// -----------------------------------------------------------------------------
