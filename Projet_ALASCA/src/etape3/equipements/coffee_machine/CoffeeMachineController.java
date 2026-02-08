package etape3.equipements.coffee_machine;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
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

import java.util.concurrent.TimeUnit;

import etape1.equipements.coffee_machine.Constants;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape3.equipements.coffee_machine.connections.connectors.CoffeeMachineActuatorConnector;
import etape3.equipements.coffee_machine.connections.connectors.CoffeeMachineSensorDataConnector;
import etape3.equipements.coffee_machine.connections.ports.CoffeeMachineActuatorOutboundPort;
import etape3.equipements.coffee_machine.connections.ports.CoffeeMachineSensorDataOutboundPort;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineActuatorCI;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachinePushImplementationI;
import etape3.equipements.coffee_machine.interfaces.CoffeeMachineSensorDataCI;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineCompoundSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineModeSensorData;
import etape3.equipements.coffee_machine.sensor_data.CoffeeMachineStateSensorData;
import etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData;
import etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>CoffeeMachineController</code> implements a controller
 * component for the coffee machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The controller is a simple fixed period threshold-based controller with
 * hysteresis for temperature control. It has two control modes. In a pull mode,
 * it calls the pull sensors of the coffee machine to get the state, mode,
 * temperature and water level. In the push mode, it sets the period for the
 * coffee machine to push the sensor data towards it and performs its control
 * decision upon each reception. It also uses a push pattern to receive changes
 * in the state of the coffee machine. For example, when the coffee machine is
 * switched on, it sends a state data telling the controller that it is now on
 * so that the controller can begin its control until the machine is switched off.
 * </p>
 *
 * <p><strong>Glass-box Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code temperatureHysteresis > 0.0}
 * invariant	{@code controlPeriod > 0}
 * invariant	{@code executionMode.isStandard() || clockURI != null && !clockURI.isEmpty()}
 * invariant	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
 * invariant	{@code actuatorIBPURI != null && !actuatorIBPURI.isEmpty()}
 * </pre>
 *
 * <p><strong>Black-box Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code STANDARD_TEMPERATURE_HYSTERESIS > 0.0}
 * invariant	{@code STANDARD_CONTROL_PERIOD > 0}
 * invariant	{@code TARGET_TEMPERATURE > 0.0}
 * invariant	{@code MIN_WATER_LEVEL > 0.0}
 * </pre>
 *
 * <p>Created on : 2025-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={CoffeeMachineSensorDataCI.CoffeeMachineSensorRequiredPullCI.class,
							  CoffeeMachineActuatorCI.class,
							  ClocksServerWithSimulationCI.class})
@OfferedInterfaces(offered={DataRequiredCI.PushCI.class})
//-----------------------------------------------------------------------------
public class			CoffeeMachineController
extends		AbstractComponent
implements	CoffeeMachinePushImplementationI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>ControlMode</code> defines the two control modes
	 * supported by the controller: PULL and PUSH.
	 */
	public static enum	ControlMode {
		PULL,
		PUSH
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;
	/** when true, some methods trace their actions.						*/
	public static boolean		VERBOSE = true;
	/** when true, some methods trace their actions.						*/
	public static boolean		DEBUG = true;

	/** the standard hysteresis used by the controller for temperature.		*/
	public static final double	STANDARD_TEMPERATURE_HYSTERESIS = 2.0;
	/** standard control period in seconds.									*/
	public static final double	STANDARD_CONTROL_PERIOD = 60.0;
	/** target temperature for the water in °C.								*/
	public static final double	TARGET_TEMPERATURE = Constants.MAX_TEMPARATURE;
	/** minimum water level in liters to allow heating/making coffee.		*/
	public static final double	MIN_WATER_LEVEL = 0.3;

	/** URI of the sensor inbound port on the {@code CoffeeMachineCyPhy}.	*/
	protected String							sensorIBP_URI;
	/** URI of the actuator inbound port on the {@code CoffeeMachineCyPhy}.*/
	protected String							actuatorIBPURI;
	/** sensor data outbound port connected to the {@code CoffeeMachine}.	*/
	protected CoffeeMachineSensorDataOutboundPort	sensorOutboundPort;
	/** actuator outbound port connected to the {@code CoffeeMachine}.		*/
	protected CoffeeMachineActuatorOutboundPort		actuatorOutboundPort;

	/** the actual hysteresis used in the control loop for temperature.		*/
	protected double							temperatureHysteresis;
	/* user set control period in seconds.									*/
	protected double							controlPeriod;
	/** control mode (push or pull) for the current execution.				*/
	protected ControlMode						controlMode;
	/* actual control period, either in pure real time (not under test)
	 * or in accelerated time (under test), expressed in nanoseconds;
	 * used for scheduling the control task.								*/
	protected long								actualControlPeriod;
	/** the current state of the coffee machine as perceived through the
	 *  sensor data received from the {@code CoffeeMachineCyPhy}.			*/
	protected CoffeeMachineState				currentState;
	/** lock controlling the access to {@code currentState}.				*/
	protected Object							stateLock;

	/** the current execution mode of the component: standard, test or
	 *  test with simulation SIL or HIL.									*/
	protected ExecutionMode						executionMode;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false
	 * otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		CoffeeMachineController instance
		)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.temperatureHysteresis > 0.0,
					CoffeeMachineController.class,
					instance,
					"temperatureHysteresis > 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.controlPeriod > 0,
					CoffeeMachineController.class,
					instance,
					"controlPeriod > 0");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.sensorIBP_URI != null &&
											!instance.sensorIBP_URI.isEmpty(),
					CoffeeMachineController.class, instance,
					"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.actuatorIBPURI != null &&
											!instance.actuatorIBPURI.isEmpty(),
					CoffeeMachineController.class, instance,
					"actuatorIBPURI != null && !actuatorIBPURI.isEmpty()");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(CoffeeMachineController instance)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkInvariant(
					X_RELATIVE_POSITION >= 0,
					CoffeeMachineController.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(
					Y_RELATIVE_POSITION >= 0,
					CoffeeMachineController.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(
					STANDARD_TEMPERATURE_HYSTERESIS > 0.0,
					CoffeeMachineController.class,
					instance,
					"STANDARD_TEMPERATURE_HYSTERESIS > 0.0");
		ret &= AssertionChecking.checkInvariant(
					STANDARD_CONTROL_PERIOD > 0,
					CoffeeMachineController.class,
					instance,
					"STANDARD_CONTROL_PERIOD > 0");
		ret &= AssertionChecking.checkInvariant(
					TARGET_TEMPERATURE > 0.0,
					CoffeeMachineController.class,
					instance,
					"TARGET_TEMPERATURE > 0.0");
		ret &= AssertionChecking.checkInvariant(
					MIN_WATER_LEVEL > 0.0,
					CoffeeMachineController.class,
					instance,
					"MIN_WATER_LEVEL > 0.0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create the controller component for standard executions.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the coffee machine sensor inbound port.
	 * @param actuatorIBP_URI	URI of the coffee machine actuator inbound port.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			CoffeeMachineController(
		String sensorIBP_URI,
		String actuatorIBP_URI
		) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI,
			 STANDARD_TEMPERATURE_HYSTERESIS, STANDARD_CONTROL_PERIOD,
			 ControlMode.PULL);
	}

	/**
	 * create the controller component for standard executions with the given
	 * control parameters.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code temperatureHysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code controlMode != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI			URI of the coffee machine sensor inbound port.
	 * @param actuatorIBP_URI		URI of the coffee machine actuator inbound port.
	 * @param temperatureHysteresis	control hysteresis around the target temperature.
	 * @param controlPeriod			control period in seconds.
	 * @param controlMode			control mode: {@code PULL} or {@code PUSH}.
	 * @throws Exception 			<i>to do</i>.
	 */
	protected			CoffeeMachineController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double temperatureHysteresis,
		double controlPeriod,
		ControlMode controlMode
		) throws Exception
	{
		// two standard threads in case the thread that runs the method execute
		// can be prevented to run by the thread running processCoffeeMachineState
		// the schedulable thread pool is used to run the control task
		super(3, 1);

		this.initialise(sensorIBP_URI, actuatorIBP_URI, temperatureHysteresis,
						controlPeriod, controlMode);

		this.executionMode = ExecutionMode.STANDARD;
		this.actualControlPeriod =
				(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1)));
		if (this.actualControlPeriod <
								TimeUnit.MILLISECONDS.toNanos(10)) {
			System.out.println(
					"Warning: accelerated control period is "
							+ "too small ("
							+ this.actualControlPeriod +
							"), unexpected scheduling problems may"
							+ " occur!");
		}
	}

	// Test executions, with or without simulation

	/**
	 * create the controller component with the given parameters.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code temperatureHysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code controlMode != null}
	 * pre	{@code executionMode.isSimulationTest()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param sensorIBP_URI			URI of the coffee machine sensor inbound port.
	 * @param actuatorIBP_URI		URI of the coffee machine actuator inbound port.
	 * @param temperatureHysteresis	control hysteresis around the target temperature.
	 * @param controlPeriod			control period in seconds.
	 * @param controlMode			control mode: {@code PULL} or {@code PUSH}.
	 * @param executionMode			execution mode for the next run.
	 * @param accelerationFactor	acceleration factor for the simulation.
	 * @throws Exception 			<i>to do</i>.
	 */
	protected			CoffeeMachineController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double temperatureHysteresis,
		double controlPeriod,
		ControlMode controlMode,
		ExecutionMode executionMode,
		double accelerationFactor
		) throws Exception
	{
		// two standard threads in case the thread that runs the method execute
		// can be prevented to run by the thread running processCoffeeMachineState
		// the schedulable thread pool is used to run the control task
		super(3, 1);

		assert	sensorIBP_URI != null && !sensorIBP_URI.isEmpty() :
				new PreconditionException(
						"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		assert	actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty() :
				new PreconditionException(
					"actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()");
		assert	temperatureHysteresis > 0.0 :
				new PreconditionException("temperatureHysteresis > 0.0");
		assert	controlPeriod > 0 :
				new PreconditionException("controlPeriod > 0");
		assert	controlMode != null :
				new PreconditionException("controlMode != null");
		assert	executionMode.isSimulationTest() :
				new PreconditionException("executionMode.isSimulationTest()");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.initialise(sensorIBP_URI, actuatorIBP_URI, temperatureHysteresis,
						controlPeriod, controlMode);

		this.executionMode = executionMode;
		// the accelerated period is in nanoseconds, hence first convert
		// the period to nanoseconds, perform the division and then
		// convert to long (hence providing a better precision than
		// first dividing and then converting to nanoseconds...)
		this.actualControlPeriod =
			(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1))/
														accelerationFactor);
		// sanity checking, the standard Java scheduler has a
		// precision no less than 10 milliseconds...
		if (this.actualControlPeriod <
								TimeUnit.MILLISECONDS.toNanos(10)) {
			System.out.println(
					"Warning: accelerated control period is "
							+ "too small ("
							+ this.actualControlPeriod +
							"), unexpected scheduling problems may"
							+ " occur!");
		}

		if (VERBOSE || DEBUG) {
			this.tracer.get().setTitle("Coffee machine controller component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		// Invariant checking
		assert	CoffeeMachineController.implementationInvariants(this) :
				new ImplementationInvariantException(
						"CoffeeMachineController.implementationInvariants(this)");
		assert	CoffeeMachineController.invariants(this) :
				new InvariantException("CoffeeMachineController.invariants(this)");
	}

	/**
	 * initialise the controller component with the given parameters.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code temperatureHysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI			URI of the coffee machine sensor inbound port.
	 * @param actuatorIBP_URI		URI of the coffee machine actuator inbound port.
	 * @param temperatureHysteresis	control hysteresis around the target temperature.
	 * @param controlPeriod			control period in seconds.
	 * @param controlMode			control mode: {@code PULL} or {@code PUSH}.
	 * @throws Exception 			<i>to do</i>.
	 */
	protected void		initialise(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double temperatureHysteresis,
		double controlPeriod,
		ControlMode controlMode
		) throws Exception
	{
		this.sensorIBP_URI = sensorIBP_URI;
		this.actuatorIBPURI = actuatorIBP_URI;
		this.temperatureHysteresis = temperatureHysteresis;
		this.controlPeriod = controlPeriod;
		this.controlMode = controlMode;
		this.stateLock = new Object();

		this.sensorOutboundPort = new CoffeeMachineSensorDataOutboundPort(this);
		this.sensorOutboundPort.publishPort();
		this.actuatorOutboundPort = new CoffeeMachineActuatorOutboundPort(this);
		this.actuatorOutboundPort.publishPort();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
					this.sensorOutboundPort.getPortURI(),
					sensorIBP_URI,
					CoffeeMachineSensorDataConnector.class.getCanonicalName());
			this.doPortConnection(
					this.actuatorOutboundPort.getPortURI(),
					this.actuatorIBPURI,
					CoffeeMachineActuatorConnector.class.getCanonicalName());

			synchronized (this.stateLock) {
				this.currentState = CoffeeMachineState.OFF;
			}

			if (VERBOSE) {
				this.traceMessage("Coffee machine controller starts.\n");
			}
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Coffee machine controller ends.\n");
		}
		this.doPortDisconnection(this.sensorOutboundPort.getPortURI());
		this.doPortDisconnection(this.actuatorOutboundPort.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.sensorOutboundPort.unpublishPort();
			this.actuatorOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachinePushImplementationI#processCoffeeMachineState(etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState)
	 */
	@Override
	public void			processCoffeeMachineState(CoffeeMachineState state)
	throws Exception
	{
		assert	state != null :
				new PreconditionException("state != null");

		System.out.println("[COFFEE CTRL] receives state: " + state);
		if (DEBUG) {
			this.traceMessage("receives coffee machine state: " + state + ".\n");
		}

		// the current state is always updated, but only in the case
		// when the coffee machine is switched on that the controller begins to
		// perform the control
		synchronized (this.stateLock) {
			CoffeeMachineState oldState = this.currentState;
			this.currentState = state;

			if (state != CoffeeMachineState.OFF && oldState == CoffeeMachineState.OFF) {
				if (this.controlMode == ControlMode.PULL) {
					System.out.println("[COFFEE CTRL] START control loop (PULL mode)");
					if (VERBOSE) {
						this.traceMessage("start pull control.\n");
					}
					if (this.executionMode.isStandard() ||
								this.executionMode.isTestWithoutSimulation()) {
						this.pullControlLoop();
					} else {
						// if a state change has been detected from OFF to ON,
						// schedule a first execution of the control loop, which
						// in turn will schedule its next execution if needed
						this.scheduleTaskOnComponent(
								new AbstractComponent.AbstractTask() {
									@Override
									public void run() {
										((CoffeeMachineController)
												this.getTaskOwner()).
															pullControlLoop();
										}
									},
								this.actualControlPeriod,
								TimeUnit.NANOSECONDS);
					}
				} else {
					if (VERBOSE) {
						this.traceMessage("start push control.\n");
					}
					long cp = (long) (TimeUnit.SECONDS.toMillis(1)
														* this.controlPeriod);
					this.sensorOutboundPort.startSensorDataPush(
													cp, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

	/**
	 * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachinePushImplementationI#processCoffeeMachineData(etape3.equipements.coffee_machine.sensor_data.CoffeeMachineStateSensorData, etape3.equipements.coffee_machine.sensor_data.CoffeeMachineModeSensorData, etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData, etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData)
	 */
	@Override
	public void			processCoffeeMachineData(
		CoffeeMachineStateSensorData state,
		CoffeeMachineModeSensorData mode,
		TemperatureSensorData temperature,
		WaterLevelSensorData waterLevel
		) throws Exception
	{
		assert	state != null : new PreconditionException("state != null");
		assert	mode != null : new PreconditionException("mode != null");
		assert	temperature != null : new PreconditionException("temperature != null");
		assert	waterLevel != null : new PreconditionException("waterLevel != null");

		// execute the control only if the coffee machine is still ON
		CoffeeMachineState s = CoffeeMachineState.OFF;
		synchronized (this.stateLock) {
			s = this.currentState;
		}
		if (s != CoffeeMachineState.OFF) {
			this.oneControlStep(state, mode, temperature, waterLevel, s);
		} else {
			// when the coffee machine is OFF, exit the control loop
			if (VERBOSE) {
				this.traceMessage("control is off.\n");
			}
		}
	}

	/**
	 * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachinePushImplementationI#processCoffeeMachineMode(etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode)
	 */
	@Override
	public void			processCoffeeMachineMode(CoffeeMachineMode mode)
	throws Exception
	{
		assert	mode != null : new PreconditionException("mode != null");

		if (DEBUG) {
			this.traceMessage("receives coffee machine mode: " + mode + ".\n");
		}
		// Currently, mode changes don't trigger specific control actions
		// but could be used for energy optimization strategies
	}

	/**
	 * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachinePushImplementationI#processCoffeeMachineTemperature(etape3.equipements.coffee_machine.sensor_data.TemperatureSensorData)
	 */
	@Override
	public void			processCoffeeMachineTemperature(
		TemperatureSensorData temperature
		) throws Exception
	{
		assert	temperature != null :
				new PreconditionException("temperature != null");

		if (DEBUG) {
			this.traceMessage("receives temperature: " + temperature + ".\n");
		}
		// Individual temperature updates can be processed here if needed
	}

	/**
	 * @see etape3.equipements.coffee_machine.interfaces.CoffeeMachinePushImplementationI#processCoffeeMachineWaterLevel(etape3.equipements.coffee_machine.sensor_data.WaterLevelSensorData)
	 */
	@Override
	public void			processCoffeeMachineWaterLevel(
		WaterLevelSensorData waterLevel
		) throws Exception
	{
		assert	waterLevel != null :
				new PreconditionException("waterLevel != null");

		if (DEBUG) {
			this.traceMessage("receives water level: " + waterLevel + ".\n");
		}
		// Individual water level updates can be processed here if needed
	}

	/**
	 * perform one control step based on the current sensor data.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code state != null}
	 * pre	{@code mode != null}
	 * pre	{@code temperature != null}
	 * pre	{@code waterLevel != null}
	 * pre	{@code priorState != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param state			current state sensor data.
	 * @param mode			current mode sensor data.
	 * @param temperature	current temperature sensor data.
	 * @param waterLevel	current water level sensor data.
	 * @param priorState	state before this control step.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		oneControlStep(
		CoffeeMachineStateSensorData state,
		CoffeeMachineModeSensorData mode,
		TemperatureSensorData temperature,
		WaterLevelSensorData waterLevel,
		CoffeeMachineState priorState
		) throws Exception
	{
		// Get current values
		double currentTemp = temperature.getMeasure().getData();
		double currentWater = waterLevel.getMeasure().getData();

		// SAFETY CONTROL MODE
		// The controller does NOT start heating automatically.
		// Heating is started by user actions (makeExpresso event).
		// The controller ONLY stops heating when:
		// 1. Maximum temperature is reached
		// 2. Water level is too low

		if (VERBOSE) {
			this.traceMessage("monitoring: state=" + priorState
					+ ", temp=" + currentTemp + "°C"
					+ ", water=" + currentWater + "L"
					+ " at " + temperature.getTimestamp() + ".\n");
		}
		
		// Safety control: stop heating if conditions are not met
		if (priorState == CoffeeMachineState.HEATING) {
			boolean shouldStopHeating = false;
			String reason = "";

			// Check water level
			if (currentWater < MIN_WATER_LEVEL) {
				shouldStopHeating = true;
				reason = "insufficient water (" + currentWater + "L < " + MIN_WATER_LEVEL + "L)";
			}
			// Check maximum temperature
			// Stop slightly before boiling point to account for reaction delay
			// and prevent overshoot due to discrete time steps
			else if (currentTemp >= TARGET_TEMPERATURE - 2.0) {
				shouldStopHeating = true;
				reason = "target temperature approached (" + currentTemp + "°C >= " + (TARGET_TEMPERATURE - 2.0) + "°C)";
			}

			if (shouldStopHeating) {
				// Stop heating
				this.actuatorOutboundPort.stopHeating();
				// Update state
				synchronized (this.stateLock) {
					this.currentState = CoffeeMachineState.ON;
				}
				System.out.println("[COFFEE CTRL] STOP heating: " + reason);
				if (VERBOSE) {
					this.traceMessage("stop heating: " + reason
							+ " at " + temperature.getTimestamp() + ".\n");
				}
			} else {
				// Still heating, just monitoring
				if (VERBOSE) {
					this.traceMessage("still heating: " + currentTemp + "°C < "
							+ TARGET_TEMPERATURE + "°C at "
							+ temperature.getTimestamp() + ".\n");
				}
				// Update state tracking
				synchronized (this.stateLock) {
					this.currentState = state.getMeasure().getData();
				}
			}
		} else {
			// Not heating, just update state tracking
			synchronized (this.stateLock) {
				this.currentState = state.getMeasure().getData();
			}
		}
	}

	/**
	 * implement the pull control loop.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		pullControlLoop()
	{
		try {
			CoffeeMachineState priorState = CoffeeMachineState.OFF;
			synchronized (this.stateLock) {
				priorState = this.currentState;
			}
			if (priorState != CoffeeMachineState.OFF
					&& !this.sensorOutboundPort.isDestroyed()) {
				try {
					CoffeeMachineCompoundSensorData sensorData =
							(CoffeeMachineCompoundSensorData)
											this.sensorOutboundPort.request();

					System.out.println("[COFFEE CTRL] pull step: state="
							+ priorState + ", temp="
							+ sensorData.getTemperature().getMeasure().getData()
							+ "C, water="
							+ sensorData.getWaterLevel().getMeasure().getData()
							+ "L");

					this.oneControlStep(sensorData.getState(),
										sensorData.getMode(),
										sensorData.getTemperature(),
										sensorData.getWaterLevel(),
										priorState);
				} catch (Exception e) {
					System.out.println("[COFFEE CTRL] ERROR in pull sensor request: "
							+ e.getMessage());
					e.printStackTrace();
				}

				// Always reschedule, even after error
				this.scheduleTask(
						o -> ((CoffeeMachineController)o).pullControlLoop(),
						this.actualControlPeriod,
						TimeUnit.NANOSECONDS);
			} else {
				System.out.println("[COFFEE CTRL] exit control (state=OFF)");
			}
		} catch (Exception e) {
			System.out.println("[COFFEE CTRL] FATAL in pullControlLoop: "
					+ e.getMessage());
			e.printStackTrace();
		}
	}
}
// -----------------------------------------------------------------------------
