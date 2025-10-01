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

import java.time.Instant;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>AbstractSignalData</code> is the common type of all signal
 * data, be it single or composed.
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
 * <p>Created on : 2025-09-17</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	AbstractSignalData
extends		TimedEntity
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

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
		AbstractSignalData instance
		)
	{
		assert instance != null : new PreconditionException("instance != null");

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
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(AbstractSignalData instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a signal data with the current time as time stamp, the current
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
	public				AbstractSignalData()
	{
		super();
	}

	/**
	 * create a signal data with the given time stamp and the current host as
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
	public				AbstractSignalData(Instant timestamp)
	{
		super(timestamp);
	}

	/**
	 * create a signal data with the given time stamp, time stamper identity
	 * and the current host hardware clock giving the time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timestamp != null}
	 * pre	{@code timestamper != null && !timestamper.isEmpty()}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(getTimestamper())}
	 * </pre>
	 *
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 * @param timestamper	identity of the time stamping host <i>e.g.</i>, its IP address.
	 */
	public				AbstractSignalData(
		Instant timestamp,
		String timestamper
		)
	{
		super(timestamp, timestamper);
	}

	/**
	 * create a signal data with the current time as time stamp, the current
	 * host as time stamper and the given accelerated clock giving the time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * post	{@code getTimestamp().equals(ac.currentInstant())}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * </pre>
	 *
	 * @param ac	an accelerated clock giving the time reference.
	 */
	public				AbstractSignalData(AcceleratedClock ac)
	{
		super(ac);
	}

	/**
	 * create a signal data with the given time stamp and the current host as
	 * time stamper.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ac != null}
	 * pre	{@code timestamp != null}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * </pre>
	 *
	 * @param ac			an accelerated clock giving the time reference.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 */
	public				AbstractSignalData(
		AcceleratedClock ac,
		Instant timestamp
		)
	{
		super(ac, timestamp);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the signal data contains only one measure and false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the signal data contains only one measure and false otherwise.
	 */
	public abstract boolean	isSingle();

	/**
	 * return true if the signal data contains more than one measure and false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the signal data contains more than one measure and false otherwise.
	 */
	public abstract	boolean	isComposed();

	/**
	 * add the information contained in this signal data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sb	a buffer to put the information in.
	 */
	public abstract void	toStringBuffer(StringBuffer sb);
}
// -----------------------------------------------------------------------------
