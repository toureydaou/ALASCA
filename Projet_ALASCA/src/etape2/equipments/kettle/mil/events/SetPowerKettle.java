package etape2.equipments.kettle.mil.events;

import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape2.equipments.kettle.mil.KettleOperationI;
import fr.sorbonne_u.devs_simulation.es.events.ES_Event;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The class <code>SetPowerKettle</code> defines the simulation event of the
 * kettle (water heater) power being set to some level (in watts).
 *
 * <p>Created on : 2026-02-06</p>
 */
public class			SetPowerKettle
extends		ES_Event
implements	KettleEventI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	public static class	PowerValue
	implements	EventInformationI
	{
		private static final long serialVersionUID = 1L;
		protected final double	power;

		public			PowerValue(double power)
		{
			super();
			assert	power >= 0.0 &&
						power <= KettleImplementationI.MAX_POWER_LEVEL :
					new NeoSim4JavaException(
							"power >= 0.0 && power <= "
							+ "KettleImplementationI.MAX_POWER_LEVEL");
			this.power = power;
		}

		public double	getPower()	{ return this.power; }

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
	protected final PowerValue	powerValue;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				SetPowerKettle(
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

	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		if (e instanceof SwitchOffKettle) {
			return true;
		} else {
			return false;
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
		assert	kettle.getState() == KettleState.ON ||
				kettle.getState() == KettleState.HEATING :
				new NeoSim4JavaException(
						"model not in the right state, should be "
						+ "KettleState.ON or HEATING but is "
						+ kettle.getState());
		kettle.setCurrentHeatingPower(
				this.powerValue.getPower(),
				this.getTimeOfOccurrence());
	}
}
// -----------------------------------------------------------------------------
