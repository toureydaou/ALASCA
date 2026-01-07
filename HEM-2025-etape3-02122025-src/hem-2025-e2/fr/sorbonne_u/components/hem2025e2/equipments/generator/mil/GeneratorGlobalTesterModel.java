package fr.sorbonne_u.components.hem2025e2.equipments.generator.mil;

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

import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicModel;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Refill;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Start;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Stop;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.TankEmpty;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.TankNoLongerEmpty;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorGlobalTesterModel</code>implements a model that
 * executes global test scenarios for the generator.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This atomic HIOA simulation model is defined as solely executing test
 * scenarios (@see fr.sorbonne_u.components.hem2025.tests_utils.AbstractTestScenarioBasedAtomicHIOA)
 * for more explanations.
 * </p>
 * <p>
 * Compared to {@code GeneratorUnitTesterModel}, this model only imports and
 * exports the external events not related to the continuous variables of
 * the generator models. In the global simulator, these continuous variables
 * are shared by the {@code ElectricMeterElectricityModel} instead.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code TankEmpty},
 *   {@code TankNoLongerEmpty}</li>
 * <li>Exported events:
 *   {@code Start},
 *   {@code Stop},
 *   {@code Refill}</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables: none</li>
 * </ul>
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
 * <p>Created on : 2025-11-03</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
		exported = {Start.class, Stop.class, Refill.class},
		imported = {TankEmpty.class, TankNoLongerEmpty.class})
//-----------------------------------------------------------------------------
public class			GeneratorGlobalTesterModel
extends		AbstractTestScenarioBasedAtomicModel
implements	TankLevelManagementI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean			VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean			DEBUG = false;

	/** single model URI.													*/
	public static final String		URI = "generator-global-tester-model";
	/**	name of the run parameter for the test scenario to be executed.		*/
	public static final String		TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";

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
	public				GeneratorGlobalTesterModel(
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
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		String testScenarioName = ModelI.createRunParameterName(this.getURI(),
														TEST_SCENARIO_RP_NAME);

		// Preconditions checking
		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(testScenarioName) :
				new MissingRunParameterException(testScenarioName);

		this.setTestScenario((TestScenarioWithSimulation)
											simParams.get(testScenarioName));
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#notTankEmpty()
	 */
	@Override
	public boolean		notTankEmpty()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#signalTankEmpty()
	 */
	@Override
	public void			signalTankEmpty()
	{
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.TankLevelManagementI#signalTankNoLongerEmpty()
	 */
	@Override
	public void			signalTankNoLongerEmpty()
	{
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// tracing
		if (VERBOSE) {
			this.logMessage("userDefinedInternalTransition at "
							+ this.getCurrentStateTime());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void		userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		EventI e = this.getStoredEventAndReset().remove(0);

		// execute the event, which will change the state of the batteries
		e.executeOn(this);

		// tracing
		if (VERBOSE) {
			this.logMessage("userDefinedExternalTransition at "
							+ this.getCurrentStateTime() + " on event " + e);
		}
	}
}
// -----------------------------------------------------------------------------
