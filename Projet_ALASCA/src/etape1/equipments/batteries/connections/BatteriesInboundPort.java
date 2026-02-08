package etape1.equipments.batteries.connections;

import etape1.equipments.batteries.BatteriesCI;
import etape1.equipments.batteries.BatteriesImplementationI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;


// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesInboundPort</code> implements an inbound port for
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
public class			BatteriesInboundPort
extends		AbstractInboundPort
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
	 * create and initialise an inbound port with a generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof BatteriesImplementationI}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(BatteriesCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				BatteriesInboundPort(ComponentI owner) throws Exception
	{
		super(BatteriesCI.class, owner);
		assert	owner instanceof BatteriesImplementationI :
				new PreconditionException(
						"owner instanceof BatteriesImplementationI");
	}

	/**
	 * create and initialise an inbound port with a given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code owner instanceof BatteriesImplementationI}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(BatteriesCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				BatteriesInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, BatteriesCI.class, owner);
		assert	owner instanceof BatteriesImplementationI :
				new PreconditionException(
						"owner instanceof BatteriesImplementationI");
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
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).nominalCapacity());
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#currentCapacity()
	 */
	@Override
	public SignalData<Double>	currentCapacity() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).currentCapacity());
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#areCharging()
	 */
	@Override
	public boolean		areCharging() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).areCharging());
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#areDischarging()
	 */
	@Override
	public boolean		areDischarging() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).areDischarging());
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#chargeLevel()
	 */
	@Override
	public SignalData<Double>	chargeLevel() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).chargeLevel());
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#getCurrentPowerConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerConsumption() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((BatteriesImplementationI)o).getCurrentPowerConsumption());
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#startCharging()
	 */
	@Override
	public void			startCharging() throws Exception
	{
		this.getOwner().handleRequest(
					o -> { ((BatteriesImplementationI)o).startCharging();
						   return null;
						 });
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesCI#stopCharging()
	 */
	@Override
	public void			stopCharging() throws Exception
	{
		this.getOwner().handleRequest(
					o -> { ((BatteriesImplementationI)o).stopCharging();
						   return null;
						 });
	}
}
// -----------------------------------------------------------------------------
