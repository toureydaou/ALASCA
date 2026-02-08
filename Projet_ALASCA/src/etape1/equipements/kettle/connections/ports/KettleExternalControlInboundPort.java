package etape1.equipements.kettle.connections.ports;

import etape1.equipements.kettle.interfaces.KettleExternalControlCI;
import etape1.equipements.kettle.interfaces.KettleExternalControlI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>KettleExternalControlInboundPort</code> implements an
 * inbound port for the <code>KettleExternalControlCI</code> component interface.
 *
 * <p>Created on : 2023-09-19</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class KettleExternalControlInboundPort extends AbstractInboundPort
		implements KettleExternalControlCI {

	private static final long serialVersionUID = 1L;

	public KettleExternalControlInboundPort(ComponentI owner) throws Exception {
		this(KettleExternalControlCI.class, owner);
	}

	public KettleExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		this(uri, KettleExternalControlCI.class, owner);
	}

	public KettleExternalControlInboundPort(
			Class<? extends OfferedCI> implementedInterface, ComponentI owner) throws Exception {
		super(implementedInterface, owner);
		assert implementedInterface != null
				&& KettleExternalControlCI.class.isAssignableFrom(implementedInterface);
		assert owner instanceof KettleExternalControlI
				: new PreconditionException("owner instanceof KettleExternalControlI");
	}

	public KettleExternalControlInboundPort(
			String uri, Class<? extends OfferedCI> implementedInterface, ComponentI owner) throws Exception {
		super(uri, implementedInterface, owner);
		assert implementedInterface != null
				&& KettleExternalControlCI.class.isAssignableFrom(implementedInterface);
		assert owner instanceof KettleExternalControlI
				: new PreconditionException("owner instanceof KettleExternalControlI");
	}

	// -------------------------------------------------------------------------
	// KettleImplementationI methods
	// -------------------------------------------------------------------------

	@Override
	public KettleState getState() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getState());
	}

	@Override
	public KettleMode getKettleMode() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getKettleMode());
	}

	@Override
	public Measure<Double> getTargetTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getTargetTemperature());
	}

	@Override
	public Measure<Double> getCurrentTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getCurrentTemperature());
	}

	@Override
	public boolean isHeating() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).isHeating());
	}

	// -------------------------------------------------------------------------
	// KettleExternalControlI methods
	// -------------------------------------------------------------------------

	@Override
	public void turnOn() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleExternalControlI) o).turnOn(); return null; });
	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleExternalControlI) o).turnOff(); return null; });
	}

	@Override
	public void suspend() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleExternalControlI) o).suspend(); return null; });
	}

	@Override
	public void resume() throws Exception {
		this.getOwner().handleRequest(o -> { ((KettleExternalControlI) o).resume(); return null; });
	}

	@Override
	public boolean isSuspended() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).isSuspended());
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getMaxPowerLevel());
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> ((KettleExternalControlI) o).getCurrentPowerLevel());
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		this.getOwner().handleRequest(o -> {
			((KettleExternalControlI) o).setCurrentPowerLevel(powerLevel);
			return null;
		});
	}
}
// -----------------------------------------------------------------------------
