package etape2;

import java.time.Instant;
import java.util.ArrayList;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import etape2.equipments.coffeemachine.mil.events.SetPowerCoffeeMachine.PowerValue;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
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
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import tests_utils.SimulationTestStep;
import tests_utils.TestScenario;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunGlobal_RT_Simulation</code> creates the real time simulator
 * for the household energy management example and then runs a typical
 * simulation in real time.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The simulation architecture for the global HEM application contains all of
 * the appliances atomic models composed under a single coupled model (a more
 * hierarchical architecture could be used but would complicate the flow of
 * events and variables) :
 * </p>
 * <p><img src="../../../../../images/hem-2025-e2/HEM_MILModel.png"/></p> 
 * <p>
 * This class shows how to describe, construct and then run a real time
 * simulation. By comparison with {@code RunHEM_Simulation}, differences
 * help understanding the passage from a synthetic simulation time run
 * to a real time one. Recall that real time simulations force the simulation
 * time to follow the real time, hence in a standard real time run, the
 * simulation time advance at the rhythm of the real time. However, such
 * simulation runs can become either very lengthy, for examples like the
 * household energy management where simulation runs could last several days,
 * or very short, for examples like simulating microprocessors where events
 * can occur at the nanosecond time scale. So it is also possible to keep the
 * same time structure but to accelerate or decelerate the real time by some
 * factor, here defined as {@code ACCELERATION_FACTOR}. A value greater than
 * one will accelerate the simulation while a value strictly between 0 and 1
 * will decelerate it.
 * </p>
 * <p>
 * So, notice the use of real time equivalent to the model descriptors and
 * the simulation engine attached to models, as well as the acceleration
 * factor passed as parameter through the descriptors. The same acceleration
 * factor must be imposed to all models to get time coherent simulations.
 * </p>
 * <p>
 * The interest of real time simulations will become clear when simulation
 * models will be used in SIL simulations with the actual component software
 * executing in parallel to the simulations. Time coherent exchanges will then
 * become possible between the code and the simulations as the execution
 * of code instructions will occur on the same time frame as the simulations.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code ACCELERATION_FACTOR > 0.0}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunGlobal_RT_Simulation
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** acceleration factor for the real time simulation; with a factor 2.0,
	 *  the simulation runs two times faster than real time i.e., a run that
	 *  is supposed to take 10 seconds in real time will take 5 seconds to
	 *  execute.															*/
	protected static final double ACCELERATION_FACTOR = 1200.0;

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

			// atomic HIOA models require RTAtomicHIOA_Descriptor while
			// atomic models require RTAtomicModelDescriptor
			// the same time unit and acceleration factor must be used for all
			// models

			// Fan models

			// the hair dyer model simulating its electricity consumption, an
			// atomic HIOA model hence we use an RTAtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					FanElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							FanElectricityModel.class,
							FanElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			// for atomic model, we use an RTAtomicModelDescriptor
			atomicModelDescriptors.put(
					FanSimpleUserModel.URI,
					RTAtomicModelDescriptor.create(
							FanSimpleUserModel.class,
							FanSimpleUserModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// CoffeeMachine models

			atomicModelDescriptors.put(
					CoffeeMachineElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							CoffeeMachineElectricityModel.class,
							CoffeeMachineElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					CoffeeMachineTemperatureModel.URI,
					RTAtomicHIOA_Descriptor.create(
							CoffeeMachineTemperatureModel.class,
							CoffeeMachineTemperatureModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			
			atomicModelDescriptors.put(
					CoffeeMachineUnitTesterModel.URI,
					RTAtomicModelDescriptor.create(
							CoffeeMachineUnitTesterModel.class,
							CoffeeMachineUnitTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// Batteries models

			// BatteriesPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					BatteriesPowerModel.URI,
					RTAtomicHIOA_Descriptor.create(
							BatteriesPowerModel.class,
							BatteriesPowerModel.URI,
							BatteriesSimulationConfiguration.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// Solar panel models

			String sunRiseAndSetURI = null;
			if (SolarPanelSimulationConfigurationI.USE_ASTRONOMICAL_MODEL) {
				// AstronomicalSunRiseAndSetModel is an atomic event scheduling
				// model, so needs an AtomicModelDescriptor
				sunRiseAndSetURI = AstronomicalSunRiseAndSetModel.URI;
				atomicModelDescriptors.put(
					AstronomicalSunRiseAndSetModel.URI,
					RTAtomicModelDescriptor.create(
							AstronomicalSunRiseAndSetModel.class,
							AstronomicalSunRiseAndSetModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			} else {
				// DeterministicSunRiseAndSetModel is an atomic event scheduling
				// model, so needs an AtomicModelDescriptor
				sunRiseAndSetURI = DeterministicSunRiseAndSetModel.URI;
				atomicModelDescriptors.put(
					DeterministicSunRiseAndSetModel.URI,
					RTAtomicModelDescriptor.create(
							DeterministicSunRiseAndSetModel.class,
							DeterministicSunRiseAndSetModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			}
			String sunIntensityModelURI = null;
			if (SolarPanelSimulationConfigurationI.
										USE_STOCHASTIC_SUN_INTENSITY_MODEL) {
				// StochasticSunIntensityModel is an atomic HIOA model, so needs
				// an AtomicHIOA_Descriptor
				sunIntensityModelURI = StochasticSunIntensityModel.URI;
				atomicModelDescriptors.put(
					StochasticSunIntensityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							StochasticSunIntensityModel.class,
							StochasticSunIntensityModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			} else {
				// DeterministicSunIntensityModel is an atomic HIOA model, so
				// needs an AtomicHIOA_Descriptor
				sunIntensityModelURI = DeterministicSunIntensityModel.URI;
				atomicModelDescriptors.put(
					DeterministicSunIntensityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							DeterministicSunIntensityModel.class,
							DeterministicSunIntensityModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			}
			// SolarPanelPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					SolarPanelPowerModel.URI,
					RTAtomicHIOA_Descriptor.create(
							SolarPanelPowerModel.class,
							SolarPanelPowerModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// Generator models

			// GeneratorFuelModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorFuelModel.URI,
					RTAtomicHIOA_Descriptor.create(
							GeneratorFuelModel.class,
							GeneratorFuelModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			// GeneratorPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorPowerModel.URI,
					RTAtomicHIOA_Descriptor.create(
							GeneratorPowerModel.class,
							GeneratorPowerModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			// BatteriesUnitTesterModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorGlobalTesterModel.URI,
					RTAtomicModelDescriptor.create(
							GeneratorGlobalTesterModel.class,
							GeneratorGlobalTesterModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// the electric meter model
			atomicModelDescriptors.put(
					ElectricMeterElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							ElectricMeterElectricityModel.class,
							ElectricMeterElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// -----------------------------------------------------------------
			// Global coupled model
			// -----------------------------------------------------------------

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(FanElectricityModel.URI);
			submodels.add(FanSimpleUserModel.URI);
			submodels.add(CoffeeMachineElectricityModel.URI);
			submodels.add(CoffeeMachineTemperatureModel.URI);
			submodels.add(CoffeeMachineUnitTesterModel.URI);
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

			// -----------------------------------------------------------------
			// Overall simulation architecture
			// -----------------------------------------------------------------


			// coupled model descriptor: an HIOA requires a
			// RTCoupledHIOA_Descriptor
			coupledModelDescriptors.put(
					GlobalCoupledModel.URI,
					new RTCoupledHIOA_Descriptor(
							GlobalCoupledModel.class,
							GlobalCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings,
							ACCELERATION_FACTOR));

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

			// run parameters for fan models

			simParams.put(
				ModelI.createRunParameterName(
					FanElectricityModel.URI,
					FanElectricityModel.LOW_MODE_CONSUMPTION_RPNAME),
				660.0);
			simParams.put(
				ModelI.createRunParameterName(
					FanElectricityModel.URI,
					FanElectricityModel.HIGH_MODE_CONSUMPTION_RPNAME),
				1320.0);
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

			FanElectricityModel.VERBOSE = false;
			FanElectricityModel.DEBUG = false;
			FanSimpleUserModel.VERBOSE = false;
			FanSimpleUserModel.DEBUG = false;

			CoffeeMachineElectricityModel.VERBOSE = false;
			CoffeeMachineElectricityModel.DEBUG = false;
			CoffeeMachineTemperatureModel.VERBOSE = false;
			CoffeeMachineTemperatureModel.DEBUG  = false;
			CoffeeMachineUnitTesterModel.VERBOSE = false;
			CoffeeMachineUnitTesterModel.DEBUG  = false;

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

			// Test scenario

			// run a CLASSICAL test scenario
			CLASSICAL.setUpSimulator(se, simParams);
			Time startTime = CLASSICAL.getStartTime();
			Duration d = CLASSICAL.getEndTime().subtract(startTime);
			// the real time of start of the simulation plus a 1s delay to give
			// the time to initialise all models in the architecture.
			long realTimeOfStart = System.currentTimeMillis() + 1000L;

			se.startRTSimulation(realTimeOfStart,
								 startTime.getSimulatedTime(),
								 d.getSimulatedDuration());

			// wait until the simulation ends i.e., the start delay  plus the
			// duration of the simulation in milliseconds plus another 2s delay
			// to make sure...
			Thread.sleep(
				1000L
				+ ((long)((d.getSimulatedDuration()*3600*1000.0)/
														ACCELERATION_FACTOR))
				+ 3000L);
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
				"    Feature: Coffee Machine operation\n\n" +
				"      Scenario: Coffee Machine switched on\n" +
				"        Given a coffee machine that is off\n" +
				"        When it is switched on at 12:30\n" +
				"        Then it is on (standby) consuming minimal power\n\n" +
				"      Scenario: Coffee Machine preparation\n" +
				"        Given a coffee machine that is on\n" +
				"        When it is filled with water at 13:00\n" +
				"        And set to Eco mode at 13:30\n" +
				"        Then it is ready to brew with Eco settings\n\n" +
				"      Scenario: Coffee Machine brewing\n" +
				"        Given a coffee machine that is ready\n" +
				"        When asked to make coffee at 14:00\n" +
				"        Then it starts heating and consuming high power\n\n" +
				"      Scenario: Serving coffee and power adjustment\n" +
				"        Given a coffee machine that has finished brewing\n" +
				"        When coffee is served at 14:30\n" +
				"        And power is manually adjusted at 14:30\n" +
				"        Then the coffee is dispensed and power consumption changes\n\n" +
				"      Scenario: Coffee Machine switched off\n" +
				"        Given a coffee machine that is on\n" +
				"        When it is switched off at 16:30\n" +
				"        Then it is off and consumes zero power\n\n" +
				"    Feature: Generator production\n\n" +
				"      Scenario: Generator operation cycle\n" +
				"        Given a standard generator with fuel\n" +
				"        When it starts at 12:15 and stops at 17:30\n" +
				"        Then it produces power and consumes fuel during this interval\n" +
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
							CoffeeMachineUnitTesterModel.URI,
							CoffeeMachineUnitTesterModel.TEST_SCENARIO_RP_NAME),
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
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnCoffeeMachine(t));
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
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetEcoModeCoffeeMachine(t));
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
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new ServeCoffee(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetPowerCoffeeMachine(t, new PowerValue(600.0)));
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
// -----------------------------------------------------------------------------
