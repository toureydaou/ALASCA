package etape1.equipements.laundry.connections.ports;

import etape1.equipements.fan.FanImplementationI.FanMode;
import etape1.equipements.fan.FanImplementationI.FanState;
import etape1.equipements.laundry.interfaces.LaundryUserCI;

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
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryUserOutboundPort</code> implements an outbound port for
 * the <code>LaundryUserCI</code> component interface.
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
 * 
 * <p>Created on : 2021-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			LaundryUserOutboundPort
extends		AbstractOutboundPort
implements	LaundryUserCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code owner != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				LaundryUserOutboundPort(ComponentI owner)
	throws Exception
	{
		super(LaundryUserCI.class, owner);
	}

	/**
	 * create an outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri != null && !uri.isEmpty()}
	 * pre	{@code owner != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public				LaundryUserOutboundPort(String uri, ComponentI owner)
	throws Exception
	{
		super(uri, LaundryUserCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#getState()
	 */
	@Override
	public LaundryState	getState() throws Exception
	{
		return ((LaundryUserCI)this.getConnector()).getState();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#getMode()
	 */
	@Override
	public LaundryMode	getLaundryMode() throws Exception
	{
		return ((LaundryUserCI)this.getConnector()).getLaundryMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		((LaundryUserCI)this.getConnector()).turnOn();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		((LaundryUserCI)this.getConnector()).turnOff();
	}

	@Override
	public LaundryWashModes getLaundryWashMode() throws Exception {
		
		return ((LaundryUserCI)this.getConnector()).getLaundryWashMode();
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return ((LaundryUserCI)this.getConnector()).getCurrentTemperature();
	}

	@Override
	public void setDryMode() throws Exception {
		 ((LaundryUserCI)this.getConnector()).setDryMode();
		
	}

	@Override
	public void setWashMode() throws Exception {
		((LaundryUserCI)this.getConnector()).setWashMode();
		
	}

	@Override
	public void setLaundryWashModeWhite() throws Exception {
		((LaundryUserCI)this.getConnector()).setLaundryWashModeWhite();
		
	}

	@Override
	public void setLaundryWashModeColor() throws Exception {
		((LaundryUserCI)this.getConnector()).setLaundryWashModeColor();
		
	}

	@Override
	public void setTemperature(WashTemperatures temp) throws Exception {
		((LaundryUserCI)this.getConnector()).setTemperature(temp);
		
	}

	
}
// -----------------------------------------------------------------------------
