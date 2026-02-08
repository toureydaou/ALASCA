package etape2.equipments.kettle.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape2.GlobalReportI;
import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
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

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleTemperatureModel</code> simulates the water
 * temperature evolution in the kettle (water heater) tank.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The derivative of temperature is computed as:
 * dT/dt = (HeatingPower) / (WaterMass * Cp) - HeatLoss
 * where WaterMass = 200 kg (200L tank, fixed).
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
@ModelExternalEvents(imported = {
		SwitchOnKettle.class, SwitchOffKettle.class,
		HeatKettle.class, DoNotHeatKettle.class
})
@ModelImportedVariable(name = "currentHeatingPower", type = Double.class)
// -----------------------------------------------------------------------------
public class KettleTemperatureModel
extends		AtomicHIOA
implements	KettleOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	public static final String URI =
			KettleTemperatureModel.class.getSimpleName();

	public static boolean VERBOSE = true;
	public static boolean DEBUG = true;

	/** Water specific heat capacity in J/kg/°C. */
	protected static double WATER_SPECIFIC_HEAT_CAPACITY = 4200.0;

	/** Fixed water mass in kg (200L tank). */
	protected static double WATER_MASS = 200.0;

	/** Insulation factor. Higher = better insulation. */
	protected static double INSULATION_CONSTANT = 1000.0;

	/** Ambient temperature in °C. */
	protected static double AMBIENT_TEMPERATURE = 20.0;

	/** Integration step in hours (1 minute). */
	protected static double STEP = 60.0 / 3600.0;

	protected KettleState currentState = KettleState.ON;
	protected final Duration integrationStep;

	protected double temperatureAcc;
	protected Time start;
	protected double meanTemperature;

	// -------------------------------------------------------------------------
	// HIOA variables
	// -------------------------------------------------------------------------

	@ImportedVariable(type = Double.class)
	protected Value<Double> currentHeatingPower;

	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double> currentWaterTemperature =
			new DerivableValue<>(this);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public KettleTemperatureModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);
		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Physics computations
	// -------------------------------------------------------------------------

	protected double computeDerivatives(Double currentTemp)
	{
		double heatingContribution = 0.0;

		if (this.currentState == KettleState.HEATING) {
			double power = this.currentHeatingPower.getValue();
			// dT/dt = P / (m * Cp) * 60.0
			heatingContribution =
					power / (WATER_MASS * WATER_SPECIFIC_HEAT_CAPACITY) * 60.0;
		}

		// Newton cooling: (T_ambient - T_water) / insulation
		double coolingContribution =
				(AMBIENT_TEMPERATURE - currentTemp) / INSULATION_CONSTANT;

		return heatingContribution + coolingContribution;
	}

	protected double computeNewTemperature(double deltaT)
	{
		Time t = this.currentWaterTemperature.getTime();
		double oldTemp = this.currentWaterTemperature.evaluateAt(t);
		double derivative =
				this.currentWaterTemperature.getFirstDerivative();

		// T(t+dt) = T(t) + T'(t) * dt * 3600
		double newTemp = oldTemp + derivative * deltaT * 3600;

		return newTemp;
	}

	// -------------------------------------------------------------------------
	// KettleOperationI implementation
	// -------------------------------------------------------------------------

	@Override
	public void setState(KettleState s)
	{
		this.currentState = s;
	}

	@Override
	public KettleState getState()
	{
		return this.currentState;
	}

	@Override
	public void setMode(KettleMode m)
	{
		// Not used in temperature model
	}

	@Override
	public KettleMode getMode()
	{
		return null;
	}

	@Override
	public void setCurrentHeatingPower(double newPower, Time t)
	{
		// Not used in temperature model - heating power is imported
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);
		this.start = initialTime;
		this.temperatureAcc = 0.0;
		if (VERBOSE) {
			this.logMessage("Simulation starts. Water Temp: "
					+ AMBIENT_TEMPERATURE);
		}
	}

	@Override
	public boolean useFixpointInitialiseVariables()
	{
		return true;
	}

	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables()
	{
		int justInitialised = 0;
		int notInitialisedYet = 0;

		if (!this.currentHeatingPower.isInitialised()) {
			notInitialisedYet++;
		} else {
			if (!this.currentWaterTemperature.isInitialised()) {
				double derivative =
						this.computeDerivatives(AMBIENT_TEMPERATURE);
				this.currentWaterTemperature.initialise(
						AMBIENT_TEMPERATURE, derivative);
				justInitialised++;
			}
		}

		return new Pair<>(justInitialised, notInitialisedYet);
	}

	@Override
	public ArrayList<EventI> output()
	{
		return null;
	}

	@Override
	public Duration timeAdvance()
	{
		return this.integrationStep;
	}

	@Override
	public void userDefinedInternalTransition(Duration elapsedTime)
	{
		double newTemp =
				this.computeNewTemperature(
						elapsedTime.getSimulatedDuration());
		double newDerivative = this.computeDerivatives(newTemp);
		this.currentWaterTemperature.setNewValue(
				newTemp, newDerivative,
				new Time(this.getCurrentStateTime().getSimulatedTime(),
						this.getSimulatedTimeUnit()));

		this.temperatureAcc +=
				newTemp * elapsedTime.getSimulatedDuration();

		if (VERBOSE && this.currentState == KettleState.HEATING) {
			this.logMessage("Temp update: "
					+ String.format("%.2f", newTemp) + "°C");
		}

		super.userDefinedInternalTransition(elapsedTime);
	}

	@Override
	public void userDefinedExternalTransition(Duration elapsedTime)
	{
		double newTemp =
				this.computeNewTemperature(
						elapsedTime.getSimulatedDuration());

		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert currentEvents != null && currentEvents.size() == 1;
		Event ce = (Event) currentEvents.get(0);

		ce.executeOn(this);

		double newDerivative = this.computeDerivatives(newTemp);
		this.currentWaterTemperature.setNewValue(
				newTemp, newDerivative,
				new Time(
						this.getCurrentStateTime().getSimulatedTime()
								+ elapsedTime.getSimulatedDuration(),
						this.getSimulatedTimeUnit()));

		super.userDefinedExternalTransition(elapsedTime);

		if (VERBOSE) {
			this.logMessage("Event received: " + ce.eventAsString()
					+ " -> State: " + this.currentState);
		}
	}

	@Override
	public void endSimulation(Time endTime)
	{
		double duration =
				endTime.subtract(this.start).getSimulatedDuration();
		if (duration > 0.0) {
			this.meanTemperature = this.temperatureAcc / duration;
		} else {
			this.meanTemperature = AMBIENT_TEMPERATURE;
		}

		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Simulation report
	// -------------------------------------------------------------------------

	public static class KettleTemperatureReport
	implements SimulationReportI, GlobalReportI
	{
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double meanTemperature;

		public KettleTemperatureReport(
			String modelURI,
			double meanTemperature
			)
		{
			super();
			this.modelURI = modelURI;
			this.meanTemperature = meanTemperature;
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
			ret.append("mean temperature = ");
			ret.append(this.meanTemperature);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
	}

	@Override
	public SimulationReportI getFinalReport()
	{
		return new KettleTemperatureReport(
				this.getURI(), this.meanTemperature);
	}
}
// -----------------------------------------------------------------------------
