package etape2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;
import etape1.equipments.batteries.Batteries;
import etape1.equipments.generator.Generator;
import etape1.equipments.solar_panel.SolarPanel;
import etape2.GlobalCoupledModel.GlobalReport;
import etape2.equipments.batteries.mil.BatteriesPowerModel;
import etape2.equipments.batteries.mil.BatteriesSimulationConfiguration;
import etape2.equipments.batteries.mil.events.BatteriesRequiredPowerChanged;
import etape2.equipments.coffeemachine.mil.CoffeeMachineElectricityModel;
import etape2.equipments.coffeemachine.mil.CoffeeMachineTemperatureModel;
import etape2.equipments.coffeemachine.mil.CoffeeMachineUnitTesterModel;
import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine.WaterValue;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.ServeCoffee;
import etape2.equipments.coffeemachine.mil.events.SetEcoModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetMaxModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetNormalModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetPowerCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import etape2.equipments.kettle.mil.KettleElectricityModel;
import etape2.equipments.kettle.mil.KettleTemperatureModel;
import etape2.equipments.kettle.mil.KettleUnitTesterModel;
import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.SetEcoModeKettle;
import etape2.equipments.kettle.mil.events.SetMaxModeKettle;
import etape2.equipments.kettle.mil.events.SetNormalModeKettle;
import etape2.equipments.kettle.mil.events.SetPowerKettle;
import etape2.equipments.kettle.mil.events.SetSuspendedModeKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
import etape2.equipments.fan.mil.FanElectricityModel;
import etape2.equipments.fan.mil.FanSimpleUserModel;
import etape2.equipments.fan.mil.events.SetHighModeFan;
import etape2.equipments.fan.mil.events.SetLowModeFan;
import etape2.equipments.fan.mil.events.SetMediumModeFan;
import etape2.equipments.fan.mil.events.SwitchOffFan;
import etape2.equipments.fan.mil.events.SwitchOnFan;
import etape2.equipments.generator.mil.GeneratorFuelModel;
import etape2.equipments.generator.mil.GeneratorGlobalTesterModel;
import etape2.equipments.generator.mil.GeneratorPowerModel;
import etape2.equipments.generator.mil.GeneratorSimulationConfiguration;
import etape2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import etape2.equipments.generator.mil.events.Refill;
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import etape2.equipments.laundry.mil.LaundryElectricityModel;
import etape2.equipments.laundry.mil.LaundryUnitTesterModel;
import etape2.equipments.laundry.mil.events.CancelWash;
import etape2.equipments.laundry.mil.events.SetColorModeLaundry;
import etape2.equipments.laundry.mil.events.SetDelicateModeLaundry;
import etape2.equipments.laundry.mil.events.SetIntensiveModeLaundry;
import etape2.equipments.laundry.mil.events.SetSpinSpeed;
import etape2.equipments.laundry.mil.events.SetWashTemperature;
import etape2.equipments.laundry.mil.events.SetWhiteModeLaundry;
import etape2.equipments.laundry.mil.events.StartWash;
import etape2.equipments.laundry.mil.events.SwitchOffLaundry;
import etape2.equipments.laundry.mil.events.SwitchOnLaundry;
import etape2.equipments.meter.mil.ElectricMeterElectricityModel;
import etape2.equipments.solar_panel.mil.AstronomicalSunRiseAndSetModel;
import etape2.equipments.solar_panel.mil.DeterministicSunIntensityModel;
import etape2.equipments.solar_panel.mil.DeterministicSunRiseAndSetModel;
import etape2.equipments.solar_panel.mil.SolarPanelPowerModel;
import etape2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
import etape2.equipments.solar_panel.mil.StochasticSunIntensityModel;
import etape2.equipments.solar_panel.mil.SunIntensityModelI;
import etape2.equipments.solar_panel.mil.SunRiseAndSetModelI;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.utils.tests.TestStepI;
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
import fr.sorbonne_u.exceptions.VerboseException;

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
					FanElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							FanElectricityModel.class,
							FanElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					FanSimpleUserModel.URI,
					AtomicModelDescriptor.create(
							FanSimpleUserModel.class,
							FanSimpleUserModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// CoffeeMachine models

			atomicModelDescriptors.put(
					CoffeeMachineElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							CoffeeMachineElectricityModel.class,
							CoffeeMachineElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					CoffeeMachineTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							CoffeeMachineTemperatureModel.class,
							CoffeeMachineTemperatureModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			
			atomicModelDescriptors.put(
					CoffeeMachineUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							CoffeeMachineUnitTesterModel.class,
							CoffeeMachineUnitTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// Laundry models

			atomicModelDescriptors.put(
					LaundryElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							LaundryElectricityModel.class,
							LaundryElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					LaundryUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							LaundryUnitTesterModel.class,
							LaundryUnitTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// Kettle models

			atomicModelDescriptors.put(
					KettleElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							KettleElectricityModel.class,
							KettleElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					KettleTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							KettleTemperatureModel.class,
							KettleTemperatureModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					KettleUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							KettleUnitTesterModel.class,
							KettleUnitTesterModel.URI,
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
							null));
			
			atomicModelDescriptors.put(
					GeneratorPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorPowerModel.class,
							GeneratorPowerModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null));
			
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
			submodels.add(FanElectricityModel.URI);
			submodels.add(FanSimpleUserModel.URI);
			submodels.add(CoffeeMachineElectricityModel.URI);
			submodels.add(CoffeeMachineTemperatureModel.URI);
			submodels.add(CoffeeMachineUnitTesterModel.URI);
			submodels.add(LaundryElectricityModel.URI);
			submodels.add(LaundryUnitTesterModel.URI);
			submodels.add(KettleElectricityModel.URI);
			submodels.add(KettleTemperatureModel.URI);
			submodels.add(KettleUnitTesterModel.URI);
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

			// Fan events

			connections.put(
				new EventSource(FanSimpleUserModel.URI,
								SwitchOnFan.class),
				new EventSink[] {
					new EventSink(FanElectricityModel.URI,
								  SwitchOnFan.class)
				});
			connections.put(
				new EventSource(FanSimpleUserModel.URI,
								SwitchOffFan.class),
				new EventSink[] {
					new EventSink(FanElectricityModel.URI,
								  SwitchOffFan.class)
				});
			connections.put(
				new EventSource(FanSimpleUserModel.URI,
								SetHighModeFan.class),
				new EventSink[] {
					new EventSink(FanElectricityModel.URI,
								  SetHighModeFan.class)
				});
			connections.put(
					new EventSource(FanSimpleUserModel.URI,
									SetMediumModeFan.class),
					new EventSink[] {
						new EventSink(FanElectricityModel.URI,
									  SetMediumModeFan.class)
					});
			connections.put(
				new EventSource(FanSimpleUserModel.URI,
								SetLowModeFan.class),
				new EventSink[] {
					new EventSink(FanElectricityModel.URI,
								  SetLowModeFan.class)
				});

			// CoffeeMachine events

			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI,
									SetPowerCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetPowerCoffeeMachine.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI,
									SwitchOnCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SwitchOnCoffeeMachine.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI,
									SwitchOffCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SwitchOffCoffeeMachine.class),
							new EventSink(CoffeeMachineTemperatureModel.URI,
										  SwitchOffCoffeeMachine.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, MakeCoffee.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  MakeCoffee.class),
							new EventSink(CoffeeMachineTemperatureModel.URI,
									MakeCoffee.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, DoNotHeat.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  DoNotHeat.class),
							new EventSink(CoffeeMachineTemperatureModel.URI,
										  DoNotHeat.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, ServeCoffee.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  ServeCoffee.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetEcoModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetEcoModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetMaxModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetMaxModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetNormalModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetNormalModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetSuspendedModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetSuspendedModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, FillWaterCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
									FillWaterCoffeeMachine.class),
					});

			// Laundry events

			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SwitchOnLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SwitchOnLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SwitchOffLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SwitchOffLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SetDelicateModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SetDelicateModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SetColorModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SetColorModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SetWhiteModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SetWhiteModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SetIntensiveModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SetIntensiveModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									StartWash.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  StartWash.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									CancelWash.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  CancelWash.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SetWashTemperature.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SetWashTemperature.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
									SetSpinSpeed.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
										  SetSpinSpeed.class)
					});

			// Kettle events

			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									SwitchOnKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  SwitchOnKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									SwitchOffKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  SwitchOffKettle.class),
							new EventSink(KettleTemperatureModel.URI,
										  SwitchOffKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									HeatKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  HeatKettle.class),
							new EventSink(KettleTemperatureModel.URI,
										  HeatKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									DoNotHeatKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  DoNotHeatKettle.class),
							new EventSink(KettleTemperatureModel.URI,
										  DoNotHeatKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									SetPowerKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  SetPowerKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									SetEcoModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  SetEcoModeKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									SetNormalModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  SetNormalModeKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									SetMaxModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  SetMaxModeKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
									SetSuspendedModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
										  SetSuspendedModeKettle.class)
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

			// Bindings among coffee machine models

			
			bindings.put(
				new VariableSource("currentHeatingPower", Double.class,
								   CoffeeMachineElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentHeatingPower", Double.class,
									 CoffeeMachineTemperatureModel.URI)
				});
			
			bindings.put(
				new VariableSource("currentWaterLevel", Double.class,
								   CoffeeMachineElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentWaterLevel", Double.class,
									 CoffeeMachineTemperatureModel.URI)
				});

			// Bindings among kettle models

			bindings.put(
				new VariableSource("currentHeatingPower", Double.class,
								   KettleElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentHeatingPower", Double.class,
									 KettleTemperatureModel.URI)
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
								   FanElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentIntensity", Double.class,
									 "currentFanIntensity", Double.class,
									 ElectricMeterElectricityModel.URI)
				});
			bindings.put(
				new VariableSource("currentIntensity", Double.class,
								   CoffeeMachineElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentIntensity", Double.class,
									 "currentCoffeeMachineIntensity", Double.class,
									 ElectricMeterElectricityModel.URI)
				});
			bindings.put(
				new VariableSource("currentIntensity", Double.class,
								   LaundryElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentIntensity", Double.class,
									 "currentLaundryIntensity", Double.class,
									 ElectricMeterElectricityModel.URI)
				});
			bindings.put(
				new VariableSource("currentIntensity", Double.class,
								   KettleElectricityModel.URI),
				new VariableSink[] {
					new VariableSink("currentIntensity", Double.class,
									 "currentKettleIntensity", Double.class,
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
					FanElectricityModel.URI,
					FanElectricityModel.LOW_MODE_CONSUMPTION_RPNAME),
				200.0);
			simParams.put(
					ModelI.createRunParameterName(
						FanElectricityModel.URI,
						FanElectricityModel.MEDIUM_MODE_CONSUMPTION_RPNAME),
					500.0);
			simParams.put(
				ModelI.createRunParameterName(
					FanElectricityModel.URI,
					FanElectricityModel.HIGH_MODE_CONSUMPTION_RPNAME),
				800.0);
			simParams.put(
				ModelI.createRunParameterName(
					FanSimpleUserModel.URI,
					FanSimpleUserModel.MEAN_STEP_RPNAME),
				0.05);
			simParams.put(
				ModelI.createRunParameterName(
					FanSimpleUserModel.URI,
					FanSimpleUserModel.MEAN_DELAY_RPNAME),
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

			FanElectricityModel.VERBOSE = true;
			FanElectricityModel.DEBUG = false;
			FanSimpleUserModel.VERBOSE = true;
			FanSimpleUserModel.DEBUG = false;

			CoffeeMachineElectricityModel.VERBOSE = false;
			CoffeeMachineElectricityModel.DEBUG = true;
			CoffeeMachineTemperatureModel.VERBOSE = false;
			CoffeeMachineTemperatureModel.DEBUG  = true;
			CoffeeMachineUnitTesterModel.VERBOSE = true;
			CoffeeMachineUnitTesterModel.DEBUG  = false;

			LaundryElectricityModel.VERBOSE = true;
			LaundryElectricityModel.DEBUG = false;
			LaundryUnitTesterModel.VERBOSE = true;
			LaundryUnitTesterModel.DEBUG = false;

			KettleElectricityModel.VERBOSE = true;
			KettleElectricityModel.DEBUG = false;
			KettleTemperatureModel.VERBOSE = true;
			KettleTemperatureModel.DEBUG = false;
			KettleUnitTesterModel.VERBOSE = true;
			KettleUnitTesterModel.DEBUG = false;

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
			TestScenarioWithSimulation classical = classical();
			Map<String, Object> classicalRunParameters =
											new HashMap<String, Object>();
			classical.addToRunParameters(classicalRunParameters);
			se.setSimulationRunParameters(classicalRunParameters);
			Time startTime = classical.getStartTime();
			Duration d = classical.getEndTime().subtract(startTime);
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
	protected static TestScenarioWithSimulation	classical()
			throws VerboseException
			{
				return new TestScenarioWithSimulation(
				"-----------------------------------------------------\n" +
				"Classical Global Simulation\n\n" +
				"  Gherkin specification\n\n" +
				"    Feature: Global Energy Management with Coffee Machine, Laundry, Fan and Kettle\n\n" +
				"      Scenario: Generator startup\n" +
				"        Given a generator with fuel\n" +
				"        When it is started at 12:15\n" +
				"        Then it begins producing power\n\n" +
				"      Scenario: Coffee Machine initialization\n" +
				"        Given a coffee machine that is off\n" +
				"        When it is switched on at 12:30\n" +
				"        Then it enters standby mode\n\n" +
				"      Scenario: Laundry Machine initialization\n" +
				"        Given a laundry machine that is off\n" +
				"        When it is switched on at 12:45\n" +
				"        Then it enters standby mode\n\n" +
				"      Scenario: Coffee Machine preparation\n" +
				"        Given the coffee machine is on\n" +
				"        When water is filled (1.0L) at 13:00\n" +
				"        And the mode is set to MAX at 13:30\n" +
				"        Then the machine is configured for high power heating\n\n" +
				"      Scenario: Laundry Machine configuration\n" +
				"        Given the laundry machine is on\n" +
				"        When the color mode is set at 13:15\n" +
				"        And the temperature is set to 40C at 13:20\n" +
				"        And the spin speed is set to 1200 RPM at 13:25\n" +
				"        Then the machine is configured for washing\n\n" +
				"      Scenario: Brewing process\n" +
				"        Given the machine has water and is configured\n" +
				"        When the MakeCoffee command is issued at 14:00\n" +
				"        Then the machine heats up and consumes electricity\n\n" +
				"      Scenario: Laundry wash cycle\n" +
				"        Given the laundry machine is configured\n" +
				"        When the wash cycle starts at 14:15\n" +
				"        Then the machine begins washing and consuming power\n\n" +
				"      Scenario: Serving coffee\n" +
				"        Given the heating phase is active/done\n" +
				"        When coffee is served at 14:30\n" +
				"        Then the water level decreases\n\n" +
				"      Scenario: Laundry mode change during wash\n" +
				"        Given the laundry machine is washing\n" +
				"        When the mode is changed to intensive at 15:00\n" +
				"        Then the power consumption increases\n\n" +
				"      Scenario: Laundry wash cycle cancellation\n" +
				"        Given the laundry machine is washing\n" +
				"        When the wash is cancelled at 16:00\n" +
				"        Then the machine returns to ON state\n\n" +
				"      Scenario: Laundry Machine shutdown\n" +
				"        Given the laundry machine is on\n" +
				"        When it is switched off at 16:15\n" +
				"        Then it stops consuming power\n\n" +
				"      Scenario: Coffee Machine shutdown\n" +
				"        Given the coffee machine is active\n" +
				"        When it is switched off at 16:30\n" +
				"        Then it stops consuming power\n\n" +
				"      Scenario: Kettle initialization\n" +
				"        Given a kettle that is off\n" +
				"        When it is switched on at 13:00\n" +
				"        Then it enters standby mode\n\n" +
				"      Scenario: Kettle heating\n" +
				"        Given the kettle is on\n" +
				"        When heating starts at 13:10 in NORMAL mode\n" +
				"        Then the water temperature rises\n\n" +
				"      Scenario: Kettle mode change\n" +
				"        Given the kettle is heating\n" +
				"        When the mode is changed to MAX at 14:30\n" +
				"        Then the heating power increases to 3000W\n\n" +
				"      Scenario: Kettle stop heating and shutdown\n" +
				"        Given the kettle is heating\n" +
				"        When heating stops at 15:30 and it is switched off at 15:45\n" +
				"        Then it stops consuming power\n\n" +
				"      Scenario: Generator shutdown\n" +
				"        Given the generator is running\n" +
				"        When it is stopped at 17:30\n" +
				"        Then it stops producing power\n" +
				"-----------------------------------------------------\n",
				"\n-----------------------------------------------------\n" +
				"End Classical\n" +
				"-----------------------------------------------------",
				"fake-clock-URI",
				GlobalSimulationConfigurationI.START_INSTANT,
				GlobalSimulationConfigurationI.END_INSTANT,
				GlobalCoupledModel.URI,
				GlobalSimulationConfigurationI.START_TIME,
				(testScenario, simulationParameters) -> {
					simulationParameters.put(
						ModelI.createRunParameterName(
							CoffeeMachineUnitTesterModel.URI,
							CoffeeMachineUnitTesterModel.TEST_SCENARIO_RP_NAME),
						testScenario);
					simulationParameters.put(
						ModelI.createRunParameterName(
							LaundryUnitTesterModel.URI,
							LaundryUnitTesterModel.TEST_SCENARIO_RP_NAME),
						testScenario);
					simulationParameters.put(
						ModelI.createRunParameterName(
							KettleUnitTesterModel.URI,
							KettleUnitTesterModel.TEST_SCENARIO_RP_NAME),
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
				},
				new TestStepI[]{
					// Kettle: switch on at 13:00
					new SimulationTestStep(
						KettleUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnKettle(t));
							return ret;
						},
						(m, t) -> {}),
					// Kettle: start heating at 13:10
					new SimulationTestStep(
						KettleUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:10:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new HeatKettle(t));
							return ret;
						},
						(m, t) -> {}),
					// Kettle: change to MAX mode at 14:30
					new SimulationTestStep(
						KettleUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetMaxModeKettle(t));
							return ret;
						},
						(m, t) -> {}),
					// Kettle: stop heating at 15:30
					new SimulationTestStep(
						KettleUnitTesterModel.URI,
						Instant.parse("2025-10-20T15:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new DoNotHeatKettle(t));
							return ret;
						},
						(m, t) -> {}),
					// Kettle: switch off at 15:45
					new SimulationTestStep(
						KettleUnitTesterModel.URI,
						Instant.parse("2025-10-20T15:45:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffKettle(t));
							return ret;
						},
						(m, t) -> {}),
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
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnCoffeeMachine(t));
							return ret;
						},
						(m, t) -> {}),
					// Laundry switch on
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:45:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnLaundry(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new FillWaterCoffeeMachine(t, new WaterValue(1.0)));
							return ret;
						},
						(m, t) -> {}),
					// Laundry configuration: set color mode
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:15:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetColorModeLaundry(t));
							return ret;
						},
						(m, t) -> {}),
					// Laundry configuration: set temperature to 40°C
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:20:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetWashTemperature(t, 40.0));
							return ret;
						},
						(m, t) -> {}),
					// Laundry configuration: set spin speed to 1200 RPM
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:25:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetSpinSpeed(t, SpinSpeed.RPM_1200));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetMaxModeCoffeeMachine(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new MakeCoffee(t));
							return ret;
						},
						(m, t) -> {}),
					// Laundry: start wash cycle
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:15:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new StartWash(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new ServeCoffee(t));
							return ret;
						},
						(m, t) -> {}),
					// Laundry: change to intensive mode during wash
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T15:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetIntensiveModeLaundry(t));
							return ret;
						},
						(m, t) -> {}),
					// Laundry: cancel wash cycle
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new CancelWash(t));
							return ret;
						},
						(m, t) -> {}),
					// Laundry: switch off
					new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:15:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffLaundry(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffCoffeeMachine(t));
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
}
// -----------------------------------------------------------------------------
