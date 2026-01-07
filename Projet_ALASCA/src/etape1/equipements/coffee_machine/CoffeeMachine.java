package etape1.equipements.coffee_machine;

import java.util.UUID;

import etape1.bases.RegistrationCI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineExternalControlJava4CI;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI;
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
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;


@OfferedInterfaces(offered = { CoffeeMachineUserCI.class, CoffeeMachineInternalControlCI.class,
		CoffeeMachineExternalControlJava4CI.class })
@RequiredInterfaces(required = { RegistrationCI.class, ClocksServerCI.class })
public class CoffeeMachine extends AbstractComponent implements CoffeeMachineInternalControlI, CoffeeMachineUserI,
		CoffeeMachineImplementationI, CoffeeMachineExternalControlI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// BCM4Java information

	

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = true;
	/** URI of the heater inbound port used in tests. */
	public static final String REFLECTION_INBOUND_PORT_URI = "COFFEE-MACHINE-RIP-URI";

	/** URI of the heater port for user interactions. */
	public static final String USER_INBOUND_PORT_URI = "COFFEE-MACHINE-USER-INBOUND-PORT-URI";

	/** URI of the heater port for internal control. */
	public static final String INTERNAL_CONTROL_INBOUND_PORT_URI = "COFFEE-INTERNAL-CONTROL-INBOUND-PORT-URI";

	/** URI of the heater port for internal control. */
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "COFFEE-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	public static final MeasurementUnit POWER_UNIT = MeasurementUnit.WATTS;
	/** measurement unit for tension used in this appliance. */
	public static final MeasurementUnit TENSION_UNIT = MeasurementUnit.VOLTS;

	public static final MeasurementUnit WATT_HOURS = MeasurementUnit.WATT_HOURS;

	public static final MeasurementUnit LITERS = MeasurementUnit.LITERS;

	/** measurement unit for tension used in this appliance. */
	public static final MeasurementUnit TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;

	public static final Measure<Double> MAX_TEMPARATURE = new Measure<Double>(Constants.MAX_TEMPARATURE,
			TEMPERATURE_UNIT);

	public static final Measure<Double> MIN_TEMPARATURE = new Measure<Double>(20.0, TEMPERATURE_UNIT);

	public static final Measure<Double> HIGH_POWER_IN_WATTS = new Measure<Double>(Constants.MAX_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> ECO_POWER_IN_WATTS = new Measure<Double>(Constants.ECO_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> NORMAL_POWER_IN_WATTS = new Measure<Double>(Constants.NORMAL_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> SUSPENDED_POWER_IN_WATTS = new Measure<Double>(Constants.SUSPENDED_MODE_POWER, POWER_UNIT);
	public static final Measure<Double> LOW_POWER_IN_WATTS = new Measure<Double>(0.0, POWER_UNIT);
	public static final Measure<Double> VOLTAGE = new Measure<Double>(Constants.MACHINE_VOLTAGE, TENSION_UNIT);

	public static final Measure<Double> WATER_CAPACITY = new Measure<Double>(Constants.WATER_CAPACITY, LITERS);
	
	public static final Measure<Double> CUP_OF_CAFE_CAPACITY = new Measure<Double>(Constants.CUP_CAPACITY, LITERS);

	public static final CoffeeMachineState INITIAL_STATE = CoffeeMachineState.OFF;

	public static final Measure<Double> INITIAL_WATER_LEVEL = new Measure<Double>(Constants.INITIAL_WATER_LEVEL,
			POWER_UNIT);

	public static final String XML_COFFEE_MACHINE_ADAPTER_DESCRIPTOR = "adapters/coffeem-adapter/coffeeci-descriptor.xml";

	protected CoffeeMachineMode currentMode;
	protected CoffeeMachineState currentState;
	protected Measure<Double> currentPowerLevel;
	protected Measure<Double> currentTemperature;
	protected Measure<Double> machineTotalConsumption;
	protected Measure<Double> currentWaterLevel;

	protected CoffeeMachineUserInboundPort cmuip;
	protected CoffeeMachineInternalInboundPort cmiip;
	protected CoffeeMachineExternalControlJava4InboundPort cmecjip;

	// port to for registering to the HEM
	protected RegistrationOutboundPort rop;

	protected String uid;

	protected boolean isIntegrationTestMode;

	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;

	/**
	 * create a hair dryer component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * true
	 * }	// no precondition.
	 * post	{@code
	 * getState() == CoffeeMachineState.OFF
	 * }
	 * post	{@code
	 * getMode() == CoffeeMachineMode.LOW
	 * }
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected CoffeeMachine(boolean isIntegrationTestMode) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI,
				EXTERNAL_CONTROL_INBOUND_PORT_URI);

	}

	/**
	 * create a hair dryer component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * CoffeeMachineInboundPortURI != null && !CoffeeMachineInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * getState() == CoffeeMachineState.OFF
	 * }
	 * post	{@code
	 * getMode() == CoffeeMachineMode.LOW
	 * }
	 * </pre>
	 * 
	 * @param CoffeeMachineInboundPortURI URI of the hair dryer inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected CoffeeMachine(boolean isIntegrationTestMode, String CoffeeMachineUserInboundPortURI,
			String CoffeeMachineInternalInboundURI, String CoffeeMachineExternalInboundURI) throws Exception {
		this(isIntegrationTestMode, REFLECTION_INBOUND_PORT_URI, CoffeeMachineUserInboundPortURI,
				CoffeeMachineInternalInboundURI, CoffeeMachineExternalInboundURI);
	}

	/**
	 * create a hair dryer component with the given reflection innbound port URI.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * CoffeeMachineInboundPortURI != null && !CoffeeMachineInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * getState() == CoffeeMachineState.OFF
	 * }
	 * post	{@code
	 * getMode() == CoffeeMachineMode.LOW
	 * }
	 * </pre>
	 *
	 * @param reflectionInboundPortURI    URI of the reflection innbound port of the
	 *                                    component.
	 * @param CoffeeMachineInboundPortURI URI of the hair dryer inbound port.
	 * @throws Exception <i>to do</i>.
	 */
	protected CoffeeMachine(boolean isIntegrationTestMode, String reflectionInboundPortURI,
			String coffeeMachineUserInboundPortURI, String coffeeMachineInternalInboundURI,
			String coffeeMachineExternalInboundURI) throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.initialise(isIntegrationTestMode, coffeeMachineUserInboundPortURI, coffeeMachineInternalInboundURI,
				coffeeMachineExternalInboundURI);
	}

	protected void initialise(boolean isIntegrationTestMode, String coffeeMachineUserInboundPortURI,
			String coffeeMachineInternalInboundURI, String coffeeMachineExternalInboundURI) throws Exception {

		this.isIntegrationTestMode = isIntegrationTestMode;

		this.cmiip = new CoffeeMachineInternalInboundPort(coffeeMachineInternalInboundURI, this);
		this.cmiip.publishPort();

		this.cmuip = new CoffeeMachineUserInboundPort(coffeeMachineUserInboundPortURI, this);
		this.cmuip.publishPort();

		this.cmecjip = new CoffeeMachineExternalControlJava4InboundPort(coffeeMachineExternalInboundURI, this);
		this.cmecjip.publishPort();

		if (isIntegrationTestMode) {
			System.out.println("Machine à café publication port Registration (CM)");
			this.rop = new RegistrationOutboundPort(this);
			this.rop.publishPort();
			System.out.println("Machine à café port Registration publié (CM)");
		}

		this.machineTotalConsumption = new Measure<Double>(0.0, POWER_UNIT);
		this.currentState = INITIAL_STATE;
		this.currentPowerLevel = LOW_POWER_IN_WATTS;
		this.currentTemperature = CoffeeMachine.MIN_TEMPARATURE;
		this.currentWaterLevel = INITIAL_WATER_LEVEL;
		this.uid = UUID.randomUUID().toString();

		if (VERBOSE) {
			this.tracer.get().setTitle("Coffee Machine component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();
		try {
			// Publication du port d'enregistrement
			// Connexion avec le HEM réalisée uniquement en cas de test d'intégration
			if (isIntegrationTestMode) {
				System.out.println("Connexion avec HEM pour enregistrement (CM)");
				this.doPortConnection(this.rop.getPortURI(), HEM.REGISTRATION_COFFEE_INBOUND_PORT_URI,
						RegistrationConnector.class.getCanonicalName());
				System.out.println("Connexion avec HEM pour enregistrement réalisée (CM)");
			}

		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.cmiip.unpublishPort();
			this.cmuip.unpublishPort();
			this.cmecjip.unpublishPort();
			if (isIntegrationTestMode) {
				this.rop.unpublishPort();
			}
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	@Override
	public boolean on() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine returns its state: " + this.currentState + ".\n");
		}
		return this.currentState == CoffeeMachineState.ON || this.currentState == CoffeeMachineState.HEATING;
	}

	@Override
	public void turnOn() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine switches on.\n");
		}

		assert !this.on() : new PreconditionException("!on()");

		if (isIntegrationTestMode) {
			this.traceMessage("Coffee Machine registering to HEM");
			this.rop.register(uid, cmecjip.getPortURI(), XML_COFFEE_MACHINE_ADAPTER_DESCRIPTOR);
			this.traceMessage("Coffee Machine registered to HEM !");
		}

		this.currentState = CoffeeMachineState.ON;
		this.currentMode = CoffeeMachineMode.SUSPEND;
		assert this.on() : new PostconditionException("on()");
		assert this.getMode() == CoffeeMachineMode.SUSPEND
				: new PreconditionException("getMode() == CoffeeMachineMode.SUSPEND");

	}

	@Override
	public void turnOff() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine switches off.\n");
		}

		assert this.on() : new PreconditionException("on()");

		if (isIntegrationTestMode) {
			this.rop.unregister(uid);
			this.doPortDisconnection(this.rop.getPortURI());
		}
		this.currentState = CoffeeMachineState.OFF;
		this.currentMode = CoffeeMachineMode.SUSPEND;

		/*
		 * As the coffee machine is switched off so the water is no longer warmed, then
		 * the water temperature cool down until it's reach it's minimal temperature
		 */
		this.currentTemperature = MIN_TEMPARATURE;

		assert !this.on() : new PostconditionException("on()");
	}

	@Override
	public boolean heating() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Heater returns its heating status " + (this.currentState == CoffeeMachineState.HEATING) + ".\n");
		}

		assert this.on() : new PreconditionException("on()");

		return this.currentState == CoffeeMachineState.HEATING;

	}

	@Override
	public void startHeating() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Heater starts heating.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert !this.heating() : new PreconditionException("!heating()");
		assert (this.getMode() == CoffeeMachineMode.ECO || this.getMode() == CoffeeMachineMode.MAX)
				: new PostconditionException(
						"!(this.getMode() == CoffeeMachineMode.ECO || this.getMode() == CoffeeMachineMode.EXPRESSO)");

		assert this.getCurrentWaterLevel().getData() > 0.0
				: new PostconditionException("getCurrentWaterLevel().getData() > 0.0");

		// turn the coffee machine to the heating state
		this.currentState = CoffeeMachineState.HEATING;

		double startTemp = this.currentTemperature.getData();
		double targetTemp = MAX_TEMPARATURE.getData();
		double waterThermalCapacity = Constants.WATER_THERMAL_CAPACITY; // water thermal capacity in J/kg°C
		double waterWeight = this.currentWaterLevel.getData();
		;

		// start heating the water using the power of the coffee machine current mode
		double currentPowerLevel = this.getPowerLevel().getData();

		// calculation of the the theorical heating duration
		double deltaT = targetTemp - startTemp;
		double heatingTimeSeconds = (waterWeight * waterThermalCapacity * deltaT) / currentPowerLevel;

		// record the time at with the machine start warming the water
		double startHeatingTime = System.currentTimeMillis();
		double finalTemperature = 0.0;

		// the simulation factor is use to accelerate the heating process
		double simulationFactor = Constants.HEATING_ACCELERATION_FACTOR;
		double simulatedTime = heatingTimeSeconds / simulationFactor;

		// increasing randomly the water temperature
		while (finalTemperature < MAX_TEMPARATURE.getData()) {

			// calculation proportional to the time duration elapsed
			double elapsed = (System.currentTimeMillis() - startHeatingTime) / 1000.0;
			double progress = Math.min(1.0, elapsed / simulatedTime);

			finalTemperature = startTemp + progress * deltaT;

			if (CoffeeMachine.VERBOSE) {
				this.traceMessage("Coffee Machine: Water current temparature -> " + finalTemperature + "°C\n");
			}

		}

		this.currentTemperature = new Measure<Double>(finalTemperature, TEMPERATURE_UNIT);

		// calculation of the power consummated by the machine
		double powerConsummated = (currentPowerLevel / Constants.HOURS_IN_SECONDS) * heatingTimeSeconds;

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Real heating duration time: " + heatingTimeSeconds + "s, simulated   : " + simulatedTime + "s.\n");

			this.traceMessage("Power consummated by Coffee Machine " + powerConsummated + "Kwh\n");
		}

		double newConsumption = this.machineTotalConsumption.getData() + powerConsummated;

		// update the machine consumption since it's start
		this.machineTotalConsumption = new Measure<Double>(newConsumption, WATT_HOURS);

		// stop water heating
		this.stopHeating();

		assert !this.heating() : new PostconditionException("!heating()");

	}

	@Override
	public void stopHeating() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine stops heating.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert this.heating() : new PreconditionException("heating()");

		switch (this.getMode()) {
		case ECO:
			/*
			 * After heating if the coffee machine is in mode ECO, the coffee machine keep
			 * it's mode to ECO and the water temperature is hold to 60°C
			 */
			this.currentTemperature = new Measure<Double>(Constants.ECO_MODE_WATER_TEMPERATURE, TEMPERATURE_UNIT);
			break;
		case MAX:
			this.setNormalMode();
			/*
			 * After heating if the coffee machine is in mode MAX, the coffee machine change
			 * it's mode to normal and the water temperature is hold to 80°C
			 */
			this.currentTemperature = new Measure<Double>(Constants.NORMAL_MODE_WATER_TEMPERATURE, TEMPERATURE_UNIT);
			break;
		default:
			this.currentTemperature = new Measure<Double>(Constants.NORMAL_MODE_WATER_TEMPERATURE, TEMPERATURE_UNIT);
			this.setNormalMode();
			break;
		}

		this.currentState = CoffeeMachineState.ON;

		assert !this.heating() : new PostconditionException("!heating()");
		assert this.getState() == CoffeeMachineState.ON
				: new PostconditionException("getState() == CoffeeMachineState.ON");
		assert this.getMode() == CoffeeMachineMode.NORMAL || this.getMode() == CoffeeMachineMode.ECO
				: new PostconditionException(
						"getMode() == CoffeeMachineMode.NORMAL || getMode() == CoffeeMachineMode.ECO");

	}

	@Override
	public CoffeeMachineState getState() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine return it's current state " + this.currentState + ".\n");
		}

		return this.currentState;
	}

	@Override
	public CoffeeMachineMode getMode() throws Exception {

		if (CoffeeMachine.VERBOSE) {

			this.traceMessage("Coffee Machine return it's current mode " + this.currentMode + ".\n");
		}

		return this.currentMode;
	}

	@Override
	public void makeExpresso() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine is making an Expresso.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert !this.heating() : new PreconditionException("!heating()");
		assert (this.getMode() == CoffeeMachineMode.MAX || this.getMode() == CoffeeMachineMode.ECO)
				: new PreconditionException("getMode() == CoffeeMachineMode.MAX || getMode() == CoffeeMachineMode.ECO");

		// start heating the water
		this.startHeating();

		double machineNewCurrentWaterLevel = this.currentWaterLevel.getData() - 0.25;

		if (machineNewCurrentWaterLevel < 0.0) {
			machineNewCurrentWaterLevel = 0.0;
		}

		this.currentWaterLevel = new Measure<Double>(machineNewCurrentWaterLevel, TEMPERATURE_UNIT);

		assert !this.heating() : new PreconditionException("!heating()");
		assert this.getMode() == CoffeeMachineMode.NORMAL || this.getMode() == CoffeeMachineMode.ECO
				: new PreconditionException(
						"getMode() == CoffeeMachineMode.NORMAL || getMode() == CoffeeMachineMode.ECO");

	}

	@Override
	public void setSuspendMode() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine turns to suspend mode.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert this.getMode() == CoffeeMachineMode.MAX || this.getMode() == CoffeeMachineMode.ECO
				|| this.getMode() == CoffeeMachineMode.NORMAL
				: new PreconditionException(
						"getMode() == CoffeeMachineMode.MAX || getMode() == CoffeeMachineMode.NORMAL || this.getMode() == CoffeeMachineMode.ECO");

		this.currentMode = CoffeeMachineMode.SUSPEND;
		this.setCurrentPowerLevel(new Measure<Double>(Constants.SUSPENDED_MODE_POWER, POWER_UNIT));

		assert this.getMode() == CoffeeMachineMode.SUSPEND
				: new PreconditionException("getMode() == CoffeeMachineMode.ECO");

	}

	@Override
	public void setEcoMode() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine turns to eco mode.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert this.getMode() == CoffeeMachineMode.MAX || this.getMode() == CoffeeMachineMode.SUSPEND
				|| this.getMode() == CoffeeMachineMode.NORMAL
				: new PreconditionException(
						"getMode() == CoffeeMachineMode.MAX || getMode() == CoffeeMachineMode.NORMAL || this.getMode() == CoffeeMachineMode.SUSPEND");

		this.currentMode = CoffeeMachineMode.ECO;
		this.setCurrentPowerLevel(new Measure<Double>(Constants.ECO_MODE_POWER, POWER_UNIT));

		assert this.getMode() == CoffeeMachineMode.ECO
				: new PreconditionException("getMode() == CoffeeMachineMode.ECO");

	}

	@Override
	public void setNormalMode() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine turns to normal mode.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert this.getMode() == CoffeeMachineMode.MAX || this.getMode() == CoffeeMachineMode.ECO
				|| this.getMode() == CoffeeMachineMode.SUSPEND
				: new PreconditionException(
						"getMode() == CoffeeMachineMode.MAX || getMode() == CoffeeMachineMode.SUSPEND || this.getMode() == CoffeeMachineMode.ECO");

		this.currentMode = CoffeeMachineMode.NORMAL;
		this.setCurrentPowerLevel(new Measure<Double>(Constants.NORMAL_MODE_POWER, POWER_UNIT));

		assert this.getMode() == CoffeeMachineMode.NORMAL
				: new PreconditionException("getMode() == CoffeeMachineMode.NORMAL");

	}

	@Override
	public void setMaxMode() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine turns to max mode.\n");
		}

		assert this.on() : new PreconditionException("on()");
		assert (this.getMode() == CoffeeMachineMode.SUSPEND || this.getMode() == CoffeeMachineMode.ECO
				|| this.getMode() == CoffeeMachineMode.NORMAL)
				: new PreconditionException(
						"getMode() == CoffeeMachineMode.NORMAL || getMode() == CoffeeMachineMode.SUSPEND || this.getMode() == CoffeeMachineMode.ECO");

		this.currentMode = CoffeeMachineMode.MAX;
		this.setCurrentPowerLevel(new Measure<Double>(Constants.MAX_MODE_POWER, POWER_UNIT));

		assert this.getMode() == CoffeeMachineMode.MAX
				: new PreconditionException("getMode() == CoffeeMachineMode.MAX");

	}

	@Override
	public void fillWater() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Filling Coffee Machine water tank.\n");
		}

		assert (this.getCurrentWaterLevel().getData() <= WATER_CAPACITY.getData()
				&& this.getCurrentWaterLevel().getData() >= 0.0)
				: new PreconditionException(
						"getCurrentWaterLevel().getData() <= WATER_CAPACITY.getData() && getCurrentWaterLevel().getData() >= 0.0");

		this.currentWaterLevel = new Measure<Double>(Constants.WATER_CAPACITY, LITERS);

		assert this.getCurrentWaterLevel().getData() == Constants.WATER_CAPACITY
				: new PreconditionException("getCurrentWaterLevel().getData() == Constants.WATER_CAPACITY");

	}

	protected Measure<Double> getCurrentWaterLevel() {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Coffee Machine return it's current water level " + this.currentWaterLevel.getData() + "L.\n");
		}
		return this.currentWaterLevel;
	}

	@Override
	public Measure<Double> getTemperature() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Coffee Machine return it's current temperature " + this.currentTemperature.getData() + "°C.\n");
		}

		return this.currentTemperature;
	}

	@Override
	public Measure<Double> getPowerLevel() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Coffee Machine return it's current power level " + this.currentPowerLevel.getData() + "W.\n");
		}

		return this.currentPowerLevel;
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {

		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Coffee Machine return it's current power level " + HIGH_POWER_IN_WATTS.getData() + "W.\n");
		}

		return HIGH_POWER_IN_WATTS;
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		this.currentPowerLevel = powerLevel;
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Coffee Machine return it's current power level " +this.currentPowerLevel + "W.\n");
		}

	}

	@Override
	public void setMode(int mode) throws Exception {
		if (mode == 1) {
			this.setSuspendMode();
		} else if (mode == 2) {
			this.setEcoMode();
		} else if (mode == 3) {
			this.setNormalMode();
		} else if (mode == 4) {
			this.setMaxMode();
		}
		
	}

}
