package etape1.equipments.batteries.connections;

import etape1.equipments.batteries.BatteriesCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesConnector</code> implements a connector for
 * the {@code BatteriesCI} component interface.
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
 * <p>Created on : 2025-09-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			BatteriesConnector
extends		AbstractConnector
implements	BatteriesCI
{
	/**
	 * @see etape1.equipments.batteries.BatteriesCI#nominalCapacity()
	 */
	@Override
	public Measure<Double>	nominalCapacity() throws Exception
	{
		return ((BatteriesCI)this.offering).nominalCapacity();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#currentCapacity()
	 */
	@Override
	public SignalData<Double>	currentCapacity() throws Exception
	{
		return ((BatteriesCI)this.offering).currentCapacity();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#areCharging()
	 */
	@Override
	public boolean		areCharging() throws Exception
	{
		return ((BatteriesCI)this.offering).areCharging();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#areDischarging()
	 */
	@Override
	public boolean		areDischarging() throws Exception
	{
		return ((BatteriesCI)this.offering).areDischarging();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#chargeLevel()
	 */
	@Override
	public SignalData<Double>	chargeLevel() throws Exception
	{
		return ((BatteriesCI)this.offering).chargeLevel();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#getCurrentPowerConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerConsumption() throws Exception
	{
		return ((BatteriesCI)this.offering).getCurrentPowerConsumption();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#startCharging()
	 */
	@Override
	public void			startCharging() throws Exception
	{
		((BatteriesCI)this.offering).startCharging();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#stopCharging()
	 */
	@Override
	public void			stopCharging() throws Exception
	{
		((BatteriesCI)this.offering).stopCharging();
	}
}
// -----------------------------------------------------------------------------
