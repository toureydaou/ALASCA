package etape1.equipements.fan;

import etape1.bases.RegistrationCI;
import etape1.equipements.fan.connections.ports.FanInboundPort;
import etape1.equipements.fan.interfaces.FanImplementationI;
import etape1.equipements.fan.interfaces.FanUserCI;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import physical_data.Measure;
import physical_data.MeasurementUnit;





/**
 * The class <code>Fan</code> implements the fan component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The fan is an uncontrollable appliance, hence it does not connect
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
@OfferedInterfaces(offered={FanUserCI.class})
@RequiredInterfaces(required = { RegistrationCI.class, ClocksServerCI.class })
public class Fan extends AbstractComponent implements FanImplementationI {
	// -------------------------------------------------------------------------
		// Constants and variables
		// -------------------------------------------------------------------------

		/** URI of the fan inbound port used in tests.					*/
		public static final String			REFLECTION_INBOUND_PORT_URI =
															"FAN-RIP-URI";	
		/** URI of the fan inbound port used in tests.					*/
		public static final String			INBOUND_PORT_URI =
													"FAN-INBOUND-PORT-URI";

		/** when true, methods trace their actions.								*/
		public static boolean				VERBOSE = false;
		/** when tracing, x coordinate of the window relative position.			*/
		public static int					X_RELATIVE_POSITION = 0;
		/** when tracing, y coordinate of the window relative position.			*/
		public static int					Y_RELATIVE_POSITION = 0;
		
		public static final Measure<Double>	LOW_POWER_IN_WATTS =
				new Measure<Double>(
							Constants.LOW_POWER_MODE_FAN,
							MeasurementUnit.WATTS);
		
		public static final Measure<Double>	MEDIUM_POWER_IN_WATTS =
				new Measure<Double>(
						Constants.MEDIUM_POWER_MODE_FAN,
							MeasurementUnit.WATTS);

		public static final Measure<Double>	HIGH_POWER_IN_WATTS =
												new Measure<Double>(
														Constants.HIGH_POWER_MODE_FAN,
															MeasurementUnit.WATTS);
		
		public static final Measure<Double>	VOLTAGE =
												new Measure<Double>(
														Constants.VOLTAGE_FAN,
															MeasurementUnit.VOLTS);

		/** initial state of the fan.									*/
		protected static final FanState	INITIAL_STATE = FanState.OFF;
		/** initial mode of the fan.										*/
		public static final FanMode	INITIAL_MODE = FanMode.LOW;

		/** current state (on, off) of the fan.							*/
		protected FanState			currentState;
		/** current mode of operation (low, high) of the fan.			*/
		protected FanMode				currentMode;

		/** inbound port offering the <code>FanCI</code> interface.		*/
		protected FanInboundPort		fip;

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
		 * create a fan component.
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
		 * create a fan component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code FanInboundPortURI != null && !FanInboundPortURI.isEmpty()}
		 * post	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 * 
		 * @param FanInboundPortURI	URI of the fan inbound port.
		 * @throws Exception				<i>to do</i>.
		 */
		protected			Fan(String FanInboundPortURI)
		throws Exception
		{
			this(REFLECTION_INBOUND_PORT_URI, FanInboundPortURI);
		}

		/**
		 * create a fan component with the given reflection innbound port
		 * URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
		 * pre	{@code FanInboundPortURI != null && !FanInboundPortURI.isEmpty()}
		 * post	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 *
		 * @param reflectionInboundPortURI	URI of the reflection innbound port of the component.
		 * @param FanInboundPortURI	URI of the fan inbound port.
		 * @throws Exception				<i>to do</i>.
		 */
		protected			Fan(
			String reflectionInboundPortURI,
			String FanInboundPortURI
			) throws Exception
		{
			super(reflectionInboundPortURI, 1, 0);
			this.initialise(FanInboundPortURI);
		}

		/**
		 * initialise the fan component.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code FanInboundPortURI != null && !FanInboundPortURI.isEmpty()}
		 * post	{@code getState() == FanState.OFF}
		 * post	{@code getMode() == FanMode.LOW}
		 * </pre>
		 * 
		 * @param FanInboundPortURI	URI of the fan inbound port.
		 * @throws Exception				<i>to do</i>.
		 */
		protected void		initialise(String FanInboundPortURI)
		throws Exception
		{
			assert	FanInboundPortURI != null :
						new PreconditionException(
											"FanInboundPortURI != null");
			assert	!FanInboundPortURI.isEmpty() :
						new PreconditionException(
											"!FanInboundPortURI.isEmpty()");

			this.currentState = INITIAL_STATE;
			this.currentMode = INITIAL_MODE;
			this.fip = new FanInboundPort(FanInboundPortURI, this);
			this.fip.publishPort();

			if (Fan.VERBOSE) {
				this.tracer.get().setTitle("Fan component");
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
				this.fip.unpublishPort();
			} catch (Throwable e) {
				throw new ComponentShutdownException(e) ;
			}
			super.shutdown();
		}

		// -------------------------------------------------------------------------
		// Component services implementation
		// -------------------------------------------------------------------------

		/**
		 * @see etape1.equipements.Fan.interfaces.FanImplementationI.equipments.Fan.FanImplementationI#getState()
		 */
		@Override
		public FanState	getState() throws Exception
		{
			if (Fan.VERBOSE) {
				this.traceMessage("Fan returns its state : " +
														this.currentState + ".\n");
			}

			return this.currentState;
		}

		/**
		 * @see etape1.equipements.Fan.interfaces.FanImplementationI.equipments.Fan.FanImplementationI#getMode()
		 */
		@Override
		public FanMode	getMode() throws Exception
		{
			if (Fan.VERBOSE) {
				this.traceMessage("Fan returns its mode : " +
														this.currentMode + ".\n");
			}

			return this.currentMode;
		}

		/**
		 * @see etape1.equipements.Fan.interfaces.FanImplementationI.equipments.Fan.FanImplementationI#turnOn()
		 */
		@Override
		public void			turnOn() throws Exception
		{
			if (Fan.VERBOSE) {
				this.traceMessage("Fan is turned on.\n");
			}

			assert	this.getState() == FanState.OFF :
					new PreconditionException("getState() == FanState.OFF");

			this.currentState = FanState.ON;
			this.currentMode = FanMode.LOW;
		}

		/**
		 * @see etape1.equipements.Fan.interfaces.FanImplementationI.equipments.Fan.FanImplementationI#turnOff()
		 */
		@Override
		public void			turnOff() throws Exception
		{
			if (Fan.VERBOSE) {
				this.traceMessage("Fan is turned off.\n");
			}

			assert	this.getState() == FanState.ON :
					new PreconditionException("getState() == FanState.ON");

			this.currentState = FanState.OFF;
		}

		/**
		 * @see etape1.equipements.Fan.interfaces.FanImplementationI.equipments.Fan.FanImplementationI#setHigh()
		 */
		@Override
		public void			setHigh() throws Exception
		{
			if (Fan.VERBOSE) {
				this.traceMessage("Fan is set high.\n");
			}

			assert	this.getState() == FanState.ON :
					new PreconditionException("getState() == FanState.ON");
			

			this.currentMode = FanMode.HIGH;
		}

		/**
		 * @see etape1.equipements.Fan.interfaces.FanImplementationI.equipments.Fan.FanImplementationI#setLow()
		 */
		@Override
		public void			setLow() throws Exception
		{
			if (Fan.VERBOSE) {
				this.traceMessage("Fan is set low.\n");
			}

			assert	this.getState() == FanState.ON :
					new PreconditionException("getState() == FanState.ON");

			this.currentMode = FanMode.LOW;
		}

		@Override
		public void setMedium() throws Exception {
			assert	this.getState() == FanState.ON :
				new PreconditionException("getState() == FanState.ON");
			
			this.currentMode = FanMode.MEDIUM;
			
		}
}
