package etape2.equipments.kettle.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape2.GlobalReportI;
import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.KettleEventI;
import etape2.equipments.kettle.mil.events.SetEcoModeKettle;
import etape2.equipments.kettle.mil.events.SetMaxModeKettle;
import etape2.equipments.kettle.mil.events.SetNormalModeKettle;
import etape2.equipments.kettle.mil.events.SetPowerKettle;
import etape2.equipments.kettle.mil.events.SetSuspendedModeKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
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
 * The class <code>KettleElectricityModel</code> defines a simulation
 * model for the electricity consumption of the kettle (water heater).
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The electric power consumption depends upon the state and the current
 * power mode: OFF => 0.0, ON => mode-dependent consumption,
 * HEATING => currentHeatingPower / voltage.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
@ModelExternalEvents(imported = {
		SwitchOnKettle.class, SwitchOffKettle.class,
		SetPowerKettle.class, HeatKettle.class, DoNotHeatKettle.class,
		SetEcoModeKettle.class, SetMaxModeKettle.class,
		SetSuspendedModeKettle.class, SetNormalModeKettle.class
})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
@ModelExportedVariable(name = "currentHeatingPower", type = Double.class)
// -----------------------------------------------------------------------------
public class KettleElectricityModel
extends		AtomicHIOA
implements	KettleOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	public static final String URI =
			KettleElectricityModel.class.getSimpleName();
	public static boolean VERBOSE = true;
	public static boolean DEBUG = false;

	protected KettleState currentState = KettleState.OFF;
	protected KettleMode currentMode = KettleMode.NORMAL;
	protected boolean consumptionHasChanged = false;

	protected double suspendedModeConsumption;
	protected double ecoModeConsumption;
	protected double normalModeConsumption;
	protected double maxModeConsumption;

	protected double totalConsumption;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentHeatingPower = new Value<Double>(this);
	@ExportedVariable(type = Double.class)
	protected final Value<Double> currentIntensity = new Value<Double>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	protected static boolean implementationInvariants(
		KettleElectricityModel instance
		)
	{
		assert instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentState != null,
				KettleElectricityModel.class, instance,
				"currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.totalConsumption >= 0.0,
				KettleElectricityModel.class, instance,
				"totalConsumption >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentHeatingPower.isInitialised() ||
					instance.currentHeatingPower.getValue() >= 0.0,
				KettleElectricityModel.class, instance,
				"!currentHeatingPower.isInitialised() || "
				+ "currentHeatingPower.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.currentIntensity.isInitialised() ||
					instance.currentIntensity.getValue() >= 0.0,
				KettleElectricityModel.class, instance,
				"!currentIntensity.isInitialised() || "
				+ "currentIntensity.getValue() >= 0.0");
		return ret;
	}

	public static boolean staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				KettleElectricityModel.class,
				"URI != null && !URI.isEmpty()");
		return ret;
	}

	protected static boolean invariants(KettleElectricityModel instance)
	{
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

	public KettleElectricityModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.suspendedModeConsumption =
				KettleImplementationI.SUSPEND_MODE_POWER;
		this.ecoModeConsumption = KettleImplementationI.ECO_MODE_POWER;
		this.normalModeConsumption = KettleImplementationI.NORMAL_MODE_POWER;
		this.maxModeConsumption = KettleImplementationI.MAX_MODE_POWER;

		this.getSimulationEngine().setLogger(new StandardLogger());

		assert KettleElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleElectricityModel."
						+ "implementationInvariants(this)");
		assert KettleElectricityModel.invariants(this) :
				new NeoSim4JavaException(
						"KettleElectricityModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void setState(KettleState s)
	{
		KettleState old = this.currentState;
		this.currentState = s;
		if (old != s) {
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public KettleState getState()
	{
		return this.currentState;
	}

	@Override
	public void setMode(KettleMode m)
	{
		KettleMode old = this.currentMode;
		this.currentMode = m;
		if (old != m) {
			this.consumptionHasChanged = true;
		}
	}

	@Override
	public KettleMode getMode()
	{
		return this.currentMode;
	}

	@Override
	public void setCurrentHeatingPower(double newPower, Time t)
	{
		assert newPower >= 0.0 &&
				newPower <= KettleImplementationI.MAX_POWER_LEVEL :
				new NeoSim4JavaException(
						"Precondition violation: newPower >= 0.0 && "
						+ "newPower <= MAX_POWER_LEVEL, but newPower = "
						+ newPower);

		double oldPower = this.currentHeatingPower.getValue();
		this.currentHeatingPower.setNewValue(newPower, t);
		if (newPower != oldPower) {
			this.consumptionHasChanged = true;
		}

		assert KettleElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleElectricityModel."
						+ "implementationInvariants(this)");
	}

	public void toggleConsumptionHasChanged()
	{
		this.consumptionHasChanged = !this.consumptionHasChanged;
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		this.currentState = KettleState.OFF;
		this.currentMode = KettleMode.NORMAL;
		this.consumptionHasChanged = false;
		this.totalConsumption = 0.0;

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}

		assert KettleElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleElectricityModel."
						+ "implementationInvariants(this)");
	}

	@Override
	public boolean useFixpointInitialiseVariables()
	{
		return true;
	}

	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables()
	{
		Pair<Integer, Integer> ret = null;

		if (!this.currentIntensity.isInitialised() ||
				!this.currentHeatingPower.isInitialised()) {
			this.currentIntensity.initialise(0.0);
			this.currentHeatingPower.initialise(
					KettleImplementationI.NORMAL_MODE_POWER);

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

		assert KettleElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleElectricityModel."
						+ "implementationInvariants(this)");

		return ret;
	}

	@Override
	public ArrayList<EventI> output()
	{
		return null;
	}

	@Override
	public Duration timeAdvance()
	{
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
	public void userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		Time t = this.getCurrentStateTime();

		if (this.currentState == KettleState.ON) {
			switch (this.currentMode) {
			case NORMAL:
				this.currentIntensity.setNewValue(
						this.normalModeConsumption /
								KettleImplementationI.MACHINE_VOLTAGE, t);
				break;
			case ECO:
				this.currentIntensity.setNewValue(
						this.ecoModeConsumption /
								KettleImplementationI.MACHINE_VOLTAGE, t);
				break;
			case MAX:
				this.currentIntensity.setNewValue(
						this.maxModeConsumption /
								KettleImplementationI.MACHINE_VOLTAGE, t);
				break;
			case SUSPEND:
				this.currentIntensity.setNewValue(
						this.suspendedModeConsumption /
								KettleImplementationI.MACHINE_VOLTAGE, t);
				break;
			}
		} else if (this.currentState == KettleState.HEATING) {
			this.currentIntensity.setNewValue(
					this.currentHeatingPower.getValue() /
							KettleImplementationI.MACHINE_VOLTAGE, t);
		} else {
			assert this.currentState == KettleState.OFF;
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

		assert KettleElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleElectricityModel."
						+ "implementationInvariants(this)");
	}

	@Override
	public void userDefinedExternalTransition(Duration elapsedTime)
	{
		super.userDefinedExternalTransition(elapsedTime);

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert ce instanceof KettleEventI;

		this.totalConsumption += Electricity.computeConsumption(
				elapsedTime,
				KettleImplementationI.MACHINE_VOLTAGE *
						this.currentIntensity.getValue());

		if (VERBOSE) {
			StringBuffer sb =
					new StringBuffer("execute the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		ce.executeOn(this);

		assert KettleElectricityModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"KettleElectricityModel."
						+ "implementationInvariants(this)");
	}

	@Override
	public void endSimulation(Time endTime)
	{
		Duration d = endTime.subtract(this.getCurrentStateTime());
		this.totalConsumption += Electricity.computeConsumption(
				d,
				KettleImplementationI.MACHINE_VOLTAGE *
						this.currentIntensity.getValue());

		if (VERBOSE) {
			this.logMessage("simulation ends.");
			this.logMessage(
					new KettleElectricityReport(
							this.uri, this.totalConsumption).printout(" "));
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	public static class KettleElectricityReport
	implements SimulationReportI, GlobalReportI
	{
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double totalConsumption;

		public KettleElectricityReport(
			String modelURI,
			double totalConsumption
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
		}

		@Override
		public String getModelURI()
		{
			return this.modelURI;
		}

		@Override
		public String printout(String indent)
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

	@Override
	public SimulationReportI getFinalReport()
	{
		return new KettleElectricityReport(
				this.getURI(), this.totalConsumption);
	}
}
// -----------------------------------------------------------------------------
