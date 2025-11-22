package etape1.equipments.generator.connections;

import etape1.equipments.generator.Generator.State;
import etape1.equipments.generator.GeneratorCI;
import etape1.equipments.generator.GeneratorImplementationI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorInboundPort</code> implements an inbound port for
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
public class			GeneratorInboundPort
extends		AbstractInboundPort
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
	 * create and initialise an inbound port with a generated URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof GeneratorImplementationI}
	 * post	{@code !isDestroyed()}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(GeneratorCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				GeneratorInboundPort(ComponentI owner) throws Exception
	{
		super(GeneratorCI.class, owner);

		// Preconditions checking
		assert	owner instanceof GeneratorImplementationI :
				new PreconditionException(
						"owner instanceof GeneratorImplementationI");
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
	 * pre	{@code owner instanceof GeneratorImplementationI}
	 * post	{@code !isDestroyed()}
	 * post	{@code getPortURI().equals(uri)}
	 * post	{@code getOwner().equals(owner)}
	 * post	{@code getImplementedInterface().equals(GeneratorCI.class)}
	 * post	{@code owner.isPortExisting(uri)}
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				GeneratorInboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, GeneratorCI.class, owner);

		// Preconditions checking
		assert	owner instanceof GeneratorImplementationI :
				new PreconditionException(
						"owner instanceof GeneratorImplementationI");
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
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).getState());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#nominalOutputTension()
	 */
	@Override
	public Measure<Double>	nominalOutputTension() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).nominalOutputTension());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#tankCapacity()
	 */
	@Override
	public Measure<Double>	tankCapacity() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).tankCapacity());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#currentTankLevel()
	 */
	@Override
	public SignalData<Double>	currentTankLevel() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).currentTankLevel());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#maxPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	maxPowerProductionCapacity() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).maxPowerProductionCapacity());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#currentPowerProduction()
	 */
	@Override
	public SignalData<Double>	currentPowerProduction() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).currentPowerProduction());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#minFuelConsumption()
	 */
	@Override
	public Measure<Double>	minFuelConsumption() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).minFuelConsumption());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#maxFuelConsumption()
	 */
	@Override
	public Measure<Double>	maxFuelConsumption() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).maxFuelConsumption());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#currentFuelConsumption()
	 */
	@Override
	public SignalData<Double>	currentFuelConsumption() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((GeneratorImplementationI)o).currentFuelConsumption());
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#startGenerator()
	 */
	@Override
	public void			startGenerator() throws Exception
	{
		this.getOwner().handleRequest(
				o -> { ((GeneratorImplementationI)o).startGenerator();
					   return null;
					 });
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#stopGenerator()
	 */
	@Override
	public void			stopGenerator() throws Exception
	{
		this.getOwner().handleRequest(
				o -> { ((GeneratorImplementationI)o).stopGenerator();
					   return null;
					 });
	}
}
// -----------------------------------------------------------------------------
