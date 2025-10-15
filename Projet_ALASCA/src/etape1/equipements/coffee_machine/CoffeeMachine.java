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
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import physical_data.Measure;
import physical_data.MeasurementUnit;

@OfferedInterfaces(offered = { CoffeeMachineUserCI.class, CoffeeMachineInternalControlCI.class,
		CoffeeMachineExternalControlJava4CI.class })
@RequiredInterfaces(required = { RegistrationCI.class, ClocksServerCI.class })
public class CoffeeMachine extends AbstractComponent
		implements CoffeeMachineInternalControlI, CoffeeMachineUserI, CoffeeMachineImplementationI, CoffeeMachineExternalControlI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// BCM4Java information

	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Coffee Machine is switched on.
	 */
	public static final int SWITCH_ON_DELAY = 2;
	/**
	 * in clock-driven scenario, the delay from the start instant at which the
	 * Coffee Machine is switched off.
	 */
	public static final int SWITCH_OFF_DELAY = 5;

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

	/** measurement unit for tension used in this appliance. */
	public static final MeasurementUnit TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;

	public static final Measure<Double> MAX_TEMPARATURE = new Measure<Double>(100.0, TEMPERATURE_UNIT);

	public static final Measure<Double> MIN_TEMPARATURE = new Measure<Double>(0.0, TEMPERATURE_UNIT);

	public static final Measure<Double> HIGH_POWER_IN_WATTS = new Measure<Double>(1500.0, POWER_UNIT);
	public static final Measure<Double> LOW_POWER_IN_WATTS = new Measure<Double>(0.0, MeasurementUnit.WATTS);
	public static final Measure<Double> VOLTAGE = new Measure<Double>(220.0, MeasurementUnit.VOLTS);

	public static final CoffeeMachineState INITIAL_STATE = CoffeeMachineState.OFF;
	public static final CoffeeMachineMode INITIAL_MODE = CoffeeMachineMode.EXPRESSO;

	public static final String XML_COFFEE_MACHINE_ADAPTER_DESCRIPTOR = "adapters/coffeem-adapter/coffeeci-descriptor.xml";

	protected CoffeeMachineMode currentMode;
	protected CoffeeMachineState currentState;
	protected Measure<Double> currentPowerLevel;
	protected Measure<Double> currentTemperature;

	protected CoffeeMachineUserInboundPort cmuip;
	protected CoffeeMachineInternalInboundPort cmiip;
	protected CoffeeMachineExternalControlJava4InboundPort cmecjip;

	// port to for registering to the HEM
	protected RegistrationOutboundPort rop;

	protected String uid;

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
	protected CoffeeMachine() throws Exception {
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI);

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
	protected CoffeeMachine(String CoffeeMachineUserInboundPortURI, String CoffeeMachineInternalInboundURI,
			String CoffeeMachineExternalInboundURI) throws Exception {
		this(REFLECTION_INBOUND_PORT_URI, CoffeeMachineUserInboundPortURI, CoffeeMachineInternalInboundURI,
				CoffeeMachineExternalInboundURI);
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
	protected CoffeeMachine(String reflectionInboundPortURI, String coffeeMachineUserInboundPortURI,
			String coffeeMachineInternalInboundURI, String coffeeMachineExternalInboundURI) throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.initialise(coffeeMachineUserInboundPortURI, coffeeMachineInternalInboundURI,
				coffeeMachineExternalInboundURI);
	}

	protected void initialise(String coffeeMachineUserInboundPortURI, String coffeeMachineInternalInboundURI,
			String coffeeMachineExternalInboundURI) throws Exception {
		this.cmiip = new CoffeeMachineInternalInboundPort(coffeeMachineInternalInboundURI, this);
		this.cmiip.publishPort();

		this.cmuip = new CoffeeMachineUserInboundPort(coffeeMachineUserInboundPortURI, this);
		this.cmuip.publishPort();

		this.cmecjip = new CoffeeMachineExternalControlJava4InboundPort(coffeeMachineExternalInboundURI, this);
		this.cmecjip.publishPort();

		System.out.println("Machine à café publication port Registration (CM)");
		this.rop = new RegistrationOutboundPort(this);
		this.rop.publishPort();
		System.out.println("Machine à café port Registration publié (CM)");
		
		this.currentMode = CoffeeMachineMode.EXPRESSO;
		this.currentState = CoffeeMachineState.OFF;
		this.currentPowerLevel = CoffeeMachine.HIGH_POWER_IN_WATTS;
		this.currentTemperature = CoffeeMachine.MIN_TEMPARATURE;
		this.uid = UUID.randomUUID().toString();

		if (VERBOSE) {
			this.tracer.get().setTitle("Coffee Machine tester component");
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
			System.out.println("Connexion avec HEM pour enregistrement (CM)");
			this.doPortConnection(this.rop.getPortURI(), HEM.REGISTRATION_COFFEE_INBOUND_PORT_URI,
					RegistrationConnector.class.getCanonicalName());
			System.out.println("Connexion avec HEM pour enregistrement réalisée (CM)");

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
			this.rop.unpublishPort();
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

		this.traceMessage("Coffee Machine registering to HEM");
		System.out.println("Enregistrement de la machine à café (CM)");
		this.rop.register(uid, cmecjip.getPortURI(), XML_COFFEE_MACHINE_ADAPTER_DESCRIPTOR);
		System.out.println("Machine à café enregistrée sur le HEM (CM)");
		this.traceMessage("Coffee Machine registered to HEM !");
		this.currentState = CoffeeMachineState.ON;

		assert this.on() : new PostconditionException("on()");

	}

	@Override
	public void turnOff() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Coffee Machine switches off.\n");
		}

		assert this.on() : new PreconditionException("on()");

		this.rop.unregister(uid);
		this.doPortDisconnection(this.rop.getPortURI());

		//this.stopHeating();
		this.currentState = CoffeeMachineState.OFF;

		assert !this.on() : new PostconditionException("on()");
	}

	@Override
	public boolean heating() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage(
					"Heater returns its heating status " + (this.currentState == CoffeeMachineState.HEATING) + ".\n");
		}

		assert !this.on() : new PreconditionException("!on()");

		return this.currentState == CoffeeMachineState.HEATING;

	}

	@Override
	public void startHeating() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Heater starts heating.\n");
		}
		assert this.on() : new PreconditionException("on()");
		assert !this.heating() : new PreconditionException("!heating()");

		this.currentState = CoffeeMachineState.HEATING;

		assert this.heating() : new PostconditionException("heating()");

	}

	@Override
	public void stopHeating() throws Exception {
		if (CoffeeMachine.VERBOSE) {
			this.traceMessage("Heater stops heating.\n");
		}
		assert this.on() : new PreconditionException("on()");
		assert this.heating() : new PreconditionException("heating()");

		this.currentState = CoffeeMachineState.ON;

		assert !this.heating() : new PostconditionException("!heating()");

	}

	@Override
	public CoffeeMachineState getState() throws Exception {
		return this.currentState;
	}

	@Override
	public CoffeeMachineMode getMode() throws Exception {
		return this.getMode();
	}

	@Override
	public void setExpresso() throws Exception {
		assert this.on() : new PreconditionException("on()");
		assert !this.heating() : new PreconditionException("!heating()");
		assert this.getMode() != CoffeeMachineMode.EXPRESSO
				: new PreconditionException("getMode() != CoffeeMachineMode.EXPRESSO");

		this.currentMode = CoffeeMachineMode.EXPRESSO;
		this.startHeating();

		assert this.getMode() == CoffeeMachineMode.EXPRESSO
				: new PreconditionException("this.getMode() == CoffeeMachineMode.EXPRESSO");

	}

	@Override
	public void setThe() throws Exception {
		assert this.on() : new PreconditionException("on()");
		assert !this.heating() : new PreconditionException("!heating()");
		assert this.getMode() != CoffeeMachineMode.THE
				: new PreconditionException("getMode() != CoffeeMachineMode.THE");

		this.currentMode = CoffeeMachineMode.THE;
		this.startHeating();

		assert this.getMode() == CoffeeMachineMode.THE
				: new PreconditionException("this.getMode() == CoffeeMachineMode.THE");

	}

	@Override
	public void setEcoMode() throws Exception {
		assert this.on() : new PreconditionException("on()");
		assert this.getMode() != CoffeeMachineMode.ECO_MODE
				: new PreconditionException("getMode() != CoffeeMachineMode.THE");

		this.currentMode = CoffeeMachineMode.ECO_MODE;
		this.startHeating();

		assert this.getMode() == CoffeeMachineMode.ECO_MODE
				: new PreconditionException("this.getMode() == CoffeeMachineMode.THE");

	}

	@Override
	public Measure<Double> getTemperature() throws Exception {
		return this.currentTemperature;
	}

	@Override
	public Measure<Double> getPowerLevel() throws Exception {
		
		return this.currentPowerLevel;
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return HIGH_POWER_IN_WATTS;
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		this.currentPowerLevel = powerLevel;
		
	}

}
