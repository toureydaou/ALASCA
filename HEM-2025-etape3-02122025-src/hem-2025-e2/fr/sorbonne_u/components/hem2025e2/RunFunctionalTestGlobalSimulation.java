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
import java.time.Instant;
import java.util.ArrayList;
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.hem2025e1.equipments.batteries.Batteries;
import fr.sorbonne_u.components.hem2025e1.equipments.generator.Generator;
import fr.sorbonne_u.components.hem2025e1.equipments.solar_panel.SolarPanel;
import fr.sorbonne_u.components.hem2025e2.GlobalCoupledModel.GlobalReport;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.BatteriesPowerModel;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.BatteriesRequiredPowerChanged;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorFuelModel;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorGlobalTesterModel;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorPowerModel;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Refill;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
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
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SetPowerHeater.PowerValue;
import fr.sorbonne_u.components.hem2025e2.equipments.meter.mil.ElectricMeterElectricityModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.DeterministicSunIntensityModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.DeterministicSunRiseAndSetModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SolarPanelPowerModel;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunIntensityModelI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.SunRiseAndSetModelI;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunriseEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events.SunsetEvent;
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

// -----------------------------------------------------------------------------
/**
 * The class <code>RunFunctionalTestGlobalSimulation</code> creates the
 * simulator for the household energy management application and then runs
 * a simulation scenario to test the functionalities of this application.
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
 * This class shows how to use simulation model descriptors to create the
 * description of a simulation architecture and then create an instance of this
 * architecture by instantiating and connecting the models. Note how models
 * are described by atomic model descriptors and coupled model descriptors and
 * then the connections between coupled models and their submodels as well as
 * exported events and variables to imported ones are described by different
 * maps. In this example, only connections of events and bindings of variables
 * between models within this architecture are necessary, but when creating
 * coupled models, they can also import and export events and variables
 * consumed and produced by their submodels.
 * </p>
 * <p>
 * The architecture object is the root of this description and it provides
 * the method {@code constructSimulator} that instantiate the models and
 * connect them. This method returns the reference on the simulator attached
 * to the root coupled model in the architecture instance, which is then used
 * to perform simulation runs by calling the method
 * {@code doStandAloneSimulation}. Notice the use of the method
 * {@code setSimulationRunParameters} to initialise some parameters of
 * the simulation defined in the different models. This method is implemented
 * to traverse all of the models, hence each one can get its own parameters by
 * carefully defining unique names for them. Also, it shows how to get the
 * simulation reports from the models after the simulation run.
 * </p>
 * <p>
 * The descriptors and maps can be viewed as kinds of nodes in the abstract
 * syntax tree of an architectural language that does not have a concrete
 * syntax yet.
 * </p>
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
 * <p>Created on : 2023-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunFunctionalTestGlobalSimulation
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

			// atomic HIOA models require AtomicHIOA_Descriptor while
			// atomic models require AtomicModelDescriptor
			// the same time unit must be used for all models

			// Hair dryer models

			// the hair dyer model simulating its electricity consumption, an
			// atomic HIOA model hence we use an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					HairDryerElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							HairDryerElectricityModel.class,
							HairDryerElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					HairDryerUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							HairDryerUnitTesterModel.class,
							HairDryerUnitTesterModel.URI,
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

			// Batteries models

			// BatteriesPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					BatteriesPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							BatteriesPowerModel.class,
							BatteriesPowerModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// Solar panel models

			// DeterministicSunRiseAndSetModel is an atomic event scheduling
			// model, so needs an AtomicModelDescriptor
			atomicModelDescriptors.put(
					DeterministicSunRiseAndSetModel.URI,
					AtomicModelDescriptor.create(
							DeterministicSunRiseAndSetModel.class,
							DeterministicSunRiseAndSetModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			// DeterministicSunIntensityModel is an atomic HIOA model, so
			// needs an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					DeterministicSunIntensityModel.URI,
					AtomicHIOA_Descriptor.create(
							DeterministicSunIntensityModel.class,
							DeterministicSunIntensityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// SolarPanelPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					SolarPanelPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							SolarPanelPowerModel.class,
							SolarPanelPowerModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// Generator models

			// GeneratorFuelModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorFuelModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorFuelModel.class,
							GeneratorFuelModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			// GeneratorPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorPowerModel.class,
							GeneratorPowerModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));
			// GeneratorGlobalTesterModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorGlobalTesterModel.URI,
					AtomicModelDescriptor.create(
							GeneratorGlobalTesterModel.class,
							GeneratorGlobalTesterModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

			// Electric meter model

			atomicModelDescriptors.put(
					ElectricMeterElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							ElectricMeterElectricityModel.class,
							ElectricMeterElectricityModel.URI,
							GlobalSimulationConfigurationI.TIME_UNIT,
							null));

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
			// CoupledHIOA_Descriptor
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
			// Simulation runs
			// -----------------------------------------------------------------

			// Tracing configuration

			HairDryerElectricityModel.VERBOSE = false;
			HairDryerElectricityModel.DEBUG = false;
			HairDryerUnitTesterModel.VERBOSE = false;
			HairDryerUnitTesterModel.DEBUG = false;

			HeaterElectricityModel.VERBOSE = true;
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

			GlobalCoupledModel.DEBUG = false;

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

	/** standard test scenario.											 	
	 * @throws VerboseException */
	protected static TestScenarioWithSimulation	classical()
	throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"-----------------------------------------------------\n" +
			"Classical\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Classical\n" +
			"-----------------------------------------------------",
			"fake-clock-URI",	// for simulation only test scenario, no clock needed
			GlobalSimulationConfigurationI.START_INSTANT,
			GlobalSimulationConfigurationI.END_INSTANT,
			GlobalCoupledModel.URI,
			GlobalSimulationConfigurationI.START_TIME,
			(testScenario, simulationParameters) -> {
				// run parameters for hair dryer models
				simulationParameters.put(
					ModelI.createRunParameterName(
						HairDryerElectricityModel.URI,
						HairDryerElectricityModel.LOW_MODE_CONSUMPTION_RPNAME),
					660.0);
				simulationParameters.put(
					ModelI.createRunParameterName(
						HairDryerElectricityModel.URI,
						HairDryerElectricityModel.HIGH_MODE_CONSUMPTION_RPNAME),
					1320.0);
				simulationParameters.put(
					ModelI.createRunParameterName(
						HairDryerUnitTesterModel.URI,
						HairDryerUnitTesterModel.TEST_SCENARIO_RP_NAME),
					testScenario);

				// run parameters for heater models
				simulationParameters.put(
					ModelI.createRunParameterName(
						HeaterUnitTesterModel.URI,
						HeaterUnitTesterModel.TEST_SCENARIO_RP_NAME),
					testScenario);

				// run parameters for batteries models
				simulationParameters.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.CAPACITY_RP_NAME),
					GlobalSimulationConfigurationI.NUMBER_OF_PARALLEL_CELLS
							* GlobalSimulationConfigurationI.
												NUMBER_OF_CELL_GROUPS_IN_SERIES
								* Batteries.CAPACITY_PER_UNIT.getData());
				simulationParameters.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.IN_POWER_RP_NAME),
					GlobalSimulationConfigurationI.NUMBER_OF_PARALLEL_CELLS
							* Batteries.IN_POWER_PER_CELL.getData());
				simulationParameters.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.MAX_OUT_POWER_RP_NAME),
					GlobalSimulationConfigurationI.NUMBER_OF_PARALLEL_CELLS
							* Batteries.MAX_OUT_POWER_PER_CELL.getData());
				simulationParameters.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.LEVEL_QUANTUM_RP_NAME),
					GlobalSimulationConfigurationI.
										BATTERIES_LEVEL_INTEGRATION_QUANTUM);
				simulationParameters.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.INITIAL_LEVEL_RP_NAME),
					GlobalSimulationConfigurationI.INITIAL_BATTERIES_LEVEL);

				// run parameters for generator models
				simulationParameters.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.CAPACITY_RP_NAME),
					GlobalSimulationConfigurationI.TANK_CAPACITY);
				simulationParameters.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.INITIAL_LEVEL_RP_NAME),
					GlobalSimulationConfigurationI.INITIAL_TANK_LEVEL);
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
					GlobalSimulationConfigurationI.FUEL_LEVEL_INTEGRATION_QUANTUM);
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

				// run parameters for solar panel models
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunRiseAndSetModel.URI,
						SunRiseAndSetModelI.LATITUDE_RP_NAME),
					GlobalSimulationConfigurationI.LATITUDE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunRiseAndSetModel.URI,
						SunRiseAndSetModelI.LONGITUDE_RP_NAME),
					GlobalSimulationConfigurationI.LONGITUDE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunRiseAndSetModel.URI,
						SunRiseAndSetModelI.START_INSTANT_RP_NAME),
					GlobalSimulationConfigurationI.START_INSTANT);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunRiseAndSetModel.URI,
						SunRiseAndSetModelI.ZONE_ID_RP_NAME),
					GlobalSimulationConfigurationI.ZONE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunIntensityModel.URI,
						SunIntensityModelI.LATITUDE_RP_NAME),
					GlobalSimulationConfigurationI.LATITUDE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunIntensityModel.URI,
						SunIntensityModelI.LONGITUDE_RP_NAME),
					GlobalSimulationConfigurationI.LONGITUDE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunIntensityModel.URI,
						SunIntensityModelI.START_INSTANT_RP_NAME),
					GlobalSimulationConfigurationI.START_INSTANT);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunIntensityModel.URI,
						SunIntensityModelI.ZONE_ID_RP_NAME),
					GlobalSimulationConfigurationI.ZONE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunIntensityModel.URI,
						SunIntensityModelI.SLOPE_RP_NAME),
					GlobalSimulationConfigurationI.SLOPE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunIntensityModel.URI,
						SunIntensityModelI.ORIENTATION_RP_NAME),
					GlobalSimulationConfigurationI.ORIENTATION);
				simulationParameters.put(
					ModelI.createRunParameterName(
						DeterministicSunIntensityModel.URI,
						SunIntensityModelI.COMPUTATION_STEP_RP_NAME),
					GlobalSimulationConfigurationI.SUN_INTENSITY_MODEL_STEP);
				simulationParameters.put(
					ModelI.createRunParameterName(
						SolarPanelPowerModel.URI,
						SolarPanelPowerModel.LATITUDE_RP_NAME),
					GlobalSimulationConfigurationI.LATITUDE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						SolarPanelPowerModel.URI,
						SolarPanelPowerModel.LONGITUDE_RP_NAME),
					GlobalSimulationConfigurationI.LONGITUDE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						SolarPanelPowerModel.URI,
						SolarPanelPowerModel.START_INSTANT_RP_NAME),
					GlobalSimulationConfigurationI.START_INSTANT);
				simulationParameters.put(
					ModelI.createRunParameterName(
						SolarPanelPowerModel.URI,
						SolarPanelPowerModel.ZONE_ID_RP_NAME),
					GlobalSimulationConfigurationI.ZONE);
				simulationParameters.put(
					ModelI.createRunParameterName(
						SolarPanelPowerModel.URI,
						SolarPanelPowerModel.MAX_POWER_RP_NAME),
					GlobalSimulationConfigurationI.NB_SQUARE_METERS
							* SolarPanel.CAPACITY_PER_SQUARE_METER.getData());
				simulationParameters.put(
					ModelI.createRunParameterName(
						SolarPanelPowerModel.URI,
						SolarPanelPowerModel.COMPUTATION_STEP_RP_NAME),
					GlobalSimulationConfigurationI.SOLAR_PANEL_POWER_MODEL_STEP);
				},
				new TestStepI[]{
					new SimulationTestStep(
						GeneratorGlobalTesterModel.URI,
						Instant.parse("2025-10-20T08:15:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Start(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T08:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnHeater(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T08:40:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T08:41:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetHighHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T08:50:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetLowHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T08:55:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Heat(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:10:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:11:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetHighHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:20:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetLowHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:25:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new DoNotHeat(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:40:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:41:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetHighHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:50:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetLowHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T09:55:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T10:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Heat(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T10:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetPowerHeater(t,
									   				   new PowerValue(880.0)));
							return ret;
						},
						(m, t) -> {}),
//					new SimulationTestStep(
//						HeaterUnitTesterModel.URI,
//						Instant.parse("2025-10-20T16:30:00.00Z"),
//						(m, t) -> {
//							ArrayList<EventI> ret = new ArrayList<>();
//							ret.add(new SwitchOffHeater(t));
//							return ret;
//						},
//						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:40:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOnHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:41:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetHighHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:50:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetLowHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						HairDryerUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:55:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffHairDryer(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
						GeneratorGlobalTesterModel.URI,
						Instant.parse("2025-10-21T07:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Stop(t));
							return ret;
						},
						(m, t) -> {}),
					new SimulationTestStep(
							HeaterUnitTesterModel.URI,
							Instant.parse("2025-10-21T07:30:00.00Z"),
							(m, t) -> {
								ArrayList<EventI> ret = new ArrayList<>();
								ret.add(new SwitchOffHeater(t));
								return ret;
							},
							(m, t) -> {})
					});
	}
}
// -----------------------------------------------------------------------------
