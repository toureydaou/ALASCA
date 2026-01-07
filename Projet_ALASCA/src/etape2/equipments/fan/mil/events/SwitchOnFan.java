package etape2.equipments.fan.mil.events;

import etape2.equipments.fan.mil.AbstractFanEvent;
import etape2.equipments.fan.mil.FanOperationI;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The class <code>SwitchOnFan</code> defines the simulation event of the Fan
 * being switched on.
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
public class SwitchOnFan extends AbstractFanEvent implements FanEventI {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>SwitchOnFan</code> event.
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
	public SwitchOnFan(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.events.ES_Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean hasPriorityOver(EventI e) {
		// if many Fan events occur at the same time, the
		// SwitchOnFan one will be executed first.
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI)
	 */
	@Override
	public void executeOn(AtomicModelI model) {
		assert	model instanceof FanOperationI :
			new NeoSim4JavaException(
					"model instanceof FanOperationI");

		((FanOperationI)model).turnOn();
	}
}
// -----------------------------------------------------------------------------
