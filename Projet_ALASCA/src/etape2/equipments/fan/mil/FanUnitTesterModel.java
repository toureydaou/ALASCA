package etape2.equipments.fan.mil;

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

import etape2.equipments.fan.mil.events.SetHighModeFan;
import etape2.equipments.fan.mil.events.SetLowModeFan;
import etape2.equipments.fan.mil.events.SetMediumModeFan;
import etape2.equipments.fan.mil.events.SwitchOffFan;
import etape2.equipments.fan.mil.events.SwitchOnFan;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import tests_utils.AbstractTestScenarioBasedAtomicModel;
import tests_utils.TestScenario;

// -----------------------------------------------------------------------------
/**
 * The class <code>FanUnitTesterModel</code> implements a unit tester simulation
 * model for the fan which runs test scenarios.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <ul>
 * <li>Imported events: none</li>
 * <li>Exported events: {@code SwitchOnFan}, {@code SwitchOffFan},
 * {@code SetLowFan}, {@code SetHighFan}</li>
 * </ul>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * true
 * }	// no more invariant
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * true
 * }	// no more invariant
 * </pre>
 * 
 * <p>
 * Created on : 2025-10-28
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(exported = { SwitchOnFan.class, SwitchOffFan.class, SetLowModeFan.class, SetMediumModeFan.class,
		SetHighModeFan.class })
//-----------------------------------------------------------------------------
public class FanUnitTesterModel extends AbstractTestScenarioBasedAtomicModel {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model. */
	public static final boolean VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model. */
	public static final boolean DEBUG = false;

	/** single model URI. */
	public static final String URI = "fan-unit-tester-model";
	/** name of the run parameter for the test scenario to be executed. */
	public static final String TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic model with the given URI (if null, one will be generated)
	 * and to be run by the given simulator using the given time unit for its clock.
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
	 * @param uri               unique identifier of the model.
	 * @param simulatedTimeUnit time unit used for the simulation clock.
	 * @param simulationEngine  simulation engine enacting the model.
	 */
	public FanUnitTesterModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine) {
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert FanUnitTesterModel.implementationInvariants(this) : new NeoSim4JavaException(
				"Implementation Invariants violation: " + "FanUnitTesterModel." + "implementationInvariants(this)");
		assert FanUnitTesterModel.invariants(this)
				: new NeoSim4JavaException("Invariants violation: FanUnitTesterModel." + "invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
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

		this.setTestScenario((TestScenario) simParams.get(testScenarioName));
	}
}
// -----------------------------------------------------------------------------
