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
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Measure</code> implements a physical value about or measure
 * made on some phenomenon having a value of generic type T and a
 * {@code MeasurementUnit}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code measurementUnit != null}
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
public class			Measure<T extends Serializable>
implements	MeasureI<T>
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long			serialVersionUID = 1L;
	/** the measured data.													*/
	protected final T					data;
	/** the measurement unit in which {@code data} is expressed.			*/
	protected final MeasurementUnit		measurementUnit;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code m != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	instance to be tested.
	 * @return	true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(Measure<?> m)
	{
		assert	m != null : new PreconditionException("m != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
					m.measurementUnit != null,
					Measure.class, m,
					"m.measurementUnit != null");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cm != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param m	instance to be tested.
	 * @return	true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(Measure<?> m)
	{
		assert	m != null : new PreconditionException("m != null");

		boolean ret = true;
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a (raw) measure with no measurement unit.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param data	the measurement data.
	 */
	public				Measure(T data)
	{
		this(data, MeasurementUnit.RAW);
	}

	/**
	 * create a measure with the given measurement unit.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code measurementUnit != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param data					the measurement data.
	 * @param measurementUnit		the measurement unit used to expressed {@code data}.
	 */
	public				Measure(
		T data,
		MeasurementUnit measurementUnit
		)
	{
		super();

		assert	measurementUnit != null :
				new PreconditionException("measurementUnit != null");

		this.data = data;
		this.measurementUnit = measurementUnit;

		assert	implementationInvariants(this) :
				new ImplementationInvariantException(
						"Measure.implementationInvariants(this)");
		assert	invariants(this) :
				new InvariantException("Measure.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.MeasureI#getData()
	 */
	@Override
	public T			getData()
	{
		return this.data;
	}

	/**
	 * @see fr.sorbonne_u.alasca.physical_data.MeasureI#getMeasurementUnit()
	 */
	@Override
	public MeasurementUnit	getMeasurementUnit()
	{
		return this.measurementUnit;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean		equals(Object obj)
	{
		if (obj == null) {
			return false;
		} else if (!(obj instanceof Measure)) {
			return false;
		} else {
			return this.data.equals(((Measure)obj).getData()) &&
				   this.measurementUnit.equals(((Measure)obj).
						   								getMeasurementUnit());
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
	 * add the local content to be embedded in a larger {@code toString}
	 * process using a {@code StringBuffer} and return the local content alone
	 * as a {@code toString}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sb != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sb	a {@code StringBuffer} to which the local content is added.
	 * @return		a {@code String} representing only the local content.
	 */
	protected String	contentAsString(StringBuffer sb)
	{
		assert	sb != null : new PreconditionException("sb != null");

		StringBuffer local = new StringBuffer();
		local.append(this.data);
		local.append(", ");
		local.append(this.measurementUnit);
		sb.append(local);
		return local.toString();
	}
}
// -----------------------------------------------------------------------------
