package etape2.equipments.laundry.mil;

import java.time.Instant;
import java.util.ArrayList;
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
 * The class <code>RunLaundryUnitaryMILSimulation2</code> creates a simulator
 * for the laundry machine and then runs a second test scenario focusing on
 * white and intensive wash modes with different temperature and spin settings.
 *
 * <p><strong>Description</strong></p>
 * <p>
 * This test exercises a different laundry lifecycle: switch on, set white mode,
 * configure high temperature (60°C) and high spin (1200 RPM), start washing,
 * switch to intensive mode mid-wash, cancel wash, then switch off.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class RunLaundryUnitaryMILSimulation2 {
	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

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
			Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
					new HashMap<>();

			atomicModelDescriptors.put(
					LaundryElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							LaundryElectricityModel.class,
							LaundryElectricityModel.URI,
							LaundrySimulationConfigurationI.TIME_UNIT,
							null));

			atomicModelDescriptors.put(
					LaundryUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							LaundryUnitTesterModel.class,
							LaundryUnitTesterModel.URI,
							LaundrySimulationConfigurationI.TIME_UNIT,
							null));

			Map<String, CoupledModelDescriptor> coupledModelDescriptors =
					new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(LaundryElectricityModel.URI);
			submodels.add(LaundryUnitTesterModel.URI);

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

			// coupled model descriptor (no variable bindings for laundry)
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

			ArchitectureI architecture =
					new Architecture(
							LaundryCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							LaundrySimulationConfigurationI.TIME_UNIT);

			SimulatorI se = architecture.constructSimulator();
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

	protected static Instant START_INSTANT =
			Instant.parse("2026-02-06T08:00:00.00Z");
	protected static Instant END_INSTANT =
			Instant.parse("2026-02-06T20:00:00.00Z");
	protected static Time START_TIME = new Time(0.0, TimeUnit.HOURS);

	protected static TestScenarioWithSimulation classical() throws VerboseException {
		return new TestScenarioWithSimulation(
				"-----------------------------------------------------\n" +
				"Classical Laundry Machine White/Intensive Test\n\n" +
				"  Gherkin specification\n\n" +
				"    Feature: laundry machine white and intensive wash control\n\n" +
				"      Scenario: laundry machine switched on\n" +
				"        Given a laundry machine that is off\n" +
				"        When it is switched on\n" +
				"        Then it is on, in standby mode (ON)\n" +
				"      Scenario: laundry machine set to white mode\n" +
				"        Given a laundry machine that is on\n" +
				"        When it is set to White mode\n" +
				"        Then the washing power level is set to the white value\n" +
				"      Scenario: laundry machine set wash temperature to 60°C\n" +
				"        Given a laundry machine that is on\n" +
				"        When the wash temperature is set to 60°C\n" +
				"        Then the machine registers the high temperature setting\n" +
				"      Scenario: laundry machine set spin speed to 1200 RPM\n" +
				"        Given a laundry machine that is on\n" +
				"        When the spin speed is set to 1200 RPM\n" +
				"        Then the machine registers the high spin speed setting\n" +
				"      Scenario: laundry machine starts washing\n" +
				"        Given a laundry machine that is on and configured\n" +
				"        When the 'StartWash' command is given\n" +
				"        Then the machine enters the WASHING state\n" +
				"      Scenario: laundry machine switched to intensive mode mid-wash\n" +
				"        Given a laundry machine that is washing in white mode\n" +
				"        When it is set to Intensive mode\n" +
				"        Then the washing power is increased to intensive value\n" +
				"      Scenario: laundry machine wash cancelled\n" +
				"        Given a laundry machine that is washing\n" +
				"        When the wash is cancelled\n" +
				"        Then the machine returns to ON state\n" +
				"      Scenario: laundry machine switched off\n" +
				"        Given a laundry machine that is on\n" +
				"        When it is switched off\n" +
				"        Then the machine is fully OFF and consumes zero power\n" +
				"-----------------------------------------------------\n",
				"\n-----------------------------------------------------\n" +
				"End Classical Laundry Machine White/Intensive Test\n" +
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
						// Switch on at 08:15
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:15:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SwitchOnLaundry(t));
									return ret;
								},
								(m, t) -> {}),
						// Set to White mode at 08:20
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:20:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SetWhiteModeLaundry(t));
									return ret;
								},
								(m, t) -> {}),
						// Set wash temperature to 60°C at 08:25
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:25:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SetWashTemperature(t, 60.0));
									return ret;
								},
								(m, t) -> {}),
						// Set spin speed to 1200 RPM at 08:30
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:30:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SetSpinSpeed(t, SpinSpeed.RPM_1200));
									return ret;
								},
								(m, t) -> {}),
						// Start washing at 08:40
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:40:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new StartWash(t));
									return ret;
								},
								(m, t) -> {}),
						// Switch to Intensive mode mid-wash at 09:30
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T09:30:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SetIntensiveModeLaundry(t));
									return ret;
								},
								(m, t) -> {}),
						// Cancel wash at 11:00
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T11:00:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new CancelWash(t));
									return ret;
								},
								(m, t) -> {}),
						// Switch off at 12:00
						new SimulationTestStep(
								LaundryUnitTesterModel.URI,
								Instant.parse("2026-02-06T12:00:00.00Z"),
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
