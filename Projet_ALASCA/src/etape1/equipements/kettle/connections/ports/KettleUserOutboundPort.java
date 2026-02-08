package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleUserCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleUserOutboundPort</code> implements an outbound port for
 * the <code>KettleUserCI</code> component interface.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class KettleUserOutboundPort extends AbstractOutboundPort implements KettleUserCI {

	private static final long serialVersionUID = 1L;

	public KettleUserOutboundPort(ComponentI owner) throws Exception {
		super(KettleUserCI.class, owner);
	}

	public KettleUserOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, KettleUserCI.class, owner);
	}

	// -------------------------------------------------------------------------
	// KettleImplementationI methods
	// -------------------------------------------------------------------------

	@Override
	public KettleState getState() throws Exception {
		return ((KettleUserCI) this.getConnector()).getState();
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return ((KettleUserCI) this.getConnector()).getKettleMode();
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		return ((KettleUserCI) this.getConnector()).getTargetTemperature();
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return ((KettleUserCI) this.getConnector()).getCurrentTemperature();
	}

	@Override
	public boolean isHeating() throws Exception {
		return ((KettleUserCI) this.getConnector()).isHeating();
	}

	// -------------------------------------------------------------------------
	// KettleExternalControlI methods
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		((KettleUserCI) this.getConnector()).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((KettleUserCI) this.getConnector()).turnOff();
	}

	@Override
	public void suspend() throws Exception {
		((KettleUserCI) this.getConnector()).suspend();
	}

	@Override
	public void resume() throws Exception {
		((KettleUserCI) this.getConnector()).resume();
	}

	@Override
	public boolean isSuspended() throws Exception {
		return ((KettleUserCI) this.getConnector()).isSuspended();
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return ((KettleUserCI) this.getConnector()).getMaxPowerLevel();
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return ((KettleUserCI) this.getConnector()).getCurrentPowerLevel();
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		((KettleUserCI) this.getConnector()).setCurrentPowerLevel(powerLevel);
	}

	// -------------------------------------------------------------------------
	// KettleUserI methods
	// -------------------------------------------------------------------------

	@Override
	public void startHeating() throws Exception {
		((KettleUserCI) this.getConnector()).startHeating();
	}

	@Override
	public void stopHeating() throws Exception {
		((KettleUserCI) this.getConnector()).stopHeating();
	}

	@Override
	public void setTargetTemperature(Measure<Double> temperature) throws Exception {
		((KettleUserCI) this.getConnector()).setTargetTemperature(temperature);
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		((KettleUserCI) this.getConnector()).setMode(mode);
	}
}
// -----------------------------------------------------------------------------
