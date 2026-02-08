package etape2.equipments.laundry.mil;

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
import java.util.function.Consumer;

import etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed;
import etape2.equipments.laundry.mil.events.CancelWash;
import etape2.equipments.laundry.mil.events.SetColorModeLaundry;
import etape2.equipments.laundry.mil.events.SetDelicateModeLaundry;
import etape2.equipments.laundry.mil.events.SetIntensiveModeLaundry;
import etape2.equipments.laundry.mil.events.SetPowerLaundry;
import etape2.equipments.laundry.mil.events.SetSpinSpeed;
import etape2.equipments.laundry.mil.events.SetWashTemperature;
import etape2.equipments.laundry.mil.events.SetWhiteModeLaundry;
import etape2.equipments.laundry.mil.events.StartWash;
import etape2.equipments.laundry.mil.events.SwitchOffLaundry;
import etape2.equipments.laundry.mil.events.SwitchOnLaundry;
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
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
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.exceptions.VerboseException;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunLaundryUnitaryMILSimulation</code> creates a simulator
 * for the laundry machine and then runs a typical simulation.
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
 * <p>Created on : 2026-01-09</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class RunLaundryUnitaryMILSimulation {
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
	public static boolean staticInvariants() {
		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	public static void main(String[] args) {
		staticInvariants();

		try {
			// map that will contain the atomic model descriptors to construct
			// the simulation architecture
			Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
					new HashMap<>();

			// the laundry model simulating its electricity consumption is an
			// atomic HIOA model hence we use an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					LaundryElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							LaundryElectricityModel.class,
							LaundryElectricityModel.URI,
							LaundrySimulationConfigurationI.TIME_UNIT,
							null));

			// the laundry unit tester model only exchanges events, an
			// atomic model hence we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					LaundryUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							LaundryUnitTesterModel.class,
							LaundryUnitTesterModel.URI,
							LaundrySimulationConfigurationI.TIME_UNIT,
							null));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String, CoupledModelDescriptor> coupledModelDescriptors =
					new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(LaundryElectricityModel.URI);
			submodels.add(LaundryUnitTesterModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource, EventSink[]> connections =
					new HashMap<EventSource, EventSink[]>();

			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SetPowerLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SetPowerLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SwitchOnLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SwitchOnLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SwitchOffLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SwitchOffLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SetDelicateModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SetDelicateModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SetColorModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SetColorModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SetWhiteModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SetWhiteModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SetIntensiveModeLaundry.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SetIntensiveModeLaundry.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							StartWash.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									StartWash.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							CancelWash.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									CancelWash.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SetWashTemperature.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SetWashTemperature.class)
					});
			connections.put(
					new EventSource(LaundryUnitTesterModel.URI,
							SetSpinSpeed.class),
					new EventSink[] {
							new EventSink(LaundryElectricityModel.URI,
									SetSpinSpeed.class)
					});

			// coupled model descriptor
			coupledModelDescriptors.put(
					LaundryCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							LaundryCoupledModel.class,
							LaundryCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							null));

			// simulation architecture
			ArchitectureI architecture =
					new Architecture(
							LaundryCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							LaundrySimulationConfigurationI.TIME_UNIT);

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
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println(sr);
			((Consumer<String>) (m -> { if (m != null) System.out.println(m); }))
											.accept(classical.endMessage());
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------
	// Test scenarios
	// -------------------------------------------------------------------------

	/** the start instant used in the test scenarios. */
	protected static Instant START_INSTANT =
			Instant.parse("2026-01-09T08:00:00.00Z");
	/** the end instant used in the test scenarios. */
	protected static Instant END_INSTANT =
			Instant.parse("2026-01-09T20:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}. */
	protected static Time START_TIME = new Time(0.0, TimeUnit.HOURS);
	
	/*protected static TestScenarioWithSimulation	classical() throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"-----------------------------------------------------\n" +
			"Classical\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: hair dryer operation\n\n" +
			"      Scenario: hair dryer switched on\n" +
			"        Given a hair dryer that is off\n" +
			"        When it is switched on\n" +
			"        Then it is on and low\n" +
			"      Scenario: hair dryer set high\n" +
			"        Given a hair dryer that is on\n" +
			"        When it is set high\n" +
			"        Then it is on and high\n" +
			"      Scenario: hair dryer set low\n" +
			"        Given a hair dryer that is on\n" +
			"        When it is set low\n" +
			"        Then it is on and low\n" +
			"      Scenario: hair dryer switched off\n" +
			"        Given a hair dryer that is on\n" +
			"        When it is switched of\n" +
			"        Then it is off\n" +
			"-----------------------------------------------------\n",
			"\n-----------------------------------------------------\n" +
			"End Classical\n" +
			"-----------------------------------------------------",
			"fake-clock",	// for simulation only test scenario, no clock needed
			START_INSTANT,
			END_INSTANT,
			LaundryCoupledModel.URI,
			START_TIME,
			(ts, simParams) -> {
				simParams.put(
					ModelI.createRunParameterName(
							LaundryUnitTesterModel.URI,
							LaundryUnitTesterModel.TEST_SCENARIO_RP_NAME),
					ts);
			},
			new SimulationTestStep[]{
				new SimulationTestStep(
						LaundryUnitTesterModel.URI,
					Instant.parse("2025-10-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SwitchOnLaundry(t));
						return ret;
					},
					(m, t) -> {}),
				new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T14:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetColorModeLaundry(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T15:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SetColorModeLaundry(t));
							return ret;
						},
						(m, t) -> {}),
				new SimulationTestStep(
						LaundryUnitTesterModel.URI,
						Instant.parse("2025-10-20T16:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffLaundry(t));
							return ret;
						},
						(m, t) -> {})
			});
	}*/
	

	/** standard test scenario, see Gherkin specification. */
	protected static TestScenarioWithSimulation	classical() throws VerboseException {
		return new TestScenarioWithSimulation(
					"-----------------------------------------------------\n" +
							"Classical Laundry Machine Test\n\n" +
							"  Gherkin specification\n\n" +
							"    Feature: laundry machine washing and state control\n\n" +
							"      Scenario: laundry machine switched on\n" +
							"        Given a laundry machine that is off\n" +
							"        When it is switched on\n" +
							"        Then it is on, in standby mode (ON), and ready to receive wash commands\n" +
							"      Scenario: laundry machine set to delicate mode\n" +
							"        Given a laundry machine that is on\n" +
							"        When it is set to Delicate mode\n" +
							"        Then the washing power level is set to the delicate value\n" +
							"      Scenario: laundry machine set to color mode\n" +
							"        Given a laundry machine that is on\n" +
							"        When it is set to Color mode\n" +
							"        Then the washing power level is set to the color value\n" +
							"      Scenario: laundry machine set wash temperature\n" +
							"        Given a laundry machine that is on\n" +
							"        When the wash temperature is set to 40°C\n" +
							"        Then the machine registers the temperature setting\n" +
							"      Scenario: laundry machine set spin speed\n" +
							"        Given a laundry machine that is on\n" +
							"        When the spin speed is set to 1200 RPM\n" +
							"        Then the machine registers the spin speed setting\n" +
							"      Scenario: laundry machine starts washing\n" +
							"        Given a laundry machine that is on and configured\n" +
							"        When the 'StartWash' command is given\n" +
							"        Then the machine enters the WASHING state and consumes power\n" +
							"      Scenario: laundry machine set to white mode during wash\n" +
							"        Given a laundry machine that is washing\n" +
							"        When it is set to White mode\n" +
							"        Then the washing power level is adjusted to the white mode value\n" +
							"      Scenario: laundry machine set to intensive mode\n" +
							"        Given a laundry machine that is washing\n" +
							"        When it is set to Intensive mode\n" +
							"        Then the washing power level is increased to intensive value\n" +
							"      Scenario: laundry machine cancel wash\n" +
							"        Given a laundry machine that is washing\n" +
							"        When the wash is cancelled\n" +
							"        Then the machine returns to ON state and stops washing\n" +
							"      Scenario: laundry machine switched off\n" +
							"        Given a laundry machine that is on\n" +
							"        When it is switched off\n" +
							"        Then the machine is fully OFF and consumes zero power\n" +
							"-----------------------------------------------------\n",
					"\n-----------------------------------------------------\n" +
							"End Classical Laundry Machine Test\n" +
							"-----------------------------------------------------",
					"fake-clock",
					START_INSTANT,
					END_INSTANT,
					LaundryCoupledModel.URI,
					START_TIME,
					(ts, simParams) -> {
						simParams.put(
								ModelI.createRunParameterName(
										LaundryUnitTesterModel.URI,
										LaundryUnitTesterModel.TEST_SCENARIO_RP_NAME),
								ts);
					},
					new SimulationTestStep[] {
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T08:15:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SwitchOnLaundry(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T08:20:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SetDelicateModeLaundry(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T08:25:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SetColorModeLaundry(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T08:30:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SetWashTemperature(t, 40.0));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T08:35:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SetSpinSpeed(t, SpinSpeed.RPM_1200));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T08:40:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new StartWash(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T10:00:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new CancelWash(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T12:00:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SetWhiteModeLaundry(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T12:05:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SetIntensiveModeLaundry(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T12:30:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new StartWash(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T16:00:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new CancelWash(t));
										return ret;
									},
									(m, t) -> {}),
							new SimulationTestStep(
									LaundryUnitTesterModel.URI,
									Instant.parse("2026-01-09T17:00:00.00Z"),
									(m, t) -> {
										ArrayList<EventI> ret = new ArrayList<>();
										ret.add(new SwitchOffLaundry(t));
										return ret;
									},
									(m, t) -> {})
					});
	}
}

// -----------------------------------------------------------------------------
