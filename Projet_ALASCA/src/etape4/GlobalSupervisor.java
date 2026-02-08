package etape4;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import etape1.equipments.batteries.Batteries;
import etape1.equipments.generator.Generator;
import etape1.equipments.solar_panel.SolarPanel;
import etape2.GlobalCoupledModel;
import etape2.GlobalSimulationConfigurationI;
import etape2.equipments.generator.mil.GeneratorSimulationConfiguration;
import etape2.equipments.solar_panel.mil.DeterministicSunIntensityModel;
import etape2.equipments.solar_panel.mil.DeterministicSunRiseAndSetModel;
import etape2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
import etape2.equipments.solar_panel.mil.SunIntensityModelI;
import etape2.equipments.solar_panel.mil.SunRiseAndSetModelI;

/**
 * The class <code>GlobalSupervisor</code> implements the supervisor component
 * for simulated runs of the Etape 4 HEM application.
 *
 * <p>
 * Uses the etape4 ComponentSimulationArchitectures which includes
 * energy source models (Generator, SolarPanel, Batteries) in addition
 * to consumer equipment models.
 * </p>
 */
public class GlobalSupervisor extends AbstractComponent
{
	public static boolean		VERBOSE = false;
	public static int			X_RELATIVE_POSITION = 0;
	public static int			Y_RELATIVE_POSITION = 0;

	public static final String	SIL_SIM_ARCHITECTURE_URI = "hem-sil-simulator";

	protected static int		NUMBER_OF_STANDARD_THREADS = 2;
	protected static int		NUMBER_OF_SCHEDULABLE_THREADS = 0;

	protected final String					simArchitectureURI;
	protected TestScenarioWithSimulation	testScenario;

	protected GlobalSupervisor(
		TestScenarioWithSimulation testScenario,
		String simArchitectureURI
		) throws Exception
	{
		super(NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		assert	simArchitectureURI != null && !simArchitectureURI.isEmpty() :
				new PreconditionException(
						"simArchitectureURI != null && "
						+ "!simArchitectureURI.isEmpty()");

		this.testScenario = testScenario;
		this.simArchitectureURI = simArchitectureURI;

		if (VERBOSE) {
			this.tracer.get().setTitle("Global supervisor (etape4)");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	@Override
	public void execute() throws Exception
	{
		this.logMessage("Global supervisor (etape4) execution begins.");

		// Get the clock
		this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI,
							 this.testScenario.getClockURI());
		AcceleratedAndSimulationClock ac =
							(AcceleratedAndSimulationClock) this.getClock();

		// Create the simulation architecture using etape4's version
		// which includes energy source models
		ComponentModelArchitecture cma =
				ComponentSimulationArchitectures.
						createComponentSimulationArchitectures(
											this.simArchitectureURI,
											GlobalCoupledModel.URI,
											ac.getSimulatedTimeUnit(),
											ac.getAccelerationFactor());
		// Create the simulation supervision plug-in and install it
		SupervisorPlugin sp = new SupervisorPlugin(cma);
		sp.setPluginURI(GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI);
		this.installPlugin(sp);
		this.logMessage("plug-in installed.");
		// Construct the simulator from the architecture
		sp.constructSimulator();
		this.logMessage("simulator constructed.");
		Map<String, Object> simParams = new HashMap<>();
		this.testScenario.addToRunParameters(simParams);

		// Provide run parameters for all energy source SIL models.
		// These are distributed by the supervisor to all component models,
		// including submodels inside ElectricMeterCyPhy's local architecture.
		java.time.Instant startInstant =
			GlobalSimulationConfigurationI.START_INSTANT;

		// --- DeterministicSunRiseAndSetModel (global architecture) ---
		simParams.put(
			ModelI.createRunParameterName(
				DeterministicSunRiseAndSetModel.URI,
				SunRiseAndSetModelI.START_INSTANT_RP_NAME),
			startInstant);
		simParams.put(
			ModelI.createRunParameterName(
				DeterministicSunRiseAndSetModel.URI,
				SunRiseAndSetModelI.ZONE_ID_RP_NAME),
			SolarPanelSimulationConfigurationI.ZONE);

		// --- DeterministicSunIntensityModel (inside ElectricMeter) ---
		String sunIntensityURI = DeterministicSunIntensityModel.URI;
		simParams.put(
			ModelI.createRunParameterName(sunIntensityURI,
				SunIntensityModelI.START_INSTANT_RP_NAME),
			startInstant);
		simParams.put(
			ModelI.createRunParameterName(sunIntensityURI,
				SunIntensityModelI.ZONE_ID_RP_NAME),
			SolarPanelSimulationConfigurationI.ZONE);
		simParams.put(
			ModelI.createRunParameterName(sunIntensityURI,
				SunIntensityModelI.COMPUTATION_STEP_RP_NAME),
			0.5);

		// --- SolarPanelPowerSILModel (inside ElectricMeter) ---
		String solarPowerURI = "SolarPanelPowerSILModel";
		simParams.put(
			ModelI.createRunParameterName(solarPowerURI, "LATITUDE"),
			SolarPanelSimulationConfigurationI.LATITUDE);
		simParams.put(
			ModelI.createRunParameterName(solarPowerURI, "LONGITUDE"),
			SolarPanelSimulationConfigurationI.LONGITUDE);
		simParams.put(
			ModelI.createRunParameterName(solarPowerURI, "START_INSTANT"),
			startInstant);
		simParams.put(
			ModelI.createRunParameterName(solarPowerURI, "ZONE_ID"),
			SolarPanelSimulationConfigurationI.ZONE);
		simParams.put(
			ModelI.createRunParameterName(solarPowerURI, "MAX_POWER"),
			(double)(SolarPanelSimulationConfigurationI.NB_SQUARE_METERS
				* SolarPanel.CAPACITY_PER_SQUARE_METER.getData()));
		simParams.put(
			ModelI.createRunParameterName(solarPowerURI, "COMPUTATION_STEP"),
			0.25);

		// --- BatteriesPowerSILModel (inside ElectricMeter) ---
		String batteriesURI = "batteries-power-sil-model";
		simParams.put(
			ModelI.createRunParameterName(batteriesURI, "CAPACITY"),
			2.0 * 2.0 * Batteries.CAPACITY_PER_UNIT.getData());
		simParams.put(
			ModelI.createRunParameterName(batteriesURI, "INITIAL_LEVEL_RATIO"),
			0.5);
		simParams.put(
			ModelI.createRunParameterName(batteriesURI, "IN_POWER"),
			2.0 * Batteries.IN_POWER_PER_CELL.getData());
		simParams.put(
			ModelI.createRunParameterName(batteriesURI, "MAX_OUT_POWER_RP_NAME"),
			2.0 * Batteries.MAX_OUT_POWER_PER_CELL.getData());
		simParams.put(
			ModelI.createRunParameterName(batteriesURI, "LEVEL_QUANTUM"),
			300.0);

		// --- GeneratorFuelSILModel (inside ElectricMeter) ---
		String genFuelURI = "GeneratorFuelSILModel";
		simParams.put(
			ModelI.createRunParameterName(genFuelURI, "CAPACITY"),
			GeneratorSimulationConfiguration.TANK_CAPACITY);
		simParams.put(
			ModelI.createRunParameterName(genFuelURI, "INITIAL_LEVEL"),
			GeneratorSimulationConfiguration.INITIAL_TANK_LEVEL);
		simParams.put(
			ModelI.createRunParameterName(genFuelURI,
				"MIN_FUEL_CONSUMPTION_RP_NAME"),
			Generator.MIN_FUEL_CONSUMPTION.getData());
		simParams.put(
			ModelI.createRunParameterName(genFuelURI,
				"MAX_FUEL_CONSUMPTION_RP_NAME"),
			Generator.MAX_FUEL_CONSUMPTION.getData());
		simParams.put(
			ModelI.createRunParameterName(genFuelURI,
				"MAX_OUT_POWER_RP_NAME"),
			Generator.MAX_POWER.getData());
		simParams.put(
			ModelI.createRunParameterName(genFuelURI, "LEVEL_QUANTUM"),
			GeneratorSimulationConfiguration.
				STANDARD_LEVEL_INTEGRATION_QUANTUM);

		// --- GeneratorPowerSILModel (inside ElectricMeter) ---
		String genPowerURI = "GeneratorPowerSILModel";
		simParams.put(
			ModelI.createRunParameterName(genPowerURI,
				"MAX_OUT_POWER_RP_NAME"),
			Generator.MAX_POWER.getData());

		sp.setSimulationRunParameters(simParams);
		this.logMessage("run parameters set, simulation begins.");

		sp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(ac.getStartEpochNanos()),
					ac.getSimulatedStartTime().getSimulatedTime(),
					ac.getSimulatedDuration().getSimulatedDuration());

		// wait for the end of the simulation
		ac.waitUntilEnd();
		Thread.sleep(250L);
		this.logMessage(sp.getFinalReport().toString());

		this.logMessage("Global supervisor (etape4) execution ends.");
	}
}
