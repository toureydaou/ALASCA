package etape3.equipements.coffee_machine.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.Heat;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.ServeCoffee;
import etape2.equipments.coffeemachine.mil.events.SetEcoModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetMaxModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetNormalModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The class <code>CoffeeMachineStateSILModel</code> is a simple model that
 * tracks the current state and mode of the coffee machine as well as receives
 * events triggering state changes and reemits them towards the other coffee
 * machine models.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This model acts as a central coordinator for state management in the coffee
 * machine SIL simulation. It receives all control events from the component,
 * updates its internal state accordingly, and reemits these events to the
 * other simulation models (electricity and temperature models) that need to
 * react to state changes.
 * </p>
 *
 * <p><strong>Implementation Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code currentState != null}
 * invariant	{@code currentMode != null}
 * </pre>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
 * </pre>
 *
 * <p>Created on : 2025-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(imported = {SwitchOnCoffeeMachine.class,
		 						 SwitchOffCoffeeMachine.class,
		 						 SetEcoModeCoffeeMachine.class,
		 						 SetMaxModeCoffeeMachine.class,
		 						 SetNormalModeCoffeeMachine.class,
		 						 SetSuspendedModeCoffeeMachine.class,
		 						 DoNotHeat.class,
		 						 Heat.class,
		 						 MakeCoffee.class,
		 						 ServeCoffee.class,
		 						 FillWaterCoffeeMachine.class},
					 exported = {SwitchOnCoffeeMachine.class,
							 	 SwitchOffCoffeeMachine.class,
							 	 SetEcoModeCoffeeMachine.class,
							 	 SetMaxModeCoffeeMachine.class,
							 	 SetNormalModeCoffeeMachine.class,
							 	 SetSuspendedModeCoffeeMachine.class,
							 	 DoNotHeat.class,
							 	 Heat.class,
							 	 MakeCoffee.class,
							 	 ServeCoffee.class,
							 	 FillWaterCoffeeMachine.class})
// -----------------------------------------------------------------------------
public class			CoffeeMachineStateSILModel
extends		AtomicModel
implements	SIL_CoffeeMachineOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a SIL model; works when only one instance is created.		*/
	public static final String	URI = CoffeeMachineStateSILModel.class.getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** current state of the coffee machine.								*/
	protected CoffeeMachineState	currentState = CoffeeMachineState.OFF;
	/** current mode of the coffee machine.									*/
	protected CoffeeMachineMode		currentMode = CoffeeMachineMode.SUSPEND;
	/** last received external event that must be reemitted.				*/
	protected EventI				lastReceived;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the glass-box invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		CoffeeMachineStateSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.currentState != null,
					CoffeeMachineStateSILModel.class,
					instance,
					"currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.currentMode != null,
					CoffeeMachineStateSILModel.class,
					instance,
					"currentMode != null");
		return ret;
	}

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
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				CoffeeMachineStateSILModel.class,
				"URI != null && !URI.isEmpty()");
		return ret;
	}

	/**
	 * return true if the black-box invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(
		CoffeeMachineStateSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a coffee machine SIL model instance.
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
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 */
	public				CoffeeMachineStateSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	CoffeeMachineStateSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineStateModel.implementationInvariants(this)");
		assert	CoffeeMachineStateSILModel.invariants(this) :
				new NeoSim4JavaException("CoffeeMachineStateModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setState(etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState)
	 */
	@Override
	public void			setState(CoffeeMachineState s)
	{
		this.currentState = s;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#getState()
	 */
	@Override
	public CoffeeMachineState	getState()
	{
		return this.currentState;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setMode(etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode)
	 */
	@Override
	public void			setMode(CoffeeMachineMode m)
	{
		this.currentMode = m;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#getMode()
	 */
	@Override
	public CoffeeMachineMode	getMode()
	{
		return this.currentMode;
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setCurrentHeatingPower(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentHeatingPower(double newPower, Time t)
	{
		// Nothing to be done here, power is managed by electricity model
	}

	/**
	 * @see etape3.equipements.coffee_machine.sil.SIL_CoffeeMachineOperationI#setCurrentWaterLevel(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentWaterLevel(double newLevel, Time t)
	{
		// Nothing to be done here, water level is managed by electricity model
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

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
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.lastReceived = null;
		this.currentState = CoffeeMachineState.OFF;
		this.currentMode = CoffeeMachineMode.SUSPEND;

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}

		assert	CoffeeMachineStateSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"CoffeeMachineStateModel.implementationInvariants(this)");
		assert	CoffeeMachineStateSILModel.invariants(this) :
				new NeoSim4JavaException("CoffeeMachineStateModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		if (this.lastReceived != null) {
			return Duration.zero(this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		assert	this.lastReceived != null :
				new NeoSim4JavaException("lastReceived != null");

		ArrayList<EventI> ret = new ArrayList<EventI>();
		ret.add(this.lastReceived);
		this.lastReceived = null;
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert	currentEvents != null && currentEvents.size() == 1 :
				new NeoSim4JavaException(
						"currentEvents != null && currentEvents.size() == 1");

		this.lastReceived = currentEvents.get(0);

		if (VERBOSE) {
			StringBuffer message = new StringBuffer(this.uri);
			message.append(" executes the external event ");
			message.append(this.lastReceived);
			this.logMessage(message.toString());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}
		super.endSimulation(endTime);
	}

	@Override
	public void setStateMode(CoffeeMachineState s, CoffeeMachineMode m) {
		this.currentState = s;
		this.currentMode = m;
	}
}
// -----------------------------------------------------------------------------
