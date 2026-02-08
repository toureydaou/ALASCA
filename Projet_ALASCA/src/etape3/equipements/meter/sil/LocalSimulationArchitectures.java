package etape3.equipements.meter.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
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
import java.util.concurrent.TimeUnit;

import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.Heat;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.ServeCoffee;
import etape2.equipments.coffeemachine.mil.events.SetEcoModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetMaxModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetNormalModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import etape2.equipments.fan.mil.events.SetHighModeFan;
import etape2.equipments.fan.mil.events.SetLowModeFan;
import etape2.equipments.fan.mil.events.SetMediumModeFan;
import etape2.equipments.fan.mil.events.SwitchOffFan;
import etape2.equipments.fan.mil.events.SwitchOnFan;
import etape2.equipments.generator.mil.GeneratorSimulationConfiguration;
import etape2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.SetEcoModeKettle;
import etape2.equipments.kettle.mil.events.SetMaxModeKettle;
import etape2.equipments.kettle.mil.events.SetNormalModeKettle;
import etape2.equipments.kettle.mil.events.SetPowerKettle;
import etape2.equipments.kettle.mil.events.SetSuspendedModeKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
import etape2.equipments.laundry.mil.events.CancelWash;
import etape2.equipments.laundry.mil.events.SetColorModeLaundry;
import etape2.equipments.laundry.mil.events.SetDelicateModeLaundry;
import etape2.equipments.laundry.mil.events.SetIntensiveModeLaundry;
import etape2.equipments.laundry.mil.events.SetPowerLaundry;
import etape2.equipments.laundry.mil.events.SetSpinSpeed;
import etape2.equipments.laundry.mil.events.SetWashTemperature;
import etape2.equipments.laundry.mil.events.SetWhiteModeLaundry;
import etape2.equipments.laundry.mil.events.StartWash;
import etape2.equipments.laundry.mil.events.SwitchOffLaundry;
import etape2.equipments.laundry.mil.events.SwitchOnLaundry;
import etape2.equipments.solar_panel.mil.DeterministicSunIntensityModel;
import etape2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
import etape3.equipements.coffee_machine.sil.CoffeeMachineElectricitySILModel;
import etape3.equipements.fan.sil.FanElectricitySILModel;
import etape3.equipements.kettle.sil.KettleElectricitySILModel;
import etape3.equipements.laundry.sil.LaundryElectricitySILModel;
import etape4.equipments.batteries.sil.BatteriesPowerSILModel;
import etape4.equipments.batteries.sil.events.CurrentBatteriesLevel;
import etape4.equipments.batteries.sil.events.SIL_BatteriesRequiredPowerChanged;
import etape4.equipments.batteries.sil.events.SIL_StartCharging;
import etape4.equipments.batteries.sil.events.SIL_StopCharging;
import etape4.equipments.generator.sil.GeneratorFuelSILModel;
import etape4.equipments.generator.sil.GeneratorPowerSILModel;
import etape4.equipments.generator.sil.events.CurrentFuelConsumption;
import etape4.equipments.generator.sil.events.CurrentFuelLevel;
import etape4.equipments.generator.sil.events.CurrentPowerProduction;
import etape4.equipments.generator.sil.events.SIL_Refill;
import etape4.equipments.solar_panel.sil.SolarPanelPowerSILModel;
import etape4.equipments.solar_panel.sil.events.PowerProductionLevel;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.HIOA_Composer;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>MILSimulationArchitectures</code>  defines the local MIL
 * simulation architecture pertaining to the electric meter component.
 *
 * <p><strong>Description</strong></p>
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
 * <p>Created on : 2023-11-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	LocalSimulationArchitectures
{
	/**
	 * create the local SIL real time simulation architecture for the
	 * {@code ElectricMeter} component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local SIL real time simulation architecture for the {@code ElectricMeter} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createElectricMeterSILArchitecture(
		String architectureURI, 
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		assert	architectureURI != null && !architectureURI.isEmpty() :
				new PreconditionException(
						"architectureURI != null && !architectureURI.isEmpty()");
		assert	rootModelURI != null && !rootModelURI.isEmpty() :
				new PreconditionException(
						"rootModelURI != null && !rootModelURI.isEmpty()");
		assert	simulatedTimeUnit != null :
				new PreconditionException("simulatedTimeUnit != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();

		// the electric meter electricity model accumulates the electric
		// power consumption and production, an atomic HIOA model hence we use
		// a RTAtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				ElectricMeterElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						ElectricMeterElectricitySILModel.class,
						ElectricMeterElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// The electricity models of all appliances will need to be put within
		// the ElectricMeter simulator to be able to share the variables
		// containing their power consumptions.
		atomicModelDescriptors.put(
				CoffeeMachineElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						CoffeeMachineElectricitySILModel.class,
						CoffeeMachineElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				FanElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						FanElectricitySILModel.class,
						FanElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				LaundryElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						LaundryElectricitySILModel.class,
						LaundryElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				KettleElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						KettleElectricitySILModel.class,
						KettleElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				BatteriesPowerSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						BatteriesPowerSILModel.class,
						BatteriesPowerSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				DeterministicSunIntensityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						DeterministicSunIntensityModel.class,
						DeterministicSunIntensityModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				SolarPanelPowerSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						SolarPanelPowerSILModel.class,
						SolarPanelPowerSILModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				GeneratorFuelSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						GeneratorFuelSILModel.class,
						GeneratorFuelSILModel.URI,
						GeneratorSimulationConfiguration.TIME_UNIT,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				GeneratorPowerSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						GeneratorPowerSILModel.class,
						GeneratorPowerSILModel.URI,
						GeneratorSimulationConfiguration.TIME_UNIT,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
				new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(ElectricMeterElectricitySILModel.URI);
		submodels.add(FanElectricitySILModel.URI);
		submodels.add(CoffeeMachineElectricitySILModel.URI);
		submodels.add(LaundryElectricitySILModel.URI);
		submodels.add(KettleElectricitySILModel.URI);
		submodels.add(BatteriesPowerSILModel.URI);
		submodels.add(DeterministicSunIntensityModel.URI);
		submodels.add(SolarPanelPowerSILModel.URI);
		submodels.add(GeneratorFuelSILModel.URI);
		submodels.add(GeneratorPowerSILModel.URI);

		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		imported.put(
				SwitchOnFan.class,
				new EventSink[] {
					new EventSink(FanElectricitySILModel.URI,
								  SwitchOnFan.class)
				});
		imported.put(
				SwitchOffFan.class,
				new EventSink[] {
					new EventSink(FanElectricitySILModel.URI,
								  SwitchOffFan.class)
				});
		imported.put(
				SetLowModeFan.class,
				new EventSink[] {
					new EventSink(FanElectricitySILModel.URI,
							SetLowModeFan.class)
				});
		imported.put(
				SetMediumModeFan.class,
				new EventSink[] {
					new EventSink(FanElectricitySILModel.URI,
							SetMediumModeFan.class)
				});
		imported.put(
				SetHighModeFan.class,
				new EventSink[] {
					new EventSink(FanElectricitySILModel.URI,
							SetHighModeFan.class)
				});

		imported.put(
				SwitchOnCoffeeMachine.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SwitchOnCoffeeMachine.class)
				});
		imported.put(
				SwitchOffCoffeeMachine.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SwitchOffCoffeeMachine.class)
				});
		imported.put(
				Heat.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  Heat.class)
				});
		
		imported.put(
				DoNotHeat.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  DoNotHeat.class)
				});
		imported.put(
				SetEcoModeCoffeeMachine.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetEcoModeCoffeeMachine.class)
				});
		imported.put(
				SetMaxModeCoffeeMachine.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetMaxModeCoffeeMachine.class)
				});
		imported.put(
				
						SetNormalModeCoffeeMachine.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetNormalModeCoffeeMachine.class)
				});
		imported.put(
				SetSuspendedModeCoffeeMachine.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetSuspendedModeCoffeeMachine.class)
				});
		imported.put(
				MakeCoffee.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  MakeCoffee.class),
					
				});
		imported.put(
				ServeCoffee.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  ServeCoffee.class)
				});
		imported.put(
				FillWaterCoffeeMachine.class,
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  FillWaterCoffeeMachine.class)
				});

		// Laundry events
		imported.put(
				SwitchOnLaundry.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SwitchOnLaundry.class)
				});
		imported.put(
				SwitchOffLaundry.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SwitchOffLaundry.class)
				});
		imported.put(
				StartWash.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  StartWash.class)
				});
		imported.put(
				CancelWash.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  CancelWash.class)
				});
		imported.put(
				SetDelicateModeLaundry.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SetDelicateModeLaundry.class)
				});
		imported.put(
				SetColorModeLaundry.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SetColorModeLaundry.class)
				});
		imported.put(
				SetWhiteModeLaundry.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SetWhiteModeLaundry.class)
				});
		imported.put(
				SetIntensiveModeLaundry.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SetIntensiveModeLaundry.class)
				});
		imported.put(
				SetWashTemperature.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SetWashTemperature.class)
				});
		imported.put(
				SetSpinSpeed.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SetSpinSpeed.class)
				});
		imported.put(
				SetPowerLaundry.class,
				new EventSink[] {
						new EventSink(LaundryElectricitySILModel.URI,
									  SetPowerLaundry.class)
				});

		// Kettle events
		imported.put(
				SwitchOnKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SwitchOnKettle.class)
				});
		imported.put(
				SwitchOffKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SwitchOffKettle.class)
				});
		imported.put(
				HeatKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  HeatKettle.class)
				});
		imported.put(
				DoNotHeatKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  DoNotHeatKettle.class)
				});
		imported.put(
				SetEcoModeKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetEcoModeKettle.class)
				});
		imported.put(
				SetNormalModeKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetNormalModeKettle.class)
				});
		imported.put(
				SetMaxModeKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetMaxModeKettle.class)
				});
		imported.put(
				SetSuspendedModeKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetSuspendedModeKettle.class)
				});
		imported.put(
				SetPowerKettle.class,
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetPowerKettle.class)
				});
		imported.put(SIL_StartCharging.class,
				new EventSink[] {
						new EventSink(BatteriesPowerSILModel.URI,
									  SIL_StartCharging.class)
				});
		imported.put(SIL_StopCharging.class,
				new EventSink[] {
						new EventSink(BatteriesPowerSILModel.URI,
									  SIL_StopCharging.class)
				});

		imported.put(
				SunriseEvent.class,
				new EventSink[] {
						new EventSink(DeterministicSunIntensityModel.URI,
									  SunriseEvent.class),
						new EventSink(SolarPanelPowerSILModel.URI,
									  SunriseEvent.class)
				});
		imported.put(
				SunsetEvent.class,
				new EventSink[] {
						new EventSink(DeterministicSunIntensityModel.URI,
									  SunsetEvent.class),
						new EventSink(SolarPanelPowerSILModel.URI,
									  SunsetEvent.class)
				});
		imported.put(
				Start.class,
				new EventSink[] {
						new EventSink(GeneratorFuelSILModel.URI,
									  Start.class),
						new EventSink(GeneratorPowerSILModel.URI,
									  Start.class)
				});
		imported.put(
				Stop.class,
				new EventSink[] {
						new EventSink(GeneratorFuelSILModel.URI,
									  Stop.class),
						new EventSink(GeneratorPowerSILModel.URI,
									  Stop.class)
				});
		imported.put(
				SIL_Refill.class,
				new EventSink[] {
						new EventSink(GeneratorFuelSILModel.URI,
								SIL_Refill.class)
				});
		
		Map<Class<? extends EventI>, ReexportedEvent> reexported =
				new HashMap<>();

		reexported.put(
		CurrentBatteriesLevel.class,
		new ReexportedEvent(BatteriesPowerSILModel.URI,
		CurrentBatteriesLevel.class));
		reexported.put(
		PowerProductionLevel.class,
		new ReexportedEvent(SolarPanelPowerSILModel.URI,
		PowerProductionLevel.class));
		reexported.put(
		TankEmpty.class,
		new ReexportedEvent(GeneratorFuelSILModel.URI,
		TankEmpty.class));
		reexported.put(
		TankNoLongerEmpty.class,
		new ReexportedEvent(GeneratorFuelSILModel.URI,
		TankNoLongerEmpty.class));
		reexported.put(
		CurrentFuelLevel.class,
		new ReexportedEvent(GeneratorFuelSILModel.URI,
		CurrentFuelLevel.class));
		reexported.put(
		CurrentFuelConsumption.class,
		new ReexportedEvent(GeneratorFuelSILModel.URI,
		CurrentFuelConsumption.class));
		reexported.put(
		CurrentPowerProduction.class,
		new ReexportedEvent(GeneratorPowerSILModel.URI,
		CurrentPowerProduction.class));

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   FanElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("currentFanIntensity",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   CoffeeMachineElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("currentCoffeeMachineIntensity",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   LaundryElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("currentLaundryIntensity",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   KettleElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("currentKettleIntensity",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});
		
		bindings.put(
				new VariableSource("batteriesInputPower",
								   Double.class,
								   BatteriesPowerSILModel.URI),
				new VariableSink[] {
					new VariableSink("batteriesInputPower",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});
		bindings.put(
				new VariableSource("batteriesOutputPower",
								   Double.class,
								   BatteriesPowerSILModel.URI),
				new VariableSink[] {
					new VariableSink("batteriesOutputPower",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});
		bindings.put(
				new VariableSource("batteriesRequiredPower",
								   Double.class,
								   ElectricMeterElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("batteriesRequiredPower",
									 Double.class,
									 BatteriesPowerSILModel.URI)
				});

		bindings.put(
				new VariableSource("sunIntensityCoef",
						   Double.class,
						   DeterministicSunIntensityModel.URI),
				new VariableSink[] {
						new VariableSink("sunIntensityCoef",
										 Double.class,
										 SolarPanelPowerSILModel.URI)
				});
		bindings.put(
				new VariableSource("solarPanelOutputPower",
						   Double.class,
						   SolarPanelPowerSILModel.URI),
				new VariableSink[] {
						new VariableSink("solarPanelOutputPower",
										 Double.class,
										 ElectricMeterElectricitySILModel.URI)
				});

		bindings.put(
				new VariableSource("generatorOutputPower",
								   Double.class,
								   GeneratorPowerSILModel.URI),
				new VariableSink[] {
					new VariableSink("generatorOutputPower",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI),
					new VariableSink("generatorOutputPower",
							 		 Double.class,
							 		 GeneratorFuelSILModel.URI)
				});
		bindings.put(
				new VariableSource("generatorRequiredPower",
								   Double.class,
								   ElectricMeterElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("generatorRequiredPower",
									 Double.class,
									 GeneratorPowerSILModel.URI)
				});
		
		Map<EventSource,EventSink[]> connections =
				new HashMap<EventSource,EventSink[]>();

		connections.put(
			new EventSource(
				ElectricMeterElectricitySILModel.URI,
				SIL_BatteriesRequiredPowerChanged.class),
			new EventSink[] {
				new EventSink(BatteriesPowerSILModel.URI,
							  SIL_BatteriesRequiredPowerChanged.class)
			});
		connections.put(
			new EventSource(
				ElectricMeterElectricitySILModel.URI,
				GeneratorRequiredPowerChanged.class),
			new EventSink[] {
				new EventSink(GeneratorPowerSILModel.URI,
							  GeneratorRequiredPowerChanged.class)
			});
		
				coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						ElectricMeterCoupledModel.class,
						rootModelURI,
						submodels,
						imported,
						null,
						null,
						null,
						null,
						null,
						bindings,
						new HIOA_Composer(),
						accelerationFactor));

		RTArchitecture architecture =
				new RTArchitecture(
						architectureURI,
						rootModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit,
						accelerationFactor);

		return architecture;
	}
}
// -----------------------------------------------------------------------------
