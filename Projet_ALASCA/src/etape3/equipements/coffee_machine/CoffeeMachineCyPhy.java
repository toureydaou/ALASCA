package etape3.equipements.coffee_machine;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import etape1.bases.RegistrationCI;
import etape1.equipements.coffee_machine.CoffeeMachine;
import etape1.equipements.coffee_machine.Constants;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineInternalControlI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineUserI;
import etape1.equipements.coffee_machine.ports.CoffeeMachineExternalControlJava4InboundPort;
import etape1.equipements.coffee_machine.ports.CoffeeMachineInternalInboundPort;
import etape1.equipements.coffee_machine.ports.CoffeeMachineUserInboundPort;
import etape1.equipements.hem.HEM;
import etape1.equipements.registration.connector.RegistrationConnector;
import etape1.equipements.registration.ports.RegistrationOutboundPort;
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
import etape3.equipements.coffee_machine.connections.ports.CoffeeMachineActuatorInboundPort;
import etape3.equipements.coffee_machine.connections.ports.CoffeeMachineSensorDataInboundPort;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineActuatorCI;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineCompoundSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineModeSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineStateSensorData;
import etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData;
import etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData;
import etape3.equipements.coffee_machine.sil.CoffeeMachineElectricitySILModel;
import etape3.equipements.coffee_machine.sil.CoffeeMachineStateSILModel;
import etape3.equipements.coffee_machine.sil.CoffeeMachineTemperatureSILModel;
import etape3.equipements.coffee_machine.sil.Local_SIL_SimulationArchitectures;
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
		@LocalArchitecture(uri = "silUnitTests", rootModelURI = "CoffeeMachineCoupledModel", simulatedTimeUnit = TimeUnit.HOURS, externalEvents = @ModelExternalEvents()),
		@LocalArchitecture(uri = "silIntegrationTests", rootModelURI = "CoffeeMachineCoupledModel", simulatedTimeUnit = TimeUnit.HOURS, externalEvents = @ModelExternalEvents(exported = {
				SwitchOnCoffeeMachine.class, SwitchOffCoffeeMachine.class, SetEcoModeCoffeeMachine.class,
				SetMaxModeCoffeeMachine.class, SetNormalModeCoffeeMachine.class, SetSuspendedModeCoffeeMachine.class,
				Heat.class, DoNotHeat.class, MakeCoffee.class, ServeCoffee.class, FillWaterCoffeeMachine.class })) })
@OfferedInterfaces(offered = { CoffeeMachineUserCI.class, CoffeeMachineInternalControlCI.class,
		CoffeeMachineExternalControlJava4CI.class, CoffeeMachineSensorDataCI.CoffeeMachineSensorOfferedPullCI.class,
		CoffeeMachineActuatorCI.class })
@RequiredInterfaces(required = { RegistrationCI.class })
public class CoffeeMachineCyPhy extends AbstractCyPhyComponent
		implements CoffeeMachineUserI, CoffeeMachineInternalControlI, CoffeeMachineExternalControlI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// BCM4Java information
	public static final String REFLECTION_INBOUND_PORT_URI = "COFFEE-MACHINE-RIP-URI";
	public static final String USER_INBOUND_PORT_URI = "COFFEE-MACHINE-USER-INBOUND-PORT-URI";
	public static final String INTERNAL_CONTROL_INBOUND_PORT_URI = "COFFEE-INTERNAL-CONTROL-INBOUND-PORT-URI";
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "COFFEE-EXTERNAL-CONTROL-INBOUND-PORT-URI";
	public static final String SENSOR_INBOUND_PORT_URI = "COFFEE-MACHINE-SENSOR-INBOUND-PORT-URI";
	public static final String ACTUATOR_INBOUND_PORT_URI = "COFFEE-MACHINE-ACTUATOR-INBOUND-PORT-URI";

	// Physical units
	public static final MeasurementUnit POWER_UNIT = MeasurementUnit.WATTS;
	public static final MeasurementUnit TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;
	public static final MeasurementUnit LITERS = MeasurementUnit.LITERS;
	public static final MeasurementUnit WATT_HOURS = MeasurementUnit.WATT_HOURS;

	// Constants
	public static final Measure<Double> MAX_TEMPERATURE = new Measure<>(Constants.MAX_TEMPARATURE, TEMPERATURE_UNIT);
	public static final Measure<Double> MIN_TEMPERATURE = new Measure<>(20.0, TEMPERATURE_UNIT);
	public static final Measure<Double> WATER_CAPACITY = new Measure<>(Constants.WATER_CAPACITY, LITERS);
	public static final Measure<Double> HIGH_POWER_IN_WATTS = new Measure<>(Constants.MAX_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> ECO_POWER_IN_WATTS = new Measure<>(Constants.ECO_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> NORMAL_POWER_IN_WATTS = new Measure<>(Constants.NORMAL_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> SUSPENDED_POWER_IN_WATTS = new Measure<>(Constants.SUSPENDED_MODE_POWER,
			POWER_UNIT);
	public static final Measure<Double> INITIAL_WATER_LEVEL = new Measure<>(Constants.INITIAL_WATER_LEVEL, LITERS);

	// Ports
	protected CoffeeMachineUserInboundPort cmuip;
	protected CoffeeMachineInternalInboundPort cmiip;
	protected CoffeeMachineExternalControlJava4InboundPort cmecjip;
	protected CoffeeMachineSensorDataInboundPort sensorInboundPort;
	protected CoffeeMachineActuatorInboundPort actuatorInboundPort;

	// State variables
	protected CoffeeMachineState currentState;
	protected CoffeeMachineMode currentMode;
	protected TimedMeasure<Double> currentPowerLevel;
	protected TimedMeasure<Double> currentTemperature;
	protected TimedMeasure<Double> currentWaterLevel;
	protected TimedMeasure<Double> machineTotalConsumption;

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
	protected static final String CURRENT_WATER_LEVEL_NAME = "currentWaterLevel";
	

	public static final String XML_COFFEE_MACHINE_ADAPTER_DESCRIPTOR = "adapters/coffeem-adapter/coffeeci-descriptor.xml";


	public static final String COFFEE_MACHINE_CONNECTOR_NAME = "CoffeeMachineGeneratedConnector";
	
	protected RegistrationOutboundPort rop;

	protected String uid;

	protected boolean isIntegrationTestMode;

	protected RTAtomicSimulatorPlugin asp;
	protected final String localArchitectureURI;
	protected final double accelerationFactor;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected CoffeeMachineCyPhy(boolean isIntegrationTestMode) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI,
				SENSOR_INBOUND_PORT_URI, ACTUATOR_INBOUND_PORT_URI);
	}

	protected CoffeeMachineCyPhy(boolean isIntegrationTestMode, String userInboundPortURI, String internalControlInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI, String actuatorInboundPortURI)
			throws Exception {
		this(isIntegrationTestMode, AbstractPort.generatePortURI(CyPhyReflectionCI.class), userInboundPortURI, internalControlInboundPortURI,
				externalControlInboundPortURI, sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected CoffeeMachineCyPhy(boolean isIntegrationTestMode, String reflectionInboundPortURI, String userInboundPortURI,
			String internalControlInboundPortURI, String externalControlInboundPortURI, String sensorInboundPortURI,
			String actuatorInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS);

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(isIntegrationTestMode, userInboundPortURI, internalControlInboundPortURI, externalControlInboundPortURI,
				sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected CoffeeMachineCyPhy(boolean isIntegrationTestMode, ExecutionMode executionMode, String clockURI) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI,
				SENSOR_INBOUND_PORT_URI, ACTUATOR_INBOUND_PORT_URI, executionMode, clockURI);
	}

	protected CoffeeMachineCyPhy(boolean isIntegrationTestMode, String userInboundPortURI, String internalControlInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI, String actuatorInboundPortURI,
			ExecutionMode executionMode, String clockURI) throws Exception {
		this(isIntegrationTestMode, AbstractPort.generatePortURI(CyPhyReflectionCI.class), userInboundPortURI, internalControlInboundPortURI,
				externalControlInboundPortURI, sensorInboundPortURI, actuatorInboundPortURI, executionMode, clockURI);
	}

	protected CoffeeMachineCyPhy(boolean isIntegrationTestMode, String reflectionInboundPortURI, String userInboundPortURI,
			String internalControlInboundPortURI, String externalControlInboundPortURI, String sensorInboundPortURI,
			String actuatorInboundPortURI, ExecutionMode executionMode, String clockURI) throws Exception {
		super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS, executionMode,
				clockURI, null);

		assert executionMode != null && executionMode.isTestWithoutSimulation()
				: new PreconditionException("executionMode != null && executionMode.isTestWithoutSimulation()");

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(isIntegrationTestMode, userInboundPortURI, internalControlInboundPortURI, externalControlInboundPortURI,
				sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected CoffeeMachineCyPhy(boolean isIntegrationTestMode, String reflectionInboundPortURI, String userInboundPortURI,
			String internalControlInboundPortURI, String externalControlInboundPortURI, String sensorInboundPortURI,
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

		this.initialise(isIntegrationTestMode, userInboundPortURI, internalControlInboundPortURI, externalControlInboundPortURI,
				sensorInboundPortURI, actuatorInboundPortURI);
	}

	protected void initialise(boolean isIntegrationTestMode, String userInboundPortURI, String internalControlInboundPortURI,
			String externalControlInboundPortURI, String sensorInboundPortURI, String actuatorInboundPortURI)
			throws Exception {

		this.isIntegrationTestMode = isIntegrationTestMode;
		this.uid = COFFEE_MACHINE_CONNECTOR_NAME;
		System.out.println("DEBUG CoffeeMachine: uid initialized to: " + this.uid);

		this.currentState = CoffeeMachineState.OFF;
		this.currentMode = CoffeeMachineMode.SUSPEND;

		this.cmuip = new CoffeeMachineUserInboundPort(userInboundPortURI, this);
		this.cmuip.publishPort();

		this.cmiip = new CoffeeMachineInternalInboundPort(internalControlInboundPortURI, this);
		this.cmiip.publishPort();

		this.cmecjip = new CoffeeMachineExternalControlJava4InboundPort(externalControlInboundPortURI, this);
		this.cmecjip.publishPort();

		this.sensorInboundPort = new CoffeeMachineSensorDataInboundPort(sensorInboundPortURI, this);
		this.sensorInboundPort.publishPort();

		this.actuatorInboundPort = new CoffeeMachineActuatorInboundPort(actuatorInboundPortURI, this);
		this.actuatorInboundPort.publishPort();
		
		
		
		if (isIntegrationTestMode) {
			System.out.println("Machine à café publication port Registration (CM)");
			this.rop = new RegistrationOutboundPort(this);
			this.rop.publishPort();
			System.out.println("Machine à café port Registration publié (CM)");
		}
		

		if (VERBOSE) {
			this.tracer.get().setTitle("CoffeeMachine CyPhy component");
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
					System.out.println("Connexion avec HEM pour enregistrement (CM)");
					this.doPortConnection(this.rop.getPortURI(), HEM.REGISTRATION_COFFEE_INBOUND_PORT_URI,
							RegistrationConnector.class.getCanonicalName());
					System.out.println("Connexion avec HEM pour enregistrement réalisée (CM)");
				}
				break;
			case UNIT_TEST_WITH_SIL_SIMULATION:
			case INTEGRATION_TEST_WITH_SIL_SIMULATION:

				if (isIntegrationTestMode) {
					System.out.println("Connexion avec HEM pour enregistrement (CM)");
					this.doPortConnection(this.rop.getPortURI(), HEM.REGISTRATION_COFFEE_INBOUND_PORT_URI,
							RegistrationConnector.class.getCanonicalName());
					System.out.println("Connexion avec HEM pour enregistrement réalisée (CM)");
				}
				
				RTArchitecture architecture = (RTArchitecture) this.localSimulationArchitectures
						.get(this.localArchitectureURI);

				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;

					@Override
					public VariableValue<Double> getModelVariableValue(String modelURI, String name) throws Exception {
						if (name.equals(CURRENT_TEMPERATURE_NAME)) {
							return ((CoffeeMachineTemperatureSILModel) this.atomicSimulators.get(modelURI)
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
		this.traceMessage("CoffeeMachine CyPhy executes.\n");

		switch (this.getExecutionMode()) {
		case STANDARD:
			this.currentPowerLevel = new TimedMeasure<>(0.0, POWER_UNIT);
			this.currentTemperature = new TimedMeasure<>(MIN_TEMPERATURE.getData(), TEMPERATURE_UNIT);
			this.currentWaterLevel = new TimedMeasure<>(INITIAL_WATER_LEVEL.getData(), LITERS);
			this.machineTotalConsumption = new TimedMeasure<>(0.0, WATT_HOURS);
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI, this.clockURI);
			this.currentPowerLevel = new TimedMeasure<>(0.0, POWER_UNIT, this.getClock(),
					this.getClock().getStartInstant());
			this.currentTemperature = new TimedMeasure<>(MIN_TEMPERATURE.getData(), TEMPERATURE_UNIT, this.getClock(),
					this.getClock().getStartInstant());
			this.currentWaterLevel = new TimedMeasure<>(INITIAL_WATER_LEVEL.getData(), LITERS, this.getClock(),
					this.getClock().getStartInstant());
			this.machineTotalConsumption = new TimedMeasure<>(0.0, WATT_HOURS, this.getClock(),
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
			this.currentTemperature = new TimedMeasure<>(MIN_TEMPERATURE.getData(), TEMPERATURE_UNIT,
					this.getClock4Simulation(), this.getClock4Simulation().getStartInstant());
			this.currentWaterLevel = new TimedMeasure<>(INITIAL_WATER_LEVEL.getData(), LITERS,
					this.getClock4Simulation(), this.getClock4Simulation().getStartInstant());
			this.machineTotalConsumption = new TimedMeasure<>(0.0, WATT_HOURS, this.getClock4Simulation(),
					this.getClock4Simulation().getStartInstant());

			this.getClock4Simulation().waitUntilEnd();
			Thread.sleep(200L);
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI, this.clockURI);

			this.currentPowerLevel = new TimedMeasure<>(0.0, POWER_UNIT, this.getClock4Simulation(),
					this.getClock4Simulation().getStartInstant());
			this.currentTemperature = new TimedMeasure<>(MIN_TEMPERATURE.getData(), TEMPERATURE_UNIT,
					this.getClock4Simulation(), this.getClock4Simulation().getStartInstant());
			this.currentWaterLevel = new TimedMeasure<>(INITIAL_WATER_LEVEL.getData(), LITERS,
					this.getClock4Simulation(), this.getClock4Simulation().getStartInstant());
			this.machineTotalConsumption = new TimedMeasure<>(0.0, WATT_HOURS, this.getClock4Simulation(),
					this.getClock4Simulation().getStartInstant());
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
			this.cmuip.unpublishPort();
			this.cmiip.unpublishPort();
			this.cmecjip.unpublishPort();
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

	protected RTArchitecture createLocalSimulationArchitecture(String architectureURI, String rootModelURI,
			TimeUnit simulatedTimeUnit, double accelerationFactor) throws Exception {
		assert architectureURI != null && !architectureURI.isEmpty();
		assert rootModelURI != null && !rootModelURI.isEmpty();
		assert simulatedTimeUnit != null;
		assert accelerationFactor > 0.0;

		RTArchitecture ret = null;
		if (architectureURI.equals(UNIT_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.createCoffeeMachineSIL_Architecture4UnitTest(architectureURI,
					rootModelURI, simulatedTimeUnit, accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.createCoffeeMachine_SIL_LocalArchitecture4IntegrationTest(
					architectureURI, rootModelURI, simulatedTimeUnit, accelerationFactor);
		} else {
			throw new BCMException("Unknown local simulation architecture URI: " + architectureURI);
		}
		return ret;
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	@Override
	public boolean on() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine returns its state: " + this.currentState + ".\n");
		}
		return this.currentState == CoffeeMachineState.ON || this.currentState == CoffeeMachineState.HEATING;
	}

	@Override
	public void turnOn() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine switches on.\n");
		}

		assert !this.on() : new PreconditionException("!on()");

		this.currentState = CoffeeMachineState.ON;
		this.currentMode = CoffeeMachineMode.SUSPEND;
		

		if (this.getExecutionMode().isSILTest()) {
			try {
				if (VERBOSE) {
					this.traceMessage("CoffeeMachine triggering SwitchOn event in simulation.\n");
				}
				((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
						t -> new SwitchOnCoffeeMachine(t));
				if (VERBOSE) {
					this.traceMessage(
							"CoffeeMachine sending state notification to controller: " + this.currentState + "\n");
				}

				this.sensorInboundPort.send(new CoffeeMachineStateSensorData(this.currentState));
				if (VERBOSE) {
					this.traceMessage("CoffeeMachine state notification sent successfully.\n");
				}
			} catch (Exception e) {
				System.out.println(
						"ERROR sending state notification: " + e.getClass().getName() + ": " + e.getMessage() + "\n");
				e.printStackTrace();
			}
		}
		
		if (isIntegrationTestMode) {
			this.traceMessage("Coffee Machine registering to HEM");
			System.out.println("DEBUG CoffeeMachine.turnOn(): uid = " + uid);
			System.out.println("DEBUG CoffeeMachine.turnOn(): this.uid = " + this.uid);
			System.out.println("DEBUG CoffeeMachine.turnOn(): COFFEE_MACHINE_CONNECTOR_NAME = " + COFFEE_MACHINE_CONNECTOR_NAME);
			this.rop.register(uid, cmecjip.getPortURI(), XML_COFFEE_MACHINE_ADAPTER_DESCRIPTOR);
			this.traceMessage("Coffee Machine registered to HEM !");
		}

		assert this.on() : new PostconditionException("on()");
	}

	@Override
	public void turnOff() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine switches off.\n");
		}

		assert this.on() : new PreconditionException("on()");
		
		

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new CoffeeMachineStateSensorData(CoffeeMachineState.OFF));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new SwitchOffCoffeeMachine(t));
		}
		

		this.currentState = CoffeeMachineState.OFF;
		this.currentMode = CoffeeMachineMode.SUSPEND;
		this.currentTemperature = new TimedMeasure<>(MIN_TEMPERATURE.getData(), TEMPERATURE_UNIT);
		
		if (isIntegrationTestMode) {
			this.rop.unregister(uid);
			this.doPortDisconnection(this.rop.getPortURI());
		}

		assert !this.on() : new PostconditionException("!on()");
	}

	@Override
	public boolean heating() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine returns its heating status "
					+ (this.currentState == CoffeeMachineState.HEATING) + ".\n");
		}

		assert this.on() : new PreconditionException("on()");
		return this.currentState == CoffeeMachineState.HEATING;
	}

	@Override
	public void startHeating() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine starts heating.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert !this.heating() : new PreconditionException("!heating()");

		this.currentState = CoffeeMachineState.HEATING;

		if (this.getExecutionMode().isSILTest()) {
			// Notify controller of state change to HEATING
			this.sensorInboundPort.send(new CoffeeMachineStateSensorData(this.currentState));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI, t -> new Heat(t));
		}

		assert this.heating() : new PostconditionException("heating()");
	}

	@Override
	public void stopHeating() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine stops heating.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert this.heating() : new PreconditionException("heating()");

		this.currentState = CoffeeMachineState.ON;

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new CoffeeMachineStateSensorData(this.currentState));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new DoNotHeat(t));
		}
		
		assert !this.heating() : new PostconditionException("!heating()");
	}

	@Override
	public CoffeeMachineState getState() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine returns its state " + this.currentState + ".\n");
		}
		return this.currentState;
	}

	@Override
	public CoffeeMachineMode getMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine returns its mode " + this.currentMode + ".\n");
		}
		return this.currentMode;
	}

	@Override
	public void setMode(int mode) throws Exception {
		switch (mode) {
		case 1:
			setSuspendMode();
			break;
		case 2:
			setEcoMode();
			break;
		case 3:
			setNormalMode();
			break;
		case 4:
			setMaxMode();
			break;
		}
	}

	@Override
	public void setSuspendMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine sets suspend mode.\n");
		}

		assert this.on() : new PreconditionException("on()");

		this.currentMode = CoffeeMachineMode.SUSPEND;
		this.setCurrentPowerLevel(SUSPENDED_POWER_IN_WATTS);

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new SetSuspendedModeCoffeeMachine(t));
		}

		assert this.getMode() == CoffeeMachineMode.SUSPEND;
	}

	@Override
	public void setEcoMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine sets eco mode.\n");
		}

		assert this.on() : new PreconditionException("on()");

		this.currentMode = CoffeeMachineMode.ECO;
		this.setCurrentPowerLevel(ECO_POWER_IN_WATTS);

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new SetEcoModeCoffeeMachine(t));
		}

		assert this.getMode() == CoffeeMachineMode.ECO;
	}

	@Override
	public void setNormalMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine sets normal mode.\n");
		}

		assert this.on() : new PreconditionException("on()");

		this.currentMode = CoffeeMachineMode.NORMAL;
		this.setCurrentPowerLevel(NORMAL_POWER_IN_WATTS);

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new SetNormalModeCoffeeMachine(t));
		}

		assert this.getMode() == CoffeeMachineMode.NORMAL;
	}

	@Override
	public void setMaxMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine sets max mode.\n");
		}

		assert this.on() : new PreconditionException("on()");

		this.currentMode = CoffeeMachineMode.MAX;
		this.setCurrentPowerLevel(HIGH_POWER_IN_WATTS);

		if (this.getExecutionMode().isSILTest()) {
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new SetMaxModeCoffeeMachine(t));
		}

		assert this.getMode() == CoffeeMachineMode.MAX;
	}

	@Override
	public void makeExpresso() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine makes an expresso.\n");
		}

		assert this.on() : new PreconditionException("on()");

		this.currentState = CoffeeMachineState.HEATING;

		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new CoffeeMachineStateSensorData(this.currentState));
			// Trigger MakeCoffee event to start heating
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new MakeCoffee(t));
		} else {
			// Simulate water consumption in non-simulation mode
			double newWaterLevel = Math.max(0.0, this.currentWaterLevel.getData() - 0.25);
			this.currentWaterLevel = new TimedMeasure<>(newWaterLevel, LITERS);
		}
		
	}

	@Override
	public void serveCoffee() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine serve coffee.\n");
		}

		double newWaterLevel = Math.max(0.0,
				this.currentWaterLevel.getData() - CoffeeMachine.CUP_OF_CAFE_CAPACITY.getData());
		this.currentWaterLevel = new TimedMeasure<>(newWaterLevel, LITERS);
		
		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new WaterLevelSensorData(currentWaterLevel));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineElectricitySILModel.URI,
					t -> new ServeCoffee(t));
		}
	}

	@Override
	public void fillWater() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine fills water tank.\n");
		}

		this.currentWaterLevel = new TimedMeasure<>(WATER_CAPACITY.getData(), LITERS);
		
		if (this.getExecutionMode().isSILTest()) {
			this.sensorInboundPort.send(new WaterLevelSensorData(currentWaterLevel));
			((RTAtomicSimulatorPlugin) this.asp).triggerExternalEvent(CoffeeMachineStateSILModel.URI,
					t -> new FillWaterCoffeeMachine(t,
							new FillWaterCoffeeMachine.WaterValue(WATER_CAPACITY.getData())));
		}
	}

	@Override
	public Measure<Double> getTemperature() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine returns temperature.\n");
		}

		Measure<Double> temp = null;
		if (this.executionMode.isSILTest()) {
			VariableValue<Double> v = this.computeCurrentTemperature();
			if (v != null) {
				temp = new Measure<>(v.getValue(), TEMPERATURE_UNIT);
			} else {
				temp = new Measure<>(this.currentTemperature.getData(), TEMPERATURE_UNIT);
			}
		} else {
			temp = new Measure<>(this.currentTemperature.getData(), this.currentTemperature.getMeasurementUnit());
		}
		return temp;
	}

	@SuppressWarnings("unchecked")
	protected VariableValue<Double> computeCurrentTemperature() throws Exception {
		try {
			return (VariableValue<Double>) this.asp.getModelVariableValue(
					CoffeeMachineTemperatureSILModel.URI, CURRENT_TEMPERATURE_NAME);
		} catch (Throwable e) {
			return null;
		}
	}


	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine returns max power level.\n");
		}
		return HIGH_POWER_IN_WATTS;
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine sets power level to " + powerLevel + ".\n");
		}

		assert this.on() : new PreconditionException("on()");

		if (powerLevel.getData() <= getMaxPowerLevel().getData()) {
			this.currentPowerLevel = new TimedMeasure<>(powerLevel.getData(), powerLevel.getMeasurementUnit());
		} else {
			this.currentPowerLevel = new TimedMeasure<>(HIGH_POWER_IN_WATTS.getData(),
					HIGH_POWER_IN_WATTS.getMeasurementUnit());
		}
	}

	@Override
	public Measure<Double> getPowerLevel() throws Exception {
		if (VERBOSE) {
			this.traceMessage("CoffeeMachine returns power level.\n");
		}

		assert this.on() : new PreconditionException("on()");

		return new Measure<>(this.currentPowerLevel.getData(), this.currentPowerLevel.getMeasurementUnit());
	}

	// -------------------------------------------------------------------------
	// Sensor methods
	// -------------------------------------------------------------------------

	public CoffeeMachineStateSensorData statePullSensor() throws Exception {
		return new CoffeeMachineStateSensorData(this.currentState);
	}

	public CoffeeMachineModeSensorData modePullSensor() throws Exception {
		return new CoffeeMachineModeSensorData(this.currentMode);
	}

	public TemperatureSensorData temperaturePullSensor() throws Exception {
		TemperatureSensorData ret = null;
		switch (this.getExecutionMode()) {
		case STANDARD:
			ret = new TemperatureSensorData(this.currentTemperature);
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			ret = new TemperatureSensorData(
					new TimedMeasure<>(this.currentTemperature.getData(), this.currentTemperature.getMeasurementUnit(),
							this.getClock(), this.currentTemperature.getTimestamp()));
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			VariableValue<Double> v = this.computeCurrentTemperature();
			if (v != null) {
				ret = new TemperatureSensorData(new TimedMeasure<>(v.getValue(), TEMPERATURE_UNIT,
						this.getClock4Simulation(), this.getClock4Simulation().instantOfSimulatedTime(v.getTime())));
			} else {
				ret = new TemperatureSensorData(
						new TimedMeasure<>(this.currentTemperature.getData(), TEMPERATURE_UNIT,
								this.getClock4Simulation(), Instant.now()));
			}
			break;
		default:
		}
		return ret;
	}

	/**
	 * return the water level sensor data.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return the water level sensor data.
	 * @throws Exception <i>to do</i>.
	 */
	public WaterLevelSensorData waterLevelPullSensor() throws Exception {
		WaterLevelSensorData ret = null;
		switch (this.getExecutionMode()) {
		case STANDARD:
			ret = new WaterLevelSensorData(this.currentWaterLevel);
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			ret = new WaterLevelSensorData(
					new TimedMeasure<>(this.currentWaterLevel.getData(), this.currentWaterLevel.getMeasurementUnit(),
							this.getClock(), this.currentWaterLevel.getTimestamp()));
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			// Water level is tracked by the component itself (fillWater,
			// makeExpresso, serveCoffee update currentWaterLevel directly).
			// Unlike temperature, water level is not computed by the
			// simulation model in this component.
			ret = new WaterLevelSensorData(
					new TimedMeasure<>(this.currentWaterLevel.getData(),
							this.currentWaterLevel.getMeasurementUnit(),
							this.getClock4Simulation(),
							this.currentWaterLevel.getTimestamp()));
			break;
		default:
		}
		return ret;
	}

	/**
	 * return a compound sensor data containing all sensor readings.
	 * 
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * This method is called by the {@code get} method of the inbound port to
	 * retrieve all sensor data at once in the pull protocol. It aggregates the
	 * state, mode, temperature, and water level into a single compound sensor data
	 * object.
	 * </p>
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return compound sensor data with all readings.
	 * @throws Exception <i>to do</i>.
	 */
	public CoffeeMachineCompoundSensorData sensorDataCompound() throws Exception {
		return new CoffeeMachineCompoundSensorData(this.statePullSensor(), this.modePullSensor(),
				this.temperaturePullSensor(), this.waterLevelPullSensor());
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
		if (this.currentState != CoffeeMachineState.OFF) {
			this.traceMessage("CoffeeMachine performs sensor data push.\n");
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
		CoffeeMachineCompoundSensorData data = new CoffeeMachineCompoundSensorData(this.statePullSensor(),
				this.modePullSensor(), this.temperaturePullSensor(), this.waterLevelPullSensor());
		this.sensorInboundPort.send(data);
	}
}