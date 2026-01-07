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
import fr.sorbonne_u.components.hem2025e1.equipments.heater.Heater.HeaterState;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.components.hem2025e2.GlobalReportI;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.HeaterEventI;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SetPowerHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e2.utils.Electricity;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
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
 * The class <code>HeaterElectricityModel</code> defines a simulation model
 * for the electricity consumption of the heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The electric power consumption (in amperes) depends upon the state and the
 * current power level i.e., {@code State.OFF => consumption == 0.0},
 * {@code State.ON => consumption == NOT_HEATING_POWER} and
 * {@code State.HEATING => consumption >= NOT_HEATING_POWER && consumption <=
 * MAX_HEATING_POWER}). The state of the heater is modified by the reception of
 * external events ({@code SwitchOnHeater}, {@code SwitchOffHeater},
 * {@code Heat} and {@code DoNotHeat}). The power level is set through the
 * external event {@code SetPowerHeater} that has a parameter defining the
 * required power level. The electric power consumption is stored in the
 * exported variable {@code currentIntensity}.
 * </p>
 * <p>
 * Initially, the mode is in state {@code State.OFF} and the electric power
 * consumption at 0.0.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code SwitchOnHeater},
 *   {@code SwitchOffHeater},
 *   {@code SetPowerHeater},
 *   {@code Heat},
 *   {@code DoNotHeat}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables:
 *   name = {@code currentIntensity}, type = {@code Double}</li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code NOT_HEATING_POWER >= 0.0}
 * invariant	{@code MAX_HEATING_POWER > NOT_HEATING_POWER}
 * invariant	{@code TENSION > 0.0}
 * invariant	{@code currentState != null}
 * invariant	{@code totalConsumption >= 0.0}
 * invariant	{@code !currentHeatingPower.isInitialised() || currentHeatingPower.getValue() >= 0.0}
 * invariant	{@code !currentIntensity.isInitialised() || currentIntensity.getValue() >= 0.0}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
 * invariant	{@code NOT_HEATING_POWER_RUNPNAME != null && !NOT_HEATING_POWER_RUNPNAME.isEmpty()}
 * invariant	{@code MAX_HEATING_POWER_RUNPNAME != null && !MAX_HEATING_POWER_RUNPNAME.isEmpty()}
 * invariant	{@code TENSION_RUNPNAME != null && !TENSION_RUNPNAME.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(imported = {SwitchOnHeater.class,
								 SwitchOffHeater.class,
								 SetPowerHeater.class,
								 Heat.class,
								 DoNotHeat.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentHeatingPower", type = Double.class)
//-----------------------------------------------------------------------------
public class			HeaterElectricityModel
extends		AtomicHIOA
implements	HeaterOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = HeaterElectricityModel.class.
															getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** current state of the heater.										*/
	protected HeaterState		currentState = HeaterState.OFF;
	/** true when the electricity consumption of the heater has changed
	 *  after executing an external event; the external event changes the
	 *  value of <code>currentState</code> and then an internal transition
	 *  will be triggered by putting through in this variable which will
	 *  update the variable <code>currentIntensity</code>.					*/
	protected boolean			consumptionHasChanged = false;

	/** total consumption of the heater during the simulation in kwh.		*/
	protected double			totalConsumption;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** the current heating power between 0 and
	 *  {@code HeaterElectricityModel.MAX_HEATING_POWER} in the power unit
	 *  used by the heater.													*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentHeatingPower =
														new Value<Double>(this);
	/** current intensity in the power unit used by the electric meter.		*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double>	currentIntensity = new Value<Double>(this);

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
	 * @return			true if the static implementation invariants are observed, false otherwise.
	 */
	protected static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
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
		HeaterElectricityModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.currentState != null,
					HeaterElectricityModel.class,
					instance,
					"currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.totalConsumption >= 0.0,
					HeaterElectricityModel.class,
					instance,
					"totalConsumption >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
					!instance.currentHeatingPower.isInitialised() ||
								instance.currentHeatingPower.getValue() >= 0.0,
					HeaterElectricityModel.class,
					instance,
					"!currentHeatingPower.isInitialised() || "
							+ "currentHeatingPower.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
					!instance.currentIntensity.isInitialised() ||
									instance.currentIntensity.getValue() >= 0.0,
					HeaterElectricityModel.class,
					instance,
					"!currentIntensity.isInitialised() || "
							+ "currentIntensity.getValue() >= 0.0");
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
		ret &= HeaterSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				HeaterElectricityModel.class,
				"URI != null && !URI.isEmpty()");
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
		HeaterElectricityModel instance
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
	 * create a heater MIL electricity model instance.
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
	public				HeaterElectricityModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterOperationI#setState(fr.sorbonne_u.components.hem2025e1.equipments.heater.Heater.HeaterState)
	 */
	@Override
	public void			setState(HeaterState s)
	{
		HeaterState old = this.currentState;
		this.currentState = s;
		if (old != s) {
			this.consumptionHasChanged = true;					
		}

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterOperationI#getState()
	 */
	@Override
	public HeaterState		getState()
	{
		return this.currentState;
	}

	/**
	 * set the current heating power of the heater to {@code newPower}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code newPower >= 0.0 && newPower <= MAX_HEATING_POWER}
	 * post	{@code getCurrentHeatingPower() == newPower}
	 * </pre>
	 *
	 * @param newPower	the new power in the unit used by the heater to be set on the heater.
	 * @param t			time at which the new power is set.
	 */
	public void			setCurrentHeatingPower(double newPower, Time t)
	{
		assert	newPower >= 0.0 &&
					newPower <= HeaterExternalControlI.MAX_POWER_LEVEL.getData() :
				new NeoSim4JavaException(
					"Precondition violation: newPower >= 0.0 && "
					+ "newPower <= HeaterElectricityModel.MAX_HEATING_POWER,"
					+ " but newPower = " + newPower);

		double oldPower = this.currentHeatingPower.getValue();
		this.currentHeatingPower.setNewValue(newPower, t);
		if (newPower != oldPower) {
			this.consumptionHasChanged = true;
		}

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");
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

		this.currentState = HeaterState.OFF;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");
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
	public Pair<Integer, Integer> fixpointInitialiseVariables()
	{
		Pair<Integer, Integer> ret = null;

		if (!this.currentIntensity.isInitialised() ||
								!this.currentHeatingPower.isInitialised()) {
			// initially, the heater is off, so its consumption is zero.
			this.currentIntensity.initialise(0.0);
			this.currentHeatingPower.initialise(
						HeaterExternalControlI.MAX_POWER_LEVEL.getData());

			if (VERBOSE) {
				StringBuffer sb = new StringBuffer("new consumption: ");
				sb.append(this.currentIntensity.getValue());
				sb.append(" ");
				sb.append(ElectricMeterImplementationI.POWER_UNIT);
				sb.append(" at ");
				sb.append(this.currentIntensity.getTime());
				sb.append(" seconds.");
				this.logMessage(sb.toString());
			}

			ret = new Pair<>(2, 0);
		} else {
			ret = new Pair<>(0, 0);
		}

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		Duration ret = null;

		if (this.consumptionHasChanged) {
			// When the consumption has changed, an immediate (delay = 0.0)
			// internal transition must be made to update the electricity
			// consumption.
			this.consumptionHasChanged = false;
			ret = Duration.zero(this.getSimulatedTimeUnit());
		} else {
			// As long as the state does not change, no internal transition
			// is made (delay = infinity).
			ret = Duration.INFINITY;
		}

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();
		if (this.currentState == HeaterState.ON) {
			this.currentIntensity.setNewValue(
					HeaterExternalControlI.NOT_HEATING_POWER.getData()/
									HeaterExternalControlI.TENSION.getData(),
					t);
		} else if (this.currentState == HeaterState.HEATING) {
			this.currentIntensity.setNewValue(
					this.currentHeatingPower.getValue()/
									HeaterExternalControlI.TENSION.getData(),
					t);
		} else {
			assert	this.currentState == HeaterState.OFF;
			this.currentIntensity.setNewValue(0.0, t);
		}

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("new consumption: ");
			sb.append(this.currentIntensity.getValue());
			sb.append(" ");
			sb.append(ElectricMeterImplementationI.POWER_UNIT);
			sb.append(" at ");
			sb.append(this.currentIntensity.getTime());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the heater model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof HeaterEventI;

		// compute the total consumption for the simulation report.
		this.totalConsumption +=
				Electricity.computeConsumption(
						elapsedTime,
						HeaterExternalControlI.TENSION.getData() *
											this.currentIntensity.getValue());

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("execute the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		// the next call will update the current state of the heater and if
		// this state has changed, it put the boolean consumptionHasChanged
		// at true, which in turn will trigger an immediate internal transition
		// to update the current intensity of the heater electricity
		// consumption.
		ce.executeOn(this);

		assert	HeaterElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.implementationInvariants(this)");
		assert	HeaterElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption +=
				Electricity.computeConsumption(
						d,
						HeaterExternalControlI.TENSION.getData() *
											this.currentIntensity.getValue());

		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>HeaterElectricityReport</code> implements the
	 * simulation report for the <code>HeaterElectricityModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>White-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p><strong>Black-box Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2023-09-29</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		HeaterElectricityReport
	implements	SimulationReportI, GlobalReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption; // in kwh


		public			HeaterElectricityReport(
			String modelURI,
			double totalConsumption
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
		}

		@Override
		public String	getModelURI()
		{
			return this.modelURI;
		}

		@Override
		public String	printout(String indent)
		{
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("total consumption in kwh = ");
			ret.append(this.totalConsumption);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}		
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport()
	{
		return new HeaterElectricityReport(this.getURI(), this.totalConsumption);
	}
}
// -----------------------------------------------------------------------------
