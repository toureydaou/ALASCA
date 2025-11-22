package etape1.equipments.batteries.connections;

import etape1.equipments.batteries.BatteriesCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesOutboundPort</code> implements an outbound port for
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
public class			BatteriesOutboundPort
extends		AbstractOutboundPort
implements	BatteriesCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create and initialise the outbound port, with a generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(BatteriesCI.class)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				BatteriesOutboundPort(ComponentI owner)
	throws Exception
	{
		super(BatteriesCI.class, owner);
	}

	/**
	 * create and initialise the outbound port, with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(BatteriesCI.class)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				BatteriesOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, BatteriesCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#nominalCapacity()
	 */
	@Override
	public Measure<Double>	nominalCapacity() throws Exception
	{
		return ((BatteriesCI)this.getConnector()).nominalCapacity();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#currentCapacity()
	 */
	@Override
	public SignalData<Double>	currentCapacity() throws Exception
	{
		return ((BatteriesCI)this.getConnector()).currentCapacity();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#areCharging()
	 */
	@Override
	public boolean		areCharging() throws Exception
	{
		return ((BatteriesCI)this.getConnector()).areCharging();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#areDischarging()
	 */
	@Override
	public boolean		areDischarging() throws Exception
	{
		return ((BatteriesCI)this.getConnector()).areDischarging();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#chargeLevel()
	 */
	@Override
	public SignalData<Double>	chargeLevel() throws Exception
	{
		return ((BatteriesCI)this.getConnector()).chargeLevel();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#getCurrentPowerConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerConsumption() throws Exception
	{
		return ((BatteriesCI)this.getConnector()).getCurrentPowerConsumption();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#startCharging()
	 */
	@Override
	public void			startCharging() throws Exception
	{
		((BatteriesCI)this.getConnector()).startCharging();
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#stopCharging()
	 */
	@Override
	public void			stopCharging() throws Exception
	{
		((BatteriesCI)this.getConnector()).stopCharging();
	}
}
// -----------------------------------------------------------------------------
