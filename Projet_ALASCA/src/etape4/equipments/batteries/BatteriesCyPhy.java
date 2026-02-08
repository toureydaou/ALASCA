package etape4.equipments.batteries;

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
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.annotations.LocalArchitecture;
import fr.sorbonne_u.components.cyphy.annotations.SIL_Simulation_Architectures;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import etape1.equipments.batteries.Batteries.State;
import etape1.equipments.batteries.BatteriesCI;
import etape1.equipments.batteries.BatteriesImplementationI;
import etape1.equipments.batteries.connections.BatteriesInboundPort;
import etape2.equipments.batteries.mil.BatteriesSimulationConfiguration;
import etape4.equipments.batteries.sil.BatteriesStateSILModel;
import etape4.equipments.batteries.sil.Local_SIL_SimulationArchitectures;
import etape4.equipments.batteries.sil.events.SIL_StartCharging;
import etape4.equipments.batteries.sil.events.SIL_StopCharging;
import etape4.equipments.batteries.sil.events.CurrentBatteriesLevel;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

// -----------------------------------------------------------------------------
/**
 * The class <code>BatteriesCyPhy</code> implements a cyber-physical component
 * for the batteries.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code totalInPower.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code totalMaxOutPower.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code nominalCapacity != null && nominalCapacity.getData() > 0.0 && nominalCapacity.getMeasurementUnit().equals(CAPACITY_UNIT)}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code MAX_OUT_POWER_PER_PARALLEL_UNIT != null && MAX_OUT_POWER_PER_PARALLEL_UNIT.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code IN_POWER_PER_PARALLEL_UNIT != null && IN_POWER_PER_PARALLEL_UNIT.getMeasurementUnit().equals(POWER_UNIT)}
 * </pre>
 * 
 * <p>Created on : 2025-12-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@SIL_Simulation_Architectures({
	// fragile: fields in annotations cannot be defined by a class constant due
	// to Java annotations field initialisers limited to static values only
	@LocalArchitecture(
		// must be equal to UNIT_TEST_ARCHITECTURE_URI
		uri = "silUnitTests",
		// must be equal to the URI of the instance of BatteriesCoupledModel
		rootModelURI = "BatteriesCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents()
		),
	@LocalArchitecture(
		// must be equal to INTEGRATION_TEST_ARCHITECTURE_URI
		uri = "silIntegrationTests",
		// must be equal to the URI of the instance of BatteriesStateSILModel
		rootModelURI = "BatteriesStateSILModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents =
			@ModelExternalEvents(
				imported = {SIL_StartCharging.class,
							SIL_StopCharging.class,
							CurrentBatteriesLevel.class},
				exported = {SIL_StartCharging.class,
							SIL_StopCharging.class}
				)
		)
	})
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered = {BatteriesCI.class})
// -----------------------------------------------------------------------------
public class			BatteriesCyPhy
extends		AbstractCyPhyComponent
implements	BatteriesImplementationI
{
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

	/** standard URI of the batteries reflection inbound port.				*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
														"batteries-rip-uri";	
	/** the standard inbound port URI when just one port is used.			*/
	public static final String			STANDARD_INBOUND_PORT_URI =
														"batteries-ibp-uri";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String			UNIT_TEST_ARCHITECTURE_URI =
														"silUnitTests";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String			INTEGRATION_TEST_ARCHITECTURE_URI =
														"silIntegrationTests";

	/** total power consumed when charging in the power unit used by
	 *  the batteries.														*/
	protected Measure<Double>			totalInPower;
	/** total maximal out power provided when discharging in the power
	 *  unit used by the batteries.	 										*/
	protected Measure<Double>			totalMaximumOutPower;
	/** nominal capacity of the batteries <i>i.e.</i>, when brand new, in
	 *  the power unit used by the batteries.								*/
	protected Measure<Double>			nominalCapacity;
	/** the current state of the batteries.	*/
	protected State						currentState;

	/** the inbound port offering the component interface
	 *  {@code BatteriesCI}.												*/
	protected BatteriesInboundPort		inboundPort;

	// Execution/Simulation

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** one thread for the method execute, which starts the local SIL
	 *  simulator, and one to answer the calls to the component services.	*/
	protected static int				NUMBER_OF_STANDARD_THREADS = 2;
	/** no need for statically defined schedulable threads.					*/
	protected static int				NUMBER_OF_SCHEDULABLE_THREADS = 0;

	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin		asp;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture or the empty string if the component does
	 *  not execute as a simulation.										*/
	protected final String				localArchitectureURI;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected final double				accelerationFactor;

	/** name used to access the value of the charge level in the
	 *  simulator when executing software-in-the-loop test.				*/
	public static final String			CHARGE_LEVEL = "charge-level";

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
	protected static boolean	implementationInvariants(BatteriesCyPhy instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.totalInPower.getMeasurementUnit().equals(POWER_UNIT),
				BatteriesCyPhy.class, instance,
				"totalInPower.getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.totalMaximumOutPower.getMeasurementUnit().equals(POWER_UNIT),
				BatteriesCyPhy.class, instance,
				"totalMaxOutPower.getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nominalCapacity != null &&
					instance.nominalCapacity.getData() > 0.0 &&
					instance.nominalCapacity.getMeasurementUnit().equals(
																CAPACITY_UNIT),
				BatteriesCyPhy.class, instance,
				"nominalCapacity != null && nominalCapacity.getData() > 0.0 && "
				+ "nominalCapacity.getMeasurementUnit().equals(CAPACITY_UNIT)");
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
				BatteriesCyPhy.class,
				"MAX_OUT_POWER_PER_CELL != null && "
				+ "MAX_OUT_POWER_PER_CELL.getMeasurementUnit()."
				+ "equals(POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				IN_POWER_PER_CELL != null &&
					IN_POWER_PER_CELL.getMeasurementUnit().equals(POWER_UNIT),
				BatteriesCyPhy.class,
				"IN_POWER_PER_CELL != null && "
				+ "IN_POWER_PER_CELL.getMeasurementUnit()."
				+ "equals(POWER_UNIT)");
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
	protected static boolean	invariants(BatteriesCyPhy instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create a batteries component for standard execution with one set of
	 * batteries in parallel, one set of one batteries in series, a generated
	 * URI for its reflection inbound port and the standard URI for the inbound
	 * port offering the {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code getExecutionMode().isStandard()}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 */
	protected			BatteriesCyPhy() throws Exception
	{
		this(1, 1);
	}

	/**
	 * create a batteries component for standard execution with given number of
	 * sets of batteries in parallel and in series, a generated URI for its
	 * reflection inbound port and the standard URI for the inbound port
	 * offering the {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code numberOfCellsInParallel > 0}
	 * pre	{@code numberOfCellGroupsInSeries > 0}
	 * post	{@code getExecutionMode().isStandard()}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param numberOfCellsInParallel		number of batteries unit put in parallel to have a higher input and output power.
	 * @param numberOfCellGroupsInSeries	number of sets of parallel batteries unit put in series to get more capacity.
	 */
	protected			BatteriesCyPhy(
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI,
			 STANDARD_INBOUND_PORT_URI,
			 numberOfCellsInParallel,
			 numberOfCellGroupsInSeries);
	}

	/**
	 * create a batteries component for standard execution with given number of
	 * sets of batteries in parallel and in series, the given URI for its
	 * reflection inbound port and the given URI for the inbound port offering
	 * the {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code numberOfCellsInParallel > 0}
	 * pre	{@code numberOfCellGroupsInSeries > 0}
	 * post	{@code getExecutionMode().isStandard()}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection inbound port of the component.
	 * @param inboundPortURI				URI of the inbound port offering the {@code BatteriesCI} component interface.
	 * @param numberOfCellsInParallel		number of batteries unit put in parallel to have a higher input and output power.
	 * @param numberOfCellGroupsInSeries	number of sets of parallel batteries unit put in series to get more capacity.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			BatteriesCyPhy(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	numberOfCellsInParallel > 0 :
				new PreconditionException("numberOfUnitsInParallel > 0");
		assert	numberOfCellGroupsInSeries > 0 :
				new PreconditionException("numberOfUnitGroupsInSeries > 0");

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(inboundPortURI,
						numberOfCellsInParallel,
						numberOfCellGroupsInSeries);

		// Invariant checking
		assert	BatteriesCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BatteriesCyPhy.implementationInvariants(this)");
		assert	BatteriesCyPhy.invariants(this) :
				new InvariantException("BatteriesCyPhy.invariants(this)");
	}

	// Tests without simulation execution

	/**
	 * create a batteries component for test without simulation execution with
	 * one set of batteries in parallel and one in series, standard URI for its
	 * reflection inbound port and inbound port offering the {@code BatteriesCI}
	 * component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param executionMode					execution mode for the next run.
	 * @param clockURI						URI of the clock used to synchronise the test scenario.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			BatteriesCyPhy(
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI,
			 STANDARD_INBOUND_PORT_URI,
			 1,
			 1,
			 executionMode,
			 clockURI);
	}

	/**
	 * create a batteries component for test without simulation execution with
	 * given number of sets of batteries in parallel and in series, standard
	 * URI for its reflection inbound port and inbound port offering the
	 * {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code numberOfCellsInParallel > 0}
	 * pre	{@code numberOfCellGroupsInSeries > 0}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param numberOfCellsInParallel		number of batteries unit put in parallel to have a higher input and output power.
	 * @param numberOfCellGroupsInSeries	number of sets of parallel batteries unit put in series to get more capacity.
	 * @param executionMode					execution mode for the next run.
	 * @param clockURI						URI of the clock used to synchronise the test scenario.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			BatteriesCyPhy(
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI,
			 STANDARD_INBOUND_PORT_URI,
			 numberOfCellsInParallel,
			 numberOfCellGroupsInSeries,
			 executionMode,
			 clockURI);
	}

	/**
	 * create a batteries component for test without simulation execution with
	 * given number of sets of batteries in parallel and in series, the given
	 * URI for its reflection inbound port and the given URI for the inbound
	 * port offering the {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code numberOfCellsInParallel > 0}
	 * pre	{@code numberOfCellGroupsInSeries > 0}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection inbound port of the component.
	 * @param inboundPortURI				URI of the inbound port offering the {@code BatteriesCI} component interface.
	 * @param numberOfCellsInParallel		number of batteries unit put in parallel to have a higher input and output power.
	 * @param numberOfCellGroupsInSeries	number of sets of parallel batteries unit put in series to get more capacity.
	 * @param executionMode					execution mode for the next run.
	 * @param clockURI						URI of the clock used to synchronise the test scenario.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			BatteriesCyPhy(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  clockURI);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	numberOfCellsInParallel > 0 :
				new PreconditionException("numberOfUnitsInParallel > 0");
		assert	numberOfCellGroupsInSeries > 0 :
				new PreconditionException("numberOfUnitGroupsInSeries > 0");

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(inboundPortURI,
						numberOfCellsInParallel,
						numberOfCellGroupsInSeries);

		// Invariant checking
		assert	BatteriesCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BatteriesCyPhy.implementationInvariants(this)");
		assert	BatteriesCyPhy.invariants(this) :
				new InvariantException("BatteriesCyPhy.invariants(this)");
	}

	// Tests with simulation

	/**
	 * create a batteries component for test without simulation execution with
	 * given number of sets of batteries in parallel and in series, the given
	 * URI for its reflection inbound port and the given URI for the inbound
	 * port offering the {@code BatteriesCI} component interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code numberOfCellsInParallel > 0}
	 * pre	{@code numberOfCellGroupsInSeries > 0}
	 * pre	{@code executionMode != null && executionMode.isSimulationTest()}
	 * pre	{@code testScenario != null}
	 * pre	{@code localArchitectureURI != null && !localArchitectureURI.isEmpty()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * post	{@code nominalCapacity().equals(new Measure<Double>(numberOfUnitsInParallel * numberOfUnitGroupsInSeries * CAPACITY_PER_UNIT.getData(), CAPACITY_PER_UNIT.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection inbound port of the component.
	 * @param inboundPortURI				URI of the inbound port offering the {@code BatteriesCI} component interface.
	 * @param numberOfCellsInParallel		number of batteries unit put in parallel to have a higher input and output power.
	 * @param numberOfCellGroupsInSeries	number of sets of parallel batteries unit put in series to get more capacity.
	 * @param executionMode					execution mode for the next run.
	 * @param testScenario					test scenario to be executed with this component.
	 * @param localArchitectureURI			URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param accelerationFactor			acceleration factor for the simulation.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			BatteriesCyPhy(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries,
		ExecutionMode executionMode,
		TestScenario testScenario,
		String localArchitectureURI,
		double accelerationFactor
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null,
				testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario,
			  ((Supplier<Set<String>>)() ->
			  		{ HashSet<String> hs = new HashSet<>();
			  		  hs.add(UNIT_TEST_ARCHITECTURE_URI);
			  		  hs.add(INTEGRATION_TEST_ARCHITECTURE_URI);
			  		  return hs;
			  		}).get(),
			  accelerationFactor);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	numberOfCellsInParallel > 0 :
				new PreconditionException("numberOfUnitsInParallel > 0");
		assert	numberOfCellGroupsInSeries > 0 :
				new PreconditionException("numberOfUnitGroupsInSeries > 0");

		this.localArchitectureURI = localArchitectureURI;
		this.accelerationFactor = accelerationFactor;

		this.initialise(inboundPortURI,
						numberOfCellsInParallel,
						numberOfCellGroupsInSeries);

		// Invariant checking
		assert	BatteriesCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BatteriesCyPhy.implementationInvariants(this)");
		assert	BatteriesCyPhy.invariants(this) :
				new InvariantException("BatteriesCyPhy.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Initialisation methods
	// -------------------------------------------------------------------------

	/**
	 * initialise the batteries.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
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
	protected void		initialise(
		String inboundPortURI,
		int numberOfCellsInParallel,
		int numberOfCellGroupsInSeries
		) throws Exception
	{
		this.nominalCapacity =
				new Measure<Double>(
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
			this.tracer.get().setTitle("Batteries component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent#createLocalSimulationArchitecture(java.lang.String, java.lang.String, java.util.concurrent.TimeUnit, double)
	 */
	@Override
	protected RTArchitecture	createLocalSimulationArchitecture(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		// Preconditions checking
		assert	architectureURI != null && !architectureURI.isEmpty() :
				new PreconditionException(
						"architectureURI != null && !architectureURI.isEmpty()");
		assert	rootModelURI != null && !rootModelURI.isEmpty() :
				new PreconditionException(
						"rootModelURI != null && !rootModelURI.isEmpty()");
		assert	simulatedTimeUnit != null :
				new PreconditionException("simulatedTimeUnit != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		RTArchitecture ret = null;
		if (architectureURI.equals(UNIT_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createBatteriesSIL_Architecture4UnitTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createBatteriesSIL_Architecture4IntegrationTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else {
			throw new BCMException("Unknown local simulation architecture "
								   + "URI: " + architectureURI);
		}
		
		return ret;
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		// Invariant checking
		assert	BatteriesCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BatteriesCyPhy.implementationInvariants(this)");
		assert	BatteriesCyPhy.invariants(this) :
				new InvariantException("BatteriesCyPhy.invariants(this)");

		// create the simulation plug-in given the current type of simulation
		// and its local architecture i.e., for the current execution
		try {
			switch (this.getExecutionMode()) {
			case STANDARD:
				break;
			case UNIT_TEST:
			case INTEGRATION_TEST:
				break;
			case UNIT_TEST_WITH_SIL_SIMULATION:
			case INTEGRATION_TEST_WITH_SIL_SIMULATION:
				RTArchitecture architecture =
					(RTArchitecture) this.localSimulationArchitectures.
												get(this.localArchitectureURI);
				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;
					/**
					 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
					 */
					@Override
					public Double	getModelStateValue(
						String modelURI,
						String name
						) throws Exception
					{
						assert	modelURI.equals(BatteriesStateSILModel.URI);
						assert	name.equals(CHARGE_LEVEL);

						return ((BatteriesStateSILModel)
									this.atomicSimulators.get(modelURI).
														getSimulatedModel()).
										getModelStateValue(modelURI, name);
					}
				};
				((RTAtomicSimulatorPlugin)this.asp).
								setPluginURI(architecture.getRootModelURI());
				((RTAtomicSimulatorPlugin)this.asp).
										setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				// the simulator inside the plug-in is created
				this.asp.createSimulator();
				break;
			case UNIT_TEST_WITH_HIL_SIMULATION:
			case INTEGRATION_TEST_WITH_HIL_SIMULATION:
				throw new BCMException("HIL simulation not implemented yet!");
			default:
			}		
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}		

		// Invariant checking
		assert	BatteriesCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BatteriesCyPhy.implementationInvariants(this)");
		assert	BatteriesCyPhy.invariants(this) :
				new InvariantException("BatteriesCyPhy.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.traceMessage("BatteriesCyPhy executes.\n");

		// Invariant checking
		assert	BatteriesCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"BatteriesCyPhy.implementationInvariants(this)");
		assert	BatteriesCyPhy.invariants(this) :
				new InvariantException("BatteriesCyPhy.invariants(this)");

		switch (this.getExecutionMode()) {
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI,
								 this.clockURI);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
			// First, the component must synchronise with other components
			// to start the execution of the test scenario; we use a
			// time-triggered synchronisation scheme with the accelerated clock
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			// to prepare for the run, set the run parameters
			this.asp.setSimulationRunParameters(
					(TestScenarioWithSimulation) this.testScenario,
					new HashMap<>());
			this.asp.initialiseSimulation(
						this.getClock4Simulation().getSimulatedStartTime(),
						this.getClock4Simulation().getSimulatedDuration());
			// schedule the start of the SIL (real time) simulation
			this.asp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(
							this.getClock4Simulation().getStartEpochNanos()),
					this.getClock4Simulation().getSimulatedStartTime().
														getSimulatedTime(),
					this.getClock4Simulation().getSimulatedDuration().
														getSimulatedDuration());
			// wait until the simulation ends
			this.getClock4Simulation().waitUntilEnd();
			// give some time for the end of simulation catering tasks
			Thread.sleep(200L);
			// get and print the simulation report
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
		default:
		}		
	}

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
	 * notify the batteries of their new state as computed by the simulation
	 * models.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @param newState	new State of the batteries.
	 */
	public void			notifyState(State newState)
	{
		this.currentState = newState;
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
		// temporary implementation, would need a physical sensor
		SignalData<Double> ret = new SignalData<>(this.nominalCapacity);

		if (VERBOSE) {
			this.logMessage("Batteries return its current capacity: " + ret);
		}

		return ret;
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
							+ this.currentState.equals(State.PRODUCING));
		}

		return this.currentState.equals(State.PRODUCING);
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#chargeLevel()
	 */
	@Override
	public SignalData<Double>	chargeLevel() throws Exception
	{
		SignalData<Double> ret;
		if (this.getExecutionMode().isSILTest()) {
			// getting the current value from the SIL simulation model
			double chargeLevelRatio =
					(double) this.asp.getModelStateValue(
										BatteriesStateSILModel.URI,
										CHARGE_LEVEL);
			ret = new SignalData<Double>(
							this.getClock4Simulation(),
							new TimedMeasure<Double>(
									chargeLevelRatio,
									MeasurementUnit.RAW,
									this.getClock()));
		} else if (this.getExecutionMode().isTestWithoutSimulation()) {
			ret = new SignalData<Double>(
							this.getClock(),
							new TimedMeasure<Double>(
									BatteriesSimulationConfiguration.
												INITIAL_BATTERIES_LEVEL_RATIO,
									MeasurementUnit.RAW,
									this.getClock()));
		} else {
			ret = new SignalData<Double>(
							new TimedMeasure<Double>(
									BatteriesSimulationConfiguration.
												INITIAL_BATTERIES_LEVEL_RATIO,
									MeasurementUnit.RAW));			
		}

		if (VERBOSE) {
			this.logMessage("Batteries return its charge level: " + ret);
		}

		return ret;
	}

	/**
	 * @see etape1.equipments.batteries.BatteriesImplementationI#getCurrentPowerConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerConsumption() throws Exception
	{
		SignalData<Double> ret;
		if (getExecutionMode().isSILTest()) {
			if (this.currentState.equals(State.CHARGING)) {
				ret = new SignalData<>(this.getClock4Simulation(),
									   this.totalInPower);
			} else {
				ret = new SignalData<>(
							this.getClock4Simulation(),
							new Measure<Double>(
									0.0,
									IN_POWER_PER_CELL.getMeasurementUnit()));
			}
		} else {
			if (this.currentState.equals(State.CHARGING)) {
				ret = new SignalData<>(this.totalInPower);
			} else {
				ret = new SignalData<>(
							new Measure<Double>(
									0.0,
									IN_POWER_PER_CELL.getMeasurementUnit()));
			}
		}

		// Postconditions checking
		assert	ret != null && ret.getMeasure().getData() >= 0.0 &&
					ret.getMeasure().getMeasurementUnit().equals(POWER_UNIT) :
				new PostconditionException(
						"return != null && return.getMeasure().getData() >= 0.0"
						+ " && return.getMeasure().getMeasurementUnit().equals("
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

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												BatteriesStateSILModel.URI,
												t -> new SIL_StartCharging(t));
		}
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

		assert	areCharging() ||
							chargeLevel().getMeasure().getData().equals(1.0) :
				new PreconditionException(
						"areCharging() || chargeLevel().getMeasure()."
						+ "getData().equals(1.0)");

		this.currentState = State.IDLE;

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												BatteriesStateSILModel.URI,
												t -> new SIL_StopCharging(t));
		}
	}
}
// -----------------------------------------------------------------------------
