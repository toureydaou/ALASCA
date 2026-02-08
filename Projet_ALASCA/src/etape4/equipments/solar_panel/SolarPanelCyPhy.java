package etape4.equipments.solar_panel;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
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

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.annotations.LocalArchitecture;
import fr.sorbonne_u.components.cyphy.annotations.SIL_Simulation_Architectures;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import etape1.equipments.meter.ElectricMeter;
import etape1.equipments.meter.ElectricMeterImplementationI;
import etape1.equipments.solar_panel.SolarPanelCI;
import etape1.equipments.solar_panel.SolarPanelImplementationI;
import etape1.equipments.solar_panel.connections.SolarPanelInboundPort;
import etape2.equipments.solar_panel.mil.events.SunriseEvent;
import etape2.equipments.solar_panel.mil.events.SunsetEvent;
import etape4.equipments.solar_panel.sil.Local_SIL_SimulationArchitectures;
import etape4.equipments.solar_panel.sil.SolarPanelStateSILModel;
import etape4.equipments.solar_panel.sil.events.PowerProductionLevel;
import etape4.equipments.solar_panel.sil.events.PowerProductionLevel.PowerLevel;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.utils.URIGenerator;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanelCyPhy</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code nominalPowerProductionCapacity != null && nominalPowerProductionCapacity.getData() > 0.0 && nominalPowerProductionCapacity.getMeasurementUnit().equals(POWER_UNIT)}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * invariant	{@code CAPACITY_PER_SQUARE_METER != null && CAPACITY_PER_SQUARE_METER.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code STANDARD_INBOUND_PORT_URI != null && !STANDARD_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code FAKE_CURRENT_POWER_PRODUCTION != null && FAKE_CURRENT_POWER_PRODUCTION.getData() > 0.0 && FAKE_CURRENT_POWER_PRODUCTION.getMeasurementUnit().equals(POWER_UNIT)}
 * </pre>
 * 
 * <p>Created on : 2025-09-26</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@SIL_Simulation_Architectures({
	// fragile: fields in annotations cannot be defined by a class constant due
	// to Java annotations field initialisers limited to static values only
	@LocalArchitecture(
		// must be equal to UNIT_TEST_ARCHITECTURE_URI
		uri = "silUnitTests",
		// must be equal to the URI of the instance of SolarPanelCoupledModel
		rootModelURI = "SolarPanelCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents()
		),
	@LocalArchitecture(
		// must be equal to INTEGRATION_TEST_ARCHITECTURE_URI
		uri = "silIntegrationTests",
		// must be equal to the URI of the instance of SolarPanelCoupledModel
		rootModelURI = "SolarPanelCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents =
			@ModelExternalEvents(
				imported = {PowerProductionLevel.class},
				exported = {SunriseEvent.class, SunsetEvent.class}
				)
		)
	})
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered = {SolarPanelCI.class})
// -----------------------------------------------------------------------------
public class			SolarPanelCyPhy
extends		AbstractCyPhyComponent
implements	SolarPanelImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** capacity of the solar panel per square meter in the power unit
	 *  used by the solar panel.											*/
	public static final Measure<Double>	CAPACITY_PER_SQUARE_METER =
													new Measure<Double>(
															250.0,
															POWER_UNIT);
	/** standard URI of the batteries reflection inbound port.				*/
	public static final String			REFLECTION_INBOUND_PORT_URI =
														"SOLAR-PANEL-RIP-URI";	
	/** the standard inbound port URI when just one port is used.			*/
	public static final String			STANDARD_INBOUND_PORT_URI =
														"solar-panel-ibp-uri";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String			UNIT_TEST_ARCHITECTURE_URI =
														"silUnitTests";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String			INTEGRATION_TEST_ARCHITECTURE_URI =
														"silIntegrationTests";

	public static final Measure<Double>	FAKE_CURRENT_POWER_PRODUCTION =
													new Measure<Double>(
															250.0,
															POWER_UNIT);

	/** number of square meters in the solar panel.							*/
	protected int						numberOfSquareMeters;
	/** nominal power production capacity of the solar panel in the power
	 *  unit used by the batteries.											*/
	protected Measure<Double>			nominalPowerProductionCapacity;
	/** inbound port offering the {@code SolarPanelCI} component interface.	*/
	protected SolarPanelInboundPort 	inboundPort;

	// Execution/Simulation

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** one thread for the method execute, which starts the local SIL
	 *  simulator, and one to answer the calls to the component services.	*/
	protected static int				NUMBER_OF_STANDARD_THREADS = 2;
	/** no need for statically defined schedulable threads.					*/
	protected static int				NUMBER_OF_SCHEDULABLE_THREADS = 0;

	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin		asp;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture or the empty string if the component does
	 *  not execute as a simulation.										*/
	protected final String				localArchitectureURI;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected final double				accelerationFactor;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(SolarPanelCyPhy instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nominalPowerProductionCapacity != null &&
					instance.nominalPowerProductionCapacity.getData() > 0.0 &&
					instance.nominalPowerProductionCapacity.getMeasurementUnit().
														equals(POWER_UNIT),
				SolarPanelCyPhy.class, instance,
				"nominalPowerProductionCapacity != null && "
				+ "nominalPowerProductionCapacity.getData() > 0.0 && "
				+ "nominalPowerProductionCapacity.getMeasurementUnit().equals("
				+ "POWER_UNIT)}");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				SolarPanelCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				SolarPanelCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				CAPACITY_PER_SQUARE_METER != null &&
					CAPACITY_PER_SQUARE_METER.getMeasurementUnit().equals(
																	POWER_UNIT),
				SolarPanelCyPhy.class,
				"CAPACITY_PER_SQUARE_METER != null && CAPACITY_PER_SQUARE_METER."
				+ "getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				STANDARD_INBOUND_PORT_URI != null &&
										!STANDARD_INBOUND_PORT_URI.isEmpty(),
				SolarPanelCyPhy.class,
				"STANDARD_INBOUND_PORT_URI != null && "
				+ "!STANDARD_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_CURRENT_POWER_PRODUCTION != null &&
					FAKE_CURRENT_POWER_PRODUCTION.getData() > 0.0 &&
					FAKE_CURRENT_POWER_PRODUCTION.getMeasurementUnit().
															equals(POWER_UNIT),
				SolarPanelCyPhy.class,
				"FAKE_CURRENT_POWER_PRODUCTION != null && "
				+ "FAKE_CURRENT_POWER_PRODUCTION.getData() > 0.0 && "
				+ "FAKE_CURRENT_POWER_PRODUCTION.getMeasurementUnit()."
				+ "equals(POWER_UNIT)");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(SolarPanelCyPhy instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= SolarPanelImplementationI.staticInvariants();
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create a solar panel component with the given number of square meters of
	 * panels, a generated URI for its reflection inbound port and the standard
	 * URI for the in bound port offering the {@code SolarPanelCI} component
	 * interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code nbOfSquareMeters > 0}
	 * post	{@code getNominalPowerProductionCapacity().equals(new Measure<Double>(nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param nbOfSquareMeters			number of square meters of solar panel to be created.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			SolarPanelCyPhy(int nbOfSquareMeters) throws Exception
	{
		this(URIGenerator.generateURIwithPrefix(
										ReflectionCI.class.getSimpleName()),
			 STANDARD_INBOUND_PORT_URI,
			 nbOfSquareMeters);
	}

	/**
	 * create a solar panel component with the given number of square meters of
	 * panels, the given URI for its reflection inbound port and the given URI
	 * for the in bound port offering the {@code SolarPanelCI} component
	 * interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code nbOfSquareMeters > 0}
	 * post	{@code getNominalPowerProductionCapacity().equals(new Measure<Double>(nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @param inboundPortURI			URI of the inbound port offering the {@code BatteriesCI} component interface.
	 * @param nbOfSquareMeters			number of square meters of solar panel to be created.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			SolarPanelCyPhy(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int nbOfSquareMeters
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	nbOfSquareMeters > 0 :
				new PreconditionException("nbOfSquareMeters > 0");

		this.initialise(inboundPortURI, nbOfSquareMeters);
		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		// Postconditions checking
		assert	getNominalPowerProductionCapacity().equals(
					new Measure<Double>(nbOfSquareMeters *
											CAPACITY_PER_SQUARE_METER.getData(),
										CAPACITY_PER_SQUARE_METER.
														getMeasurementUnit())) :
				new PostconditionException(
						"getNominalPowerProductionCapacity().equals("
						+ "new Measure<Double>(nbOfSquareMeters * "
						+ "CAPACITY_PER_SQUARE_METER.getData(), "
						+ "CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))");

		// Invariant checking
		assert	SolarPanelCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SolarPanel.implementationInvariants(this)");
		assert	SolarPanelCyPhy.invariants(this) :
				new InvariantException("SolarPanel.invariants(this)");
	}

	// Tests without simulation execution

	/**
	 * create a solar panel component with the given number of square meters of
	 * panels, a generated URI for its reflection inbound port and the standard
	 * URI for the in bound port offering the {@code SolarPanelCI} component
	 * interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code nbOfSquareMeters > 0}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
	 * post	{@code getNominalPowerProductionCapacity().equals(new Measure<Double>(nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param nbOfSquareMeters			number of square meters of solar panel to be created.
	 * @param executionMode				execution mode for the next run.
	 * @param clockURI					URI of the clock used to synchronise the test scenario.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			SolarPanelCyPhy(
		int nbOfSquareMeters,
		ExecutionMode executionMode,
		String clockURI
		
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI,
			 STANDARD_INBOUND_PORT_URI,
			 nbOfSquareMeters,
			 executionMode,
			 clockURI);
	}

	/**
	 * create a solar panel component with the given number of square meters of
	 * panels, the given URI for its reflection inbound port and the given URI
	 * for the in bound port offering the {@code SolarPanelCI} component
	 * interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code nbOfSquareMeters > 0}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !.isEmpty()}
	 * post	{@code getExecutionMode().isTestWithoutSimulation()}
	 * post	{@code getNominalPowerProductionCapacity().equals(new Measure<Double>(nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @param inboundPortURI			URI of the inbound port offering the {@code BatteriesCI} component interface.
	 * @param nbOfSquareMeters			number of square meters of solar panel to be created.
	 * @param executionMode				execution mode for the next run.
	 * @param clockURI					URI of the clock used to synchronise the test scenario.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			SolarPanelCyPhy(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int nbOfSquareMeters,
		ExecutionMode executionMode,
		String clockURI
		
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  clockURI);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	nbOfSquareMeters > 0 :
				new PreconditionException("nbOfSquareMeters > 0");

		this.initialise(inboundPortURI, nbOfSquareMeters);
		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		// Postconditions checking
		assert	getNominalPowerProductionCapacity().equals(
					new Measure<Double>(nbOfSquareMeters *
											CAPACITY_PER_SQUARE_METER.getData(),
										CAPACITY_PER_SQUARE_METER.
														getMeasurementUnit())) :
				new PostconditionException(
						"getNominalPowerProductionCapacity().equals("
						+ "new Measure<Double>(nbOfSquareMeters * "
						+ "CAPACITY_PER_SQUARE_METER.getData(), "
						+ "CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))");

		// Invariant checking
		assert	SolarPanelCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SolarPanel.implementationInvariants(this)");
		assert	SolarPanelCyPhy.invariants(this) :
				new InvariantException("SolarPanel.invariants(this)");
	}

	// Tests with simulation

	/**
	 * create a solar panel component with the given number of square meters of
	 * panels, the given URI for its reflection inbound port and the given URI
	 * for the in bound port offering the {@code SolarPanelCI} component
	 * interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null}
	 * pre	{@code inboundPortURI != null && !inboundPortURI.isEmpty()}
	 * pre	{@code nbOfSquareMeters > 0}
	 * pre	{@code executionMode != null && executionMode.isSimulationTest()}
	 * pre	{@code testScenario != null}
	 * pre	{@code localArchitectureURI != null && !localArchitectureURI.isEmpty()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getNominalPowerProductionCapacity().equals(new Measure<Double>(nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @param inboundPortURI			URI of the inbound port offering the {@code BatteriesCI} component interface.
	 * @param nbOfSquareMeters			number of square meters of solar panel to be created.
	 * @param executionMode				execution mode for the next run.
	 * @param testScenario				test scenario to be executed with this component.
	 * @param localArchitectureURI		URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param accelerationFactor		acceleration factor for the simulation.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			SolarPanelCyPhy(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int nbOfSquareMeters,
		ExecutionMode executionMode,
		TestScenario testScenario,
		String localArchitectureURI,
		double accelerationFactor
		
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null,
				testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario,
			  ((Supplier<Set<String>>)() ->
		  		{ HashSet<String> hs = new HashSet<>();
		  		  hs.add(UNIT_TEST_ARCHITECTURE_URI);
		  		  hs.add(INTEGRATION_TEST_ARCHITECTURE_URI);
		  		  return hs;
		  		}).get(),
		  accelerationFactor);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	nbOfSquareMeters > 0 :
				new PreconditionException("nbOfSquareMeters > 0");

		this.initialise(inboundPortURI, nbOfSquareMeters);
		this.localArchitectureURI = localArchitectureURI;
		this.accelerationFactor = accelerationFactor;

		// Postconditions checking
		assert	getNominalPowerProductionCapacity().equals(
					new Measure<Double>(nbOfSquareMeters *
											CAPACITY_PER_SQUARE_METER.getData(),
										CAPACITY_PER_SQUARE_METER.
														getMeasurementUnit())) :
				new PostconditionException(
						"getNominalPowerProductionCapacity().equals("
						+ "new Measure<Double>(nbOfSquareMeters * "
						+ "CAPACITY_PER_SQUARE_METER.getData(), "
						+ "CAPACITY_PER_SQUARE_METER.getMeasurementUnit()))");

		// Invariant checking
		assert	SolarPanelCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SolarPanel.implementationInvariants(this)");
		assert	SolarPanelCyPhy.invariants(this) :
				new InvariantException("SolarPanel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Initialisation methods
	// -------------------------------------------------------------------------

	/**
	 * initialise the solar panel.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param inboundPortURI	URI of the solar panel inbound prot.
	 * @param nbOfSquareMeters	number of square meters for the solar panel.
	 * @throws Exception		<i>to do</i>.
	 */
	protected void		initialise(
		String inboundPortURI,
		int nbOfSquareMeters
		) throws Exception
	{
		this.numberOfSquareMeters = nbOfSquareMeters;
		this.nominalPowerProductionCapacity =
				new Measure<Double>(
						nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),
						CAPACITY_PER_SQUARE_METER.getMeasurementUnit());
		this.inboundPort = new SolarPanelInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Solar Panel component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent#createLocalSimulationArchitecture(java.lang.String, java.lang.String, java.util.concurrent.TimeUnit, double)
	 */
	@Override
	protected RTArchitecture	createLocalSimulationArchitecture(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		// Preconditions checking
		assert	architectureURI != null && !architectureURI.isEmpty() :
				new PreconditionException(
						"architectureURI != null && !architectureURI.isEmpty()");
		assert	rootModelURI != null && !rootModelURI.isEmpty() :
				new PreconditionException(
						"rootModelURI != null && !rootModelURI.isEmpty()");
		assert	simulatedTimeUnit != null :
				new PreconditionException("simulatedTimeUnit != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		RTArchitecture ret = null;
		if (architectureURI.equals(UNIT_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createSolarPanelSIL_Architecture4UnitTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createSolarPanelSIL_Architecture4IntegrationTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else {
			throw new BCMException("Unknown local simulation architecture "
								   + "URI: " + architectureURI);
		}
		
		return ret;
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		// Invariant checking
		assert	SolarPanelCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SolarPanelCyPhy.implementationInvariants(this)");
		assert	SolarPanelCyPhy.invariants(this) :
				new InvariantException("SolarPanelCyPhy.invariants(this)");

		// create the simulation plug-in given the current type of simulation
		// and its local architecture i.e., for the current execution
		try {
			switch (this.getExecutionMode()) {
			case STANDARD:
				break;
			case UNIT_TEST:
			case INTEGRATION_TEST:
				break;
			case UNIT_TEST_WITH_SIL_SIMULATION:
				RTArchitecture architecture =
					(RTArchitecture) this.localSimulationArchitectures.
												get(this.localArchitectureURI);
				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;
					/**
					 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
					 */
					@Override
					public PowerLevel	getModelStateValue(
						String modelURI,
						String name
						) throws Exception
					{
						assert	modelURI.equals(SolarPanelStateSILModel.URI);
						assert	name.equals(
									SolarPanelStateSILModel.POWER_LEVEL_NAME);

						return ((SolarPanelStateSILModel)
									this.atomicSimulators.get(modelURI).
														getSimulatedModel()).
										getModelStateValue(modelURI, name);
					}
				};
				((RTAtomicSimulatorPlugin)this.asp).
								setPluginURI(architecture.getRootModelURI());
				((RTAtomicSimulatorPlugin)this.asp).
										setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				// the simulator inside the plug-in is created
				this.asp.createSimulator();
				// to prepare for the run, set the run parameters
				this.asp.setSimulationRunParameters(
						(TestScenarioWithSimulation) this.testScenario,
						new HashMap<>());
				break;
			case INTEGRATION_TEST_WITH_SIL_SIMULATION:
				architecture =
					(RTArchitecture) this.localSimulationArchitectures.
											get(this.localArchitectureURI);
				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;
					/**
					 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
					 */
					@Override
					public PowerLevel	getModelStateValue(
						String modelURI,
						String name
						) throws Exception
					{
						assert	modelURI.equals(SolarPanelStateSILModel.URI);
						assert	name.equals(
									SolarPanelStateSILModel.POWER_LEVEL_NAME);

						return ((SolarPanelStateSILModel)
									this.atomicSimulators.get(modelURI).
													getSimulatedModel()).
										getModelStateValue(modelURI, name);
					}
				};
				((RTAtomicSimulatorPlugin)this.asp).
							setPluginURI(architecture.getRootModelURI());
				((RTAtomicSimulatorPlugin)this.asp).
									setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				break;
			case UNIT_TEST_WITH_HIL_SIMULATION:
			case INTEGRATION_TEST_WITH_HIL_SIMULATION:
				throw new BCMException("HIL simulation not implemented yet!");
			default:
			}		
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}		

		// Invariant checking
		assert	SolarPanelCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SolarPanelCyPhy.implementationInvariants(this)");
		assert	SolarPanelCyPhy.invariants(this) :
				new InvariantException("SolarPanelCyPhy.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.traceMessage("SolarPanelCyPhy executes.\n");

		// Invariant checking
		assert	SolarPanelCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SolarPanelCyPhy.implementationInvariants(this)");
		assert	SolarPanelCyPhy.invariants(this) :
				new InvariantException("SolarPanelCyPhy.invariants(this)");

		switch (this.getExecutionMode()) {
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI,
								 this.clockURI);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
			// First, the component must synchronise with other components
			// to start the execution of the test scenario; we use a
			// time-triggered synchronisation scheme with the accelerated clock
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.asp.initialiseSimulation(
						this.getClock4Simulation().getSimulatedStartTime(),
						this.getClock4Simulation().getSimulatedDuration());
			// schedule the start of the SIL (real time) simulation
			this.asp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(
							this.getClock4Simulation().getStartEpochNanos()),
					this.getClock4Simulation().getSimulatedStartTime().
														getSimulatedTime(),
					this.getClock4Simulation().getSimulatedDuration().
														getSimulatedDuration());
			// wait until the simulation ends
			this.getClock4Simulation().waitUntilEnd();
			// give some time for the end of simulation catering tasks
			Thread.sleep(200L);
			// get and print the simulation report
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
		default:
		}		
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.inboundPort.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}		
		super.shutdown();
	}	

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelImplementationI#getNumberOfSquareMeters()
	 */
	@Override
	public int			getNumberOfSquareMeters() throws Exception
	{
		return this.numberOfSquareMeters;
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelImplementationI#getNominalPowerProductionCapacity()
	 */
	@Override
	public Measure<Double>	getNominalPowerProductionCapacity() throws Exception
	{
		Measure<Double> ret = this.nominalPowerProductionCapacity;

		if (VERBOSE) {
			this.logMessage("Solar Panel returns its nominal power production"
							+ " capacity: " + ret);
		}
		// Postconditions checking
		assert	ret != null && ret.getData() > 0.0 &&
								ret.getMeasurementUnit().equals(POWER_UNIT) :
				new PostconditionException(
						"return != null && return.getData() > 0.0 && "
						+ "return.getMeasurementUnit().equals(POWER_UNIT)");

		return ret;
	}

	/**
	 * @see etape1.equipments.solar_panel.SolarPanelImplementationI#getCurrentPowerProductionLevel()
	 */
	@Override
	public SignalData<Double> getCurrentPowerProductionLevel() throws Exception
	{
		SignalData<Double> ret;
		if (this.getExecutionMode().equals(ExecutionMode.STANDARD)) {
			// temporary implementation, would need a physical sensor
			ret = new SignalData<>(
					new TimedMeasure<Double>(
							FAKE_CURRENT_POWER_PRODUCTION.getData(),
							FAKE_CURRENT_POWER_PRODUCTION.getMeasurementUnit()));
		} else if (this.getExecutionMode().isTestWithoutSimulation()) {
			// temporary implementation, would need a physical sensor
			ret = new SignalData<>(
					this.getClock(),
					new TimedMeasure<Double>(
							FAKE_CURRENT_POWER_PRODUCTION.getData(),
							FAKE_CURRENT_POWER_PRODUCTION.getMeasurementUnit(),
							this.getClock()));
		} else {
			assert	this.getExecutionMode().isSimulationTest();
			// power level in the electric meter power unit
			PowerLevel p =
					(PowerLevel) this.asp.getModelStateValue(
									SolarPanelStateSILModel.URI,
									SolarPanelStateSILModel.POWER_LEVEL_NAME);
			double levelInWatts;
			if (ElectricMeterImplementationI.POWER_UNIT.equals(MeasurementUnit.AMPERES)) {
				levelInWatts =
						p.getPowerLevel() * ElectricMeter.TENSION.getData();
			} else {
				levelInWatts = p.getPowerLevel();
			}
			AcceleratedAndSimulationClock ac = this.getClock4Simulation();
			ret = new SignalData<>(
					ac,
					new TimedMeasure<Double>(
							levelInWatts,
							POWER_UNIT,
							ac,
							ac.instantOfSimulatedTime(p.getTimestamp())));
		}
		
		if (VERBOSE) {
			this.logMessage("Solar Panel returns its current power production"
							+ " capacity: " + ret);
		}

		// Postconditions checking
		assert	ret != null &&
					ret.getMeasure().getMeasurementUnit().equals(POWER_UNIT) :
				new PostconditionException(
						"return != null && return.getMeasure()."
						+ "getMeasurementUnit().equals(POWER_UNIT)");
		assert	ret.getMeasure().getData() >= 0.0 &&
					ret.getMeasure().getData() <=
								getNominalPowerProductionCapacity().getData() :
				new PostconditionException(
						"return.getMeasure().getData() >= 0.0 && return."
						+ "getMeasure().getData() <= "
						+ "getNominalPowerProductionCapacity().getData()");

		return ret;
	}
}
// -----------------------------------------------------------------------------
