package etape4.equipments.generator.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
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
import etape2.equipments.generator.mil.GeneratorCoupledModel;
import etape2.equipments.generator.mil.GeneratorSimulationConfiguration;
import etape2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import etape4.equipments.generator.sil.events.CurrentFuelConsumption;
import etape4.equipments.generator.sil.events.CurrentFuelLevel;
import etape4.equipments.generator.sil.events.CurrentPowerProduction;
import etape4.equipments.generator.sil.events.SIL_Refill;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Local_SIL_SimulationArchitectures</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class provides static methods that create the local software-in-the-loop
 * real time simulation architectures for the component {@code GeneratorCyPhy}.
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
 * <p>Created on : 2025-12-30</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	Local_SIL_SimulationArchitectures
{
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	public static RTArchitecture	createGeneratorSIL_Architecture4UnitTest(
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

		// GeneratorStateSILModel is an atomic model, so needs an
		// AtomicModelDescriptor
		atomicModelDescriptors.put(
				GeneratorStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						GeneratorStateSILModel.class,
						GeneratorStateSILModel.URI,
						GeneratorSimulationConfiguration.TIME_UNIT,
						null,
						accelerationFactor));
		// GeneratorFuelSILModel is an atomic HIOA model, so needs an
		// AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				GeneratorFuelSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						GeneratorFuelSILModel.class,
						GeneratorFuelSILModel.URI,
						GeneratorSimulationConfiguration.TIME_UNIT,
						null,
						accelerationFactor));
		// GeneratorPowerSILModel is an atomic HIOA model, so needs an
		// AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				GeneratorPowerSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						GeneratorPowerSILModel.class,
						GeneratorPowerSILModel.URI,
						GeneratorSimulationConfiguration.TIME_UNIT,
						null,
						accelerationFactor));
		// BatteriesUnitTesterModel is an atomic HIOA model, so needs an
		// AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				GeneratorUnitTesterSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						GeneratorUnitTesterSILModel.class,
						GeneratorUnitTesterSILModel.URI,
						GeneratorSimulationConfiguration.TIME_UNIT,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(GeneratorStateSILModel.URI);
		submodels.add(GeneratorFuelSILModel.URI);
		submodels.add(GeneratorPowerSILModel.URI);
		submodels.add(GeneratorUnitTesterSILModel.URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(GeneratorStateSILModel.URI, Start.class),
				new EventSink[] {
						new EventSink(GeneratorPowerSILModel.URI, Start.class),
						new EventSink(GeneratorFuelSILModel.URI, Start.class)
				});
		connections.put(
				new EventSource(GeneratorStateSILModel.URI, Stop.class),
				new EventSink[] {
						new EventSink(GeneratorPowerSILModel.URI, Stop.class),
						new EventSink(GeneratorFuelSILModel.URI, Stop.class)
				});
		connections.put(
				new EventSource(GeneratorStateSILModel.URI, SIL_Refill.class),
				new EventSink[] {
						new EventSink(GeneratorFuelSILModel.URI,
									  SIL_Refill.class)
				});

		connections.put(
				new EventSource(GeneratorFuelSILModel.URI, TankEmpty.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  TankEmpty.class),
						new EventSink(GeneratorPowerSILModel.URI,
									  TankEmpty.class)
				});
		connections.put(
				new EventSource(GeneratorFuelSILModel.URI,
								TankNoLongerEmpty.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  TankNoLongerEmpty.class),
						new EventSink(GeneratorPowerSILModel.URI,
									  TankNoLongerEmpty.class)
				});
		connections.put(
				new EventSource(GeneratorFuelSILModel.URI,
								CurrentFuelLevel.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  CurrentFuelLevel.class)
				});
		connections.put(
				new EventSource(GeneratorFuelSILModel.URI,
								CurrentFuelConsumption.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  CurrentFuelConsumption.class)
				});

		connections.put(
				new EventSource(GeneratorPowerSILModel.URI,
								GeneratorRequiredPowerChanged.class),
				new EventSink[] {
						new EventSink(GeneratorFuelSILModel.URI,
									  GeneratorRequiredPowerChanged.class)
				});
		connections.put(
				new EventSource(GeneratorPowerSILModel.URI,
								CurrentPowerProduction.class),
				new EventSink[] {
						new EventSink(GeneratorStateSILModel.URI,
									  CurrentPowerProduction.class)
				});

		// variable sharing bindings between exporting and importing
		// models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		VariableSource source =
				new VariableSource("generatorOutputPower", Double.class,
								   GeneratorPowerSILModel.URI);
		VariableSink[] sinks = new VariableSink[] {
				new VariableSink("generatorOutputPower", Double.class,
								 GeneratorUnitTesterSILModel.URI),
				new VariableSink("generatorOutputPower", Double.class,
								 GeneratorFuelSILModel.URI)
		};
		bindings.put(source, sinks);
		source = new VariableSource("generatorRequiredPower", Double.class,
									GeneratorUnitTesterSILModel.URI);
		sinks = new VariableSink[] {
				new VariableSink("generatorRequiredPower", Double.class,
								 GeneratorPowerSILModel.URI)
		};
		bindings.put(source, sinks);

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						GeneratorCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						null,
						connections,
						null,
						null,
						null,
						bindings));

		// simulation architecture
		RTArchitecture architecture =
				new RTArchitecture(
						architectureURI,
						rootModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						GeneratorSimulationConfiguration.TIME_UNIT,
						accelerationFactor);

		return architecture;
	}

	public static RTArchitecture	createGeneratorSIL_Architecture4IntegrationTest(
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

		// GeneratorStateSILModel is an atomic model, so needs an
		// AtomicModelDescriptor
		atomicModelDescriptors.put(
				rootModelURI,
				RTAtomicModelDescriptor.create(
						GeneratorStateSILModel.class,
						rootModelURI,
						GeneratorSimulationConfiguration.TIME_UNIT,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

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
