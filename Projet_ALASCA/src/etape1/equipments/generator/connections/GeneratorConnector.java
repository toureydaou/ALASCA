package etape1.equipments.generator.connections;

import etape1.equipments.generator.Generator.State;
import etape1.equipments.generator.GeneratorCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorConnector</code> implements a connector for
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
public class			GeneratorConnector
extends		AbstractConnector
implements	GeneratorCI
{
	/**
	 * @see etape1.equipments.generator.GeneratorCI#getState()
	 */
	@Override
	public State		getState() throws Exception
	{
		return ((GeneratorCI)this.offering).getState();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#nominalOutputTension()
	 */
	@Override
	public Measure<Double>	nominalOutputTension() throws Exception
	{
		return ((GeneratorCI)this.offering).nominalOutputTension();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#tankCapacity()
	 */
	@Override
	public Measure<Double>	tankCapacity() throws Exception
	{
		return ((GeneratorCI)this.offering).tankCapacity();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#currentTankLevel()
	 */
	@Override
	public SignalData<Double>	currentTankLevel() throws Exception
	{
		return ((GeneratorCI)this.offering).currentTankLevel();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#maxPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	maxPowerProductionCapacity() throws Exception
	{
		return ((GeneratorCI)this.offering).maxPowerProductionCapacity();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#currentPowerProduction()
	 */
	@Override
	public SignalData<Double>	currentPowerProduction() throws Exception
	{
		return ((GeneratorCI)this.offering).currentPowerProduction();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#minFuelConsumption()
	 */
	@Override
	public Measure<Double>	minFuelConsumption() throws Exception
	{
		return ((GeneratorCI)this.offering).minFuelConsumption();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#maxFuelConsumption()
	 */
	@Override
	public Measure<Double>	maxFuelConsumption() throws Exception
	{
		return ((GeneratorCI)this.offering).maxFuelConsumption();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#currentFuelConsumption()
	 */
	@Override
	public SignalData<Double>	currentFuelConsumption() throws Exception
	{
		return ((GeneratorCI)this.offering).currentFuelConsumption();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#startGenerator()
	 */
	@Override
	public void			startGenerator() throws Exception
	{
		((GeneratorCI)this.offering).startGenerator();
	}

	/**
	 * @see etape1.equipments.generator.GeneratorCI#stopGenerator()
	 */
	@Override
	public void			stopGenerator() throws Exception
	{
		((GeneratorCI)this.offering).stopGenerator();
	}
}
// -----------------------------------------------------------------------------
