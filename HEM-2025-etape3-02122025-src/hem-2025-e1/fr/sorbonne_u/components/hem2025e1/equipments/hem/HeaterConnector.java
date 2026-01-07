package fr.sorbonne_u.components.hem2025e1.equipments.hem;

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
import fr.sorbonne_u.components.hem2025.bases.AdjustableCI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlJava4CI;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterConnector</code> *manually* implements a connector
 * bridging the gap between the given generic component interface
 * {@code AdjustableCI} and the actual component interface offered by the
 * thermostated heater component {@code HeaterCI}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The code given here illustrates how a connector can be used to implement
 * a required interface given some offered interface that is different.
 * The objective is to be able to automatically generate such a connector
 * at run-time from an XML descriptor of the required adjustments.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code currentMode >= 0 && currentMode <= MAX_MODE}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MAX_MODE > 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterConnector
extends		AbstractConnector
implements	AdjustableCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** modes will be defined by five power levels, including a power
	 *  level of 0.0 watts; note that modes go from 1 (0.0 watts) to
	 *  6 (2000.0 watts).													*/
	public static final int		MAX_MODE = 6;
	/** the minimum admissible temperature from which the heater should
	 *  be resumed in priority after being suspended to save energy.		*/
	public static final double	MIN_ADMISSIBLE_TEMP = 12.0;
	/** the maximal admissible difference between the target and the
	 *  current temperature from which the heater should be resumed in
	 *  priority after being suspended to save energy.						*/
	public static final double	MAX_ADMISSIBLE_DELTA = 10.0;

	/** the current mode of the heater.										*/
	protected int		currentMode;
	/** true if the heater has been suspended, false otherwise.				*/
	protected boolean	isSuspended;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an instance of connector.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code !suspended}
	 * post	{@code currentMode() == MAX_MODE}
	 * </pre>
	 *
	 */
	public				HeaterConnector()
	{
		super();
		this.currentMode = MAX_MODE;
		this.isSuspended = false;
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * compute and return the power level associated with the {@code mode}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code mode > 0 && newMode <= MAX_MODE}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param mode			a mode for the heater.
	 * @return				the power consumption for {@code newMode} in amperes.
	 * @throws Exception	<i>to do</i>.
	 */
	protected double	computePowerLevel(int mode) throws Exception
	{
		assert	mode > 0 || mode <= MAX_MODE :
				new PreconditionException("mode > 0 || mode <= MAX_MODE");

		double maxPowerLevel =
				((HeaterExternalControlJava4CI)this.offering).
													getMaxPowerLevelJava4();
		return (mode - 1) * maxPowerLevel/(MAX_MODE - 1);
	}

	/**
	 * set the heater at this power level.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code newPowerLevel >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newPowerLevel	a new power level to be set on the heater.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		setNewPowerLevel(double newPowerLevel) throws Exception
	{
		assert	newPowerLevel >= 0.0 :
				new PreconditionException("newPowerLevel >= 0.0");

		double maxPowerLevel =
				((HeaterExternalControlJava4CI)this.offering).
													getMaxPowerLevelJava4();
		
		if (newPowerLevel > maxPowerLevel) {
			newPowerLevel = maxPowerLevel;
		}
		((HeaterExternalControlJava4CI)this.offering).
									setCurrentPowerLevelJava4(newPowerLevel);
	}

	/**
	 * compute and set the power level associated with the {@code newMode}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code newMode >= 0 && newMode <= MAX_MODE}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param newMode		a new mode for the heater.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		computeAndSetNewPowerLevel(int newMode) throws Exception
	{
		double newPowerLevel = this.computePowerLevel(newMode);
		this.setNewPowerLevel(newPowerLevel);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#maxMode()
	 */
	@Override
	public int			maxMode() throws Exception
	{
		return MAX_MODE;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#upMode()
	 */
	@Override
	public boolean		upMode() throws Exception
	{
		assert	!this.suspended() : new PreconditionException("!suspended()");
		assert	this.currentMode() < MAX_MODE :
				new PreconditionException("currentMode() < MAX_MODE");

		try {
			this.computeAndSetNewPowerLevel(this.currentMode + 1);
			this.currentMode++;
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#downMode()
	 */
	@Override
	public boolean		downMode() throws Exception
	{
		assert	!this.suspended() : new PreconditionException("!suspended()");
		assert	this.currentMode() > 0 :
				new PreconditionException("currentMode() > 0");

		try {
			this.computeAndSetNewPowerLevel(this.currentMode - 1);
			this.currentMode--;
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#setMode(int)
	 */
	@Override
	public boolean		setMode(int modeIndex) throws Exception
	{
		assert	!this.suspended() : new PreconditionException("!suspended()");
		assert	modeIndex > 0 && modeIndex <= this.maxMode() :
				new PreconditionException(
						"modeIndex > 0 && modeIndex <= maxMode()");

		try {
			this.computeAndSetNewPowerLevel(modeIndex);
			this.currentMode = modeIndex;
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#currentMode()
	 */
	@Override
	public int			currentMode() throws Exception
	{
		assert	!suspended() : new PreconditionException("!suspended()");

		return this.currentMode;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#getModeConsumption(int)
	 */
	@Override
	public double		getModeConsumption(int modeIndex) throws Exception
	{
		assert	modeIndex > 0 && modeIndex <= this.maxMode() :
				new PreconditionException(
						"modeIndex > 0 && modeIndex <= maxMode()");

		return this.computePowerLevel(modeIndex);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#suspended()
	 */
	@Override
	public boolean		suspended() throws Exception
	{
		return this.isSuspended;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#suspend()
	 */
	@Override
	public boolean		suspend() throws Exception
	{
		assert	!this.suspended() : new PreconditionException("!suspended()");

		try {
			((HeaterExternalControlJava4CI)this.offering).
												setCurrentPowerLevelJava4(0.0);
			this.isSuspended = true;
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#resume()
	 */
	@Override
	public boolean		resume() throws Exception
	{
		assert	this.suspended() : new PreconditionException("suspended()");

		try {
			this.computeAndSetNewPowerLevel(this.currentMode);
			this.isSuspended = false;
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025.bases.AdjustableCI#emergency()
	 */
	@Override
	public double		emergency() throws Exception
	{
		assert	this.suspended() : new PreconditionException("suspended()");

		double currentTemperature =
					((HeaterExternalControlJava4CI)this.offering).
												getCurrentTemperatureJava4();
		double targetTemperature =
					((HeaterExternalControlJava4CI)this.offering).
												getTargetTemperatureJava4();
		double delta = Math.abs(targetTemperature - currentTemperature);
		double ret = -1.0;
		if (currentTemperature < HeaterConnector.MIN_ADMISSIBLE_TEMP ||
							delta >= HeaterConnector.MAX_ADMISSIBLE_DELTA) {
			ret = 1.0;
		} else {
			ret = delta/HeaterConnector.MAX_ADMISSIBLE_DELTA;
		}

		assert	ret >= 0.0 && ret <= 1.0 :
				new PostconditionException("return >= 0.0 && return <= 1.0");

		return ret;
	}
}
// -----------------------------------------------------------------------------
