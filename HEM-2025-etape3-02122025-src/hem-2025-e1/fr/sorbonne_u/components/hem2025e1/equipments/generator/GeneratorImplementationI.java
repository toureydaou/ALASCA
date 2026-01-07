package fr.sorbonne_u.components.hem2025e1.equipments.generator;

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

import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.components.hem2025e1.equipments.generator.Generator.State;

// -----------------------------------------------------------------------------
/**
 * The interface <code>GeneratorImplementationI</code> defines the signatures
 * of methods to be implemented by an electric generator component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code POWER_UNIT != null}
 * invariant	{@code TENSION_UNIT != null}
 * </pre>
 * 
 * <p>Created on : 2025-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		GeneratorImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** measurement unit for power used in this appliance.					*/
	public static final MeasurementUnit	POWER_UNIT = MeasurementUnit.WATTS;
	/** measurement unit for tension used in this appliance.				*/
	public static final MeasurementUnit	TENSION_UNIT = MeasurementUnit.VOLTS;
	/** capacity measurement unit for the generator.						*/
	public static final MeasurementUnit	CAPACITY_UNIT = MeasurementUnit.LITERS;
	/** fuel consumption measurement unit for the generator.				*/
	public static final MeasurementUnit	CONSUMPTION_UNIT =
												MeasurementUnit.LITERS_PER_HOUR;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				POWER_UNIT != null,
				GeneratorImplementationI.class,
				"POWER_UNIT != null");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION_UNIT != null,
				GeneratorImplementationI.class,
				"TENSION_UNIT != null");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Signatures
	// -------------------------------------------------------------------------

	/**
	 * return the state of the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the state of the generator.
	 * @throws Exception	<i>to do</i>.
	 */
	public State			getState() throws Exception;

	/**
	 * return the output AC tension in the tension unit used by the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(TENSION_UNIT)}
	 * </pre>
	 *
	 * @return				the output AC tension in the tension unit used by the generator.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	nominalOutputTension() throws Exception;

	/**
	 * return the tank capacity in the capacity unit used by the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(CAPACITY_UNIT)}
	 * </pre>
	 *
	 * @return				the current tank level in the tension unit used by the generator.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	tankCapacity() throws Exception;

	/**
	 * return the current tank level in the capacity unit used by the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getMeasure().getData() > 0.0 && return.getMeasure().getMeasurementUnit().equals(CAPACITY_UNIT)}
	 * </pre>
	 *
	 * @return				the current tank level in the tension unit used by the generator.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	currentTankLevel() throws Exception;

	/**
	 * return the maximum power production level in the power unit used by the
	 * generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null && ret.getData() >= 0.0 && ret.getMeasurementUnit().equals(POWER_UNIT)}
	 * </pre>
	 *
	 * @return				the maximum power production level in the power unit used by the generator.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	maxPowerProductionCapacity() throws Exception;

	/**
	 * return the current power production level in the power unit used by the
	 * generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code ret != null && ret.getMeasure().getData() >= 0.0 && ret.getMeasure().getMeasurementUnit().equals(POWER_UNIT)}
	 * post {@code ret.getMeasure().getData() <= maxPowerProductionCapacity().getData()}
	 * </pre>
	 *
	 * @return				the current power production level in the power unit used by the generator.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	currentPowerProduction() throws Exception;

	/**
	 * return the minimal fuel consumption of the generator in the generator
	 * consumption unit.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
	 * post	{@code return.getData() <= maxFuelConsumption().getData()}
	 * </pre>
	 *
	 * @return				the minimal fuel consumption of the generator in the generator consumption unit.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	minFuelConsumption() throws Exception;

	/**
	 * return the maximal fuel consumption of the generator in the generator
	 * consumption unit.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
	 * </pre>
	 *
	 * @return				the maximal fuel consumption of the generator in the generator consumption unit.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	maxFuelConsumption() throws Exception;

	/**
	 * return the current fuel consumption of the generator in the generator
	 * consumption unit.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
	 * post	{@code return.getMeasure().getData() >= minFuelConsumption().getData() && return.getMeasure().getData() <= maxFuelConsumption().getData()}
	 * </pre>
	 *
	 * @return				the current fuel consumption of the generator in the generator consumption unit.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	currentFuelConsumption() throws Exception;

	/**
	 * start the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code State.OFF.equals(getState())}
	 * post	{@code State.IDLE.equals(getState())}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			startGenerator() throws Exception;

	/**
	 * stop the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !State.OFF.equals(getState())}
	 * post	{@code State.OFF.equals(getState())}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			stopGenerator() throws Exception;
}
// -----------------------------------------------------------------------------
