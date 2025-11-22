package etape2.equipments.generator.mil;

import etape1.equipments.generator.Generator.State;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorStateManagementI</code> declares the signatures of
 * methods that a simulation model must implement to manage the state of the
 * generator, typically called by external events when executed on the model.
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
 * <p>Created on : 2025-10-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		GeneratorStateManagementI
{
	/**
	 * return the current state of the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current state of the batteries.
	 */
	public State		getGeneratorState();

	/**
	 * set the new state of the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code newState != null}
	 * post	{@code currentState.equals(newState)}
	 * </pre>
	 *
	 * @param newState	the new state of the batteries.
	 */
	public void			setGeneratorState(State newState);
}
// -----------------------------------------------------------------------------
