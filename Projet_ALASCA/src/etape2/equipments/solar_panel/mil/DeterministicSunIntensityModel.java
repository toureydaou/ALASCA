package etape2.equipments.solar_panel.mil;

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

import etape2.equipments.solar_panel.mil.events.SolarPanelEventI;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
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
 * The class <code>DeterministicSunIntensityModel</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation model provides a sun intensity coefficient in [0, 1] as
 * the result of a sine from 0 to {@code Math.PI} from sunrise to sunset.
 * The sunrise and sunset times are fixed constants during the whole simulation
 * but can be changed from simulations to simulations. The constants and
 * functions are defined in {@code SolarPanelSimulationConfigurationI}.
 * </p>
 * 
 * <ul>
 * <li>Imported events: {@code SunriseEvent}, {@code SunsetEvent}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables:
 *   name = {@code sunIntensityCoef}, type = {@code Double}</li>
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
 * <p>Created on : 2025-10-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@ModelExternalEvents(imported = {SunriseEvent.class, SunsetEvent.class})
@ModelExportedVariable(name = "sunIntensityCoef", type = Double.class)
//-----------------------------------------------------------------------------
public class			DeterministicSunIntensityModel
extends		AtomicHIOA
implements	SunIntensityModelI
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
	public static final String	URI = "deterministic-sun-intensity-model";

	/** computation step in hours.											*/
	public double			computationStep;

	/** start time of the simulation in {@code Instant} format.				*/
	protected Instant		startInstant;
	/** time zone of the solar panel.										*/
	protected ZoneId		zoneId;

	/** current time, in {@code ZonedTimeDate} format.						*/
	protected ZonedDateTime current;
	/** current state, day or night.										*/
	protected SunState		currentState;

	/** the intensity coefficient computed by this model.					*/
	@ExportedVariable(type = Double.class)
	protected final Value<Double> sunIntensityCoef = new Value<Double>(this) ;

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
	public				DeterministicSunIntensityModel(
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
	 * @see etape2.equipments.solar_panel.mil.SunStateManagementI#setState(etape2.equipments.solar_panel.mil.SunState)
	 */
	@Override
	public void			setState(SunState s)
	{
		this.currentState = s;
	}

	/**
	 * @see etape2.equipments.solar_panel.mil.SunStateManagementI#setCurrent(java.time.ZonedDateTime)
	 */
	@Override
	public void			setCurrent(ZonedDateTime newCurrent)
	{
		this.current = newCurrent;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		String startInstantName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.START_INSTANT_RP_NAME);
		String zoneIdName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.ZONE_ID_RP_NAME);
		String computationStepName =
				ModelI.createRunParameterName(
									this.getURI(),
									SunIntensityModelI.COMPUTATION_STEP_RP_NAME);

		assert	simParams != null :
				new MissingRunParameterException("simParams != null");
		assert	simParams.containsKey(startInstantName) :
				new MissingRunParameterException(startInstantName);
		assert	simParams.containsKey(zoneIdName) :
				new MissingRunParameterException(zoneIdName);
		assert	simParams.containsKey(computationStepName) :
				new MissingRunParameterException(computationStepName);

		this.startInstant = (Instant) simParams.get(startInstantName);
		this.zoneId = (ZoneId) simParams.get(zoneIdName);
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
			this.logMessage(
				"DeterministicSunIntensityModel::initialiseState initialTime "
				+ initialTime);
		}

		this.current =
				ZonedDateTime.ofInstant(this.startInstant, this.zoneId);
		ZonedDateTime sunRiseTime =
				ZonedDateTime.ofInstant(
						SolarPanelSimulationConfigurationI.getSunrise(
														this.startInstant),
						SolarPanelSimulationConfigurationI.ZONE);
		ZonedDateTime sunSetTime =
				ZonedDateTime.ofInstant(
						SolarPanelSimulationConfigurationI.getSunset(
														this.startInstant),
						SolarPanelSimulationConfigurationI.ZONE);

		if (DEBUG) {
			this.logMessage("initialiseState sunrise = " + sunRiseTime);
			this.logMessage("initialiseState current = " + current);
			this.logMessage("initialiseState sunset = " + sunSetTime);
		}

		if (current.isAfter(sunRiseTime) && current.isBefore(sunSetTime)) {
			// next event is sunrise, hence we are at night
			this.currentState = SunState.DAY;
		} else {
			// next event is sunset, hence we are during the day
			this.currentState = SunState.NIGHT;
		}

		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent = this.currentStateTime.add(this.nextTimeAdvance);
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
		if (DEBUG) {
			this.logMessage(
					"fixpointInitialiseVariables "
					+ this.sunIntensityCoef.isInitialised());
		}

		// at each call, the method tries to initialise the variables held by
		// the model and return the number of variables that were initialised
		// and the number that could not be initialised (because variables
		// imported from other models they depend upon are not yet initialised)
		int numberOfNewlyInitialisedVariables = 0;
		int numberOfStillNotInitialisedVariables = 0;
		
		if (!this.sunIntensityCoef.isInitialised()) {
			this.sunIntensityCoef.initialise(this.computeNewIntensity());
			numberOfNewlyInitialisedVariables++;
			if (DEBUG) {
				this.logMessage("fixpointInitialiseVariables "
								+ this.sunIntensityCoef.getValue());
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
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		if (DEBUG) {
			this.logMessage("timeAdvance " + this.currentState);
		}

		if (this.currentState == null || this.currentState.equals(SunState.NIGHT)) {
			return Duration.INFINITY;
		} else {
			return new Duration(this.computationStep, this.getSimulatedTimeUnit());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		long stepInSeconds = (long) (computationStep * 3600.0);
		this.current = this.current.plusSeconds(stepInSeconds);
		this.sunIntensityCoef.setNewValue(this.computeNewIntensity(),
										  this.getCurrentStateTime());

		if (VERBOSE) {
			this.logMessage(
				"userDefinedInternalTransition new sunIntensityCoef = "
				+ this.sunIntensityCoef.getValue() + " at "
				+ this.getCurrentStateTime());
		}
	}

	/**
	 * return the new sun intensity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the new sun intensity.
	 */
	protected double	computeNewIntensity()
	{
		return SolarPanelSimulationConfigurationI.
					deterministicSunIntensityCoef(this.current.toInstant());
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{		
		super.userDefinedExternalTransition(elapsedTime);

		assert	this.currentStoredEvents.size() == 1 :
				new AssertionError("currentStoredEvents.size() == 1");

		SolarPanelEventI e =
				(SolarPanelEventI) this.getStoredEventAndReset().remove(0);

		if (VERBOSE) {
			this.logMessage("userDefinedExternalTransition " + e);
		}

		e.executeOn(this);
	}
}
// -----------------------------------------------------------------------------
