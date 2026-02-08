package etape2.equipments.kettle.mil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.SetEcoModeKettle;
import etape2.equipments.kettle.mil.events.SetMaxModeKettle;
import etape2.equipments.kettle.mil.events.SetNormalModeKettle;
import etape2.equipments.kettle.mil.events.SetPowerKettle;
import etape2.equipments.kettle.mil.events.SetSuspendedModeKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
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
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.exceptions.VerboseException;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunKettleUnitaryMILSimulation2</code> creates a simulator
 * for the kettle (water heater) and then runs a second test scenario using
 * ECO mode and suspended mode transitions.
 *
 * <p><strong>Description</strong></p>
 * <p>
 * This test exercises the kettle lifecycle with ECO mode and suspend/resume:
 * switch on, set ECO mode, start heating, suspend, resume to NORMAL mode,
 * stop heating, and switch off.
 * </p>
 *
 * <p>Created on : 2026-02-06</p>
 */
public class RunKettleUnitaryMILSimulation2
{
	public static boolean staticInvariants()
	{
		boolean ret = true;
		return ret;
	}

	public static void main(String[] args)
	{
		staticInvariants();

		try {
			Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors =
					new HashMap<>();

			atomicModelDescriptors.put(
					KettleElectricityModel.URI,
					AtomicHIOA_Descriptor.create(
							KettleElectricityModel.class,
							KettleElectricityModel.URI,
							KettleSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					KettleTemperatureModel.URI,
					AtomicHIOA_Descriptor.create(
							KettleTemperatureModel.class,
							KettleTemperatureModel.URI,
							KettleSimulationConfigurationI.TIME_UNIT,
							null));
			atomicModelDescriptors.put(
					KettleUnitTesterModel.URI,
					AtomicModelDescriptor.create(
							KettleUnitTesterModel.class,
							KettleUnitTesterModel.URI,
							KettleSimulationConfigurationI.TIME_UNIT,
							null));

			Map<String, CoupledModelDescriptor> coupledModelDescriptors =
					new HashMap<>();

			Set<String> submodels = new HashSet<String>();
			submodels.add(KettleElectricityModel.URI);
			submodels.add(KettleTemperatureModel.URI);
			submodels.add(KettleUnitTesterModel.URI);

			Map<EventSource, EventSink[]> connections =
					new HashMap<EventSource, EventSink[]>();

			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							SwitchOnKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									SwitchOnKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							SwitchOffKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									SwitchOffKettle.class),
							new EventSink(KettleTemperatureModel.URI,
									SwitchOffKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							HeatKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									HeatKettle.class),
							new EventSink(KettleTemperatureModel.URI,
									HeatKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							DoNotHeatKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									DoNotHeatKettle.class),
							new EventSink(KettleTemperatureModel.URI,
									DoNotHeatKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							SetPowerKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									SetPowerKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							SetEcoModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									SetEcoModeKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							SetNormalModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									SetNormalModeKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							SetMaxModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									SetMaxModeKettle.class)
					});
			connections.put(
					new EventSource(KettleUnitTesterModel.URI,
							SetSuspendedModeKettle.class),
					new EventSink[] {
							new EventSink(KettleElectricityModel.URI,
									SetSuspendedModeKettle.class)
					});

			// variable bindings
			Map<VariableSource, VariableSink[]> bindings =
					new HashMap<VariableSource, VariableSink[]>();

			bindings.put(
					new VariableSource("currentHeatingPower", Double.class,
							KettleElectricityModel.URI),
					new VariableSink[] {
							new VariableSink("currentHeatingPower", Double.class,
									KettleTemperatureModel.URI)
					});

			coupledModelDescriptors.put(
					KettleCoupledModel.URI,
					new CoupledHIOA_Descriptor(
							KettleCoupledModel.class,
							KettleCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							null,
							null,
							bindings));

			ArchitectureI architecture =
					new Architecture(
							KettleCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							KettleSimulationConfigurationI.TIME_UNIT);

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
			Instant.parse("2026-02-06T14:00:00.00Z");
	protected static Time START_TIME = new Time(0.0, TimeUnit.HOURS);

	protected static TestScenarioWithSimulation classical() throws VerboseException {
		return new TestScenarioWithSimulation(
				"-----------------------------------------------------\n" +
				"Classical Kettle ECO/Suspend Test\n\n" +
				"  Gherkin specification\n\n" +
				"    Feature: kettle (water heater) ECO mode and suspend control\n\n" +
				"      Scenario: kettle switched on\n" +
				"        Given a kettle that is off\n" +
				"        When it is switched on\n" +
				"        Then it is on, in standby mode (ON), with NORMAL mode\n" +
				"      Scenario: kettle set to ECO mode\n" +
				"        Given a kettle that is on\n" +
				"        When it is set to ECO mode\n" +
				"        Then the heating power level is set to 1000W\n" +
				"      Scenario: kettle starts heating (HeatKettle)\n" +
				"        Given a kettle that is on in ECO mode\n" +
				"        When the 'HeatKettle' command is given\n" +
				"        Then the water temperature rises at ECO power rate\n" +
				"      Scenario: kettle suspended\n" +
				"        Given a kettle that is heating in ECO mode\n" +
				"        When it is set to SUSPENDED mode\n" +
				"        Then the heating power drops to 0W (suspended)\n" +
				"      Scenario: kettle resumed to NORMAL mode\n" +
				"        Given a kettle that is suspended\n" +
				"        When it is set to NORMAL mode\n" +
				"        Then the heating power is restored to 2000W\n" +
				"      Scenario: kettle stops heating (DoNotHeatKettle)\n" +
				"        Given a kettle that is heating\n" +
				"        When it is asked to stop heating\n" +
				"        Then it returns to ON state and temperature starts cooling\n" +
				"      Scenario: kettle switched off\n" +
				"        Given a kettle that is on\n" +
				"        When it is switched off\n" +
				"        Then the machine is fully OFF and consumes zero power\n" +
				"-----------------------------------------------------\n",
				"\n-----------------------------------------------------\n" +
				"End Classical Kettle ECO/Suspend Test\n" +
				"-----------------------------------------------------",
				"fake-clock",
				START_INSTANT,
				END_INSTANT,
				KettleCoupledModel.URI,
				START_TIME,
				(ts, simParams) -> {
					simParams.put(
							ModelI.createRunParameterName(
									KettleUnitTesterModel.URI,
									KettleUnitTesterModel.TEST_SCENARIO_RP_NAME),
							ts);
				},
				new SimulationTestStep[] {
						// Switch on at 08:30
						new SimulationTestStep(
								KettleUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:30:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SwitchOnKettle(t));
									return ret;
								},
								(m, t) -> {}),
						// Set to ECO mode at 08:35
						new SimulationTestStep(
								KettleUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:35:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SetEcoModeKettle(t));
									return ret;
								},
								(m, t) -> {}),
						// Start heating at 08:40
						new SimulationTestStep(
								KettleUnitTesterModel.URI,
								Instant.parse("2026-02-06T08:40:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new HeatKettle(t));
									return ret;
								},
								(m, t) -> {}),
						// Suspend at 09:30
						new SimulationTestStep(
								KettleUnitTesterModel.URI,
								Instant.parse("2026-02-06T09:30:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SetSuspendedModeKettle(t));
									return ret;
								},
								(m, t) -> {}),
						// Resume to NORMAL mode at 10:00
						new SimulationTestStep(
								KettleUnitTesterModel.URI,
								Instant.parse("2026-02-06T10:00:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SetNormalModeKettle(t));
									return ret;
								},
								(m, t) -> {}),
						// Stop heating at 11:00
						new SimulationTestStep(
								KettleUnitTesterModel.URI,
								Instant.parse("2026-02-06T11:00:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new DoNotHeatKettle(t));
									return ret;
								},
								(m, t) -> {}),
						// Switch off at 12:00
						new SimulationTestStep(
								KettleUnitTesterModel.URI,
								Instant.parse("2026-02-06T12:00:00.00Z"),
								(m, t) -> {
									ArrayList<EventI> ret = new ArrayList<>();
									ret.add(new SwitchOffKettle(t));
									return ret;
								},
								(m, t) -> {})
				});
	}
}
// -----------------------------------------------------------------------------
