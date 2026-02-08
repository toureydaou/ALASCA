package etape2.equipments.laundry.mil;

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

import etape1.equipements.laundry.Laundry;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape2.GlobalReportI;
import etape2.equipments.laundry.mil.events.*;
import etape2.utils.Electricity;
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
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryElectricityModel</code> defines a simulation
 * model for the electricity consumption of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The electric power consumption (in amperes) depends upon the state and the
 * current wash mode i.e., {@code State.OFF => consumption == 0.0},
 * {@code State.ON => consumption == IDLE_POWER} and
 * {@code State.WASHING/RINSING/SPINNING/DRYING => consumption based on wash mode}.
 * The state of the laundry is modified by the reception of external events.
 * The electric power consumption is stored in the exported variable {@code currentIntensity}.
 * </p>
 *
 * <ul>
 * <li>Imported events: {@code SwitchOnLaundry}, {@code SwitchOffLaundry},
 * {@code SetPowerLaundry}, {@code StartWash}, {@code CancelWash}, etc.</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables: name = {@code currentIntensity}, type = {@code Double}</li>
 * </ul>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(imported = { SwitchOnLaundry.class, SwitchOffLaundry.class,
		SetPowerLaundry.class, StartWash.class, CancelWash.class,
		SetDelicateModeLaundry.class, SetColorModeLaundry.class,
		SetWhiteModeLaundry.class, SetIntensiveModeLaundry.class,
		SetWashTemperature.class, SetSpinSpeed.class })
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentWashingPower", type = Double.class)
@ModelExportedVariable(name = "currentWaterLevel", type = Double.class)
@ModelExportedVariable(name = "currentWashTemperature", type = Double.class)
public class LaundryElectricityModel extends AtomicHIOA implements LaundryOperationI {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created. */
	public static final String URI = LaundryElectricityModel.class.getSimpleName();
	/** when true, leaves a trace of the execution of the model. */
	public static boolean VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model. */
	public static boolean DEBUG = false;

	/** current state of the laundry machine. */
	protected LaundryState currentState = LaundryState.OFF;
	/** current wash mode of the laundry machine. */
	protected LaundryWashMode currentWashMode = LaundryWashMode.COLOR;
	/** current spin speed of the laundry machine. */
	protected SpinSpeed currentSpinSpeed = SpinSpeed.RPM_1000;

	/**
	 * true when the electricity consumption of the laundry has changed after
	 * executing an external event.
	 */
	protected boolean consumptionHasChanged = false;

	/** power consumption for each wash mode in watts. */
	protected double delicateModeConsumption;
	protected double colorModeConsumption;
	protected double whiteModeConsumption;
	protected double intensiveModeConsumption;

	/** maximum water capacity in liters. */
	protected double maxWaterCapacity;

	/** total consumption of the laundry during the simulation in kwh. */
	protected double totalConsumption;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** the current washing power in watts. */
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentWashingPower = new Value<Double>(this);

	/** current intensity in amperes. */
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentIntensity = new Value<Double>(this);

	/** current water level in liters. */
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentWaterLevel = new Value<Double>(this);

	/** current wash temperature in degrees Celsius. */
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentWashTemperature = new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean staticImplementationInvariants() {
		return true;
	}

	protected static boolean implementationInvariants(LaundryElectricityModel instance) {
		assert instance != null : new NeoSim4JavaException("Precondition violation: instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(instance.currentState != null,
				LaundryElectricityModel.class, instance, "currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(instance.totalConsumption >= 0.0,
				LaundryElectricityModel.class, instance, "totalConsumption >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentWashingPower.isInitialised() || instance.currentWashingPower.getValue() >= 0.0,
				LaundryElectricityModel.class, instance,
				"!currentWashingPower.isInitialised() || currentWashingPower.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentIntensity.isInitialised() || instance.currentIntensity.getValue() >= 0.0,
				LaundryElectricityModel.class, instance,
				"!currentIntensity.isInitialised() || currentIntensity.getValue() >= 0.0");
		return ret;
	}

	protected static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(URI != null && !URI.isEmpty(),
				LaundryElectricityModel.class, "URI != null && !URI.isEmpty()");
		return ret;
	}

	protected static boolean invariants(LaundryElectricityModel instance) {
		assert instance != null : new NeoSim4JavaException("Precondition violation: instance != null");
		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a laundry MIL electricity model instance.
	 *
	 * @param uri               URI of the model.
	 * @param simulatedTimeUnit time unit used for the simulation time.
	 * @param simulationEngine  simulation engine to which the model is attached.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryElectricityModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine)
			throws Exception {

		super(uri, simulatedTimeUnit, simulationEngine);

		this.delicateModeConsumption = Laundry.DELICATE_MODE_POWER_IN_WATTS.getData();
		this.colorModeConsumption = Laundry.COLOR_MODE_POWER_IN_WATTS.getData();
		this.whiteModeConsumption = Laundry.WHITE_MODE_POWER_IN_WATTS.getData();
		this.intensiveModeConsumption = Laundry.INTENSIVE_MODE_POWER_IN_WATTS.getData();
		this.maxWaterCapacity = Laundry.DRUM_CAPACITY.getData();

		this.getSimulationEngine().setLogger(new StandardLogger());

		assert LaundryElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("LaundryElectricityModel.implementationInvariants(this)");
		assert LaundryElectricityModel.invariants(this)
				: new NeoSim4JavaException("LaundryElectricityModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods from LaundryOperationI
	// -------------------------------------------------------------------------

	@Override
	public void setState(LaundryState s) {
		LaundryState old = this.currentState;
		this.currentState = s;
		if (old != s) {
			this.consumptionHasChanged = true;
		}
	}

	public void setState(LaundryState s, Time t) {
		this.setState(s);
	}

	@Override
	public LaundryState getState() {
		return this.currentState;
	}

	@Override
	public void setWashMode(LaundryWashMode m) {
		LaundryWashMode old = this.currentWashMode;
		this.currentWashMode = m;
		if (old != m) {
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public LaundryWashMode getWashMode() {
		return this.currentWashMode;
	}

	@Override
	public void setStateMode(LaundryState s, LaundryWashMode m) {
		LaundryState oldState = this.currentState;
		LaundryWashMode oldMode = this.currentWashMode;
		this.currentState = s;
		this.currentWashMode = m;
		if (oldState != s || oldMode != m) {
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public void setCurrentWashingPower(double newPower, Time t) {
		assert newPower >= 0.0 : new NeoSim4JavaException("newPower >= 0.0");
		double oldPower = this.currentWashingPower.isInitialised() ? this.currentWashingPower.getValue() : 0.0;
		this.currentWashingPower.setNewValue(newPower, t);
		if (oldPower != newPower) {
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public void setCurrentWaterLevel(double newLevel, Time t) {
		assert newLevel >= 0.0 : new NeoSim4JavaException("newLevel >= 0.0");
		this.currentWaterLevel.setNewValue(newLevel, t);
	}

	@Override
	public void setCurrentWashTemperature(double newTemp, Time t) {
		assert newTemp >= 0.0 : new NeoSim4JavaException("newTemp >= 0.0");
		this.currentWashTemperature.setNewValue(newTemp, t);
	}

	@Override
	public void setSpinSpeed(SpinSpeed speed) {
		this.currentSpinSpeed = speed;
	}

	@Override
	public SpinSpeed getSpinSpeed() {
		return this.currentSpinSpeed;
	}

	public Value<Double> getCurrentWaterLevel() {
		return this.currentWaterLevel;
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.currentState = LaundryState.OFF;
		this.currentWashMode = LaundryWashMode.COLOR;
		this.currentSpinSpeed = SpinSpeed.RPM_1000;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		this.getSimulationEngine().toggleDebugMode();
		this.logMessage("simulation begins.\n");

		assert LaundryElectricityModel.implementationInvariants(this)
				: new NeoSim4JavaException("LaundryElectricityModel.implementationInvariants(this)");
		assert LaundryElectricityModel.invariants(this)
				: new NeoSim4JavaException("LaundryElectricityModel.invariants(this)");
	}

	@Override
	public boolean useFixpointInitialiseVariables() {
		return true;
	}

	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		Pair<Integer, Integer> ret = null;

		if (!this.currentIntensity.isInitialised() ||
				!this.currentWashingPower.isInitialised() ||
				!this.currentWaterLevel.isInitialised() ||
				!this.currentWashTemperature.isInitialised()) {
			this.currentIntensity.initialise(0.0);
			this.currentWashingPower.initialise(0.0);
			this.currentWaterLevel.initialise(0.0);
			this.currentWashTemperature.initialise(20.0);
			StringBuffer sb = new StringBuffer("initialisation of variables: ");
			sb.append("currentIntensity = ");
			sb.append(this.currentIntensity.getValue());
			sb.append(", currentWashingPower = ");
			sb.append(this.currentWashingPower.getValue());
			sb.append(", currentWaterLevel = ");
			sb.append(this.currentWaterLevel.getValue());
			sb.append(", currentWashTemperature = ");
			sb.append(this.currentWashTemperature.getValue());
			sb.append(".\n");
			this.logMessage(sb.toString());
			ret = new Pair<>(4, 0);
		} else {
			ret = new Pair<>(0, 0);
		}

		return ret;
	}

	@Override
	public ArrayList<EventI> output() {
		return null;
	}

	@Override
	public Duration timeAdvance() {
		Duration ret = null;

		if (this.consumptionHasChanged) {
			this.consumptionHasChanged = false;
			ret = Duration.zero(this.getSimulatedTimeUnit());
		} else {
			ret = Duration.INFINITY;
		}

		return ret;
	}

	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();

		switch (this.currentState) {
		case OFF:
			this.currentIntensity.setNewValue(0.0, t);
			break;
		case ON:
			// When ON but not washing, minimal consumption
			switch (this.currentWashMode) {
			case DELICATE:
				this.currentIntensity.setNewValue(this.delicateModeConsumption / Laundry.VOLTAGE.getData(), t);
				break;
			case COLOR:
				this.currentIntensity.setNewValue(this.colorModeConsumption / Laundry.VOLTAGE.getData(), t);
				break;
			case WHITE:
				this.currentIntensity.setNewValue(this.whiteModeConsumption / Laundry.VOLTAGE.getData(), t);
				break;
			case INTENSIVE:
				this.currentIntensity.setNewValue(this.intensiveModeConsumption / Laundry.VOLTAGE.getData(), t);
				break;
			}
			break;
		case WASHING:
		case RINSING:
		case SPINNING:
		case DRYING:
			// During active cycles, use current washing power
			this.currentIntensity.setNewValue(
					this.currentWashingPower.getValue() / Laundry.VOLTAGE.getData(), t);
			break;
		}

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("new consumption: ");
			sb.append(this.currentIntensity.getValue());
			sb.append(" ");
			sb.append(ElectricMeterImplementationI.POWER_UNIT);
			sb.append(" at ");
			sb.append(this.currentIntensity.getTime());
			sb.append(".\n");
			this.logMessage(sb.toString());
		}
	}

	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert ce instanceof LaundryEventI;

		this.totalConsumption += Electricity.computeConsumption(elapsedTime,
				Laundry.VOLTAGE.getData() * this.currentIntensity.getValue());

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("execute the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".\n");
			this.logMessage(sb.toString());
		}

		ce.executeOn(this);
	}

	@Override
	public void endSimulation(Time endTime) {
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption += Electricity.computeConsumption(d,
				Laundry.VOLTAGE.getData() * this.currentIntensity.getValue());

		if (VERBOSE) {
			this.logMessage("simulation ends.\n");
			this.logMessage(new LaundryElectricityReport(this.uri, this.totalConsumption).printout("  "));
		}

		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>LaundryElectricityReport</code> implements the
	 * simulation report for the <code>LaundryElectricityModel</code>.
	 */
	public static class LaundryElectricityReport implements SimulationReportI, GlobalReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalConsumption; // in kwh

		public LaundryElectricityReport(String modelURI, double totalConsumption) {
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
		}

		@Override
		public String getModelURI() {
			return this.modelURI;
		}

		@Override
		public String printout(String indent) {
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

	@Override
	public SimulationReportI getFinalReport() {
		return new LaundryElectricityReport(this.getURI(), this.totalConsumption);
	}
}
// -----------------------------------------------------------------------------
