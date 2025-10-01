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
import fr.sorbonne_u.components.hem2025e1.equipments.heater.Heater;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserCI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterUserJava4InboundPort</code> implements an inbound port
 * for the {@code HeaterUserJava4CI} component interface.
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
 * invariant	{@code getOwner() instanceof HeaterUserI}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterUserJava4InboundPort
extends		HeaterUserInboundPort
implements	HeaterUserJava4CI
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
	 * pre	{@code owner instanceof HeaterUserI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterUserJava4InboundPort(ComponentI owner) throws Exception
	{
		super(HeaterUserCI.class, owner);
		assert	owner instanceof HeaterUserI :
				new PreconditionException("owner instanceof HeaterUserI");
	}

	/**
	 * create an inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * pre	{@code owner instanceof HeaterUserI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterUserJava4InboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, HeaterUserCI.class, owner);
		assert	owner instanceof HeaterUserI :
				new PreconditionException("owner instanceof HeaterUserI");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI#setTargetTemperatureJava4(double)
	 */
	@Override
	public void			setTargetTemperatureJava4(double target)
	throws Exception
	{
		this.setTargetTemperature(
				new Measure<Double>(target, Heater.TEMPERATURE_UNIT));
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI#getMaxPowerLevelJava4()
	 */
	@Override
	public double		getMaxPowerLevelJava4() throws Exception
	{
		return this.getMaxPowerLevel().getData();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI#setCurrentPowerLevelJava4(double)
	 */
	@Override
	public void			setCurrentPowerLevelJava4(double powerLevel)
	throws Exception
	{
		this.setCurrentPowerLevel(
				new Measure<Double>(powerLevel, Heater.POWER_UNIT));
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI#getCurrentPowerLevelJava4()
	 */
	@Override
	public double		getCurrentPowerLevelJava4() throws Exception
	{
		return this.getCurrentPowerLevel().getMeasure().getData();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI#getTargetTemperatureJava4()
	 */
	@Override
	public double		getTargetTemperatureJava4() throws Exception
	{
		
		return this.getTargetTemperature().getData();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI#getCurrentTemperatureJava4()
	 */
	@Override
	public double		getCurrentTemperatureJava4() throws Exception
	{
		
		return this.getCurrentTemperature().getMeasure().getData();
	}
}
// -----------------------------------------------------------------------------
