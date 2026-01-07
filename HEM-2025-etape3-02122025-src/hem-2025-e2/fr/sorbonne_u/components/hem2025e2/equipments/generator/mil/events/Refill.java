package fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events;

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
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.GeneratorFuelModel;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Refill</code> implements the event that marks the refilling
 * of the generator tank with the given quantity of fuel.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code ((FuelQuantity)instance.getEventInformation()).quantity != null}
 * invariant	{@code MeasurementUnit.LITERS.equals(((FuelQuantity)instance.getEventInformation()).quantity.getMeasurementUnit())}
 * invariant	{@code ((FuelQuantity)instance.getEventInformation()).quantity.getData() > 0.0}
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
public class			Refill
extends		Event
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>FuelQuantity</code> implements a fuel quantity to
	 * be added to the generator tank when refilling.
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
	 * <p>Created on : 2025-10-27</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class	FuelQuantity
	implements	EventInformationI
	{
		private static final long serialVersionUID = 1L;
		/** the quantity to be added in MeasurementUnit.LITERS.				*/
		protected Measure<Double>	quantity;

		/**
		 * create an instance.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code quantity != null && MeasurementUnit.LITERS.equals(quantity.getMeasurementUnit()) && quantity.getData() > 0.0}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param quantity	the quantity to be added in MeasurementUnit.LITERS.
		 */
		public			FuelQuantity(Measure<Double> quantity)
		{
			super();
			assert	quantity != null &&
						MeasurementUnit.LITERS.equals(
											quantity.getMeasurementUnit()) &&
							quantity.getData() > 0.0 :
					new NeoSim4JavaException(
							"quantity != null && MeasurementUnit.LITERS.equals("
							+ "quantity.getMeasurementUnit()) && quantity."
							+ "getData() > 0.0");

			this.quantity = quantity;
		}

		/**
		 * return the value of quantity.
		 *
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code return != null}
		 * </pre>
		 *
		 * @return the quantity of the refill.
		 */
		public Measure<Double>	getQuantity()
		{
			return quantity;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String		toString()
		{
			return this.getClass().getSimpleName() + "["
													+ this.getQuantity() + "]";
		}
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

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
	public static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
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
	protected static boolean	implementationInvariants(Refill instance)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= Refill.staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
				((FuelQuantity)instance.getEventInformation()).quantity != null,
				Refill.class, instance,
				"((FuelQuantity)instance.getEventInformation()).quantity != null");
		ret &= AssertionChecking.checkImplementationInvariant(
				MeasurementUnit.LITERS.equals(((FuelQuantity)instance.
						getEventInformation()).quantity.getMeasurementUnit()),
				Refill.class, instance,
				"MeasurementUnit.LITERS.equals(((FuelQuantity)instance."
				+ "getEventInformation()).quantity.getMeasurementUnit())");
		ret &= AssertionChecking.checkImplementationInvariant(
				((FuelQuantity)instance.getEventInformation()).
													quantity.getData() > 0.0,
				Refill.class, instance,
				"((FuelQuantity)instance.getEventInformation()).quantity."
				+ "getData() > 0.0");
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
	public static boolean	staticInvariants()
	{
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
	protected static boolean	invariants(Refill instance)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= Refill.staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>TankNoLongerEmpty</code> event.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code timeOfOccurrence != null}
	 * post	{@code getTimeOfOccurrence().equals(timeOfOccurrence)}
	 * post	{@code getEventInformation() == null}
	 * </pre>
	 *
	 * @param content			the quantity of fuel to be added.
	 * @param timeOfOccurrence	time of occurrence of the event.
	 */
	public				Refill(
		Time timeOfOccurrence,
		EventInformationI content
		)
	{
		super(timeOfOccurrence, content);

		// Invariant checking
		assert	Refill.implementationInvariants(this) :
				new NeoSim4JavaException(
						"Implementation Invariants violation: "
						+ "Refill.implementationInvariants(this)");
		assert	Refill.invariants(this) :
				new NeoSim4JavaException(
						"Invariants violation: Refill.invariants(this)");
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
		assert	model instanceof GeneratorFuelModel :
				new NeoSim4JavaException(
						"Precondition violation: model instanceof "
						+ "GeneratorPowerModel");

		GeneratorFuelModel m = (GeneratorFuelModel) model;
		m.refill(((FuelQuantity)this.content).getQuantity().getData());
	}
}
// -----------------------------------------------------------------------------
