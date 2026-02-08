package etape1.equipments.batteries;

import etape1.equipments.batteries.connections.BatteriesInboundPort;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.alasca.physical_data.SignalData;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;


// -----------------------------------------------------------------------------
/**
 * The class <code>Batteries</code> implements a component for the batteries.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code totalInPower.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code totalMaxOutPower.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code nominalCapacity != null && nominalCapacity.getData() > 0.0 && nominalCapacity.getMeasurementUnit().equals(CAPACITY_UNIT)}
 * invariant	{@code FAKE_CURRENT_CAPACITY.getData() <= nominalCapacity.getData()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MAX_OUT_POWER_PER_PARALLEL_UNIT != null && MAX_OUT_POWER_PER_PARALLEL_UNIT.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code IN_POWER_PER_PARALLEL_UNIT != null && IN_POWER_PER_PARALLEL_UNIT.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code FAKE_CURRENT_CAPACITY != null && FAKE_CURRENT_CAPACITY.getData() > 0.0 && FAKE_CURRENT_CAPACITY.getMeasurementUnit().equals(CAPACITY_UNIT)}
 * </pre>
 * 
 * <p>Created on : 2025-09-25</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered = {BatteriesCI.class})
public class			Batteries
extends		AbstractComponent
implements	BatteriesImplementationI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>State</code> defines the states in which the
	 * batteries and its charger can be.
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
	 * <p>Created on : 2025-10-13</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	State
	{
		/** batteries are charging.											*/
		CHARGING,
		/** batteries are delivering power.									*/
		PRODUCING,
		/** batteries are EMPTY.											*/
		EMPTY,
		/** batteries are neither charging nor delivering power.			*/
		IDLE
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** capacity of batteries unit in watts.								*/
	public static final Measure<Double>	CAPACITY_PER_UNIT =
													new Measure<Double>(
															5500.0,
															CAPACITY_UNIT);
	/**	maximal deliverable power per batteries cell put in parallel,
	 *  in the power unit used by the batteries.							*/
	public static final Measure<Double>	MAX_OUT_POWER_PER_CELL =
													new Measure<Double>(
															5500.0,
															POWER_UNIT);
	/**	power consumed per batteries cell when charging in parallel,
	 *  in the power unit used by the batteries.							*/
	public static final Measure<Double>	IN_POWER_PER_CELL =
													new Measure<Double>(
															1100.0,
															POWER_UNIT);

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** the standard inbound port URI when just one port is used.			*/
	public static final String			STANDARD_INBOUND_PORT_URI =
														"batteries-ibp-uri";
	/** value for current capacity used in tests.							*/
	public static final Measure<Double>	FAKE_CURRENT_CAPACITY =
														new Measure<Double>(
																5000.0,
																CAPACITY_UNIT);
	/** value for charge level used in tests.								*/
	public static final Measure<Double>	FAKE_CHARGE_LEVEL =
													new Measure<Double>(
															0.75,
															MeasurementUnit.RAW);
	/** total power consumed when charging in the power unit used by
	 *  the batteries.														*/
	protected final Measure<Double>	totalInPower;
	/** total maximal out power provided when discharging in the power
	 *  unit used by the batteries.	 										*/
	protected final Measure<Double>	totalMaximumOutPower;
	/** nominal capacity of the batteries <i>i.e.</i>, when brand new, in
	 *  the power unit used by the batteries.								*/
	protected final Measure<Double>	nominalCapacity;
	/** the current state of the batteries.	*/
	protected State					currentState;

	/** the inbound port offering the component interface
	 *  {@code BatteriesCI}.												*/
	protected BatteriesInboundPort	inboundPort;

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
	protected static boolean	implementationInvariants(Batteries instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.totalInPower.getMeasurementUnit().equals(POWER_UNIT),
				Batteries.class, instance,
				"totalInPower.getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.totalMaximumOutPower.getMeasurementUnit().equals(POWER_UNIT),
				Batteries.class, instance,
				"totalMaxOutPower.getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nominalCapacity != null &&
					instance.nominalCapacity.getData() > 0.0 &&
					instance.nominalCapacity.getMeasurementUnit().equals(
																CAPACITY_UNIT),
				Batteries.class, instance,
				"nominalCapacity != null && nominalCapacity.getData() > 0.0 && "
				+ "nominalCapacity.getMeasurementUnit().equals(CAPACITY_UNIT)");
		ret &= AssertionChecking.checkImplementationInvariant(
				FAKE_CURRENT_CAPACITY.getData() <= instance.nominalCapacity.getData(),
				Batteries.class, instance,
				"FAKE_CURRENT_CAPACITY.getData() <= nominalCapacity.getData()");
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
		ret &= AssertionChecking.checkStaticInvariant(
				MAX_OUT_POWER_PER_CELL != null &&
					MAX_OUT_POWER_PER_CELL.getMeasurementUnit().
															equals(POWER_UNIT),
				Batteries.class,
				"MAX_OUT_POWER_PER_CELL != null && "
				+ "MAX_OUT_POWER_PER_CELL.getMeasurementUnit()."
				+ "equals(POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				IN_POWER_PER_CELL != null &&
					IN_POWER_PER_CELL.getMeasurementUnit().
															equals(POWER_UNIT),
				Batteries.class,
				"IN_POWER_PER_CELL != null && "
				+ "IN_POWER_PER_CELL.getMeasurementUnit()."
				+ "equals(POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_CURRENT_CAPACITY != null &&
					FAKE_CURRENT_CAPACITY.getData() > 0.0 &&
					FAKE_CURRENT_CAPACITY.getMeasurementUnit().
														equals(CAPACITY_UNIT),
				Batteries.class,
				"FAKE_CURRENT_CAPACITY != null && FAKE_CURRENT_CAPACITY."
				+ "getData() > 0.0 && FAKE_CURRENT_CAPACITY.getMeasurementUnit()."
				+ "equals(CAPACITY_UNIT)");
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
	 * @param instance		instance to be tested.
	 * @return				true if the invariants are observed, false otherwise.
	 * @throws Exception	<i>to do</i>.
	 */
	protected static boolean	invariants(Batteries instance) throws Exception
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
	 * create a batteries component with one set of batteries in parallel,
	 * one set of one batteries in series, a generated URI for its reflection
	 * inbound port and the standard URI for the in bound port offering the
	 * {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 */
	protected			Batteries() throws Exception
	{
		this(1, 1);
	}

	/**
	 * create a batteries component with given number of sets of batteries in
	 * parallel and in series, a generated URI for its reflection inbound port
	 * and the standard URI for the in bound port offering the
	 * {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code numberOfCellsInParallel > 0}
	 * pre	{@code numberOfCellGroupsInSeries > 0}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param numberOfCellsInParallel		number of batteries unit put in parallel to have a higher input and output power.
	 * @param numberOfCellGroupsInSeries	number of sets of parallel batteries unit put in series to get more capacity.
	 */
	protected			Batteries(
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries
		) throws Exception
	{
		this(STANDARD_INBOUND_PORT_URI,
			 numberOfCellsInParallel,
			 numberOfCellGroupsInSeries);
	}

	/**
	 * create a batteries component with given number of sets of batteries in
	 * parallel and in series, the given URI for its reflection inbound port and
	 * the given URI for the in bound port offering the {@code BatteriesCI}
	 * component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code numberOfCellsInParallel > 0}
	 * pre	{@code numberOfCellGroupsInSeries > 0}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param inboundPortURI				URI of the inbound port offering the {@code BatteriesCI} component interface.
	 * @param numberOfCellsInParallel		number of batteries unit put in parallel to have a higher input and output power.
	 * @param numberOfCellGroupsInSeries	number of sets of parallel batteries unit put in series to get more capacity.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			Batteries(
		String inboundPortURI,
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries
		) throws Exception
	{
		super(1, 0);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException("inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	numberOfCellsInParallel > 0 :
				new PreconditionException("numberOfUnitsInParallel > 0");
		assert	numberOfCellGroupsInSeries > 0 :
				new PreconditionException("numberOfUnitGroupsInSeries > 0");

		this.nominalCapacity = new Measure<Double>(
										numberOfCellsInParallel *
											numberOfCellGroupsInSeries *
												CAPACITY_PER_UNIT.getData(),
										CAPACITY_PER_UNIT.getMeasurementUnit());
		this.totalInPower =
				new Measure<Double>(
						numberOfCellsInParallel *
										IN_POWER_PER_CELL.getData(),
						IN_POWER_PER_CELL.getMeasurementUnit());
		this.totalMaximumOutPower =
				new Measure<Double>(
						numberOfCellsInParallel *
									MAX_OUT_POWER_PER_CELL.getData(),
						MAX_OUT_POWER_PER_CELL.getMeasurementUnit());
		this.currentState = State.IDLE;
		this.inboundPort = new BatteriesInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();

		// Postconditions checking
		assert	nominalCapacity().equals(
					new Measure<Double>(
							numberOfCellsInParallel *
									numberOfCellGroupsInSeries *
											CAPACITY_PER_UNIT.getData(),
							CAPACITY_PER_UNIT.getMeasurementUnit())) :
				new PostconditionException(
						"nominalCapacity().equals(new Measure<Double>("
						+ "numberOfUnitsInParallel * numberOfUnitGroupsInSeries"
						+ " * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT."
						+ "getMeasurementUnit()))");

	
		if (VERBOSE) {
			this.tracer.get().setTitle("Batteries tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		// Invariant checking
		assert	Batteries.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Batteries.implementationInvariants(this)");
		assert	Batteries.invariants(this) :
				new InvariantException("Batteries.invariants(this)");
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
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * notify the batteries that they are currently producing power, a state in
	 * which they can go only if they are not charging and when some powder is
	 * required to be delivered.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !areCharging()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void			notifyProducing() throws Exception
	{
		assert	!this.areCharging() : new NeoSim4JavaException("!areCharging()");

		this.currentState = State.PRODUCING;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#nominalCapacity()
	 */
	@Override
	public Measure<Double>	nominalCapacity() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Batteries return its nominal capacity: "
							+ this.nominalCapacity);
		}
		return this.nominalCapacity;
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#currentCapacity()
	 */
	@Override
	public SignalData<Double>	currentCapacity() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Batteries return its current capacity: "
							+ FAKE_CURRENT_CAPACITY);
		}
		// temporary implementation, would need a physical sensor
		return new SignalData<>(FAKE_CURRENT_CAPACITY);
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#areCharging()
	 */
	@Override
	public boolean		areCharging() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Batteries return its charging state: "
							+ this.currentState.equals(State.CHARGING));
		}
		return this.currentState.equals(State.CHARGING);
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#areDischarging()
	 */
	@Override
	public boolean		areDischarging() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Batteries return its discharging state: "
							+ this.state.equals(State.PRODUCING));
		}
		return this.state.equals(State.PRODUCING);
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#chargeLevel()
	 */
	@Override
	public SignalData<Double>	chargeLevel() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Batteries return its charge level: "
							+ FAKE_CHARGE_LEVEL);
		}
		// temporary implementation, would need a physical sensor
		return new SignalData<>(FAKE_CHARGE_LEVEL);
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#getCurrentPowerConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerConsumption() throws Exception
	{
		// Preconditions checking
		assert	areCharging() : new PreconditionException("areCharging()");

		SignalData<Double> ret = new SignalData<>(this.totalInPower);

		// Postconditions checking
		assert	ret != null && ret.getMeasure().getData() > 0.0 &&
					ret.getMeasure().getMeasurementUnit().equals(POWER_UNIT) :
				new PostconditionException(
						"return != null && return.getMeasure().getData() > 0.0 "
						+ "&& return.getMeasure().getMeasurementUnit().equals("
						+ "POWER_UNIT)");

		if (VERBOSE) {
			this.logMessage("Batteries return its current power consumption: "
							+ ret);
		}

		return ret;
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#startCharging()
	 */
	@Override
	public void			startCharging() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Batteries start charging");
		}
		assert	!areCharging() && !areDischarging() &&
								chargeLevel().getMeasure().getData() < 1.0 :
				new PreconditionException(
						"!areCharging() && !areDischarging() && "
						+ "chargeLevel().getMeasure().getData() < 1.0");

		this.currentState = State.CHARGING;
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#stopCharging()
	 */
	@Override
	public void			stopCharging() throws Exception
	{
		if (VERBOSE) {
			this.logMessage("Batteries stop charging");
		}
		assert	areCharging() : new PreconditionException("areCharging()");

		this.currentState = State.IDLE;
	}
}
// -----------------------------------------------------------------------------
