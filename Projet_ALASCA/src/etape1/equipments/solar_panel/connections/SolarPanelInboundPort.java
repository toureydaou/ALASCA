package etape1.equipments.solar_panel.connections;

import etape1.equipments.solar_panel.SolarPanelCI;
import etape1.equipments.solar_panel.SolarPanelImplementationI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;


// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelInboundPort</code> implements an inbound port for
 * the {@code SolarPanelCI} component interface.
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
 * <p>Created on : 2025-09-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			SolarPanelInboundPort
extends		AbstractInboundPort
implements	SolarPanelCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port with a generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof SolarPanelImplementationI}
	 * pre	{@code SolarPanelCI.class.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(SolarPanelCI.class)}
	 * </pre>
	 *
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				SolarPanelInboundPort(ComponentI owner)
	throws Exception
	{
		super(SolarPanelCI.class, owner);

		// Preconditions checking
		assert	owner instanceof SolarPanelImplementationI :
				new PreconditionException(
						"owner instanceof SolarPanelImplementationI");
	}

	/**
	 * create an inbound port with the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof SolarPanelImplementationI}
	 * pre	{@code !owner.isPortExisting(uri)}
	 * pre	{@code SolarPanelCI.class.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(SolarPanelCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri					unique identifier of the port.
	 * @param owner					component that owns this port.
	 * @throws Exception 			<i>to do</i>.
	 */
	public				SolarPanelInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, SolarPanelCI.class, owner);

		// Preconditions checking
		assert	owner instanceof SolarPanelImplementationI :
				new PreconditionException(
						"owner instanceof SolarPanelImplementationI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getNumberOfSquareMeters()
	 */
	@Override
	public int			getNumberOfSquareMeters() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((SolarPanelImplementationI)o).getNumberOfSquareMeters());
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getNominalPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	getNominalPowerProductionCapacity() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((SolarPanelImplementationI)o).
										getNominalPowerProductionCapacity());
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getCurrentPowerProductionLevel()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerProductionLevel()
	throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((SolarPanelImplementationI)o).
										getCurrentPowerProductionLevel());
	}
}
// -----------------------------------------------------------------------------
