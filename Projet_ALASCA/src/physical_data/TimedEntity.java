package physical_data;

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

import java.time.Instant;
import java.util.Set;
import java.time.Duration;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import java.net.UnknownHostException;

// -----------------------------------------------------------------------------
/**
 * The class <code>TimedEntity</code> defines the common fields and methods
 * managing the timing information of timed entities.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The major implementation decision for this functionality is to use the
 * class {@code java.time.Instant} to represent punctual times, as well as the
 * class {@code java.time.Duration} to represent elapsed times.
 * </p>
 * <p>
 * Another important decision is to use IP addresses to identify hosts, which
 * is indeed fragile as IP addresses can be local and also dynamically
 * attributed to hosts.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code timestamp != null}
 * invariant	{@code timestamper != null && ! timestamper.isEmpty()}
 * invariant	{@code ac != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-11-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	TimedEntity
implements	TimingI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long 			serialVersionUID = 1L;
	/** time stamp as a Java {@code Instant} object.						*/
	protected final Instant				timestamp;
	/** identity of the time stamper, a host IP address or the URI of the
	 *  accelerated clock if one is used.									*/
	protected final String				timestamper;
	/** accelerated clock giving the time reference, if any.				*/
	protected final AcceleratedClock	ac;

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
	protected static boolean	implementationInvariants(TimedEntity instance)
	{
		assert	instance != null : new PreconditionException("instance != null");

		boolean ret = true;
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
	 * @return			true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(TimedEntity instance)
	{
		assert	instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a timed entity with the current time as time stamp, the current
	 * host as time stamper and its hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code getTimestamp().equals(Instant.ofEpochMilli(System.currentTimeMillis())}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * </pre>
	 *
	 */
	public				TimedEntity()
	{
		this(Instant.ofEpochMilli(System.currentTimeMillis()));
	}

	/**
	 * create a timed entity with the given time stamp and the current host as
	 * time stamper and its hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timestamp != null}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * </pre>
	 *
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 */
	public				TimedEntity(
		Instant timestamp
		)
	{
		this(timestamp, getStandardTimestamper());

		assert	getTimestamper().equals(getStandardTimestamper()) :
				new PostconditionException(
						"getTimeReference().equals(getStandardTimestamper())");
	}

	/**
	 * create a timed entity with the given time stamp, time stamper identity
	 * and the current host hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timestamp != null}
	 * pre	{@code timestamper != null && !timestamper.isEmpty()}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimestamper().equals(timestamper)}
	 * </pre>
	 *
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 * @param timestamper	identity of the time stamping host <i>e.g.</i>, its IP address.
	 */
	public				TimedEntity(
		Instant timestamp,
		String timestamper
		)
	{
		super();

		assert	timestamp != null :
				new PreconditionException("timestamp != null");
		assert	timestamper != null && !timestamper.isEmpty() :
				new PreconditionException(
						"timestamper != null && !timestamper.isEmpty()");

		this.timestamp = timestamp;
		this.timestamper = timestamper;
		this.ac = null;

		assert	getTimestamp().equals(timestamp) :
				new PostconditionException("getTimestamp().equals(timestamp)");
		assert	getTimestamper().equals(timestamper) :
				new PreconditionException("getTimestamper().equals(timestamper)");

		assert	TimedEntity.implementationInvariants(this) :
				new ImplementationInvariantException(
						"TimedEntity.implementationInvariants(this)");
		assert	TimedEntity.invariants(this) :
				new InvariantException("TimedEntity.invariants(this)");
	}

	/**
	 * create a timed entity with the current time as time stamp, the current
	 * host as time stamper and the given accelerated clock giving the time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * post	{@code getTimestamp().equals(ac.currentInstant())}
	 * post	{@code getTimestamper().equals(ac.getClockURI())}
	 * </pre>
	 *
	 * @param ac	an accelerated clock giving the time reference.
	 */
	public				TimedEntity(AcceleratedClock ac)
	{
		this(ac, ac.currentInstant());
	}

	/**
	 * create a timed entity with the given time stamp and the current host as
	 * time stamper.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code timestamp != null}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimestamper().equals(ac.getClockURI())}
	 * </pre>
	 *
	 * @param ac			an accelerated clock giving the time reference.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 */
	public				TimedEntity(
		AcceleratedClock ac,
		Instant timestamp
		)
	{
		super();

		assert	ac != null : new PreconditionException("ac != null");
		assert	timestamp != null :
				new PreconditionException("timestamp != null");

		this.ac = ac;
		this.timestamp = timestamp;
		this.timestamper = ac.getClockURI();

		assert	getTimestamp().equals(timestamp) :
				new PostconditionException("getTimestamp().equals(timestamp)");
		assert	getTimestamper().equals(ac.getClockURI()) :
				new PostconditionException(
						"getTimestamper().equals(ac.getClockURI())");

		assert	TimedEntity.implementationInvariants(this) :
				new ImplementationInvariantException(
						"TimedEntity.implementationInvariants(this)");
		assert	TimedEntity.invariants(this) :
				new InvariantException("TimedEntity.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * return the identity of the standard time stamper, to be interpreted in
	 * the context; for example, it can be the IP address of the host which
	 * hardware clock is used as time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && !return.isEmpty()}
	 * </pre>
	 *
	 * @return	the identity of the standard time stamper.
	 */
	protected static String	getStandardTimestamper()
	{
		try {
			return java.net.Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return UNKNOWN_TIMESTAMPER;
		}
	}

	// -------------------------------------------------------------------------
	// Internal methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.TimingI#hasHardwareTimeReference()
	 */
	@Override
	public boolean	hasHardwareTimeReference()
	{
		return this.ac == null;
	}

	/**
	 * return the current instant under the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current instant under the time reference.
	 */
	protected Instant	getCurrentInstant()
	{
		if (this.hasHardwareTimeReference()) {
			return Instant.ofEpochMilli(System.currentTimeMillis());
		} else {
			return this.ac.currentInstant();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String		toString()
	{
		StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
		sb.append('[');
		this.contentAsString(sb);
		sb.append(']');
		return sb.toString();
	}

	/**
	 * add the information contained in this part of a timed entity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sb != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sb	a buffer to put the information in.
	 */
	protected void		contentAsString(StringBuffer sb)
	{
		assert	sb != null : new PreconditionException("sb != null");

		sb.append(this.timestamp);
		sb.append(", ");
		sb.append(this.getTimestamper());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.TimingI#getTimestamp()
	 */
	@Override
	public Instant		getTimestamp()
	{
		return this.timestamp;
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.TimingI#getTimestamper()
	 */
	@Override
	public String		getTimestamper()
	{
		return this.timestamper;
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.TimingI#freshness()
	 */
	@Override
	public Duration		freshness() throws IncoherentTimestampersException
	{
		return this.freshness(this.getCurrentInstant());
	}

	/**
	 * return the freshness of the timed entity against {@code current}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code current != null && current.compareTo(getTimestamp()) > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	the current instant.
	 * @return			the freshness of the timed entity against {@code current}.
	 * @throws IncoherentTimestampersException never happens at this level.
	 */
	protected Duration	freshness(Instant current)
	throws IncoherentTimestampersException
	{
		assert	current != null && current.compareTo(this.getTimestamp()) > 0 :
				new PreconditionException(
						"current != null && "
						+ "current.compareTo(this.getTimestamp()) > 0");

		return Duration.between(this.getTimestamp(), current);
	}

	/**
	 * return true as for single time entities, the time stamper coherence is
	 * meaningless as there is only one time stamper.
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
	public boolean		coherentTimestampers()
	{
		return true;
	}

	/**
	 * @throws IncoherentTimestampersException never happen at this level.
	 * @see fr.sorbonne_u.alasca.physical_data.TimingI#timeCoherence()
	 */
	@Override
	public Duration		timeCoherence()	throws IncoherentTimestampersException
	{
		return Duration.ZERO;
	}

	/**
	 * collect the time stamps of this timed entity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code s != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param s	the set of time stamps to which must be added the time stamps of this timed entity.
	 */
	protected void		collectTimestamps(Set<Instant> s)
	{
		s.add(this.getTimestamp());
	}

	/**
	 * collect reference Unix Epoch times in nanoseconds of the time stampers
	 * of timed entity; for a {@code TimedEntity}, there is only one time
	 * stamper.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The objective of this method is to compare the time stampers among a
	 * set of timed entities to ensure that there is not a so large deviation
	 * among them that their time stamps become incomparable. The time stamper
	 * of a {@code TimedEntity} can be either an accelerated clock or the
	 * hardware clock of the host on which it has been created. For the moment,
	 * we consider that the hardware clocks of the computers used in an
	 * application are correctly synchronised, so the method puts in the set
	 * {@code s} the current Unix Epoch time in nanoseconds on the current host
	 * so that as the collection is assumed to be fast, this will result as a
	 * coherent set of time stampers if only hardware clocks have been used to
	 * time stamp all of the timed entities.
	 * </p>
	 * <p>
	 * If the time stamper is an accelerated clock, the method puts in {@code s}
	 * its start time in Unix Epoch time in nanoseconds. Hence, if all of the
	 * collected times come from accelerated clocks, differences in their start
	 * times can be large and therefore subject to deviations that must be
	 * detected. As we assume that in applications, their differences should be
	 * as small as possible. If there is a mix between accelerated clocks and
	 * hardware clocks used to time stamp the timed entities, the differences
	 * between their start times of accelerated clocks and the current times of
	 * hardware clocks should be large enough to be detected as a too large
	 * deviation, so an incoherent set of time stampers.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code s != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param s	the set to which must be added the start time in Unix Epoch time in nanoseconds according to the accelerated clock of this timed entity.
	 */
	protected void		collectReferenceEpochTimesInNanos(Set<Long> s)
	{
		assert	s != null : new PreconditionException("s != null");

		if (this.ac == null) {
			s.add(System.nanoTime());
		} else {
			s.add(this.ac.getStartEpochNanos());
		}
	}
}
// -----------------------------------------------------------------------------
