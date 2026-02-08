package etape4.equipments.solar_panel.sil;

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
import etape2.equipments.solar_panel.mil.DeterministicSunIntensityModel;
import etape2.equipments.solar_panel.mil.DeterministicSunRiseAndSetModel;
import etape2.equipments.solar_panel.mil.SolarPanelCoupledModel;
import etape2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
import etape4.equipments.solar_panel.sil.events.PowerProductionLevel;
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
 * The class <code>Local_SIL_SimulationArchitectures</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class provides static methods that create the local software-in-the-loop
 * real time simulation architectures for the component {@code SolarPanelCyPhy}.
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

	public static RTArchitecture	createSolarPanelSIL_Architecture4UnitTest(
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

		// DeterministicSunRiseAndSetModel is an atomic event scheduling
		// model, so needs an AtomicModelDescriptor
		atomicModelDescriptors.put(
				DeterministicSunRiseAndSetModel.URI,
				RTAtomicModelDescriptor.create(
						DeterministicSunRiseAndSetModel.class,
						DeterministicSunRiseAndSetModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		// SolarPanelPowerSILModel is an atomic HIOA model, so needs an
		// AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				DeterministicSunIntensityModel.URI,
				RTAtomicHIOA_Descriptor.create(
						DeterministicSunIntensityModel.class,
						DeterministicSunIntensityModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		// DeterministicSunRiseAndSetModel is an atomic model, so needs an
		// AtomicModelDescriptor
		atomicModelDescriptors.put(
				SolarPanelStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						SolarPanelStateSILModel.class,
						SolarPanelStateSILModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		// SolarPanelPowerSILModel is an atomic HIOA model, so needs an
		// AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				SolarPanelPowerSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						SolarPanelPowerSILModel.class,
						SolarPanelPowerSILModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(DeterministicSunRiseAndSetModel.URI);
		submodels.add(DeterministicSunIntensityModel.URI);
		submodels.add(SolarPanelStateSILModel.URI);
		submodels.add(SolarPanelPowerSILModel.URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(DeterministicSunRiseAndSetModel.URI,
								SunriseEvent.class),
				new EventSink[] {
						new EventSink(DeterministicSunIntensityModel.URI,
									  SunriseEvent.class),
						new EventSink(SolarPanelPowerSILModel.URI,
									  SunriseEvent.class)
				});
		connections.put(
				new EventSource(DeterministicSunRiseAndSetModel.URI,
								SunsetEvent.class),
				new EventSink[] {
						new EventSink(DeterministicSunIntensityModel.URI,
									  SunsetEvent.class),
						new EventSink(SolarPanelPowerSILModel.URI,
									  SunsetEvent.class)
				});

		connections.put(
				new EventSource(SolarPanelPowerSILModel.URI,
								PowerProductionLevel.class),
				new EventSink[] {
						new EventSink(SolarPanelStateSILModel.URI,
									  PowerProductionLevel.class)
				});

		// variable sharing bindings between exporting and importing
		// models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		VariableSource source =
				new VariableSource("sunIntensityCoef", Double.class,
								   DeterministicSunIntensityModel.URI);
		VariableSink[] sinks =
				new VariableSink[] {
						new VariableSink("sunIntensityCoef", Double.class,
										 SolarPanelPowerSILModel.URI)
		};
		bindings.put(source, sinks);

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						SolarPanelCoupledModel.class,
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
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						accelerationFactor);

		return architecture;
	}

	public static RTArchitecture	createSolarPanelSIL_Architecture4IntegrationTest(
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

		// DeterministicSunRiseAndSetModel is an atomic event scheduling
		// model, so needs an AtomicModelDescriptor
		atomicModelDescriptors.put(
				DeterministicSunRiseAndSetModel.URI,
				RTAtomicModelDescriptor.create(
						DeterministicSunRiseAndSetModel.class,
						DeterministicSunRiseAndSetModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		// BatteriesPowerModel is an atomic HIOA model, so needs an
		// AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				SolarPanelStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						SolarPanelStateSILModel.class,
						SolarPanelStateSILModel.URI,
						SolarPanelSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(DeterministicSunRiseAndSetModel.URI);
		submodels.add(SolarPanelStateSILModel.URI);

		// events imported by the coupled model and passed to submodels
		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		imported.put(
				PowerProductionLevel.class,
				new EventSink[] {
					new EventSink(SolarPanelStateSILModel.URI,
								  PowerProductionLevel.class)
				});

		// events emitted by submodels reexported by the coupled model
		Map<Class<? extends EventI>, ReexportedEvent> reexported =
															new HashMap<>();

		reexported.put(
				SunriseEvent.class,
				new ReexportedEvent(DeterministicSunRiseAndSetModel.URI,
									SunriseEvent.class));
		reexported.put(
				SunsetEvent.class,
				new ReexportedEvent(DeterministicSunRiseAndSetModel.URI,
									SunsetEvent.class));

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						SolarPanelCoupledModel.class,
						rootModelURI,
						submodels,
						imported,
						reexported,
						null,
						null,
						null,
						null,
						null,
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
