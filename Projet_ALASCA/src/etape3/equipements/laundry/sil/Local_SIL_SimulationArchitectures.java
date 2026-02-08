package etape3.equipements.laundry.sil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import etape2.equipments.laundry.mil.LaundryCoupledModel;
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
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Local_SIL_SimulationArchitectures</code> defines the local
 * software-in-the-loop simulation architectures pertaining to the laundry
 * machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The class provides static methods that create the local software-in-the-loop
 * real time simulation architectures for the component {@code LaundryCyPhy}.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public abstract class Local_SIL_SimulationArchitectures
{
	/**
	 * create the local SIL real time simulation architecture for the
	 * {@code LaundryCyPhy} component when performing SIL unit tests.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * In laundry unit tests, the local simulation architecture contains:
	 * </p>
	 * <ol>
	 * <li>The {@code LaundryStateSILModel} keeps track of state changes and
	 *   re-emits events towards the electricity model.</li>
	 * <li>The {@code LaundryElectricitySILModel} keeps track of the electric
	 *   power consumed by the laundry in a variable
	 *   {@code currentIntensity}.</li>
	 * </ol>
	 *
	 * @param architectureURI		URI to be given to the created architecture.
	 * @param rootModelURI			URI of the root model.
	 * @param simulatedTimeUnit		simulated time unit.
	 * @param accelerationFactor	acceleration factor.
	 * @return						the local SIL architecture for unit test.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture createLaundrySIL_Architecture4UnitTest(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		assert architectureURI != null && !architectureURI.isEmpty() :
			new PreconditionException(
					"architectureURI != null && !architectureURI.isEmpty()");
		assert rootModelURI != null && !rootModelURI.isEmpty() :
			new PreconditionException(
					"rootModelURI != null && !rootModelURI.isEmpty()");
		assert simulatedTimeUnit != null :
			new PreconditionException("simulatedTimeUnit != null");
		assert accelerationFactor > 0.0 :
			new PreconditionException("accelerationFactor > 0.0");

		Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();

		atomicModelDescriptors.put(
				LaundryElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						LaundryElectricitySILModel.class,
						LaundryElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				LaundryStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						LaundryStateSILModel.class,
						LaundryStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String, CoupledModelDescriptor> coupledModelDescriptors =
				new HashMap<>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(LaundryElectricitySILModel.URI);
		submodels.add(LaundryStateSILModel.URI);

		Map<EventSource, EventSink[]> connections =
				new HashMap<EventSource, EventSink[]>();

		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SwitchOnLaundry.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SwitchOnLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SwitchOffLaundry.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SwitchOffLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							StartWash.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  StartWash.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							CancelWash.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  CancelWash.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetDelicateModeLaundry.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SetDelicateModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetColorModeLaundry.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SetColorModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetWhiteModeLaundry.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SetWhiteModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetIntensiveModeLaundry.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SetIntensiveModeLaundry.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetWashTemperature.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SetWashTemperature.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetSpinSpeed.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SetSpinSpeed.class)
			});
		connections.put(
			new EventSource(LaundryStateSILModel.URI,
							SetPowerLaundry.class),
			new EventSink[] {
				new EventSink(LaundryElectricitySILModel.URI,
							  SetPowerLaundry.class)
			});

		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledModelDescriptor(
						LaundryCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						null,
						connections,
						null,
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

	/**
	 * create the local SIL real time simulation architecture for the
	 * {@code LaundryCyPhy} component when performing SIL integration tests.
	 *
	 * <p><strong>Description</strong></p>
	 *
	 * <p>
	 * In integration tests, the {@code LaundryCyPhy} component defines only
	 * one atomic model, the {@code LaundryStateSILModel}. The
	 * {@code LaundryElectricitySILModel} is located in the
	 * {@code ElectricMeter} component. Events exported by
	 * {@code LaundryStateSILModel} are reexported to reach the electricity
	 * model in the electric meter.
	 * </p>
	 *
	 * @param architectureURI		URI to be given to the created architecture.
	 * @param rootModelURI			URI of the root model.
	 * @param simulatedTimeUnit		simulated time unit.
	 * @param accelerationFactor	acceleration factor.
	 * @return						the local SIL architecture for integration test.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture createLaundrySIL_Architecture4IntegrationTest(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		assert architectureURI != null && !architectureURI.isEmpty() :
			new PreconditionException(
					"architectureURI != null && !architectureURI.isEmpty()");
		assert rootModelURI != null && !rootModelURI.isEmpty() :
			new PreconditionException(
					"rootModelURI != null && !rootModelURI.isEmpty()");
		assert simulatedTimeUnit != null :
			new PreconditionException("simulatedTimeUnit != null");
		assert accelerationFactor > 0.0 :
			new PreconditionException("accelerationFactor > 0.0");

		Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
				new HashMap<>();

		atomicModelDescriptors.put(
				rootModelURI,
				RTAtomicModelDescriptor.create(
						LaundryStateSILModel.class,
						rootModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		Map<String, CoupledModelDescriptor> coupledModelDescriptors =
				new HashMap<>();

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
