package fr.sorbonne_u.components.hem2025e1.equipments.heater;

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
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterExternalControlJava4InboundPort;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterInternalControlInboundPort;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterUserJava4InboundPort;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;

// -----------------------------------------------------------------------------
/**
 * The class <code>Heater</code> implements a heater component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code currentState != null}
 * invariant	{@code targetTemperature != null && targetTemperature.getMeasurementUnit().equals(TEMPERATURE_UNIT)}
 * invariant	{@code targetTemperature.getData() >= MIN_TARGET_TEMPERATURE.getData() && targetTemperature.getData() <= MAX_TARGET_TEMPERATURE.getData()}
 * invariant	{@code currentPowerLevel == null || currentPowerLevel.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code currentPowerLevel == null || currentPowerLevel.getData() >= 0.0 && currentPowerLevel.getData() <= MAX_POWER_LEVEL.getData()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INTERNAL_CONTROL_INBOUND_PORT_URI != null && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code EXTERNAL_CONTROL_INBOUND_PORT_URI != null && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered={HeaterUserJava4CI.class,
							HeaterInternalControlCI.class,
							HeaterExternalControlJava4CI.class})
public class			Heater
extends		AbstractComponent
implements	HeaterUserI,
			HeaterInternalControlI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>HeaterState</code> describes the operation
	 * states of the heater.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Created on : 2021-09-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	HeaterState
	{
		/** heater is on, but not heating.									*/
		ON,
		/** heater is heating.												*/
		HEATING,
		/** heater is off.													*/
		OFF
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// BCM4Java information

	/** URI of the heater inbound port used in tests.						*/
	public static final String		REFLECTION_INBOUND_PORT_URI =
															"Heater-RIP-URI";	

	/** URI of the heater port for user interactions.						*/
	public static final String		USER_INBOUND_PORT_URI =
												"HEATER-USER-INBOUND-PORT-URI";
	/** URI of the heater port for internal control.						*/
	public static final String		INTERNAL_CONTROL_INBOUND_PORT_URI =
									"HEATER-INTERNAL-CONTROL-INBOUND-PORT-URI";
	/** URI of the heater port for internal control.						*/
	public static final String		EXTERNAL_CONTROL_INBOUND_PORT_URI =
									"HEATER-EXTERNAL-CONTROL-INBOUND-PORT-URI";


	/** inbound port offering the <code>HeaterUserCI</code> interface.		*/
	protected HeaterUserJava4InboundPort			hip;
	/** inbound port offering the <code>HeaterInternalControlCI</code>
	 *  interface.															*/
	protected HeaterInternalControlInboundPort		hicip;
	/** inbound port offering the <code>HeaterExternalControlCI</code>
	 *  interface.															*/
	protected HeaterExternalControlJava4InboundPort	hecip;

	/** when true, methods trace their actions.								*/
	public static boolean			VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int				X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int				Y_RELATIVE_POSITION = 0;

	// Appliance information
	
	/** standard target temperature for the heater in celsius.				*/
	protected static final Measure<Double>	STANDARD_TARGET_TEMPERATURE =
												new Measure<>(
														19.0,
														TEMPERATURE_UNIT);
	/** fake current temperature, used when testing without simulation. 	*/
	public static final SignalData<Double>	FAKE_CURRENT_TEMPERATURE =
												new SignalData<>(
													new Measure<>(
															10.0,
															TEMPERATURE_UNIT));

	/** current state (on, off) of the heater.								*/
	protected HeaterState						currentState;
	/**	current power level of the heater.									*/
	protected TimedMeasure<Double>				currentPowerLevel;
	/** target temperature for the heating.									*/
	protected TimedMeasure<Double>				targetTemperature;

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
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param h	instance to be tested.
	 * @return	true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(Heater h)
	{
		assert	h != null : new PreconditionException("h != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				h.currentState != null,
				Heater.class, h,
				"h.currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(
				h.targetTemperature.getData() >=
							MIN_TARGET_TEMPERATURE.getData() &&
					h.targetTemperature.getData() <=
								MAX_TARGET_TEMPERATURE.getData(),
				Heater.class, h,
				"targetTemperature.getData() >= MIN_TARGET_TEMPERATURE.getData() && "
				+ "targetTemperature.getData() <= MIN_TARGET_TEMPERATURE.getData()");
		ret &= AssertionChecking.checkImplementationInvariant(
				h.currentPowerLevel.getData() >= 0.0 &&
							h.currentPowerLevel.getData() <=
													MAX_POWER_LEVEL.getData(),
				Heater.class, h,
				"currentPowerLevel.getData() >= 0.0 && "
				+ "currentPowerLevel.getData() <= "
				+ "MAX_POWER_LEVEL.getData()");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= HeaterTemperatureI.staticInvariants();
		ret &= HeaterExternalControlI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				Heater.class,
				"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty(),
				Heater.class,
				"USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				INTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
				Heater.class,
				"INTERNAL_CONTROL_INBOUND_PORT_URI != null && "
							+ "!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
				Heater.class,
				"EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&"
							+ "!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				Heater.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				Heater.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param h	instance to be tested.
	 * @return	true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(Heater h)
	{
		assert	h != null : new PreconditionException("h != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected			Heater() throws Exception
	{
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI,
			 EXTERNAL_CONTROL_INBOUND_PORT_URI);
	}

	/**
	 * create a new heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			Heater(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI
		) throws Exception
	{
		super(1, 0);
		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI);
	}

	/**
	 * create a new heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI				URI of the reflection inbound port of the component.
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			Heater(
		String reflectionInboundPortURI,
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);

		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI);
	}

	/**
	 * initialise a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @throws Exception							<i>to do</i>.
	 */
	protected void		initialise(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI
		) throws Exception
	{
		assert	heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty();
		assert	heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty();
		assert	heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty();

		this.currentState = HeaterState.OFF;
		this.currentPowerLevel =
				new TimedMeasure<>(
						MAX_POWER_LEVEL.getData(),
						MAX_POWER_LEVEL.getMeasurementUnit());
		this.targetTemperature =
				new TimedMeasure<>(
						STANDARD_TARGET_TEMPERATURE.getData(),
						STANDARD_TARGET_TEMPERATURE.getMeasurementUnit());

		this.hip = new HeaterUserJava4InboundPort(heaterUserInboundPortURI, this);
		this.hip.publishPort();
		this.hicip = new HeaterInternalControlInboundPort(
									heaterInternalControlInboundPortURI, this);
		this.hicip.publishPort();
		this.hecip = new HeaterExternalControlJava4InboundPort(
									heaterExternalControlInboundPortURI, this);
		this.hecip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Heater component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();		
		}

		assert	Heater.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Heater.implementationInvariants(this)");
		assert	Heater.invariants(this) :
				new InvariantException("Heater.invariants(this)");
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
			this.hip.unpublishPort();
			this.hicip.unpublishPort();
			this.hecip.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#on()
	 */
	@Override
	public boolean		on() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its state: " +
											this.currentState + ".\n");
		}
		return this.currentState == HeaterState.ON ||
									this.currentState == HeaterState.HEATING;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#switchOn()
	 */
	@Override
	public void			switchOn() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater switches on.\n");
		}

		assert	!this.on() : new PreconditionException("!on()");

		this.currentState = HeaterState.ON;

		assert	 this.on() : new PostconditionException("on()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#switchOff()
	 */
	@Override
	public void			switchOff() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater switches off.\n");
		}

		assert	this.on() : new PreconditionException("on()");

		this.currentState = HeaterState.OFF;

		assert	 !this.on() : new PostconditionException("!on()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#setTargetTemperature(fr.sorbonne_u.alasca.physical_data.Measure)
	 */
	@Override
	public void			setTargetTemperature(Measure<Double> target)
	throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater sets a new target "
										+ "temperature: " + target + ".\n");
		}

		assert	target != null &&
						TEMPERATURE_UNIT.equals(target.getMeasurementUnit()) :
				new PreconditionException(
						"target != null && TEMPERATURE_UNIT.equals("
						+ "target.getMeasurementUnit())");
		assert	target.getData() >= MIN_TARGET_TEMPERATURE.getData() &&
						target.getData() <= MAX_TARGET_TEMPERATURE.getData() :
				new PreconditionException(
						"target.getData() >= MIN_TARGET_TEMPERATURE.getData() "
						+ "&& target.getData() <= MAX_TARGET_TEMPERATURE.getData()");

		this.targetTemperature =
				new TimedMeasure<Double>(target.getData(),
										 target.getMeasurementUnit());

		assert	getTargetTemperature().getMeasure().equals(target) :
				new PostconditionException(
						"getTargetTemperature().getMeasure().equals(target)");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI#getTargetTemperature()
	 */
	@Override
	public SignalData<Double>	getTargetTemperature()
	throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its target"
							+ " temperature " + this.targetTemperature + ".\n");
		}

		SignalData<Double> ret = new SignalData<Double>(this.targetTemperature);

		assert	ret != null && TEMPERATURE_UNIT.equals(
									ret.getMeasure().getMeasurementUnit()) :
				new PostconditionException(
						"return != null && TEMPERATURE_UNIT.equals("
						+ "return.getMeasure().getMeasurementUnit())");
		assert	ret.getMeasure().getData() >= MIN_TARGET_TEMPERATURE.getData() &&
					ret.getMeasure().getData() <= MAX_TARGET_TEMPERATURE.getData() :
				new PostconditionException(
						"return.getMeasure().getData() >= "
						+ "MIN_TARGET_TEMPERATURE.getData() "
						+ "&& return.getMeasure().getData() <= "
						+ "MAX_TARGET_TEMPERATURE.getData()");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI#getCurrentTemperature()
	 */
	@Override
	public SignalData<Double>	getCurrentTemperature()
	throws Exception
	{
		assert	this.on() : new PreconditionException("on()");

		// Temporary implementation; would need a temperature sensor.
		SignalData<Double> currentTemperature = FAKE_CURRENT_TEMPERATURE;
		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns the current"
							+ " temperature " + currentTemperature + ".\n");
		}

		return  currentTemperature;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlI#heating()
	 */
	@Override
	public boolean		heating() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its heating status " + 
						(this.currentState == HeaterState.HEATING) + ".\n");
		}

		assert	this.on() : new PreconditionException("on()");

		return this.currentState == HeaterState.HEATING;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlI#startHeating()
	 */
	@Override
	public void			startHeating() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater starts heating.\n");
		}
		assert	this.on() : new PreconditionException("on()");
		assert	!this.heating() : new PreconditionException("!heating()");

		this.currentState = HeaterState.HEATING;

		assert	this.heating() : new PostconditionException("heating()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlI#stopHeating()
	 */
	@Override
	public void			stopHeating() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater stops heating.\n");
		}
		assert	this.on() : new PreconditionException("on()");
		assert	this.heating() : new PreconditionException("heating()");

		this.currentState = HeaterState.ON;

		assert	!this.heating() : new PostconditionException("!heating()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI#getMaxPowerLevel()
	 */
	@Override
	public Measure<Double>	getMaxPowerLevel() throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its max power level " + 
					MAX_POWER_LEVEL + ".\n");
		}

		return MAX_POWER_LEVEL;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI#setCurrentPowerLevel(fr.sorbonne_u.alasca.physical_data.Measure)
	 */
	@Override
	public void			setCurrentPowerLevel(Measure<Double> powerLevel)
	throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater sets its power level to " + 
														powerLevel + ".\n");
		}

		assert	this.on() : new PreconditionException("on()");
		assert	powerLevel != null && powerLevel.getData() >= 0.0 &&
							powerLevel.getMeasurementUnit().equals(POWER_UNIT) :
				new PreconditionException(
						"powerLevel != null && powerLevel.getData() >= 0.0 && "
						+ "powerLevel.getMeasurementUnit().equals(POWER_UNIT)");

		if (powerLevel.getData() <= getMaxPowerLevel().getData()) {
			this.currentPowerLevel = new TimedMeasure<Double>(
											powerLevel.getData(),
											powerLevel.getMeasurementUnit());
		} else {
			this.currentPowerLevel = new TimedMeasure<Double>(
										MAX_POWER_LEVEL.getData(),
										MAX_POWER_LEVEL.getMeasurementUnit());
		}

		assert	powerLevel.getData() > getMaxPowerLevel().getData() ||
						getCurrentPowerLevel().getMeasure().getData() ==
														powerLevel.getData() :
				new PostconditionException(
						"powerLevel.getData() > getMaxPowerLevel().getData() "
						+ "|| getCurrentPowerLevel().getData() == "
						+ "powerLevel.getData()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI#getCurrentPowerLevel()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerLevel()
	throws Exception
	{
		if (Heater.VERBOSE) {
			this.traceMessage("Heater returns its current power level " + 
					this.currentPowerLevel + ".\n");
		}

		assert	this.on() : new PreconditionException("on()");

		SignalData<Double> ret =
				new SignalData<Double>(this.currentPowerLevel);

		assert	ret != null && ret.getMeasure().getMeasurementUnit().
															equals(POWER_UNIT) :
				new PreconditionException(
						"return != null && return.getMeasure()."
						+ "getMeasurementUnit().equals(POWER_UNIT)");
		assert	ret.getMeasure().getData() >= 0.0 &&
					ret.getMeasure().getData() <= getMaxPowerLevel().getData() :
				new PostconditionException(
							"return.getMeasure().getData() >= 0.0 && "
							+ "return.getMeasure().getData() <= "
							+ "getMaxPowerLevel().getData()");

		return ret;
	}
}
// -----------------------------------------------------------------------------
