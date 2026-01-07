package fr.sorbonne_u.components.hem2025e2.equipments.heater.mil;

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
import java.time.Instant;
import java.util.ArrayList;
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SetPowerHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SetPowerHeater.PowerValue;
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
import fr.sorbonne_u.exceptions.VerboseException;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunHeaterUnitarySimulation</code> creates a simulator
 * for the heater and then runs a typical simulation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The simulation architecture for the heater contains four atomic models
 * composed under a coupled model:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2025-e2/HeaterUnitTestArchitecture.png"/></p>
 * <p>
 * The {@code HeaterUnitTesterModel} emits events corresponding to actions of
 * a user mainly towards the {@code HeaterElectricityModel} keeping track of
 * the state of the heater and its power consumption in an exported variable
 * {@code currentIntensity} not used here. The {@code ExternalTemperatureModel}
 * simulates the temperature outside, which is taken into account by the
 * {@code HeaterTemperatureModel} that simulates the room temperature. As the
 * room temperature depends upon the fact that the heater actually heats or
 * not and with which power, relevant events that changes the status of the
 * heater are propagated to the {@code HeaterTemperatureModel}, which also
 * imports the variable {@code currentHeatingPower} from the
 * {@code HeaterElectricityModel} as it influences the quickness of the
 * temperature raise when heating.
 * </p>
 * <p>
 * The code of the {@code main} method shows how to use simulation model
 * descriptors to create the description of a simulation architecture and then
 * create an instance of this architecture by instantiating and connecting the
 * models. Note how models are described by atomic model descriptors and coupled
 * model descriptors and then the connections between coupled models and their
 * submodels as well as exported events and variables to imported ones are
 * described by different maps. In this example, only connections of events and
 * bindings of variables between models within this architecture are necessary,
 * but when creating coupled models, they can also import and export events and
 * variables consumed and produced by their submodels.
 * </p>
 * <p>
 * The architecture object is the root of this description and it provides
 * the method {@code constructSimulator} that instantiate the models and
 * connect them. This method returns the reference on the simulator attached
 * to the root coupled model in the architecture instance, which is then used
 * to perform simulation runs by calling the method
 * {@code doStandAloneSimulation}.
 * </p>
 * <p>
 * The descriptors and maps can be viewed as kinds of nodes in the abstract
 * syntax tree of an architectural language that does not have a concrete
 * syntax yet.
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
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunHeaterUnitaryMILSimulation
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
		ret &= HeaterSimulationConfigurationI.staticInvariants();
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

			// the heater models simulating its electricity consumption, its
			// temperatures and the external temperature are atomic HIOA models
			// hence we use an AtomicHIOA_Descriptor(s)
			atomicModelDescriptors.put(
					HeaterElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							HeaterElectricityModel.class,
							HeaterElectricityModel.URI,
							HeaterSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					HeaterTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							HeaterTemperatureModel.class,
							HeaterTemperatureModel.URI,
							HeaterSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					ExternalTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							ExternalTemperatureModel.class,
							ExternalTemperatureModel.URI,
							HeaterSimulationConfigurationI.TIME_UNIT,
							null));
			// the heater unit tester model only exchanges event, an
			// atomic model hence we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					HeaterUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							HeaterUnitTesterModel.class,
							HeaterUnitTesterModel.URI,
							HeaterSimulationConfigurationI.TIME_UNIT,
							null));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HeaterElectricityModel.URI);
			submodels.add(HeaterTemperatureModel.URI);
			submodels.add(ExternalTemperatureModel.URI);
			submodels.add(HeaterUnitTesterModel.URI);
			
			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
					new EventSource(HeaterUnitTesterModel.URI,
									SetPowerHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  SetPowerHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.URI,
									SwitchOnHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  SwitchOnHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.URI,
									SwitchOffHeater.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  SwitchOffHeater.class),
							new EventSink(HeaterTemperatureModel.URI,
										  SwitchOffHeater.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.URI, Heat.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  Heat.class),
							new EventSink(HeaterTemperatureModel.URI,
										  Heat.class)
					});
			connections.put(
					new EventSource(HeaterUnitTesterModel.URI, DoNotHeat.class),
					new EventSink[] {
							new EventSink(HeaterElectricityModel.URI,
										  DoNotHeat.class),
							new EventSink(HeaterTemperatureModel.URI,
										  DoNotHeat.class)
					});

			// variable bindings between exporting and importing models
			Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();

			bindings.put(new VariableSource("externalTemperature",
											Double.class,
											ExternalTemperatureModel.URI),
						 new VariableSink[] {
								 new VariableSink("externalTemperature",
										 		  Double.class,
										 		  HeaterTemperatureModel.URI)
						 });
			bindings.put(new VariableSource("currentHeatingPower",
											Double.class,
											HeaterElectricityModel.URI),
						 new VariableSink[] {
								 new VariableSink("currentHeatingPower",
										 		  Double.class,
										 		  HeaterTemperatureModel.URI)
						 });

			// coupled model descriptor
			coupledModelDescriptors.put(
					HeaterCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							HeaterCoupledModel.class,
							HeaterCoupledModel.URI,
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
							HeaterCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							HeaterSimulationConfigurationI.TIME_UNIT);

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
									Instant.parse("2025-10-20T18:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}.												*/
	protected static Time		START_TIME = new Time(0.0, TimeUnit.HOURS);

	/** standard test scenario, see Gherkin specification.				 	
	 * @throws VerboseException */
	protected static TestScenarioWithSimulation	classical() throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"-----------------------------------------------------\n" +
			"Classical\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: heater operation\n\n" +
			"      Scenario: heater switched on\n" +
			"        Given a heater that is off\n" +
			"        When it is switched on\n" +
			"        Then it is on but not heating though set at the highest power level\n" +
			"      Scenario: heater heats\n" +
			"        Given a heater that is on and not heating\n" +
			"        When it is asked to heat\n" +
			"        Then it is on and it heats at the current power level\n" +
			"      Scenario: heater stops heating\n" +
			"        Given a hair dryer that is heating\n" +
			"        When it is asked not to heat\n" +
			"        Then it is on but it stops heating\n" +
			"      Scenario: heater heats\n" +
			"        Given a heater that is on and not heating\n" +
			"        When it is asked to heat\n" +
			"        Then it is on and it heats at the current power level\n" +
			"      Scenario: heater set a different power level\n" +
			"        Given a heater that is heating\n" +
			"        When it is set to a new power level\n" +
			"        Then it is on and it heats at the new power level\n" +
			"      Scenario: hair dryer switched off\n" +
			"        Given a hair dryer that is on\n" +
			"        When it is switched of\n" +
			"        Then it is off\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Classical\n" +
			"-----------------------------------------------------",
			"fake-clock-URI",	// for simulation only test scenario, no clock needed
			START_INSTANT,
			END_INSTANT,
			HeaterCoupledModel.URI,
			START_TIME,
			(ts, simParams) -> {
				simParams.put(
					ModelI.createRunParameterName(
						HeaterUnitTesterModel.URI,
						HeaterUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
					HeaterUnitTesterModel.URI,
					Instant.parse("2025-10-20T12:30:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SwitchOnHeater(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Heat(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T13:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new DoNotHeat(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new Heat(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetPowerHeater(t,
									   				   new PowerValue(880.0)));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						HeaterUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:30:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffHeater(t));
							return ret;
						},
						(m, t) -> {})
			});
	}
}
// -----------------------------------------------------------------------------
