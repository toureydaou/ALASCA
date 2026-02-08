package etape2.equipments.kettle.mil;

import java.util.concurrent.TimeUnit;

// -----------------------------------------------------------------------------
/**
 * The interface <code>KettleSimulationConfigurationI</code> defines common
 * constants and configuration parameters for the Kettle simulator.
 *
 * <p>Created on : 2026-02-06</p>
 */
public interface		KettleSimulationConfigurationI
{
	/** time unit used in the Kettle simulator. */
	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;

	public static boolean	staticInvariants()
	{
		boolean ret = true;
		return ret;
	}
}
// -----------------------------------------------------------------------------
