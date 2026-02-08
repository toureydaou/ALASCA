package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleExternalControlOutboundPort</code> implements an
 * outbound port for the <code>KettleExternalControlCI</code> component interface.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class KettleExternalControlOutboundPort extends AbstractOutboundPort
		implements KettleExternalControlCI {

	private static final long serialVersionUID = 1L;

	public KettleExternalControlOutboundPort(ComponentI owner) throws Exception {
		super(KettleExternalControlCI.class, owner);
	}

	public KettleExternalControlOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, KettleExternalControlCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// KettleImplementationI methods
	// -------------------------------------------------------------------------

	@Override
	public KettleState getState() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).getState();
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).getKettleMode();
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).getTargetTemperature();
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).getCurrentTemperature();
	}

	@Override
	public boolean isHeating() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).isHeating();
	}

	// -------------------------------------------------------------------------
	// KettleExternalControlI methods
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		((KettleExternalControlCI) this.getConnector()).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((KettleExternalControlCI) this.getConnector()).turnOff();
	}

	@Override
	public void suspend() throws Exception {
		((KettleExternalControlCI) this.getConnector()).suspend();
	}

	@Override
	public void resume() throws Exception {
		((KettleExternalControlCI) this.getConnector()).resume();
	}

	@Override
	public boolean isSuspended() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).isSuspended();
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).getMaxPowerLevel();
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return ((KettleExternalControlCI) this.getConnector()).getCurrentPowerLevel();
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		((KettleExternalControlCI) this.getConnector()).setCurrentPowerLevel(powerLevel);
	}
}
// -----------------------------------------------------------------------------
