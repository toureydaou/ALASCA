package fr.sorbonne_u.components.hem2025e1.equipments.batteries.connections;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI;
import fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesImplementationI;
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
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI#nominalCapacity()
	 */
	@Override
	public Measure<Double>	nominalCapacity() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).nominalCapacity());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI#currentCapacity()
	 */
	@Override
	public SignalData<Double>	currentCapacity() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).currentCapacity());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI#areCharging()
	 */
	@Override
	public boolean		areCharging() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).areCharging());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI#areDischarging()
	 */
	@Override
	public boolean		areDischarging() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).areDischarging());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI#chargeLevel()
	 */
	@Override
	public SignalData<Double>	chargeLevel() throws Exception
	{
		return this.getOwner().handleRequest(
					o -> ((BatteriesImplementationI)o).chargeLevel());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesImplementationI#getCurrentPowerConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerConsumption() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((BatteriesImplementationI)o).getCurrentPowerConsumption());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI#startCharging()
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
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.batteries.BatteriesCI#stopCharging()
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
