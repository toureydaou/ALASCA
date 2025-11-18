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

import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;

// -----------------------------------------------------------------------------
/**
 * The class <code>TestStep</code> implements a description of a test action
 * to be taken as part of a scenario.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A test step is executed by a simulation model designated by its URI at some
 * instant, its time of occurrence. The time management used in test steps and
 * tests scenario is based on two time lines. Test actions are planned using the
 * class {@code Instant}. A test scenario has a start instant and an end
 * instant. The time of occurrence of a test step is an instant between the
 * start and end instants. This time line of instants is mapped to the time
 * line of the simulation clock represented by the class {@code Time} of the
 * simulation framework.
 * </p>
 * <p>
 * The test action <i>per se</i> is organised following the DEVS protocol for
 * internal transitions. It is composed of:
 * </p>
 * <ul>
 *   <li>an output function that generates the array list of events to be
 *     exported at the defined internal transition,</li>
 *   <li>an internal transition function that performs the modifications of
 *     the state of the model according to the sought action.</li>
 * </ul>
 * <p>
 * The output function takes the reference to the simulation model and the
 * simulated time of occurrence; it returns an array list of events as does the
 * method {@code output} in the framework. The internal transition function also
 * takes as argument the reference to the simulation model and the simulated
 * time of occurrence of the transition but does not return a result as it
 * works by side effects on the simulation model.
 * </p>
 * <p>
 * The above ingredients of a test step are initialised at creation time by th
 * constructor. The class defines the methods {@code generateOutput} and
 * {@code performInternalTransition} that execute their respective function.
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
 * <p>Created on : 2025-10-20</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			SimulationTestStep
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the simulation model; that must perform the step.			*/
	protected final String							performingAtomicModelURI;
	/** instant at which the step must be executed.							*/
	protected final Instant							timeOfOccurrence;
	/** function that generates the events that the model will output at
	 *  this step.															*/
	protected final BiFunction<AtomicModel, Time, ArrayList<EventI>>
													outputGenerator;
	/** function that performs the effects of the internal transition on
	 *  the simulation model.												*/
	protected final BiConsumer<AtomicModel,Time>	internalTransitionPerformer;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a simulation test step.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code performingAtomicModelURI != null && !performingAtomicModelURI.isEmpty()}
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code outputGenerator != null}
	 * pre	{@code internalTransitionPerformer != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param performingAtomicModelURI		URI of the simulation model; that must perform the step.
	 * @param timeOfOccurrence				instant at which the step must be executed.
	 * @param outputGenerator				function that generates the events that the model will output at this step.
	 * @param internalTransitionPerformer	function that performs the effects of the internal transition on the simulation model.
	 */
	public				SimulationTestStep(
		String performingAtomicModelURI,
		Instant timeOfOccurrence,
		BiFunction<AtomicModel, Time, ArrayList<EventI>> outputGenerator,
		BiConsumer<AtomicModel,Time> internalTransitionPerformer
		)
	{
		super();

		assert	performingAtomicModelURI != null && !performingAtomicModelURI.isEmpty() :
				new PreconditionException("performingAtomicModelURI != null && !performingAtomicModelURI.isEmpty()");
		assert	timeOfOccurrence != null :
				new PreconditionException("timeOfOccurrence != null");
		assert	outputGenerator != null :
				new PreconditionException("outputGenerator != null");
		assert	internalTransitionPerformer != null :
				new PreconditionException("internalTransitionPerformer != null");

		this.performingAtomicModelURI = performingAtomicModelURI;
		this.timeOfOccurrence = timeOfOccurrence;
		this.outputGenerator = outputGenerator;
		this.internalTransitionPerformer = internalTransitionPerformer;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the URI of the atomic model that must perform this test step.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && !return.isEmpty()}
	 * </pre>
	 *
	 * @return	the URI of the atomic model that must perform this test step.
	 */
	public String		getPerformingAtomicModelURI()
	{
		return this.performingAtomicModelURI;
	}

	/**
	 * return the instant at which the internal transition must occur.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the instant at which the internal transition must occur.
	 */
	public Instant		getTimeOfOccurrence()
	{
		return this.timeOfOccurrence;
	}

	/**
	 * generate the events to be output by the atomic simulation model {@code m}
	 * at the time {@code m.getTimeOfNextEvent()}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && m.getURI().equals(getPerformingAtomicModelURI())}
	 * post	{@code return == null || return.stream().allMatch(e -> m.getTimeOfNextEvent().equals(e.getTimeOfOccurrence()))}
	 * </pre>
	 *
	 * @param m				atomic model that outputs the events.
	 * @return				a list of events to be output by the simulation model.
	 */
	public ArrayList<EventI>	generateOutput(AtomicModel m)
	{
		assert	m != null && m.getURI().equals(this.getPerformingAtomicModelURI()) :
				new PreconditionException(
						"m != null && m.getURI().equals("
						+ "getPerformingAtomicModelURI())");

		ArrayList<EventI> ret =
				this.outputGenerator.apply(m, m.getTimeOfNextEvent());

		assert	ret == null ||
					ret.stream().allMatch(
							e -> m.getTimeOfNextEvent().equals(
													e.getTimeOfOccurrence())) :
				new PostconditionException(
						"return == null || return.stream().allMatch("
						+ "e -> m.getTimeOfNextEvent().equals("
						+ "e.getTimeOfOccurrence()))");

		return ret;
	}

	/**
	 * perform the effects of the next internal transition on the simulation
	 * model {@code m}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null && m.getURI().equals(getPerformingAtomicModelURI())}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m		atomic model that performs the internal transition.
	 */
	public void					performInternalTransition(AtomicModel m)
	{
		assert	m != null && m.getURI().equals(this.getPerformingAtomicModelURI()) :
				new PreconditionException(
						"m != null && m.getURI().equals("
						+ "getPerformingAtomicModelURI())");

		this.internalTransitionPerformer.accept(m, m.getCurrentStateTime());
	}
}
// -----------------------------------------------------------------------------
