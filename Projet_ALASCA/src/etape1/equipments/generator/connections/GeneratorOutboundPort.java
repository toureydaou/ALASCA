package etape1.equipments.generator.connections;

import etape1.equipments.generator.Generator.State;
import etape1.equipments.generator.GeneratorCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratortOutboundPort</code> implements an outbound port for
 * the {@code GeneratorCI} component interface.
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
 * <p>Created on : 2025-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			GeneratorOutboundPort
extends		AbstractOutboundPort
implements	GeneratorCI
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
	 * post	{@code getImplementedInterface().equals(GeneratorCI.class)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code !connected()}
	 * post {@code !isRemotelyConnected()}
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				GeneratorOutboundPort(ComponentI owner)
	throws Exception
	{
		super(GeneratorCI.class, owner);
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
	 * post	{@code getImplementedInterface().equals(GeneratorCI.class)}
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
	public				GeneratorOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, GeneratorCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipments.generator.GeneratorCI#getState()
	 */
	@Override
	public State		getState() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).getState();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#nominalOutputTension()
	 */
	@Override
	public Measure<Double>	nominalOutputTension() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).nominalOutputTension();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#tankCapacity()
	 */
	@Override
	public Measure<Double>	tankCapacity() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).tankCapacity();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#currentTankLevel()
	 */
	@Override
	public SignalData<Double>	currentTankLevel() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).currentTankLevel();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#maxPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	maxPowerProductionCapacity() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).maxPowerProductionCapacity();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#currentPowerProduction()
	 */
	@Override
	public SignalData<Double>	currentPowerProduction() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).currentPowerProduction();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#minFuelConsumption()
	 */
	@Override
	public Measure<Double>	minFuelConsumption() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).minFuelConsumption();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#maxFuelConsumption()
	 */
	@Override
	public Measure<Double>	maxFuelConsumption() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).maxFuelConsumption();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#currentFuelConsumption()
	 */
	@Override
	public SignalData<Double>	currentFuelConsumption() throws Exception
	{
		return ((GeneratorCI)this.getConnector()).currentFuelConsumption();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#startGenerator()
	 */
	@Override
	public void			startGenerator() throws Exception
	{
		((GeneratorCI)this.getConnector()).startGenerator();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#stopGenerator()
	 */
	@Override
	public void			stopGenerator() throws Exception
	{
		((GeneratorCI)this.getConnector()).stopGenerator();
	}
}
// -----------------------------------------------------------------------------
