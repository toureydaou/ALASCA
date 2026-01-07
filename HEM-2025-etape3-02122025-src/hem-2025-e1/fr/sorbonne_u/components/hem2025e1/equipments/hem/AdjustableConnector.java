package fr.sorbonne_u.components.hem2025e1.equipments.hem;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
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
import fr.sorbonne_u.components.hem2025.bases.AdjustableCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>AdjustableConnector</code> is a plain standard BCM4Java
 * connector assuming that the inbound port offers the same component
 * interface <code>AdjustableCI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This class is used only for debugging purposes and not used for the project
 * in its correctly running state.
 * </p>
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
 * <p>Created on : 2024-09-09</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AdjustableConnector
extends		AbstractConnector
implements	AdjustableCI
{
	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#maxMode()
	 */
	@Override
	public int			maxMode() throws Exception
	{
		return ((AdjustableCI)this.offering).maxMode();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#upMode()
	 */
	@Override
	public boolean		upMode() throws Exception
	{
		return ((AdjustableCI)this.offering).upMode();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#downMode()
	 */
	@Override
	public boolean		downMode() throws Exception
	{
		return ((AdjustableCI)this.offering).downMode();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#setMode(int)
	 */
	@Override
	public boolean		setMode(int modeIndex) throws Exception
	{
		return ((AdjustableCI)this.offering).setMode(modeIndex);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#currentMode()
	 */
	@Override
	public int			currentMode() throws Exception
	{
		return ((AdjustableCI)this.offering).currentMode();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#getModeConsumption(int)
	 */
	@Override
	public double		getModeConsumption(int modeIndex) throws Exception
	{
		return ((AdjustableCI)this.offering).getModeConsumption(modeIndex);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#suspended()
	 */
	@Override
	public boolean		suspended() throws Exception
	{
		return ((AdjustableCI)this.offering).suspended();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#suspend()
	 */
	@Override
	public boolean		suspend() throws Exception
	{
		return ((AdjustableCI)this.offering).suspend();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#resume()
	 */
	@Override
	public boolean		resume() throws Exception
	{
		return ((AdjustableCI)this.offering).resume();
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#emergency()
	 */
	@Override
	public double		emergency() throws Exception
	{
		return ((AdjustableCI)this.offering).emergency();
	}
}
// -----------------------------------------------------------------------------
