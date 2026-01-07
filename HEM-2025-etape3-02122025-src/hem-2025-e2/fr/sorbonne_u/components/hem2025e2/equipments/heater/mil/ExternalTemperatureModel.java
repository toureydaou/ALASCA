package fr.sorbonne_u.components.hem2025e2.equipments.heater.mil;

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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The class <code>ExternalTemperatureModel</code> defines a simulation model
 * for the environment, namely the external temperature of the house.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The model makes the temperature vary over some period representing typically
 * a day. The variation is taken as a cosine between {@code Math.PI} and
 * {@code 3*Math.PI}. The cosine (plus 1 and divided by 2 to vary between 0 and
 * 1) is taken as a coefficient applied to the maximal variation over a day and
 * then added to the minimal temperature to get the current temperature.
 * </p>
 * <p>
 * The model reevaluates the external temperature at some predefined rate i.e.,
 * the evaluation step. It exports this temperature in a variable called
 * {@code externalTemperature}.
 * </p>
 * 
 * <ul>
 * <li>Imported events: none</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables:
 *   name = {@code externalTemperature}, type = {@code Double}</li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code cycleTime >= 0.0 && cycleTime <= PERIOD}
 * invariant	{@code STEP > 0.0}
 * invariant	{@code evaluationStep.getSimulatedDuration() > 0.0}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MeasurementUnit.CELSIUS.equals(HeaterTemperatureI.TEMPERATURE_UNIT)}
 * invariant	{@code URI != null && !URI.isEmpty()}
 * invariant	{@code MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE}
 * invariant	{@code PERIOD > 0.0}
 * </pre>
 * 
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExportedVariable(name = "externalTemperature", type = Double.class)
//-----------------------------------------------------------------------------
public class			ExternalTemperatureModel
extends		AtomicHIOA
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String		URI = ExternalTemperatureModel.class.
															getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean			VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean			DEBUG = false;

	// TODO: define as simulation run parameters
	/** minimal external temperature in celsius.							*/
	protected static double			MIN_EXTERNAL_TEMPERATURE = -5.0;
	/** maximal external temperature in celsius.							*/
	protected static double			MAX_EXTERNAL_TEMPERATURE = 15.0;
	/** period of the temperature variation cycle (day); the cycle begins
	 *  at the minimal temperature and ends at the same temperature.		*/
	protected static double			PERIOD = 24.0;
	/** evaluation step for the equation (assumed in hours).				*/
	protected static double			STEP = 60.0/3600.0;	// 60 seconds

	/** evaluation step as a duration, including the time unit.				*/
	protected final Duration		evaluationStep;
	/** current time in the external temperature variation cycle.			*/
	protected double				cycleTime;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current external temperature in Celsius.							*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	externalTemperature =
												new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static implementation invariants are observed, false otherwise.
	 */
	protected static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				STEP > 0.0,
				ExternalTemperatureModel.class,
				"STEP > 0.0");
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		ExternalTemperatureModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.cycleTime >= 0.0 && instance.cycleTime <= PERIOD,
				ExternalTemperatureModel.class,
				instance,
				"cycleTime >= 0.0 && instance.cycleTime <= PERIOD");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.evaluationStep.getSimulatedDuration() > 0.0,
				ExternalTemperatureModel.class,
				instance,
				"evaluationStep.getSimulatedDuration() > 0.0");
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
	 * @return			true if the static invariants are observed, false otherwise.
	 */
	protected static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= HeaterSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				MeasurementUnit.CELSIUS.equals(
										HeaterTemperatureI.TEMPERATURE_UNIT),
				ExternalTemperatureModel.class,
				"MeasurementUnit.CELSIUS.equals("
				+ "HeaterTemperatureI.TEMPERATURE_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				ExternalTemperatureModel.class,
				"URI != null && !URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE,
				ExternalTemperatureModel.class,
				"MAX_EXTERNAL_TEMPERATURE > MIN_EXTERNAL_TEMPERATURE");
		ret &= AssertionChecking.checkStaticInvariant(
				PERIOD > 0.0,
				ExternalTemperatureModel.class,
				"PERIOD > 0.0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(
		ExternalTemperatureModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an external temperature MIL model instance.
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
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do</i>.
	 */
	public				ExternalTemperatureModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	ExternalTemperatureModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.implementationInvariants(this)");
		assert	ExternalTemperatureModel.invariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.cycleTime = 0.0;

		assert	ExternalTemperatureModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.implementationInvariants(this)");
		assert	ExternalTemperatureModel.invariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.invariants(this)");
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
		Pair<Integer, Integer> ret = null;

		if (!this.externalTemperature.isInitialised()) {
			this.externalTemperature.initialise(MIN_EXTERNAL_TEMPERATURE);

			if (VERBOSE) {
				this.logMessage("simulation begins.");
				StringBuffer message =
					new StringBuffer("current external temperature: ");
				message.append(this.externalTemperature.getValue());
				message.append(" at ");
				message.append(this.getCurrentStateTime());
				this.logMessage(message.toString());
			}

			ret = new Pair<>(1, 0);
		} else {
			ret = new Pair<>(0, 0);
		}

		assert	ExternalTemperatureModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.implementationInvariants(this)");
		assert	ExternalTemperatureModel.invariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#initialiseVariables()
	 */
	@Override
	public void			initialiseVariables()
	{
		super.initialiseVariables();

		assert	ExternalTemperatureModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.implementationInvariants(this)");
		assert	ExternalTemperatureModel.invariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		// the model does not export any event.
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		// the model makes an internal transition every evaluation step
		// duration
		return this.evaluationStep;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// compute the current time in the cycle
		this.cycleTime += elapsedTime.getSimulatedDuration();
		if (this.cycleTime > PERIOD) {
			this.cycleTime -= PERIOD;
		}
		// compute the new temperature
		double c = Math.cos((1.0 + this.cycleTime/(PERIOD/2.0))*Math.PI);
		double newTemp =
				MIN_EXTERNAL_TEMPERATURE +
					(MAX_EXTERNAL_TEMPERATURE - MIN_EXTERNAL_TEMPERATURE)*
																((1.0 + c)/2.0);
		this.externalTemperature.setNewValue(newTemp,
											 this.getCurrentStateTime());

		// Tracing
		if (VERBOSE) {
			StringBuffer message =
				new StringBuffer("current external temperature: ");
			message.append(this.externalTemperature.getValue());
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			this.logMessage(message.toString());
		}

		assert	ExternalTemperatureModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.implementationInvariants(this)");
		assert	ExternalTemperatureModel.invariants(this) :
				new NeoSim4JavaException(
						"ExternalTemperatureModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport()
	{
		return null;
	}
}
// -----------------------------------------------------------------------------
