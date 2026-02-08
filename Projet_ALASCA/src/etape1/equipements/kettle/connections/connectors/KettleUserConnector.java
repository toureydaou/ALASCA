package etape1.equipements.kettle.connections.connectors;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleUserCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.connectors.AbstractConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleUserConnector</code> implements a connector for the
 * <code>KettleUserCI</code> component interface.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class KettleUserConnector extends AbstractConnector implements KettleUserCI {

	@Override
	public KettleState getState() throws Exception {
		return ((KettleUserCI) this.offering).getState();
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return ((KettleUserCI) this.offering).getKettleMode();
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		return ((KettleUserCI) this.offering).getTargetTemperature();
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return ((KettleUserCI) this.offering).getCurrentTemperature();
	}

	@Override
	public boolean isHeating() throws Exception {
		return ((KettleUserCI) this.offering).isHeating();
	}

	@Override
	public void turnOn() throws Exception {
		((KettleUserCI) this.offering).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((KettleUserCI) this.offering).turnOff();
	}

	@Override
	public void suspend() throws Exception {
		((KettleUserCI) this.offering).suspend();
	}

	@Override
	public void resume() throws Exception {
		((KettleUserCI) this.offering).resume();
	}

	@Override
	public boolean isSuspended() throws Exception {
		return ((KettleUserCI) this.offering).isSuspended();
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return ((KettleUserCI) this.offering).getMaxPowerLevel();
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return ((KettleUserCI) this.offering).getCurrentPowerLevel();
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		((KettleUserCI) this.offering).setCurrentPowerLevel(powerLevel);
	}

	@Override
	public void startHeating() throws Exception {
		((KettleUserCI) this.offering).startHeating();
	}

	@Override
	public void stopHeating() throws Exception {
		((KettleUserCI) this.offering).stopHeating();
	}

	@Override
	public void setTargetTemperature(Measure<Double> temperature) throws Exception {
		((KettleUserCI) this.offering).setTargetTemperature(temperature);
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		((KettleUserCI) this.offering).setMode(mode);
	}
}
// -----------------------------------------------------------------------------
