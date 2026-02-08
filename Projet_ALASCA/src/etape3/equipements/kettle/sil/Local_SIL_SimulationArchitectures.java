package etape3.equipements.kettle.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
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
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Local_SIL_SimulationArchitectures</code> defines the local
 * software-in-the-loop simulation architectures pertaining to the kettle.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The class provides static methods that create the local software-in-the-loop
 * real time simulation architectures for the {@code KettleCyPhy} component.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public abstract class	Local_SIL_SimulationArchitectures
{
	/**
	 * Create the local SIL simulation architecture for the {@code KettleCyPhy}
	 * component used in unit tests.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * In this architecture, the kettle simulator consists of three atomic models:
	 * </p>
	 * <ol>
	 * <li>{@code KettleStateSILModel}: tracks state/mode, reemits events.</li>
	 * <li>{@code KettleElectricitySILModel}: tracks electricity consumption,
	 *     exports {@code currentIntensity} and {@code currentHeatingPower}.</li>
	 * <li>{@code KettleTemperatureSILModel}: simulates water temperature,
	 *     imports {@code currentHeatingPower}.</li>
	 * </ol>
	 *
	 * @param architectureURI		URI of the simulation architecture.
	 * @param rootModelURI			URI of the root model.
	 * @param simulatedTimeUnit		simulated time unit.
	 * @param accelerationFactor	acceleration factor.
	 * @return						the local SIL architecture for unit tests.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createKettleSIL_Architecture4UnitTest(
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

		// Atomic model descriptors
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// State model: only exchanges events (AtomicModel)
		atomicModelDescriptors.put(
				KettleStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						KettleStateSILModel.class,
						KettleStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// Electricity model: HIOA with exported variables
		atomicModelDescriptors.put(
				KettleElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						KettleElectricitySILModel.class,
						KettleElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// Temperature model: HIOA with imported/internal variables
		atomicModelDescriptors.put(
				KettleTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						KettleTemperatureSILModel.class,
						KettleTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// Coupled model descriptors
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// Submodels
		Set<String> submodels = new HashSet<String>();
		submodels.add(KettleStateSILModel.URI);
		submodels.add(KettleElectricitySILModel.URI);
		submodels.add(KettleTemperatureSILModel.URI);

		// Event connections: State → Electricity and Temperature
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SwitchOnKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SwitchOnKettle.class),
						new EventSink(KettleTemperatureSILModel.URI,
									  SwitchOnKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SwitchOffKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SwitchOffKettle.class),
						new EventSink(KettleTemperatureSILModel.URI,
									  SwitchOffKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								HeatKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  HeatKettle.class),
						new EventSink(KettleTemperatureSILModel.URI,
									  HeatKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								DoNotHeatKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  DoNotHeatKettle.class),
						new EventSink(KettleTemperatureSILModel.URI,
									  DoNotHeatKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SetEcoModeKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetEcoModeKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SetNormalModeKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetNormalModeKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SetMaxModeKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetMaxModeKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SetSuspendedModeKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetSuspendedModeKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SetPowerKettle.class),
				new EventSink[] {
						new EventSink(KettleElectricitySILModel.URI,
									  SetPowerKettle.class)
				});

		// Variable bindings: Electricity → Temperature
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(
				new VariableSource("currentHeatingPower",
								   Double.class,
								   KettleElectricitySILModel.URI),
				new VariableSink[] {
						new VariableSink("currentHeatingPower",
										 Double.class,
										 KettleTemperatureSILModel.URI)
				});

		// Coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						KettleCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						null,
						connections,
						null,
						null,
						null,
						bindings,
						accelerationFactor));

		// Simulation architecture
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

	/**
	 * Create the local SIL simulation architecture for the {@code KettleCyPhy}
	 * component when used in integration tests.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * In integration test mode, the {@code KettleElectricitySILModel} is moved
	 * to the {@code ElectricMeterCyPhy} component. The coupled model keeps
	 * only {@code KettleStateSILModel} and {@code KettleTemperatureSILModel},
	 * and reexports all events from the state model towards the electric meter.
	 * </p>
	 *
	 * @param architectureURI		URI of the simulation architecture.
	 * @param rootModelURI			URI of the root model.
	 * @param simulatedTimeUnit		simulated time unit.
	 * @param accelerationFactor	acceleration factor.
	 * @return						the local SIL architecture for integration tests.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createKettle_SIL_LocalArchitecture4IntegrationTest(
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

		// Atomic model descriptors
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		atomicModelDescriptors.put(
				KettleStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						KettleStateSILModel.class,
						KettleStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				KettleTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						KettleTemperatureSILModel.class,
						KettleTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// Coupled model descriptors
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// Submodels (no electricity model in integration mode)
		Set<String> submodels = new HashSet<String>();
		submodels.add(KettleStateSILModel.URI);
		submodels.add(KettleTemperatureSILModel.URI);

		// Reexported events: all events from State model are reexported
		// towards the ElectricMeter's electricity model
		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				SwitchOnKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									SwitchOnKettle.class));
		reexported.put(
				SwitchOffKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									SwitchOffKettle.class));
		reexported.put(
				HeatKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									HeatKettle.class));
		reexported.put(
				DoNotHeatKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									DoNotHeatKettle.class));
		reexported.put(
				SetEcoModeKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									SetEcoModeKettle.class));
		reexported.put(
				SetNormalModeKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									SetNormalModeKettle.class));
		reexported.put(
				SetMaxModeKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									SetMaxModeKettle.class));
		reexported.put(
				SetSuspendedModeKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									SetSuspendedModeKettle.class));
		reexported.put(
				SetPowerKettle.class,
				new ReexportedEvent(KettleStateSILModel.URI,
									SetPowerKettle.class));

		// Internal event connections: State → Temperature
		// (only heating-related events affect temperature)
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SwitchOnKettle.class),
				new EventSink[] {
						new EventSink(KettleTemperatureSILModel.URI,
									  SwitchOnKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								SwitchOffKettle.class),
				new EventSink[] {
						new EventSink(KettleTemperatureSILModel.URI,
									  SwitchOffKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								HeatKettle.class),
				new EventSink[] {
						new EventSink(KettleTemperatureSILModel.URI,
									  HeatKettle.class)
				});
		connections.put(
				new EventSource(KettleStateSILModel.URI,
								DoNotHeatKettle.class),
				new EventSink[] {
						new EventSink(KettleTemperatureSILModel.URI,
									  DoNotHeatKettle.class)
				});

		// No variable bindings in integration mode
		// (currentHeatingPower is null → temperature model uses fallback)
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		// Coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						KettleCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						reexported,
						connections,
						null,
						null,
						null,
						bindings,
						accelerationFactor));

		// Simulation architecture
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
