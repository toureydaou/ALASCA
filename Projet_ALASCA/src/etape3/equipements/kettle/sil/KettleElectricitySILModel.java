package etape3.equipements.kettle.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape2.equipments.kettle.mil.KettleElectricityModel;
import etape2.equipments.kettle.mil.KettleSimulationConfigurationI;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleElectricitySILModel</code> defines a SIL model of the
 * electricity consumption of a kettle.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This model extends the MIL {@code KettleElectricityModel} to work within
 * a SIL simulation context. It exports {@code currentIntensity} and
 * {@code currentHeatingPower} variables that are used by the
 * {@code ElectricMeterElectricitySILModel} to compute global electricity
 * consumption.
 * </p>
 *
 * <ul>
 * <li>Imported events: {@code SwitchOnKettle}, {@code SwitchOffKettle},
 * {@code SetPowerKettle}, {@code HeatKettle}, {@code DoNotHeatKettle},
 * {@code SetEcoModeKettle}, {@code SetMaxModeKettle},
 * {@code SetSuspendedModeKettle}, {@code SetNormalModeKettle}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables: name = {@code currentIntensity}, type =
 * {@code Double}; name = {@code currentHeatingPower}, type =
 * {@code Double}</li>
 * </ul>
 *
 * <p>Created on : 2026-02-06</p>
 */
// -----------------------------------------------------------------------------
public class KettleElectricitySILModel
extends		KettleElectricityModel
implements	SIL_KettleOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI for an instance model; works as long as only one instance is
	 *  created. */
	public static final String URI =
			KettleElectricitySILModel.class.getSimpleName();

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	public static boolean staticInvariants() {
		boolean ret = true;
		ret &= KettleSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				KettleElectricitySILModel.class,
				"URI != null && !URI.isEmpty()");
		return ret;
	}

	protected static boolean invariants(KettleElectricitySILModel instance) {
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

	public KettleElectricitySILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

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
				!this.currentHeatingPower.isInitialised()) {
			this.currentIntensity.initialise(0.0);
			this.currentHeatingPower.initialise(
					KettleImplementationI.NORMAL_MODE_POWER);

			if (VERBOSE) {
				StringBuffer sb = new StringBuffer("new consumption: ");
				sb.append(this.currentIntensity.getValue());
				sb.append(" at ");
				sb.append(this.currentIntensity.getTime());
				this.logMessage(sb.toString());
			}

			ret = new Pair<>(2, 0);
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
