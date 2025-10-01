package fr.sorbonne_u.components.hem2025e1.equipments.heater;

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

import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The interface <code>HeaterUserI</code> declares the signature of the heater
 * component services corresponding to the actions a user can perform on the
 * heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		HeaterUserI
extends		HeaterExternalControlI
{
	/**
	 * return true if the heater is currently running.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				true if the heater is currently running.
	 * @throws Exception	<i>to do</i>.
	 */
	public boolean		on() throws Exception;

	/**
	 * switch on the heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !on()}
	 * post	{@code on()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			switchOn() throws Exception;

	/**
	 * switch off the heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * post	{@code !on()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			switchOff() throws Exception;

	/**
	 * set the target temperature.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null && TEMPERATURE_UNIT.equals(target.getMeasurementUnit())}
	 * pre	{@code target.getData() >= MIN_TARGET_TEMPERATURE.getData() && target.getData() <= MAX_TARGET_TEMPERATURE.getData()}
	 * post	{@code getTargetTemperature().equals(target)}
	 * </pre>
	 *
	 * @param target		the new target temperature.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setTargetTemperature(Measure<Double> target)
	throws Exception;
}
// -----------------------------------------------------------------------------
