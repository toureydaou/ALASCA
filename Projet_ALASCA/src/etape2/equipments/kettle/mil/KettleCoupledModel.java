package etape2.equipments.kettle.mil;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.StaticVariableDescriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.CoupledModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.CoordinatorI;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleCoupledModel</code> defines a simple coupled
 * model used to assemble the models defined for the kettle (water heater)
 * in order to execute unit tests on the kettle simulator.
 *
 * <p>Created on : 2026-02-06</p>
 */
public class KettleCoupledModel extends CoupledModel {
	private static final long serialVersionUID = 1L;
	public static final String URI = KettleCoupledModel.class.getSimpleName();

	public KettleCoupledModel(String uri, TimeUnit simulatedTimeUnit, CoordinatorI simulationEngine,
			ModelI[] submodels, Map<Class<? extends EventI>, EventSink[]> imported,
			Map<Class<? extends EventI>, ReexportedEvent> reexported, Map<EventSource, EventSink[]> connections)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine, submodels, imported, reexported, connections);
	}

	public KettleCoupledModel(String uri, TimeUnit simulatedTimeUnit, CoordinatorI simulationEngine,
			ModelI[] submodels, Map<Class<? extends EventI>, EventSink[]> imported,
			Map<Class<? extends EventI>, ReexportedEvent> reexported, Map<EventSource, EventSink[]> connections,
			Map<StaticVariableDescriptor, VariableSink[]> importedVars,
			Map<VariableSource, StaticVariableDescriptor> reexportedVars, Map<VariableSource, VariableSink[]> bindings)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine, submodels, imported, reexported, connections, importedVars,
				reexportedVars, bindings);
	}
}
// -----------------------------------------------------------------------------
