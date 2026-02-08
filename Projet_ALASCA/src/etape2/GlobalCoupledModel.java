package etape2;


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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import etape2.equipments.solar_panel.mil.SolarPanelPowerModel;
import etape2.equipments.solar_panel.mil.SunIntensityModelI;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
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
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * The class <code>GlobalCoupledModel</code> defines a simple coupled model for
 * the household management simulator.
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
 * invariant	{@code URI != null && !URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2023-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			GlobalCoupledModel
extends		CoupledModel
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = true;

	/** URI for an instance model; works as long as only one instance is
	 *  created.															*/
	public static final String	URI = GlobalCoupledModel.class.getSimpleName();

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * creating the coupled model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine != null && !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine instanceof CoordinationEngine}
	 * pre	{@code submodels != null && submodels.length > 1}
	 * pre	{@code Stream.of(submodels).allMatch(s -> s != null)}
	 * pre	{@code imported != null}
	 * pre	{@code reexported != null}
	 * pre	{@code connections != null}
	 * pre	{@code importedVars != null}
	 * pre	{@code reexportedVars != null}
	 * pre	{@code bindings != null}
	 * post	{@code !isDebugModeOn()}
	 * post	{@code getURI() != null && !getURI().isEmpty()}
	 * post	{@code uri == null || getURI().equals(uri)}
	 * post	{@code getSimulatedTimeUnit().equals(simulatedTimeUnit)}
	 * post	{@code getSimulationEngine().equals(simulationEngine)}
	 * </pre>
	 *
	 * @param uri				URI of the coupled model to be created.
	 * @param simulatedTimeUnit	time unit used in the simulation by the model.
	 * @param simulationEngine	simulation engine enacting the model.
	 * @param submodels			array of submodels of the new coupled model.
	 * @param imported			map from imported event types to submodels consuming them.
	 * @param reexported		map from event types exported by submodels that are reexported by this coupled model.
	 * @param connections		map connecting event sources to arrays of event sinks among submodels.
	 * @param importedVars		variables imported by the coupled model that are consumed by submodels.
	 * @param reexportedVars	variables exported by submodels that are reexported by the coupled model.
	 * @param bindings			bindings between exported and imported variables among submodels.
	 * @throws Exception		<i>to do</i>.
	 */
	public				GlobalCoupledModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		CoordinatorI simulationEngine,
		ModelI[] submodels,
		Map<Class<? extends EventI>, EventSink[]> imported,
		Map<Class<? extends EventI>, ReexportedEvent> reexported,
		Map<EventSource, EventSink[]> connections,
		Map<StaticVariableDescriptor, VariableSink[]> importedVars,
		Map<VariableSource, StaticVariableDescriptor> reexportedVars,
		Map<VariableSource, VariableSink[]> bindings
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine, submodels,
			  imported, reexported, connections,
			  importedVars, reexportedVars, bindings);

		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * when more than one model has an internal transition to perform at some
	 * simulated time, select the one that will execute.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In the solar panel simulator, the power output by the solar panel is
	 * computed from the sun intensity, hence to get the most precise value,
	 * the sun intensity model must execute first and then the solar panel
	 * power model.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no more preconditions.
	 * post	{@code true}	// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.devs_simulation.models.CoupledModel#select(java.lang.String[])
	 */
	@Override
	public String		select(String[] candidates)
	{
		// retrieve the URI of the sun intensity and solar panel power models
		String sunIntensityModelURI = null;
		String solarPanelPowerModelURI = null;
		for (int i = 0 ; (sunIntensityModelURI == null ||
								solarPanelPowerModelURI == null) &&
						 					i <= this.submodels.length ; i++) {
			if (this.submodels[i] instanceof SunIntensityModelI) {
				sunIntensityModelURI = this.submodels[i].getURI();
			} else if (this.submodels[i] instanceof SolarPanelPowerModel) {
				solarPanelPowerModelURI = this.submodels[i].getURI();
			}
		}
		assert	sunIntensityModelURI != null && solarPanelPowerModelURI != null :
				new NeoSim4JavaException(
						"sunIntensityModelURI != null &&"
						+ "solarPanelPowerModelURI != null");

		// check if the two types of models are present in the candidates
		boolean intensityModelPresent = false;
		boolean powerModelPresent = false;
		for (int i = 0 ; (!intensityModelPresent || !powerModelPresent)
							 && i < candidates.length ; i++) {
			if (sunIntensityModelURI.equals(candidates[i])) {
				intensityModelPresent = true;
			} else {
				if (solarPanelPowerModelURI.equals(candidates[i])) {
					powerModelPresent = true;
				}
			}
		}

		String elected = null;
		if (intensityModelPresent && powerModelPresent) {
			// if they are both present, the sun intensity model must go first
			elected = sunIntensityModelURI;
		} else {
			// otherwise, the default selection is applied
			elected = super.select(candidates);
		}

		// tracing
		if (GlobalCoupledModel.DEBUG) {
			this.logMessage(
					"select "
					+ (!intensityModelPresent && !powerModelPresent)
					+ " returns " + elected);
		}

		return elected;
	}
	
	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>GlobalReport</code> implements the simulation report for
	 * the global HEM project simulator.
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
	 * <p>Created on : 2025-10-14</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		GlobalReport
	implements	SimulationReportI, GlobalReportI
	{
		private static final long serialVersionUID = 1L;
		ArrayList<GlobalReportI>	subreports;
		protected String		modelURI;

		public				GlobalReport(String modelURI)
		{
			super();
			this.modelURI = modelURI;
			this.subreports = new ArrayList<>();
		}

		@Override
		public String		getModelURI()
		{
			return this.modelURI;
		}

		public void			addSubReport(GlobalReportI r)
		{
			this.subreports.add(r);
		}

		@Override
		public String		printout(String indent)
		{
			StringBuffer ret = new StringBuffer(indent);
			ret.append("--------------------------\n");
			ret.append(indent);
			ret.append(this.modelURI);
			ret.append(" report\n");
			for (int i = 0; i < this.subreports.size() ; i++) {
				ret.append(this.subreports.get(i).printout(indent + "  "));
			}
			ret.append(indent);
			ret.append("--------------------------\n");
			return ret.toString();
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.CoupledModel#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport()
	{
		GlobalReport ret = new GlobalReport(URI);
		for (int i = 0 ; i < this.submodels.length ; i++) {
			GlobalReportI r = (GlobalReportI)this.submodels[i].getFinalReport();
			if (r != null) {
				ret.addSubReport(r);
			}
		}		
		return ret;
	}
}
// -----------------------------------------------------------------------------
