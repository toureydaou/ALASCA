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
import etape2.GlobalCoupledModel;
import etape2.GlobalSimulationConfigurationI;
import etape2.equipments.solar_panel.mil.DeterministicSunRiseAndSetModel;
import etape2.equipments.solar_panel.mil.SolarPanelSimulationConfigurationI;
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

		// Solar panel's DeterministicSunRiseAndSetModel requires
		// START_INSTANT and ZONE_ID run parameters
		simParams.put(
			ModelI.createRunParameterName(
				DeterministicSunRiseAndSetModel.URI,
				SunRiseAndSetModelI.START_INSTANT_RP_NAME),
			GlobalSimulationConfigurationI.START_INSTANT);
		simParams.put(
			ModelI.createRunParameterName(
				DeterministicSunRiseAndSetModel.URI,
				SunRiseAndSetModelI.ZONE_ID_RP_NAME),
			SolarPanelSimulationConfigurationI.ZONE);

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
