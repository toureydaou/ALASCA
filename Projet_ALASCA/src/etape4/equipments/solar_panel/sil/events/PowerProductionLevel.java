package etape4.equipments.solar_panel.sil.events;

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
import etape4.equipments.solar_panel.sil.SolarPanelStateSILModel;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

// -----------------------------------------------------------------------------
/**
 * The class <code>PowerProductionLevel</code> represent an instantaneous power
 * production level of the solar panel.
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
 * <p>Created on : 2026-01-05</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			PowerProductionLevel
extends		Event
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	public static class	PowerLevel
	implements	EventInformationI
	{
		private static final long serialVersionUID = 1L;
		/** power level to be transmitted.									*/
		protected final double	powerLevel;
		/** time at which the power level has been computed.				*/
		protected final Time	timestamp;

		/**
		 * create a power level instance.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code powerLevel >= 0.0}
		 * pre	{@code timestamp != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param powerLevel	power level to be transmitted.
		 * @param timestamp		time at which the power level has been computed.
		 */
		public PowerLevel(double powerLevel, Time timestamp)
		{
			super();

			assert	powerLevel >= 0.0 :
					new NeoSim4JavaException("powerLevel >= 0.0");
			assert	timestamp != null :
					new NeoSim4JavaException("timestamp != null");

			this.powerLevel = powerLevel;
			this.timestamp = timestamp;
		}

		/**
		 * return the value of powerLevel.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return the powerLevel.
		 */
		public double	getPowerLevel()
		{
			return powerLevel;
		}

		/**
		 * return the value of timestamp.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return the timestamp.
		 */
		public Time getTimestamp() {
			return timestamp;
		}

		/**
		 * @see java.lang.Object#clone()
		 */
		@Override
		public PowerLevel	clone() throws CloneNotSupportedException
		{
			return new PowerLevel(this.powerLevel, this.timestamp);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String	toString()
		{
			StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
			sb.append("[powerLevel = ");
			sb.append(this.powerLevel);
			sb.append(", timestamp = ");
			sb.append(this.timestamp);
			sb.append("]");
			return sb.toString();
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
	 * create an event from the given time of occurrence and event description.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code powerLevel == null || getEventInformation().equals(powerLevel)}
	 * </pre>
	 *
	 * @param timeOfOccurrence	time of occurrence of the created event.
	 * @param powerLevel		the powder level to be transmitted.
	 */
	public				PowerProductionLevel(
		Time timeOfOccurrence,
		PowerLevel powerLevel
		)
	{
		super(timeOfOccurrence, powerLevel);
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
		assert	model instanceof SolarPanelStateSILModel :
				new NeoSim4JavaException(
						"Precondition violation: model instanceof "
						+ "SolarPanelStateSILModel");

		((SolarPanelStateSILModel)model).setNewPowerLevel(
									(PowerLevel) this.getEventInformation());
	}
}
// -----------------------------------------------------------------------------
