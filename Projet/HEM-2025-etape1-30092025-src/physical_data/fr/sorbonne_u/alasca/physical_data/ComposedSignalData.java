package fr.sorbonne_u.alasca.physical_data;

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

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ComposedSignalData</code> implements a composed signal data,
 * with its own time stamping information, and combining several single or
 * composed signal data having their own time stamping information.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code data != null && data.length > 1 && Stream.of(data).allMatch(sd -> sd != null)}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-09-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			ComposedSignalData
extends		AbstractSignalData
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;


	/**	the largest difference among the accelerated clocks in Unix,
	 *  in nanoseconds.													 	*/
	public static long				DEVIATION_TOLERANCE_AMONG_TIME_REFERENCES =
											TimeUnit.MILLISECONDS.toNanos(10);
	/** array of single or composed signal data.							*/
	protected final AbstractSignalData[]	data;

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
	protected static boolean	implementationInvariants(
		ComposedSignalData instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		// The implementation invariant is ensured by the fact that the data
		// field is final and the check is made as precondition in every
		// constructor
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
	protected static boolean	invariants(ComposedSignalData instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a composed signal data with the given array of signal data as
	 * content, the current time as time stamp, the current host as time stamper
	 * and its hardware clock giving the time reference.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * Note that the array {@code data} is not copied but kept as is in the
	 * resulting composed signal data, hence introducing sharing between the
	 * calling context and the newly created composed signal data.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code data != null && data.length > 1 && Stream.of(data).allMatch(sd -> sd != null)}
	 * post	{@code getTimestamp().equals(Instant.ofEpochMilli(System.currentTimeMillis())}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * </pre>
	 *
	 * @param data	array of signal data that will be the content of the new composed signal data.
	 */
	public				ComposedSignalData(AbstractSignalData[] data)
	{
		super();

		// Preconditions checking
		assert	data != null && data.length > 1 &&
									Stream.of(data).allMatch(sd -> sd != null) :
				new PreconditionException(
						"data != null && data.length > 1 && "
						+ "Stream.of(data).allMatch(sd -> sd != null)");

		this.data = data;

		// Invariant checking
		assert	ComposedSignalData.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ComposedSignalData.implementationInvariants(this)");
		assert	ComposedSignalData.invariants(this) :
				new InvariantException("ComposedSignalData.invariants(this)");
	}

	/**
	 * create a composed signal data with the given array of signal data as
	 * content, the given time stamp and the current host as time stamper and
	 * its hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code data != null && data.length > 1 && Stream.of(data).allMatch(sd -> sd != null)}
	 * pre	{@code timestamp != null}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * </pre>
	 *
	 * @param data			array of signal data that will be the content of the new composed signal data.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 */
	public				ComposedSignalData(
		AbstractSignalData[] data,
		Instant timestamp
		)
	{
		super(timestamp);

		// Preconditions checking
		assert	data != null && data.length > 1 &&
									Stream.of(data).allMatch(sd -> sd != null) :
				new PreconditionException(
						"data != null && data.length > 1 && "
						+ "Stream.of(data).allMatch(sd -> sd != null)");

		this.data = data;

		// Invariant checking
		assert	ComposedSignalData.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ComposedSignalData.implementationInvariants(this)");
		assert	ComposedSignalData.invariants(this) :
				new InvariantException("ComposedSignalData.invariants(this)");
	}

	/**
	 * create a composed signal data with the given array of signal data as
	 * content, the given time stamp, time stamper identity and the current host
	 * hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code data != null && data.length > 1 && Stream.of(data).allMatch(sd -> sd != null)}
	 * pre	{@code timestamp != null}
	 * pre	{@code timestamper != null && !timestamper.isEmpty()}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(getTimestamper())}
	 * </pre>
	 *
	 * @param data			array of signal data that will be the content of the new composed signal data.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 * @param timestamper	identity of the time stamping host <i>e.g.</i>, its IP address.
	 */
	public				ComposedSignalData(
		AbstractSignalData[] data,
		Instant timestamp,
		String timestamper
		)
	{
		super(timestamp, timestamper);

		// Preconditions checking
		assert	data != null && data.length > 1 &&
									Stream.of(data).allMatch(sd -> sd != null) :
				new PreconditionException(
						"data != null && data.length > 1 && "
						+ "Stream.of(data).allMatch(sd -> sd != null)");

		this.data = data;

		// Invariant checking
		assert	ComposedSignalData.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ComposedSignalData.implementationInvariants(this)");
		assert	ComposedSignalData.invariants(this) :
				new InvariantException("ComposedSignalData.invariants(this)");
	}

	/**
	 * create a composed signal data with the given array of signal data as
	 * content, the current time as time stamp, the current host as time stamper
	 * and the given accelerated clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code data != null && data.length > 1 && Stream.of(data).allMatch(sd -> sd != null)}
	 * pre	{@code ac != null}
	 * post	{@code getTimestamp().equals(ac.currentInstant())}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * </pre>
	 *
	 * @param data	array of signal data that will be the content of the new composed signal data.
	 * @param ac	an accelerated clock giving the time reference.
	 */
	public				ComposedSignalData(
		AbstractSignalData[] data,
		AcceleratedClock ac
		)
	{
		super(ac);

		// Preconditions checking
		assert	data != null && data.length > 1 &&
									Stream.of(data).allMatch(sd -> sd != null) :
				new PreconditionException(
						"data != null && data.length > 1 && "
						+ "Stream.of(data).allMatch(sd -> sd != null)");

		this.data = data;

		// Invariant checking
		assert	ComposedSignalData.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ComposedSignalData.implementationInvariants(this)");
		assert	ComposedSignalData.invariants(this) :
				new InvariantException("ComposedSignalData.invariants(this)");
	}

	/**
	 * create a composed signal data with the given array of signal data as
	 * content, the given time stamp and the current host as time stamper.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code data != null && data.length > 1 && Stream.of(data).allMatch(sd -> sd != null)}
	 * pre	{@code ac != null}
	 * pre	{@code timestamp != null}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * </pre>
	 *
	 * @param data			array of signal data that will be the content of the new composed signal data.
	 * @param ac			an accelerated clock giving the time reference.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 */
	public				ComposedSignalData(
		AbstractSignalData[] data,
		AcceleratedClock ac,
		Instant timestamp
		)
	{
		super(ac, timestamp);

		// Preconditions checking
		assert	data != null && data.length > 1 &&
									Stream.of(data).allMatch(sd -> sd != null) :
				new PreconditionException(
						"data != null && data.length > 1 && "
						+ "Stream.of(data).allMatch(sd -> sd != null)");

		this.data = data;

		// Invariant checking
		assert	ComposedSignalData.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ComposedSignalData.implementationInvariants(this)");
		assert	ComposedSignalData.invariants(this) :
				new InvariantException("ComposedSignalData.invariants(this)");
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
		return false;
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.AbstractSignalData#isComposed()
	 */
	@Override
	public boolean		isComposed()
	{
		return true;
	}

	/**
	 * return the number of data directly contained in this composed signal data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the number of data directly contained in this composed signal data.
	 */
	public int			size()
	{
		return this.data.length;
	}

	/**
	 * return	the signal data at index {@code i} in this composed signal data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code i >= 0 && i < this.size()}
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @param i	index of a signal data to be retrieved.
	 * @return	the signal data at index {@code i} in this composed signal data.
	 */
	public AbstractSignalData	get(int i)
	{
		assert	i >= 0 && i < this.size() :
				new PreconditionException("i >= 0 && i < this.size()");

		return this.data[i];
	}

	/**
	 * return a stream to process the signal data in this composed signal data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code eturn != null}
	 * </pre>
	 *
	 * @return	a stream to process the signal data in this composed signal data.
	 */
	public Stream<AbstractSignalData>	stream()
	{
		return Stream.of(this.data);
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.AbstractSignalData#toStringBuffer(java.lang.StringBuffer)
	 */
	@Override
	public void toStringBuffer(StringBuffer sb)
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
	protected void			contentAsString(StringBuffer sb)
	{
		assert	sb != null : new PreconditionException("sb != null");

		for (int i = 0 ; i < this.size() ; i++) {
			this.get(i).toStringBuffer(sb);
			sb.append(", ");
		}
		super.contentAsString(sb);
	}

	/**
	 * return true if the observed deviation among time references is under the
	 * threshold {@code DEVIATION_TOLERANCE_AMONG_TIME_REFERENCES}, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the observed deviation among time references is under the threshold {@code DEVIATION_TOLERANCE_AMONG_TIME_REFERENCES}, false otherwise.
	 */
	@Override
	public boolean		coherentTimestampers()
	{
		return this.timestampersCoherence() <=
								DEVIATION_TOLERANCE_AMONG_TIME_REFERENCES;
	}

	/**
	 * compute the largest difference between the start times of the
	 * accelerated clocks used as time references for the single signal data
	 * within this composed signal data and recursively.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <p>
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the largest difference between the start times of the accelerated clocks used as time references for the single signal data
	 * within this composed signal data and recursively.
	 */
	protected long		timestampersCoherence()
	{
		SortedSet<Long> s = new TreeSet<>();
		this.collectReferenceEpochTimesInNanos(s);
		return s.last() - s.first();
	}

	/**
	 * for composed signal data, the freshness is defined as the duration
	 * between {@code current} and the earliest time stamp among the ones of
	 * single signal data contained in this composed signal data and recursively
	 * within every composed signal data it may contain.
	 * 
	 * @see fr.sorbonne_u.alasca.physical_data.TimedEntity#freshness(java.time.Instant)
	 */
	@Override
	protected Duration	freshness(Instant current)
	throws	IncoherentTimestampersException
	{
		if (!this.coherentTimestampers()) {
			new IncoherentTimestampersException();
		}

		SortedSet<Instant> s = new TreeSet<>();
		this.collectTimestamps(s);
		return Duration.between(s.first(), current);
	}

	/**
	 * for composed signal data, the freshness is defined as the duration
	 * between the earliest and the latest time stamps among the ones of
	 * single signal data contained in this composed signal data and recursively
	 * within every composed signal data it may contain.
	 * 
	 * @see fr.sorbonne_u.alasca.physical_data.TimedEntity#timeCoherence()
	 */
	@Override
	public Duration			timeCoherence()
	throws	IncoherentTimestampersException
	{
		if (!this.coherentTimestampers()) {
			new IncoherentTimestampersException();
		}

		SortedSet<Instant> s = new TreeSet<>();
		this.collectTimestamps(s);
		return Duration.between(s.first(), s.last());
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.TimedEntity#collectTimestamps(java.util.Set)
	 */
	@Override
	protected void			collectTimestamps(Set<Instant> s)
	{
		super.collectTimestamps(s);

		for (int i = 0 ; i < this.size() ; i++) {
			this.get(i).collectTimestamps(s);
		}
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.TimedEntity#collectReferenceEpochTimesInNanos(java.util.Set)
	 */
	@Override
	protected void			collectReferenceEpochTimesInNanos(Set<Long> s)
	{
		super.collectReferenceEpochTimesInNanos(s);

		for (int i = 0 ; i < this.size() ; i++) {
			this.get(i).collectReferenceEpochTimesInNanos(s);
		}
	}
}
// -----------------------------------------------------------------------------
