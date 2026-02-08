package etape3.equipements.laundry;

import java.util.concurrent.TimeUnit;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryState;
import etape3.equipements.laundry.connections.connectors.LaundryActuatorConnector;
import etape3.equipements.laundry.connections.connectors.LaundrySensorDataConnector;
import etape3.equipements.laundry.connections.ports.LaundryActuatorOutboundPort;
import etape3.equipements.laundry.connections.ports.LaundrySensorDataOutboundPort;
import etape3.equipements.laundry.sensor_data.LaundryStateSensorData;
import etape3.equipements.laundry.sensor_data.WashProgressSensorData;
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
 * The class <code>LaundryController</code> implements a controller component
 * for the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This controller monitors the laundry machine wash cycle using pull-based
 * sensor data. When the laundry machine is switched on, the controller
 * begins periodic monitoring. It logs wash progress and can stop the
 * wash if necessary.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
@RequiredInterfaces(required={
	LaundrySensorDataCI.LaundrySensorRequiredPullCI.class,
	LaundryActuatorCI.class,
	ClocksServerWithSimulationCI.class})
@OfferedInterfaces(offered={DataRequiredCI.PushCI.class})
public class LaundryController
extends		AbstractComponent
implements	LaundryPushImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	public static int X_RELATIVE_POSITION = 0;
	public static int Y_RELATIVE_POSITION = 0;
	public static boolean VERBOSE = true;
	public static boolean DEBUG = true;

	public static final double STANDARD_CONTROL_PERIOD = 60.0;

	protected String sensorIBP_URI;
	protected String actuatorIBP_URI;
	protected LaundrySensorDataOutboundPort sensorOutboundPort;
	protected LaundryActuatorOutboundPort actuatorOutboundPort;

	protected double controlPeriod;
	protected long actualControlPeriod;
	protected LaundryState currentState;
	protected Object stateLock;
	protected ExecutionMode executionMode;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	protected LaundryController(
		String sensorIBP_URI,
		String actuatorIBP_URI
	) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI, STANDARD_CONTROL_PERIOD);
	}

	protected LaundryController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double controlPeriod
	) throws Exception
	{
		super(3, 1);

		this.initialise(sensorIBP_URI, actuatorIBP_URI, controlPeriod);
		this.executionMode = ExecutionMode.STANDARD;
		this.actualControlPeriod =
			(long)(this.controlPeriod * TimeUnit.SECONDS.toNanos(1));
	}

	protected LaundryController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double controlPeriod,
		ExecutionMode executionMode,
		double accelerationFactor
	) throws Exception
	{
		super(3, 1);

		assert sensorIBP_URI != null && !sensorIBP_URI.isEmpty()
			: new PreconditionException(
					"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		assert actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()
			: new PreconditionException(
					"actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()");
		assert controlPeriod > 0
			: new PreconditionException("controlPeriod > 0");
		assert executionMode.isSimulationTest()
			: new PreconditionException("executionMode.isSimulationTest()");
		assert accelerationFactor > 0.0
			: new PreconditionException("accelerationFactor > 0.0");

		this.initialise(sensorIBP_URI, actuatorIBP_URI, controlPeriod);
		this.executionMode = executionMode;
		this.actualControlPeriod =
			(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1))
													/ accelerationFactor);

		if (this.actualControlPeriod < TimeUnit.MILLISECONDS.toNanos(10)) {
			System.out.println(
				"Warning: accelerated control period is too small ("
				+ this.actualControlPeriod
				+ "), unexpected scheduling problems may occur!");
		}

		if (VERBOSE || DEBUG) {
			this.tracer.get().setTitle("Laundry controller component");
			this.tracer.get().setRelativePosition(
				X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	protected void initialise(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double controlPeriod
	) throws Exception
	{
		this.sensorIBP_URI = sensorIBP_URI;
		this.actuatorIBP_URI = actuatorIBP_URI;
		this.controlPeriod = controlPeriod;
		this.stateLock = new Object();

		this.sensorOutboundPort = new LaundrySensorDataOutboundPort(this);
		this.sensorOutboundPort.publishPort();
		this.actuatorOutboundPort = new LaundryActuatorOutboundPort(this);
		this.actuatorOutboundPort.publishPort();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public synchronized void start() throws ComponentStartException {
		super.start();

		try {
			this.doPortConnection(
				this.sensorOutboundPort.getPortURI(),
				sensorIBP_URI,
				LaundrySensorDataConnector.class.getCanonicalName());
			this.doPortConnection(
				this.actuatorOutboundPort.getPortURI(),
				this.actuatorIBP_URI,
				LaundryActuatorConnector.class.getCanonicalName());

			synchronized (this.stateLock) {
				this.currentState = LaundryState.OFF;
			}

			if (VERBOSE) {
				this.traceMessage("Laundry controller starts.\n");
			}
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	@Override
	public synchronized void finalise() throws Exception {
		if (VERBOSE) {
			this.traceMessage("Laundry controller ends.\n");
		}
		this.doPortDisconnection(this.sensorOutboundPort.getPortURI());
		this.doPortDisconnection(this.actuatorOutboundPort.getPortURI());
		super.finalise();
	}

	@Override
	public synchronized void shutdown() throws ComponentShutdownException {
		try {
			this.sensorOutboundPort.unpublishPort();
			this.actuatorOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Push callbacks (LaundryPushImplementationI)
	// -------------------------------------------------------------------------

	@Override
	public void processLaundryState(LaundryState laundryState) {
		assert laundryState != null;

		if (DEBUG) {
			this.traceMessage("receives laundry state: "
				+ laundryState + ".\n");
		}

		synchronized (this.stateLock) {
			LaundryState oldState = this.currentState;
			this.currentState = laundryState;

			if (laundryState != LaundryState.OFF
				&& oldState == LaundryState.OFF) {
				if (VERBOSE) {
					this.traceMessage("start pull control.\n");
				}
				if (this.executionMode.isStandard()
					|| this.executionMode.isTestWithoutSimulation()) {
					this.pullControlLoop();
				} else {
					this.scheduleTaskOnComponent(
						new AbstractComponent.AbstractTask() {
							@Override
							public void run() {
								((LaundryController)
									this.getTaskOwner()).pullControlLoop();
							}
						},
						this.actualControlPeriod,
						TimeUnit.NANOSECONDS);
				}
			}
		}
	}

	@Override
	public void processWashProgress(WashProgressSensorData washProgress) {
		assert washProgress != null;

		if (DEBUG) {
			this.traceMessage("receives wash progress: waterTemp="
				+ washProgress.getWaterTemperature()
				+ "C, waterLevel=" + washProgress.getWaterLevel()
				+ "L, spinSpeed=" + washProgress.getSpinSpeed()
				+ ", power=" + washProgress.getPowerConsumption() + "W.\n");
		}
	}

	@Override
	public void processWashCycleComplete(boolean success) {
		if (VERBOSE) {
			this.traceMessage("wash cycle complete: success="
				+ success + ".\n");
		}
		synchronized (this.stateLock) {
			this.currentState = LaundryState.ON;
		}
	}

	// -------------------------------------------------------------------------
	// Control loop
	// -------------------------------------------------------------------------

	protected void pullControlLoop() {
		try {
			LaundryState priorState = LaundryState.OFF;
			synchronized (this.stateLock) {
				priorState = this.currentState;
			}
			if (priorState != LaundryState.OFF) {
				LaundryStateSensorData stateData =
					this.sensorOutboundPort.statePullSensor();

				if (DEBUG) {
					this.traceMessage("pull control step: state="
						+ stateData.getState() + ".\n");
				}

				this.oneControlStep(stateData, priorState);

				if (VERBOSE) {
					this.traceMessage("scheduling next pull in "
						+ this.actualControlPeriod + " ns.\n");
				}
				this.scheduleTask(
					o -> ((LaundryController)o).pullControlLoop(),
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

	protected void oneControlStep(
		LaundryStateSensorData stateData,
		LaundryState priorState
	) throws Exception
	{
		LaundryState currentLaundryState = stateData.getState();

		if (VERBOSE) {
			this.traceMessage("monitoring: priorState=" + priorState
				+ ", currentState=" + currentLaundryState + ".\n");
		}

		synchronized (this.stateLock) {
			this.currentState = currentLaundryState;
		}
	}
}
// -----------------------------------------------------------------------------
