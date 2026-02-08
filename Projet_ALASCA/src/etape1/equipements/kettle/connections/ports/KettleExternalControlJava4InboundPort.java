package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleExternalControlJava4CI;
import etape1.equipements.kettle.interfaces.KettleImplementationI;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleState;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleExternalControlJava4InboundPort</code> extends the
 * external control inbound port with Java 4 compatible methods for
 * Javassist connector generation.
 *
 * <p>
 * This port delegates to the parent port's methods (which use
 * KettleExternalControlI interface) instead of casting directly to Kettle.
 * This ensures compatibility with both Kettle (etape1) and KettleCyPhy
 * (etape3) components.
 * </p>
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class KettleExternalControlJava4InboundPort extends KettleExternalControlInboundPort
		implements KettleExternalControlJava4CI {

	private static final long serialVersionUID = 1L;

	public KettleExternalControlJava4InboundPort(ComponentI owner) throws Exception {
		super(KettleExternalControlJava4CI.class, owner);
	}

	public KettleExternalControlJava4InboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, KettleExternalControlJava4CI.class, owner);
	}

	// -------------------------------------------------------------------------
	// Java4 methods (primitive types for Javassist)
	// Delegates to parent port methods (KettleExternalControlI) instead of
	// casting to Kettle directly, for compatibility with KettleCyPhy.
	// -------------------------------------------------------------------------

	@Override
	public int getStateJava4() throws Exception {
		KettleState state = this.getState();
		return state.ordinal();
	}

	@Override
	public int getKettleModeJava4() throws Exception {
		KettleMode mode = this.getKettleMode();
		return mode.ordinal();
	}

	@Override
	public double getTargetTemperatureJava4() throws Exception {
		return this.getTargetTemperature().getData();
	}

	@Override
	public double getCurrentTemperatureJava4() throws Exception {
		return this.getCurrentTemperature().getData();
	}

	@Override
	public double getMaxPowerLevelJava4() throws Exception {
		return this.getMaxPowerLevel().getData();
	}

	@Override
	public double getCurrentPowerLevelJava4() throws Exception {
		return this.getCurrentPowerLevel().getData();
	}

	@Override
	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception {
		this.setCurrentPowerLevel(
			new Measure<Double>(powerLevel, KettleImplementationI.POWER_UNIT));
	}

	@Override
	public void setModeJava4(int mode) throws Exception {
		// Map int mode to power level and set via setCurrentPowerLevel
		double powerLevel;
		switch (mode) {
			case 1: powerLevel = 0.0; break;       // SUSPEND
			case 2: powerLevel = 1000.0; break;     // ECO
			case 3: powerLevel = 2000.0; break;     // NORMAL
			case 4: powerLevel = 3000.0; break;     // MAX
			default: powerLevel = 0.0;
		}
		this.setCurrentPowerLevel(
			new Measure<Double>(powerLevel, KettleImplementationI.POWER_UNIT));
	}
}
// -----------------------------------------------------------------------------
