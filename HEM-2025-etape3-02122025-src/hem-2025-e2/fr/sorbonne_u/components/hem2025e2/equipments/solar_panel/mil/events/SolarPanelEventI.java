package fr.sorbonne_u.components.hem2025e2.equipments.solar_panel.mil.events;

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

import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import java.io.Serializable;
import java.time.ZonedDateTime;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

// -----------------------------------------------------------------------------
/**
 * The interface <code>SolarPanelEventI</code> defines a common
 * super-type for events that are internal to sunrise and sunset models.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * Besides being a common super-type, this interface provides definitions that
 * are used by the solar panel events.
 * </p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2025-10-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		SolarPanelEventI
extends		EventI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>Position</code> defines a position, either longitude or
	 * latitude, in degrees, minutes and seconds.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Implementation Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code degree >= 0 && degree < 360}
	 * invariant	{@code minutes >= 0 && minutes < 60}
	 * invariant	{@code seconds >= 0 && seconds < 60}
	 * </pre>
	 * 
	 * <p><strong>Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2025-10-06</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public class		Position
	implements	Serializable
	{
		// ---------------------------------------------------------------------
		// Constants and variables
		// ---------------------------------------------------------------------

		private static final long serialVersionUID = 1L;
		protected final int		degree;
		protected final int		minutes;
		protected final double	seconds;

		// -------------------------------------------------------------------------
		// Constructors
		// -------------------------------------------------------------------------

		public			Position(int degree, int minutes, double seconds)
		{
			super();

			assert	degree >= 0 && degree < 360 :
					new NeoSim4JavaException("degree >= 0 && degree < 360");
			assert	minutes >= 0 && minutes < 60 :
					new NeoSim4JavaException("minutes >= 0 && minutes < 60");
			assert	seconds >= 0 && seconds < 60 :
					new NeoSim4JavaException("seconds >= 0 && seconds < 60");

			this.degree = degree;
			this.minutes = minutes;
			this.seconds = seconds;
		}

		// -------------------------------------------------------------------------
		// Methods
		// -------------------------------------------------------------------------

		/**
		 * return the value of degree.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return the degree
		 */
		public int getDegree() {
			return degree;
		}

		/**
		 * return the value of minutes.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return the minutes
		 */
		public int getMinutes() {
			return minutes;
		}

		/**
		 * return the value of seconds.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return the seconds
		 */
		public double getSeconds() {
			return seconds;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String	toString()
		{
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append('[');
			sb.append(this.degree);
			sb.append(", ");
			sb.append(this.minutes);
			sb.append(", ");
			sb.append(this.seconds);
			sb.append(']');
			return sb.toString();
		}
	}

	/**
	 * The class <code>CurrentZonedDateTime</code>
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
	 * <p>Created on : 2025-10-06</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public class		CurrentZonedDateTime
	implements	EventInformationI
	{
		// ---------------------------------------------------------------------
		// Constants and variables
		// ---------------------------------------------------------------------
 
		private static final long serialVersionUID = 1L;
		protected ZonedDateTime	current;

		// -------------------------------------------------------------------------
		// Constructors
		// -------------------------------------------------------------------------

		public CurrentZonedDateTime(ZonedDateTime current) {
			super();
			this.current = current;
		}

		// -------------------------------------------------------------------------
		// Methods
		// -------------------------------------------------------------------------

		/**
		 * return the value of the current time.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return	the value of the current time.
		 */
		public ZonedDateTime	getCurrent() {
			return current;
		}

		/**
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no more preconditions.
		 * post	{@code true}	// no more postconditions.
		 * </pre>
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String		toString()
		{
			return this.getClass().getSimpleName() + '[' + this.current + ']';
		}
	}

	// -------------------------------------------------------------------------
	// Signatures
	// -------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
