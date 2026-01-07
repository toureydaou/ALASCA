package fr.sorbonne_u.components.hem2025e1.equipments.generator;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
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

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.hem2025e1.equipments.generator.connections.GeneratorInboundPort;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Generator</code> implements an electric generator component
 * for the Household Energy Management project.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code STANDARD_INBOUND_PORT_URI != null && !STANDARD_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code OUTPUT_AC_TENSION != null && (OUTPUT_AC_TENSION.equals(new Measure<Double>(110.0, TENSION_UNIT)) || OUTPUT_AC_TENSION.equals(new Measure<Double>(220.0, TENSION_UNIT)))}
 * invariant	{@code MIN_FUEL_CONSUMPTION != null && MIN_FUEL_CONSUMPTION.getData() > 0.0 && MIN_FUEL_CONSUMPTION.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
 * invariant	{@code MAX_FUEL_CONSUMPTION != null && MAX_FUEL_CONSUMPTION.getData() > 0.0 && MAX_FUEL_CONSUMPTION.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
 * invariant	{@code MIN_FUEL_CONSUMPTION.getData() <= MAX_FUEL_CONSUMPTION.getData()}
 * invariant	{@code TANK_CAPACITY != null && TANK_CAPACITY.getData() > 0.0 && TANK_CAPACITY.getMeasurementUnit().equals(CAPACITY_UNIT)}
 * invariant	{@code MAX_POWER != null && MAX_POWER.getData() > 0.0 && MAX_POWER.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code FAKE_CURRENT_POWER_LEVEL != null && FAKE_CURRENT_POWER_LEVEL.getData() > 0.0 && FAKE_CURRENT_POWER_LEVEL.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code FAKE_CURRENT_POWER_LEVEL.getData() <= MAX_POWER.getData()}
 * invariant	{@code FAKE_FUEL_CONSUMPTION != null && FAKE_FUEL_CONSUMPTION.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
 * invariant	{@code FAKE_FUEL_CONSUMPTION.getData() >= MIN_FUEL_CONSUMPTION.getData() && FAKE_FUEL_CONSUMPTION.getData() <= MAX_FUEL_CONSUMPTION.getData()}
 * invariant	{@code FAKE_CURRENT_TANK_LEVEL != null && FAKE_CURRENT_TANK_LEVEL.getData() >= 0.0 && FAKE_CURRENT_TANK_LEVEL.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
 * invariant	{@code FAKE_CURRENT_TANK_LEVEL.getData() <= TANK_CAPACITY.getData()}
 * </pre>
 * 
 * <p>Created on : 2025-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = {GeneratorCI.class})
public class			Generator
extends		AbstractComponent
implements	GeneratorImplementationI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> defines the states in which the
	 * generator can be.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Implementation Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p><strong>Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2025-10-17</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	State
	{
		OFF,
		TANK_EMPTY,
		IDLE,
		PRODUCING;
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** the standard inbound port URI when just one port is used.			*/
	public static final String			STANDARD_INBOUND_PORT_URI =
														"generator-ibp-uri";

	/** output AC tension of the generator.									*/
	public static final Measure<Double>	OUTPUT_AC_TENSION =
													new Measure<Double>(
															220.0,
															TENSION_UNIT);
	/** minimal fuel consumption of the generator when producing no
	 *  power.																*/
	public static final Measure<Double>	MIN_FUEL_CONSUMPTION =
													new Measure<Double>(
															4.0,
															CONSUMPTION_UNIT);
	/** maximal fuel consumption of the generator when producing no
	 *  power.																*/
	public static final Measure<Double>	MAX_FUEL_CONSUMPTION =
													new Measure<Double>(
															10.0,
															CONSUMPTION_UNIT);
	/** standard tank capacity.												*/
	protected static final Measure<Double>	TANK_CAPACITY =
													new Measure<Double>(
															40.0,
															CAPACITY_UNIT);
	/** standard maximum power level.										*/
	public static final Measure<Double>	MAX_POWER = new Measure<Double>(
															5500.0,
															POWER_UNIT);

	/** value for current power level used in tests.						*/
	public static final Measure<Double>		FAKE_CURRENT_POWER_LEVEL =
													new Measure<Double>(
															2000.0,
															POWER_UNIT);
	/** test value for fuel consumption of the generator.					*/
	public static final Measure<Double>	FAKE_FUEL_CONSUMPTION =
													new Measure<Double>(
															6.0,
															CONSUMPTION_UNIT);
	/** test value for the current tank level.								*/
	public static final Measure<Double>	FAKE_CURRENT_TANK_LEVEL =
													new Measure<Double>(
															20.0,
															CAPACITY_UNIT);

	/** maximum power in {@code POWER_UNIT}.								*/
	protected final Measure<Double>	maxPower;
	/** tank capacity in {@code CAPACITY_UNIT}.								*/
	protected final Measure<Double>	tankCapacity;
	/** minimal fuel consumption of the generator when producing no power,
	 *  in {@code CONSUMPTION_UNIT}.										*/
	protected final Measure<Double>	minFuelConsumption;
	/** maximal fuel consumption of the generator when producing the
	 *  maximum power, in {@code CONSUMPTION_UNIT}.							*/
	protected final Measure<Double>	maxFuelConsumption;

	/** current state of the generator.										*/
	protected State					currentState;

	/** the inbound port offering the component interface
	 *  {@code BatteriesCI}.												*/
	protected GeneratorInboundPort	inboundPort;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
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
	protected static boolean	implementationInvariants(Generator instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				true,
				Generator.class, instance,
				"");
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
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= GeneratorImplementationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				STANDARD_INBOUND_PORT_URI != null &&
										!STANDARD_INBOUND_PORT_URI.isEmpty(),
				Generator.class,
				"STANDARD_INBOUND_PORT_URI != null && "
				+ "!STANDARD_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				OUTPUT_AC_TENSION != null &&
					(OUTPUT_AC_TENSION.equals(new Measure<Double>(110.0, TENSION_UNIT)) ||
							OUTPUT_AC_TENSION.equals(new Measure<Double>(220.0, TENSION_UNIT))),
				Generator.class,
				"OUTPUT_AC_TENSION != null && (OUTPUT_AC_TENSION.equals("
				+ "new Measure<Double>(110.0, TENSION_UNIT)) || "
				+ "OUTPUT_AC_TENSION.equals(new Measure<Double>(220.0, "
				+ "TENSION_UNIT)))");
		ret &= AssertionChecking.checkStaticInvariant(
				MIN_FUEL_CONSUMPTION != null &&
					MIN_FUEL_CONSUMPTION.getData() > 0.0 &&
						MIN_FUEL_CONSUMPTION.getMeasurementUnit().equals(
															CONSUMPTION_UNIT),
				Generator.class,
				"MIN_FUEL_CONSUMPTION != null && MIN_FUEL_CONSUMPTION.getData()"
				+ " > 0.0 && MIN_FUEL_CONSUMPTION.getMeasurementUnit().equals("
				+ "CONSUMPTION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MAX_FUEL_CONSUMPTION != null &&
					MAX_FUEL_CONSUMPTION.getData() > 0.0 &&
						MAX_FUEL_CONSUMPTION.getMeasurementUnit().equals(
															CONSUMPTION_UNIT),
				Generator.class,
				"MAX_FUEL_CONSUMPTION != null && MAX_FUEL_CONSUMPTION.getData()"
				+ " > 0.0 && MAX_FUEL_CONSUMPTION.getMeasurementUnit().equals("
				+ "CONSUMPTION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MIN_FUEL_CONSUMPTION.getData() <= MAX_FUEL_CONSUMPTION.getData(),
				Generator.class,
				"MIN_FUEL_CONSUMPTION.getData() <= MAX_FUEL_CONSUMPTION.getData()");
		ret &= AssertionChecking.checkStaticInvariant(
				TANK_CAPACITY != null && TANK_CAPACITY.getData() > 0.0 &&
						TANK_CAPACITY.getMeasurementUnit().equals(CAPACITY_UNIT),
				Generator.class,
				"TANK_CAPACITY != null && TANK_CAPACITY.getData() > 0.0 && "
				+ "TANK_CAPACITY.getMeasurementUnit().equals(CAPACITY_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MAX_POWER != null && MAX_POWER.getData() > 0.0 &&
							MAX_POWER.getMeasurementUnit().equals(POWER_UNIT),
				Generator.class,
				"MAX_POWER != null && MAX_POWER.getData() > 0.0 && "
				+ "MAX_POWER.getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_CURRENT_POWER_LEVEL != null &&
					FAKE_CURRENT_POWER_LEVEL.getData() > 0.0 &&
						FAKE_CURRENT_POWER_LEVEL.getMeasurementUnit().equals(
																	POWER_UNIT),
				Generator.class,
				"FAKE_CURRENT_POWER_LEVEL != null && FAKE_CURRENT_POWER_LEVEL."
				+ "getData() > 0.0 && FAKE_CURRENT_POWER_LEVEL."
				+ "getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_CURRENT_POWER_LEVEL.getData() <= MAX_POWER.getData(),
				Generator.class,
				"FAKE_CURRENT_POWER_LEVEL.getData() <= MAX_POWER.getData()");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_FUEL_CONSUMPTION != null &&
					FAKE_FUEL_CONSUMPTION.getData() > 0.0 &&
						FAKE_FUEL_CONSUMPTION.getMeasurementUnit().equals(
															CONSUMPTION_UNIT),
				Generator.class,
				"FAKE_FUEL_CONSUMPTION != null && FAKE_FUEL_CONSUMPTION."
				+ "getMeasurementUnit().equals(CONSUMPTION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_FUEL_CONSUMPTION.getData() >= MIN_FUEL_CONSUMPTION.getData()
					&& FAKE_FUEL_CONSUMPTION.getData() <= MAX_FUEL_CONSUMPTION.getData(),
				Generator.class,
				"FAKE_FUEL_CONSUMPTION.getData() >= MIN_FUEL_CONSUMPTION."
				+ "getData() && FAKE_FUEL_CONSUMPTION.getData() <= "
				+ "MAX_FUEL_CONSUMPTION.getData()");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_CURRENT_TANK_LEVEL != null &&
					FAKE_CURRENT_TANK_LEVEL.getData() >= 0.0 &&
						FAKE_CURRENT_TANK_LEVEL.getMeasurementUnit().equals(
																CAPACITY_UNIT),
				Generator.class,
				"FAKE_CURRENT_TANK_LEVEL != null && FAKE_CURRENT_TANK_LEVEL."
				+ "getData() >= 0.0 && FAKE_CURRENT_TANK_LEVEL."
				+ "getMeasurementUnit().equals(CONSUMPTION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_CURRENT_TANK_LEVEL.getData() <= TANK_CAPACITY.getData(),
				Generator.class,
				"FAKE_CURRENT_TANK_LEVEL.getData() <= TANK_CAPACITY.getData()");
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
	protected static boolean	invariants(Generator instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a standard electric generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected			Generator() throws Exception
	{
		this(STANDARD_INBOUND_PORT_URI,
			 MAX_POWER,
			 TANK_CAPACITY,
			 MIN_FUEL_CONSUMPTION,
			 MAX_FUEL_CONSUMPTION);
	}

	/**
	 * create an electric generator with the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code power != null && power.getData() > 0.0 && power.getMeasurementUnit().equals(POWER_UNIT)}
	 * pre	{@code tankCapacity != null && tankCapacity.getData() > 0.0 && tankCapacity.getMeasurementUnit().equals(CAPACITY_UNIT)}
	 * pre	{@code minFuelConsumption != null && minFuelConsumption.getData() > 0.0 && minFuelConsumption.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
	 * pre	{@code maxFuelConsumption != null && maxFuelConsumption.getData() > 0.0 && maxFuelConsumption.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
	 * post	{@code !isRunning()}
	 * post	{@code power.equals(maxPowerProductionCapacity())}
	 * post	{@code tankCapacity.equals(tankCapacity())}
	 * post	{@code minFuelConsumption.equals(minFuelConsumption())}
	 * post	{@code maxFuelConsumption.equals(maxFuelConsumption())}
	 * </pre>
	 *
	 * @param power					maximum power.
	 * @param tankCapacity			tank capacity.
	 * @param minFuelConsumption	minimum fuel consumption.
	 * @param maxFuelConsumption	maximum fuel consumption.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			Generator(
		Measure<Double> power,
		Measure<Double> tankCapacity,
		Measure<Double> minFuelConsumption,
		Measure<Double> maxFuelConsumption
		) throws Exception
	{
		this(STANDARD_INBOUND_PORT_URI,
			 power, tankCapacity, minFuelConsumption, maxFuelConsumption);
	}

	/**
	 * create an electric generator with the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code power != null && power.getData() > 0.0 && power.getMeasurementUnit().equals(POWER_UNIT)}
	 * pre	{@code tankCapacity != null && tankCapacity.getData() > 0.0 && tankCapacity.getMeasurementUnit().equals(CAPACITY_UNIT)}
	 * pre	{@code minFuelConsumption != null && minFuelConsumption.getData() > 0.0 && minFuelConsumption.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
	 * pre	{@code maxFuelConsumption != null && maxFuelConsumption.getData() > 0.0 && maxFuelConsumption.getMeasurementUnit().equals(CONSUMPTION_UNIT)}
	 * post	{@code State.OFF.equals(getState())}
	 * post	{@code power.equals(maxPowerProductionCapacity())}
	 * post	{@code tankCapacity.equals(tankCapacity())}
	 * post	{@code minFuelConsumption.equals(minFuelConsumption())}
	 * post	{@code maxFuelConsumption.equals(maxFuelConsumption())}
	 * </pre>
	 *
	 * @param inboundPortURI		the inbound port offering the component interface {@code BatteriesCI}.
	 * @param power					maximum power.
	 * @param tankCapacity			tank capacity.
	 * @param minFuelConsumption	minimum fuel consumption.
	 * @param maxFuelConsumption	maximum fuel consumption.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			Generator(
		String inboundPortURI,
		Measure<Double> power,
		Measure<Double> tankCapacity,
		Measure<Double> minFuelConsumption,
		Measure<Double> maxFuelConsumption
		) throws Exception
	{
		super(1, 0);

		// Preconditions checking
		assert	power != null && power.getData() > 0.0 &&
								power.getMeasurementUnit().equals(POWER_UNIT) :
				new PreconditionException(
						"power != null && power.getData() > 0.0 && "
						+ "power.getMeasurementUnit().equals(POWER_UNIT)");
		assert	tankCapacity != null && tankCapacity.getData() > 0.0 &&
						tankCapacity.getMeasurementUnit().equals(CAPACITY_UNIT) :
				new PreconditionException(
						"tankCapacity != null && tankCapacity.getData() > 0.0 "
						+ "&& tankCapacity.getMeasurementUnit().equals(CAPACITY_UNIT)");
		assert	minFuelConsumption != null && minFuelConsumption.getData() > 0.0 &&
					minFuelConsumption.getMeasurementUnit().equals(CONSUMPTION_UNIT) :
				new PreconditionException(
						"minFuelConsumption != null && minFuelConsumption."
						+ "getData() > 0.0 && minFuelConsumption.getMeasurementUnit()."
						+ "equals(CONSUMPTION_UNIT)");
		assert	maxFuelConsumption != null && maxFuelConsumption.getData() > 0.0 &&
					maxFuelConsumption.getMeasurementUnit().equals(CONSUMPTION_UNIT) :
				new PreconditionException(
						"maxFuelConsumption != null && maxFuelConsumption."
						+ "getData() > 0.0 && maxFuelConsumption.getMeasurementUnit()."
						+ "equals(CONSUMPTION_UNIT)");

		this.currentState = State.OFF;
		this.maxPower = power;
		this.tankCapacity = tankCapacity;
		this.minFuelConsumption = minFuelConsumption;
		this.maxFuelConsumption = maxFuelConsumption;

		this.inboundPort = new GeneratorInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();

		// Postconditions checking
		assert	State.OFF.equals(this.getState()) :
				new PostconditionException("State.OFF.equals(getState())");
		assert	power.equals(maxPowerProductionCapacity()) :
				new PostconditionException("power.equals(maxPowerProductionCapacity())");
		assert	tankCapacity.equals(this.tankCapacity()) :
				new PostconditionException("tankCapacity.equals(tankCapacity())");
		assert	minFuelConsumption.equals(minFuelConsumption()) :
				new PostconditionException("minFuelConsumption.equals(minFuelConsumption())");
		assert	maxFuelConsumption.equals(maxFuelConsumption()) :
				new PostconditionException("maxFuelConsumption.equals(maxFuelConsumption())");
			

		if (VERBOSE) {
			this.tracer.get().setTitle("Generator component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		// Invariant checking
		assert	Generator.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Generator.implementationInvariants(this)");
		assert	Generator.invariants(this) :
				new InvariantException("Generator.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.inboundPort.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#getState()
	 */
	@Override
	public State		getState() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Generator returns its running status: "
							+ this.currentState);
		}

		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#nominalOutputTension()
	 */
	@Override
	public Measure<Double>	nominalOutputTension() throws Exception
	{
		Measure<Double> ret = OUTPUT_AC_TENSION;

		if (VERBOSE) {
			this.logMessage("Generator returns its nominal output tension: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getData() > 0.0 &&
								ret.getMeasurementUnit().equals(TENSION_UNIT) :
				new PostconditionException(
						"return != null && return.getData() > 0.0 && "
						+ "return.getMeasurementUnit().equals(TENSION_UNIT)");
		
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#tankCapacity()
	 */
	@Override
	public Measure<Double>	tankCapacity() throws Exception
	{
		Measure<Double> ret = this.tankCapacity;

		if (VERBOSE) {
			this.logMessage("Generator returns its tank capacity: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getData() > 0.0 &&
								ret.getMeasurementUnit().equals(CAPACITY_UNIT) :
				new PostconditionException(
						"return != null && return.getData() > 0.0 && "
						+ "return.getMeasurementUnit().equals(CAPACITY_UNIT)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#currentTankLevel()
	 */
	@Override
	public SignalData<Double>	currentTankLevel() throws Exception
	{
		// temporary implementation, would need a physical sensor
		SignalData<Double> ret = new SignalData<>(FAKE_CURRENT_TANK_LEVEL);

		if (VERBOSE) {
			this.logMessage("Generator returns its current tank level: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getMeasure().getData() > 0.0 &&
					ret.getMeasure().getMeasurementUnit().equals(CAPACITY_UNIT) :
				new PostconditionException(
						"return != null && return.getMeasure().getData() > 0.0 "
						+ "&& return.getMeasure().getMeasurementUnit().equals("
						+ "CAPACITY_UNIT)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#maxPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	maxPowerProductionCapacity() throws Exception
	{
		Measure<Double> ret = this.maxPower;

		if (VERBOSE) {
			this.logMessage("Generator returns its maximum power production: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getData() >= 0.0 &&
								ret.getMeasurementUnit().equals(POWER_UNIT) :
				new PostconditionException(
						"return != null && return.getData() >= 0.0 && "
						+ "return.getMeasurementUnit().equals(POWER_UNIT)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#currentPowerProduction()
	 */
	@Override
	public SignalData<Double>	currentPowerProduction() throws Exception
	{
		// temporary implementation, would need a physical sensor.
		SignalData<Double> ret = new SignalData<>(FAKE_CURRENT_POWER_LEVEL);

		if (VERBOSE) {
			this.logMessage("Generator returns its current power production: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getMeasure().getData() >= 0.0 &&
					ret.getMeasure().getMeasurementUnit().equals(POWER_UNIT) :
				new PostconditionException(
						"return != null && return.getMeasure().getData() >= 0.0"
						+ " && ret.getMeasure().getMeasurementUnit().equals("
						+ "POWER_UNIT)");
		assert	ret.getMeasure().getData() <=
									maxPowerProductionCapacity().getData() :
				new PostconditionException(
						"return.getMeasure().getData() <= "
						+ "maxPowerProductionCapacity().getData()");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#minFuelConsumption()
	 */
	@Override
	public Measure<Double>	minFuelConsumption() throws Exception
	{
		Measure<Double> ret = this.minFuelConsumption;

		if (VERBOSE) {
			this.logMessage("Generator returns its minimum fuel consumption: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getData() > 0.0 &&
							ret.getMeasurementUnit().equals(CONSUMPTION_UNIT) :
				new PostconditionException(
						"return != null && return.getData() > 0.0 && "
						+ "return.getMeasurementUnit().equals(CONSUMPTION_UNIT)");
		assert	ret.getData() <= maxFuelConsumption().getData() :
				new PreconditionException(
						"return.getData() <= maxFuelConsumption().getData()");
		
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#maxFuelConsumption()
	 */
	@Override
	public Measure<Double>	maxFuelConsumption() throws Exception
	{
		Measure<Double> ret = this.maxFuelConsumption;

		if (VERBOSE) {
			this.logMessage("Generator returns its maximum fuel consumption: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getData() > 0.0 &&
							ret.getMeasurementUnit().equals(CONSUMPTION_UNIT) :
				new PostconditionException(
						"return != null && return.getData() > 0.0 && "
						+ "return.getMeasurementUnit().equals(CONSUMPTION_UNIT)");
		
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#currentFuelConsumption()
	 */
	@Override
	public SignalData<Double>	currentFuelConsumption() throws Exception
	{
		// temporary implementation, would need a physical sensor.
		SignalData<Double> ret = new SignalData<>(FAKE_FUEL_CONSUMPTION);

		if (VERBOSE) {
			this.logMessage("Generator returns its current fuel consumption: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getMeasure().getMeasurementUnit().
													equals(CONSUMPTION_UNIT) :
				new PostconditionException(
						"return != null && return.getMeasurementUnit()."
						+ "equals(CONSUMPTION_UNIT)");
		assert	ret.getMeasure().getData() >= minFuelConsumption().getData() &&
					ret.getMeasure().getData() <= maxFuelConsumption().getData() :
				new PreconditionException(
						"return.getMeasure().getData() >= minFuelConsumption()."
						+ "getData() && return.getMeasure().getData() <= "
						+ "maxFuelConsumption().getData()");
		
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#startGenerator()
	 */
	@Override
	public void			startGenerator() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Generator starts running.");
		}

		// Preconditions checking
		assert	State.OFF.equals(getState()) :
				new PreconditionException("State.OFF.equals(getState())");

		this.currentState = State.IDLE;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.generator.GeneratorImplementationI#stopGenerator()
	 */
	@Override
	public void			stopGenerator() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Generator stops running.");
		}

		// Preconditions checking
		assert	!State.OFF.equals(getState()) :
				new PreconditionException("!State.OFF.equals(getState())");

		this.currentState = State.OFF;
	}
}
// -----------------------------------------------------------------------------
