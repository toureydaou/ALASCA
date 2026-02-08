package etape3.equipements.laundry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import etape1.bases.RegistrationCI;
import etape1.equipements.laundry.Constants;
import etape1.equipements.laundry.Laundry;
import etape1.equipements.laundry.interfaces.LaundryExternalControlI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlJava4CI;
import etape1.equipements.laundry.interfaces.LaundryUserCI;
import etape1.equipements.laundry.interfaces.LaundryUserI;
import etape1.equipements.laundry.ports.LaundryExternalControlJava4InboundPort;
import etape1.equipements.laundry.ports.LaundryUserInboundPort;
import etape1.equipements.hem.HEM;
import etape1.equipements.registration.connector.RegistrationConnector;
import etape1.equipements.registration.ports.RegistrationOutboundPort;
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
import etape3.equipements.laundry.connections.LaundryActuatorInboundPort;
import etape3.equipements.laundry.connections.ports.LaundrySensorDataInboundPort;
import etape3.equipements.laundry.sensor_data.LaundryStateSensorData;
import etape3.equipements.laundry.sensor_data.WashProgressSensorData;
import etape3.equipements.laundry.sil.LaundryStateSILModel;
import etape3.equipements.laundry.sil.Local_SIL_SimulationArchitectures;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.annotations.LocalArchitecture;
import fr.sorbonne_u.components.cyphy.annotations.SIL_Simulation_Architectures;
import fr.sorbonne_u.components.cyphy.interfaces.CyPhyReflectionCI;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryCyPhy</code> implements the laundry machine as a
 * BCM4Java CyPhy component with SIL simulation support.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This component extends the etape1 Laundry with SIL simulation capabilities.
 * It triggers simulation events when state changes occur, and provides sensor
 * data and actuator interfaces for the controller component.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
@SIL_Simulation_Architectures({
	@LocalArchitecture(
		uri = "silUnitTests",
		rootModelURI = "LaundryCoupledModel",
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents()),
	@LocalArchitecture(
		uri = "silIntegrationTests",
		rootModelURI = "LaundryStateSILModel",
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents(
			exported = {
				SwitchOnLaundry.class, SwitchOffLaundry.class,
				StartWash.class, CancelWash.class,
				SetDelicateModeLaundry.class, SetColorModeLaundry.class,
				SetWhiteModeLaundry.class, SetIntensiveModeLaundry.class,
				SetWashTemperature.class, SetSpinSpeed.class,
				SetPowerLaundry.class
			}))
})
@OfferedInterfaces(offered = {
	LaundryUserCI.class,
	LaundryExternalControlJava4CI.class,
	LaundrySensorDataCI.LaundrySensorOfferedPullCI.class,
	LaundryActuatorCI.class
})
@RequiredInterfaces(required = { RegistrationCI.class })
public class LaundryCyPhy
extends		AbstractCyPhyComponent
implements	LaundryUserI,
			LaundryExternalControlI,
			LaundryInternalControlI,
			LaundrySensorDataInboundPort.LaundrySensorDataOwnerI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static final String REFLECTION_INBOUND_PORT_URI = "LAUNDRY-CYPHY-RIP-URI";
	public static final String USER_INBOUND_PORT_URI = Laundry.USER_INBOUND_PORT_URI;
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = Laundry.EXTERNAL_CONTROL_INBOUND_PORT_URI;
	public static final String SENSOR_INBOUND_PORT_URI = "LAUNDRY-SENSOR-INBOUND-PORT-URI";
	public static final String ACTUATOR_INBOUND_PORT_URI = "LAUNDRY-ACTUATOR-INBOUND-PORT-URI";

	public static final MeasurementUnit POWER_UNIT = MeasurementUnit.WATTS;
	public static final MeasurementUnit TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;
	public static final MeasurementUnit VOLUME_UNIT = MeasurementUnit.LITERS;

	public static final Measure<Double> MAX_POWER_IN_WATTS = Laundry.MAX_POWER_IN_WATTS;
	public static final Measure<Double> MIN_POWER_IN_WATTS = Laundry.MIN_POWER_IN_WATTS;
	public static final Measure<Double> DELICATE_MODE_POWER_IN_WATTS = Laundry.DELICATE_MODE_POWER_IN_WATTS;
	public static final Measure<Double> COLOR_MODE_POWER_IN_WATTS = Laundry.COLOR_MODE_POWER_IN_WATTS;
	public static final Measure<Double> WHITE_MODE_POWER_IN_WATTS = Laundry.WHITE_MODE_POWER_IN_WATTS;
	public static final Measure<Double> INTENSIVE_MODE_POWER_IN_WATTS = Laundry.INTENSIVE_MODE_POWER_IN_WATTS;

	public static final String XML_LAUNDRY_ADAPTER_DESCRIPTOR = Laundry.XML_LAUNDRY_ADAPTER_DESCRIPTOR;
	public static final String LAUNDRY_CONNECTOR_NAME = "LaundryGeneratedConnector";

	public static final String UNIT_TEST_ARCHITECTURE_URI = "silUnitTests";
	public static final String INTEGRATION_TEST_ARCHITECTURE_URI = "silIntegrationTests";

	public static boolean VERBOSE = true;
	public static boolean DEBUG = false;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	protected static int NUMBER_OF_STANDARD_THREADS = 2;
	protected static int NUMBER_OF_SCHEDULABLE_THREADS = 1;

	// Ports
	protected LaundryUserInboundPort luip;
	protected LaundryExternalControlJava4InboundPort lecip;
	protected LaundrySensorDataInboundPort sensorInboundPort;
	protected LaundryActuatorInboundPort actuatorInboundPort;
	protected RegistrationOutboundPort rop;

	// State variables
	protected LaundryState currentState;
	protected LaundryWashMode currentWashMode;
	protected SpinSpeed currentSpinSpeed;
	protected Measure<Double> currentPowerConsumption;
	protected Measure<Double> currentPowerLevel;
	protected double currentWashTemperature;
	protected double currentWaterLevel;
	protected boolean isSuspended;

	// Simulation
	protected String uid;
	protected boolean isIntegrationTestMode;
	protected RTAtomicSimulatorPlugin asp;
	protected final String localArchitectureURI;
	protected final double accelerationFactor;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected LaundryCyPhy(boolean isIntegrationTestMode) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI,
			 EXTERNAL_CONTROL_INBOUND_PORT_URI,
			 SENSOR_INBOUND_PORT_URI, ACTUATOR_INBOUND_PORT_URI);
	}

	protected LaundryCyPhy(
		boolean isIntegrationTestMode,
		String userInboundPortURI,
		String externalControlInboundPortURI,
		String sensorInboundPortURI,
		String actuatorInboundPortURI
	) throws Exception
	{
		this(isIntegrationTestMode,
			 AbstractPort.generatePortURI(CyPhyReflectionCI.class),
			 userInboundPortURI, externalControlInboundPortURI,
			 sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected LaundryCyPhy(
		boolean isIntegrationTestMode,
		String reflectionInboundPortURI,
		String userInboundPortURI,
		String externalControlInboundPortURI,
		String sensorInboundPortURI,
		String actuatorInboundPortURI
	) throws Exception
	{
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(isIntegrationTestMode, userInboundPortURI,
						externalControlInboundPortURI,
						sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected LaundryCyPhy(
		boolean isIntegrationTestMode,
		ExecutionMode executionMode,
		String clockURI
	) throws Exception
	{
		this(isIntegrationTestMode,
			 USER_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI,
			 SENSOR_INBOUND_PORT_URI, ACTUATOR_INBOUND_PORT_URI,
			 executionMode, clockURI);
	}

	protected LaundryCyPhy(
		boolean isIntegrationTestMode,
		String userInboundPortURI,
		String externalControlInboundPortURI,
		String sensorInboundPortURI,
		String actuatorInboundPortURI,
		ExecutionMode executionMode,
		String clockURI
	) throws Exception
	{
		this(isIntegrationTestMode,
			 AbstractPort.generatePortURI(CyPhyReflectionCI.class),
			 userInboundPortURI, externalControlInboundPortURI,
			 sensorInboundPortURI, actuatorInboundPortURI,
			 executionMode, clockURI);
	}

	protected LaundryCyPhy(
		boolean isIntegrationTestMode,
		String reflectionInboundPortURI,
		String userInboundPortURI,
		String externalControlInboundPortURI,
		String sensorInboundPortURI,
		String actuatorInboundPortURI,
		ExecutionMode executionMode,
		String clockURI
	) throws Exception
	{
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS, executionMode, clockURI, null);

		assert executionMode != null && executionMode.isTestWithoutSimulation()
			: new PreconditionException(
					"executionMode != null && "
					+ "executionMode.isTestWithoutSimulation()");

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(isIntegrationTestMode, userInboundPortURI,
						externalControlInboundPortURI,
						sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected LaundryCyPhy(
		boolean isIntegrationTestMode,
		String reflectionInboundPortURI,
		String userInboundPortURI,
		String externalControlInboundPortURI,
		String sensorInboundPortURI,
		String actuatorInboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario,
		String localArchitectureURI,
		double accelerationFactor
	) throws Exception
	{
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS, executionMode,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null, testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario,
			  ((Supplier<Set<String>>) () -> {
				  HashSet<String> hs = new HashSet<>();
				  hs.add(UNIT_TEST_ARCHITECTURE_URI);
				  hs.add(INTEGRATION_TEST_ARCHITECTURE_URI);
				  return hs;
			  }).get(),
			  accelerationFactor);

		assert executionMode != null && executionMode.isSimulationTest()
			: new PreconditionException(
					"executionMode != null && "
					+ "executionMode.isSimulationTest()");

		this.localArchitectureURI = localArchitectureURI;
		this.accelerationFactor = accelerationFactor;

		this.initialise(isIntegrationTestMode, userInboundPortURI,
						externalControlInboundPortURI,
						sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected void initialise(
		boolean isIntegrationTestMode,
		String userInboundPortURI,
		String externalControlInboundPortURI,
		String sensorInboundPortURI,
		String actuatorInboundPortURI
	) throws Exception
	{
		this.isIntegrationTestMode = isIntegrationTestMode;
		this.uid = LAUNDRY_CONNECTOR_NAME;

		this.currentState = LaundryState.OFF;
		this.currentWashMode = LaundryWashMode.COLOR;
		this.currentSpinSpeed = SpinSpeed.RPM_1000;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;
		this.currentPowerLevel = MIN_POWER_IN_WATTS;
		this.currentWashTemperature = Constants.TEMP_40;
		this.currentWaterLevel = 0.0;
		this.isSuspended = false;

		this.luip = new LaundryUserInboundPort(userInboundPortURI, this);
		this.luip.publishPort();

		this.lecip = new LaundryExternalControlJava4InboundPort(
						externalControlInboundPortURI, this);
		this.lecip.publishPort();

		this.sensorInboundPort = new LaundrySensorDataInboundPort(
									sensorInboundPortURI, this);
		this.sensorInboundPort.publishPort();

		this.actuatorInboundPort = new LaundryActuatorInboundPort(
									actuatorInboundPortURI, this);
		this.actuatorInboundPort.publishPort();

		if (isIntegrationTestMode) {
			this.rop = new RegistrationOutboundPort(this);
			this.rop.publishPort();
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Laundry CyPhy component");
			this.tracer.get().setRelativePosition(
					X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			switch (this.getExecutionMode()) {
			case STANDARD:
			case UNIT_TEST:
				break;
			case INTEGRATION_TEST:
				if (isIntegrationTestMode) {
					this.doPortConnection(
						this.rop.getPortURI(),
						HEM.REGISTRATION_LAUNDRY_INBOUND_PORT_URI,
						RegistrationConnector.class.getCanonicalName());
				}
				break;
			case UNIT_TEST_WITH_SIL_SIMULATION:
			case INTEGRATION_TEST_WITH_SIL_SIMULATION:
				if (isIntegrationTestMode) {
					this.doPortConnection(
						this.rop.getPortURI(),
						HEM.REGISTRATION_LAUNDRY_INBOUND_PORT_URI,
						RegistrationConnector.class.getCanonicalName());
				}

				RTArchitecture architecture =
					(RTArchitecture) this.localSimulationArchitectures
						.get(this.localArchitectureURI);

				this.asp = new RTAtomicSimulatorPlugin();
				this.asp.setPluginURI(architecture.getRootModelURI());
				this.asp.setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				this.asp.createSimulator();
				this.asp.setSimulationRunParameters(
					(TestScenarioWithSimulation) this.testScenario,
					new HashMap<>());
				break;
			case UNIT_TEST_WITH_HIL_SIMULATION:
			case INTEGRATION_TEST_WITH_HIL_SIMULATION:
				throw new BCMException("HIL simulation not implemented yet!");
			default:
			}
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	@Override
	public void execute() throws Exception {
		this.traceMessage("Laundry CyPhy executes.\n");

		switch (this.getExecutionMode()) {
		case STANDARD:
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(
				ClocksServer.STANDARD_INBOUNDPORT_URI, this.clockURI);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
				ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
				this.clockURI);
			this.asp.initialiseSimulation(
				this.getClock4Simulation().getSimulatedStartTime(),
				this.getClock4Simulation().getSimulatedDuration());
			this.asp.startRTSimulation(
				TimeUnit.NANOSECONDS.toMillis(
					this.getClock4Simulation().getStartEpochNanos()),
				this.getClock4Simulation().getSimulatedStartTime()
					.getSimulatedTime(),
				this.getClock4Simulation().getSimulatedDuration()
					.getSimulatedDuration());
			this.getClock4Simulation().waitUntilEnd();
			Thread.sleep(200L);
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
				ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
				this.clockURI);
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		default:
		}
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.luip.unpublishPort();
			this.lecip.unpublishPort();
			this.sensorInboundPort.unpublishPort();
			this.actuatorInboundPort.unpublishPort();
			if (isIntegrationTestMode) {
				this.rop.unpublishPort();
			}
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	protected RTArchitecture createLocalSimulationArchitecture(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
	) throws Exception
	{
		assert architectureURI != null && !architectureURI.isEmpty();
		assert rootModelURI != null && !rootModelURI.isEmpty();
		assert simulatedTimeUnit != null;
		assert accelerationFactor > 0.0;

		RTArchitecture ret = null;
		if (architectureURI.equals(UNIT_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures
					.createLaundrySIL_Architecture4UnitTest(
						architectureURI, rootModelURI,
						simulatedTimeUnit, accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures
					.createLaundrySIL_Architecture4IntegrationTest(
						architectureURI, rootModelURI,
						simulatedTimeUnit, accelerationFactor);
		} else {
			throw new BCMException(
				"Unknown local simulation architecture URI: "
				+ architectureURI);
		}
		return ret;
	}

	// -------------------------------------------------------------------------
	// Component services - LaundryImplementationI
	// -------------------------------------------------------------------------

	@Override
	public boolean isRunning() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: checking if running: "
				+ (this.currentState == LaundryState.WASHING) + "\n");
		}
		return this.currentState == LaundryState.WASHING
			|| this.currentState == LaundryState.RINSING
			|| this.currentState == LaundryState.SPINNING
			|| this.currentState == LaundryState.DRYING;
	}

	@Override
	public LaundryState getState() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry returns state: "
				+ this.currentState + "\n");
		}
		return this.currentState;
	}

	@Override
	public LaundryWashMode getWashMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry returns wash mode: "
				+ this.currentWashMode + "\n");
		}
		return this.currentWashMode;
	}

	@Override
	public Measure<Double> getWashTemperature() throws Exception {
		return new Measure<>(this.currentWashTemperature, TEMPERATURE_UNIT);
	}

	@Override
	public SpinSpeed getSpinSpeed() throws Exception {
		return this.currentSpinSpeed;
	}

	@Override
	public boolean isDelayedStartSet() throws Exception {
		return false;
	}

	@Override
	public long getDelayedStartTime() throws Exception {
		return 0;
	}

	// -------------------------------------------------------------------------
	// Component services - LaundryUserI
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry switches on.\n");
		}

		assert this.currentState == LaundryState.OFF
			: new PreconditionException("currentState == LaundryState.OFF");

		this.currentState = LaundryState.ON;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SwitchOnLaundry(t));
			this.sensorInboundPort.send(
				new LaundryStateSensorData(this.currentState,
										   this.currentWashMode));
		}

		if (isIntegrationTestMode) {
			this.traceMessage("Laundry registering to HEM.\n");
			this.rop.register(uid, lecip.getPortURI(),
							  XML_LAUNDRY_ADAPTER_DESCRIPTOR);
			this.traceMessage("Laundry registered to HEM!\n");
		}

		assert this.currentState == LaundryState.ON
			: new PostconditionException("currentState == LaundryState.ON");
	}

	@Override
	public void turnOff() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry switches off.\n");
		}

		assert this.currentState != LaundryState.OFF
			: new PreconditionException("currentState != LaundryState.OFF");
		assert !this.isRunning()
			: new PreconditionException("!isRunning()");

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(
				new LaundryStateSensorData(LaundryState.OFF,
										   this.currentWashMode));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SwitchOffLaundry(t));
		}

		this.currentState = LaundryState.OFF;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;
		this.isSuspended = false;

		if (isIntegrationTestMode) {
			this.rop.unregister(uid);
			this.doPortDisconnection(this.rop.getPortURI());
		}

		assert this.currentState == LaundryState.OFF
			: new PostconditionException("currentState == LaundryState.OFF");
	}

	@Override
	public void startWash() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry starts wash cycle.\n");
		}

		assert this.currentState == LaundryState.ON
			: new PreconditionException("currentState == LaundryState.ON");

		this.currentState = LaundryState.WASHING;
		this.updatePowerForCurrentMode();

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(
				new LaundryStateSensorData(this.currentState,
										   this.currentWashMode));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new StartWash(t));
		}

		assert this.currentState == LaundryState.WASHING
			: new PostconditionException(
					"currentState == LaundryState.WASHING");
	}

	@Override
	public void cancelWash() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry cancels wash cycle.\n");
		}

		assert this.isRunning()
			: new PreconditionException("isRunning()");

		this.currentState = LaundryState.ON;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(
				new LaundryStateSensorData(this.currentState,
										   this.currentWashMode));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new CancelWash(t));
		}

		assert this.currentState == LaundryState.ON
			: new PostconditionException("currentState == LaundryState.ON");
	}

	@Override
	public void setWhiteMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry sets WHITE mode.\n");
		}

		assert this.currentState == LaundryState.ON
			: new PreconditionException("currentState == LaundryState.ON");

		this.currentWashMode = LaundryWashMode.WHITE;

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SetWhiteModeLaundry(t));
		}
	}

	@Override
	public void setColorMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry sets COLOR mode.\n");
		}

		assert this.currentState == LaundryState.ON
			: new PreconditionException("currentState == LaundryState.ON");

		this.currentWashMode = LaundryWashMode.COLOR;

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SetColorModeLaundry(t));
		}
	}

	@Override
	public void setDelicateMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry sets DELICATE mode.\n");
		}

		assert this.currentState == LaundryState.ON
			: new PreconditionException("currentState == LaundryState.ON");

		this.currentWashMode = LaundryWashMode.DELICATE;

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SetDelicateModeLaundry(t));
		}
	}

	@Override
	public void setIntensiveMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry sets INTENSIVE mode.\n");
		}

		assert this.currentState == LaundryState.ON
			: new PreconditionException("currentState == LaundryState.ON");

		this.currentWashMode = LaundryWashMode.INTENSIVE;

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SetIntensiveModeLaundry(t));
		}
	}

	@Override
	public void setWashTemperature(Measure<Double> temp) throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry sets wash temperature to "
				+ temp.getData() + "°C.\n");
		}

		assert this.currentState == LaundryState.ON
			: new PreconditionException("currentState == LaundryState.ON");

		this.currentWashTemperature = temp.getData();

		if (this.getExecutionMode().isSILTest()) {
			final double tempValue = temp.getData();
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SetWashTemperature(t, tempValue));
		}
	}

	@Override
	public void setSpinSpeed(SpinSpeed speed) throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry sets spin speed to " + speed + ".\n");
		}

		assert this.currentState == LaundryState.ON
			: new PreconditionException("currentState == LaundryState.ON");

		this.currentSpinSpeed = speed;

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(
				LaundryStateSILModel.URI,
				t -> new SetSpinSpeed(t, speed));
		}
	}

	@Override
	public void setDelayedStart(long delayInSeconds) throws Exception {
		// Not implemented in CyPhy version
	}

	@Override
	public void cancelDelayedStart() throws Exception {
		// Not implemented in CyPhy version
	}

	// -------------------------------------------------------------------------
	// Component services - LaundryInternalControlI
	// -------------------------------------------------------------------------

	@Override
	public boolean isOn() throws Exception {
		return this.currentState != LaundryState.OFF;
	}

	@Override
	public boolean isWashing() throws Exception {
		return this.isRunning();
	}

	@Override
	public void stopWash() throws Exception {
		this.cancelWash();
	}

	@Override
	public void setWashMode(LaundryWashMode mode) throws Exception {
		switch (mode) {
		case WHITE: setWhiteMode(); break;
		case COLOR: setColorMode(); break;
		case DELICATE: setDelicateMode(); break;
		case INTENSIVE: setIntensiveMode(); break;
		}
	}

	@Override
	public void setWashTemperature(double temperature) throws Exception {
		this.setWashTemperature(
			new Measure<>(temperature, TEMPERATURE_UNIT));
	}

	@Override
	public double getWaterLevel() throws Exception {
		return this.currentWaterLevel;
	}

	@Override
	public double getPowerConsumption() throws Exception {
		return this.currentPowerConsumption.getData();
	}

	// -------------------------------------------------------------------------
	// Component services - LaundryExternalControlI
	// -------------------------------------------------------------------------

	@Override
	public void suspend() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry suspends.\n");
		}
		this.isSuspended = true;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;
	}

	@Override
	public void resume() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry resumes.\n");
		}
		this.isSuspended = false;
		if (this.isRunning()) {
			this.updatePowerForCurrentMode();
		}
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return MAX_POWER_IN_WATTS;
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return this.currentPowerLevel;
	}

	@Override
	public Measure<Double> getCurrentWashTemperature() throws Exception {
		return new Measure<>(this.currentWashTemperature, TEMPERATURE_UNIT);
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel)
	throws Exception
	{
		if (powerLevel.getData() < MAX_POWER_IN_WATTS.getData()) {
			this.currentPowerLevel =
				new Measure<>(powerLevel.getData(), POWER_UNIT);
		}
	}

	@Override
	public void setMode(int mode) throws Exception {
		switch (mode) {
		case 1: this.setDelicateMode(); break;
		case 2: this.setColorMode(); break;
		case 3: this.setWhiteMode(); break;
		case 4: this.setIntensiveMode(); break;
		}
	}

	// -------------------------------------------------------------------------
	// Sensor data methods (LaundrySensorDataOwnerI)
	// -------------------------------------------------------------------------

	@Override
	public LaundryStateSensorData statePullSensor() throws Exception {
		return new LaundryStateSensorData(this.currentState,
												this.currentWashMode);
	}

	@Override
	public WashProgressSensorData washProgressPullSensor() throws Exception {
		return new WashProgressSensorData(
			this.currentWashTemperature,
			this.currentWaterLevel,
			this.currentSpinSpeed,
			this.currentPowerConsumption.getData());
	}

	@Override
	public double waterTemperaturePullSensor() throws Exception {
		return this.currentWashTemperature;
	}

	@Override
	public double waterLevelPullSensor() throws Exception {
		return this.currentWaterLevel;
	}

	@Override
	public void startWashProgressPushSensor(long controlPeriod, TimeUnit tu)
	throws Exception
	{
		this.sensorDataPushTask(controlPeriod, tu);
	}

	@Override
	public void stopWashProgressPushSensor() throws Exception {
		// Push stops naturally when machine is OFF
	}

	protected void sensorDataPushTask(long period, TimeUnit tu)
	throws Exception
	{
		if (this.currentState != LaundryState.OFF) {
			this.sensorInboundPort.send(
				new LaundryStateSensorData(this.currentState,
											this.currentWashMode));

			this.scheduleTaskOnComponent(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							sensorDataPushTask(period, tu);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, period, tu);
		}
	}

	// -------------------------------------------------------------------------
	// Internal helper methods
	// -------------------------------------------------------------------------

	protected void updatePowerForCurrentMode() {
		switch (this.currentWashMode) {
		case WHITE:
			this.currentPowerConsumption = WHITE_MODE_POWER_IN_WATTS;
			break;
		case COLOR:
			this.currentPowerConsumption = COLOR_MODE_POWER_IN_WATTS;
			break;
		case DELICATE:
			this.currentPowerConsumption = DELICATE_MODE_POWER_IN_WATTS;
			break;
		case INTENSIVE:
			this.currentPowerConsumption = INTENSIVE_MODE_POWER_IN_WATTS;
			break;
		}
	}
}
// -----------------------------------------------------------------------------
