package etape4.equipments.batteries.sil.events;

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

import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import etape1.equipments.batteries.Batteries.State;
import etape4.equipments.batteries.sil.BatteriesStateSILModel;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

// -----------------------------------------------------------------------------
/**
 * The class <code>CurrentBatteriesLevel</code> implements an event that
 * carries the current charge level of the batteries in
 * {@code MeasurementUnit.WATT_HOURS}.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * TO DO: represent the charge level as a measure.
 * </p>
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
 * <p>Created on : 2025-12-30</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CurrentBatteriesLevel
extends		Event
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>StateAndLevelValue</code> represents a state and a charge
	 * level as an information attached to an event.
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
	 * <p>Created on : 2025-12-30</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	StateAndLevelValue
	implements	EventInformationI
	{
		private static final long serialVersionUID = 1L;
		/** tolerance used when comparing two {@code double} values.		*/
		protected static final double TOLERANCE = 1e-8;

		/** state of the batteries at the moment the level was taken.		*/
		protected final State	batteriesState;
		/** the batteries level in {@code MeasurementUnit.WATT_HOURS}.		*/
		protected final double	batteriesLevel;

		/**
		 * create a level value object with the given batteries level.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code batteriesLevel >= 0.0}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param batteriesState	state of the batteries at the moment the level was taken.
		 * @param batteriesLevel	batteries level in {@code MeasurementUnit.WATT_HOURS}.
		 */
		public			StateAndLevelValue(
			State batteriesState,
			double batteriesLevel)
		{
			super();

			assert	batteriesLevel >= 0.0 :
					new NeoSim4JavaException("batteriesLevel >= 0.0");

			this.batteriesState = batteriesState;
			this.batteriesLevel = batteriesLevel;
		}

		/**
		 * return the state of the batteries at the moment the level was taken.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return	the state of the batteries at the moment the level was taken.
		 */
		public State	getBatteriesState()
		{
			return this.batteriesState;
		}

		/**
		 * return the value of batteries level in
		 * {@code MeasurementUnit.WATT_HOURS}.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code return >= 0.0}
		 * </pre>
		 *
		 * @return the batteries level in {@code MeasurementUnit.WATT_HOURS}.
		 */
		public double	getBatteriesLevel()
		{
			return batteriesLevel;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean		equals(Object obj)
		{
			if (obj == null) {
				return false;
			} else if (obj instanceof StateAndLevelValue) {
				StateAndLevelValue lv = (StateAndLevelValue) obj;
				return Math.abs(batteriesLevel - lv.batteriesLevel) < TOLERANCE;
			} else {
				return false;
			}
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String	toString()
		{
			StringBuffer ret = new StringBuffer(this.getClass().getSimpleName());
			ret.append("[batteriesState = ");
			ret.append(this.batteriesState);
			ret.append(", batteriesLevel = ");
			ret.append(this.batteriesLevel);
			ret.append("]");
			return ret.toString();
		}
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>StartCharging</code> event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * pre	{@code batteriesLevel != null}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code batteriesLevel.equals(getEventInformation())}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the event.
	 * @param batteriesLevel	the batteries level in {@code MeasurementUnit.WATT_HOURS}.
	 */
	public				CurrentBatteriesLevel(
		Time timeOfOccurrence,
		StateAndLevelValue batteriesLevel
		)
	{
		super(timeOfOccurrence,
			  AssertionChecking.assertNonNullOrThrow(
				batteriesLevel,
				() -> new NeoSim4JavaException(
							"Precondition violation: batteriesLevel != null")));

		assert	batteriesLevel.equals(getEventInformation()) :
				new NeoSim4JavaException(
						"Postcondition violation: batteriesLevel.equals("
						+ "getEventInformation())");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI)
	 */
	@Override
	public void			executeOn(AtomicModelI model)
	{
		// Preconditions checking
		assert	model instanceof BatteriesStateSILModel :
				new NeoSim4JavaException(
						"Precondition violation: model instanceof "
						+ "BatteriesStateSILModel");

		BatteriesStateSILModel m = (BatteriesStateSILModel) model;
		StateAndLevelValue slv = (StateAndLevelValue)this.getEventInformation();
		m.setBatteriesState(slv.getBatteriesState());
		m.setCurrentLevel(slv.getBatteriesLevel());
	}
}
// -----------------------------------------------------------------------------
