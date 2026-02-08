package etape3.equipements.laundry.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape1.equipements.laundry.Laundry;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape2.equipments.laundry.mil.LaundryElectricityModel;
import etape2.equipments.laundry.mil.LaundrySimulationConfigurationI;
import etape2.equipments.laundry.mil.events.LaundryEventI;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryElectricitySILModel</code> defines a SIL model of the
 * electricity consumption of a laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This model extends the MIL {@code LaundryElectricityModel} to work within
 * a SIL simulation context. It exports a {@code currentIntensity} variable
 * that is used by the {@code ElectricMeterElectricitySILModel} to compute
 * global electricity consumption.
 * </p>
 *
 * <ul>
 * <li>Imported events: {@code SwitchOnLaundry}, {@code SwitchOffLaundry},
 * {@code StartWash}, {@code CancelWash}, {@code SetDelicateModeLaundry},
 * {@code SetColorModeLaundry}, {@code SetWhiteModeLaundry},
 * {@code SetIntensiveModeLaundry}, {@code SetWashTemperature},
 * {@code SetSpinSpeed}, {@code SetPowerLaundry}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables: name = {@code currentIntensity}, type =
 * {@code Double}</li>
 * </ul>
 *
 * <p>Created on : 2026-02-06</p>
 */
// -----------------------------------------------------------------------------
public class LaundryElectricitySILModel extends LaundryElectricityModel {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for an instance model; works as long as only one instance is
	 *  created. */
	public static final String URI =
			LaundryElectricitySILModel.class.getSimpleName();

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				LaundryElectricitySILModel.class,
				"URI != null && !URI.isEmpty()");
		return ret;
	}

	protected static boolean invariants(LaundryElectricitySILModel instance) {
		assert instance != null :
			new NeoSim4JavaException(
					"Precondition violation: instance != null");
		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public LaundryElectricitySILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.delicateModeConsumption =
				Laundry.DELICATE_MODE_POWER_IN_WATTS.getData();
		this.colorModeConsumption =
				Laundry.COLOR_MODE_POWER_IN_WATTS.getData();
		this.whiteModeConsumption =
				Laundry.WHITE_MODE_POWER_IN_WATTS.getData();
		this.intensiveModeConsumption =
				Laundry.INTENSIVE_MODE_POWER_IN_WATTS.getData();

		if (VERBOSE || DEBUG) {
			this.getSimulationEngine().setLogger(new StandardLogger());
		}
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time startTime) {
		super.initialiseState(startTime);

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}
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

			if (VERBOSE) {
				StringBuffer sb = new StringBuffer("new consumption: ");
				sb.append(this.currentIntensity.getValue());
				sb.append(" at ");
				sb.append(this.currentIntensity.getTime());
				this.logMessage(sb.toString());
			}

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
			ret = new Duration(0.0, this.getSimulatedTimeUnit());
		} else {
			ret = Duration.INFINITY;
		}

		return ret;
	}

	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		super.userDefinedInternalTransition(elapsedTime);

		if (VERBOSE) {
			StringBuffer message = new StringBuffer(
					"executes an internal transition ");
			message.append("with current consumption ");
			message.append(this.currentIntensity.getValue());
			message.append(" ");
			message.append(ElectricMeterImplementationI.POWER_UNIT);
			message.append(" at ");
			message.append(this.currentIntensity.getTime());
			this.logMessage(message.toString());
		}
	}

	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		super.userDefinedExternalTransition(elapsedTime);
	}

	@Override
	public void endSimulation(Time endTime) {
		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	@Override
	public void setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

		if (simParams.containsKey(
				AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			this.getSimulationEngine().setLogger(
					AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}
}
// -----------------------------------------------------------------------------
