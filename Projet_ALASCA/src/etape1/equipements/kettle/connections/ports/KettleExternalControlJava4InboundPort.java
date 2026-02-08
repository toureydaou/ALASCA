package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.Kettle;
import etape1.equipements.kettle.interfaces.KettleExternalControlJava4CI;
import fr.sorbonne_u.components.ComponentI;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleExternalControlJava4InboundPort</code> extends the
 * external control inbound port with Java 4 compatible methods for
 * Javassist connector generation.
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
	// -------------------------------------------------------------------------

	@Override
	public int getStateJava4() throws Exception {
		return this.getOwner().handleRequest(o -> ((Kettle) o).getStateJava4());
	}

	@Override
	public int getKettleModeJava4() throws Exception {
		return this.getOwner().handleRequest(o -> ((Kettle) o).getKettleModeJava4());
	}

	@Override
	public double getTargetTemperatureJava4() throws Exception {
		return this.getOwner().handleRequest(o -> ((Kettle) o).getTargetTemperatureJava4());
	}

	@Override
	public double getCurrentTemperatureJava4() throws Exception {
		return this.getOwner().handleRequest(o -> ((Kettle) o).getCurrentTemperatureJava4());
	}

	@Override
	public double getMaxPowerLevelJava4() throws Exception {
		return this.getOwner().handleRequest(o -> ((Kettle) o).getMaxPowerLevelJava4());
	}

	@Override
	public double getCurrentPowerLevelJava4() throws Exception {
		return this.getOwner().handleRequest(o -> ((Kettle) o).getCurrentPowerLevelJava4());
	}

	@Override
	public void setCurrentPowerLevelJava4(double powerLevel) throws Exception {
		this.getOwner().handleRequest(o -> {
			((Kettle) o).setCurrentPowerLevelJava4(powerLevel);
			return null;
		});
	}

	@Override
	public void setModeJava4(int mode) throws Exception {
		this.getOwner().handleRequest(o -> {
			((Kettle) o).setModeJava4(mode);
			return null;
		});
	}
}
// -----------------------------------------------------------------------------
