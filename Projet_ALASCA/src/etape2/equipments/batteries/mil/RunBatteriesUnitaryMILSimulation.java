package etape2.equipments.batteries.mil;

import java.time.Instant;
import java.util.ArrayList;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import etape1.equipments.batteries.Batteries;
import etape2.equipments.batteries.mil.events.BatteriesAvailable;
import etape2.equipments.batteries.mil.events.BatteriesEmpty;
import etape2.equipments.batteries.mil.events.BatteriesRequiredPowerChanged;
import etape2.equipments.batteries.mil.events.StartCharging;
import etape2.equipments.batteries.mil.events.StopCharging;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import tests_utils.SimulationTestStep;
import tests_utils.TestScenario;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunBatteriesUnitaryMILSimulation</code> is the main class
 * used to run simulations on the models of the batteries in isolation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The simulation architecture for the solar panel contains two atomic models
 * composed under a coupled model:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2025-e2/BatteriesUnitTestArchitecture.png"/></p> 
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
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunBatteriesUnitaryMILSimulation
{
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= BatteriesSimulationConfiguration.staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		staticInvariants();
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
																new HashMap<>();

			// BatteriesPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					BatteriesPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							BatteriesPowerModel.class,
							BatteriesPowerModel.URI,
							BatteriesSimulationConfiguration.TIME_UNIT,
							null));
			// BatteriesUnitTesterModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					BatteriesUnitTesterModel.URI,
					AtomicHIOA_Descriptor.create(
							BatteriesUnitTesterModel.class,
							BatteriesUnitTesterModel.URI,
							BatteriesSimulationConfiguration.TIME_UNIT,
							null));

			
			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(BatteriesPowerModel.URI);
			submodels.add(BatteriesUnitTesterModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(BatteriesUnitTesterModel.URI,
									StartCharging.class),
					new EventSink[] {
							new EventSink(BatteriesPowerModel.URI,
										  StartCharging.class)
					});
			connections.put(
					new EventSource(BatteriesUnitTesterModel.URI,
							StopCharging.class),
					new EventSink[] {
							new EventSink(BatteriesPowerModel.URI,
									StopCharging.class)
					});
			connections.put(
					new EventSource(BatteriesUnitTesterModel.URI,
									BatteriesRequiredPowerChanged.class),
					new EventSink[] {
							new EventSink(BatteriesPowerModel.URI,
									BatteriesRequiredPowerChanged.class)
					});
			connections.put(
					new EventSource(BatteriesPowerModel.URI,
									BatteriesEmpty.class),
					new EventSink[] {
							new EventSink(BatteriesUnitTesterModel.URI,
										  BatteriesEmpty.class)
					});
			connections.put(
					new EventSource(BatteriesPowerModel.URI,
									BatteriesAvailable.class),
					new EventSink[] {
							new EventSink(BatteriesUnitTesterModel.URI,
									BatteriesAvailable.class)
					});

			// variable sharing bindings between exporting and importing
			// models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			VariableSource source =
					new VariableSource("batteriesInputPower", Double.class,
									   BatteriesPowerModel.URI);
			VariableSink[] sinks =
					new VariableSink[] {
						new VariableSink("batteriesInputPower", Double.class,
										 BatteriesUnitTesterModel.URI)
					};
			bindings.put(source, sinks);
			source = new VariableSource("batteriesOutputPower", Double.class,
									   BatteriesPowerModel.URI);
			sinks = new VariableSink[] {
						new VariableSink("batteriesOutputPower", Double.class,
										 BatteriesUnitTesterModel.URI)
					};
			bindings.put(source, sinks);
			source = new VariableSource("batteriesRequiredPower", Double.class,
										BatteriesUnitTesterModel.URI);
			sinks = new VariableSink[] {
						new VariableSink("batteriesRequiredPower", Double.class,
										 BatteriesPowerModel.URI)
					};
			bindings.put(source, sinks);

			// coupled model descriptor
			coupledModelDescriptors.put(
					BatteriesCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							BatteriesCoupledModel.class,
							BatteriesCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							BatteriesCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							BatteriesSimulationConfiguration.TIME_UNIT);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();

			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

			// run a CLASSICAL test scenario
			CLASSICAL.setUpSimulator(se);
			Time startTime = CLASSICAL.getStartTime();
			Duration d = CLASSICAL.getEndTime().subtract(startTime);
			se.doStandAloneSimulation(startTime.getSimulatedTime(),
									  d.getSimulatedDuration());
			se.getSimulatedModel().finalise();

			// run a BATTERIES_EMPTY test scenario
			BATTERIES_EMPTY.setUpSimulator(se);
			startTime = BATTERIES_EMPTY.getStartTime();
			d = BATTERIES_EMPTY.getEndTime().subtract(startTime);
			se.doStandAloneSimulation(startTime.getSimulatedTime(),
									  d.getSimulatedDuration());
			se.getSimulatedModel().finalise();

			// run a BATTERIES_CHARGING test scenario
			BATTERIES_CHARGING.setUpSimulator(se);
			startTime = BATTERIES_CHARGING.getStartTime();
			d = BATTERIES_CHARGING.getEndTime().subtract(startTime);
			se.doStandAloneSimulation(startTime.getSimulatedTime(),
									  d.getSimulatedDuration());
			se.getSimulatedModel().finalise();
			System.exit(0);
		} catch (Throwable e) {
			throw new RuntimeException(e) ;
		}
	}

	// -------------------------------------------------------------------------
	// Test scenarios
	// -------------------------------------------------------------------------

	/** the start instant used in the test scenarios.						*/
	protected static Instant	START_INSTANT =
									Instant.parse("2025-10-20T12:00:00.00Z");
	/** the end instant used in the test scenarios.							*/
	protected static Instant	END_INSTANT =
									Instant.parse("2025-10-20T20:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}.												*/
	protected static Time		START_TIME =
									new Time(0.0, TimeUnit.HOURS);

	/** standard test scenario, see Gherkin specification.				 	*/
	protected static TestScenario	CLASSICAL =
		new TestScenario(
			"-----------------------------------------------------\n" +
			"Classical\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: batteries charging and discharging\n\n" +
			"      Scenario: batteries produce without going empty\n" +
			"        Given a standard batteries neither full nor empty\n" +
			"        When it is producing for a limited time\n" +
			"        Then the batteries level goes down but stays not empty\n\n" +
			"      Scenario: batteries charge without going full\n" +
			"        Given a standard batteries neither full nor empty\n" +
			"        When it is charging for a limited time\n" +
			"        Then the batteries level goes up but stays not full\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End classical\n" +
			"-----------------------------------------------------",
			START_INSTANT,
			END_INSTANT,
			START_TIME,
			(se, ts) -> { 
				HashMap<String, Object> simParams = new HashMap<>();
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.CAPACITY_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* BatteriesSimulationConfiguration.
												NUMBER_OF_CELL_GROUPS_IN_SERIES
							* Batteries.CAPACITY_PER_UNIT.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.IN_POWER_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* Batteries.IN_POWER_PER_CELL.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.MAX_OUT_POWER_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* Batteries.MAX_OUT_POWER_PER_CELL.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.LEVEL_QUANTUM_RP_NAME),
					BatteriesSimulationConfiguration.
											STANDARD_LEVEL_INTEGRATION_QUANTUM);
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.INITIAL_LEVEL_RP_NAME),
					BatteriesSimulationConfiguration.INITIAL_BATTERIES_LEVEL);
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesUnitTesterModel.URI,
						BatteriesUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
				se.setSimulationRunParameters(simParams);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> { return null; },
					(m, t) -> {
						// this indicates a need for power from the batteries
						((BatteriesUnitTesterModel)m).
									batteriesRequiredPower.setNewValue(5.0, t);
					}),
				// the notification of the continuous state change must be done
				// after the modification as the modification of the state is
				// performed in the internal transition method, which is
				// executed after the output; hence a first internal transition
				// updates the required power, then an immediate second internal
				// transition will trigger the continuous state change event to
				// be emitted
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new BatteriesRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T14:00:00.00Z"),
					(m, t) -> { return null; },
					(m, t) -> {
						// this indicates that no more power is needed
						((BatteriesUnitTesterModel)m).
									batteriesRequiredPower.setNewValue(0.0, t);
					}),
				// same idea to stop producing
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T14:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new BatteriesRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T15:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new StartCharging(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T16:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new StopCharging(t));
						return ret;
					},
					(m, t) -> {})
			}	// end SimulationTestStep[]
		);	// end TestScenario

	/** test scenario where the batteries are used until empty,
	 *  see Gherkin specification.										 	*/
	protected static TestScenario	BATTERIES_EMPTY =
		new TestScenario(
			"-----------------------------------------------------\n" +
			"Batteries empty\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: batteries becoming empty\n\n" +
			"      Scenario: batteries produce until empty\n" +
			"        Given a standard batteries not empty\n" +
			"        When it is producing for a sufficiently long time\n" +
			"        Then the level goes down until becoming empty\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Batteries empty\n" +
			"-----------------------------------------------------",
			START_INSTANT,
			END_INSTANT,
			START_TIME,
			(se, ts) -> { 
				HashMap<String, Object> simParams = new HashMap<>();
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.CAPACITY_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* BatteriesSimulationConfiguration.
												NUMBER_OF_CELL_GROUPS_IN_SERIES
							* Batteries.CAPACITY_PER_UNIT.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.IN_POWER_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* Batteries.IN_POWER_PER_CELL.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.MAX_OUT_POWER_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* Batteries.MAX_OUT_POWER_PER_CELL.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.LEVEL_QUANTUM_RP_NAME),
					BatteriesSimulationConfiguration.
											STANDARD_LEVEL_INTEGRATION_QUANTUM);
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.INITIAL_LEVEL_RP_NAME),
					BatteriesSimulationConfiguration.INITIAL_BATTERIES_LEVEL);
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesUnitTesterModel.URI,
						BatteriesUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
				se.setSimulationRunParameters(simParams);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> { return null; },
					(m, t) -> {
						((BatteriesUnitTesterModel)m).
									batteriesRequiredPower.setNewValue(25.0, t);
					}),
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new BatteriesRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {})
			}	// end SimulationTestStep[]
		);	// end TestScenario


	/** test scenario where the empty is charged until full,
	 *  see Gherkin specification.										 	*/
	protected static TestScenario	BATTERIES_CHARGING =
		new TestScenario(
			"-----------------------------------------------------\n" +
			"Batteries full\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: batteries charging until full\n\n" +
			"      Scenario: batteries charging until full\n" +
			"        Given a standard empty batteries\n" +
			"        When it is charging for a sufficiently long time\n" +
			"        Then the level goes up until full\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Batteries full\n" +
			"-----------------------------------------------------",
			START_INSTANT,
			END_INSTANT,
			START_TIME,
			(se, ts) -> { 
				HashMap<String, Object> simParams = new HashMap<>();
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.CAPACITY_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* BatteriesSimulationConfiguration.
												NUMBER_OF_CELL_GROUPS_IN_SERIES
							* Batteries.CAPACITY_PER_UNIT.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.IN_POWER_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* Batteries.IN_POWER_PER_CELL.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.MAX_OUT_POWER_RP_NAME),
					BatteriesSimulationConfiguration.NUMBER_OF_PARALLEL_CELLS
						* Batteries.MAX_OUT_POWER_PER_CELL.getData());
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.LEVEL_QUANTUM_RP_NAME),
					BatteriesSimulationConfiguration.
											STANDARD_LEVEL_INTEGRATION_QUANTUM);
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesPowerModel.URI,
						BatteriesPowerModel.INITIAL_LEVEL_RP_NAME),
					BatteriesSimulationConfiguration.INITIAL_BATTERIES_LEVEL);
				simParams.put(
					ModelI.createRunParameterName(
						BatteriesUnitTesterModel.URI,
						BatteriesUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
				se.setSimulationRunParameters(simParams);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					BatteriesUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new StartCharging(t));
						return ret;
					},
					(m, t) -> {}),
			}	// end SimulationTestStep[]
		);	// end TestScenario
}
// -----------------------------------------------------------------------------
