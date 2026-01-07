package fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil;

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

import java.util.concurrent.TimeUnit;
import org.shredzone.commons.suncalc.SunTimes;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeter;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SolarPanelEventI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunriseEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunsetEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SolarPanelEventI.Position;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelPowerModel</code> implements the simulation model
 * that computes the current power production of the solar panel.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation model computes the production power of the solar panel as
 * the maximum power of the solar panel times an imported performance
 * coefficient in [0, 1]. The model exports the production power towards the
 * electric meter model.
 * </p>
 * 
 * <ul>
 * <li>Imported events: {@code SunriseEvent}, {@code SunsetEvent}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables:
 *   name = {@code sunIntensityCoef}, type = {@code Double}</li>
 * <li>Exported variables:
 *   name = {@code solarPanelPowerProduction}, type = {@code Double}</li>
 * </ul>
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
 * <p>Created on : 2025-10-07</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(imported = {SunriseEvent.class, SunsetEvent.class})
@ModelImportedVariable(name = "sunIntensityCoef", type = Double.class)
@ModelExportedVariable(name = "solarPanelOutputPower", type = Double.class)
//-----------------------------------------------------------------------------
public class			SolarPanelPowerModel
extends		AtomicHIOA
implements	SunStateManagementI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** single model URI.													*/
	public static final String	URI = "solar-panel-power-model";
	/**	name of the run parameter for the initialisation of the latitude.	*/
	public static final String	LATITUDE_RP_NAME = "LATITUDE";
	/**	name of the run parameter for the initialisation of the longitude.	*/
	public static final String	LONGITUDE_RP_NAME = "LONGITUDE";
	/**	name of the run parameter for the initialisation of the start
	 *  instant.															*/
	public static final String	START_INSTANT_RP_NAME = "START_INSTANT";
	/**	name of the run parameter for the initialisation of the time zone.	*/
	public static final String	ZONE_ID_RP_NAME = "ZONE_ID";
	/**	name of the run parameter for the initialisation of the solar
	 *  panel maximum power.												*/
	public static final String	MAX_POWER_RP_NAME = "MAX_POWER";
	/**	name of the run parameter for the initialisation of the
	 *  computation step.													*/
	public static final String	COMPUTATION_STEP_RP_NAME = "COMPUTATION_STEP";

	/** computation step in hours.											*/
	public double			computationStep;

	/** start time of the simulation in {@code Instant} format.				*/
	protected Instant		startInstant;
	/** time zone of the solar panel.										*/
	protected ZoneId		zoneId;
	/**	latitude of the solar panel in degrees, minutes and seconds.		*/
	protected Position		latitude;
	/**	latitude of the solar panel in degrees, minutes and seconds.		*/
	protected Position		longitude;
	/** maximum power production of the solar panel in
	 *  {@code MeasurementUnit.WATTS}										*/
	protected double		maxPower;

	/** current state, day or night.										*/
	protected SunState		currentState;

	/** the intensity coefficient computed by {@code SunIntensityModel}.	*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>	sunIntensityCoef;
	/** the current power produced by the solar panel computed by this
	 *  model in the power unit used by the electric meter.					*/
	@ExportedVariable(type = Double.class)
	protected Value<Double>	solarPanelOutputPower = new Value<>(this);

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an atomic hybrid input/output model with the given URI (if null,
	 * one will be generated) and to be run by the given simulator using the
	 * given time unit for its clock.
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
	 * @param uri				unique identifier of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation clock.
	 * @param simulationEngine	simulation engine enacting the model.
	 */
	public				SolarPanelPowerModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		)
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunStateManagementI#setState(fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunState)
	 */
	@Override
	public void			setState(SunState s)
	{
		this.currentState = s;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunStateManagementI#setCurrent(java.time.ZonedDateTime)
	 */
	@Override
	public void			setCurrent(ZonedDateTime newCurrent)
	{
		;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		String latitudeName = ModelI.createRunParameterName(this.getURI(),
															LATITUDE_RP_NAME);
		String longitudeName = ModelI.createRunParameterName(this.getURI(),
															LONGITUDE_RP_NAME);
		String startInstantName = ModelI.createRunParameterName(this.getURI(),
														START_INSTANT_RP_NAME);
		String zoneIdName = ModelI.createRunParameterName(this.getURI(),
															ZONE_ID_RP_NAME);
		String maxPowerName = ModelI.createRunParameterName(this.getURI(),
															MAX_POWER_RP_NAME);
		String computationStepName = ModelI.createRunParameterName(this.getURI(),
													COMPUTATION_STEP_RP_NAME);

		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(latitudeName) :
				new MissingRunParameterException(latitudeName);
		assert	simParams.containsKey(longitudeName) :
				new MissingRunParameterException(longitudeName);
		assert	simParams.containsKey(startInstantName) :
				new MissingRunParameterException(startInstantName);
		assert	simParams.containsKey(zoneIdName) :
				new MissingRunParameterException(zoneIdName);
		assert	simParams.containsKey(maxPowerName) :
				new MissingRunParameterException(maxPowerName);
		assert	simParams.containsKey(computationStepName) :
				new MissingRunParameterException(computationStepName);
		
		this.latitude = (Position) simParams.get(latitudeName);
		this.longitude = (Position) simParams.get(longitudeName);
		this.startInstant = (Instant) simParams.get(startInstantName);
		this.zoneId = (ZoneId) simParams.get(zoneIdName);
		this.maxPower = (double) simParams.get(maxPowerName);
		this.computationStep = (double) simParams.get(computationStepName);

		if (DEBUG) {
			this.logMessage("setSimulationRunParameters");
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		if (DEBUG) {
			this.logMessage("initialiseState initialTime " + initialTime);
		}

		ZonedDateTime current =
				ZonedDateTime.ofInstant(this.startInstant, this.zoneId);
		SunTimes st = SunTimes.compute()
				.on(current)
            	.latitude(latitude.getDegree(),
            			  latitude.getMinutes(),
            			  latitude.getSeconds())
            	.longitude(longitude.getDegree(),
            			   longitude.getMinutes(),
            			   longitude.getSeconds())
            	.execute();
		ZonedDateTime sunRiseTime = st.getRise();
		ZonedDateTime sunSetTime = st.getSet();

		if (sunRiseTime.compareTo(sunSetTime) <= 0) {
			// next event is sunrise, hence we are at night
			this.currentState = SunState.NIGHT;
		} else {
			// next event is sunset, hence we are during the day
			this.currentState = SunState.DAY;
		}

		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent = this.currentStateTime.add(this.nextTimeAdvance);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean	useFixpointInitialiseVariables()
	{
		// when several models have dependencies among their variables that
		// forces an order in their initialisation, the fix point algorithm must
		// be used
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		if (DEBUG) {
			this.logMessage("fixpointInitialiseVariables "
							+ this.solarPanelOutputPower.isInitialised());
		}

		// at each call, the method tries to initialise the variables held by
		// the model and return the number of variables that were initialised
		// and the number that could not be initialised (because variables
		// imported from other models they depend upon are not yet initialised)
		int numberOfNewlyInitialisedVariables = 0;
		int numberOfStillNotInitialisedVariables = 0;

		// the variable sunIntensityCoef is the one that this model holds and
		// that must be initialised, but it depends upon the imported variable
		// solarPanelPowerProduction
		if (!this.solarPanelOutputPower.isInitialised()) {
			// if sunIntensityCoef has been initialised, then
			// solarPanelPowerProduction can be initialised
			if (this.sunIntensityCoef.isInitialised()) {
				this.solarPanelOutputPower.initialise(
						this.powerProduction(this.sunIntensityCoef.getValue()));
				// and then the number of newly initialised variable is 1
				numberOfNewlyInitialisedVariables++;
				if (VERBOSE) {
					this.logMessage(
							"fixpointInitialiseVariables "
							+ this.solarPanelOutputPower.getValue());
				}
			} else {
				// otherwise, solarPanelPowerProduction cannot be initialised
				// and the number of variables not initialised by this execution
				// is one
				numberOfStillNotInitialisedVariables++;
			}
		}

		// the two counters are returned and aggregated among the different
		// execution of fixpointInitialiseVariables in the different models
		// if the total numbers gives 0 still not initialised variable, then
		// the fix point has been reached, but if there are still variables not
		// initialised but some have been initialised during the run (i.e.,
		// numberOfNewlyInitialisedVariables > 0) then the method
		// fixpointInitialiseVariables must be rerun on all models until all
		// variables have been initialised
		return new Pair<Integer, Integer>(numberOfNewlyInitialisedVariables,
										  numberOfStillNotInitialisedVariables);
	}

	/**
	 * return the power produced by the solar panel in the power unit used by
	 * the electric meter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sunIntensityCoef	a sun intensity coefficient.
	 * @return					the power produced by the solar panel in the power unit used by the electric meter.
	 */
	protected double		powerProduction(double sunIntensityCoef)
	{
		double powerInWatts = sunIntensityCoef * this.maxPower;
		double ret;
		switch (ElectricMeterImplementationI.POWER_UNIT) {
		case WATTS:
			ret = powerInWatts;
			break;
		case AMPERES:
			ret = powerInWatts/ElectricMeter.TENSION.getData();
			break;
		default:
			throw new NeoSim4JavaException(
									"incorrect measurement unit: "
									+ ElectricMeterImplementationI.POWER_UNIT);
		}
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration timeAdvance()
	{
		if (DEBUG) {
			this.logMessage("timeAdvance " + this.currentState);
		}

		if (this.currentState == null ||
									SunState.NIGHT.equals(this.currentState)) {
			return Duration.INFINITY;
		} else {
			return new Duration(this.computationStep,
								this.getSimulatedTimeUnit());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// update the power production
		double coef =
				this.sunIntensityCoef.evaluateAt(this.getCurrentStateTime());
		double production = this.powerProduction(coef);
		this.solarPanelOutputPower.setNewValue(production,
												   this.getCurrentStateTime());

		if (DEBUG) {
			this.logMessage("userDefinedInternalTransition output power = "
					+ production + " "
					+ ElectricMeterImplementationI.POWER_UNIT
					+ " sunIntensityCoef = " + this.sunIntensityCoef.getValue()
					+ " at "
					+ this.getCurrentStateTime());
		} else if (VERBOSE) {
			this.logMessage("userDefinedInternalTransition output power = "
							+ production + " "
							+ ElectricMeterImplementationI.POWER_UNIT + " at "
							+ this.getCurrentStateTime());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{		
		super.userDefinedExternalTransition(elapsedTime);

		assert	this.currentStoredEvents.size() == 1 :
				new NeoSim4JavaException("currentStoredEvents.size() == 1");

		SolarPanelEventI e =
				(SolarPanelEventI) this.getStoredEventAndReset().remove(0);

		e.executeOn(this);

		// update the power production
		double coef =
				this.sunIntensityCoef.evaluateAt(this.getCurrentStateTime());
		double production = this.powerProduction(coef);
		this.solarPanelOutputPower.setNewValue(production,
											   this.getCurrentStateTime());

		if (DEBUG) {
			this.logMessage("userDefinedInternalTransition output power = "
					+ this.solarPanelOutputPower.getValue() + " "
					+ ElectricMeterImplementationI.POWER_UNIT
					+ " sunIntensityCoef = " + this.sunIntensityCoef.getValue()
					+ " at "
					+ this.getCurrentStateTime());
		} else if (VERBOSE) {
			this.logMessage("userDefinedExternalTransition output power = "
							+ this.solarPanelOutputPower.getValue()
							+ " at " + this.getCurrentStateTime());
		}
	}
}
// -----------------------------------------------------------------------------
