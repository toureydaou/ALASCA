package etape1.equipements.fan;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryer;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI.HairDryerMode;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI.HairDryerState;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>Fan</code> implements the hair dryer component.
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
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered= {FanUserCI.class})
public class Fan extends AbstractComponent implements FanImplementationI {


	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
			"FAN-RIP-URI";	
	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String			INBOUND_PORT_URI =
			"FAN-INBOUND-PORT-URI";

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	public static final Measure<Double>	HIGH_POWER_IN_WATTS =
			new Measure<Double>(
					800.0,
					MeasurementUnit.WATTS);
	public static final Measure<Double>	LOW_POWER_IN_WATTS =
			new Measure<Double>(
					60.0,
					MeasurementUnit.WATTS);
	public static final Measure<Double>	VOLTAGE =
			new Measure<Double>(
					220.0,
					MeasurementUnit.VOLTS);

	/** initial state of the hair dryer.									*/
	protected static final FanState	INITIAL_STATE = FanState.OFF;
	/** initial mode of the hair dryer.										*/
	protected static final FanMode	INITIAL_MODE = FanMode.LOW;

	/** current state (on, off) of the hair dryer.							*/
	protected FanState			currentState;
	/** current mode of operation (low, high) of the hair dryer.			*/
	protected FanMode				currentMode;

	/** inbound port offering the <code>FanCI</code> interface.		*/
	protected FanInboundPort		hdip;


	// -------------------------------------------------------------------------
		// Invariants
		// -------------------------------------------------------------------------

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
		protected static boolean	implementationInvariants(Fan hd)
		{
			assert	hd != null : new PreconditionException("hd != null");

			boolean ret = true;

			ret &= AssertionChecking.checkInvariant(
					INITIAL_STATE != null,
					Fan.class, hd,
					"INITIAL_STATE != null");
			ret &= AssertionChecking.checkInvariant(
					INITIAL_MODE != null,
					Fan.class, hd,
					"INITIAL_MODE != null");
			ret &= AssertionChecking.checkInvariant(
					hd.currentState != null,
					Fan.class, hd,
					"hd.currentState != null");
			ret &= AssertionChecking.checkInvariant(
					hd.currentMode != null,
					Fan.class, hd,
					"hd.currentMode != null");
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
		protected static boolean	invariants(Fan hd)
		{
			assert	hd != null : new PreconditionException("hd != null");

			boolean ret = true;
			ret &= AssertionChecking.checkImplementationInvariant(
					REFLECTION_INBOUND_PORT_URI != null &&
										!REFLECTION_INBOUND_PORT_URI.isEmpty(),
					Fan.class, hd,
					"REFLECTION_INBOUND_PORT_URI != null && "
									+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
			ret &= AssertionChecking.checkImplementationInvariant(
					INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
					Fan.class, hd,
					"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
			ret &= AssertionChecking.checkImplementationInvariant(
					X_RELATIVE_POSITION >= 0,
					Fan.class, hd,
					"X_RELATIVE_POSITION >= 0");
			ret &= AssertionChecking.checkImplementationInvariant(
					Y_RELATIVE_POSITION >= 0,
					Fan.class, hd,
					"Y_RELATIVE_POSITION >= 0");
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
		 * post	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 * 
		 * @throws Exception	<i>to do</i>.
		 */
		protected			Fan() throws Exception
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
		 * post	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 * 
		 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
		 * @throws Exception				<i>to do</i>.
		 */
		protected			Fan(String hairDryerInboundPortURI)
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
		 * post	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 *
		 * @param reflectionInboundPortURI	URI of the reflection innbound port of the component.
		 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
		 * @throws Exception				<i>to do</i>.
		 */
		protected			Fan(
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
		 * post	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
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
			this.hdip = new FanInboundPort(hairDryerInboundPortURI, this);
			this.hdip.publishPort();

			if (Fan.VERBOSE) {
				this.tracer.get().setTitle("Hair dryer component");
				this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
													  Y_RELATIVE_POSITION);
				this.toggleTracing();
			}

			assert	Fan.implementationInvariants(this) :
					new ImplementationInvariantException(
							"Fan.implementationInvariants(this)");
			assert	Fan.invariants(this) :
					new InvariantException("Fan.invariants(this)");
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


	@Override
	public FanState getState() throws Exception {
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its state : " +
													this.currentState + ".\n");
		}

		return this.currentState;
	}

	@Override
	public FanMode getMode() throws Exception {
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer returns its mode : " +
													this.currentMode + ".\n");
		}

		return this.currentMode;
	}

	@Override
	public void turnOn() throws Exception {

		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned on.\n");
		}

		assert	this.getState() == FanState.OFF :
				new PreconditionException("getState() == HairDryerState.OFF");

		this.currentState = FanState.ON;
		this.currentMode = FanMode.LOW;


	}

	@Override
	public void turnOff() throws Exception {
		if (HairDryer.VERBOSE) {
			this.traceMessage("Hair dryer is turned on.\n");
		}

		assert	this.getState() == FanState.OFF :
				new PreconditionException("getState() == HairDryerState.OFF");

		this.currentState = FanState.OFF;

	}

	@Override
	public void setHigh() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMedium() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLow() throws Exception {
		// TODO Auto-generated method stub

	} 

}
