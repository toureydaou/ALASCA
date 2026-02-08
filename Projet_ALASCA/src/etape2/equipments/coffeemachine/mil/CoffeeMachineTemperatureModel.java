package etape2.equipments.coffeemachine.mil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import etape1.equipements.coffee_machine.Constants;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineMode;
import etape1.equipements.coffee_machine.interfaces.CoffeeMachineImplementationI.CoffeeMachineState;
import etape2.GlobalReportI;
import etape2.equipments.coffeemachine.mil.events.DoNotHeat;
import etape2.equipments.coffeemachine.mil.events.MakeCoffee;
import etape2.equipments.coffeemachine.mil.events.SwitchOffCoffeeMachine;
import etape2.equipments.coffeemachine.mil.events.SwitchOnCoffeeMachine;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

// -----------------------------------------------------------------------------
/**
 * La classe <code>CoffeeMachineTemperatureModel</code> simule l'évolution de la
 * température de l'eau dans le réservoir. *
 * <p>
 * <strong>Physique</strong>
 * </p>
 * La dérivée de la température est calculée selon la formule : dT/dt =
 * (Puissance_Chauffage) / (Masse_Eau * Capacité_Thermique_Eau) -
 * (Pertes_Thermiques)
 */
@ModelExternalEvents(imported = { SwitchOnCoffeeMachine.class, SwitchOffCoffeeMachine.class, MakeCoffee.class,
		DoNotHeat.class })
@ModelImportedVariable(name = "currentHeatingPower", type = Double.class)
@ModelImportedVariable(name = "currentWaterLevel", type = Double.class)
public class CoffeeMachineTemperatureModel extends AtomicHIOA implements CoffeeMachineOperationI {

	// -------------------------------------------------------------------------
	// Constantes et Variables
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;
	public static final String URI = CoffeeMachineTemperatureModel.class.getSimpleName();

	public static boolean VERBOSE = true;
	public static boolean DEBUG = true;

	/** Capacité thermique spécifique de l'eau en J/kg/°C (ou Kelvin). */
	protected static double WATER_SPECIFIC_HEAT_CAPACITY = Constants.WATER_THERMAL_CAPACITY ;

	/**
	 * Facteur de perte thermique (isolation du réservoir). Plus il est grand,
	 * meilleure est l'isolation.
	 */
	protected static double INSULATION_CONSTANT = 1000.0;

	/** Température ambiante (si non importée d'un modèle de pièce). */
	protected static double AMBIENT_TEMPERATURE = 20.0;

	
	protected static double		STEP = 60.0/3600.0;

	/** État actuel de la machine (similaire au modèle électrique). */
	protected CoffeeMachineState currentState = CoffeeMachineState.ON;

	protected final Duration integrationStep;

	/**
	 * accumulator to compute the mean external temperature for the simulation
	 * report.
	 */
	protected double temperatureAcc;
	/** the simulation time of start used to compute the mean temperature. */
	protected Time start;
	/**
	 * the mean temperature over the simulation duration for the simulation report.
	 */
	protected double meanTemperature;

	// -------------------------------------------------------------------------
	// Variables HIOA
	// -------------------------------------------------------------------------

	/** Puissance de chauffe actuelle (Importée du modèle électrique). */
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentHeatingPower;

	/** Niveau d'eau actuel en Litres/kg (Importée du modèle électrique). */
	@ImportedVariable(type = Double.class)
	protected Value<Double> currentWaterLevel;

	/** Température de l'eau actuelle (Variable interne calculée). */
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double> currentWaterTemperature = new DerivableValue<>(this);

	// -------------------------------------------------------------------------
	// Constructeur
	// -------------------------------------------------------------------------

	public CoffeeMachineTemperatureModel(String uri, TimeUnit simulatedTimeUnit, AtomicSimulatorI simulationEngine)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.getSimulationEngine().setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Méthodes de calcul physique (Adaptation de la méthode startHeating)
	// -------------------------------------------------------------------------

	/**
	 * Calcule la dérivée de la température (dT/dt) à l'instant t. * C'est ici que
	 * l'on adapte la formule : heatingTime = (mass * Cp * deltaT) / Power
	 * Devenant : Rate (dT/dt) = Power / (mass * Cp) * @param currentTemp La
	 * température actuelle.
	 * 
	 * @return La dérivée (variation en °C par seconde).
	 */
	protected double computeDerivatives(Double currentTemp) {

		// Récupérer la masse d'eau (en kg, supposant 1L = 1kg)
		double waterMass = this.currentWaterLevel.getValue();

		// Sécurité : si pas d'eau, pas de chauffe (ou chauffe très vite le métal,
		// mais on évite la division par zéro).
		if (waterMass <= 0.001) {
			return 0.0;
		}

		double heatingContribution = 0.0;

		// Contribution du chauffage (Gain)
		if (this.currentState == CoffeeMachineState.HEATING) {
			double power = this.currentHeatingPower.getValue(); // En Watts (Joules/sec)

			// Formule : P = m * Cp * (dT/dt) => dT/dt = P / (m * Cp)
			heatingContribution = power / (waterMass * WATER_SPECIFIC_HEAT_CAPACITY) * 60.0;
		}

		// Contribution du refroidissement (Perte vers l'extérieur)
		// Loi de refroidissement de Newton : k * (T_ext - T_eau)
		// Ici simplifié avec une constante d'isolation.
		double coolingContribution = (AMBIENT_TEMPERATURE - currentTemp) / INSULATION_CONSTANT;

		return heatingContribution + coolingContribution;
	}

	/**
	 * Calcule la nouvelle température après un temps deltaT via la méthode d'Euler.
	 */
	protected double computeNewTemperature(double deltaT) {
		Time t = this.currentWaterTemperature.getTime();
		double oldTemp = this.currentWaterTemperature.evaluateAt(t);
		double derivative = this.currentWaterTemperature.getFirstDerivative();

		// T(t+dt) = T(t) + T'(t) * dt
		double newTemp = oldTemp + derivative * deltaT * 3600;

		return newTemp;
	}

	// -------------------------------------------------------------------------
	// Gestion de l'état (Appelé par les événements)
	// -------------------------------------------------------------------------

	public void setState(CoffeeMachineState s) {
		this.currentState = s;
	}

	public CoffeeMachineState getState() {
		return this.currentState;
	}

	// -------------------------------------------------------------------------
	// Protocole DEVS
	// -------------------------------------------------------------------------

	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);
		// Initialisation : Température ambiante, pas de dérivée au début.
		this.start = initialTime;
		if (VERBOSE) {
			this.logMessage("Simulation starts. Water Temp: " + AMBIENT_TEMPERATURE);
		}
	}

	@Override
	public boolean useFixpointInitialiseVariables() {
		return true;
	}

	@Override
	public Pair<Integer, Integer> fixpointInitialiseVariables() {
		
		int justInitialised = 0;
		int notInitialisedYet = 0;
		
		// Si l'électricité n'a pas encore initialisé le niveau d'eau ou la puissance
		if (!this.currentWaterLevel.isInitialised() || !this.currentHeatingPower.isInitialised()) {
			notInitialisedYet++;
		}
		else {
			
			if (!this.currentWaterTemperature.isInitialised()) {
				// Ici, on peut appeler computeDerivatives sans risque de NullPointerException
				double derivative = this.computeDerivatives(AMBIENT_TEMPERATURE);
				this.currentWaterTemperature.initialise(AMBIENT_TEMPERATURE, derivative);
				justInitialised++;
			}
		}

		return new Pair<>(justInitialised, notInitialisedYet);
	}

	@Override
	public ArrayList<EventI> output() {
		return null; // Modèle passif, ne génère pas d'événements sortants pour l'instant
	}

	@Override
	public Duration timeAdvance() {
		// On avance par petits pas (STEP) pour recalculer l'intégrale
		return this.integrationStep;
	}

	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		// Calculer la nouvelle température basée sur le temps écoulé
		double newTemp = this.computeNewTemperature(elapsedTime.getSimulatedDuration());

		// Mettre à jour la variable HIOA
		// Recalculer la dérivée pour le prochain pas (car la température a changé,
		// donc le refroidissement change)
		double newDerivative = this.computeDerivatives(newTemp);
		this.currentWaterTemperature.setNewValue(newTemp, newDerivative,
				new Time(this.getCurrentStateTime().getSimulatedTime(), this.getSimulatedTimeUnit()));

		

		if (VERBOSE && this.currentState == CoffeeMachineState.HEATING) {
			this.logMessage("Temp update: " + String.format("%.2f", newTemp) + "°C");
		}
		
		super.userDefinedInternalTransition(elapsedTime);
	}

	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		// Mise à jour la température jusqu'à l'instant présent (avant de traiter
		// l'événement)
		double newTemp = this.computeNewTemperature(elapsedTime.getSimulatedDuration());

		// Récupérer et exécuter l'événement (Heat, DoNotHeat, etc.)
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		assert currentEvents != null && currentEvents.size() == 1;
		Event ce = (Event) currentEvents.get(0);

		// L'exécution de l'événement va appeler this.setState(...)
		ce.executeOn(this);

		// Recalculer la dérivée avec le NOUVEL état (ex: on vient de passer en
		// HEATING)
		double newDerivative = this.computeDerivatives(newTemp);

		// Sauvegarder
		this.currentWaterTemperature.setNewValue(newTemp, newDerivative,
				new Time(this.getCurrentStateTime().getSimulatedTime() + elapsedTime.getSimulatedDuration(),
						this.getSimulatedTimeUnit()));

		super.userDefinedExternalTransition(elapsedTime);

		if (VERBOSE) {
			this.logMessage("Event received: " + ce.eventAsString() + " -> State: " + this.currentState + " | Water Level -> " + this.currentWaterLevel.getValue() );
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void endSimulation(Time endTime) {
		this.meanTemperature = this.temperatureAcc / endTime.subtract(this.start).getSimulatedDuration();

		if (VERBOSE) {
			this.logMessage("simulation ends.");
			
		}
		super.endSimulation(endTime);
	}

	public static class CoffeeMachineTemperatureReport implements SimulationReportI, GlobalReportI {
		private static final long serialVersionUID = 1L;
		protected String modelURI;
		protected double meanTemperature;

		public CoffeeMachineTemperatureReport(String modelURI, double meanTemperature) {
			super();
			this.modelURI = modelURI;
			this.meanTemperature = meanTemperature;
		}

		@Override
		public String getModelURI() {
			return this.modelURI;
		}

		@Override
		public String printout(String indent) {
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("mean temperature = ");
			ret.append(this.meanTemperature);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
	}

	/*
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI getFinalReport() {
		return new CoffeeMachineTemperatureReport(this.getURI(), this.meanTemperature);
	}

	@Override
	public void setMode(CoffeeMachineMode m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CoffeeMachineMode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStateMode(CoffeeMachineState on, CoffeeMachineMode normal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentHeatingPower(double newPower, Time t) {
		// Not used in temperature model - heating power is imported
	}

	@Override
	public void setCurrentWaterLevel(double newLevel, Time t) {
		// Not used in temperature model - water level is imported
	}
}