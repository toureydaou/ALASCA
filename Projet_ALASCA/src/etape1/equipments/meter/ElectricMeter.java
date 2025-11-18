package etape1.equipments.meter;

import etape1.equipments.meter.connections.ElectricMeterInboundPort;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import physical_data.Measure;
import physical_data.MeasurementUnit;
import physical_data.SignalData;


// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeter</code> implements a simplified electric meter
 * component.
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
 * invariant	{@code ELECTRIC_METER_INBOUND_PORT_URI != null && !ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * invariant	{@code TENSION != null}
 * invariant	{@code TENSION.getData() > 0.0}
 * invariant	{@code TENSION.getMeasurementUnit().equals(MeasurementUnit.VOLTS)}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@OfferedInterfaces(offered={ElectricMeterCI.class})
public class			ElectricMeter
extends		AbstractComponent
implements	ElectricMeterImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** URI of the electric meter inbound port used in tests.				*/
	public static final String	ELECTRIC_METER_INBOUND_PORT_URI =
															"ELECTRIC-METER";
	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;

	/**	the tension in the electric circuits of this meter.					*/
	public static Measure<Double>	TENSION = new Measure<Double>(
														220.0,
														MeasurementUnit.VOLTS);
	/** inbound port offering the <code>ElectricMeterCI</code> interface.	*/
	protected ElectricMeterInboundPort	emip;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code em != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param em	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(ElectricMeter em)
	{
		assert	em != null : new PreconditionException("em != null");

		boolean ret = true;
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
				ELECTRIC_METER_INBOUND_PORT_URI != null &&
								!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty(),
				ElectricMeter.class,
				"ELECTRIC_METER_INBOUND_PORT_URI != null &&"
							+ "!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				ElectricMeter.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				ElectricMeter.class,
				"Y_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION != null,
				ElectricMeter.class,
				"TENSION != null");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION.getData() > 0.0,
				ElectricMeter.class,
				"TENSION.getData() > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION.getMeasurementUnit().equals(MeasurementUnit.VOLTS),
				ElectricMeter.class,
				"TENSION.getMeasurementUnit().equals(MeasurementUnit.VOLTS)");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code em != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param em	instance to be tested.
	 * @return	true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(ElectricMeter em)
	{
		assert	em != null : new PreconditionException("em != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ELECTRIC_METER_INBOUND_PORT_URI != null && !ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			ElectricMeter() throws Exception
	{
		this(ELECTRIC_METER_INBOUND_PORT_URI);

		assert	ELECTRIC_METER_INBOUND_PORT_URI != null &&
								!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty() :
				new PreconditionException(
						"ELECTRIC_METER_INBOUND_PORT_URI != null && "
						+ "!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()");
	}

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeter(
		String electricMeterInboundPortURI
		) throws Exception
	{
		this(electricMeterInboundPortURI, 1, 0);

		assert	electricMeterInboundPortURI != null &&
									!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");
	}

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @param nbThreads						number of standard threads.
	 * @param nbSchedulableThreads			number of schedulable threads.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeter(
		String electricMeterInboundPortURI,
		int nbThreads,
		int nbSchedulableThreads
		) throws Exception
	{
		super(nbThreads, nbSchedulableThreads);
	
		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");

		this.initialise(electricMeterInboundPortURI);
	}

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection innbound port of the component.
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @param nbThreads						number of standard threads.
	 * @param nbSchedulableThreads			number of schedulable threads.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeter(
		String reflectionInboundPortURI,
		String electricMeterInboundPortURI,
		int nbThreads,
		int nbSchedulableThreads
		) throws Exception
	{
		super(reflectionInboundPortURI, nbThreads, nbSchedulableThreads);

		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");

		this.initialise(electricMeterInboundPortURI);
	}

	/**
	 * initialise an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected void		initialise(String electricMeterInboundPortURI)
	throws Exception
	{
		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");

		this.emip =
				new ElectricMeterInboundPort(electricMeterInboundPortURI, this);
		this.emip.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Electric meter component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		assert	implementationInvariants(this) :
				new ImplementationInvariantException(
						"ElectricMeter.glassBoxInvariants(this)");
		assert	invariants(this) :
				new InvariantException("ElectricMeter.blackBoxInvariants(this)");
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.emip.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipments.meter.ElectricMeterImplementationI#getTension()
	 */
	@Override
	public Measure<Double>		getTension() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter returns its tension.\n");
		}

		return TENSION;
	}

	/**
	 * @see etape1.equipments.meter.ElectricMeterImplementationI#getCurrentConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentConsumption() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter returns its current consumption.\n");
		}

		SignalData<Double> ret =
			new SignalData<>(new Measure<Double>(
									0.0,
									ElectricMeterImplementationI.POWER_UNIT));

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.isSingle() : new PostconditionException("return.isSingle()");
		assert	ret.getMeasure().getData() >= 0.0 :
				new PostconditionException("return.getMeasure().getData() >= 0.0");
		assert	ret.getMeasure().getMeasurementUnit().equals(
									ElectricMeterImplementationI.POWER_UNIT) :
				new PostconditionException(
						"return.getMeasure().getMeasurementUnit().equals("
						+ "ElectricMeterImplementationI.POWER_UNIT)");

		return ret;
	}

	/**
	 * @see etape1.equipments.meter.ElectricMeterImplementationI#getCurrentProduction()
	 */
	@Override
	public SignalData<Double>	getCurrentProduction() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter returns its current production.\n");
		}

		SignalData<Double> ret =
				new SignalData<>(
							new Measure<Double>(
									0.0,
									ElectricMeterImplementationI.POWER_UNIT));

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.isSingle() : new PostconditionException("return.isSingle()");
		assert	ret.getMeasure().getData() >= 0.0 :
				new PostconditionException("return.getMeasure().getData() >= 0.0");
		assert	ret.getMeasure().getMeasurementUnit().equals(
									ElectricMeterImplementationI.POWER_UNIT) :
				new PostconditionException(
						"return.getMeasure().getMeasurementUnit().equals("
						+ "ElectricMeterImplementationI.POWER_UNIT)");

		return ret;
	}
}
// -----------------------------------------------------------------------------
