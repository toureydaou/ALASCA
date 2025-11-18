package etape2;

import java.time.Instant;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import etape2.GlobalCoupledModel.GlobalReport;
import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.Heat;
import etape2.equipments.coffeemachine.mil.events.SetPowerCoffeeMachine.PowerValue;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import tests_utils.SimulationTestStep;
import tests_utils.TestScenario;

public class			RunGlobalSimulation
{
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= GlobalSimulationConfigurationI.staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		staticInvariants();
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		try {
			// -----------------------------------------------------------------
			// Atomic models
			// -----------------------------------------------------------------

			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			atomicModelDescriptors.put(
					HairDryerElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							HairDryerElectricityModel.class,
							HairDryerElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					HairDryerSimpleUserModel.URI,
					AtomicModelDescriptor.create(
							HairDryerSimpleUserModel.class,
							HairDryerSimpleUserModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// Heater models

			atomicModelDescriptors.put(
					HeaterElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							HeaterElectricityModel.class,
							HeaterElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					HeaterTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							HeaterTemperatureModel.class,
							HeaterTemperatureModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					HeaterUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							HeaterUnitTesterModel.class,
							HeaterUnitTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			
			atomicModelDescriptors.put(
					BatteriesPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							BatteriesPowerModel.class,
							BatteriesPowerModel.URI,
							BatteriesSimulationConfiguration.TIME_UNIT,
							null));

			// Solar panel models

			String sunRiseAndSetURI = null;
			if (SolarPanelSimulationConfigurationI.USE_ASTRONOMICAL_MODEL) {
				// AstronomicalSunRiseAndSetModel is an atomic event scheduling
				// model, so needs an AtomicModelDescriptor
				sunRiseAndSetURI = AstronomicalSunRiseAndSetModel.URI;
				atomicModelDescriptors.put(
					AstronomicalSunRiseAndSetModel.URI,
					AtomicModelDescriptor.create(
							AstronomicalSunRiseAndSetModel.class,
							AstronomicalSunRiseAndSetModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null));
			} else {
				// DeterministicSunRiseAndSetModel is an atomic event scheduling
				// model, so needs an AtomicModelDescriptor
				sunRiseAndSetURI = DeterministicSunRiseAndSetModel.URI;
				atomicModelDescriptors.put(
					DeterministicSunRiseAndSetModel.URI,
					AtomicModelDescriptor.create(
							DeterministicSunRiseAndSetModel.class,
							DeterministicSunRiseAndSetModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null));
			}
			String sunIntensityModelURI = null;
			if (SolarPanelSimulationConfigurationI.
										USE_STOCHASTIC_SUN_INTENSITY_MODEL) {
				// StochasticSunIntensityModel is an atomic HIOA model, so needs
				// an AtomicHIOA_Descriptor
				sunIntensityModelURI = StochasticSunIntensityModel.URI;
				atomicModelDescriptors.put(
					StochasticSunIntensityModel.URI,
					AtomicHIOA_Descriptor.create(
							StochasticSunIntensityModel.class,
							StochasticSunIntensityModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null));
			} else {
				// DeterministicSunIntensityModel is an atomic HIOA model, so
				// needs an AtomicHIOA_Descriptor
				sunIntensityModelURI = DeterministicSunIntensityModel.URI;
				atomicModelDescriptors.put(
					DeterministicSunIntensityModel.URI,
					AtomicHIOA_Descriptor.create(
							DeterministicSunIntensityModel.class,
							DeterministicSunIntensityModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null));
			}
			// SolarPanelPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					SolarPanelPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							SolarPanelPowerModel.class,
							SolarPanelPowerModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null));

			
			atomicModelDescriptors.put(
					GeneratorFuelModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorFuelModel.class,
							GeneratorFuelModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
			
			atomicModelDescriptors.put(
					GeneratorPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorPowerModel.class,
							GeneratorPowerModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
			
			atomicModelDescriptors.put(
					GeneratorGlobalTesterModel.URI,
					AtomicModelDescriptor.create(
							GeneratorGlobalTesterModel.class,
							GeneratorGlobalTesterModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null));


			atomicModelDescriptors.put(
					ElectricMeterElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							ElectricMeterElectricityModel.class,
							ElectricMeterElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HairDryerElectricityModel.URI);
			submodels.add(HairDryerSimpleUserModel.URI);
			submodels.add(HeaterElectricityModel.URI);
			submodels.add(HeaterTemperatureModel.URI);
			submodels.add(ExternalTemperatureModel.URI);
			submodels.add(HeaterUnitTesterModel.URI);
			submodels.add(BatteriesPowerModel.URI);
			submodels.add(sunRiseAndSetURI);
			submodels.add(sunIntensityModelURI);
			submodels.add(SolarPanelPowerModel.URI);
			submodels.add(GeneratorFuelModel.URI);
			submodels.add(GeneratorPowerModel.URI);
			submodels.add(GeneratorGlobalTesterModel.URI);
			submodels.add(ElectricMeterElectricityModel.URI);

			// -----------------------------------------------------------------
			// Event exchanging connections
			// -----------------------------------------------------------------

			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			// Hair dryer events

			connections.put(
				new EventSource(HairDryerSimpleUserModel.URI,
								SwitchOnHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerElectricityModel.URI,
								  SwitchOnHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerSimpleUserModel.URI,
								SwitchOffHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerElectricityModel.URI,
								  SwitchOffHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerSimpleUserModel.URI,
								SetHighHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerElectricityModel.URI,
								  SetHighHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerSimpleUserModel.URI,
								SetLowHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerElectricityModel.URI,
								  SetLowHairDryer.class)
				});

			// Heater events

			connections.put(
				new EventSource(HeaterUnitTesterModel.URI,
								SetPowerHeater.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI,
								  SetPowerHeater.class)
				});
			connections.put(
				new EventSource(HeaterUnitTesterModel.URI,
								SwitchOnHeater.class),
				new EventSink[] {
						new EventSink(HeaterElectricityModel.URI,
									  SwitchOnHeater.class)
				});
			connections.put(
				new EventSource(HeaterUnitTesterModel.URI,
								SwitchOffHeater.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI,
								  SwitchOffHeater.class),
					new EventSink(HeaterTemperatureModel.URI,
								  SwitchOffHeater.class)
				});
			connections.put(
				new EventSource(HeaterUnitTesterModel.URI, Heat.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI, Heat.class),
					new EventSink(HeaterTemperatureModel.URI, Heat.class)
				});
			connections.put(
				new EventSource(HeaterUnitTesterModel.URI, DoNotHeat.class),
				new EventSink[] {
					new EventSink(HeaterElectricityModel.URI, DoNotHeat.class),
					new EventSink(HeaterTemperatureModel.URI, DoNotHeat.class)
				});

			// Batteries events

			connections.put(
					new EventSource(ElectricMeterElectricityModel.URI,
									BatteriesRequiredPowerChanged.class),
					new EventSink[] {
						new EventSink(BatteriesPowerModel.URI,
									  BatteriesRequiredPowerChanged.class)
					});

			// Solar panel events

			connections.put(
				new EventSource(sunRiseAndSetURI, SunriseEvent.class),
				new EventSink[] {
					new EventSink(sunIntensityModelURI, SunriseEvent.class),
					new EventSink(SolarPanelPowerModel.URI, SunriseEvent.class)
				});
			connections.put(
				new EventSource(sunRiseAndSetURI, SunsetEvent.class),
				new EventSink[] {
					new EventSink(sunIntensityModelURI, SunsetEvent.class),
					new EventSink(SolarPanelPowerModel.URI, SunsetEvent.class)
				});

			// Generator events

			connections.put(
				new EventSource(GeneratorGlobalTesterModel.URI, Start.class),
				new EventSink[] {
					new EventSink(GeneratorFuelModel.URI, Start.class),
					new EventSink(GeneratorPowerModel.URI, Start.class)
				});
			connections.put(
				new EventSource(GeneratorGlobalTesterModel.URI, Stop.class),
				new EventSink[] {
					new EventSink(GeneratorFuelModel.URI, Stop.class),
					new EventSink(GeneratorPowerModel.URI, Stop.class)
				});
			connections.put(
				new EventSource(GeneratorGlobalTesterModel.URI, Refill.class),
				new EventSink[] {
					new EventSink(GeneratorFuelModel.URI, Refill.class)
				});
			connections.put(
				new EventSource(ElectricMeterElectricityModel.URI,
								GeneratorRequiredPowerChanged.class),
				new EventSink[] {
					new EventSink(GeneratorPowerModel.URI,
								  GeneratorRequiredPowerChanged.class)
				});

			connections.put(
				new EventSource(GeneratorFuelModel.URI, TankEmpty.class),
				new EventSink[] {
					new EventSink(GeneratorPowerModel.URI, TankEmpty.class)
				});
			connections.put(
				new EventSource(GeneratorFuelModel.URI, TankNoLongerEmpty.class),
				new EventSink[] {
					new EventSink(GeneratorPowerModel.URI,
								  TankNoLongerEmpty.class)
				});

			connections.put(
				new EventSource(GeneratorPowerModel.URI,
								GeneratorRequiredPowerChanged.class),
				new EventSink[] {
					new EventSink(GeneratorFuelModel.URI,
								  GeneratorRequiredPowerChanged.class)
				});

			// -----------------------------------------------------------------
			// Variable bindings
			// -----------------------------------------------------------------

			Map<VariableSource,VariableSink[]> bindings =
					new HashMap<VariableSource,VariableSink[]>();

			// Bindings among heater models

			bindings.put(
				new VariableSource("externalTemperature", Double.class,
								   ExternalTemperatureModel.URI),
				new VariableSink[] {
					new VariableSink("externalTemperature", Double.class,
									 HeaterTemperatureModel.URI)
				});
			bindings.put(
				new VariableSource("currentHeatingPower", Double.class,
								   HeaterElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentHeatingPower", Double.class,
									 HeaterTemperatureModel.URI)
				});

			// Bindings among solar panel models

			bindings.put(
				new VariableSource("sunIntensityCoef", Double.class,
								   sunIntensityModelURI),
				new VariableSink[] {
					new VariableSink("sunIntensityCoef", Double.class,
									 SolarPanelPowerModel.URI)
				});

			// Bindings among generator models

			bindings.put(
				new VariableSource("generatorOutputPower", Double.class,
								   GeneratorPowerModel.URI),
				new VariableSink[] {
					new VariableSink("generatorOutputPower", Double.class,
									 ElectricMeterElectricityModel.URI),
					new VariableSink("generatorOutputPower", Double.class,
									 GeneratorFuelModel.URI)
				});
			bindings.put(
				new VariableSource("generatorRequiredPower", Double.class,
								   ElectricMeterElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("generatorRequiredPower", Double.class,
									 GeneratorPowerModel.URI)
				});

			// Bindings among appliances and power production units models and
			// the electric meter model

			bindings.put(
				new VariableSource("batteriesOutputPower", Double.class,
								   BatteriesPowerModel.URI),
				new VariableSink[] {
					new VariableSink("batteriesOutputPower", Double.class,
									 ElectricMeterElectricityModel.URI)
				});
			bindings.put(
				new VariableSource("batteriesInputPower", Double.class,
								   BatteriesPowerModel.URI),
				new VariableSink[] {
					new VariableSink("batteriesInputPower", Double.class,
									 ElectricMeterElectricityModel.URI)
				});
			bindings.put(
				new VariableSource("batteriesRequiredPower", Double.class,
								   ElectricMeterElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("batteriesRequiredPower", Double.class,
									 BatteriesPowerModel.URI)
				});

			bindings.put(
				new VariableSource("solarPanelOutputPower", Double.class,
								   SolarPanelPowerModel.URI),
				new VariableSink[] {
					new VariableSink("solarPanelOutputPower", Double.class,
									 ElectricMeterElectricityModel.URI)
				});

			bindings.put(
				new VariableSource("currentIntensity", Double.class,
								   HairDryerElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentIntensity", Double.class,
									 "currentHairDryerIntensity", Double.class,
									 ElectricMeterElectricityModel.URI)
				});
			bindings.put(
				new VariableSource("currentIntensity", Double.class,
								   HeaterElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentIntensity", Double.class,
									 "currentHeaterIntensity", Double.class,
									 ElectricMeterElectricityModel.URI)
				});

			
			coupledModelDescriptors.put(
					GlobalCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							GlobalCoupledModel.class,
							GlobalCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							GlobalCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							GlobalSimulationConfigurationI.TIME_UNIT);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();

			// -----------------------------------------------------------------
			// Simulation run parameters
			// -----------------------------------------------------------------

			Map<String,Object> simParams = new HashMap<>();

			// run parameters for hair dryer models

			simParams.put(
				ModelI.createRunParameterName(
					HairDryerElectricityModel.URI,
					HairDryerElectricityModel.LOW_MODE_CONSUMPTION_RPNAME),
				660.0);
			simParams.put(
				ModelI.createRunParameterName(
					HairDryerElectricityModel.URI,
					HairDryerElectricityModel.HIGH_MODE_CONSUMPTION_RPNAME),
				1320.0);
			simParams.put(
				ModelI.createRunParameterName(
					HairDryerSimpleUserModel.URI,
					HairDryerSimpleUserModel.MEAN_STEP_RPNAME),
				0.05);
			simParams.put(
				ModelI.createRunParameterName(
					HairDryerSimpleUserModel.URI,
					HairDryerSimpleUserModel.MEAN_DELAY_RPNAME),
				2.0);

			// run parameters for solar panel models

			simParams.put(
				ModelI.createRunParameterName(
					sunRiseAndSetURI,
					SunRiseAndSetModelI.LATITUDE_RP_NAME),
				SolarPanelSimulationConfigurationI.LATITUDE);
			simParams.put(
				ModelI.createRunParameterName(
					sunRiseAndSetURI,
					SunRiseAndSetModelI.LONGITUDE_RP_NAME),
				SolarPanelSimulationConfigurationI.LONGITUDE);
			simParams.put(
				ModelI.createRunParameterName(
					sunRiseAndSetURI,
					SunRiseAndSetModelI.START_INSTANT_RP_NAME),
				GlobalSimulationConfigurationI.START_INSTANT);
			simParams.put(
				ModelI.createRunParameterName(
					sunRiseAndSetURI,
					SunRiseAndSetModelI.ZONE_ID_RP_NAME),
				SolarPanelSimulationConfigurationI.ZONE);

			simParams.put(
				ModelI.createRunParameterName(
					sunIntensityModelURI,
					SunIntensityModelI.LATITUDE_RP_NAME),
				SolarPanelSimulationConfigurationI.LATITUDE);
			simParams.put(
				ModelI.createRunParameterName(
					sunIntensityModelURI,
					SunIntensityModelI.LONGITUDE_RP_NAME),
				SolarPanelSimulationConfigurationI.LONGITUDE);
			simParams.put(
				ModelI.createRunParameterName(
					sunIntensityModelURI,
					SunIntensityModelI.START_INSTANT_RP_NAME),
				GlobalSimulationConfigurationI.START_INSTANT);
			simParams.put(
				ModelI.createRunParameterName(
					sunIntensityModelURI,
					SunIntensityModelI.ZONE_ID_RP_NAME),
				SolarPanelSimulationConfigurationI.ZONE);
			simParams.put(
				ModelI.createRunParameterName(
					sunIntensityModelURI,
					SunIntensityModelI.SLOPE_RP_NAME),
				SolarPanelSimulationConfigurationI.SLOPE);
			simParams.put(
				ModelI.createRunParameterName(
					sunIntensityModelURI,
					SunIntensityModelI.ORIENTATION_RP_NAME),
				SolarPanelSimulationConfigurationI.ORIENTATION);
			simParams.put(
				ModelI.createRunParameterName(
					sunIntensityModelURI,
					SunIntensityModelI.COMPUTATION_STEP_RP_NAME),
				0.5);

			simParams.put(
				ModelI.createRunParameterName(
					SolarPanelPowerModel.URI,
					SolarPanelPowerModel.LATITUDE_RP_NAME),
				SolarPanelSimulationConfigurationI.LATITUDE);
			simParams.put(
				ModelI.createRunParameterName(
					SolarPanelPowerModel.URI,
					SolarPanelPowerModel.LONGITUDE_RP_NAME),
				SolarPanelSimulationConfigurationI.LONGITUDE);
			simParams.put(
				ModelI.createRunParameterName(
					SolarPanelPowerModel.URI,
					SolarPanelPowerModel.START_INSTANT_RP_NAME),
				GlobalSimulationConfigurationI.START_INSTANT);
			simParams.put(
				ModelI.createRunParameterName(
					SolarPanelPowerModel.URI,
					SolarPanelPowerModel.ZONE_ID_RP_NAME),
				SolarPanelSimulationConfigurationI.ZONE);
			simParams.put(
				ModelI.createRunParameterName(
					SolarPanelPowerModel.URI,
					SolarPanelPowerModel.MAX_POWER_RP_NAME),
				SolarPanelSimulationConfigurationI.NB_SQUARE_METERS *
								SolarPanel.CAPACITY_PER_SQUARE_METER.getData());
			simParams.put(
				ModelI.createRunParameterName(
					SolarPanelPowerModel.URI,
					SolarPanelPowerModel.COMPUTATION_STEP_RP_NAME),
				0.25);

			// -----------------------------------------------------------------
			// Simulation runs
			// -----------------------------------------------------------------

			// Tracing configuration

			HairDryerElectricityModel.VERBOSE = false;
			HairDryerElectricityModel.DEBUG = false;
			HairDryerSimpleUserModel.VERBOSE = false;
			HairDryerSimpleUserModel.DEBUG = false;

			HeaterElectricityModel.VERBOSE = false;
			HeaterElectricityModel.DEBUG = false;
			HeaterTemperatureModel.VERBOSE = false;
			HeaterTemperatureModel.DEBUG  = false;
			ExternalTemperatureModel.VERBOSE = false;
			ExternalTemperatureModel.DEBUG  = false;
			HeaterUnitTesterModel.VERBOSE = false;
			HeaterUnitTesterModel.DEBUG  = false;

			BatteriesPowerModel.VERBOSE = true;
			BatteriesPowerModel.DEBUG = false;

			if (SolarPanelSimulationConfigurationI.USE_ASTRONOMICAL_MODEL) {
				AstronomicalSunRiseAndSetModel.VERBOSE = false;
				AstronomicalSunRiseAndSetModel.DEBUG = false;
			} else {
				DeterministicSunRiseAndSetModel.VERBOSE = false;
				DeterministicSunRiseAndSetModel.DEBUG = false;
			}
			if (SolarPanelSimulationConfigurationI.
										USE_STOCHASTIC_SUN_INTENSITY_MODEL) {
				StochasticSunIntensityModel.VERBOSE = false;
				StochasticSunIntensityModel.DEBUG = false;
			} else {
				DeterministicSunIntensityModel.VERBOSE = false;
				DeterministicSunIntensityModel.DEBUG = false;
			}
			SolarPanelPowerModel.VERBOSE = false;
			SolarPanelPowerModel.DEBUG = false;

			GeneratorFuelModel.VERBOSE = false;
			GeneratorFuelModel.DEBUG = false;
			GeneratorPowerModel.VERBOSE = false;
			GeneratorPowerModel.DEBUG = false;
			GeneratorGlobalTesterModel.VERBOSE = false;
			GeneratorGlobalTesterModel.DEBUG = false;

			ElectricMeterElectricityModel.VERBOSE = true;
			ElectricMeterElectricityModel.DEBUG = false;

			// this add additional time at each simulation step in
			// standard simulations (useful for debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

			// Test scenario

			// run a CLASSICAL test scenario
			CLASSICAL.setUpSimulator(se, simParams);
			Time startTime = CLASSICAL.getStartTime();
			Duration d = CLASSICAL.getEndTime().subtract(startTime);
			se.doStandAloneSimulation(startTime.getSimulatedTime(),
									  d.getSimulatedDuration());
			// Optional: simulation report
			GlobalReport r = (GlobalReport) se.getFinalReport();
			System.out.println(r.printout(""));
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	// -------------------------------------------------------------------------
	// Test scenarios
	// -------------------------------------------------------------------------

	/** standard test scenario, see Gherkin specification.				 	*/
	protected static TestScenario	CLASSICAL =
		new TestScenario(
				"-----------------------------------------------------\n" +
				"Classical\n\n" +
				"  Gherkin specification\n\n" +
				"    Feature: heater operation\n\n" +
				"      Scenario: heater switched on\n" +
				"        Given a heater that is off\n" +
				"        When it is switched on\n" +
				"        Then it is on but not heating though set at the highest power level\n" +
				"      Scenario: heater heats\n" +
				"        Given a heater that is on and not heating\n" +
				"        When it is asked to heat\n" +
				"        Then it is on and it heats at the current power level\n" +
				"      Scenario: heater stops heating\n" +
				"        Given a hair dryer that is heating\n" +
				"        When it is asked not to heat\n" +
				"        Then it is on but it stops heating\n" +
				"      Scenario: heater heats\n" +
				"        Given a heater that is on and not heating\n" +
				"        When it is asked to heat\n" +
				"        Then it is on and it heats at the current power level\n" +
				"      Scenario: heater set a different power level\n" +
				"        Given a heater that is heating\n" +
				"        When it is set to a new power level\n" +
				"        Then it is on and it heats at the new power level\n" +
				"      Scenario: hair dryer switched off\n" +
				"        Given a hair dryer that is on\n" +
				"        When it is switched of\n" +
				"        Then it is off\n" +
				"    Feature: generator production\n\n" +
				"      Scenario: generator produces for a limited time without emptying the tank\n" +
				"        Given a standard generator with a tank not full neither empty\n" +
				"        When it is producing for a limited time\n" +
				"        Then the tank level goes down but stays not empty\n" +
				"-----------------------------------------------------\n",
				"\n-----------------------------------------------------\n" +
				"End Classical\n" +
				"-----------------------------------------------------",
				GlobalSimulationConfigurationI.START_INSTANT,
				GlobalSimulationConfigurationI.END_INSTANT,
				GlobalSimulationConfigurationI.START_TIME,
				(simulationEngine, testScenario, simulationParameters) -> {
					simulationParameters.put(
						ModelI.createRunParameterName(
							HeaterUnitTesterModel.URI,
							HeaterUnitTesterModel.TEST_SCENARIO_RP_NAME),
						testScenario);
					simulationParameters.put(
						ModelI.createRunParameterName(
							BatteriesPowerModel.URI,
							BatteriesPowerModel.CAPACITY_RP_NAME),
						BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
								* BatteriesSimulationConfiguration.
														NUMBER_OF_CELL_GROUPS_IN_SERIES
									* Batteries.CAPACITY_PER_UNIT.getData());
					simulationParameters.put(
						ModelI.createRunParameterName(
							BatteriesPowerModel.URI,
							BatteriesPowerModel.IN_POWER_RP_NAME),
						BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
								* Batteries.IN_POWER_PER_CELL.getData());
					simulationParameters.put(
						ModelI.createRunParameterName(
							BatteriesPowerModel.URI,
							BatteriesPowerModel.MAX_OUT_POWER_RP_NAME),
						BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
								* Batteries.MAX_OUT_POWER_PER_CELL.getData());
					simulationParameters.put(
						ModelI.createRunParameterName(
							BatteriesPowerModel.URI,
							BatteriesPowerModel.LEVEL_QUANTUM_RP_NAME),
						BatteriesSimulationConfiguration.
													STANDARD_LEVEL_INTEGRATION_QUANTUM);
					simulationParameters.put(
						ModelI.createRunParameterName(
							BatteriesPowerModel.URI,
							BatteriesPowerModel.INITIAL_LEVEL_RP_NAME),
						BatteriesSimulationConfiguration.INITIAL_BATTERIES_LEVEL);
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorFuelModel.URI,
							GeneratorFuelModel.CAPACITY_RP_NAME),
						GeneratorSimulationConfiguration.TANK_CAPACITY);
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorFuelModel.URI,
							GeneratorFuelModel.INITIAL_LEVEL_RP_NAME),
						GeneratorSimulationConfiguration.INITIAL_TANK_LEVEL);
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorFuelModel.URI,
							GeneratorFuelModel.MIN_FUEL_CONSUMPTION_RP_NAME),
						Generator.MIN_FUEL_CONSUMPTION.getData());
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorFuelModel.URI,
							GeneratorFuelModel.MAX_FUEL_CONSUMPTION_RP_NAME),
						Generator.MAX_FUEL_CONSUMPTION.getData());
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorFuelModel.URI,
							GeneratorFuelModel.LEVEL_QUANTUM_RP_NAME),
						GeneratorSimulationConfiguration.
											STANDARD_LEVEL_INTEGRATION_QUANTUM);
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorFuelModel.URI,
							GeneratorFuelModel.MAX_OUT_POWER_RP_NAME),
						Generator.MAX_POWER.getData());
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorPowerModel.URI,
							GeneratorPowerModel.MAX_OUT_POWER_RP_NAME),
						Generator.MAX_POWER.getData());
					simulationParameters.put(
						ModelI.createRunParameterName(
							GeneratorGlobalTesterModel.URI,
							GeneratorGlobalTesterModel.TEST_SCENARIO_RP_NAME),
						testScenario);
					simulationEngine.setSimulationRunParameters(
														simulationParameters);
				},
				new SimulationTestStep[]{
					new SimulationTestStep(
						GeneratorGlobalTesterModel.URI,
						Instant.parse("2025-10-20T12:15:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Start(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnHeater(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Heat(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new DoNotHeat(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Heat(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetPowerHeater(t,
									   				   new PowerValue(880.0)));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffHeater(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						GeneratorGlobalTesterModel.URI,
						Instant.parse("2025-10-20T17:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Stop(t));
							return ret;
						},
						(m, t) -> {})
					});
}
// -----------------------------------------------------------------------------
