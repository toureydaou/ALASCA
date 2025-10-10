package etape1.equipements.laundry.connections.connectors;

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

import fr.sorbonne_u.components.connectors.AbstractConnector;
import physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryUserConnector</code> implements a connector for the
 * <code>LaundryUserCI</code> component interface.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * true
 * }	// no more invariant
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * true
 * }	// no more invariant
 * </pre>
 * 
 * <p>
 * Created on : 2023-09-19
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class LaundryUserConnector extends AbstractConnector implements LaundryUserCI {
	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#getState()
	 */
	@Override
	public LaundryState getState() throws Exception {
		return ((LaundryUserCI) this.offering).getState();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#getMode()
	 */
	@Override
	public LaundryMode getLaundryMode() throws Exception {
		return ((LaundryUserCI) this.offering).getLaundryMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#turnOn()
	 */
	@Override
	public void turnOn() throws Exception {
		((LaundryUserCI) this.offering).turnOn();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserCI.equipments.Fan.FanUserCI#turnOff()
	 */
	@Override
	public void turnOff() throws Exception {
		((LaundryUserCI) this.offering).turnOff();
	}

	@Override
	public LaundryWashModes getLaundryWashMode() throws Exception {

		return ((LaundryUserCI) this.offering).getLaundryWashMode();
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return ((LaundryUserCI) this.offering).getCurrentTemperature();
	}

	@Override
	public void setDryMode() throws Exception {
		((LaundryUserCI) this.offering).setDryMode();

	}

	@Override
	public void setWashMode() throws Exception {
		((LaundryUserCI) this.offering).setWashMode();

	}

	@Override
	public void setLaundryWashModeWhite() throws Exception {
		((LaundryUserCI) this.offering).setLaundryWashModeWhite();

	}

	@Override
	public void setLaundryWashModeColor() throws Exception {
		((LaundryUserCI) this.offering).setLaundryWashModeColor();

	}

	@Override
	public void setTemperature(WashTemperatures temp) throws Exception {
		((LaundryUserCI) this.offering).setTemperature(temp);

	}
}
// -----------------------------------------------------------------------------
