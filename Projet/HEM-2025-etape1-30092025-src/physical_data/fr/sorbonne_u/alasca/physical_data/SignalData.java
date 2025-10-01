package fr.sorbonne_u.alasca.physical_data;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
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

import java.io.Serializable;
import java.time.Instant;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;

// -----------------------------------------------------------------------------
/**
 * The class <code>SignalData</code> implements a simple signal data, combining
 * a measure and its time stamping information inherited from
 * {@code TimedEntity}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A signal is the measure of a physical phenomena over time. Given the discrete
 * nature of computers, signals are known only at discrete points in time using
 * measures. An instance of {@code SignalData} represents one of these timed
 * measures, composed of a value of a generic type {@code T} and a measurement
 * unit of type {@code MeasurementUnit} (value which can be
 * {@code MeasurementUnit.RAW} when there is no actual unit).
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code measure != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-11-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			SignalData<T extends Serializable>
extends		AbstractSignalData
implements	Serializable
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	/** measured data.														*/
	protected final Measure<T>		measure;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sd	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(SignalData<?> sd)
	{
		assert	sd != null : new PreconditionException("sd != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				sd.measure != null,
				SignalData.class,
				sd,
				"sd.measure != null");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sd	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(SignalData<?> sd)
	{
		assert	sd != null : new PreconditionException("sd != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a sensor data from a measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code measure != null}
	 * post	{@code getTimestamp().equals(Instant.ofEpochMilli(System.currentTimeMillis())}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * post	{@code getMeasure().equals(measure)}
	 * </pre>
	 *
	 * @param measure	the measured data.
	 */
	public				SignalData(Measure<T> measure)
	{
		super();

		assert	measure != null : new PreconditionException("measure != null");

		this.measure = measure;

		assert	getMeasure().equals(measure) :
				new PostconditionException("getMeasure().equals(measure)");

		assert	SignalData.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SensorData.implementationInvariants(this)");
		assert	SignalData.invariants(this) :
				new InvariantException("SensorData.invariants(this)");
	}

	/**
	 * create a sensor data from a measure and a given {@code Instant} as
	 * time stamp.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code measure != null}
	 * pre	{@code timestamp != null}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * post	{@code getMeasure().equals(measure)}
	 * </pre>
	 *
	 * @param measure	the measured data.
	 * @param timestamp	the instant at which the sensor data is created.
	 */
	public				SignalData(Measure<T> measure, Instant timestamp)
	{
		super(timestamp);

		assert	measure != null : new PreconditionException("measure != null");

		this.measure = measure;

		assert	getMeasure().equals(measure) :
				new PostconditionException("getMeasure().equals(measure)");

		assert	implementationInvariants(this) :
				new ImplementationInvariantException(
						"SensorData.implementationInvariants(this)");
		assert	invariants(this) :
				new InvariantException("SensorData.invariants(this)");
	}

	/**
	 * create a sensor data from a compound measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code measure != null}
	 * post	{@code getTimestamp().equals(ac.currentInstant())}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * post	{@code getMeasure().equals(measure)}
	 * </pre>
	 *
	 * @param ac		accelerated clock used as time reference.
	 * @param measure	the measured data.
	 */
	public				SignalData(
		AcceleratedClock ac,
		Measure<T> measure
		)
	{
		this(ac, measure, ac.currentInstant());
	}

	/**
	 * create a sensor data from a compound measure.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code measure != null}
	 * pre	{@code timestamp != null}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * post	{@code getMeasure().equals(measure)}
	 * </pre>
	 *
	 * @param ac		accelerated clock used as time reference.
	 * @param measure	the measured data.
	 * @param timestamp	the instant at which the sensor data is created.
	 */
	public				SignalData(
		AcceleratedClock ac,
		Measure<T> measure,
		Instant timestamp
		)
	{
		super(ac, timestamp);

		assert	ac != null : new PreconditionException("ac != null");
		assert	measure != null : new PreconditionException("measure != null");

		this.measure = measure;

		assert	getMeasure().equals(measure) :
				new PostconditionException("getMeasure().equals(measure)");

		assert	SignalData.implementationInvariants(this) :
				new ImplementationInvariantException(
						"SensorData.implementationInvariants(this)");
		assert	SignalData.invariants(this) :
				new InvariantException("SensorData.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.AbstractSignalData#isSingle()
	 */
	@Override
	public boolean		isSingle()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.AbstractSignalData#isComposed()
	 */
	@Override
	public boolean		isComposed()
	{
		return false;
	}

	/** @return the data.													*/
	public Measure<T>	getMeasure()
	{
		return this.measure;
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.AbstractSignalData#toStringBuffer(java.lang.StringBuffer)
	 */
	@Override
	public void			toStringBuffer(StringBuffer sb)
	{
		assert	sb != null : new PreconditionException("sb != null");

		sb.append(this.getClass().getSimpleName());
		sb.append('[');
		this.contentAsString(sb);
		sb.append(']');
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.TimedEntity#contentAsString(java.lang.StringBuffer)
	 */
	@Override
	protected void		contentAsString(StringBuffer sb)
	{
		sb.append(this.measure.toString());
		sb.append(", ");
		super.contentAsString(sb);
	}
}
// -----------------------------------------------------------------------------
