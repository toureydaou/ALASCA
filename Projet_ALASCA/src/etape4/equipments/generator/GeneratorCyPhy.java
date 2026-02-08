package etape4.equipments.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


import etape1.equipments.generator.GeneratorCI;
import etape1.equipments.generator.GeneratorImplementationI;
import etape1.equipments.generator.connections.GeneratorInboundPort;
import etape2.equipments.generator.mil.GeneratorSimulationConfiguration;
import etape2.equipments.generator.mil.events.Start;
import etape2.equipments.generator.mil.events.Stop;
import etape2.equipments.generator.mil.events.TankEmpty;
import etape2.equipments.generator.mil.events.TankNoLongerEmpty;
import etape4.equipments.generator.sil.GeneratorStateSILModel;
import etape4.equipments.generator.sil.Local_SIL_SimulationArchitectures;
import etape4.equipments.generator.sil.events.CurrentFuelConsumption;
import etape4.equipments.generator.sil.events.CurrentFuelLevel;
import etape4.equipments.generator.sil.events.CurrentPowerProduction;
import etape4.equipments.generator.sil.events.SIL_Refill;
import etape4.equipments.generator.sil.events.TimedPhysicalMeasure;

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
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>GeneratorCyPhy</code> implements an electric generator
 * component for the Household Energy Management project.
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
 * </pre>
 * 
 * <p>Created on : 2025-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@SIL_Simulation_Architectures({
	// fragile: fields in annotations cannot be defined by a class constant due
	// to Java annotations field initialisers limited to static values only
	@LocalArchitecture(
		// must be equal to UNIT_TEST_ARCHITECTURE_URI
		uri = "silUnitTests",
		// must be equal to the URI of the instance of BatteriesCoupledModel
		rootModelURI = "GeneratorCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents()
		),
	@LocalArchitecture(
		// must be equal to INTEGRATION_TEST_ARCHITECTURE_URI
		uri = "silIntegrationTests",
		// must be equal to the URI of the instance of BatteriesStateSILModel
		rootModelURI = "GeneratorStateSILModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents =
			@ModelExternalEvents(
				imported = {TankEmpty.class, TankNoLongerEmpty.class,
							CurrentPowerProduction.class, CurrentFuelLevel.class,
							CurrentFuelConsumption.class},
				exported = {Start.class, Stop.class, SIL_Refill.class}
				)
		)
	})
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered = {GeneratorCI.class})
// -----------------------------------------------------------------------------
public class			GeneratorCyPhy
extends		AbstractCyPhyComponent
implements	GeneratorImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** standard URI of the batteries reflection inbound port.				*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
														"generator-rip-uri";	
	/** the standard inbound port URI when just one port is used.			*/
	public static final String			STANDARD_INBOUND_PORT_URI =
														"generator-ibp-uri";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String			UNIT_TEST_ARCHITECTURE_URI =
														"silUnitTests";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String			INTEGRATION_TEST_ARCHITECTURE_URI =
														"silIntegrationTests";

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
	public static final Measure<Double>	TANK_CAPACITY =
													new Measure<Double>(
															40.0,
															CAPACITY_UNIT);
	/** standard maximum power level.										*/
	public static final Measure<Double>	MAX_POWER = new Measure<Double>(
															5500.0,
															POWER_UNIT);

	/** maximum power in {@code POWER_UNIT}.								*/
	protected Measure<Double>			maxPower;
	/** tank capacity in {@code CAPACITY_UNIT}.								*/
	protected Measure<Double>			tankCapacity;
	/** minimal fuel consumption of the generator when producing no power,
	 *  in {@code CONSUMPTION_UNIT}.										*/
	protected Measure<Double>			minFuelConsumption;
	/** maximal fuel consumption of the generator when producing the
	 *  maximum power, in {@code CONSUMPTION_UNIT}.							*/
	protected Measure<Double>			maxFuelConsumption;

	/** current state of the generator.										*/
	protected State						currentState;

	/** the inbound port offering the component interface
	 *  {@code BatteriesCI}.												*/
	protected GeneratorInboundPort		inboundPort;

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
	protected static boolean	implementationInvariants(GeneratorCyPhy instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				true,
				GeneratorCyPhy.class, instance,
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
				GeneratorCyPhy.class,
				"STANDARD_INBOUND_PORT_URI != null && "
				+ "!STANDARD_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				OUTPUT_AC_TENSION != null &&
					(OUTPUT_AC_TENSION.equals(new Measure<Double>(110.0, TENSION_UNIT)) ||
							OUTPUT_AC_TENSION.equals(new Measure<Double>(220.0, TENSION_UNIT))),
				GeneratorCyPhy.class,
				"OUTPUT_AC_TENSION != null && (OUTPUT_AC_TENSION.equals("
				+ "new Measure<Double>(110.0, TENSION_UNIT)) || "
				+ "OUTPUT_AC_TENSION.equals(new Measure<Double>(220.0, "
				+ "TENSION_UNIT)))");
		ret &= AssertionChecking.checkStaticInvariant(
				MIN_FUEL_CONSUMPTION != null &&
					MIN_FUEL_CONSUMPTION.getData() > 0.0 &&
						MIN_FUEL_CONSUMPTION.getMeasurementUnit().equals(
															CONSUMPTION_UNIT),
				GeneratorCyPhy.class,
				"MIN_FUEL_CONSUMPTION != null && MIN_FUEL_CONSUMPTION.getData()"
				+ " > 0.0 && MIN_FUEL_CONSUMPTION.getMeasurementUnit().equals("
				+ "CONSUMPTION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MAX_FUEL_CONSUMPTION != null &&
					MAX_FUEL_CONSUMPTION.getData() > 0.0 &&
						MAX_FUEL_CONSUMPTION.getMeasurementUnit().equals(
															CONSUMPTION_UNIT),
				GeneratorCyPhy.class,
				"MAX_FUEL_CONSUMPTION != null && MAX_FUEL_CONSUMPTION.getData()"
				+ " > 0.0 && MAX_FUEL_CONSUMPTION.getMeasurementUnit().equals("
				+ "CONSUMPTION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MIN_FUEL_CONSUMPTION.getData() <= MAX_FUEL_CONSUMPTION.getData(),
				GeneratorCyPhy.class,
				"MIN_FUEL_CONSUMPTION.getData() <= MAX_FUEL_CONSUMPTION.getData()");
		ret &= AssertionChecking.checkStaticInvariant(
				TANK_CAPACITY != null && TANK_CAPACITY.getData() > 0.0 &&
						TANK_CAPACITY.getMeasurementUnit().equals(CAPACITY_UNIT),
				GeneratorCyPhy.class,
				"TANK_CAPACITY != null && TANK_CAPACITY.getData() > 0.0 && "
				+ "TANK_CAPACITY.getMeasurementUnit().equals(CAPACITY_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				MAX_POWER != null && MAX_POWER.getData() > 0.0 &&
							MAX_POWER.getMeasurementUnit().equals(POWER_UNIT),
				GeneratorCyPhy.class,
				"MAX_POWER != null && MAX_POWER.getData() > 0.0 && "
				+ "MAX_POWER.getMeasurementUnit().equals(POWER_UNIT)");
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
	protected static boolean	invariants(GeneratorCyPhy instance)
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
	 * create a standard electric generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected			GeneratorCyPhy() throws Exception
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
	 * post	{@code getExecutionMode().isStandard()}
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
	protected			GeneratorCyPhy(
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
	 * post	{@code getExecutionMode().isStandard()}
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
	protected			GeneratorCyPhy(
		String inboundPortURI,
		Measure<Double> power,
		Measure<Double> tankCapacity,
		Measure<Double> minFuelConsumption,
		Measure<Double> maxFuelConsumption
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(inboundPortURI, power, tankCapacity,
						minFuelConsumption, maxFuelConsumption);

		// Invariant checking
		assert	GeneratorCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Generator.implementationInvariants(this)");
		assert	GeneratorCyPhy.invariants(this) :
				new InvariantException("Generator.invariants(this)");
	}

	// Tests without simulation execution

	/**
	 * create an electric generator with the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
	 * post	{@code State.OFF.equals(getState())}
	 * </pre>
	 *
	 * @param executionMode			execution mode for the next run.
	 * @param clockURI				URI of the clock used to synchronise the test scenario.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			GeneratorCyPhy(
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(STANDARD_INBOUND_PORT_URI,
			 MAX_POWER,
			 TANK_CAPACITY,
			 MIN_FUEL_CONSUMPTION,
			 MAX_FUEL_CONSUMPTION,
			 executionMode,
			 clockURI);
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
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
	 * post	{@code State.OFF.equals(getState())}
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
	 * @param executionMode			execution mode for the next run.
	 * @param clockURI				URI of the clock used to synchronise the test scenario.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			GeneratorCyPhy(
		Measure<Double> power,
		Measure<Double> tankCapacity,
		Measure<Double> minFuelConsumption,
		Measure<Double> maxFuelConsumption,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(STANDARD_INBOUND_PORT_URI, power, tankCapacity, minFuelConsumption,
				maxFuelConsumption, executionMode, clockURI);
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
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
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
	 * @param executionMode			execution mode for the next run.
	 * @param clockURI				URI of the clock used to synchronise the test scenario.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			GeneratorCyPhy(
		String inboundPortURI,
		Measure<Double> power,
		Measure<Double> tankCapacity,
		Measure<Double> minFuelConsumption,
		Measure<Double> maxFuelConsumption,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  clockURI);

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(inboundPortURI, power, tankCapacity,
						minFuelConsumption, maxFuelConsumption);

		// Invariant checking
		assert	GeneratorCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Generator.implementationInvariants(this)");
		assert	GeneratorCyPhy.invariants(this) :
				new InvariantException("Generator.invariants(this)");
	}

	// Tests with simulation

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
	 * pre	{@code executionMode != null && executionMode.isSimulationTest()}
	 * pre	{@code testScenario != null}
	 * pre	{@code localArchitectureURI != null && !localArchitectureURI.isEmpty()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getExecutionMode().equals(executionMode)}
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
	 * @param executionMode			execution mode for the next run.
	 * @param testScenario					test scenario to be executed with this component.
	 * @param localArchitectureURI			URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param accelerationFactor			acceleration factor for the simulation.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			GeneratorCyPhy(
		String inboundPortURI,
		Measure<Double> power,
		Measure<Double> tankCapacity,
		Measure<Double> minFuelConsumption,
		Measure<Double> maxFuelConsumption,
		ExecutionMode executionMode,
		TestScenario testScenario,
		String localArchitectureURI,
		double accelerationFactor
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
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

		this.localArchitectureURI = localArchitectureURI;
		this.accelerationFactor = accelerationFactor;

		this.initialise(inboundPortURI, power, tankCapacity,
						minFuelConsumption, maxFuelConsumption);

		// Invariant checking
		assert	GeneratorCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Generator.implementationInvariants(this)");
		assert	GeneratorCyPhy.invariants(this) :
				new InvariantException("Generator.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Initialisation methods
	// -------------------------------------------------------------------------

	/**
	 * initialise the generator.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
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
	protected void		initialise(
		String inboundPortURI,
		Measure<Double> power,
		Measure<Double> tankCapacity,
		Measure<Double> minFuelConsumption,
		Measure<Double> maxFuelConsumption
		) throws Exception
	{
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

		if (VERBOSE) {
			this.tracer.get().setTitle("Generator component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

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
						createGeneratorSIL_Architecture4UnitTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createGeneratorSIL_Architecture4IntegrationTest(
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
		assert	GeneratorCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Generator.implementationInvariants(this)");
		assert	GeneratorCyPhy.invariants(this) :
				new InvariantException("Generator.invariants(this)");

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
				// for the HairDryer, real time MIL and SIL use the same
				// simulation models
				RTArchitecture architecture =
					(RTArchitecture) this.localSimulationArchitectures.
												get(this.localArchitectureURI);
				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;
					/**
					 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
					 */
					@Override
					public Object	getModelStateValue(
						String modelURI,
						String name
						) throws Exception
					{
						assert	modelURI.equals(GeneratorStateSILModel.URI);
						assert	GeneratorStateSILModel.STATE_VALUE_NAME.equals(name)
								|| GeneratorStateSILModel.POWER_PRODUCTION_VALUE_NAME.equals(name)
								|| GeneratorStateSILModel.FUEL_LEVEL_VALUE_NAME.equals(name)
								|| GeneratorStateSILModel.FUEL_CONSUMPTION_VALUE_NAME.equals(name);

						return ((GeneratorStateSILModel)
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
		assert	GeneratorCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Generator.implementationInvariants(this)");
		assert	GeneratorCyPhy.invariants(this) :
				new InvariantException("Generator.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.traceMessage("GeneratorCyPhy executes.\n");

		// Invariant checking
		assert	GeneratorCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Generator.implementationInvariants(this)");
		assert	GeneratorCyPhy.invariants(this) :
				new InvariantException("Generator.invariants(this)");

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
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#getState()
	 */
	@Override
	public State		getState() throws Exception
	{
		State ret;
		if (this.getExecutionMode().isSILTest()) {
			 if (this.asp == null) {
				 // the simulator does not run yet, return the initial value
				 // for the current state.
				 ret = this.currentState;
			 } else {
				 ret = (State) this.asp.getModelStateValue(
									GeneratorStateSILModel.URI,
									GeneratorStateSILModel.STATE_VALUE_NAME);
			 }
		} else if (this.getExecutionMode().isTestWithoutSimulation()) {
			ret = this.currentState;
		} else {
			// temporary implementation, would need a physical sensor
			ret = this.currentState;			
		}

		if (VERBOSE) {
			this.logMessage("Generator returns its running status: " + ret);
		}

		return ret;
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#nominalOutputTension()
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
	 * @see etape1.equipments.generator.GeneratorImplementationI#tankCapacity()
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
	 * @see etape1.equipments.generator.GeneratorImplementationI#refillTank(fr.sorbonne_u.alasca.physical_data.Measure)
	 */
	@Override
	public void				refillTank(Measure<Double> quantity)
	throws Exception
	{
		// Preconditions checking
		assert	quantity != null :
				new PreconditionException("quantity != null");
		assert	quantity.getData() > 0.0 :
				new PreconditionException("quantity.getData() > 0.0");
		assert	quantity.getMeasurementUnit().equals(CAPACITY_UNIT) :
				new PreconditionException(
						"quantity.getMeasurementUnit().equals(CAPACITY_UNIT)");

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
								GeneratorStateSILModel.URI,
								t -> new SIL_Refill(
										t,
										new SIL_Refill.FuelQuantity(quantity)));
		} else {
			// temporary implementation; would need the hardware
		}
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#currentTankLevel()
	 */
	@Override
	public SignalData<Double>	currentTankLevel() throws Exception
	{
		SignalData<Double> ret;
		if (this.getExecutionMode().isSILTest()) {
			if (this.asp == null) {
				 // the simulator does not run yet, return the initial value
				 // for the tank level.
				ret = new SignalData<Double>(
						this.getClock4Simulation(),
						new Measure<Double>(
								GeneratorSimulationConfiguration.INITIAL_TANK_LEVEL,
								GeneratorImplementationI.CAPACITY_UNIT));
			} else {
				TimedPhysicalMeasure tpm =
					(TimedPhysicalMeasure) this.asp.getModelStateValue(
								GeneratorStateSILModel.URI,
								GeneratorStateSILModel.FUEL_LEVEL_VALUE_NAME);
				ret = new SignalData<Double>(
						this.getClock4Simulation(),
						new TimedMeasure<Double>(
								tpm.getValue(),
								GeneratorImplementationI.CAPACITY_UNIT,
								this.getClock4Simulation(),
								this.getClock4Simulation().
									instantOfSimulatedTime(tpm.getTimestamp())));
			}
		} else if (this.getExecutionMode().isTestWithoutSimulation()) {
			ret = new SignalData<Double>(
						this.getClock(),
						new TimedMeasure<Double>(
								GeneratorSimulationConfiguration.
														INITIAL_TANK_LEVEL,
								GeneratorImplementationI.CAPACITY_UNIT,
								this.getClock()));
		} else {
			// temporary implementation, would need a physical sensor
			ret = new SignalData<Double>(
						new Measure<Double>(
								GeneratorSimulationConfiguration.
														INITIAL_TANK_LEVEL,
								GeneratorImplementationI.CAPACITY_UNIT));			
		}

		if (VERBOSE) {
			this.logMessage("Generator returns its current tank level: "
							+ ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getMeasure().getData() >= 0.0 &&
					ret.getMeasure().getMeasurementUnit().equals(CAPACITY_UNIT) :
				new PostconditionException(
						"return != null && return.getMeasure().getData() >= 0.0 "
						+ "&& return.getMeasure().getMeasurementUnit().equals("
						+ "CAPACITY_UNIT)");

		return ret;
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#maxPowerProductionCapacity()
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
	 * @see etape1.equipments.generator.GeneratorImplementationI#currentPowerProduction()
	 */
	@Override
	public SignalData<Double>	currentPowerProduction() throws Exception
	{
		SignalData<Double> ret;
		if (this.getExecutionMode().isSILTest()) {
			if (this.asp == null) {
				 // the simulator does not run yet, return the initial value
				 // for the power production.
				ret = new SignalData<Double>(
						new Measure<Double>(
								0.0,
								GeneratorImplementationI.POWER_UNIT));	
			} else {
				TimedPhysicalMeasure tpm =
					(TimedPhysicalMeasure) this.asp.getModelStateValue(
							GeneratorStateSILModel.URI,
							GeneratorStateSILModel.POWER_PRODUCTION_VALUE_NAME);
				ret = new SignalData<Double>(
						this.getClock4Simulation(),
						new TimedMeasure<Double>(
								tpm.getValue(),
								GeneratorImplementationI.POWER_UNIT,
								this.getClock4Simulation(),
								this.getClock4Simulation().
									instantOfSimulatedTime(tpm.getTimestamp())));
			}
		} else if (this.getExecutionMode().isTestWithoutSimulation()) {
			ret = new SignalData<Double>(
						this.getClock(),
						new TimedMeasure<Double>(
								0.0,
								GeneratorImplementationI.POWER_UNIT,
								this.getClock()));
		} else {
			// temporary implementation, would need a physical sensor
			ret = new SignalData<Double>(
						new Measure<Double>(
								0.0,
								GeneratorImplementationI.POWER_UNIT));			
		}

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
	 * @see etape1.equipments.generator.GeneratorImplementationI#minFuelConsumption()
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
	 * @see etape1.equipments.generator.GeneratorImplementationI#maxFuelConsumption()
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
	 * @see etape1.equipments.generator.GeneratorImplementationI#currentFuelConsumption()
	 */
	@Override
	public SignalData<Double>	currentFuelConsumption() throws Exception
	{
		SignalData<Double> ret;
		if (this.getExecutionMode().isSILTest()) {
			if (this.asp == null) {
				 // the simulator does not run yet, return the initial value
				 // for the fuel consumption.
				ret = new SignalData<Double>(
						new Measure<Double>(
								0.0,
								GeneratorImplementationI.CONSUMPTION_UNIT));
			} else {
				TimedPhysicalMeasure tpm =
					(TimedPhysicalMeasure) this.asp.getModelStateValue(
							GeneratorStateSILModel.URI,
							GeneratorStateSILModel.FUEL_CONSUMPTION_VALUE_NAME);
				ret = new SignalData<Double>(
						this.getClock4Simulation(),
						new TimedMeasure<Double>(
								tpm.getValue(),
								GeneratorImplementationI.CONSUMPTION_UNIT,
								this.getClock4Simulation(),
								this.getClock4Simulation().
									instantOfSimulatedTime(tpm.getTimestamp())));
			}
		} else if (this.getExecutionMode().isTestWithoutSimulation()) {
			ret = new SignalData<Double>(
						this.getClock(),
						new TimedMeasure<Double>(
								0.0,
								GeneratorImplementationI.CONSUMPTION_UNIT,
								this.getClock()));
		} else {
			// temporary implementation, would need a physical sensor
			ret = new SignalData<Double>(
						new Measure<Double>(
								0.0,
								GeneratorImplementationI.CONSUMPTION_UNIT));			
		}

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
		assert	ret.getMeasure().getData() >= 0.0 &&
					ret.getMeasure().getData() <= maxFuelConsumption().getData() :
				new PostconditionException(
						"return.getMeasure().getData() >= 0.0 && "
						+ "return.getMeasure().getData() <= "
						+ "maxFuelConsumption().getData()");
		
		return ret;
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#startGenerator()
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

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												GeneratorStateSILModel.URI,
												t -> new Start(t));
		}
	}

	/**
	 * @see etape1.equipments.generator.GeneratorImplementationI#stopGenerator()
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

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												GeneratorStateSILModel.URI,
												t -> new Stop(t));
		}
	}
}
// -----------------------------------------------------------------------------
