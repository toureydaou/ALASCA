package etape4;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import etape2.GlobalCoupledModel;
import etape2.equipments.coffeemachine.mil.CoffeeMachineCoupledModel;
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
import etape2.equipments.kettle.mil.KettleCoupledModel;
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
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.solar_panel.mil.SolarPanelCoupledModel;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
import etape3.CoordinatorComponent;
import etape4.equipments.batteries.sil.events.SIL_StartCharging;
import etape4.equipments.batteries.sil.events.SIL_StopCharging;
import etape4.equipments.generator.sil.events.SIL_Refill;
import etape3.equipements.coffee_machine.CoffeeMachineCyPhy;
import etape3.equipements.fan.FanCyPhy;
import etape3.equipements.fan.sil.FanStateSILModel;
import etape3.equipements.kettle.KettleCyPhy;
import etape3.equipements.laundry.LaundryCyPhy;
import etape3.equipements.laundry.sil.LaundryStateSILModel;
import etape3.equipements.meter.ElectricMeterCyPhy;
import etape3.equipements.meter.sil.ElectricMeterCoupledModel;
import etape4.equipments.batteries.BatteriesCyPhy;
import etape4.equipments.batteries.sil.BatteriesStateSILModel;
import etape4.equipments.generator.GeneratorCyPhy;
import etape4.equipments.generator.sil.GeneratorStateSILModel;
import etape4.equipments.solar_panel.SolarPanelCyPhy;
import fr.sorbonne_u.components.cyphy.plugins.devs.CoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentModelArchitecture;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>ComponentSimulationArchitectures</code> defines the global
 * component simulation architectures for the Etape 4 HEM application.
 *
 * <p>
 * Extends the Etape 3 architecture by adding energy source models:
 * Generator, SolarPanel, and Batteries.
 * </p>
 */
public abstract class ComponentSimulationArchitectures
{
	@SuppressWarnings("unchecked")
	public static RTComponentModelArchitecture
									createComponentSimulationArchitectures(
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

		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// =====================================================================
		// Consumer equipment models (same as etape3)
		// =====================================================================

		// Fan
		atomicModelDescriptors.put(
				FanStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						FanStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnFan.class,
							SwitchOffFan.class,
							SetLowModeFan.class,
							SetMediumModeFan.class,
							SetHighModeFan.class},
						simulatedTimeUnit,
						FanCyPhy.REFLECTION_INBOUND_PORT_URI
						));

		// Coffee Machine
		atomicModelDescriptors.put(
				CoffeeMachineCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						CoffeeMachineCoupledModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnCoffeeMachine.class,
							SwitchOffCoffeeMachine.class,
							Heat.class,
							DoNotHeat.class,
							SetEcoModeCoffeeMachine.class,
							SetMaxModeCoffeeMachine.class,
							SetNormalModeCoffeeMachine.class,
							SetSuspendedModeCoffeeMachine.class,
							MakeCoffee.class,
							ServeCoffee.class,
							FillWaterCoffeeMachine.class},
						simulatedTimeUnit,
						CoffeeMachineCyPhy.REFLECTION_INBOUND_PORT_URI));

		// Laundry
		atomicModelDescriptors.put(
				LaundryStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						LaundryStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnLaundry.class,
							SwitchOffLaundry.class,
							StartWash.class,
							CancelWash.class,
							SetDelicateModeLaundry.class,
							SetColorModeLaundry.class,
							SetWhiteModeLaundry.class,
							SetIntensiveModeLaundry.class,
							SetWashTemperature.class,
							SetSpinSpeed.class,
							SetPowerLaundry.class},
						simulatedTimeUnit,
						LaundryCyPhy.REFLECTION_INBOUND_PORT_URI));

		// Kettle
		atomicModelDescriptors.put(
				KettleCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						KettleCoupledModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnKettle.class,
							SwitchOffKettle.class,
							HeatKettle.class,
							DoNotHeatKettle.class,
							SetEcoModeKettle.class,
							SetNormalModeKettle.class,
							SetMaxModeKettle.class,
							SetSuspendedModeKettle.class,
							SetPowerKettle.class},
						simulatedTimeUnit,
						KettleCyPhy.REFLECTION_INBOUND_PORT_URI));

		// Electric Meter
		atomicModelDescriptors.put(
				ElectricMeterCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						ElectricMeterCoupledModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnFan.class,
							SwitchOffFan.class,
							SetLowModeFan.class,
							SetMediumModeFan.class,
							SetHighModeFan.class,
							SwitchOnCoffeeMachine.class,
							SwitchOffCoffeeMachine.class,
							Heat.class,
							DoNotHeat.class,
							SetEcoModeCoffeeMachine.class,
							SetMaxModeCoffeeMachine.class,
							SetNormalModeCoffeeMachine.class,
							SetSuspendedModeCoffeeMachine.class,
							MakeCoffee.class,
							ServeCoffee.class,
							FillWaterCoffeeMachine.class,
							SwitchOnLaundry.class,
							SwitchOffLaundry.class,
							StartWash.class,
							CancelWash.class,
							SetDelicateModeLaundry.class,
							SetColorModeLaundry.class,
							SetWhiteModeLaundry.class,
							SetIntensiveModeLaundry.class,
							SetWashTemperature.class,
							SetSpinSpeed.class,
							SetPowerLaundry.class,
							SwitchOnKettle.class,
							SwitchOffKettle.class,
							HeatKettle.class,
							DoNotHeatKettle.class,
							SetEcoModeKettle.class,
							SetNormalModeKettle.class,
							SetMaxModeKettle.class,
							SetSuspendedModeKettle.class,
							SetPowerKettle.class},
						(Class<? extends EventI>[]) new Class<?>[]{},
						simulatedTimeUnit,
						ElectricMeterCyPhy.REFLECTION_INBOUND_PORT_URI));

		// =====================================================================
		// Energy source models (new in etape4)
		// =====================================================================

		// Generator - standalone model, events triggered internally via
		// asp.triggerExternalEvent(). Must declare exported events to match
		// the @ModelExternalEvents annotation on GeneratorStateSILModel.
		atomicModelDescriptors.put(
				GeneratorStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						GeneratorStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							Start.class, Stop.class, SIL_Refill.class},
						simulatedTimeUnit,
						GeneratorCyPhy.REFLECTION_INBOUND_PORT_URI));

		// Solar Panel - coupled model with sun rise/set + state models.
		// The local integration test architecture re-exports SunriseEvent
		// and SunsetEvent from DeterministicSunRiseAndSetModel.
		atomicModelDescriptors.put(
				SolarPanelCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						SolarPanelCoupledModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SunriseEvent.class, SunsetEvent.class},
						simulatedTimeUnit,
						SolarPanelCyPhy.REFLECTION_INBOUND_PORT_URI));

		// Batteries - standalone model, events triggered internally via
		// asp.triggerExternalEvent(). Must declare exported events to match
		// the @ModelExternalEvents annotation on BatteriesStateSILModel.
		atomicModelDescriptors.put(
				BatteriesStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						BatteriesStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SIL_StartCharging.class, SIL_StopCharging.class},
						simulatedTimeUnit,
						BatteriesCyPhy.REFLECTION_INBOUND_PORT_URI));

		// =====================================================================
		// Coupled model (root)
		// =====================================================================

		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		// Consumer equipment
		submodels.add(FanStateSILModel.URI);
		submodels.add(CoffeeMachineCoupledModel.URI);
		submodels.add(LaundryStateSILModel.URI);
		submodels.add(KettleCoupledModel.URI);
		submodels.add(ElectricMeterCoupledModel.URI);
		// Energy sources
		submodels.add(GeneratorStateSILModel.URI);
		submodels.add(SolarPanelCoupledModel.URI);
		submodels.add(BatteriesStateSILModel.URI);

		// =====================================================================
		// Event connections (consumer equipment → ElectricMeter)
		// =====================================================================

		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		// Fan → ElectricMeter
		connections.put(
			new EventSource(FanStateSILModel.URI, SwitchOnFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SwitchOnFan.class)
			});
		connections.put(
			new EventSource(FanStateSILModel.URI, SwitchOffFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SwitchOffFan.class)
			});
		connections.put(
			new EventSource(FanStateSILModel.URI, SetLowModeFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetLowModeFan.class)
			});
		connections.put(
			new EventSource(FanStateSILModel.URI, SetHighModeFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetHighModeFan.class)
			});
		connections.put(
			new EventSource(FanStateSILModel.URI, SetMediumModeFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetMediumModeFan.class)
			});

		// CoffeeMachine → ElectricMeter
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, SwitchOnCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, SwitchOnCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, SwitchOffCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, SwitchOffCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, Heat.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, Heat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, DoNotHeat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, SetEcoModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, SetEcoModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, SetMaxModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, SetMaxModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, SetNormalModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, SetNormalModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, SetSuspendedModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, SetSuspendedModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, MakeCoffee.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, MakeCoffee.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, ServeCoffee.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, ServeCoffee.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI, FillWaterCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI, FillWaterCoffeeMachine.class)
				});

		// Laundry → ElectricMeter
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SwitchOnLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SwitchOnLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SwitchOffLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SwitchOffLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, StartWash.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, StartWash.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, CancelWash.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, CancelWash.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SetDelicateModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetDelicateModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SetColorModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetColorModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SetWhiteModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetWhiteModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SetIntensiveModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetIntensiveModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SetWashTemperature.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetWashTemperature.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SetSpinSpeed.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetSpinSpeed.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI, SetPowerLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetPowerLaundry.class)
			});

		// Kettle → ElectricMeter
		connections.put(
			new EventSource(KettleCoupledModel.URI, SwitchOnKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SwitchOnKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, SwitchOffKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SwitchOffKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, HeatKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, HeatKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, DoNotHeatKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, DoNotHeatKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, SetEcoModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetEcoModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, SetNormalModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetNormalModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, SetMaxModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetMaxModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, SetSuspendedModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetSuspendedModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI, SetPowerKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI, SetPowerKettle.class)
			});

		// Note: Energy sources (Generator, SolarPanel, Batteries) have NO
		// cross-component event connections. Their events are triggered
		// internally via asp.triggerExternalEvent() and their production
		// data is read via component ports.

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				RTComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null,
						accelerationFactor));

		RTComponentModelArchitecture architecture =
				new RTComponentModelArchitecture(
						architectureURI,
						rootModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
}
