package etape1.equipements.fan.connections.ports;

import etape1.equipements.fan.interfaces.FanExternalControlCI;
import etape1.equipements.fan.interfaces.FanExternalControlI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.alasca.physical_data.Measure;

public class FanExternalControlInboundPort
extends AbstractInboundPort
implements FanExternalControlCI {

	private static final long serialVersionUID = 1L;

	public FanExternalControlInboundPort(ComponentI owner) throws Exception {
		super(FanExternalControlCI.class, owner);
	}

	public FanExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, FanExternalControlCI.class, owner);
	}

	protected FanExternalControlInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, implementedInterface, owner);
	}

	@Override
	public Measure<Double> getPowerLevel() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((FanExternalControlI) o).getPowerLevel());
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return this.getOwner().handleRequest(
				o -> ((FanExternalControlI) o).getMaxPowerLevel());
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((FanExternalControlI) o).setCurrentPowerLevel(powerLevel);
					return null;
				});
	}

	@Override
	public void setMode(int mode) throws Exception {
		this.getOwner().handleRequest(
				o -> {
					((FanExternalControlI) o).setMode(mode);
					return null;
				});
	}
}
