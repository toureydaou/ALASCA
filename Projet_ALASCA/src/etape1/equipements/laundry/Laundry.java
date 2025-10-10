package etape1.equipements.laundry;

import etape1.equipements.laundry.connections.ports.LaundryExternalControlJava4InboundPort;
import etape1.equipements.laundry.connections.ports.LaundryUserInboundPort;
import etape1.equipements.laundry.interfaces.LaundryExternalControlCI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlJava4CI;
import etape1.equipements.laundry.interfaces.LaundryImplementationI;
import etape1.equipements.laundry.interfaces.LaundryUserCI;
import etape1.equipements.laundry.interfaces.LaundryUserI;

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
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;

import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;
import physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>Laundry</code> implements a Laundry component.
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
@OfferedInterfaces(offered = { LaundryUserCI.class, LaundryExternalControlCI.class,
		LaundryExternalControlJava4CI.class })
//@RequiredInterfaces(required = { LaundryUserCI.class, LaundryExternalControlCI.class, LaundryExternalControlJava4CI.class })
public class Laundry extends AbstractComponent
		implements LaundryImplementationI, LaundryUserI, LaundryExternalControlI {
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>LaundryState</code> describes the operation states of
	 * the Laundry.
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

	/** URI of the Laundry inbound port used in tests. */
	public static final String REFLECTION_INBOUND_PORT_URI = "Laundry-RIP-URI";

	/** URI of the Laundry port for user interactions. */
	public static final String USER_INBOUND_PORT_URI = "Laundry-USER-INBOUND-PORT-URI";
	/** URI of the Laundry port for internal control. */
	// public static final String INTERNAL_CONTROL_INBOUND_PORT_URI =
	// "Laundry-INTERNAL-CONTROL-INBOUND-PORT-URI";
	/** URI of the Laundry port for internal control. */
	public static final String EXTERNAL_CONTROL_INBOUND_PORT_URI = "Laundry-EXTERNAL-CONTROL-INBOUND-PORT-URI";

	/** inbound port offering the <code>LaundryUserCI</code> interface. */
	protected LaundryUserInboundPort hip;
	/**
	 * inbound port offering the <code>LaundryInternalControlCI</code> interface.
	 */
	// protected LaundryInternalControlInboundPort hicip;
	/**
	 * inbound port offering the <code>LaundryExternalControlCI</code> interface.
	 */
	protected LaundryExternalControlJava4InboundPort hecip;
	

	/** when true, methods trace their actions. */
	public static boolean VERBOSE = false;
	/** when tracing, x coordinate of the window relative position. */
	public static int X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position. */
	public static int Y_RELATIVE_POSITION = 0;

	// Appliance information

	/** standard target temperature for the Laundry in celsius. */
	protected static final Measure<Double> STANDARD_TARGET_TEMPERATURE = new Measure<>(19.0, TEMPERATURE_UNIT);
	/** fake current temperature, used when testing without simulation. */
	public static final SignalData<Double> FAKE_CURRENT_TEMPERATURE = new SignalData<>(
			new Measure<>(10.0, TEMPERATURE_UNIT));

	protected LaundryState INITIAL_STATE = LaundryState.ON;
	protected LaundryMode INITIAL_MODE = LaundryMode.WASH;

	protected Measure<Double> INITIAL_WASH_TEMPERATURE = new Measure<Double>(30.0, TEMPERATURE_UNIT);;

	/** current state (on, off) of the Laundry. */
	protected LaundryState currentState;

	protected LaundryMode currentLaundryMode;

	protected LaundryWashModes currentWashMode;

	protected Measure<Double> washTemperature;
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
	 * protected static boolean implementationInvariants(Laundry h) {
	 * 
	 * assert h != null : new PreconditionException("h != null");
	 * 
	 * boolean ret = true; ret &=
	 * AssertionChecking.checkImplementationInvariant(h.currentState != null,
	 * Laundry.class, h, "h.currentState != null"); ret &=
	 * AssertionChecking.checkImplementationInvariant( h.targetTemperature.getData()
	 * >= MIN_TARGET_TEMPERATURE.getData() && h.targetTemperature.getData() <=
	 * MAX_TARGET_TEMPERATURE.getData(), Laundry.class, h,
	 * "targetTemperature.getData() >= MIN_TARGET_TEMPERATURE.getData() && " +
	 * "targetTemperature.getData() <= MIN_TARGET_TEMPERATURE.getData()"); ret &=
	 * AssertionChecking.checkImplementationInvariant(
	 * h.currentPowerLevel.getMeasure().getData() >= 0.0 &&
	 * h.currentPowerLevel.getMeasure().getData() <= MAX_POWER_LEVEL.getData(),
	 * Laundry.class, h, "currentPowerLevel.getMeasure().getData() >= 0.0 && " +
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
	 * protected static boolean invariants(Laundry h) { assert h != null : new
	 * PreconditionException("h != null");
	 * 
	 * boolean ret = true; ret &= LaundryTemperatureI.invariants(h); ret &=
	 * LaundryExternalControlI.invariants(h); ret &=
	 * AssertionChecking.checkInvariant( REFLECTION_INBOUND_PORT_URI != null &&
	 * !REFLECTION_INBOUND_PORT_URI.isEmpty(), Laundry.class, h,
	 * "REFLECTION_INBOUND_PORT_URI != null && " +
	 * "!REFLECTION_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant(USER_INBOUND_PORT_URI != null &&
	 * !USER_INBOUND_PORT_URI.isEmpty(), Laundry.class, h,
	 * "USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant( INTERNAL_CONTROL_INBOUND_PORT_URI != null
	 * && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(), Laundry.class, h,
	 * "INTERNAL_CONTROL_INBOUND_PORT_URI != null && " +
	 * "!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant( EXTERNAL_CONTROL_INBOUND_PORT_URI != null
	 * && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(), Laundry.class, h,
	 * "EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&" +
	 * "!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()"); ret &=
	 * AssertionChecking.checkInvariant(X_RELATIVE_POSITION >= 0, Laundry.class, h,
	 * "X_RELATIVE_POSITION >= 0"); ret &=
	 * AssertionChecking.checkInvariant(Y_RELATIVE_POSITION >= 0, Laundry.class, h,
	 * "Y_RELATIVE_POSITION >= 0"); return ret; }
	 */

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new Laundry.
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
	 * protected Laundry() throws Exception { this(USER_INBOUND_PORT_URI,
	 * INTERNAL_CONTROL_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI); }
	 */

	protected Laundry() throws Exception {
		this(USER_INBOUND_PORT_URI, EXTERNAL_CONTROL_INBOUND_PORT_URI);
	}

	/**
	 * create a new Laundry.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryInternalControlInboundPortURI != null && !LaundryInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 * 
	 * @param LaundryUserInboundPortURI            URI of the inbound port to call
	 *                                             the Laundry component for user
	 *                                             interactions.
	 * @param LaundryInternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Laundry component for
	 *                                             internal control.
	 * @param LaundryExternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Laundry component for
	 *                                             external control.
	 * @throws Exception <i>to do</i>.
	 */
	/*
	 * protected Laundry(String LaundryUserInboundPortURI, String
	 * LaundryInternalControlInboundPortURI, String
	 * LaundryExternalControlInboundPortURI) throws Exception { super(1, 0);
	 * this.initialise(LaundryUserInboundPortURI,
	 * LaundryInternalControlInboundPortURI, LaundryExternalControlInboundPortURI);
	 * }
	 */

	protected Laundry(String LaundryUserInboundPortURI, String LaundryExternalControlInboundPortURI) throws Exception {
		super(1, 0);
		this.initialise(LaundryUserInboundPortURI, LaundryExternalControlInboundPortURI);
	}

	/**
	 * create a new Laundry.
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
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryInternalControlInboundPortURI != null && !LaundryInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI             URI of the reflection inbound
	 *                                             port of the component.
	 * @param LaundryUserInboundPortURI            URI of the inbound port to call
	 *                                             the Laundry component for user
	 *                                             interactions.
	 * @param LaundryInternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Laundry component for
	 *                                             internal control.
	 * @param LaundryExternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Laundry component for
	 *                                             external control.
	 * @throws Exception <i>to do</i>.
	 */
	protected Laundry(String reflectionInboundPortURI, String LaundryUserInboundPortURI,
			String LaundryInternalControlInboundPortURI, String LaundryExternalControlInboundPortURI) throws Exception {
		super(reflectionInboundPortURI, 1, 0);

		this.initialise(LaundryUserInboundPortURI, 
				LaundryExternalControlInboundPortURI);
	}

	/**
	 * create a new thermostated Laundry.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryInternalControlInboundPortURI != null && !LaundryInternalControlInboundPortURI.isEmpty()
	 * }
	 * pre	{@code
	 * LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty()
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param LaundryUserInboundPortURI            URI of the inbound port to call
	 *                                             the Laundry component for user
	 *                                             interactions.
	 * @param LaundryInternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Laundry component for
	 *                                             internal control.
	 * @param LaundryExternalControlInboundPortURI URI of the inbound port to call
	 *                                             the Laundry component for
	 *                                             external control.
	 * @throws Exception <i>to do</i>.
	 */

	/*
	 * protected void initialise(String LaundryUserInboundPortURI, String
	 * LaundryInternalControlInboundPortURI, String
	 * LaundryExternalControlInboundPortURI) throws Exception { assert
	 * LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty();
	 * assert LaundryInternalControlInboundPortURI != null &&
	 * !LaundryInternalControlInboundPortURI.isEmpty(); assert
	 * LaundryExternalControlInboundPortURI != null &&
	 * !LaundryExternalControlInboundPortURI.isEmpty();
	 * 
	 * this.currentState = LaundryState.OFF; // this.currentPowerLevel = new
	 * SignalData<>(MAX_POWER_LEVEL); // this.targetTemperature =
	 * STANDARD_TARGET_TEMPERATURE;
	 * 
	 * // this.hip = new LaundryUserJava4InboundPort(LaundryUserInboundPortURI,
	 * this); // this.hip.publishPort(); // this.hicip = new //
	 * LaundryInternalControlInboundPort(LaundryInternalControlInboundPortURI, //
	 * this); // this.hicip.publishPort(); this.hecip = new
	 * LaundryExternalControlJava4InboundPort(LaundryExternalControlInboundPortURI,
	 * this); this.hecip.publishPort();
	 * 
	 * if (VERBOSE) { this.tracer.get().setTitle("Laundry component");
	 * this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
	 * Y_RELATIVE_POSITION); this.toggleTracing(); }
	 * 
	 * /* assert Laundry.implementationInvariants(this) : new
	 * ImplementationInvariantException("Laundry.implementationInvariants(this)");
	 * assert Laundry.invariants(this) : new
	 * InvariantException("Laundry.invariants(this)");
	 */
	//}

	

	protected void initialise(String LaundryUserInboundPortURI, 
			String LaundryExternalControlInboundPortURI) throws Exception {
		assert LaundryUserInboundPortURI != null && !LaundryUserInboundPortURI.isEmpty();
		assert LaundryExternalControlInboundPortURI != null && !LaundryExternalControlInboundPortURI.isEmpty();

		this.currentState = LaundryState.OFF;
		this.currentLaundryMode = LaundryMode.WASH;
		// this.currentPowerLevel = new SignalData<>(MAX_POWER_LEVEL);
		// this.targetTemperature = STANDARD_TARGET_TEMPERATURE;

		// this.hip = new LaundryUserJava4InboundPort(LaundryUserInboundPortURI, this);
		// this.hip.publishPort();
		// this.hicip = new
		// LaundryInternalControlInboundPort(LaundryInternalControlInboundPortURI,
		// this);
		// this.hicip.publishPort();
		this.hip = new LaundryUserInboundPort(LaundryUserInboundPortURI, this);
		this.hip.publishPort();
		this.hecip = new LaundryExternalControlJava4InboundPort(LaundryExternalControlInboundPortURI, this);
		this.hecip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Laundry component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		/*
		 * assert Laundry.implementationInvariants(this) : new
		 * ImplementationInvariantException("Laundry.implementationInvariants(this)");
		 * assert Laundry.invariants(this) : new
		 * InvariantException("Laundry.invariants(this)");
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
	public LaundryState getState() throws Exception {

		return this.currentState;
	}

	@Override
	public LaundryMode getLaundryMode() throws Exception {

		assert this.getState() == LaundryState.ON : new PreconditionException("getState() == LaundryState.ON");

		return currentLaundryMode;
	}

	@Override
	public LaundryWashModes getLaundryWashMode() throws Exception {

		assert this.getLaundryMode() == LaundryMode.WASH
				: new PreconditionException("getLaundryMode() == LaundryMode.WASH");

		return currentWashMode;
	}

	@Override
	public void turnOn() throws Exception {

		assert this.getState() == LaundryState.OFF : new PreconditionException("getState() == LaundryState.OFF");

		this.currentState = LaundryState.ON;

	}

	@Override
	public void turnOff() throws Exception {

		assert this.getState() == LaundryState.ON : new PreconditionException("getState() == LaundryState.ON");

		this.currentState = LaundryState.OFF;
	}

	@Override
	public void setWashMode() throws Exception {

		assert this.getLaundryMode() == LaundryMode.DRY
				: new PreconditionException("getLaundryMode() == LaundryMode.DRY");

		this.currentLaundryMode = LaundryMode.WASH;

	}

	@Override
	public void setDryMode() throws Exception {

		assert this.getLaundryMode() == LaundryMode.WASH
				: new PreconditionException("getLaundryMode() == LaundryMode.WASH");

		this.currentLaundryMode = LaundryMode.DRY;
	}

	@Override
	public void setTemperature(WashTemperatures temp) throws Exception {

		assert this.getLaundryMode() == LaundryMode.WASH
				: new PreconditionException("getLaundryMode() == LaundryMode.WASH");

		if (this.getLaundryWashMode() == LaundryWashModes.COLOR) {
			switch (temp) {
			case T_30:
				this.washTemperature = new Measure<Double>(30.0, TEMPERATURE_UNIT);
				break;
			case T_40:
				this.washTemperature = new Measure<Double>(40.0, TEMPERATURE_UNIT);
				break;
			case T_50:
				this.washTemperature = new Measure<Double>(50.0, TEMPERATURE_UNIT);
				break;
			case T_60:
				this.washTemperature = new Measure<Double>(60.0, TEMPERATURE_UNIT);
				break;
			default:
				this.washTemperature = INITIAL_WASH_TEMPERATURE;
				break;
			}
		}

		if (this.getLaundryWashMode() == LaundryWashModes.WHITE) {
			switch (temp) {
			case T_30:
				this.washTemperature = new Measure<Double>(30.0, TEMPERATURE_UNIT);
				break;
			case T_40:
				this.washTemperature = new Measure<Double>(40.0, TEMPERATURE_UNIT);
				break;
			case T_50:
				this.washTemperature = new Measure<Double>(50.0, TEMPERATURE_UNIT);
				break;
			case T_60:
				this.washTemperature = new Measure<Double>(60.0, TEMPERATURE_UNIT);
				break;
			case T_70:
				this.washTemperature = new Measure<Double>(60.0, TEMPERATURE_UNIT);
				break;
			case T_80:
				this.washTemperature = new Measure<Double>(60.0, TEMPERATURE_UNIT);
				break;
			case T_90:
				this.washTemperature = new Measure<Double>(60.0, TEMPERATURE_UNIT);
				break;

			default:
				this.washTemperature = INITIAL_WASH_TEMPERATURE;
				break;
			}
		}

	}

	@Override
	public void setLaundryWashModeWhite() throws Exception {

		assert this.getLaundryWashMode() == LaundryWashModes.COLOR
				: new PreconditionException("getLaundryWashMode() == LaundryWashModes.COLOR");

		this.currentWashMode = LaundryWashModes.WHITE;
	}

	@Override
	public void setLaundryWashModeColor() throws Exception {

		assert this.getLaundryWashMode() == LaundryWashModes.WHITE
				: new PreconditionException("getLaundryWashMode() == LaundryWashModes.WHITE");

		this.currentWashMode = LaundryWashModes.COLOR;

	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {

		return this.washTemperature;
	}

}
// -----------------------------------------------------------------------------
