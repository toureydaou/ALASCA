package etape4.equipments.solar_panel.sil;

import java.util.Map;

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

import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape4.equipments.solar_panel.sil.events.PowerProductionLevel;
import etape4.equipments.solar_panel.sil.events.PowerProductionLevel.PowerLevel;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelStateSILModel</code> simulates the solar panel
 * by keeping track of the current power production level computed by the
 * {@code SolarPanelPowerSILModel}.
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
 * <p>Created on : 2026-01-05</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@ModelExternalEvents(imported = {PowerProductionLevel.class})
// -----------------------------------------------------------------------------
public class			SolarPanelStateSILModel
extends		AtomicModel
implements	ModelStateAccessI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** single model URI.													*/
	public static final String	URI =
								SolarPanelStateSILModel.class.getSimpleName();
	/** name of the value to be retrieved when following the state access
	 *  protocol defined by {@code ModelStateAccessI}.						*/
	public static final String	POWER_LEVEL_NAME = "power-level";
	/** most recent value of power production level.						*/
	protected PowerLevel		powerLevel;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic model with the given URI (if null, one will be
	 * generated) and to be run by the given simulator using the given time unit
	 * for its clock.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine != null && !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine instanceof AtomicEngine}
	 * post	{@code !isDebugModeOn()}
	 * post	{@code getURI() != null && !getURI().isEmpty()}
	 * post	{@code uri == null || getURI().equals(uri)}
	 * post	{@code getSimulatedTimeUnit().equals(simulatedTimeUnit)}
	 * post	{@code getSimulationEngine().equals(simulationEngine)}
	 * </pre>
	 *
	 * @param uri				unique identifier of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation clock.
	 * @param simulationEngine	simulation engine enacting the model.
	 */
	public				SolarPanelStateSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		// this gets the reference on the owner component which is required
		// to have simulation models able to make the component perform some
		// operations or tasks or to get the value of variables held by the
		// component when necessary.
		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			// by the following, all of the logging will appear in the owner
			// component logger
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}

		if (DEBUG) {
			this.logMessage("setSimulationRunParameters");
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.powerLevel = new PowerLevel(0.0, initialTime);
	}

	/**
	 * set the most recent value of power level.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code powerLevel != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param powerLevel	the most recent value of power level.
	 */
	public void			setNewPowerLevel(PowerLevel powerLevel)
	{
		assert	powerLevel != null :
				new NeoSim4JavaException(
						"Precondition violation: powerLevel != null");

		synchronized (powerLevel) {
			this.powerLevel = powerLevel;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI#getModelStateValue(java.lang.String, java.lang.String)
	 */
	@Override
	public PowerLevel	getModelStateValue(
		String modelURI,
		String name
		) throws Exception
	{
		synchronized (this.powerLevel) {
			// Preconditions checking
			assert	this.uri.equals(modelURI) :
					new NeoSim4JavaException(
						"Precondition violation: getURI().equals(modelURI)");
			assert	POWER_LEVEL_NAME.equals(name) :
					new NeoSim4JavaException(
						"Precondition violation: POWER_LEVEL_NAME.equals(name)");
			return this.powerLevel.clone();
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		return Duration.INFINITY;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{		
		super.userDefinedExternalTransition(elapsedTime);

		assert	this.currentStoredEvents.size() == 1 :
				new NeoSim4JavaException("currentStoredEvents.size() == 1");

		EventI e = this.getStoredEventAndReset().remove(0);
		e.executeOn(this);

		if (DEBUG || VERBOSE) {
			this.logMessage("userDefinedInternalTransition power level = "
					+ this.powerLevel + " "
					+ ElectricMeterImplementationI.POWER_UNIT
					+ " at "
					+ this.getCurrentStateTime());
		}
	}
}
// -----------------------------------------------------------------------------
