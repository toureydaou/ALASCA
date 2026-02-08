package etape2.equipments.kettle.mil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape2.equipments.kettle.mil.events.DoNotHeatKettle;
import etape2.equipments.kettle.mil.events.HeatKettle;
import etape2.equipments.kettle.mil.events.SetEcoModeKettle;
import etape2.equipments.kettle.mil.events.SetMaxModeKettle;
import etape2.equipments.kettle.mil.events.SetNormalModeKettle;
import etape2.equipments.kettle.mil.events.SetPowerKettle;
import etape2.equipments.kettle.mil.events.SetSuspendedModeKettle;
import etape2.equipments.kettle.mil.events.SwitchOffKettle;
import etape2.equipments.kettle.mil.events.SwitchOnKettle;
import fr.sorbonne_u.components.cyphy.utils.tests.AbstractTestScenarioBasedAtomicModel;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleUnitTesterModel</code> defines a model that is
 * used to test the models defining the kettle (water heater) simulator.
 *
 * <p>Created on : 2026-02-06</p>
 */
@ModelExternalEvents(exported = {
		SwitchOnKettle.class, SwitchOffKettle.class,
		SetPowerKettle.class, HeatKettle.class, DoNotHeatKettle.class,
		SetEcoModeKettle.class, SetMaxModeKettle.class,
		SetSuspendedModeKettle.class, SetNormalModeKettle.class
})
// -----------------------------------------------------------------------------
public class KettleUnitTesterModel extends AbstractTestScenarioBasedAtomicModel {
	private static final long serialVersionUID = 1L;
	public static final String URI = KettleUnitTesterModel.class.getSimpleName();
	public static boolean VERBOSE = true;
	public static boolean DEBUG = false;
	public static final String TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";

	public KettleUnitTesterModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
		String testScenarioName = ModelI.createRunParameterName(this.getURI(), TEST_SCENARIO_RP_NAME);

		assert simParams != null : new MissingRunParameterException("simParams != null");
		assert simParams.containsKey(testScenarioName) : new MissingRunParameterException(testScenarioName);

		this.setTestScenario((TestScenarioWithSimulation) simParams.get(testScenarioName));
	}

	@Override
	public SimulationReportI getFinalReport() {
		return null;
	}
}
// -----------------------------------------------------------------------------
