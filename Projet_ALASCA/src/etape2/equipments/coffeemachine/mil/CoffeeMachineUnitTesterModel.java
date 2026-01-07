package etape2.equipments.coffeemachine.mil;

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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.ServeCoffee;
import etape2.equipments.coffeemachine.mil.events.SetEcoModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetMaxModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetNormalModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetPowerCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicModel;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;


// -----------------------------------------------------------------------------
/**
 * The class <code>CoffeeMachineUnitTesterModel</code> defines a model that is
 * used to test the models defining the heater simulator.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <ul>
 * <li>Imported events: none</li>
 * <li>Exported events: {@code SwitchOnCoffeeMachine},
 * {@code SwitchOffCoffeeMachine}, {@code SetPowerCoffeeMachine}, {@code Heat},
 * {@code DoNotHeat}</li>
 * </ul>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * step >= 0
 * }
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * URI != null && !URI.isEmpty()
 * }
 * </pre>
 * 
 * <p>
 * Created on : 2023-09-29
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(exported = { SwitchOnCoffeeMachine.class, SwitchOffCoffeeMachine.class, 
		SetEcoModeCoffeeMachine.class, SetMaxModeCoffeeMachine.class, SetNormalModeCoffeeMachine.class, SetSuspendedModeCoffeeMachine.class,
		FillWaterCoffeeMachine.class, MakeCoffee.class, ServeCoffee.class, DoNotHeat.class, SetPowerCoffeeMachine.class })
// -----------------------------------------------------------------------------
public class CoffeeMachineUnitTesterModel extends AbstractTestScenarioBasedAtomicModel {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created. */
	public static final String URI = CoffeeMachineUnitTesterModel.class.getSimpleName();
	/** when true, leaves a trace of the execution of the model. */
	public static boolean VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model. */
	public static boolean DEBUG = false;
	/** name of the run parameter for the test scenario to be executed. */
	public static final String TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>CoffeeMachineUnitTesterModel</code> instance.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * uri == null || !uri.isEmpty()
	 * }
	 * pre	{@code
	 * simulatedTimeUnit != null
	 * }
	 * pre	{@code
	 * simulationEngine != null && !simulationEngine.isModelSet()
	 * }
	 * pre	{@code
	 * simulationEngine instanceof AtomicEngine
	 * }
	 * post	{@code
	 * !isDebugModeOn()
	 * }
	 * post	{@code
	 * getURI() != null && !getURI().isEmpty()
	 * }
	 * post	{@code
	 * uri == null || getURI().equals(uri)
	 * }
	 * post	{@code
	 * getSimulatedTimeUnit().equals(simulatedTimeUnit)
	 * }
	 * post	{@code
	 * getSimulationEngine().equals(simulationEngine)
	 * }
	 * </pre>
	 *
	 * @param uri               URI of the model.
	 * @param simulatedTimeUnit time unit used for the simulation time.
	 * @param simulationEngine  simulation engine to which the model is attached.
	 * @throws Exception <i>to do</i>.
	 */
	public CoffeeMachineUnitTesterModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		String testScenarioName = ModelI.createRunParameterName(this.getURI(), TEST_SCENARIO_RP_NAME);

		// Preconditions checking
		assert simParams != null : new MissingRunParameterException("simParams != null");
		assert simParams.containsKey(testScenarioName) : new MissingRunParameterException(testScenarioName);

		this.setTestScenario((TestScenarioWithSimulation) simParams.get(testScenarioName));
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI getFinalReport() {
		return null;
	}
}
// -----------------------------------------------------------------------------
