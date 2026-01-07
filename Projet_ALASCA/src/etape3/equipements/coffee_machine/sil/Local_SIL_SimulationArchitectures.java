package etape3.equipements.coffee_machine.sil;

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

import etape2.equipments.coffeemachine.mil.CoffeeMachineCoupledModel;
import etape2.equipments.coffeemachine.mil.events.*;
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
 * software-in-the-loop simulation architectures pertaining to the coffee
 * machine appliance.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The class provides static methods that create the local software-in-the-loop
 * real time simulation architectures for the {@code CoffeeMachineCyPhy} component.
 * </p>
 * <p>
 * The simulation architectures created in this class are local to components
 * in the sense that they define the simulators that are created and run by
 * each component. The one for unit test is meant to be executed alone in the
 * {@code CoffeeMachineCyPhy} component. The one for integration test is meant to
 * be executed within a larger simulator for the entire application component
 * architectures where they are seen as atomic models to be composed by a
 * coupled model that will reside in a coordinator component.
 * </p>
 *
 * <p><strong>Implementation  Invariants</strong></p>
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
 * <p>Created on : 2025-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	Local_SIL_SimulationArchitectures
{
	/**
	 * create the local software-in-the-loop simulation architecture for the
	 * {@code CoffeeMachineCyPhy} component used in unit tests.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * In this simulation architecture, the coffee machine simulator consists of
	 * three atomic models:
	 * </p>
	 * <ol>
	 * <li>The {@code CoffeeMachineStateSILModel} keeps track of the state
	 *   (switched on, switched off, heating) and mode (SUSPEND, ECO, NORMAL, MAX)
	 *   of the coffee machine. The state changes are triggered by the reception
	 *   of external events directly received from the {@code CoffeeMachineCyPhy}
	 *   component methods; whenever a state change occurs, the triggering event
	 *   is reemitted towards the {@code CoffeeMachineElectricitySILModel} and the
	 *   {@code CoffeeMachineTemperatureSILModel}.</li>
	 * <li>The {@code CoffeeMachineElectricitySILModel} keeps track of the electric
	 *   power consumed by the coffee machine in a variable <code>currentIntensity</code>,
	 *   which is exported but not used in this simulation of the coffee machine in
	 *   isolation. It also manages the water level and current heating power.</li>
	 * <li>The {@code CoffeeMachineTemperatureSILModel} simulates the temperature
	 *   of the water in the coffee machine, using the current power of the
	 *   machine, which it keeps track of through the imported variables
	 *   {@code currentHeatingPower} and {@code currentWaterLevel}. The evolution
	 *   of the temperature also depends upon the fact that the machine actually
	 *   is heating or not.</li>
	 * </ol>
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
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local software-in-the-loop real time simulation architecture for the unit tests of the {@code CoffeeMachine} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createCoffeeMachineSIL_Architecture4UnitTest(
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

		// the coffee machine state model only exchanges events, an atomic model
		// hence we use an RTAtomicModelDescriptor
		atomicModelDescriptors.put(
				CoffeeMachineStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						CoffeeMachineStateSILModel.class,
						CoffeeMachineStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// the coffee machine models simulating its electricity consumption and
		// temperature are atomic HIOA models hence we use RTAtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				CoffeeMachineElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						CoffeeMachineElectricitySILModel.class,
						CoffeeMachineElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				CoffeeMachineTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						CoffeeMachineTemperatureSILModel.class,
						CoffeeMachineTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(CoffeeMachineStateSILModel.URI);
		submodels.add(CoffeeMachineElectricitySILModel.URI);
		submodels.add(CoffeeMachineTemperatureSILModel.URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								SwitchOnCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SwitchOnCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								SwitchOffCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SwitchOffCoffeeMachine.class),
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  SwitchOffCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								Heat.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  Heat.class),
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  Heat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								DoNotHeat.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  DoNotHeat.class),
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  DoNotHeat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								SetEcoModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetEcoModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								SetMaxModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetMaxModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								SetNormalModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetNormalModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								SetSuspendedModeCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  SetSuspendedModeCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								MakeCoffee.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  MakeCoffee.class),
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  MakeCoffee.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								ServeCoffee.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  ServeCoffee.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								FillWaterCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineElectricitySILModel.URI,
									  FillWaterCoffeeMachine.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(
				new VariableSource("currentHeatingPower",
								   Double.class,
								   CoffeeMachineElectricitySILModel.URI),
				new VariableSink[] {
						new VariableSink("currentHeatingPower",
										 Double.class,
										 CoffeeMachineTemperatureSILModel.URI)
				});
		bindings.put(
				new VariableSource("currentWaterLevel",
								   Double.class,
								   CoffeeMachineElectricitySILModel.URI),
				new VariableSink[] {
						new VariableSink("currentWaterLevel",
										 Double.class,
										 CoffeeMachineTemperatureSILModel.URI)
				});

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						CoffeeMachineCoupledModel.class,
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

		// simulation architecture
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
	 * create the local software-in-the-loop real time simulation architecture
	 * for the {@code CoffeeMachineCyPhy} component when used in integration tests.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * The simulation architecture created for {@code CoffeeMachineCyPhy} real time
	 * integration tests is similar to the one used for unit test, except that
	 * the {@code CoffeeMachineElectricitySILModel} is moved to the local
	 * simulator of the {@code ElectricMeterCyPhy} component to cater for
	 * the binding of its exported variable {@code currentIntensity}
	 * with the electricity model of the electric meter. Because of this move,
	 * the state changes in the coffee machine triggered by the events must be
	 * transmitted to {@code CoffeeMachineElectricitySILModel} by making the
	 * coupled model reexport them.
	 * </p>
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
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local SIL real time simulation architecture for the integration tests of the {@code CoffeeMachine} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createCoffeeMachine_SIL_LocalArchitecture4IntegrationTest(
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
				CoffeeMachineStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						CoffeeMachineStateSILModel.class,
						CoffeeMachineStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				CoffeeMachineTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						CoffeeMachineTemperatureSILModel.class,
						CoffeeMachineTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(CoffeeMachineStateSILModel.URI);
		submodels.add(CoffeeMachineTemperatureSILModel.URI);

		// events emitted by submodels that are reexported towards other models
		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				SwitchOnCoffeeMachine.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									SwitchOnCoffeeMachine.class));
		reexported.put(
				SwitchOffCoffeeMachine.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									SwitchOffCoffeeMachine.class));
		reexported.put(
				Heat.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									Heat.class));
		reexported.put(
				DoNotHeat.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									DoNotHeat.class));
		reexported.put(
				SetEcoModeCoffeeMachine.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									SetEcoModeCoffeeMachine.class));
		reexported.put(
				SetMaxModeCoffeeMachine.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									SetMaxModeCoffeeMachine.class));
		reexported.put(
				SetNormalModeCoffeeMachine.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									SetNormalModeCoffeeMachine.class));
		reexported.put(
				SetSuspendedModeCoffeeMachine.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									SetSuspendedModeCoffeeMachine.class));
		reexported.put(
				MakeCoffee.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									MakeCoffee.class));
		reexported.put(
				ServeCoffee.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									ServeCoffee.class));
		reexported.put(
				FillWaterCoffeeMachine.class,
				new ReexportedEvent(CoffeeMachineStateSILModel.URI,
									FillWaterCoffeeMachine.class));

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								SwitchOffCoffeeMachine.class),
				new EventSink[] {
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  SwitchOffCoffeeMachine.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								Heat.class),
				new EventSink[] {
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  Heat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								DoNotHeat.class),
				new EventSink[] {
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  DoNotHeat.class)
				});
		connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI,
								MakeCoffee.class),
				new EventSink[] {
						new EventSink(CoffeeMachineTemperatureSILModel.URI,
									  MakeCoffee.class)
				});

		// variable bindings between exporting and importing models
		// Note: in integration mode, currentHeatingPower and currentWaterLevel
		// are imported from the electricity model in the electric meter
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						CoffeeMachineCoupledModel.class,
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

		// simulation architecture
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
