package etape1.equipments.solar_panel.connections;

import etape1.equipments.solar_panel.SolarPanelCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.components.connectors.AbstractConnector;


// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelConnector</code> implements a connector for
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
public class			SolarPanelConnector
extends		AbstractConnector
implements	SolarPanelCI
{
	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getNumberOfSquareMeters()
	 */
	@Override
	public int			getNumberOfSquareMeters() throws Exception
	{
		return ((SolarPanelCI)this.offering).getNumberOfSquareMeters();
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getNominalPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	getNominalPowerProductionCapacity() throws Exception
	{
		return ((SolarPanelCI)this.offering).getNominalPowerProductionCapacity();
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelCI#getCurrentPowerProductionLevel()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerProductionLevel()
	throws Exception
	{
		return ((SolarPanelCI)this.offering).getCurrentPowerProductionLevel();
	}
}
// -----------------------------------------------------------------------------
