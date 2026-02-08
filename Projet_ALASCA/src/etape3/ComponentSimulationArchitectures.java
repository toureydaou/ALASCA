package etape3;

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
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
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
import etape2.equipments.solar_panel.mil.SolarPanelCoupledModel;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
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
import etape4.equipments.batteries.sil.events.CurrentBatteriesLevel;
import etape4.equipments.batteries.sil.events.SIL_StartCharging;
import etape4.equipments.batteries.sil.events.SIL_StopCharging;
import etape4.equipments.generator.GeneratorCyPhy;
import etape4.equipments.generator.sil.GeneratorStateSILModel;
import etape4.equipments.generator.sil.events.CurrentFuelConsumption;
import etape4.equipments.generator.sil.events.CurrentFuelLevel;
import etape4.equipments.generator.sil.events.CurrentPowerProduction;
import etape4.equipments.generator.sil.events.SIL_Refill;
import etape4.equipments.solar_panel.SolarPanelCyPhy;
import etape4.equipments.solar_panel.sil.events.PowerProductionLevel;
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


// -----------------------------------------------------------------------------
/**
 * The class <code>ComponentSimulationArchitectures</code> defines the global
 * component simulation architectures for the whole HEM application.
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
public abstract class	ComponentSimulationArchitectures
{
	/**
	 * create the global SIL real time component simulation architecture for the
	 * HEM application.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code architectureURI != null && !architectureURI.isEmpty()}
	 * pre	{@code rootModelURI != null && !rootModelURI.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code return != null}
	 * post {@code return.getArchitectureURI().equals(architectureURI)}
	 * post	{@code return.getRootModelURI().equals(rootModelURI)}
	 * post	{@code return.getSimulationTimeUnit().equals(simulatedTimeUnit)}
	 * </pre>
	 *
	 * @param architectureURI		URI of the component model architecture to be created.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor for this run.
	 * @return						the global SIL real time simulation  architecture for the HEM application.
	 * @throws Exception			<i>to do</i>.
	 */
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

		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				BatteriesStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						BatteriesStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							CurrentBatteriesLevel.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							SIL_StartCharging.class,
							SIL_StopCharging.class},
						simulatedTimeUnit,
						BatteriesCyPhy.REFLECTION_INBOUND_PORT_URI));

		atomicModelDescriptors.put(
				SolarPanelCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						SolarPanelCoupledModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							PowerProductionLevel.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							SunriseEvent.class,
							SunsetEvent.class},
						simulatedTimeUnit,
						SolarPanelCyPhy.REFLECTION_INBOUND_PORT_URI));

		atomicModelDescriptors.put(
				GeneratorStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						GeneratorStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{
							TankEmpty.class, TankNoLongerEmpty.class,
							CurrentPowerProduction.class, CurrentFuelLevel.class,
							CurrentFuelConsumption.class},
						(Class<? extends EventI>[]) new Class<?>[]{
							Start.class, Stop.class, SIL_Refill.class},
						simulatedTimeUnit,
						GeneratorCyPhy.REFLECTION_INBOUND_PORT_URI));
		
		atomicModelDescriptors.put(
				FanStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						FanStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnFan.class,	// notice that the
							SwitchOffFan.class,	// exported events of
							SetLowModeFan.class,
							SetMediumModeFan.class,// the atomic model
							SetHighModeFan.class},	// appear here
						simulatedTimeUnit,
						FanCyPhy.REFLECTION_INBOUND_PORT_URI
						));

		atomicModelDescriptors.put(
				CoffeeMachineCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						CoffeeMachineCoupledModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
									// notice that the
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

		// Note: CoffeeMachineTemperatureSILModel is defined INSIDE CoffeeMachineCoupledModel
		// It should not be declared here as a separate atomic model descriptor

		// Laundry: root model is LaundryStateSILModel (atomic, no coupled)
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

		// Kettle: root model is KettleCoupledModel (coupled: State + Temperature)
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

		// The electric meter also has a SIL simulation model
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

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(FanStateSILModel.URI);
		submodels.add(CoffeeMachineCoupledModel.URI);
		// Note: CoffeeMachineTemperatureSILModel is INSIDE CoffeeMachineCoupledModel
		submodels.add(LaundryStateSILModel.URI);
		submodels.add(KettleCoupledModel.URI);
		submodels.add(ElectricMeterCoupledModel.URI);
		submodels.add(ElectricMeterCoupledModel.URI);
		submodels.add(BatteriesStateSILModel.URI);
		submodels.add(SolarPanelCoupledModel.URI);
		submodels.add(GeneratorStateSILModel.URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		// first, the events going from the hair dryer to the electric meter
		connections.put(
			new EventSource(FanStateSILModel.URI,
							SwitchOnFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOnFan.class)
			});
		connections.put(
			new EventSource(FanStateSILModel.URI,
							SwitchOffFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOffFan.class)
			});
		connections.put(
			new EventSource(FanStateSILModel.URI,
							SetLowModeFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
						SetLowModeFan.class)
			});
		connections.put(
			new EventSource(FanStateSILModel.URI,
							SetHighModeFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
						SetHighModeFan.class)
			});

		// Add connection for SetMediumModeFan
		connections.put(
			new EventSource(FanStateSILModel.URI,
							SetMediumModeFan.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
						SetMediumModeFan.class)
			});

		// second, the events going from the CoffeeMachine to the electric meter
		// and to the temperature model
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								SwitchOnCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  SwitchOnCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								SwitchOffCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  SwitchOffCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								Heat.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  Heat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								DoNotHeat.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  DoNotHeat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								SetEcoModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  SetEcoModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								SetMaxModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  SetMaxModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								SetNormalModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  SetNormalModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								SetSuspendedModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  SetSuspendedModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								MakeCoffee.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  MakeCoffee.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								ServeCoffee.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  ServeCoffee.class)
				});
		connections.put(
				new EventSource(CoffeeMachineCoupledModel.URI,
								FillWaterCoffeeMachine.class),
				new EventSink[] {
						new EventSink(ElectricMeterCoupledModel.URI,
									  FillWaterCoffeeMachine.class)
				});

		// third, the events going from the Laundry to the electric meter
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SwitchOnLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOnLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SwitchOffLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOffLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							StartWash.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  StartWash.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							CancelWash.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  CancelWash.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetDelicateModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetDelicateModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetColorModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetColorModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetWhiteModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetWhiteModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetIntensiveModeLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetIntensiveModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetWashTemperature.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetWashTemperature.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetSpinSpeed.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetSpinSpeed.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetPowerLaundry.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetPowerLaundry.class)
			});

		// fourth, the events going from the Kettle to the electric meter
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							SwitchOnKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOnKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							SwitchOffKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOffKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							HeatKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  HeatKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							DoNotHeatKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  DoNotHeatKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							SetEcoModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetEcoModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							SetNormalModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetNormalModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							SetMaxModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetMaxModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							SetSuspendedModeKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetSuspendedModeKettle.class)
			});
		connections.put(
			new EventSource(KettleCoupledModel.URI,
							SetPowerKettle.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetPowerKettle.class)
			});
		
		connections.put(
				new EventSource(BatteriesStateSILModel.URI,
								SIL_StartCharging.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SIL_StartCharging.class)
				});
		connections.put(
				new EventSource(BatteriesStateSILModel.URI,
								SIL_StopCharging.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SIL_StopCharging.class)
				});
		connections.put(
				new EventSource(ElectricMeterCoupledModel.URI,
								CurrentBatteriesLevel.class),
				new EventSink[] {
					new EventSink(BatteriesStateSILModel.URI,
								  CurrentBatteriesLevel.class)
				});

		// events exchanged between the solar panel the electric meter
		connections.put(
				new EventSource(SolarPanelCoupledModel.URI,
								SunriseEvent.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SunriseEvent.class)
				});
		connections.put(
				new EventSource(SolarPanelCoupledModel.URI,
								SunsetEvent.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SunsetEvent.class)
				});
		connections.put(
				new EventSource(ElectricMeterCoupledModel.URI,
								PowerProductionLevel.class),
				new EventSink[] {
					new EventSink(SolarPanelCoupledModel.URI,
							PowerProductionLevel.class)
				});

		// events exchanged between the generator the electric meter
		connections.put(
				new EventSource(GeneratorStateSILModel.URI,
								Start.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  Start.class)
				});
		connections.put(
				new EventSource(GeneratorStateSILModel.URI,
								Stop.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  Stop.class)
				});
		connections.put(
				new EventSource(GeneratorStateSILModel.URI,
								SIL_Refill.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SIL_Refill.class)
				});
		connections.put(
				new EventSource(ElectricMeterCoupledModel.URI,
								TankEmpty.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  TankEmpty.class)
					});
		connections.put(
				new EventSource(ElectricMeterCoupledModel.URI,
								TankNoLongerEmpty.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  TankNoLongerEmpty.class)
					});
		connections.put(
				new EventSource(ElectricMeterCoupledModel.URI,
								CurrentPowerProduction.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  CurrentPowerProduction.class)
					});
		connections.put(
				new EventSource(ElectricMeterCoupledModel.URI,
								CurrentFuelLevel.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  CurrentFuelLevel.class)
					});
		connections.put(
				new EventSource(ElectricMeterCoupledModel.URI,
								CurrentFuelConsumption.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  CurrentFuelConsumption.class)
					});

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
// -----------------------------------------------------------------------------
