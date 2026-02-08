package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleImplementationI.KettleMode;
import etape1.equipements.kettle.interfaces.KettleUserCI;
import etape1.equipements.kettle.interfaces.KettleUserI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleUserInboundPort</code> implements an inbound port for
 * the <code>KettleUserCI</code> component interface.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class KettleUserInboundPort extends AbstractInboundPort implements KettleUserCI {

	private static final long serialVersionUID = 1L;

	public KettleUserInboundPort(ComponentI owner) throws Exception {
		super(KettleUserCI.class, owner);
		assert owner instanceof KettleUserI
				: new PreconditionException("owner instanceof KettleUserI");
	}

	public KettleUserInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, KettleUserCI.class, owner);
		assert owner instanceof KettleUserI
				: new PreconditionException("owner instanceof KettleUserI");
	}

	// -------------------------------------------------------------------------
	// KettleImplementationI methods
	// -------------------------------------------------------------------------

	@Override
	public KettleState getState() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).getState());
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).getKettleMode());
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).getTargetTemperature());
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).getCurrentTemperature());
	}

	@Override
	public boolean isHeating() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).isHeating());
	}

	// -------------------------------------------------------------------------
	// KettleExternalControlI methods
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).turnOn(); return null; });
	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).turnOff(); return null; });
	}

	@Override
	public void suspend() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).suspend(); return null; });
	}

	@Override
	public void resume() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).resume(); return null; });
	}

	@Override
	public boolean isSuspended() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).isSuspended());
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).getMaxPowerLevel());
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleUserI) o).getCurrentPowerLevel());
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).setCurrentPowerLevel(powerLevel); return null; });
	}

	// -------------------------------------------------------------------------
	// KettleUserI methods
	// -------------------------------------------------------------------------

	@Override
	public void startHeating() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).startHeating(); return null; });
	}

	@Override
	public void stopHeating() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).stopHeating(); return null; });
	}

	@Override
	public void setTargetTemperature(Measure<Double> temperature) throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).setTargetTemperature(temperature); return null; });
	}

	@Override
	public void setMode(KettleMode mode) throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleUserI) o).setMode(mode); return null; });
	}
}
// -----------------------------------------------------------------------------
