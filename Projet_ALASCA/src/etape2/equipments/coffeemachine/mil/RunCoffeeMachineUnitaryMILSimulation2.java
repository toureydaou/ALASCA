package etape2.equipments.coffeemachine.mil;

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

import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.FillWaterCoffeeMachine.WaterValue;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.ServeCoffee;
import etape2.equipments.coffeemachine.mil.events.SetEcoModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetMaxModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetNormalModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetPowerCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SetSuspendedModeCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
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
 * The class <code>RunCoffeeMachineUnitarySimulation</code> creates a simulator
 * for the coffeeMachine and then runs a typical simulation.
 *
 * <p><strong>Description</strong></p>
 * 
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
public class			RunCoffeeMachineUnitaryMILSimulation2
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
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		//ret &= CoffeeMachineSimulationConfigurationI.staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	public static void main(String[] args)
	{
		staticInvariants();

		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

			// the coffeeMachine models simulating its electricity consumption, its
			// temperatures and the external temperature are atomic HIOA models
			// hence we use an AtomicHIOA_Descriptor(s)
			atomicModelDescriptors.put(
					CoffeeMachineElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							CoffeeMachineElectricityModel.class,
							CoffeeMachineElectricityModel.URI,
							CoffeeMachineSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					CoffeeMachineTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							CoffeeMachineTemperatureModel.class,
							CoffeeMachineTemperatureModel.URI,
							CoffeeMachineSimulationConfigurationI.TIME_UNIT,
							null));
			
			// the coffeeMachine unit tester model only exchanges event, an
			// atomic model hence we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					CoffeeMachineUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							CoffeeMachineUnitTesterModel.class,
							CoffeeMachineUnitTesterModel.URI,
							CoffeeMachineSimulationConfigurationI.TIME_UNIT,
							null));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(CoffeeMachineElectricityModel.URI);
			submodels.add(CoffeeMachineTemperatureModel.URI);
			
			submodels.add(CoffeeMachineUnitTesterModel.URI);
			
			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI,
									SetPowerCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetPowerCoffeeMachine.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI,
									SwitchOnCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SwitchOnCoffeeMachine.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI,
									SwitchOffCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SwitchOffCoffeeMachine.class),
							new EventSink(CoffeeMachineTemperatureModel.URI,
										  SwitchOffCoffeeMachine.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, MakeCoffee.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  MakeCoffee.class),
							new EventSink(CoffeeMachineTemperatureModel.URI,
									MakeCoffee.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, DoNotHeat.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  DoNotHeat.class),
							new EventSink(CoffeeMachineTemperatureModel.URI,
										  DoNotHeat.class)
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, ServeCoffee.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  ServeCoffee.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetEcoModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetEcoModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetMaxModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetMaxModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetNormalModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetNormalModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, SetSuspendedModeCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
										  SetSuspendedModeCoffeeMachine.class),
					});
			connections.put(
					new EventSource(CoffeeMachineUnitTesterModel.URI, FillWaterCoffeeMachine.class),
					new EventSink[] {
							new EventSink(CoffeeMachineElectricityModel.URI,
									FillWaterCoffeeMachine.class),
					});

			// variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			
			bindings.put(new VariableSource("currentHeatingPower",
											Double.class,
											CoffeeMachineElectricityModel.URI),
						 new VariableSink[] {
								 new VariableSink("currentHeatingPower",
										 		  Double.class,
										 		  CoffeeMachineTemperatureModel.URI)
						 });
			
			bindings.put(new VariableSource("currentWaterLevel",
											Double.class,
											CoffeeMachineElectricityModel.URI),
						 new VariableSink[] {
								 new VariableSink("currentWaterLevel",
										 		  Double.class,
										 		  CoffeeMachineTemperatureModel.URI)
						 });

			// coupled model descriptor
			coupledModelDescriptors.put(
					CoffeeMachineCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							CoffeeMachineCoupledModel.class,
							CoffeeMachineCoupledModel.URI,
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
							CoffeeMachineCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							CoffeeMachineSimulationConfigurationI.TIME_UNIT);

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
									Instant.parse("2025-10-20T12:00:00.00Z");
	/** the end instant used in the test scenarios.							*/
	protected static Instant	END_INSTANT =
									Instant.parse("2025-10-20T14:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}.												*/
	protected static Time		START_TIME = new Time(0.0, TimeUnit.HOURS);

	/** standard test scenario, see Gherkin specification.				 	*/
	protected static TestScenario	CLASSICAL =
		new TestScenario(
				"-----------------------------------------------------\n" +
				"Classical CoffeeMachine Heating Test\n\n" +
				"  Gherkin specification\n\n" +
				"    Feature: coffeeMachine heating and state control\n\n" +
				"      Scenario: coffeeMachine switched on\n" +
				"        Given a coffeeMachine that is off\n" +
				"        When it is switched on\n" +
				"        Then it is on, in standby mode (ON), and ready to receive filling water commands\n" +
				"      Scenario: coffeeMachine filled with water\n" +
				"        Given a coffeeMachine that is on\n" +
				"        When it is filled with water\n" +
				"        Then it is on, in standby mode (ON), and ready to receive heating commands\n" +
				"      Scenario: coffeeMachine set to a specific mode (e.g., ECO)\n" +
				"        Given a coffeeMachine that is on and not heating\n" +
				"        When it is set to Eco mode\n" +
				"        Then the heating power level is set to the high (Eco) value\n" +
				"      Scenario: coffeeMachine starts heating (MakeCoffee)\n" +
				"        Given a coffeeMachine that is on, has water, and is not heating\n" +
				"        When the 'MakeCoffee' command is given\n" +
				"        Then the machine enters the HEATING state and consumes power at the current level\n" +
				"      Scenario: coffeeMachine stops heating (DoNotHeat)\n" +
				"        Given a coffeeMachine that is heating\n" +
				"        When it is asked not to heat (DoNotHeat command)\n" +
				"        Then it returns to the ON state and NORMAL mode and stops consuming heating power\n" +
				"     Scenario: coffeeMachine  serve coffee (ServeCoffee)\n" +
				"        Given a coffeeMachine that is on\n" +
				"        When it is asked to serve coffee (ServeCoffee command)\n" +
				"        Then it stay on the ON state and stops consuming heating power\n" +
				"     Scenario: coffeeMachine  serve coffee (ServeCoffee)\n" +
				"        Given a coffeeMachine that is on\n" +
				"        When it is asked to serve coffee (ServeCoffee command)\n" +
				"        Then it stay on the ON state and stops consuming heating power\n" +
				"     Scenario: coffeeMachine  serve coffee (ServeCoffee)\n" +
				"        Given a coffeeMachine that is on\n" +
				"        When it is asked to serve coffee (ServeCoffee command)\n" +
				"        Then it stay on the ON state and stops consuming heating power\n" +
				"      Scenario: coffeeMachine switched off\n" +
				"        Given a coffeeMachine that is on\n" +
				"        When it is switched off\n" +
				"        Then the machine is fully OFF and consumes zero power\n" +
				"-----------------------------------------------------\n",
				"\n-----------------------------------------------------\n" +
				"End Classical CoffeeMachine Test\n" +
				"-----------------------------------------------------",
			START_INSTANT,
			END_INSTANT,
			START_TIME,
			(se, ts) -> { 
				HashMap<String, Object> simParams = new HashMap<>();
				simParams.put(
					ModelI.createRunParameterName(
						CoffeeMachineUnitTesterModel.URI,
						CoffeeMachineUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
				se.setSimulationRunParameters(simParams);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					CoffeeMachineUnitTesterModel.URI,
					Instant.parse("2025-10-20T12:30:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SwitchOnCoffeeMachine(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:35:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new FillWaterCoffeeMachine(t, new WaterValue(1.0)));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:40:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetEcoModeCoffeeMachine(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:45:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new MakeCoffee(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T12:55:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new DoNotHeat(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:05:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new ServeCoffee(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:10:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new ServeCoffee(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:15:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new ServeCoffee(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						CoffeeMachineUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffCoffeeMachine(t));
							return ret;
						},
						(m, t) -> {})
			});
}
// -----------------------------------------------------------------------------
