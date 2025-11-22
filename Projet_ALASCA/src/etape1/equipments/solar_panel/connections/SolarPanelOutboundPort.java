package etape1.equipments.solar_panel.connections;

import etape1.equipments.solar_panel.SolarPanelCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelOutboundPort</code> implements an outbound port for
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
public class			SolarPanelOutboundPort
extends		AbstractOutboundPort
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
	 * create and initialise outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code SolarPanelCI.class.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(SolarPanelCI.class)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				SolarPanelOutboundPort(ComponentI owner)
	throws Exception
	{
		super(SolarPanelCI.class, owner);
	}

	/**
	 * create and initialize outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code SolarPanelCI.class.isAssignableFrom(getClass())}
	 * post	{@code !isDestroyed()}
	 * post	{@code getImplementedInterface().equals(SolarPanelCI.class)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * post	{@code owner.isPortExisting(getPortURI())}
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				SolarPanelOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, SolarPanelCI.class, owner);
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
		return ((SolarPanelCI)this.getConnector()).getNumberOfSquareMeters();
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getNominalPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	getNominalPowerProductionCapacity() throws Exception
	{
		return ((SolarPanelCI)this.getConnector()).
											getNominalPowerProductionCapacity();
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getCurrentPowerProductionLevel()
	 */
	@Override
	public SignalData<Double> getCurrentPowerProductionLevel() throws Exception
	{
		return ((SolarPanelCI)this.getConnector()).
											getCurrentPowerProductionLevel();
	}
}
// -----------------------------------------------------------------------------
