package tests_utils;

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
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>TimeUtils</code>
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
 * <p>Created on : 2025-10-21</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	TimeUtils
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticImplementationInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				true,
				TimeUtils.class,
				"");
		return ret;
	}

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
	protected static boolean	implementationInvariants(TimeUtils instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= TimeUtils.staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(true, TimeUtils.class, instance, "");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants() {
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				true,
				TimeUtils.class,
				"");
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
	protected static boolean	invariants(TimeUtils instance)
	{
		assert instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= TimeUtils.staticInvariants();
		ret &= AssertionChecking.checkInvariant(
				true,
				TimeUtils.class, instance,
				"");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the {@code fr.sorbonne_u.devs_simulation.models.time.Duration}
	 * between {@code from} and {@code to} in the {@code tu} time unit.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code from != null}
	 * pre	{@code to != null && (from.isBefore(to) || from.equals(to))}
	 * pre	{@code tu != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param from	an instant that begins an interval.
	 * @param to	an instant that ends an interval.
	 * @param tu	a time unit in which to express the result.
	 * @return		the duration between {@code from} and {@code to} in the {@code tu} time unit.
	 */
	public static Duration	betweenInDuration(
		Instant from,
		Instant to,
		TimeUnit tu
		)
	{
		assert	from != null : new PreconditionException("from != null");
		assert	to != null && (from.isBefore(to) || from.equals(to)) :
			new PreconditionException(
					"to != null && (from.isBefore(to) || from.equals(to))");
		assert	tu != null : new PreconditionException("tu != null");

		java.time.Duration d = java.time.Duration.between(from, to);
		long dInNanos = d.toNanos();
		long mantissa = tu.convert(dInNanos, TimeUnit.NANOSECONDS);
		long nanosInSimulatedTimeUnit = TimeUnit.NANOSECONDS.convert(1, tu);
		double fraction = (dInNanos - mantissa * nanosInSimulatedTimeUnit);
		return new Duration(mantissa + fraction/nanosInSimulatedTimeUnit, tu);
	}
}
// -----------------------------------------------------------------------------
