package etape1.equipements.laundry;

import etape1.bases.RegistrationCI;
import etape1.equipements.hem.HEM;
import etape1.equipements.laundry.interfaces.LaundryExternalControlCI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlJava4CI;
import etape1.equipements.laundry.interfaces.LaundryImplementationI;
import etape1.equipements.laundry.interfaces.LaundryUserCI;
import etape1.equipements.laundry.interfaces.LaundryUserI;
import etape1.equipements.laundry.ports.LaundryExternalControlJava4InboundPort;
import etape1.equipements.laundry.ports.LaundryUserInboundPort;
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

// -----------------------------------------------------------------------------
/**
 * The class <code>Laundry</code> implements a Laundry machine component.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 *
 * <p>
 * This component provides user and external control interfaces for a washing
 * machine. It supports integration with the Home Energy Management (HEM) system
 * through dynamic registration using an XML adapter descriptor.
 * </p>
 *
 * <p>
 * Created on : 2026-01-08
 * </p>
 *
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = { LaundryUserCI.class, LaundryExternalControlCI.class,
		LaundryExternalControlJava4CI.class })
@RequiredInterfaces(required = { RegistrationCI.class, ClocksServerCI.class })
public class Laundry extends AbstractComponent
		implements LaundryUserI, LaundryImplementationI, LaundryExternalControlI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = true;

	/** URI of the laundry reflection inbound port used in tests. */
	public static final String REFLECTION_INBOUND_PORT_URI = "LAUNDRY-RIP-URI";

	/** URI of the laundry port for user interactions. */
	public static final String USER_INBOUND_PORT_URI = "LAUNDRY-USER-INBOUND-PORT-URI";

	/** URI of the laundry port for external control. */
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "LAUNDRY-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	/** Path to the XML adapter descriptor for HEM registration. */
	public static final String XML_LAUNDRY_ADAPTER_DESCRIPTOR = "adapters/laundry-adapter/laundryci-descriptor.xml";

	// Measurement units
	public static final MeasurementUnit POWER_UNIT = MeasurementUnit.WATTS;
	public static final MeasurementUnit TENSION_UNIT = MeasurementUnit.VOLTS;
	public static final MeasurementUnit TEMPERATURE_UNIT = MeasurementUnit.CELSIUS;
	public static final MeasurementUnit VOLUME_UNIT = MeasurementUnit.LITERS;
	public static final MeasurementUnit RPM_UNIT = MeasurementUnit.RAW;

	// Machine power constants with Measure
	public static final Measure<Double> MAX_POWER_IN_WATTS = new Measure<Double>(Constants.MAX_POWER_CONSUMPTION,
			POWER_UNIT);
	public static final Measure<Double> MIN_POWER_IN_WATTS = new Measure<Double>(Constants.MIN_POWER_CONSUMPTION,
			POWER_UNIT);
	public static final Measure<Double> NOMINAL_POWER_IN_WATTS = new Measure<Double>(
			Constants.NOMINAL_POWER_CONSUMPTION, POWER_UNIT);

	// Wash mode power consumption with Measure (aligned with XML descriptor)
	public static final Measure<Double> DELICATE_MODE_POWER_IN_WATTS = new Measure<Double>(
			Constants.DELICATE_MODE_POWER, POWER_UNIT); // Mode 0
	public static final Measure<Double> COLOR_MODE_POWER_IN_WATTS = new Measure<Double>(Constants.COLOR_MODE_POWER,
			POWER_UNIT); // Mode 1
	public static final Measure<Double> WHITE_MODE_POWER_IN_WATTS = new Measure<Double>(Constants.WHITE_MODE_POWER,
			POWER_UNIT); // Mode 2
	public static final Measure<Double> INTENSIVE_MODE_POWER_IN_WATTS = new Measure<Double>(
			Constants.INTENSIVE_MODE_POWER, POWER_UNIT); // Mode 3

	// Voltage
	public static final Measure<Double> VOLTAGE = new Measure<Double>(Constants.MACHINE_VOLTAGE, TENSION_UNIT);

	// Capacity
	public static final Measure<Double> DRUM_CAPACITY = new Measure<Double>(Constants.DRUM_CAPACITY_KG,
			MeasurementUnit.RAW);
	public static final Measure<Double> WATER_CAPACITY = new Measure<Double>(Constants.WATER_CAPACITY_LITERS,
			VOLUME_UNIT);

	// Initial state
	public static final LaundryState INITIAL_STATE = LaundryState.OFF;
	public static final LaundryWashMode INITIAL_WASH_MODE = LaundryWashMode.COLOR;
	public static final WashTemperature INITIAL_TEMPERATURE = WashTemperature.T_40;
	public static final SpinSpeed INITIAL_SPIN_SPEED = SpinSpeed.RPM_1000;

	// Current state variables
	protected LaundryState currentState;
	protected LaundryWashMode currentWashMode;
	protected WashTemperature currentTemperature;
	protected SpinSpeed currentSpinSpeed;
	protected Measure<Double> currentPowerConsumption;
	protected Measure<Double> currentPowerLevel;
	protected Measure<Double> currentWashTemperature;
	protected boolean isSuspended;

	// BCM4Java ports
	protected LaundryUserInboundPort luip;
	protected LaundryExternalControlJava4InboundPort lecip;

	// Registration port for HEM
	protected RegistrationOutboundPort rop;

	// Component identification
	protected String uid;

	// Test mode flag
	protected boolean isIntegrationTestMode;

	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;
	
	private static final String LAUNDRY_CONNECTOR_NAME = "LaundryGeneratedConnector";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a laundry machine component.
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
	 * getState() == LaundryState.OFF
	 * }
	 * post	{@code
	 * getWashMode() == LaundryWashMode.COLOR
	 * }
	 * </pre>
	 *
	 * @param isIntegrationTestMode true if running integration tests with HEM.
	 * @throws Exception <i>to do</i>.
	 */
	protected Laundry(boolean isIntegrationTestMode) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI);
	}

	/**
	 * create a laundry machine component.
	 *
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 *
	 * <pre>
	 * pre	{@code
	 * userInboundPortURI != null && !userInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * externalControlInboundPortURI != null && !externalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * getState() == LaundryState.OFF
	 * }
	 * post	{@code
	 * getWashMode() == LaundryWashMode.COLOR
	 * }
	 * </pre>
	 *
	 * @param isIntegrationTestMode         true if running integration tests with
	 *                                      HEM.
	 * @param userInboundPortURI            URI of the user inbound port.
	 * @param externalControlInboundPortURI URI of the external control inbound
	 *                                      port.
	 * @throws Exception <i>to do</i>.
	 */
	protected Laundry(boolean isIntegrationTestMode, String userInboundPortURI, String externalControlInboundPortURI)
			throws Exception {
		this(isIntegrationTestMode, REFLECTION_INBOUND_PORT_URI, userInboundPortURI, externalControlInboundPortURI);
	}

	/**
	 * create a laundry machine component with the given reflection inbound port
	 * URI.
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
	 * userInboundPortURI != null && !userInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * externalControlInboundPortURI != null && !externalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * getState() == LaundryState.OFF
	 * }
	 * post	{@code
	 * getWashMode() == LaundryWashMode.COLOR
	 * }
	 * </pre>
	 *
	 * @param isIntegrationTestMode         true if running integration tests with
	 *                                      HEM.
	 * @param reflectionInboundPortURI      URI of the reflection inbound port of
	 *                                      the component.
	 * @param userInboundPortURI            URI of the user inbound port.
	 * @param externalControlInboundPortURI URI of the external control inbound
	 *                                      port.
	 * @throws Exception <i>to do</i>.
	 */
	protected Laundry(boolean isIntegrationTestMode, String reflectionInboundPortURI, String userInboundPortURI,
			String externalControlInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 1);
		this.initialise(isIntegrationTestMode, userInboundPortURI, externalControlInboundPortURI);
	}

	/**
	 * initialize the laundry machine component.
	 *
	 * @param isIntegrationTestMode         true if running integration tests with
	 *                                      HEM.
	 * @param userInboundPortURI            URI of the user inbound port.
	 * @param externalControlInboundPortURI URI of the external control inbound
	 *                                      port.
	 * @throws Exception <i>to do</i>.
	 */
	protected void initialise(boolean isIntegrationTestMode, String userInboundPortURI,
			String externalControlInboundPortURI) throws Exception {

		this.isIntegrationTestMode = isIntegrationTestMode;

		// Create and publish user inbound port
		this.luip = new LaundryUserInboundPort(userInboundPortURI, this);
		this.luip.publishPort();

		// Create and publish external control inbound port
		this.lecip = new LaundryExternalControlJava4InboundPort(externalControlInboundPortURI, this);
		this.lecip.publishPort();

		// Create and publish registration port if in integration test mode
		if (isIntegrationTestMode) {
			System.out.println("Laundry: Creating registration outbound port");
			this.rop = new RegistrationOutboundPort(this);
			this.rop.publishPort();
			System.out.println("Laundry: Registration outbound port published");
		}

		// Initialize state
		this.currentState = INITIAL_STATE;
		this.currentWashMode = INITIAL_WASH_MODE;
		this.currentSpinSpeed = INITIAL_SPIN_SPEED;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;
		this.currentPowerLevel = MIN_POWER_IN_WATTS;
		this.currentWashTemperature = new Measure<Double>(Constants.TEMP_40, TEMPERATURE_UNIT);
		this.isSuspended = false;
		this.uid = LAUNDRY_CONNECTOR_NAME;

		// Setup tracing
		if (VERBOSE) {
			this.tracer.get().setTitle("Laundry Machine component");
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
			// Connect to HEM for registration only in integration test mode
			if (isIntegrationTestMode) {
				System.out.println("Laundry: Connecting to HEM for registration");
				this.doPortConnection(this.rop.getPortURI(), HEM.REGISTRATION_LAUNDRY_INBOUND_PORT_URI,
						RegistrationConnector.class.getCanonicalName());
				System.out.println("Laundry: Connected to HEM for registration");
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
			this.luip.unpublishPort();
			this.lecip.unpublishPort();
			if (isIntegrationTestMode) {
				this.rop.unpublishPort();
			}
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Methods from LaundryImplementationI
	// -------------------------------------------------------------------------

	@Override
	public boolean isRunning() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: checking if running: "
					+ (this.currentState == LaundryState.WASHING || this.currentState == LaundryState.RINSING
							|| this.currentState == LaundryState.SPINNING || this.currentState == LaundryState.DRYING)
					+ "\n");
		}
		return this.currentState == LaundryState.WASHING || this.currentState == LaundryState.RINSING
				|| this.currentState == LaundryState.SPINNING || this.currentState == LaundryState.DRYING;
	}

	@Override
	public LaundryState getState() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: returning current state: " + this.currentState + "\n");
		}
		return this.currentState;
	}

	@Override
	public LaundryWashMode getWashMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: returning current wash mode: " + this.currentWashMode + "\n");
		}
		return this.currentWashMode;
	}

	@Override
	public Measure<Double> getWashTemperature() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: returning current temperature: " + this.currentTemperature + "\n");
		}
		return this.currentWashTemperature;
	}

	@Override
	public SpinSpeed getSpinSpeed() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: returning current spin speed: " + this.currentSpinSpeed + "\n");
		}
		return this.currentSpinSpeed;
	}

	// -------------------------------------------------------------------------
	// Methods from LaundryUserI
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: turning on\n");
		}

		assert this.currentState == LaundryState.OFF : new PreconditionException("currentState == LaundryState.OFF");

		// Register to HEM if in integration test mode
		if (isIntegrationTestMode) {
			this.traceMessage("Laundry: Registering to HEM");
			this.rop.register(uid, lecip.getPortURI(), XML_LAUNDRY_ADAPTER_DESCRIPTOR);
			this.traceMessage("Laundry: Registered to HEM!");
		}

		this.currentState = LaundryState.ON;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;

		assert this.currentState == LaundryState.ON : new PostconditionException("currentState == LaundryState.ON");
	}

	@Override
	public void turnOff() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: turning off\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isRunning() : new PreconditionException("!isRunning()");

		// Unregister from HEM if in integration test mode
		if (isIntegrationTestMode) {
			this.rop.unregister(uid);
			this.doPortDisconnection(this.rop.getPortURI());
		}

		this.currentState = LaundryState.OFF;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;
		this.isSuspended = false;

		assert this.currentState == LaundryState.OFF : new PostconditionException("currentState == LaundryState.OFF");
	}

	@Override
	public void startWash() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: starting wash cycle\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isSuspended : new PreconditionException("!isSuspended");

		this.currentState = LaundryState.WASHING;

		// Set power consumption based on wash mode
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

		if (VERBOSE) {
			this.traceMessage("Laundry: washing with mode " + this.currentWashMode + " at "
					+ this.currentPowerConsumption.getData() + "W\n");
		}

		assert this.currentState == LaundryState.WASHING
				: new PostconditionException("currentState == LaundryState.WASHING");
	}

	@Override
	public void cancelWash() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: cancelling wash cycle\n");
		}

		assert this.isRunning() : new PreconditionException("isRunning()");

		this.currentState = LaundryState.ON;
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;

		assert this.currentState == LaundryState.ON : new PostconditionException("currentState == LaundryState.ON");
	}

	@Override
	public void setWhiteMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: setting WHITE mode\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isRunning() : new PreconditionException("!isRunning()");

		this.currentWashMode = LaundryWashMode.WHITE;

		assert this.currentWashMode == LaundryWashMode.WHITE
				: new PostconditionException("currentWashMode == LaundryWashMode.WHITE");
	}

	@Override
	public void setColorMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: setting COLOR mode\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isRunning() : new PreconditionException("!isRunning()");

		this.currentWashMode = LaundryWashMode.COLOR;

		assert this.currentWashMode == LaundryWashMode.COLOR
				: new PostconditionException("currentWashMode == LaundryWashMode.COLOR");
	}

	@Override
	public void setDelicateMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: setting DELICATE mode\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isRunning() : new PreconditionException("!isRunning()");

		this.currentWashMode = LaundryWashMode.DELICATE;

		assert this.currentWashMode == LaundryWashMode.DELICATE
				: new PostconditionException("currentWashMode == LaundryWashMode.DELICATE");
	}

	@Override
	public void setIntensiveMode() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: setting INTENSIVE mode\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isRunning() : new PreconditionException("!isRunning()");

		this.currentWashMode = LaundryWashMode.INTENSIVE;

		assert this.currentWashMode == LaundryWashMode.INTENSIVE
				: new PostconditionException("currentWashMode == LaundryWashMode.INTENSIVE");
	}

	@Override
	public void setWashTemperature(Measure<Double> temp) throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: setting wash temperature to " + temp + "\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isRunning() : new PreconditionException("!isRunning()");

		this.currentWashTemperature = new Measure<Double>(temp.getData(), TEMPERATURE_UNIT);

		assert this.currentWashTemperature.getData() == temp.getData() : new PostconditionException("currentTemperature == temp");
	}

	@Override
	public void setSpinSpeed(SpinSpeed speed) throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: setting spin speed to " + speed + "\n");
		}

		assert this.currentState == LaundryState.ON : new PreconditionException("currentState == LaundryState.ON");
		assert !this.isRunning() : new PreconditionException("!isRunning()");

		this.currentSpinSpeed = speed;

		assert this.currentSpinSpeed == speed : new PostconditionException("currentSpinSpeed == speed");
	}

	// -------------------------------------------------------------------------
	// Methods from LaundryExternalControlI
	// -------------------------------------------------------------------------

	@Override
	public void suspend() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: suspending\n");
		}

		assert this.currentState == LaundryState.ON || this.isRunning()
				: new PreconditionException("currentState == LaundryState.ON || isRunning()");
		assert !this.isSuspended : new PreconditionException("!isSuspended");

		this.isSuspended = true;
		// Keep current state but reduce power consumption
		double savedPower = this.currentPowerConsumption.getData();
		this.currentPowerConsumption = MIN_POWER_IN_WATTS;

		if (VERBOSE) {
			this.traceMessage("Laundry: suspended (was consuming " + savedPower + "W)\n");
		}

		assert this.isSuspended : new PostconditionException("isSuspended");
	}

	@Override
	public void resume() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: resuming\n");
		}

		assert this.isSuspended : new PreconditionException("isSuspended");

		this.isSuspended = false;

		// Restore power consumption based on current state and mode
		if (this.isRunning()) {
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

		if (VERBOSE) {
			this.traceMessage("Laundry: resumed \n");
		}

		assert !this.isSuspended : new PostconditionException("!isSuspended");
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: returning max power level " + MAX_POWER_IN_WATTS.getData() + "W\n");
		}
		return MAX_POWER_IN_WATTS;
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry: returning current power level: " + this.currentPowerLevel.getData() + "W\n");
		}
		return this.currentPowerLevel;
	}

	@Override
	public Measure<Double> getCurrentWashTemperature() throws Exception {
		return this.currentWashTemperature;
	}

	@Override
	public void setMode(int mode) throws Exception {
		assert mode >= 1 && mode <= 4 : new PreconditionException("mode >= 1 && mode <= 4");

		switch (mode) {
		case 1:
			this.setDelicateMode();
			break;
		case 2:
			this.setColorMode();
			break;
		case 3:
			this.setWhiteMode();
			break;
		case 4:
			this.setIntensiveMode();
			break;
		}

	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		
		if (powerLevel.getData() < MAX_POWER_IN_WATTS.getData()) {
			this.currentPowerLevel = new Measure<Double>(powerLevel.getData(), POWER_UNIT);
		}
		
	}

}
// -----------------------------------------------------------------------------
