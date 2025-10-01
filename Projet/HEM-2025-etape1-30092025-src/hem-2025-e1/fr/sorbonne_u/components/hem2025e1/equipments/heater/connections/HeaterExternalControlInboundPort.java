package fr.sorbonne_u.components.hem2025e1.equipments.heater.connections;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlCI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterExternalControlInboundPort</code> implements the
 * inbound port for the {@code HeaterExternalControlCI} component interface.
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
 * invariant	{@code getOwner() instanceof HeaterExternalControlI}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterExternalControlInboundPort
extends		AbstractInboundPort
implements	HeaterExternalControlCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof HeaterExternalControlI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterExternalControlInboundPort(ComponentI owner)
	throws Exception
	{
		this(HeaterExternalControlCI.class, owner);
	}

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof HeaterExternalControlI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterExternalControlInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		this(uri, HeaterExternalControlCI.class, owner);
	}

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code implementedInterface != null && HeaterExternalControlCI.class.isAssignableFrom(implementedInterface)}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof HeaterExternalControlI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				HeaterExternalControlInboundPort(
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(implementedInterface, owner);

		assert	implementedInterface != null &&
						HeaterExternalControlCI.class.isAssignableFrom(
														implementedInterface)  :
				new PreconditionException(
						"implementedInterface != null && "
						+ "HeaterExternalControlCI.class.isAssignableFrom("
						+ "implementedInterface)");
		assert	owner instanceof HeaterExternalControlI :
				new PreconditionException(
						"owner instanceof HeaterExternalControlI");
	}

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code implementedInterface != null && HeaterExternalControlCI.class.isAssignableFrom(implementedInterface)}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof HeaterExternalControlI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param implementedInterface	interface implemented by this port.
	 * @param owner					component that owns this port.
	 * @throws Exception			<i>to do</i>.
	 */
	public				HeaterExternalControlInboundPort(
		String uri,
		Class<? extends OfferedCI> implementedInterface,
		ComponentI owner
		) throws Exception
	{
		super(uri, implementedInterface, owner);

		assert	implementedInterface != null &&
						HeaterExternalControlCI.class.isAssignableFrom(
														implementedInterface)  :
				new PreconditionException(
						"implementedInterface != null && "
						+ "HeaterExternalControlCI.class.isAssignableFrom("
						+ "implementedInterface)");
		assert	owner instanceof HeaterExternalControlI :
				new PreconditionException(
						"owner instanceof HeaterExternalControlI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlCI#getMaxPowerLevel()
	 */
	@Override
	public Measure<Double>	getMaxPowerLevel() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((HeaterExternalControlI)o).getMaxPowerLevel());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlCI#setCurrentPowerLevel(fr.sorbonne_u.alasca.physical_data.Measure)
	 */
	@Override
	public void			setCurrentPowerLevel(Measure<Double> powerLevel)
	throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((HeaterExternalControlI)o).
											setCurrentPowerLevel(powerLevel);
						return null;
					 });
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlCI#getCurrentPowerLevel()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerLevel() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((HeaterExternalControlI)o).getCurrentPowerLevel());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlCI#getTargetTemperature()
	 */
	@Override
	public Measure<Double>	getTargetTemperature() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((HeaterTemperatureI)o).getTargetTemperature());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlCI#getCurrentTemperature()
	 */
	@Override
	public SignalData<Double>	getCurrentTemperature() throws Exception
	{
 		return this.getOwner().handleRequest(
 				o -> ((HeaterTemperatureI)o).getCurrentTemperature());
	}
}
// -----------------------------------------------------------------------------
