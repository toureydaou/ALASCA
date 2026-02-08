package etape4.equipments.batteries.sil;

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
import java.util.ArrayList;
import java.util.Map;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicHIOA;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import etape2.equipments.batteries.mil.BatteriesUserI;
import etape2.equipments.batteries.mil.events.BatteriesAvailable;
import etape2.equipments.batteries.mil.events.BatteriesEmpty;
import etape4.equipments.batteries.sil.events.SIL_BatteriesRequiredPowerChanged;
import etape4.equipments.batteries.sil.events.SIL_StartCharging;
import etape4.equipments.batteries.sil.events.SIL_StopCharging;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariables;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesUnitTesterSILModel</code> implements a model that
 * executes test scenarios for the batteries.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This atomic HIOA simulation model is defined as solely executing test
 * scenarios (@see fr.sorbonne_u.components.hem2025.tests_utils.AbstractTestScenarioBasedAtomicHIOA)
 * for more explanations.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code BatteriesEmpty},
 *   {@code BatteriesAvailable}
 *   </li>
 * <li>Exported events:
 *   {@code StartCharging},
 *   {@code StopCharging},
 *   {@code SIL_BatteriesRequiredPowerChanged}</li>
 * <li>Imported variables:
 *   <ul>
 *   <li>name = {@code batteriesInputPower}, type = {@code Double}</li>
 *   <li>name = {@code batteriesOutputPower}, type = {@code Double}</li>
 *   </ul>
 * <li>Exported variables:
 *   name = {@code batteriesRequiredPower}, type = {@code Double}</li>
 * </li>
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
 * <p>Created on : 2025-10-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(
	imported = {BatteriesEmpty.class, BatteriesAvailable.class},
	exported = {SIL_StartCharging.class, SIL_StopCharging.class,
				SIL_BatteriesRequiredPowerChanged.class})
@ModelImportedVariables({
	@ModelImportedVariable(name = "batteriesInputPower", type = Double.class),
	@ModelImportedVariable(name = "batteriesOutputPower", type = Double.class)
})
@ModelExportedVariable(name = "batteriesRequiredPower", type = Double.class)
//-----------------------------------------------------------------------------
public class			BatteriesUnitTesterSILModel
extends		AbstractTestScenarioBasedAtomicHIOA
implements	BatteriesUserI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static final boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static final boolean		DEBUG = false;

	/** single model URI.													*/
	public static final String	URI = "batteries-unit-tester-model";
	/**	name of the run parameter for the test scenario to be executed.		*/
	public static final String	TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";

	/** when true, the batteries empty, otherwise, it is false.				*/
	protected boolean			batteriesEmpty;
	/** power consumed from the electric circuit to charge the batteries
	 *  in {@code MeasurementUnit.AMPERES}.									*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>		batteriesInputPower;
	/** power required by the electric circuit from the batteries in
	 *  {@code MeasurementUnit.AMPERES}.									*/
	@ExportedVariable(type = Double.class)
	protected Value<Double>		batteriesRequiredPower = new Value<>(this);
	/** power delivered to the electric circuit by the batteries in
	 *  {@code MeasurementUnit.AMPERES}.									*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>		batteriesOutputPower;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic hybrid input/output model with the given URI (if null,
	 * one will be generated) and to be run by the given simulator using the
	 * given time unit for its clock.
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
	public				BatteriesUnitTesterSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert	BatteriesUnitTesterSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "BatteriesUnitTesterModel.implementationInvariants("
						+ "this)");
		assert	BatteriesUnitTesterSILModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: BatteriesUnitTesterModel."
						+ "invariants(this)");
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
	}

	/**
	 * set a new batteries required power value.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code value >= 0.0}
	 * pre	{@code t != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param value	a new batteries required power value.
	 * @param t		the simulated time at which the change is made.
	 */
	public void			setBatteriesRequiredPower(double value, Time t)
	{
		assert	value >= 0 : new NeoSim4JavaException("value >= 0");
		assert	t != null : new NeoSim4JavaException("t != null");

		this.batteriesRequiredPower.setNewValue(value, t);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.batteriesEmpty = false;

		super.initialiseState(initialTime);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		// at each call, the method tries to initialise the variables held by
		// the model and return the number of variables that were initialised
		// and the number that could not be initialised (because variables
		// imported from other models they depend upon are not yet initialised)
		int numberOfNewlyInitialisedVariables = 0;
		int numberOfStillNotInitialisedVariables = 0;

		// the variable batteriesRequiredPower is exported and does not depend
		// upon any other variable, hence it can be immediately initialised
		if (!this.batteriesRequiredPower.isInitialised()) {
			this.batteriesRequiredPower.initialise(0.0);
			numberOfNewlyInitialisedVariables++;
			if (DEBUG) {
				this.logMessage(
						"fixpointInitialiseVariables batteriesRequiredPower = "
						+ this.batteriesRequiredPower.getValue());
			}
		}

		// the two counters are returned and aggregated among the different
		// execution of fixpointInitialiseVariables in the different models
		// if the total numbers gives 0 still not initialised variable, then
		// the fix point has been reached, but if there are still variables not
		// initialised but some have been initialised during the run (i.e.,
		// numberOfNewlyInitialisedVariables > 0) then the method
		// fixpointInitialiseVariables must be rerun on all models until all
		// variables have been initialised
		return new Pair<Integer, Integer>(numberOfNewlyInitialisedVariables,
										  numberOfStillNotInitialisedVariables);
	}

	/**
	 * @see etape2.equipments.batteries.mil.BatteriesUserI#signalBatteriesEmpty()
	 */
	@Override
	public void			signalBatteriesEmpty()
	{
		this.batteriesEmpty = true;
	}

	/**
	 * @see etape2.equipments.batteries.mil.BatteriesUserI#signalBatteriesAvailable()
	 */
	@Override
	public void			signalBatteriesAvailable()
	{
		this.batteriesEmpty = false;
	}

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicHIOA#output()
	 */
	@Override
	public ArrayList<EventI> output() {
		return super.output();
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// tracing
		if (VERBOSE) {
			this.logMessage(
				"userDefinedInternalTransition at "
				+ this.getCurrentStateTime()
				+ ": batteries empty = " + this.batteriesEmpty
				+ ", batteriesInputPower = " + this.batteriesInputPower
				+ ", batteriesRequiredPower = " + this.batteriesRequiredPower
				+ ", batteriesOutputPower = " + this.batteriesOutputPower);
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
			this.logMessage(
				"userDefinedExternalTransition at "
				+ this.getCurrentStateTime()
				+ " on event " + e
				+ ": batteries empty = " + this.batteriesEmpty
				+ ", batteriesInputPower = " + this.batteriesInputPower
				+ ", batteriesRequiredPower = " + this.batteriesRequiredPower
				+ ", batteriesOutputPower = " + this.batteriesOutputPower);
		}
	}
}
// -----------------------------------------------------------------------------
