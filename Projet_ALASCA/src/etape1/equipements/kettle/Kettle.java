package etape1.equipements.kettle;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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

import etape1.bases.RegistrationCI;
import etape1.equipements.hem.HEM;
import etape1.equipements.kettle.connections.ports.KettleExternalControlJava4InboundPort;
import etape1.equipements.kettle.connections.ports.KettleUserInboundPort;
import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import etape1.equipements.kettle.interfaces.KettleExternalControlI;
import etape1.equipements.kettle.interfaces.KettleExternalControlJava4CI;
import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleUserCI;
import etape1.equipements.kettle.interfaces.KettleUserI;
import etape1.equipements.registration.connector.RegistrationConnector;
import etape1.equipements.registration.ports.RegistrationOutboundPort;
import fr.sorbonne_u.alasca.physical_data.Measure;
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
 * The class <code>Kettle</code> implements the water heater (chauffe-eau)
 * component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The water heater is a "non-programmable" appliance that heats water in a
 * tank (200L) to a target temperature (30-80 degrees Celsius). It can be
 * controlled remotely by the HEM to modulate its maximum power consumption
 * through different operating modes (SUSPEND, ECO, NORMAL, MAX).
 * </p>
 *
 * <p>Created on : 2023-09-18</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = { KettleUserCI.class, KettleExternalControlCI.class,
		KettleExternalControlJava4CI.class })
@RequiredInterfaces(required = { RegistrationCI.class, ClocksServerCI.class })
public class Kettle extends AbstractComponent
		implements KettleImplementationI, KettleUserI, KettleExternalControlI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the Kettle reflection inbound port used in tests. */
	public static final String REFLECTION_INBOUND_PORT_URI = "Kettle-RIP-URI";

	/** URI of the Kettle port for user interactions. */
	public static final String USER_INBOUND_PORT_URI = "Kettle-USER-INBOUND-PORT-URI";

	/** URI of the Kettle port for external control. */
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "Kettle-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	/** XML adapter descriptor path for connector generation. */
	public static final String XML_KETTLE_ADAPTER_DESCRIPTOR = "adapters/kettle-adapter/kettleci-descriptor.xml";

	/** Connector name for HEM registration. */
	public static final String KETTLE_CONNECTOR_NAME = "KettleGeneratedConnector";

	/** Fake current temperature used when testing without simulation. */
	public static final double FAKE_CURRENT_TEMPERATURE = 40.0;

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;

	// Component state

	/** current state of the water heater. */
	protected KettleState currentState;
	/** current power mode of the water heater. */
	protected KettleMode currentMode;
	/** target temperature for the water heater. */
	protected Measure<Double> targetTemperature;
	/** current power level. */
	protected Measure<Double> currentPowerLevel;

	/** true when the water heater is suspended by HEM. */
	protected boolean suspended;
	/** mode saved before suspension, restored on resume. */
	protected KettleMode modeBeforeSuspend;

	// Ports

	/** inbound port offering the KettleUserCI interface. */
	protected KettleUserInboundPort hip;
	/** inbound port offering the KettleExternalControlJava4CI interface. */
	protected KettleExternalControlJava4InboundPort hecip;
	/** outbound port for registering to the HEM. */
	protected RegistrationOutboundPort rop;

	/** uid for HEM registration. */
	protected String uid;
	/** true when running in integration test mode (with HEM). */
	protected boolean isIntegrationTestMode;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected Kettle(boolean isIntegrationTestMode) throws Exception {
		this(isIntegrationTestMode, USER_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI);
	}

	protected Kettle(boolean isIntegrationTestMode,
			String kettleUserInboundPortURI,
			String kettleExternalControlInboundPortURI) throws Exception {
		super(REFLECTION_INBOUND_PORT_URI, 1, 1);
		this.initialise(isIntegrationTestMode, kettleUserInboundPortURI,
				kettleExternalControlInboundPortURI);
	}

	protected void initialise(boolean isIntegrationTestMode,
			String kettleUserInboundPortURI,
			String kettleExternalControlInboundPortURI) throws Exception {

		assert kettleUserInboundPortURI != null && !kettleUserInboundPortURI.isEmpty();
		assert kettleExternalControlInboundPortURI != null && !kettleExternalControlInboundPortURI.isEmpty();

		this.isIntegrationTestMode = isIntegrationTestMode;

		this.currentState = KettleState.OFF;
		this.currentMode = KettleMode.NORMAL;
		this.targetTemperature = new Measure<Double>(DEFAULT_TARGET_TEMPERATURE, TEMPERATURE_UNIT);
		this.currentPowerLevel = new Measure<Double>(0.0, POWER_UNIT);
		this.suspended = false;
		this.modeBeforeSuspend = null;
		this.uid = KETTLE_CONNECTOR_NAME;

		this.hip = new KettleUserInboundPort(kettleUserInboundPortURI, this);
		this.hip.publishPort();

		this.hecip = new KettleExternalControlJava4InboundPort(kettleExternalControlInboundPortURI, this);
		this.hecip.publishPort();

		if (isIntegrationTestMode) {
			this.rop = new RegistrationOutboundPort(this);
			this.rop.publishPort();
		}

		if (VERBOSE) {
			this.tracer.get().setTitle("Kettle (Water Heater) component");
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
			if (isIntegrationTestMode) {
				this.doPortConnection(this.rop.getPortURI(),
						HEM.REGISTRATION_KETTLE_INBOUND_PORT_URI,
						RegistrationConnector.class.getCanonicalName());
			}
		} catch (Throwable e) {
			throw new ComponentStartException(e);
		}
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.hip.unpublishPort();
			this.hecip.unpublishPort();
			if (isIntegrationTestMode) {
				this.rop.unpublishPort();
			}
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------------------

	protected double getPowerForMode(KettleMode mode) {
		switch (mode) {
		case SUSPEND:	return SUSPEND_MODE_POWER;
		case ECO:		return ECO_MODE_POWER;
		case NORMAL:	return NORMAL_MODE_POWER;
		case MAX:		return MAX_MODE_POWER;
		default:		return 0.0;
		}
	}

	// -------------------------------------------------------------------------
	// KettleImplementationI
	// -------------------------------------------------------------------------

	@Override
	public KettleState getState() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle returns its state: " + this.currentState + ".\n");
		}
		return this.currentState;
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle returns its mode: " + this.currentMode + ".\n");
		}
		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		return this.currentMode;
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle returns target temperature: "
					+ this.targetTemperature.getData() + " C.\n");
		}
		return this.targetTemperature;
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle returns current temperature: "
					+ FAKE_CURRENT_TEMPERATURE + " C.\n");
		}
		return new Measure<Double>(FAKE_CURRENT_TEMPERATURE, TEMPERATURE_UNIT);
	}

	@Override
	public boolean isHeating() throws Exception {
		return this.currentState == KettleState.HEATING;
	}

	// -------------------------------------------------------------------------
	// KettleExternalControlI
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle switches on.\n");
		}

		assert this.getState() == KettleState.OFF
				: new PreconditionException("getState() == KettleState.OFF");

		if (isIntegrationTestMode) {
			this.traceMessage("Kettle registering to HEM.\n");
			this.rop.register(uid, hecip.getPortURI(), XML_KETTLE_ADAPTER_DESCRIPTOR);
			this.traceMessage("Kettle registered to HEM.\n");
		}

		this.currentState = KettleState.ON;
		this.currentMode = KettleMode.NORMAL;
		this.currentPowerLevel = new Measure<Double>(NORMAL_MODE_POWER, POWER_UNIT);
		this.suspended = false;

		assert this.getState() == KettleState.ON
				: new PostconditionException("getState() == KettleState.ON");
	}

	@Override
	public void turnOff() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle switches off.\n");
		}

		assert this.getState() == KettleState.ON
				: new PreconditionException("getState() == KettleState.ON");

		if (isIntegrationTestMode) {
			this.rop.unregister(uid);
		}

		this.currentState = KettleState.OFF;
		this.currentPowerLevel = new Measure<Double>(0.0, POWER_UNIT);
		this.suspended = false;
		this.modeBeforeSuspend = null;

		assert this.getState() == KettleState.OFF
				: new PostconditionException("getState() == KettleState.OFF");
	}

	@Override
	public void suspend() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle is suspended by HEM.\n");
		}

		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		assert !this.isSuspended()
				: new PreconditionException("!isSuspended()");

		this.modeBeforeSuspend = this.currentMode;
		this.currentMode = KettleMode.SUSPEND;
		this.currentPowerLevel = new Measure<Double>(SUSPEND_MODE_POWER, POWER_UNIT);
		this.suspended = true;

		if (this.currentState == KettleState.HEATING) {
			this.currentState = KettleState.ON;
		}

		assert this.isSuspended()
				: new PostconditionException("isSuspended()");
	}

	@Override
	public void resume() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle is resumed by HEM.\n");
		}

		assert this.isSuspended()
				: new PreconditionException("isSuspended()");

		this.suspended = false;
		if (this.modeBeforeSuspend != null) {
			this.currentMode = this.modeBeforeSuspend;
			this.currentPowerLevel = new Measure<Double>(
					getPowerForMode(this.currentMode), POWER_UNIT);
			this.modeBeforeSuspend = null;
		} else {
			this.currentMode = KettleMode.NORMAL;
			this.currentPowerLevel = new Measure<Double>(NORMAL_MODE_POWER, POWER_UNIT);
		}

		assert !this.isSuspended()
				: new PostconditionException("!isSuspended()");
	}

	@Override
	public boolean isSuspended() throws Exception {
		return this.suspended;
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle returns max power level: " + MAX_POWER_LEVEL + " W.\n");
		}
		return new Measure<Double>(MAX_POWER_LEVEL, POWER_UNIT);
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle returns current power level: "
					+ this.currentPowerLevel.getData() + " W.\n");
		}
		return this.currentPowerLevel;
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle sets power level to: "
					+ powerLevel.getData() + " W.\n");
		}

		assert powerLevel != null && powerLevel.getData() >= 0.0
				: new PreconditionException("powerLevel != null && powerLevel.getData() >= 0.0");
		assert powerLevel.getData() <= MAX_POWER_LEVEL
				: new PreconditionException("powerLevel.getData() <= MAX_POWER_LEVEL");

		this.currentPowerLevel = powerLevel;
	}

	// -------------------------------------------------------------------------
	// KettleUserI
	// -------------------------------------------------------------------------

	@Override
	public void startHeating() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle starts heating.\n");
		}

		assert this.getState() == KettleState.ON
				: new PreconditionException("getState() == KettleState.ON");
		assert this.getKettleMode() != KettleMode.SUSPEND
				: new PreconditionException("getKettleMode() != KettleMode.SUSPEND");

		this.currentState = KettleState.HEATING;

		assert this.getState() == KettleState.HEATING
				: new PostconditionException("getState() == KettleState.HEATING");
	}

	@Override
	public void stopHeating() throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle stops heating.\n");
		}

		assert this.getState() == KettleState.HEATING
				: new PreconditionException("getState() == KettleState.HEATING");

		this.currentState = KettleState.ON;

		assert this.getState() == KettleState.ON
				: new PostconditionException("getState() == KettleState.ON");
	}

	@Override
	public void setTargetTemperature(Measure<Double> temperature) throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle sets target temperature to: "
					+ temperature.getData() + " C.\n");
		}

		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		assert temperature != null
				: new PreconditionException("temperature != null");
		assert temperature.getData() >= MIN_TARGET_TEMPERATURE
				: new PreconditionException("temperature.getData() >= MIN_TARGET_TEMPERATURE");
		assert temperature.getData() <= MAX_TARGET_TEMPERATURE
				: new PreconditionException("temperature.getData() <= MAX_TARGET_TEMPERATURE");

		this.targetTemperature = temperature;

		assert this.getTargetTemperature().getData() == temperature.getData()
				: new PostconditionException("getTargetTemperature().getData() == temperature.getData()");
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		if (Kettle.VERBOSE) {
			this.traceMessage("Kettle sets mode to: " + mode + ".\n");
		}

		assert this.getState() != KettleState.OFF
				: new PreconditionException("getState() != KettleState.OFF");
		assert mode != null
				: new PreconditionException("mode != null");

		this.currentMode = mode;
		this.currentPowerLevel = new Measure<Double>(getPowerForMode(mode), POWER_UNIT);

		assert this.getKettleMode() == mode
				: new PostconditionException("getKettleMode() == mode");
	}

	// -------------------------------------------------------------------------
	// Java4 methods (for Javassist connector generation)
	// -------------------------------------------------------------------------

	public int getStateJava4() throws Exception {
		KettleState state = this.getState();
		switch (state) {
		case OFF:		return 0;
		case ON:		return 1;
		case HEATING:	return 2;
		default:		return 0;
		}
	}

	public int getKettleModeJava4() throws Exception {
		KettleMode mode = this.getKettleMode();
		switch (mode) {
		case SUSPEND:	return 0;
		case ECO:		return 1;
		case NORMAL:	return 2;
		case MAX:		return 3;
		default:		return 0;
		}
	}

	public double getTargetTemperatureJava4() throws Exception {
		return this.getTargetTemperature().getData();
	}

	public double getCurrentTemperatureJava4() throws Exception {
		return this.getCurrentTemperature().getData();
	}

	public double getMaxPowerLevelJava4() throws Exception {
		return this.getMaxPowerLevel().getData();
	}

	public double getCurrentPowerLevelJava4() throws Exception {
		return this.getCurrentPowerLevel().getData();
	}

	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception {
		this.setCurrentPowerLevel(new Measure<Double>(powerLevel, POWER_UNIT));
	}

	public void setModeJava4(int mode) throws Exception {
		switch (mode) {
		case 1: this.setMode(KettleMode.ECO); break;
		case 2: this.setMode(KettleMode.NORMAL); break;
		case 3: this.setMode(KettleMode.MAX); break;
		default: break;
		}
	}
}

// -----------------------------------------------------------------------------
