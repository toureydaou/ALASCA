package etape3.equipements.coffee_machine.sil;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;

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

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape2.equipments.coffeemachine.mil.CoffeeMachineOperationI;
import fr.sorbonne_u.devs_simulation.models.time.Time;

// -----------------------------------------------------------------------------
/**
 * The interface <code>SIL_CoffeeMachineOperationI</code> defines the common
 * operations used by events to execute on the coffee machine SIL models.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface extends the basic coffee machine operations to support
 * Software-In-the-Loop simulation. It adds methods to set the current heating
 * power and water level at specific simulation times.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 *
 * <p>Created on : 2025-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		SIL_CoffeeMachineOperationI extends CoffeeMachineOperationI
{
	

	/**
	 * set the current heating power of the coffee machine to {@code newPower}.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code newPower >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newPower	the new power in watts to be set on the coffee machine.
	 * @param t			time at which the new power is set.
	 */
	public void			setCurrentHeatingPower(double newPower, Time t);

	/**
	 * set the current water level in the coffee machine to {@code newLevel}.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code newLevel >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newLevel	the new water level in liters.
	 * @param t			time at which the new level is set.
	 */
	public void			setCurrentWaterLevel(double newLevel, Time t);
}
// -----------------------------------------------------------------------------
