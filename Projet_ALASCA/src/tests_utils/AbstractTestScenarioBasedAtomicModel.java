package tests_utils;

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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractTestScenarioBasedAtomicModel</code> implements the
 * basic atomic model methods for models that runs test scenarios.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This class is a copy of {@code AbstractTestScenarioBasedAtomicHIOA} to
 * pass by the simple inheritance constraint of Java.
 * </p>
 * <p>
 * The class defines the methods of the DEVS protocol working hand in hand with
 * an instant of {@code TestScenario} to execute test steps for atomic models
 * that must perform steps in the test scenario. As an abstract class, it is
 * mean to be extended by the concrete atomic models appearing in the simulator.
 * Hence, atomic models must have been designed to be able to execute steps in
 * test scenarios on order to be used that way. The atomic models doing so may
 * either be fully dedicated to the execution of test scenarios, and then their
 * implementation is very easy, essentially using the methods provided by this
 * abstract class, or they can also have other internal and external transitions
 * to be implemented, in such case, their DEVS protocol methods must be
 * carefully defined, especially when their own transitions must mix with
 * test steps transitions, for example when it must add to the external events
 * to be output by the test step some other events of their own no provisioned
 * by the test step.
 * </p>
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
 * <p>Created on : 2025-10-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			AbstractTestScenarioBasedAtomicModel
extends		AtomicModel
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** the test scenario to be executed.									*/
	private TestScenario		testScenario;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic model with the given URI (if null,  one will be
	 * generated) and to be run by the given simulator using the given time
	 * unit for its clock.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine == null || !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine == null || simulationEngine instanceof AtomicEngine}
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
	public				AbstractTestScenarioBasedAtomicModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		// Invariant checking
		assert AbstractTestScenarioBasedAtomicModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "AbstractTestScenarioBasedAtomicModel."
						+ "implementationInvariants(this)");
		assert AbstractTestScenarioBasedAtomicModel.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: "
						+ "AbstractTestScenarioBasedAtomicModel.invariants("
						+ "this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the test scenario has been set, otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the test scenario has been set, otherwise false.
	 */
	public boolean		testScenarioSet()
	{
		return this.testScenario != null;
	}

	/**
	 * set the test scenario to be executed.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code testScenario != null}
	 * pre	{@code testScenario.atomicModelAppearsIn(getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param testScenario
	 */
	public void			setTestScenario(TestScenario testScenario)
	{
		assert	testScenario != null :
				new PreconditionException("testScenario != null");
		assert	testScenario.atomicModelAppearsIn(this.getURI()) :
				new PreconditionException(
						"testScenario.atomicModelAppearsIn(getURI())");

		this.testScenario = testScenario;
	}

	/**
	 * return the test scenario under execution.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code testScenarioSet()}
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the test scenario under execution.
	 */
	public TestScenario	getTestScenario()
	{
		assert	this.testScenarioSet() :
				new NeoSim4JavaException("testScenarioSet()");

		return this.testScenario;
	}

	/**
	 * return the time advance to the next internal transition in the test
	 * scenario for this model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code testScenarioSet()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		assert	this.testScenarioSet() :
				new NeoSim4JavaException("testScenarioSet()");

		Duration ret = null;
		if (!this.testScenario.scenarioTerminated(this)) {
			ret = this.testScenario.simulatedDelayToNextStep(this);
		} else {
			ret = Duration.INFINITY;
		}

		if (DEBUG) {
			this.logMessage(
					"AbstractTestScenarioBasedAtomicModel::timeAdvance returns "
					+ ret);
		}

		return ret;
	}

	/**
	 * generate the output for the current internal transition of this model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code testScenarioSet()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		assert	this.testScenarioSet() :
				new NeoSim4JavaException("testScenarioSet()");

		ArrayList<EventI> ret = this.testScenario.generateOutput(this);

		if (DEBUG) {
			this.logMessage("AbstractTestScenarioBasedAtomicModel::output at "
							+ this.getTimeOfNextEvent() + " returns " + ret);
		}

		return ret;
	}

	/**
	 * perform the next internal transition in the test scenario for this model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code testScenarioSet()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		assert	this.testScenarioSet() :
				new NeoSim4JavaException("testScenarioSet()");

		super.userDefinedInternalTransition(elapsedTime);

		if (DEBUG) {
			this.logMessage(
				"AbstractTestScenarioBasedAtomicModel::"
				+ "userDefinedInternalTransition begins at "
				+ this.getCurrentStateTime()
				+ " with elapsed time " + elapsedTime);
		}

		this.testScenario.performInternalTransition(this);
		this.advanceToNextStep();

		if (DEBUG) {
			this.logMessage(
				"AbstractTestScenarioBasedAtomicModel::"
				+ "userDefinedInternalTransition ends at "
				+ this.getCurrentStateTime()
				+ " with time of next step in "
				+ this.testScenario.simulatedDelayToNextStep(this));
		}
	}	

	/**
	 * advance to next step in the test scenario for model {@code m}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code testScenarioSet()}
	 * pre	{@code getTestScenario().atomicModelAppearsIn(getURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		advanceToNextStep()
	{
		assert	this.testScenarioSet() :
				new NeoSim4JavaException(
						"Precondition violation: testScenarioSet()");
		assert	this.getTestScenario().atomicModelAppearsIn(this.getURI()) :
				new NeoSim4JavaException(
						"Precondition violation: getTestScenario()."
						+ "atomicModelAppearsIn(getURI())");

		if (DEBUG) {
			this.logMessage(
				"AbstractTestScenarioBasedAtomicModel::advanceToNextStep at "
				+ this.getCurrentStateTime());
		}

		if (!this.testScenario.scenarioTerminated(this)) {
			this.testScenario.advanceToNextStep(this);
		}
	}
}
// -----------------------------------------------------------------------------
