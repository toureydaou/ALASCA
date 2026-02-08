package etape3.equipements.kettle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import etape1.bases.RegistrationCI;
import etape1.equipements.hem.HEM;
import etape1.equipements.kettle.interfaces.KettleExternalControlI;
import etape1.equipements.kettle.interfaces.KettleExternalControlJava4CI;
import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleUserCI;
import etape1.equipements.kettle.interfaces.KettleUserI;
import etape1.equipements.kettle.connections.ports.KettleExternalControlJava4InboundPort;
import etape1.equipements.kettle.connections.ports.KettleUserInboundPort;
import etape1.equipements.registration.connector.RegistrationConnector;
import etape1.equipements.registration.ports.RegistrationOutboundPort;
import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.SetEcoModeKettle;
import etape2.equipments.kettle.mil.events.SetMaxModeKettle;
import etape2.equipments.kettle.mil.events.SetNormalModeKettle;
import etape2.equipments.kettle.mil.events.SetPowerKettle;
import etape2.equipments.kettle.mil.events.SetSuspendedModeKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
import etape3.equipements.kettle.connections.ports.KettleActuatorInboundPort;
import etape3.equipements.kettle.connections.ports.KettleSensorDataInboundPort;
import etape3.equipements.kettle.sensor_data.KettleCompoundSensorData;
import etape3.equipements.kettle.sensor_data.KettleModeSensorData;
import etape3.equipements.kettle.sensor_data.KettleStateSensorData;
import etape3.equipements.kettle.sensor_data.KettleTemperatureSensorData;
import etape3.equipements.kettle.sil.KettleStateSILModel;
import etape3.equipements.kettle.sil.KettleTemperatureSILModel;
import etape3.equipements.kettle.sil.Local_SIL_SimulationArchitectures;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.annotations.LocalArchitecture;
import fr.sorbonne_u.components.cyphy.annotations.SIL_Simulation_Architectures;
import fr.sorbonne_u.components.cyphy.interfaces.CyPhyReflectionCI;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI.VariableValue;
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
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;

@SIL_Simulation_Architectures({
		@LocalArchitecture(uri = "silUnitTests", rootModelURI = "KettleCoupledModel", simulatedTimeUnit = TimeUnit.HOURS, externalEvents = @ModelExternalEvents()),
		@LocalArchitecture(uri = "silIntegrationTests", rootModelURI = "KettleCoupledModel", simulatedTimeUnit = TimeUnit.HOURS, externalEvents = @ModelExternalEvents(exported = {
				SwitchOnKettle.class, SwitchOffKettle.class, SetEcoModeKettle.class,
				SetMaxModeKettle.class, SetNormalModeKettle.class, SetSuspendedModeKettle.class,
				HeatKettle.class, DoNotHeatKettle.class, SetPowerKettle.class })) })
@OfferedInterfaces(offered = { KettleUserCI.class,
		KettleExternalControlJava4CI.class, KettleSensorDataCI.KettleSensorOfferedPullCI.class,
		KettleActuatorCI.class })
@RequiredInterfaces(required = { RegistrationCI.class })
public class KettleCyPhy extends AbstractCyPhyComponent
		implements KettleUserI, KettleExternalControlI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// BCM4Java information
	public static final String REFLECTION_INBOUND_PORT_URI = "KETTLE-RIP-URI";
	public static final String USER_INBOUND_PORT_URI = "KETTLE-USER-INBOUND-PORT-URI";
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "KETTLE-EXTERNAL-CONTROL-INBOUND-PORT-URI";
	public static final String SENSOR_INBOUND_PORT_URI = "KETTLE-SENSOR-INBOUND-PORT-URI";
	public static final String ACTUATOR_INBOUND_PORT_URI = "KETTLE-ACTUATOR-INBOUND-PORT-URI";

	// Physical units
	public static final MeasurementUnit POWER_UNIT = MeasurementUnit.WATTS;
	public static final MeasurementUnit TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;

	// Constants
	public static final Measure<Double> MAX_TEMPERATURE = new Measure<>(KettleImplementationI.MAX_TARGET_TEMPERATURE, TEMPERATURE_UNIT);
	public static final Measure<Double> MIN_TEMPERATURE = new Measure<>(KettleImplementationI.MIN_TARGET_TEMPERATURE, TEMPERATURE_UNIT);
	public static final Measure<Double> HIGH_POWER_IN_WATTS = new Measure<>(KettleImplementationI.MAX_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> ECO_POWER_IN_WATTS = new Measure<>(KettleImplementationI.ECO_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> NORMAL_POWER_IN_WATTS = new Measure<>(KettleImplementationI.NORMAL_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> SUSPENDED_POWER_IN_WATTS = new Measure<>(KettleImplementationI.SUSPEND_MODE_POWER, POWER_UNIT);

	// Ports
	protected KettleUserInboundPort kuip;
	protected KettleExternalControlJava4InboundPort kecjip;
	protected KettleSensorDataInboundPort sensorInboundPort;
	protected KettleActuatorInboundPort actuatorInboundPort;

	// State variables
	protected KettleState currentState;
	protected KettleMode currentMode;
	protected double targetTemperature;
	protected TimedMeasure<Double> currentPowerLevel;
	protected TimedMeasure<Double> currentTemperature;

	// Execution/Simulation
	public static boolean VERBOSE = true;
	public static boolean DEBUG = false;
	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;

	protected static int NUMBER_OF_STANDARD_THREADS = 2;
	protected static int NUMBER_OF_SCHEDULABLE_THREADS = 0;

	public static final String UNIT_TEST_ARCHITECTURE_URI = "silUnitTests";
	public static final String INTEGRATION_TEST_ARCHITECTURE_URI = "silIntegrationTests";

	protected static final String CURRENT_TEMPERATURE_NAME = "currentTemperature";

	public static final String XML_KETTLE_ADAPTER_DESCRIPTOR = "adapters/kettle-adapter/kettleci-descriptor.xml";

	public static final String KETTLE_CONNECTOR_NAME = "KettleGeneratedConnector";

	protected RegistrationOutboundPort rop;

	protected String uid;

	protected boolean isIntegrationTestMode;

	protected RTAtomicSimulatorPlugin asp;
	protected final String localArchitectureURI;
	protected final double accelerationFactor;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected KettleCyPhy(boolean isIntegrationTestMode) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI,
				SENSOR_INBOUND_PORT_URI, ACTUATOR_INBOUND_PORT_URI);
	}

	protected KettleCyPhy(boolean isIntegrationTestMode, String userInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI, String actuatorInboundPortURI)
			throws Exception {
		this(isIntegrationTestMode, AbstractPort.generatePortURI(CyPhyReflectionCI.class), userInboundPortURI,
				externalControlInboundPortURI, sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected KettleCyPhy(boolean isIntegrationTestMode, String reflectionInboundPortURI, String userInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI,
			String actuatorInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS);

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(isIntegrationTestMode, userInboundPortURI, externalControlInboundPortURI,
				sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected KettleCyPhy(boolean isIntegrationTestMode, ExecutionMode executionMode, String clockURI) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI,
				SENSOR_INBOUND_PORT_URI, ACTUATOR_INBOUND_PORT_URI, executionMode, clockURI);
	}

	/**
	 * Constructor with instanceId for multiple instances.
	 */
	protected KettleCyPhy(int instanceId, boolean isIntegrationTestMode, ExecutionMode executionMode, String clockURI) throws Exception {
		this(isIntegrationTestMode,
			 REFLECTION_INBOUND_PORT_URI + "-" + instanceId,
			 USER_INBOUND_PORT_URI + "-" + instanceId,
			 EXTERNAL_CONTROL_INBOUND_PORT_URI + "-" + instanceId,
			 SENSOR_INBOUND_PORT_URI + "-" + instanceId,
			 ACTUATOR_INBOUND_PORT_URI + "-" + instanceId,
			 executionMode, clockURI);
		this.uid = "Kettle_" + instanceId;
	}

	protected KettleCyPhy(boolean isIntegrationTestMode, String userInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI, String actuatorInboundPortURI,
			ExecutionMode executionMode, String clockURI) throws Exception {
		this(isIntegrationTestMode, AbstractPort.generatePortURI(CyPhyReflectionCI.class), userInboundPortURI,
				externalControlInboundPortURI, sensorInboundPortURI, actuatorInboundPortURI, executionMode, clockURI);
	}

	protected KettleCyPhy(boolean isIntegrationTestMode, String reflectionInboundPortURI, String userInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI,
			String actuatorInboundPortURI, ExecutionMode executionMode, String clockURI) throws Exception {
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS, executionMode,
				clockURI, null);

		assert executionMode != null && executionMode.isTestWithoutSimulation()
				: new PreconditionException("executionMode != null && executionMode.isTestWithoutSimulation()");

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(isIntegrationTestMode, userInboundPortURI, externalControlInboundPortURI,
				sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected KettleCyPhy(boolean isIntegrationTestMode, String reflectionInboundPortURI, String userInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI,
			String actuatorInboundPortURI, ExecutionMode executionMode, TestScenario testScenario,
			String localArchitectureURI, double accelerationFactor) throws Exception {
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS, executionMode,
				AssertionChecking.assertTrueAndReturnOrThrow(testScenario != null, testScenario.getClockURI(),
						() -> new PreconditionException("testScenario != null")),
				testScenario, ((Supplier<Set<String>>) () -> {
					HashSet<String> hs = new HashSet<>();
					hs.add(UNIT_TEST_ARCHITECTURE_URI);
					hs.add(INTEGRATION_TEST_ARCHITECTURE_URI);
					return hs;
				}).get(), accelerationFactor);

		assert executionMode != null && executionMode.isSimulationTest()
				: new PreconditionException("executionMode != null && executionMode.isSimulationTest()");

		this.localArchitectureURI = localArchitectureURI;
		this.accelerationFactor = accelerationFactor;

		this.initialise(isIntegrationTestMode, userInboundPortURI, externalControlInboundPortURI,
				sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected void initialise(boolean isIntegrationTestMode, String userInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI, String actuatorInboundPortURI)
			throws Exception {

		this.isIntegrationTestMode = isIntegrationTestMode;
		this.uid = KETTLE_CONNECTOR_NAME;
		System.out.println("DEBUG Kettle: uid initialized to: " + this.uid);

		this.currentState = KettleState.OFF;
		this.currentMode = KettleMode.NORMAL;
		this.targetTemperature = KettleImplementationI.DEFAULT_TARGET_TEMPERATURE;

		this.kuip = new KettleUserInboundPort(userInboundPortURI, this);
		this.kuip.publishPort();

		this.kecjip = new KettleExternalControlJava4InboundPort(externalControlInboundPortURI, this);
		this.kecjip.publishPort();

		this.sensorInboundPort = new KettleSensorDataInboundPort(sensorInboundPortURI, this);
		this.sensorInboundPort.publishPort();

		this.actuatorInboundPort = new KettleActuatorInboundPort(actuatorInboundPortURI, this);
		this.actuatorInboundPort.publishPort();

		if (isIntegrationTestMode) {
			System.out.println("Kettle publication port Registration");
			this.rop = new RegistrationOutboundPort(this);
			this.rop.publishPort();
			System.out.println("Kettle port Registration published");
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Kettle CyPhy component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
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
					System.out.println("Connexion avec HEM pour enregistrement (Kettle)");
					this.doPortConnection(this.rop.getPortURI(), HEM.REGISTRATION_KETTLE_INBOUND_PORT_URI,
							RegistrationConnector.class.getCanonicalName());
					System.out.println("Connexion avec HEM pour enregistrement realisee (Kettle)");
				}
				break;
			case UNIT_TEST_WITH_SIL_SIMULATION:
			case INTEGRATION_TEST_WITH_SIL_SIMULATION:

				if (isIntegrationTestMode) {
					System.out.println("Connexion avec HEM pour enregistrement (Kettle)");
					this.doPortConnection(this.rop.getPortURI(), HEM.REGISTRATION_KETTLE_INBOUND_PORT_URI,
							RegistrationConnector.class.getCanonicalName());
					System.out.println("Connexion avec HEM pour enregistrement realisee (Kettle)");
				}

				RTArchitecture architecture = (RTArchitecture) this.localSimulationArchitectures
						.get(this.localArchitectureURI);

				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;

					@Override
					public VariableValue<Double> getModelVariableValue(String modelURI, String name) throws Exception {
						if (name.equals(CURRENT_TEMPERATURE_NAME)) {
							return ((KettleTemperatureSILModel) this.atomicSimulators.get(modelURI)
									.getSimulatedModel()).getCurrentTemperature();
						}
						throw new BCMException("Unknown variable: " + name);
					}
				};

				((RTAtomicSimulatorPlugin) this.asp).setPluginURI(architecture.getRootModelURI());
				((RTAtomicSimulatorPlugin) this.asp).setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				this.asp.createSimulator();
				this.asp.setSimulationRunParameters((TestScenarioWithSimulation) this.testScenario, new HashMap<>());
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
		this.traceMessage("Kettle CyPhy executes.\n");

		switch (this.getExecutionMode()) {
		case STANDARD:
			this.currentPowerLevel = new TimedMeasure<>(0.0, POWER_UNIT);
			this.currentTemperature = new TimedMeasure<>(KettleImplementationI.MIN_TARGET_TEMPERATURE, TEMPERATURE_UNIT);
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI, this.clockURI);
			this.currentPowerLevel = new TimedMeasure<>(0.0, POWER_UNIT, this.getClock(),
					this.getClock().getStartInstant());
			this.currentTemperature = new TimedMeasure<>(KettleImplementationI.MIN_TARGET_TEMPERATURE, TEMPERATURE_UNIT, this.getClock(),
					this.getClock().getStartInstant());
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI, this.clockURI);
			this.asp.initialiseSimulation(this.getClock4Simulation().getSimulatedStartTime(),
					this.getClock4Simulation().getSimulatedDuration());
			this.asp.startRTSimulation(TimeUnit.NANOSECONDS.toMillis(this.getClock4Simulation().getStartEpochNanos()),
					this.getClock4Simulation().getSimulatedStartTime().getSimulatedTime(),
					this.getClock4Simulation().getSimulatedDuration().getSimulatedDuration());

			this.currentPowerLevel = new TimedMeasure<>(0.0, POWER_UNIT, this.getClock4Simulation(),
					this.getClock4Simulation().getStartInstant());
			this.currentTemperature = new TimedMeasure<>(KettleImplementationI.MIN_TARGET_TEMPERATURE, TEMPERATURE_UNIT,
					this.getClock4Simulation(), this.getClock4Simulation().getStartInstant());

			this.getClock4Simulation().waitUntilEnd();
			Thread.sleep(200L);
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI, this.clockURI);

			this.currentPowerLevel = new TimedMeasure<>(0.0, POWER_UNIT, this.getClock4Simulation(),
					this.getClock4Simulation().getStartInstant());
			this.currentTemperature = new TimedMeasure<>(KettleImplementationI.MIN_TARGET_TEMPERATURE, TEMPERATURE_UNIT,
					this.getClock4Simulation(), this.getClock4Simulation().getStartInstant());
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
			this.kuip.unpublishPort();
			this.kecjip.unpublishPort();
			this.sensorInboundPort.unpublishPort();
			this.actuatorInboundPort.unpublishPort();
			if (isIntegrationTestMode) {
				if (this.rop.connected()) {
					this.doPortDisconnection(this.rop.getPortURI());
				}
				this.rop.unpublishPort();
			}
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	protected RTArchitecture createLocalSimulationArchitecture(String architectureURI, String rootModelURI,
			TimeUnit simulatedTimeUnit, double accelerationFactor) throws Exception {
		assert architectureURI != null && !architectureURI.isEmpty();
		assert rootModelURI != null && !rootModelURI.isEmpty();
		assert simulatedTimeUnit != null;
		assert accelerationFactor > 0.0;

		RTArchitecture ret = null;
		if (architectureURI.equals(UNIT_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.createKettleSIL_Architecture4UnitTest(architectureURI,
					rootModelURI, simulatedTimeUnit, accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.createKettle_SIL_LocalArchitecture4IntegrationTest(
					architectureURI, rootModelURI, simulatedTimeUnit, accelerationFactor);
		} else {
			throw new BCMException("Unknown local simulation architecture URI: " + architectureURI);
		}
		return ret;
	}

	// -------------------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------------------

	protected double getPowerForMode(KettleMode mode) {
		switch (mode) {
		case SUSPEND:	return KettleImplementationI.SUSPEND_MODE_POWER;
		case ECO:		return KettleImplementationI.ECO_MODE_POWER;
		case NORMAL:	return KettleImplementationI.NORMAL_MODE_POWER;
		case MAX:		return KettleImplementationI.MAX_MODE_POWER;
		default:		return 0.0;
		}
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	@Override
	public KettleState getState() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle returns its state: " + this.currentState + ".\n");
		}
		return this.currentState;
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle returns its mode: " + this.currentMode + ".\n");
		}
		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		return this.currentMode;
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle returns target temperature: " + this.targetTemperature + " C.\n");
		}
		return new Measure<>(this.targetTemperature, TEMPERATURE_UNIT);
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle returns current temperature.\n");
		}

		Measure<Double> temp = null;
		if (this.executionMode.isSILTest()) {
			VariableValue<Double> v = this.computeCurrentTemperature();
			temp = new Measure<>(v.getValue(), TEMPERATURE_UNIT);
		} else {
			temp = new Measure<>(this.currentTemperature.getData(), this.currentTemperature.getMeasurementUnit());
		}
		return temp;
	}

	@SuppressWarnings("unchecked")
	protected VariableValue<Double> computeCurrentTemperature() throws Exception {
		return (VariableValue<Double>) this.asp.getModelVariableValue(KettleTemperatureSILModel.URI, CURRENT_TEMPERATURE_NAME);
	}

	@Override
	public boolean isHeating() throws Exception {
		return this.currentState == KettleState.HEATING;
	}

	@Override
	public void turnOn() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle switches on.\n");
		}

		assert this.getState() == KettleState.OFF
				: new PreconditionException("getState() == KettleState.OFF");

		this.currentState = KettleState.ON;
		this.currentMode = KettleMode.NORMAL;

		if (this.getExecutionMode().isSILTest()) {
			try {
				if (VERBOSE) {
					this.traceMessage("Kettle triggering SwitchOn event in simulation.\n");
				}
				((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
						t -> new SwitchOnKettle(t));
				if (VERBOSE) {
					this.traceMessage(
							"Kettle sending state notification to controller: " + this.currentState + "\n");
				}

				this.sensorInboundPort.send(new KettleStateSensorData(this.currentState));
				if (VERBOSE) {
					this.traceMessage("Kettle state notification sent successfully.\n");
				}
			} catch (Exception e) {
				System.out.println(
						"ERROR sending state notification: " + e.getClass().getName() + ": " + e.getMessage() + "\n");
				e.printStackTrace();
			}
		}

		if (isIntegrationTestMode) {
			this.traceMessage("Kettle registering to HEM");
			System.out.println("DEBUG Kettle.turnOn(): uid = " + uid);
			System.out.println("DEBUG Kettle.turnOn(): this.uid = " + this.uid);
			System.out.println("DEBUG Kettle.turnOn(): KETTLE_CONNECTOR_NAME = " + KETTLE_CONNECTOR_NAME);
			this.rop.register(uid, kecjip.getPortURI(), XML_KETTLE_ADAPTER_DESCRIPTOR);
			this.traceMessage("Kettle registered to HEM !");
		}

		assert this.getState() == KettleState.ON
				|| this.getState() == KettleState.HEATING
				: new PostconditionException(
						"getState() == KettleState.ON || getState() == KettleState.HEATING");
	}

	@Override
	public void turnOff() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle switches off.\n");
		}

		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");

		// If currently heating, stop heating first to respect state machine:
		// HEATING →(DoNotHeat)→ ON →(SwitchOff)→ OFF
		if (this.currentState == KettleState.HEATING) {
			if (VERBOSE) {
				this.traceMessage("Kettle is HEATING, stopping heating before turning off.\n");
			}
			this.stopHeating();
		}

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new KettleStateSensorData(KettleState.OFF));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
					t -> new SwitchOffKettle(t));
		}

		this.currentState = KettleState.OFF;
		this.currentMode = KettleMode.NORMAL;
		this.currentTemperature = new TimedMeasure<>(KettleImplementationI.MIN_TARGET_TEMPERATURE, TEMPERATURE_UNIT);

		if (isIntegrationTestMode) {
			this.rop.unregister(uid);
			this.doPortDisconnection(this.rop.getPortURI());
		}

		assert this.getState() == KettleState.OFF
				: new PostconditionException("getState() == KettleState.OFF");
	}

	@Override
	public void startHeating() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle starts heating.\n");
		}

		assert this.getState() == KettleState.ON
				: new PreconditionException("getState() == KettleState.ON");

		this.currentState = KettleState.HEATING;

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new KettleStateSensorData(this.currentState));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI, t -> new HeatKettle(t));
		}

		assert this.getState() == KettleState.HEATING
				: new PostconditionException("getState() == KettleState.HEATING");
	}

	@Override
	public void stopHeating() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle stops heating.\n");
		}

		assert this.getState() == KettleState.HEATING
				: new PreconditionException("getState() == KettleState.HEATING");

		this.currentState = KettleState.ON;

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new KettleStateSensorData(this.currentState));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
					t -> new DoNotHeatKettle(t));
		}

		assert this.getState() == KettleState.ON
				: new PostconditionException("getState() == KettleState.ON");
	}

	@Override
	public void setTargetTemperature(Measure<Double> temperature) throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle sets target temperature to: " + temperature.getData() + " C.\n");
		}

		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		assert temperature != null
				: new PreconditionException("temperature != null");
		assert temperature.getData() >= KettleImplementationI.MIN_TARGET_TEMPERATURE
				: new PreconditionException("temperature.getData() >= MIN_TARGET_TEMPERATURE");
		assert temperature.getData() <= KettleImplementationI.MAX_TARGET_TEMPERATURE
				: new PreconditionException("temperature.getData() <= MAX_TARGET_TEMPERATURE");

		this.targetTemperature = temperature.getData();

		assert this.getTargetTemperature().getData() == temperature.getData()
				: new PostconditionException("getTargetTemperature().getData() == temperature.getData()");
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle sets mode to: " + mode + ".\n");
		}

		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		assert mode != null
				: new PreconditionException("mode != null");

		this.currentMode = mode;
		this.setCurrentPowerLevel(new Measure<>(getPowerForMode(mode), POWER_UNIT));

		if (this.getExecutionMode().isSILTest()) {
			switch (mode) {
			case SUSPEND:
				((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
						t -> new SetSuspendedModeKettle(t));
				break;
			case ECO:
				((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
						t -> new SetEcoModeKettle(t));
				break;
			case NORMAL:
				((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
						t -> new SetNormalModeKettle(t));
				break;
			case MAX:
				((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
						t -> new SetMaxModeKettle(t));
				break;
			}
		}

		assert this.getKettleMode() == mode
				: new PostconditionException("getKettleMode() == mode");
	}

	@Override
	public void suspend() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle is suspended by HEM.\n");
		}

		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		assert !this.isSuspended()
				: new PreconditionException("!isSuspended()");

		this.currentMode = KettleMode.SUSPEND;
		this.setCurrentPowerLevel(SUSPENDED_POWER_IN_WATTS);

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
					t -> new SetSuspendedModeKettle(t));
		}

		assert this.isSuspended()
				: new PostconditionException("isSuspended()");
	}

	@Override
	public void resume() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle is resumed by HEM.\n");
		}

		assert this.isSuspended()
				: new PreconditionException("isSuspended()");

		this.currentMode = KettleMode.NORMAL;
		this.setCurrentPowerLevel(NORMAL_POWER_IN_WATTS);

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(KettleStateSILModel.URI,
					t -> new SetNormalModeKettle(t));
		}

		assert !this.isSuspended()
				: new PostconditionException("!isSuspended()");
	}

	@Override
	public boolean isSuspended() throws Exception {
		return this.currentMode == KettleMode.SUSPEND;
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle returns max power level.\n");
		}
		return HIGH_POWER_IN_WATTS;
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle returns current power level.\n");
		}
		return new Measure<>(this.currentPowerLevel.getData(), this.currentPowerLevel.getMeasurementUnit());
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		if (VERBOSE) {
			this.traceMessage("Kettle sets power level to " + powerLevel + ".\n");
		}

		if (powerLevel.getData() <= getMaxPowerLevel().getData()) {
			this.currentPowerLevel = new TimedMeasure<>(powerLevel.getData(), powerLevel.getMeasurementUnit());
		} else {
			this.currentPowerLevel = new TimedMeasure<>(HIGH_POWER_IN_WATTS.getData(),
					HIGH_POWER_IN_WATTS.getMeasurementUnit());
		}
	}

	// -------------------------------------------------------------------------
	// Sensor methods
	// -------------------------------------------------------------------------

	public KettleStateSensorData statePullSensor() throws Exception {
		return new KettleStateSensorData(this.currentState);
	}

	public KettleModeSensorData modePullSensor() throws Exception {
		return new KettleModeSensorData(this.currentMode);
	}

	public KettleTemperatureSensorData temperaturePullSensor() throws Exception {
		KettleTemperatureSensorData ret = null;
		switch (this.getExecutionMode()) {
		case STANDARD:
			ret = new KettleTemperatureSensorData(this.currentTemperature.getData());
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			ret = new KettleTemperatureSensorData(this.currentTemperature.getData());
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			try {
				VariableValue<Double> v = this.computeCurrentTemperature();
				ret = new KettleTemperatureSensorData(v.getValue());
			} catch (Throwable e) {
				// Must catch Throwable: AssertionError extends Error, not Exception
				System.out.println("[KETTLE] SIL temperature not ready, using default 20.0C");
				ret = new KettleTemperatureSensorData(20.0);
			}
			break;
		default:
		}
		return ret;
	}

	public KettleCompoundSensorData sensorDataCompound() throws Exception {
		return new KettleCompoundSensorData(this.statePullSensor(), this.modePullSensor(),
				this.temperaturePullSensor());
	}

	public void startSensorDataPush(long period, TimeUnit tu) throws Exception {
		long actualPeriod = -1L;
		if (this.executionMode.isStandard()) {
			actualPeriod = period * tu.toNanos(1);
		} else {
			AcceleratedClock ac = this.clock.get();
			actualPeriod = (long) ((period * tu.toNanos(1)) / ac.getAccelerationFactor());
		}
		this.sensorDataPushTask(actualPeriod);
	}

	protected void sensorDataPushTask(long actualPeriod) throws Exception {
		if (this.currentState != KettleState.OFF) {
			this.traceMessage("Kettle performs sensor data push.\n");
			this.sensorDataPush();

			if (this.executionMode.isStandard() || this.executionMode.isSILTest() || this.executionMode.isHILTest()) {
				this.scheduleTaskOnComponent(new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						try {
							sensorDataPushTask(actualPeriod);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, actualPeriod, TimeUnit.NANOSECONDS);
			}
		}
	}

	protected void sensorDataPush() throws Exception {
		KettleCompoundSensorData data = new KettleCompoundSensorData(this.statePullSensor(),
				this.modePullSensor(), this.temperaturePullSensor());
		this.sensorInboundPort.send(data);
	}
}
