package etape1.equipements.kettle;

import etape1.equipements.kettle.connections.ports.KettleExternalControlJava4InboundPort;
import etape1.equipements.kettle.connections.ports.KettleUserInboundPort;
import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import etape1.equipements.kettle.interfaces.KettleExternalControlI;
import etape1.equipements.kettle.interfaces.KettleExternalControlJava4CI;
import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleUserCI;
import etape1.equipements.kettle.interfaces.KettleUserI;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>Kettle</code> implements a Kettle component.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * <strong>Implementation Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * currentState != null
 * }
 * invariant	{@code
 * targetTemperature != null && targetTemperature.getMeasurementUnit().equals(TEMPERATURE_UNIT)
 * }
 * invariant	{@code
 * targetTemperature.getData() >= MIN_TARGET_TEMPERATURE.getData()
 * 		&& targetTemperature.getData() <= MAX_TARGET_TEMPERATURE.getData()
 * }
 * invariant	{@code
 * currentPowerLevel == null || currentPowerLevel.getMeasure().getMeasurementUnit().equals(POWER_UNIT)
 * }
 * invariant	{@code
 * currentPowerLevel == null || currentPowerLevel.getMeasure().getData() >= 0.0
 * 		&& currentPowerLevel.getMeasure().getData() <= MAX_POWER_LEVEL.getData()
 * }
 * </pre>
 * 
 * <p>
 * <strong>Invariants</strong>
 * </p>
 * 
 * <pre>
 * invariant	{@code
 * REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()
 * }
 * invariant	{@code
 * USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()
 * }
 * invariant	{@code
 * INTERNAL_CONTROL_INBOUND_PORT_URI != null && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()
 * }
 * invariant	{@code
 * EXTERNAL_CONTROL_INBOUND_PORT_URI != null && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()
 * }
 * invariant	{@code
 * X_RELATIVE_POSITION >= 0
 * }
 * invariant	{@code
 * Y_RELATIVE_POSITION >= 0
 * }
 * </pre>
 * 
 * <p>
 * Created on : 2023-09-18
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = { KettleUserCI.class, KettleExternalControlCI.class,
		KettleExternalControlJava4CI.class })
//@RequiredInterfaces(required = { KettleUserCI.class, KettleExternalControlCI.class, KettleExternalControlJava4CI.class })
public class Kettle extends AbstractComponent
		implements KettleImplementationI, KettleUserI, KettleExternalControlI {
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>KettleState</code> describes the operation states of
	 * the Kettle.
	 *
	 * <p>
	 * <strong>Description</strong>
	 * </p>
	 * 
	 * <p>
	 * Created on : 2021-09-10
	 * </p>
	 * 
	 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// BCM4Java information

	/** URI of the Kettle inbound port used in tests. */
	public static final String REFLECTION_INBOUND_PORT_URI = "Kettle-RIP-URI";

	/** URI of the Kettle port for user interactions. */
	public static final String USER_INBOUND_PORT_URI = "Kettle-USER-INBOUND-PORT-URI";
	/** URI of the Kettle port for internal control. */
	// public static final String INTERNAL_CONTROL_INBOUND_PORT_URI =
	// "Kettle-INTERNAL-CONTROL-INBOUND-PORT-URI";
	/** URI of the Kettle port for internal control. */
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "Kettle-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	/** inbound port offering the <code>KettleUserCI</code> interface. */
	protected KettleUserInboundPort hip;
	/**
	 * inbound port offering the <code>KettleInternalControlCI</code> interface.
	 */
	// protected KettleInternalControlInboundPort hicip;
	/**
	 * inbound port offering the <code>KettleExternalControlCI</code> interface.
	 */
	protected KettleExternalControlJava4InboundPort hecip;
	

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;

	// Appliance information

	/** standard target temperature for the Kettle in celsius. */
	protected static final Measure<Double> STANDARD_TARGET_TEMPERATURE = new Measure<>(19.0, TEMPERATURE_UNIT);
	/** fake current temperature, used when testing without simulation. */
	public static final SignalData<Double> FAKE_CURRENT_TEMPERATURE = new SignalData<>(
			new Measure<>(10.0, TEMPERATURE_UNIT));

	protected KettleState INITIAL_STATE = KettleState.ON;
	protected KettleMode INITIAL_MODE = KettleMode.TOTAL;

	protected Measure<Double> INITIAL_WASH_TEMPERATURE = new Measure<Double>(30.0, TEMPERATURE_UNIT);;

	/** current state (on, off) of the Kettle. */
	protected KettleState currentState;

	protected KettleMode currentKettleMode;

	protected Measure<Double> Temperature;
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * h != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param h instance to be tested.
	 * @return true if the implementation invariants are observed, false otherwise.
	 */
	/*
	 * protected static boolean implementationInvariants(Kettle h) {
	 * 
	 * assert h != null : new PreconditionException("h != null");
	 * 
	 * boolean ret = true; ret &=
	 * AssertionChecking.checkImplementationInvariant(h.currentState != null,
	 * Kettle.class, h, "h.currentState != null"); ret &=
	 * AssertionChecking.checkImplementationInvariant( h.targetTemperature.getData()
	 * >= MIN_TARGET_TEMPERATURE.getData() && h.targetTemperature.getData() <=
	 * MAX_TARGET_TEMPERATURE.getData(), Kettle.class, h,
	 * "targetTemperature.getData() >= MIN_TARGET_TEMPERATURE.getData() && " +
	 * "targetTemperature.getData() <= MIN_TARGET_TEMPERATURE.getData()"); ret &=
	 * AssertionChecking.checkImplementationInvariant(
	 * h.currentPowerLevel.getMeasure().getData() >= 0.0 &&
	 * h.currentPowerLevel.getMeasure().getData() <= MAX_POWER_LEVEL.getData(),
	 * Kettle.class, h, "currentPowerLevel.getMeasure().getData() >= 0.0 && " +
	 * "currentPowerLevel.getMeasure().getData() <= " +
	 * "MAX_POWER_LEVEL.getData()"); return ret; }
	 */

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * h != null
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param h instance to be tested.
	 * @return true if the invariants are observed, false otherwise.
	 */
	/*
	 * protected static boolean invariants(Kettle h) { assert h != null : new
	 * PreconditionException("h != null");
	 * 
	 * boolean ret = true; ret &= KettleTemperatureI.invariants(h); ret &=
	 * KettleExternalControlI.invariants(h); ret &=
	 * AssertionChecking.checkInvariant( REFLECTION_INBOUND_PORT_URI != null &&
	 * !REFLECTION_INBOUND_PORT_URI.isEmpty(), Kettle.class, h,
	 * "REFLECTION_INBOUND_PORT_URI != null && " +
	 * "!REFLECTION_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant(USER_INBOUND_PORT_URI != null &&
	 * !USER_INBOUND_PORT_URI.isEmpty(), Kettle.class, h,
	 * "USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant( INTERNAL_CONTROL_INBOUND_PORT_URI != null
	 * && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(), Kettle.class, h,
	 * "INTERNAL_CONTROL_INBOUND_PORT_URI != null && " +
	 * "!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant( EXTERNAL_CONTROL_INBOUND_PORT_URI != null
	 * && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(), Kettle.class, h,
	 * "EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&" +
	 * "!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant(X_RELATIVE_POSITION >= 0, Kettle.class, h,
	 * "X_RELATIVE_POSITION >= 0"); ret &=
	 * AssertionChecking.checkInvariant(Y_RELATIVE_POSITION >= 0, Kettle.class, h,
	 * "Y_RELATIVE_POSITION >= 0"); return ret; }
	 */

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new Kettle.
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
	 * true
	 * }	// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	/*
	 * protected Kettle() throws Exception { this(USER_INBOUND_PORT_URI,
	 * INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI); }
	 */

	protected Kettle() throws Exception {
		this(USER_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI);
	}

	/**
	 * create a new Kettle.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleInternalControlInboundPortURI != null && !KettleInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 * 
	 * @param KettleUserInboundPortURI            URI of the inbound port to call
	 *                                             the Kettle component for user
	 *                                             interactions.
	 * @param KettleInternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Kettle component for
	 *                                             internal control.
	 * @param KettleExternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Kettle component for
	 *                                             external control.
	 * @throws Exception <i>to do</i>.
	 */
	/*
	 * protected Kettle(String KettleUserInboundPortURI, String
	 * KettleInternalControlInboundPortURI, String
	 * KettleExternalControlInboundPortURI) throws Exception { super(1, 0);
	 * this.initialise(KettleUserInboundPortURI,
	 * KettleInternalControlInboundPortURI, KettleExternalControlInboundPortURI);
	 * }
	 */

	protected Kettle(String KettleUserInboundPortURI, String KettleExternalControlInboundPortURI) throws Exception {
		super(1, 0);
		this.initialise(KettleUserInboundPortURI, KettleExternalControlInboundPortURI);
	}

	/**
	 * create a new Kettle.
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
	 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleInternalControlInboundPortURI != null && !KettleInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI             URI of the reflection inbound
	 *                                             port of the component.
	 * @param KettleUserInboundPortURI            URI of the inbound port to call
	 *                                             the Kettle component for user
	 *                                             interactions.
	 * @param KettleInternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Kettle component for
	 *                                             internal control.
	 * @param KettleExternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Kettle component for
	 *                                             external control.
	 * @throws Exception <i>to do</i>.
	 */
	protected Kettle(String reflectionInboundPortURI, String KettleUserInboundPortURI,
			String KettleInternalControlInboundPortURI, String KettleExternalControlInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 0);

		this.initialise(KettleUserInboundPortURI, 
				KettleExternalControlInboundPortURI);
	}

	/**
	 * create a new thermostated Kettle.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleInternalControlInboundPortURI != null && !KettleInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param KettleUserInboundPortURI            URI of the inbound port to call
	 *                                             the Kettle component for user
	 *                                             interactions.
	 * @param KettleInternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Kettle component for
	 *                                             internal control.
	 * @param KettleExternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Kettle component for
	 *                                             external control.
	 * @throws Exception <i>to do</i>.
	 */

	/*
	 * protected void initialise(String KettleUserInboundPortURI, String
	 * KettleInternalControlInboundPortURI, String
	 * KettleExternalControlInboundPortURI) throws Exception { assert
	 * KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty();
	 * assert KettleInternalControlInboundPortURI != null &&
	 * !KettleInternalControlInboundPortURI.isEmpty(); assert
	 * KettleExternalControlInboundPortURI != null &&
	 * !KettleExternalControlInboundPortURI.isEmpty();
	 * 
	 * this.currentState = KettleState.OFF; // this.currentPowerLevel = new
	 * SignalData<>(MAX_POWER_LEVEL); // this.targetTemperature =
	 * STANDARD_TARGET_TEMPERATURE;
	 * 
	 * // this.hip = new KettleUserJava4InboundPort(KettleUserInboundPortURI,
	 * this); // this.hip.publishPort(); // this.hicip = new //
	 * KettleInternalControlInboundPort(KettleInternalControlInboundPortURI, //
	 * this); // this.hicip.publishPort(); this.hecip = new
	 * KettleExternalControlJava4InboundPort(KettleExternalControlInboundPortURI,
	 * this); this.hecip.publishPort();
	 * 
	 * if (VERBOSE) { this.tracer.get().setTitle("Kettle component");
	 * this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
	 * Y_RELATIVE_POSITION); this.toggleTracing(); }
	 * 
	 * /* assert Kettle.implementationInvariants(this) : new
	 * ImplementationInvariantException("Kettle.implementationInvariants(this)");
	 * assert Kettle.invariants(this) : new
	 * InvariantException("Kettle.invariants(this)");
	 */
	//}

	

	protected void initialise(String KettleUserInboundPortURI, 
			String KettleExternalControlInboundPortURI) throws Exception {
		assert KettleUserInboundPortURI != null && !KettleUserInboundPortURI.isEmpty();
		assert KettleExternalControlInboundPortURI != null && !KettleExternalControlInboundPortURI.isEmpty();

		this.currentState = KettleState.OFF;
		this.currentKettleMode = KettleMode.TOTAL;
		// this.currentPowerLevel = new SignalData<>(MAX_POWER_LEVEL);
		// this.targetTemperature = STANDARD_TARGET_TEMPERATURE;

		// this.hip = new KettleUserJava4InboundPort(KettleUserInboundPortURI, this);
		// this.hip.publishPort();
		// this.hicip = new
		// KettleInternalControlInboundPort(KettleInternalControlInboundPortURI,
		// this);
		// this.hicip.publishPort();
		this.hip = new KettleUserInboundPort(KettleUserInboundPortURI, this);
		this.hip.publishPort();
		this.hecip = new KettleExternalControlJava4InboundPort(KettleExternalControlInboundPortURI, this);
		this.hecip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Kettle component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		/*
		 * assert Kettle.implementationInvariants(this) : new
		 * ImplementationInvariantException("Kettle.implementationInvariants(this)");
		 * assert Kettle.invariants(this) : new
		 * InvariantException("Kettle.invariants(this)");
		 */
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			 this.hip.unpublishPort();
			//this.hicip.unpublishPort();
			this.hecip.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	@Override
	public KettleState getState() throws Exception {

		return this.currentState;
	}

	@Override
	public KettleMode getKettleMode() throws Exception {

		assert this.getState() == KettleState.ON : new PreconditionException("getState() == KettleState.ON");

		return currentKettleMode;
	}

	@Override
	public void turnOn() throws Exception {

		assert this.getState() == KettleState.OFF : new PreconditionException("getState() == KettleState.OFF");

		this.currentState = KettleState.ON;

	}

	@Override
	public void turnOff() throws Exception {

		assert this.getState() == KettleState.ON : new PreconditionException("getState() == KettleState.ON");

		this.currentState = KettleState.OFF;
	}

	@Override
	public void setTotalMode() throws Exception {

		assert this.getKettleMode() == KettleMode.TOTAL
				: new PreconditionException("getKettleMode() == KettleMode.DRY");

		this.currentKettleMode = KettleMode.TOTAL;

	}

	@Override
	public void setPartialMode() throws Exception {

		assert this.getKettleMode() == KettleMode.PARTIAL
				: new PreconditionException("getKettleMode() == KettleMode.WASH");

		this.currentKettleMode = KettleMode.PARTIAL;
	}

	@Override
	public void setTemperature() throws Exception {

		if (this.getKettleMode() == KettleMode.TOTAL) {
			this.Temperature = new Measure<Double>(100.0, TEMPERATURE_UNIT);
		}

		if (this.getKettleMode() == KettleMode.PARTIAL) {
			this.Temperature = new Measure<Double>(60.0, TEMPERATURE_UNIT);
			}
		}

	}

// -----------------------------------------------------------------------------
