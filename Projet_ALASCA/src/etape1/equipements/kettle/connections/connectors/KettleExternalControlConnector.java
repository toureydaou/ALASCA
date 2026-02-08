package etape1.equipements.kettle.connections.connectors;

import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.connectors.AbstractConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleExternalControlConnector</code> implements a
 * connector for the {@code KettleExternalControlCI} component interface.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class KettleExternalControlConnector extends AbstractConnector
		implements KettleExternalControlCI {

	@Override
	public KettleState getState() throws Exception {
		return ((KettleExternalControlCI) this.offering).getState();
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return ((KettleExternalControlCI) this.offering).getKettleMode();
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		return ((KettleExternalControlCI) this.offering).getTargetTemperature();
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return ((KettleExternalControlCI) this.offering).getCurrentTemperature();
	}

	@Override
	public boolean isHeating() throws Exception {
		return ((KettleExternalControlCI) this.offering).isHeating();
	}

	@Override
	public void turnOn() throws Exception {
		((KettleExternalControlCI) this.offering).turnOn();
	}

	@Override
	public void turnOff() throws Exception {
		((KettleExternalControlCI) this.offering).turnOff();
	}

	@Override
	public void suspend() throws Exception {
		((KettleExternalControlCI) this.offering).suspend();
	}

	@Override
	public void resume() throws Exception {
		((KettleExternalControlCI) this.offering).resume();
	}

	@Override
	public boolean isSuspended() throws Exception {
		return ((KettleExternalControlCI) this.offering).isSuspended();
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return ((KettleExternalControlCI) this.offering).getMaxPowerLevel();
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return ((KettleExternalControlCI) this.offering).getCurrentPowerLevel();
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		((KettleExternalControlCI) this.offering).setCurrentPowerLevel(powerLevel);
	}
}
// -----------------------------------------------------------------------------
