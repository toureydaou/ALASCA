package fr.sorbonne_u.components.hem2025e1.equipments.hairdryer;

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
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.connections.HairDryerInboundPort;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.alasca.physical_data.Measure;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryer</code> implements the hair dryer component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The hair dryer is an uncontrollable appliance, hence it does not connect
 * with the household energy manager. However, it will connect later to the
 * electric panel to take its (simulated) electricity consumption into account.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code INITIAL_STATE != null}
 * invariant	{@code INITIAL_MODE != null}
 * invariant	{@code currentState != null}
 * invariant	{@code currentMode != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code HIGH_POWER_IN_WATTS != null && HIGH_POWER_IN_WATTS.getData() > 0.0 && HIGH_POWER_IN_WATTS.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code LOW_POWER_IN_WATTS != null && LOW_POWER_IN_WATTS.getData() > 0.0 && LOW_POWER_IN_WATTS.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code TENSION != null && (TENSION.getData() == 110.0 || TENSION.getData() == 220.0) && TENSION.getMeasurementUnit().equals(TENSION_UNIT)}
 * invariant	{@code INITIAL_STATE != null && INITIAL_MODE != null}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered={HairDryerUserCI.class})
public class			HairDryer
extends		AbstractComponent
implements	HairDryerImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
														"HAIR-DRYER-RIP-URI";	
	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			INBOUND_PORT_URI =
												"HAIR-DRYER-INBOUND-PORT-URI";

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** power consumption when in mode HIGH in the power unit used by
	 *  the hair dryer.														*/
	public static final Measure<Double>	HIGH_POWER = new Measure<Double>(
														1100.0,
														POWER_UNIT);
	/** power consumption when in mode LOW in the power unit used by
	 *  the hair dryer.														*/
	public static final Measure<Double>	LOW_POWER = new Measure<Double>(
														660.0,
														POWER_UNIT);
	/** tension required by the hair dryer in the power unit used by
	 *  the hair dryer.														*/
	public static final Measure<Double>	TENSION = new Measure<Double>(
														220.0,
														TENSION_UNIT);

	/** initial state of the hair dryer.									*/
	public static final HairDryerState	INITIAL_STATE = HairDryerState.OFF;
	/** initial mode of the hair dryer.										*/
	public static final HairDryerMode	INITIAL_MODE = HairDryerMode.LOW;

	/** current state (on, off) of the hair dryer.							*/
	protected HairDryerState			currentState;
	/** current mode of operation (low, high) of the hair dryer.			*/
	protected HairDryerMode				currentMode;

	/** inbound port offering the <code>HairDryerCI</code> interface.		*/
	protected HairDryerInboundPort		hdip;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				INITIAL_STATE != null, HairDryer.class,
				"INITIAL_STATE != null");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				INITIAL_MODE != null, HairDryer.class,
				"INITIAL_MODE != null");
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hd	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(HairDryer hd)
	{
		assert	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkInvariant(
				hd.currentState != null, HairDryer.class, hd,
				"currentState != null");
		ret &= AssertionChecking.checkInvariant(
				hd.currentMode != null, HairDryer.class, hd,
				"currentMode != null");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				HairDryer.class,
				"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
				HairDryer.class,
				"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				HIGH_POWER != null &&
					HIGH_POWER.getData() > 0.0 &&
					HIGH_POWER.getMeasurementUnit().equals(POWER_UNIT),
				HairDryer.class,
				"HIGH_POWER_IN_WATTS != null && HIGH_POWER_IN_WATTS.getData()"
				+ " > 0.0 && HIGH_POWER_IN_WATTS.getMeasurementUnit().equals("
				+ "POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				LOW_POWER != null &&
					LOW_POWER.getData() > 0.0 &&
					LOW_POWER.getMeasurementUnit().equals(POWER_UNIT),
				HairDryer.class,
				"LOW_POWER_IN_WATTS != null && LOW_POWER_IN_WATTS.getData() >"
				+ " 0.0 && LOW_POWER_IN_WATTS.getMeasurementUnit().equals("
				+ "POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION != null &&
					(TENSION.getData() == 110.0 || TENSION.getData() == 220.0) &&
					TENSION.getMeasurementUnit().equals(TENSION_UNIT),
				HairDryer.class,
				"TENSION != null && (TENSION.getData() == 110.0 || TENSION."
				+ "getData() == 220.0) && TENSION.getMeasurementUnit().equals("
				+ "TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				INITIAL_STATE != null && INITIAL_MODE != null,
				HairDryer.class,
				"INITIAL_STATE != null && INITIAL_MODE != null");
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				HairDryer.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				HairDryer.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hd	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(HairDryer hd)
	{
		assert	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			HairDryer() throws Exception
	{
		this(INBOUND_PORT_URI);
	}

	/**
	 * create a hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(String hairDryerInboundPortURI)
	throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, hairDryerInboundPortURI);
	}

	/**
	 * create a hair dryer component with the given reflection innbound port
	 * URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection innbound port of the component.
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryer(
		String reflectionInboundPortURI,
		String hairDryerInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);
		this.initialise(hairDryerInboundPortURI);
	}

	/**
	 * initialise the hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		initialise(String hairDryerInboundPortURI)
	throws Exception
	{
		assert	hairDryerInboundPortURI != null :
					new PreconditionException(
										"hairDryerInboundPortURI != null");
		assert	!hairDryerInboundPortURI.isEmpty() :
					new PreconditionException(
										"!hairDryerInboundPortURI.isEmpty()");

		this.currentState = INITIAL_STATE;
		this.currentMode = INITIAL_MODE;
		this.hdip = new HairDryerInboundPort(hairDryerInboundPortURI, this);
		this.hdip.publishPort();

		if (HairDryer.VERBOSE) {
			this.tracer.get().setTitle("Hair dryer component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		assert	HairDryer.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryer.implementationInvariants(this)");
		assert	HairDryer.invariants(this) :
				new InvariantException("HairDryer.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hdip.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#getState()
	 */
	@Override
	public HairDryerState	getState() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its state : " +
													this.currentState + ".\n");
		}

		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public HairDryerMode	getMode() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its mode : " +
													this.currentMode + ".\n");
		}

		return this.currentMode;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned on.\n");
		}

		assert	this.getState() == HairDryerState.OFF :
				new PreconditionException("getState() == HairDryerState.OFF");

		this.currentState = HairDryerState.ON;
		this.currentMode = HairDryerMode.LOW;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned off.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");

		this.currentState = HairDryerState.OFF;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is set high.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.LOW :
				new PreconditionException("getMode() == HairDryerMode.LOW");

		this.currentMode = HairDryerMode.HIGH;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception
	{
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is set low.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.HIGH :
				new PreconditionException("getMode() == HairDryerMode.HIGH");

		this.currentMode = HairDryerMode.LOW;
	}
}
// -----------------------------------------------------------------------------
