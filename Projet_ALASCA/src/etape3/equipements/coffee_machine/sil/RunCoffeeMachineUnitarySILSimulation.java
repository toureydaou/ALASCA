package etape3.equipements.coffee_machine.sil;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import etape2.equipments.coffeemachine.mil.CoffeeMachineCoupledModel;
import etape2.equipments.coffeemachine.mil.CoffeeMachineUnitTesterModel;
import etape2.equipments.coffeemachine.mil.events.*;
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
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
 * The class <code>RunCoffeeMachineUnitarySILSimulation</code> creates a
 * standalone real time simulator for the coffee machine and then runs a
 * typical simulation scenario.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This class shows how to create and configure a standalone real time SIL
 * simulation that can be used to test the behavior of the coffee machine
 * without integrating it with the other equipment in the home energy management
 * system. It creates the simulation architecture and then runs a typical
 * test scenario.
 * </p>
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
 * <p>Created on : 2025-01-07</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunCoffeeMachineUnitarySILSimulation
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** the acceleration factor used in the real time SIL simulations.	 	*/
	public static final double		ACCELERATION_FACTOR = 3600.0;
	/** the time unit used in the simulation.								*/
	public static final TimeUnit	TIME_UNIT = TimeUnit.HOURS;

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	public static void main(String[] args)
	{
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			// the coffee machine state model only exchanges events, an atomic model
			// hence we use an RTAtomicModelDescriptor
			atomicModelDescriptors.put(
					CoffeeMachineStateSILModel.URI,
					RTAtomicModelDescriptor.create(
							CoffeeMachineStateSILModel.class,
							CoffeeMachineStateSILModel.URI,
							TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// the coffee machine models simulating its electricity consumption and
			// temperature are atomic HIOA models hence we use RTAtomicHIOA_Descriptor(s)
			atomicModelDescriptors.put(
					CoffeeMachineElectricitySILModel.URI,
					RTAtomicHIOA_Descriptor.create(
							CoffeeMachineElectricitySILModel.class,
							CoffeeMachineElectricitySILModel.URI,
							TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					CoffeeMachineTemperatureSILModel.URI,
					RTAtomicHIOA_Descriptor.create(
							CoffeeMachineTemperatureSILModel.class,
							CoffeeMachineTemperatureSILModel.URI,
							TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// the coffee machine unit tester model only exchanges events, an
			// atomic model hence we use an RTAtomicModelDescriptor
			atomicModelDescriptors.put(
					CoffeeMachineUnitTesterModel.URI,
					RTAtomicModelDescriptor.create(
							CoffeeMachineUnitTesterModel.class,
							CoffeeMachineUnitTesterModel.URI,
							TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(CoffeeMachineStateSILModel.URI);
			submodels.add(CoffeeMachineElectricitySILModel.URI);
			submodels.add(CoffeeMachineTemperatureSILModel.URI);
			submodels.add(CoffeeMachineUnitTesterModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			// connections from unit tester to state model
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, SwitchOnCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, SwitchOnCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, SwitchOffCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, SwitchOffCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, DoNotHeat.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, DoNotHeat.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, SetEcoModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, SetEcoModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, SetMaxModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, SetMaxModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, SetNormalModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, SetNormalModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, SetSuspendedModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, SetSuspendedModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, MakeCoffee.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, MakeCoffee.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, ServeCoffee.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, ServeCoffee.class)
					});
			connections.put(
				new EventSource(CoffeeMachineUnitTesterModel.URI, FillWaterCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineStateSILModel.URI, FillWaterCoffeeMachine.class)
					});

			// connections from state model to electricity and temperature models
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, SwitchOnCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI,
								  SwitchOnCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, SwitchOffCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI,
								  SwitchOffCoffeeMachine.class),
					new EventSink(CoffeeMachineTemperatureSILModel.URI,
								  SwitchOffCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, DoNotHeat.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI, DoNotHeat.class),
					new EventSink(CoffeeMachineTemperatureSILModel.URI, DoNotHeat.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, SetEcoModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI,
								  SetEcoModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, SetMaxModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI,
								  SetMaxModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, SetNormalModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI,
								  SetNormalModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, SetSuspendedModeCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI,
								  SetSuspendedModeCoffeeMachine.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, MakeCoffee.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI, MakeCoffee.class),
					new EventSink(CoffeeMachineTemperatureSILModel.URI, MakeCoffee.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, ServeCoffee.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI, ServeCoffee.class)
					});
			connections.put(
				new EventSource(CoffeeMachineStateSILModel.URI, FillWaterCoffeeMachine.class),
				new EventSink[] {
					new EventSink(CoffeeMachineElectricitySILModel.URI,
								  FillWaterCoffeeMachine.class)
					});

			// variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			bindings.put(
				new VariableSource("currentHeatingPower",
								   Double.class,
								   CoffeeMachineElectricitySILModel.URI),
				new VariableSink[] {
						new VariableSink("currentHeatingPower",
										 Double.class,
										 CoffeeMachineTemperatureSILModel.URI)
				});
			bindings.put(
				new VariableSource("currentWaterLevel",
								   Double.class,
								   CoffeeMachineElectricitySILModel.URI),
				new VariableSink[] {
						new VariableSink("currentWaterLevel",
										 Double.class,
										 CoffeeMachineTemperatureSILModel.URI)
				});

			// coupled model descriptor
			coupledModelDescriptors.put(
					CoffeeMachineCoupledModel.URI,
					new RTCoupledHIOA_Descriptor(
							CoffeeMachineCoupledModel.class,
							CoffeeMachineCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings,
							ACCELERATION_FACTOR));

			// simulation architecture
			ArchitectureI architecture =
					new RTArchitecture(
							CoffeeMachineCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							TIME_UNIT);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();
			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

			// run a CLASSICAL test scenario
			TestScenarioWithSimulation classical = classical();
			System.out.println(classical.beginMessage());
			Map<String, Object> classicalRunParameters =
												new HashMap<String, Object>();
			classical.addToRunParameters(classicalRunParameters);
			se.setSimulationRunParameters(classicalRunParameters);
			Time startTime = classical.getStartTime();
			Duration d = classical.getEndTime().subtract(startTime);
			long realTimeStart = System.currentTimeMillis() + 200;
			se.startRTSimulation(realTimeStart,
								 startTime.getSimulatedTime(),
								 d.getSimulatedDuration());
			long executionDuration =
				new Double(
						TIME_UNIT.toMillis(1)
							* (d.getSimulatedDuration()/ACCELERATION_FACTOR)).
																	longValue();
			Thread.sleep(executionDuration + 2000L);
			System.out.println(classical.endMessage());
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	// -------------------------------------------------------------------------
	// Test scenarios
	// -------------------------------------------------------------------------

	/** the start instant used in the test scenarios.						*/
	protected static Instant	START_INSTANT =
									Instant.parse("2025-10-20T08:00:00.00Z");
	/** the end instant used in the test scenarios.							*/
	protected static Instant	END_INSTANT =
									Instant.parse("2025-10-20T14:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}.												*/
	protected static Time		START_TIME = new Time(0.0, TimeUnit.HOURS);

	/** standard test scenario for coffee machine.
	 * @throws VerboseException */
	protected static TestScenarioWithSimulation	classical() throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"-----------------------------------------------------\n" +
			"Classical Coffee Machine Test\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: coffee machine operation\n\n" +
			"      Scenario: coffee machine switched on\n" +
			"        Given a coffee machine that is off\n" +
			"        When it is switched on\n" +
			"        Then it is on in suspend mode\n" +
			"      Scenario: coffee machine set to normal mode\n" +
			"        Given a coffee machine that is on\n" +
			"        When it is set to normal mode\n" +
			"        Then it is on and consuming normal power\n" +
			"      Scenario: coffee machine starts heating\n" +
			"        Given a coffee machine in normal mode\n" +
			"        When it is asked to heat\n" +
			"        Then it is on and it heats the water\n" +
			"      Scenario: coffee machine makes coffee\n" +
			"        Given a coffee machine that is heating\n" +
			"        When it makes a coffee\n" +
			"        Then the water level decreases\n" +
			"      Scenario: coffee machine stops heating\n" +
			"        Given a coffee machine that is heating\n" +
			"        When it is asked not to heat\n" +
			"        Then it is on but it stops heating\n" +
			"      Scenario: coffee machine set to eco mode\n" +
			"        Given a coffee machine that is on\n" +
			"        When it is set to eco mode\n" +
			"        Then it is on and consuming eco power\n" +
			"      Scenario: coffee machine switched off\n" +
			"        Given a coffee machine that is on\n" +
			"        When it is switched off\n" +
			"        Then it is off\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Classical Coffee Machine Test\n" +
			"-----------------------------------------------------",
			"fake-clock-URI",	// for simulation only test scenario, no clock needed
			START_INSTANT,
			END_INSTANT,
			CoffeeMachineCoupledModel.URI,
			START_TIME,
			(ts, simParams) -> {
				simParams.put(
					ModelI.createRunParameterName(
						CoffeeMachineUnitTesterModel.URI,
						CoffeeMachineUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					CoffeeMachineUnitTesterModel.URI,
					Instant.parse("2025-10-20T08:30:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SwitchOnCoffeeMachine(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					CoffeeMachineUnitTesterModel.URI,
					Instant.parse("2025-10-20T09:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SetNormalModeCoffeeMachine(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					CoffeeMachineUnitTesterModel.URI,
					Instant.parse("2025-10-20T10:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new MakeCoffee(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					CoffeeMachineUnitTesterModel.URI,
					Instant.parse("2025-10-20T10:30:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new DoNotHeat(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					CoffeeMachineUnitTesterModel.URI,
					Instant.parse("2025-10-20T11:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SetEcoModeCoffeeMachine(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
					CoffeeMachineUnitTesterModel.URI,
					Instant.parse("2025-10-20T12:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SwitchOffCoffeeMachine(t));
						return ret;
					},
					(m, t) -> {})
			});
	}
}
// -----------------------------------------------------------------------------
