package etape3.equipements.kettle;

import java.util.concurrent.TimeUnit;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import etape3.equipements.kettle.connections.connectors.KettleActuatorConnector;
import etape3.equipements.kettle.connections.connectors.KettleSensorDataConnector;
import etape3.equipements.kettle.connections.ports.KettleActuatorOutboundPort;
import etape3.equipements.kettle.connections.ports.KettleSensorDataOutboundPort;
import etape3.equipements.kettle.sensor_data.KettleTemperatureSensorData;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleController</code> implements a controller component
 * for the kettle (water heater).
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The controller is a simple fixed period threshold-based controller with
 * hysteresis. It has two control modes. In a pull mode, it calls the pull
 * sensors of the kettle to get the current temperature. In the push mode,
 * the kettle pushes data to the controller at regular intervals.
 * It uses a push pattern to receive changes in the state of the kettle.
 * When the kettle is switched on, it sends a state data telling the
 * controller that it is now on so that the controller can begin its control
 * until the kettle is switched off.
 * </p>
 * <p>
 * Hysteresis control with target temperature and hysteresis band:
 * <ul>
 * <li>If currentTemp &lt; targetTemp - hysteresis: start heating</li>
 * <li>If currentTemp &gt; targetTemp + hysteresis: stop heating</li>
 * </ul>
 * Default: target=30C, hysteresis=10C, giving band [20C, 40C].
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
@RequiredInterfaces(required={
	KettleSensorDataCI.KettleSensorRequiredPullCI.class,
	KettleActuatorCI.class,
	ClocksServerWithSimulationCI.class})
@OfferedInterfaces(offered={DataRequiredCI.PushCI.class})
public class			KettleController
extends		AbstractComponent
implements	KettlePushImplementationI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>ControlMode</code> describes the two possible
	 * control modes for the kettle controller.
	 */
	public static enum	ControlMode {
		/** pull mode: the controller queries the kettle sensors.			*/
		PULL,
		/** push mode: the kettle pushes data to the controller.			*/
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

	/** the standard hysteresis used by the controller.						*/
	public static final double	STANDARD_HYSTERESIS = 10.5;
	/** standard control period in seconds.									*/
	public static final double	STANDARD_CONTROL_PERIOD = 60.0;
	/** standard target temperature for the kettle in celsius.				*/
	public static final double	STANDARD_TARGET_TEMPERATURE = 32.5;

	/** URI of the sensor inbound port on the {@code KettleCyPhy}.			*/
	protected String							sensorIBP_URI;
	/** URI of the actuator inbound port on the {@code KettleCyPhy}.		*/
	protected String							actuatorIBP_URI;
	/** sensor data outbound port connected to the {@code KettleCyPhy}.		*/
	protected KettleSensorDataOutboundPort		sensorOutboundPort;
	/** actuator outbound port connected to the {@code KettleCyPhy}.		*/
	protected KettleActuatorOutboundPort		actuatorOutboundPort;

	/** the actual hysteresis used in the control loop.						*/
	protected double							hysteresis;
	/** user set control period in seconds.									*/
	protected double							controlPeriod;
	/** control mode (push or pull) for the current execution.				*/
	protected ControlMode						controlMode;
	/** actual control period, either in pure real time (not under test)
	 * or in accelerated time (under test), expressed in nanoseconds.		*/
	protected long								actualControlPeriod;
	/** the current state of the kettle as perceived through the sensor
	 *  data received from the {@code KettleCyPhy}.						*/
	protected KettleState						currentState;
	/** lock controlling the access to {@code currentState}.				*/
	protected Object							stateLock;
	/** the current execution mode of the component.						*/
	protected ExecutionMode						executionMode;

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
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the kettle sensor inbound port.
	 * @param actuatorIBP_URI	URI of the kettle actuator inbound port.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			KettleController(
		String sensorIBP_URI,
		String actuatorIBP_URI
		) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI,
			 STANDARD_HYSTERESIS, STANDARD_CONTROL_PERIOD,
			 ControlMode.PULL);
	}

	/**
	 * create the controller component for standard executions with the given
	 * control parameters.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code controlMode != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the kettle sensor inbound port.
	 * @param actuatorIBP_URI	URI of the kettle actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @param controlMode		control mode: {@code PULL} or {@code PUSH}.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			KettleController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod,
		ControlMode controlMode
		) throws Exception
	{
		super(3, 1);

		this.initialise(sensorIBP_URI, actuatorIBP_URI, hysteresis,
						controlPeriod, controlMode);

		this.executionMode = ExecutionMode.STANDARD;
		this.actualControlPeriod =
				(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1)));

		if (this.actualControlPeriod <
								TimeUnit.MILLISECONDS.toNanos(10)) {
			if (VERBOSE) {
				this.traceMessage("Warning: control period is too small ("
						+ this.actualControlPeriod + " ns).\n");
			}
		}
	}

	// Test executions, with or without simulation

	/**
	 * create the controller component with the given parameters for test
	 * executions.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code controlMode != null}
	 * pre	{@code executionMode.isSimulationTest()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI			URI of the kettle sensor inbound port.
	 * @param actuatorIBP_URI		URI of the kettle actuator inbound port.
	 * @param hysteresis			control hysteresis around the target temperature.
	 * @param controlPeriod			control period in seconds.
	 * @param controlMode			control mode: {@code PULL} or {@code PUSH}.
	 * @param executionMode			execution mode for the next run.
	 * @param accelerationFactor	acceleration factor for the simulation.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			KettleController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod,
		ControlMode controlMode,
		ExecutionMode executionMode,
		double accelerationFactor
		) throws Exception
	{
		super(3, 1);

		assert	sensorIBP_URI != null && !sensorIBP_URI.isEmpty() :
				new PreconditionException(
						"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		assert	actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty() :
				new PreconditionException(
					"actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()");
		assert	hysteresis > 0.0 :
				new PreconditionException("hysteresis > 0.0");
		assert	controlPeriod > 0 :
				new PreconditionException("controlPeriod > 0");
		assert	controlMode != null :
				new PreconditionException("controlMode != null");
		assert	executionMode.isSimulationTest() :
				new PreconditionException("executionMode.isSimulationTest()");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.initialise(sensorIBP_URI, actuatorIBP_URI, hysteresis,
						controlPeriod, controlMode);

		this.executionMode = executionMode;
		this.actualControlPeriod =
			(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1))/
														accelerationFactor);

		if (this.actualControlPeriod <
								TimeUnit.MILLISECONDS.toNanos(10)) {
			if (VERBOSE) {
				this.traceMessage("Warning: accelerated control period is "
						+ "too small (" + this.actualControlPeriod
						+ " ns).\n");
			}
		}

		if (VERBOSE || DEBUG) {
			this.tracer.get().setTitle("Kettle controller component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	/**
	 * initialise the controller component with the given parameters.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the kettle sensor inbound port.
	 * @param actuatorIBP_URI	URI of the kettle actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @param controlMode		control mode: {@code PULL} or {@code PUSH}.
	 * @throws Exception		<i>to do</i>.
	 */
	protected void		initialise(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod,
		ControlMode controlMode
		) throws Exception
	{
		this.sensorIBP_URI = sensorIBP_URI;
		this.actuatorIBP_URI = actuatorIBP_URI;
		this.hysteresis = hysteresis;
		this.controlPeriod = controlPeriod;
		this.controlMode = controlMode;
		this.stateLock = new Object();

		this.sensorOutboundPort = new KettleSensorDataOutboundPort(this);
		this.sensorOutboundPort.publishPort();
		this.actuatorOutboundPort = new KettleActuatorOutboundPort(this);
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
					KettleSensorDataConnector.class.getCanonicalName());
			this.doPortConnection(
					this.actuatorOutboundPort.getPortURI(),
					this.actuatorIBP_URI,
					KettleActuatorConnector.class.getCanonicalName());

			synchronized (this.stateLock) {
				this.currentState = KettleState.OFF;
			}

			if (VERBOSE) {
				this.traceMessage("Kettle controller starts.\n");
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
			this.traceMessage("Kettle controller ends.\n");
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
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal methods - Push callbacks
	// -------------------------------------------------------------------------

	/**
	 * receive and process the state data coming from the kettle component,
	 * starting the control loop if the state has changed from {@code OFF} to
	 * {@code ON} or {@code HEATING}.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code kettleState != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param kettleState	state data received from the kettle component.
	 */
	@Override
	public void			processKettleState(KettleState kettleState)
	{
		assert	kettleState != null :
				new PreconditionException("kettleState != null");

		if (DEBUG) {
			this.traceMessage("receives kettle state: " + kettleState + ".\n");
		}

		synchronized (this.stateLock) {
			KettleState oldState = this.currentState;
			this.currentState = kettleState;

			if (kettleState != KettleState.OFF
				&& oldState == KettleState.OFF) {
				if (this.controlMode == ControlMode.PULL) {
					if (VERBOSE) {
						this.traceMessage("start pull control.\n");
					}
					if (this.executionMode.isStandard() ||
							this.executionMode.isTestWithoutSimulation()) {
						this.pullControlLoop();
					} else {
						this.scheduleTaskOnComponent(
							new AbstractComponent.AbstractTask() {
								@Override
								public void run() {
									((KettleController)
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
					try {
						this.sensorOutboundPort.
								startSensorDataPush(cp, TimeUnit.MILLISECONDS);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	/**
	 * receive and process the temperature data pushed from the kettle.
	 * In push control mode, this method triggers the control step.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code temperature != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param temperature	temperature data pushed from the kettle component.
	 */
	@Override
	public void			processKettleTemperature(
		KettleTemperatureSensorData temperature
		)
	{
		assert	temperature != null :
				new PreconditionException("temperature != null");

		try {
			KettleState s = KettleState.OFF;
			synchronized (this.stateLock) {
				s = this.currentState;
			}
			if (s != KettleState.OFF) {
				double currentTemp = temperature.getTemperature();
				this.oneControlStep(currentTemp, s);
			} else {
				if (VERBOSE) {
					this.traceMessage("control is off.\n");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------
	// Control logic
	// -------------------------------------------------------------------------

	/**
	 * execute one control step based on the current temperature and state.
	 * Uses hysteresis: c &lt; target - hysteresis =&gt; heat,
	 *                  c &gt; target + hysteresis =&gt; stop.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param currentTemp	the current water temperature in celsius.
	 * @param priorState	the prior state of the kettle.
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		oneControlStep(
		double currentTemp,
		KettleState priorState
		) throws Exception
	{
		double lowThreshold =
				STANDARD_TARGET_TEMPERATURE - this.hysteresis;
		double highThreshold =
				STANDARD_TARGET_TEMPERATURE + this.hysteresis;

		StringBuffer sb = new StringBuffer();

		if (currentTemp < lowThreshold) {
			// temperature is too low, start heating
			if (KettleState.HEATING != priorState) {
				this.actuatorOutboundPort.startHeating();
				synchronized (this.stateLock) {
					this.currentState = KettleState.HEATING;
				}
				System.out.println(
						"[KETTLE CTRL] START heating: "
						+ String.format("%.2fC < %.0fC",
										currentTemp, lowThreshold));
			}
			if (VERBOSE) {
				if (KettleState.HEATING != priorState) {
					sb.append("start heating: ");
				} else {
					sb.append("still heating: ");
				}
				sb.append(String.format("%.2fC < %.0fC",
										currentTemp, lowThreshold));
				sb.append(" (target=");
				sb.append(STANDARD_TARGET_TEMPERATURE);
				sb.append("C - ");
				sb.append(this.hysteresis);
				sb.append("C).\n");
				this.traceMessage(sb.toString());
			}
		} else if (currentTemp > highThreshold) {
			// temperature is high enough, stop heating
			if (KettleState.HEATING == priorState) {
				this.actuatorOutboundPort.stopHeating();
				synchronized (this.stateLock) {
					this.currentState = KettleState.ON;
				}
				System.out.println(
						"[KETTLE CTRL] STOP heating: "
						+ String.format("%.2fC > %.0fC",
										currentTemp, highThreshold));
			}
			if (VERBOSE) {
				if (KettleState.HEATING == priorState) {
					sb.append("stop heating: ");
				} else {
					sb.append("still not heating: ");
				}
				sb.append(String.format("%.2fC > %.0fC",
										currentTemp, highThreshold));
				sb.append(" (target=");
				sb.append(STANDARD_TARGET_TEMPERATURE);
				sb.append("C + ");
				sb.append(this.hysteresis);
				sb.append("C).\n");
				this.traceMessage(sb.toString());
			}
		} else {
			// in the hysteresis band, maintain current state
			if (VERBOSE) {
				if (KettleState.HEATING == priorState) {
					sb.append("still heating: ");
				} else {
					sb.append("still not heating: ");
				}
				sb.append(String.format("%.2fC in [%.0fC, %.0fC]",
						currentTemp, lowThreshold, highThreshold));
				sb.append(".\n");
				this.traceMessage(sb.toString());
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
	 */
	protected void		pullControlLoop()
	{
		try {
			KettleState priorState = KettleState.OFF;
			synchronized (this.stateLock) {
				priorState = this.currentState;
			}
			if (priorState != KettleState.OFF) {
				// get the temperature data from the kettle
				KettleTemperatureSensorData tempData =
						this.sensorOutboundPort.temperaturePullSensor();
				double currentTemp = tempData.getTemperature();

				if (DEBUG) {
					this.traceMessage(
							"pull control step: state=" + priorState
							+ ", temp=" + String.format("%.2f", currentTemp)
							+ "C.\n");
				}

				this.oneControlStep(currentTemp, priorState);

				// schedule the next execution of the loop
				this.scheduleTask(
						o -> ((KettleController)o).pullControlLoop(),
						this.actualControlPeriod,
						TimeUnit.NANOSECONDS);
			} else {
				if (VERBOSE) {
					this.traceMessage("exit the control.\n");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
// -----------------------------------------------------------------------------
