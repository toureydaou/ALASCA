package fr.sorbonne_u.components.hem2025e1.equipments.solar_panel;

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

import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelImplementationI</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code POWER_UNIT != null}
 * </pre>
 * 
 * <p>Created on : 2025-09-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		SolarPanelImplementationI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** measurement unit for power used in this appliance.					*/
	public static final MeasurementUnit	POWER_UNIT = MeasurementUnit.WATTS;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return			true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				POWER_UNIT != null,
				SolarPanelImplementationI.class,
				"POWER_UNIT != null");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Signature and default methods
	// -------------------------------------------------------------------------

	/**
	 * return the number of square meters in the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return > 0}
	 * </pre>
	 *
	 * @return				the number of square meters in the solar panel.
	 * @throws Exception	<i>to do</i>.
	 */
	public int				getNumberOfSquareMeters() throws Exception;

	/**
	 * return the nominal power production capacity of the solar panels in the
	 * power unit used by the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getData() > 0.0 && return.getMeasurementUnit().equals(POWER_UNIT)}
	 * </pre>
	 *
	 * @return				the nominal power production capacity of the solar panels in the power unit used by the solar panel.
	 * @throws Exception	<i>to do</i>.
	 */
	public Measure<Double>	getNominalPowerProductionCapacity()
	throws Exception;

	/**
	 * return the current power production capacity of the solar panels in the
	 * power unit used by the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.getMeasure().getMeasurementUnit().equals(POWER_UNIT)}
	 * post	{@code return.getMeasure().getData() >= 0.0 && return.getMeasure().getData() <= getNominalPowerProductionCapacity().getData()}	// no postcondition.
	 * </pre>
	 *
	 * @return				the current power production capacity of the solar panels in the power unit used by the solar panel.
	 * @throws Exception	<i>to do</i>.
	 */
	public SignalData<Double>	getCurrentPowerProductionLevel()
	throws Exception;
}
// -----------------------------------------------------------------------------
