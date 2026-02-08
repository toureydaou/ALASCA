package etape2.equipments.kettle.mil.events;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape2.equipments.kettle.mil.KettleOperationI;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The class <code>SwitchOffKettle</code> defines the simulation event of the
 * kettle (water heater) being switched off.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class			SwitchOffKettle
extends		ES_Event
implements	KettleEventI
{
	private static final long serialVersionUID = 1L;

	public				SwitchOffKettle(Time timeOfOccurrence)
	{
		super(timeOfOccurrence, null);
	}

	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		return false;
	}

	@Override
	public void			executeOn(AtomicModelI model)
	{
		assert	model instanceof KettleOperationI :
				new NeoSim4JavaException(
						"Precondition violation: model instanceof "
						+ "KettleOperationI");

		KettleOperationI kettle = (KettleOperationI)model;
		assert	kettle.getState() == KettleState.ON :
				new NeoSim4JavaException(
						"model not in the right state, should be "
						+ "KettleState.ON but is " + kettle.getState());
		kettle.setState(KettleState.OFF);
	}
}
// -----------------------------------------------------------------------------
