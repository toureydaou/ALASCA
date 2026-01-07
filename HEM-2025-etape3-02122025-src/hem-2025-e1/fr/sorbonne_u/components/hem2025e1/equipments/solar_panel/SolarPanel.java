package fr.sorbonne_u.components.hem2025e1.equipments.solar_panel;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.hem2025e1.equipments.solar_panel.connections.SolarPanelInboundPort;
import fr.sorbonne_u.components.reflection.interfaces.ReflectionCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.utils.URIGenerator;

// -----------------------------------------------------------------------------
/**
 * The class <code>SolarPanel</code>
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
@OfferedInterfaces(offered = {SolarPanelCI.class})
public class			SolarPanel
extends		AbstractComponent
implements	SolarPanelImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** capacity of the solar panel per square meter in the power unit
	 *  used by the solar panel.											*/
	public static final Measure<Double>	CAPACITY_PER_SQUARE_METER =
													new Measure<Double>(
															250.0,
															POWER_UNIT);
	/** the standard inbound port URI when just one port is used.			*/
	public static final String			STANDARD_INBOUND_PORT_URI =
														"solar-panel-ibp-uri";

	public static final Measure<Double>	FAKE_CURRENT_POWER_PRODUCTION =
													new Measure<Double>(
															250.0,
															POWER_UNIT);

	/** number of square meters in the solar panel.							*/
	protected final int				numberOfSquareMeters;
	/** nominal power production capacity of the solar panel in the power
	 *  unit used by the batteries.											*/
	protected final Measure<Double>	nominalPowerProductionCapacity;
	/** inbound port offering the {@code SolarPanelCI} component interface.	*/
	protected SolarPanelInboundPort inboundPort;

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
	protected static boolean	implementationInvariants(SolarPanel instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.nominalPowerProductionCapacity != null &&
					instance.nominalPowerProductionCapacity.getData() > 0.0 &&
					instance.nominalPowerProductionCapacity.getMeasurementUnit().
														equals(POWER_UNIT),
				SolarPanel.class, instance,
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
				SolarPanel.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				SolarPanel.class,
				"Y_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				CAPACITY_PER_SQUARE_METER != null &&
					CAPACITY_PER_SQUARE_METER.getMeasurementUnit().equals(
																	POWER_UNIT),
				SolarPanel.class,
				"CAPACITY_PER_SQUARE_METER != null && CAPACITY_PER_SQUARE_METER."
				+ "getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				STANDARD_INBOUND_PORT_URI != null &&
										!STANDARD_INBOUND_PORT_URI.isEmpty(),
				SolarPanel.class,
				"STANDARD_INBOUND_PORT_URI != null && "
				+ "!STANDARD_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				FAKE_CURRENT_POWER_PRODUCTION != null &&
					FAKE_CURRENT_POWER_PRODUCTION.getData() > 0.0 &&
					FAKE_CURRENT_POWER_PRODUCTION.getMeasurementUnit().
															equals(POWER_UNIT),
				SolarPanel.class,
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
	protected static boolean	invariants(SolarPanel instance)
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
	protected			SolarPanel(int nbOfSquareMeters) throws Exception
	{
		this(URIGenerator.generateURIwithPrefix(ReflectionCI.class.getSimpleName()),
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
	protected			SolarPanel(
		String reflectionInboundPortURI,
		String inboundPortURI,
		int nbOfSquareMeters
		) throws Exception
	{
		super(reflectionInboundPortURI, 1, 0);

		// Preconditions checking
		assert	inboundPortURI != null && !inboundPortURI.isEmpty() :
				new PreconditionException(
						"inboundPortURI != null && !inboundPortURI.isEmpty()");
		assert	nbOfSquareMeters > 0 :
				new PreconditionException("nbOfSquareMeters > 0");

		this.numberOfSquareMeters = nbOfSquareMeters;
		this.nominalPowerProductionCapacity =
				new Measure<Double>(
						nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),
						CAPACITY_PER_SQUARE_METER.getMeasurementUnit());
		this.inboundPort = new SolarPanelInboundPort(inboundPortURI, this);
		this.inboundPort.publishPort();

		// Postconditions checking
		assert getNominalPowerProductionCapacity().equals(new Measure<Double>(nbOfSquareMeters * CAPACITY_PER_SQUARE_METER.getData(),CAPACITY_PER_SQUARE_METER.getMeasurementUnit())) :
							new PostconditionException("");

		if (VERBOSE) {
			this.tracer.get().setTitle("Solar Panel component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		// Invariant checking
		assert	SolarPanel.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SolarPanel.implementationInvariants(this)");
		assert	SolarPanel.invariants(this) :
				new InvariantException("SolarPanel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

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
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.solar_panel.SolarPanelImplementationI#getNumberOfSquareMeters()
	 */
	@Override
	public int			getNumberOfSquareMeters() throws Exception
	{
		return this.numberOfSquareMeters;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.solar_panel.SolarPanelImplementationI#getNominalPowerProductionCapacity()
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
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.solar_panel.SolarPanelImplementationI#getCurrentPowerProductionLevel()
	 */
	@Override
	public SignalData<Double> getCurrentPowerProductionLevel() throws Exception
	{
		// temporary implementation, would need a physical sensor
		SignalData<Double> ret =
				new SignalData<>(FAKE_CURRENT_POWER_PRODUCTION);
		
		if (VERBOSE) {
			this.logMessage("Solar Panel returns its current power production"
							+ " capacity: " + ret);
		}

		// Postconditions checking
		assert	ret != null && ret.getMeasure().getMeasurementUnit().equals(POWER_UNIT) :
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
