package fr.sorbonne_u.components.hem2025e2.equipments.generator.mil;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.time.Instant;
import java.util.ArrayList;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.hem2025e1.equipments.generator.Generator;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Refill;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Refill.FuelQuantity;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Start;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.Stop;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.TankEmpty;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.TankNoLongerEmpty;
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
import fr.sorbonne_u.exceptions.VerboseException;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunGeneratorUnitaryMILSimulation</code> is the main class
 * used to run simulations on the models of the generator in isolation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The simulation architecture for the solar panel contains three atomic models
 * composed under a coupled model:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2025-e2/GeneratorUnitTestArchitecture.png"/></p> 
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
 * <p>Created on : 2025-10-20</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunGeneratorUnitaryMILSimulation
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
		ret &= GeneratorSimulationConfiguration.staticInvariants();
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

			// GeneratorFuelModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorFuelModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorFuelModel.class,
							GeneratorFuelModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null));
			// GeneratorPowerModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorPowerModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorPowerModel.class,
							GeneratorPowerModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null));
			// BatteriesUnitTesterModel is an atomic HIOA model, so needs an
			// AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					GeneratorUnitTesterModel.URI,
					AtomicHIOA_Descriptor.create(
							GeneratorUnitTesterModel.class,
							GeneratorUnitTesterModel.URI,
							GeneratorSimulationConfiguration.TIME_UNIT,
							null));


			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(GeneratorFuelModel.URI);
			submodels.add(GeneratorPowerModel.URI);
			submodels.add(GeneratorUnitTesterModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(GeneratorUnitTesterModel.URI, Start.class),
					new EventSink[] {
							new EventSink(GeneratorFuelModel.URI, Start.class),
							new EventSink(GeneratorPowerModel.URI, Start.class)
					});
			connections.put(
					new EventSource(GeneratorUnitTesterModel.URI, Stop.class),
					new EventSink[] {
							new EventSink(GeneratorFuelModel.URI, Stop.class),
							new EventSink(GeneratorPowerModel.URI, Stop.class)
					});
			connections.put(
					new EventSource(GeneratorUnitTesterModel.URI, Refill.class),
					new EventSink[] {
							new EventSink(GeneratorFuelModel.URI, Refill.class)
					});
			connections.put(
					new EventSource(GeneratorUnitTesterModel.URI,
									GeneratorRequiredPowerChanged.class),
					new EventSink[] {
							new EventSink(GeneratorPowerModel.URI,
										  GeneratorRequiredPowerChanged.class)
					});

			connections.put(
					new EventSource(GeneratorFuelModel.URI, TankEmpty.class),
					new EventSink[] {
							new EventSink(GeneratorPowerModel.URI,
										  TankEmpty.class)
					});
			connections.put(
					new EventSource(GeneratorFuelModel.URI,
									TankNoLongerEmpty.class),
					new EventSink[] {
							new EventSink(GeneratorPowerModel.URI,
										  TankNoLongerEmpty.class)
					});

			connections.put(
					new EventSource(GeneratorPowerModel.URI,
									GeneratorRequiredPowerChanged.class),
					new EventSink[] {
							new EventSink(GeneratorFuelModel.URI,
										  GeneratorRequiredPowerChanged.class)
					});

			// variable sharing bindings between exporting and importing
			// models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			VariableSource source =
					new VariableSource("generatorOutputPower", Double.class,
									   GeneratorPowerModel.URI);
			VariableSink[] sinks = new VariableSink[] {
						new VariableSink("generatorOutputPower", Double.class,
										 GeneratorUnitTesterModel.URI),
						new VariableSink("generatorOutputPower", Double.class,
										 GeneratorFuelModel.URI)
					};
			bindings.put(source, sinks);
			source = new VariableSource("generatorRequiredPower", Double.class,
										GeneratorUnitTesterModel.URI);
			sinks = new VariableSink[] {
						new VariableSink("generatorRequiredPower", Double.class,
										 GeneratorPowerModel.URI)
					};
			bindings.put(source, sinks);

			// coupled model descriptor
			coupledModelDescriptors.put(
					GeneratorCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							GeneratorCoupledModel.class,
							GeneratorCoupledModel.URI,
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
							GeneratorCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							GeneratorSimulationConfiguration.TIME_UNIT);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();

			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

			// run a CLASSICAL test scenario
			TestScenarioWithSimulation classical = classical();
			Map<String, Object> classicalRunParameters =
												new HashMap<String, Object>();
			classical.addToRunParameters(classicalRunParameters);
			se.setSimulationRunParameters(classicalRunParameters);
			Time startTime = classical.getStartTime();
			Duration d = classical.getEndTime().subtract(startTime);
			((Consumer<String>) (m -> { if (m != null) System.out.println(m); }))
											.accept(classical.beginMessage());
			se.doStandAloneSimulation(startTime.getSimulatedTime(),
									  d.getSimulatedDuration());
			se.getSimulatedModel().finalise();
			((Consumer<String>) (m -> { if (m != null) System.out.println(m); }))
											.accept(classical.endMessage());

			// run a TANK_EMPTY test scenario
			TestScenarioWithSimulation tankEmpty = tankEmpty();
			Map<String, Object> tankEmptyRunParameters =
												new HashMap<String, Object>();
			tankEmpty.addToRunParameters(tankEmptyRunParameters);
			se.setSimulationRunParameters(tankEmptyRunParameters);
			startTime = tankEmpty.getStartTime();
			d = tankEmpty.getEndTime().subtract(startTime);
			((Consumer<String>) (m -> { if (m != null) System.out.println(m); }))
											.accept(tankEmpty.beginMessage());
			se.doStandAloneSimulation(startTime.getSimulatedTime(),
									  d.getSimulatedDuration());
			se.getSimulatedModel().finalise();
			((Consumer<String>) (m -> { if (m != null) System.out.println(m); }))
											.accept(tankEmpty.endMessage());

			// run a TANK_REFILL test scenario
			TestScenarioWithSimulation tankRefill = tankRefill();
			Map<String, Object> tankRefillRunParameters =
											new HashMap<String, Object>();
			tankRefill.addToRunParameters(tankRefillRunParameters);
			se.setSimulationRunParameters(tankRefillRunParameters);
			startTime = tankRefill.getStartTime();
			d = tankRefill.getEndTime().subtract(startTime);
			((Consumer<String>) (m -> { if (m != null) System.out.println(m); }))
											.accept(tankRefill.beginMessage());
			se.doStandAloneSimulation(startTime.getSimulatedTime(),
									  d.getSimulatedDuration());
			se.getSimulatedModel().finalise();
			((Consumer<String>) (m -> { if (m != null) System.out.println(m); }))
											.accept(tankRefill.endMessage());
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
									Instant.parse("2025-10-20T18:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}.												*/
	protected static Time		START_TIME =
									new Time(0.0, TimeUnit.HOURS);

	/** standard test scenario, see Gherkin specification.				 	
	 * @throws VerboseException */
	protected static TestScenarioWithSimulation	classical()
	throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"-----------------------------------------------------\n" +
			"Classical\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: generator production\n\n" +
			"      Scenario: generator produces for a limited time without emptying the tank\n" +
			"        Given a standard generator with a tank not full neither empty\n" +
			"        When it is producing for a limited time\n" +
			"        Then the tank level goes down but stays not empty\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Classical\n" +
			"-----------------------------------------------------",
			"fake-clock-URI",	// for simulation only test scenario, no clock needed
			START_INSTANT,
			END_INSTANT,
			GeneratorCoupledModel.URI,
			START_TIME,
			(ts, simParams) -> {
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.CAPACITY_RP_NAME),
					GeneratorSimulationConfiguration.TANK_CAPACITY);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.INITIAL_LEVEL_RP_NAME),
					GeneratorSimulationConfiguration.INITIAL_TANK_LEVEL);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MIN_FUEL_CONSUMPTION_RP_NAME),
					Generator.MIN_FUEL_CONSUMPTION.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MAX_FUEL_CONSUMPTION_RP_NAME),
					Generator.MAX_FUEL_CONSUMPTION.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.LEVEL_QUANTUM_RP_NAME),
					GeneratorSimulationConfiguration.
										STANDARD_LEVEL_INTEGRATION_QUANTUM);
				simParams.put(
						ModelI.createRunParameterName(
							GeneratorFuelModel.URI,
							GeneratorFuelModel.MAX_OUT_POWER_RP_NAME),
						Generator.MAX_POWER.getData());
				simParams.put(
						ModelI.createRunParameterName(
							GeneratorPowerModel.URI,
							GeneratorPowerModel.MAX_OUT_POWER_RP_NAME),
						Generator.MAX_POWER.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorUnitTesterModel.URI,
						GeneratorUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorUnitTesterModel.URI,
						GeneratorUnitTesterModel.INITIAL_LEVEL_RP_NAME),
					GeneratorSimulationConfiguration.INITIAL_TANK_LEVEL);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new Start(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T14:00:00.00Z"),
					(m, t) -> {
						return null;
					},
					(m, t) -> {
						((GeneratorUnitTesterModel)m).
								generatorRequiredPower.setNewValue(5.0, t);
					}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T14:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new GeneratorRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T15:00:00.00Z"),
					(m, t) -> {
						return null;
					},
					(m, t) -> {
						((GeneratorUnitTesterModel)m).
								generatorRequiredPower.setNewValue(0.0, t);
					}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T15:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new GeneratorRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T16:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new Stop(t));
						return ret;
					},
					(m, t) -> {})
			}	// end SimulationTestStep[]
		);	// end TestScenario
	}

	/** test scenario where the generator tank is used until empty,
	 *  see Gherkin specification.										 	
	 * @throws VerboseException */
	protected static TestScenarioWithSimulation	tankEmpty()
	throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"-----------------------------------------------------\n" +
			"Generator tank empty\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: generator tank becoming empty\n\n" +
			"      Scenario: generator produces until emptying the tank\n" +
			"        Given a standard generator with a tank not empty\n" +
			"        When it is producing for a sufficiently long time\n" +
			"        Then the tank level goes down until becoming empty\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Generator tank empty\n" +
			"-----------------------------------------------------",
			"fake-clock-URI",	// for simulation only test scenario, no clock needed
			START_INSTANT,
			END_INSTANT,
			GeneratorCoupledModel.URI,
			START_TIME,
			(ts, simParams) -> {
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.CAPACITY_RP_NAME),
					GeneratorSimulationConfiguration.TANK_CAPACITY);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.INITIAL_LEVEL_RP_NAME),
					GeneratorSimulationConfiguration.INITIAL_TANK_LEVEL);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MIN_FUEL_CONSUMPTION_RP_NAME),
					Generator.MIN_FUEL_CONSUMPTION.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MAX_FUEL_CONSUMPTION_RP_NAME),
					Generator.MAX_FUEL_CONSUMPTION.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.LEVEL_QUANTUM_RP_NAME),
					GeneratorSimulationConfiguration.
											STANDARD_LEVEL_INTEGRATION_QUANTUM);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MAX_OUT_POWER_RP_NAME),
					Generator.MAX_POWER.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorPowerModel.URI,
						GeneratorPowerModel.MAX_OUT_POWER_RP_NAME),
					Generator.MAX_POWER.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorUnitTesterModel.URI,
						GeneratorUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorUnitTesterModel.URI,
						GeneratorUnitTesterModel.INITIAL_LEVEL_RP_NAME),
					GeneratorSimulationConfiguration.INITIAL_TANK_LEVEL);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new Start(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T14:00:00.00Z"),
					(m, t) -> {
						return null;
					},
					(m, t) -> {
						((GeneratorUnitTesterModel)m).
								generatorRequiredPower.setNewValue(25.0, t);
					}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T14:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new GeneratorRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T16:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new Stop(t));
						return ret;
					},
					(m, t) -> {
						((GeneratorUnitTesterModel)m).
									generatorRequiredPower.setNewValue(0.0, t);
					})
			}	// end SimulationTestStep[]
		);	// end TestScenario
	}

	/** test scenario where the generator empty tank is refilled the used,
	 *  see Gherkin specification.										 	
	 * @throws VerboseException */
	protected static TestScenarioWithSimulation	tankRefill()
	throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"-----------------------------------------------------\n" +
			"Generator tank refilled\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: generator tank refill\n\n" +
			"      Scenario: generator produces after refilling the tank\n" +
			"        Given a standard generator with an empty tank\n" +
			"        When it is refilled\n" +
			"        Then the tank level goes up\n" +
			"        And the generator can produce more power\n" + 
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Generator tank refilled\n" +
			"-----------------------------------------------------",
			"fake-clock-URI",	// for simulation only test scenario, no clock needed
			START_INSTANT,
			END_INSTANT,
			GeneratorCoupledModel.URI,
			START_TIME,
			(ts, simParams) -> {
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.CAPACITY_RP_NAME),
					GeneratorSimulationConfiguration.TANK_CAPACITY);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.INITIAL_LEVEL_RP_NAME),
					0.0);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MIN_FUEL_CONSUMPTION_RP_NAME),
					Generator.MIN_FUEL_CONSUMPTION.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MAX_FUEL_CONSUMPTION_RP_NAME),
					Generator.MAX_FUEL_CONSUMPTION.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.LEVEL_QUANTUM_RP_NAME),
					GeneratorSimulationConfiguration.
												STANDARD_LEVEL_INTEGRATION_QUANTUM);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorFuelModel.URI,
						GeneratorFuelModel.MAX_OUT_POWER_RP_NAME),
					Generator.MAX_POWER.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorPowerModel.URI,
						GeneratorPowerModel.MAX_OUT_POWER_RP_NAME),
					Generator.MAX_POWER.getData());
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorUnitTesterModel.URI,
						GeneratorUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
				simParams.put(
					ModelI.createRunParameterName(
						GeneratorUnitTesterModel.URI,
						GeneratorUnitTesterModel.INITIAL_LEVEL_RP_NAME),
					0.0);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(
							new Refill(t,
									   new FuelQuantity(
										new Measure<Double>(
											GeneratorSimulationConfiguration.
														INITIAL_TANK_LEVEL,
											MeasurementUnit.LITERS))));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
						GeneratorUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Start(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T15:00:00.00Z"),
					(m, t) -> {
						return null;
					},
					(m, t) -> {
						((GeneratorUnitTesterModel)m).
								generatorRequiredPower.setNewValue(25.0, t);
					}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T15:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new GeneratorRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T16:00:00.00Z"),
					(m, t) -> {
						return null;
					},
					(m, t) -> {
						((GeneratorUnitTesterModel)m).
									generatorRequiredPower.setNewValue(0.0, t);
					}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T16:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new GeneratorRequiredPowerChanged(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					GeneratorUnitTesterModel.URI,
					Instant.parse("2025-10-20T17:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new Stop(t));
						return ret;
					},
					(m, t) -> { })
			}	// end SimulationTestStep[]
		);	// end TestScenario
	}
}
// -----------------------------------------------------------------------------
