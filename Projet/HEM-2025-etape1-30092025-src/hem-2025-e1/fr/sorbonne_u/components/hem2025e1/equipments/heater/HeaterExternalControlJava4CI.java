package fr.sorbonne_u.components.hem2025e1.equipments.heater;

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

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterExternalControlJava4CI</code> extends the component
 * interface {@code HeaterExternalControlCI} with signatures that can be used
 * in Java 4.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		HeaterExternalControlJava4CI
extends		HeaterExternalControlCI
{

	/**
	 * get the maximum power level by calling the synonymous method
	 * {@code Measure<Double>	getMaxPowerLevel()}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0.0}
	 * </pre>
	 *
	 * @return				the maximum power level.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getMaxPowerLevelJava4() throws Exception;

	/**
	 * set the current power level by calling the synonymous method
	 * {@code setCurrentPowerLevel(Measure<Double> powerLevel)};
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code powerLevel >= 0.0}
	 * post	{@code powerLevel > getMaxPowerLevelJava4() || getCurrentPowerLevelJava4() == powerLevel}
	 * post	{@code powerLevel <= getMaxPowerLevelJava4() || getCurrentPowerLevelJava4() == Heater.MAX_POWER_LEVEL.getData()}
	 * </pre>
	 *
	 * @param powerLevel	new power level in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			setCurrentPowerLevelJava4(double powerLevel)
	throws Exception;

	/**
	 * get the current power level by calling the synonymous method
	 * {@code SignalData<Double> getCurrentPowerLevel()}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return >= 0.0 && return <= getMaxPowerLevelJava4()}
	 * </pre>
	 *
	 * @return				the current power level in watts.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getCurrentPowerLevelJava4() throws Exception;

	/**
	 * get the target temperature by calling the synonymous method
	 * {@code Measure<Double> getTargetTemperature()}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return >= Heater.MIN_TARGET_TEMPERATURE.getData() && return <= Heater.MAX_TARGET_TEMPERATURE.getData()}
	 * </pre>
	 *
	 * @return				the target temperature in celsius.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getTargetTemperatureJava4() throws Exception ;

	/**
	 * get the current temperature by calling the synonymous method
	 * {@code SignalData<Double> getCurrentTemperature()}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the current temperature in celsius.
	 * @throws Exception	<i>to do</i>.
	 */
	public double		getCurrentTemperatureJava4() throws Exception;
}
// -----------------------------------------------------------------------------
