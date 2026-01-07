package fr.sorbonne_u.components.hem2025e2;

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

import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.hem2025e2.GlobalCoupledModel.GlobalReport;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.BatteriesPowerModel;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.BatteriesRequiredPowerChanged;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorFuelModel;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorGlobalTesterModel;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorPowerModel;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Refill;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Start;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Stop;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.TankEmpty;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.TankNoLongerEmpty;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.HairDryerElectricityModel;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.HairDryerUnitTesterModel;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.ExternalTemperatureModel;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterElectricityModel;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterTemperatureModel;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterUnitTesterModel;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SetPowerHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.meter.mil.ElectricMeterElectricityModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.DeterministicSunIntensityModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.DeterministicSunRiseAndSetModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SolarPanelPowerModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunriseEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunsetEvent;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunFunctionalTestGlobal_RT_Simulation</code> creates the real
 * time simulator for the household energy management example and then runs a
 * simulation scenario to test the functionalities of this application.
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
public class			RunFunctionalTestGlobal_RT_Simulation
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------


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

			// Hair dryer models

			// the hair dyer model simulating its electricity consumption, an
			// atomic HIOA model hence we use an RTAtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					HairDryerElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HairDryerElectricityModel.class,
							HairDryerElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));
			// for atomic model, we use an RTAtomicModelDescriptor
			atomicModelDescriptors.put(
					HairDryerUnitTesterModel.URI,
					RTAtomicModelDescriptor.create(
							HairDryerUnitTesterModel.class,
							HairDryerUnitTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

			// Heater models

			atomicModelDescriptors.put(
					HeaterElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HeaterElectricityModel.class,
							HeaterElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					HeaterTemperatureModel.URI,
					RTAtomicHIOA_Descriptor.create(
							HeaterTemperatureModel.class,
							HeaterTemperatureModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.URI,
					RTAtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					HeaterUnitTesterModel.URI,
					RTAtomicModelDescriptor.create(
							HeaterUnitTesterModel.class,
							HeaterUnitTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

			// Batteries models

			// BatteriesPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					BatteriesPowerModel.URI,
					RTAtomicHIOA_Descriptor.create(
							BatteriesPowerModel.class,
							BatteriesPowerModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

			// Solar panel models

			// DeterministicSunRiseAndSetModel is an atomic event scheduling
			// model, so needs an AtomicModelDescriptor
			atomicModelDescriptors.put(
					DeterministicSunRiseAndSetModel.URI,
					RTAtomicModelDescriptor.create(
							DeterministicSunRiseAndSetModel.class,
							DeterministicSunRiseAndSetModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));
			// DeterministicSunIntensityModel is an atomic HIOA model, so
			// needs an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					DeterministicSunIntensityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							DeterministicSunIntensityModel.class,
							DeterministicSunIntensityModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

			// SolarPanelPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					SolarPanelPowerModel.URI,
					RTAtomicHIOA_Descriptor.create(
							SolarPanelPowerModel.class,
							SolarPanelPowerModel.URI,
							SolarPanelSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

			// Generator models

			// GeneratorFuelModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorFuelModel.URI,
					RTAtomicHIOA_Descriptor.create(
							GeneratorFuelModel.class,
							GeneratorFuelModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));
			// GeneratorPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorPowerModel.URI,
					RTAtomicHIOA_Descriptor.create(
							GeneratorPowerModel.class,
							GeneratorPowerModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));
			// GeneratorGlobalTesterModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorGlobalTesterModel.URI,
					RTAtomicModelDescriptor.create(
							GeneratorGlobalTesterModel.class,
							GeneratorGlobalTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

			// Electric meter model

			// the electric meter model
			atomicModelDescriptors.put(
					ElectricMeterElectricityModel.URI,
					RTAtomicHIOA_Descriptor.create(
							ElectricMeterElectricityModel.class,
							ElectricMeterElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null,
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

			// -----------------------------------------------------------------
			// Global coupled model
			// -----------------------------------------------------------------

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HairDryerElectricityModel.URI);
			submodels.add(HairDryerUnitTesterModel.URI);
			submodels.add(HeaterElectricityModel.URI);
			submodels.add(HeaterTemperatureModel.URI);
			submodels.add(ExternalTemperatureModel.URI);
			submodels.add(HeaterUnitTesterModel.URI);
			submodels.add(BatteriesPowerModel.URI);
			submodels.add(DeterministicSunRiseAndSetModel.URI);
			submodels.add(DeterministicSunIntensityModel.URI);
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
				new EventSource(HairDryerUnitTesterModel.URI,
								SwitchOnHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerElectricityModel.URI,
								  SwitchOnHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerUnitTesterModel.URI,
								SwitchOffHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerElectricityModel.URI,
								  SwitchOffHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerUnitTesterModel.URI,
								SetHighHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerElectricityModel.URI,
								  SetHighHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerUnitTesterModel.URI,
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
				new EventSource(DeterministicSunRiseAndSetModel.URI,
								SunriseEvent.class),
				new EventSink[] {
					new EventSink(DeterministicSunIntensityModel.URI,
								  SunriseEvent.class),
					new EventSink(SolarPanelPowerModel.URI, SunriseEvent.class)
				});
			connections.put(
				new EventSource(DeterministicSunRiseAndSetModel.URI,
								SunsetEvent.class),
				new EventSink[] {
					new EventSink(DeterministicSunIntensityModel.URI,
								  SunsetEvent.class),
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
								   DeterministicSunIntensityModel.URI),
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
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR));

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
			// Simulation runs
			// -----------------------------------------------------------------

			// Tracing configuration

			HairDryerElectricityModel.VERBOSE = false;
			HairDryerElectricityModel.DEBUG = false;
			HairDryerUnitTesterModel.VERBOSE = false;
			HairDryerUnitTesterModel.DEBUG = false;

			HeaterElectricityModel.VERBOSE = false;
			HeaterElectricityModel.DEBUG = false;
			HeaterTemperatureModel.VERBOSE = false;
			HeaterTemperatureModel.DEBUG  = false;
			ExternalTemperatureModel.VERBOSE = false;
			ExternalTemperatureModel.DEBUG  = false;
			HeaterUnitTesterModel.VERBOSE = false;
			HeaterUnitTesterModel.DEBUG  = false;

			BatteriesPowerModel.VERBOSE = false;
			BatteriesPowerModel.DEBUG = false;

			DeterministicSunRiseAndSetModel.VERBOSE = false;
			DeterministicSunRiseAndSetModel.DEBUG = false;
			DeterministicSunIntensityModel.VERBOSE = false;
			DeterministicSunIntensityModel.DEBUG = false;
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

			GlobalCoupledModel.DEBUG = true;

			// Test scenario

			// run a CLASSICAL test scenario
			TestScenarioWithSimulation classical =
					RunFunctionalTestGlobalSimulation.classical();
			Map<String, Object> classicalRunParameters =
												new HashMap<String, Object>();
			classical.addToRunParameters(classicalRunParameters);
			se.setSimulationRunParameters(classicalRunParameters);
			Time startTime = classical.getStartTime();
			Duration d = classical.getEndTime().subtract(startTime);
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
							GlobalSimulationConfigurationI.ACCELERATION_FACTOR))
				+ 3000L);
			// Optional: simulation report
			GlobalReport r = (GlobalReport) se.getFinalReport();
			System.out.println(r.printout(""));
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------
