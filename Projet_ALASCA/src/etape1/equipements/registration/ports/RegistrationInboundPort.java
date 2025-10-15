package etape1.equipements.registration.ports;

import etape1.bases.RegistrationCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

public class RegistrationInboundPort extends AbstractInboundPort implements RegistrationCI {

	private static final long serialVersionUID = 1L;

	public RegistrationInboundPort(ComponentI owner) throws Exception {
		super(RegistrationInboundPort.class, owner);
		//assert owner instanceof RegistrationI : new PreconditionException("owner instanceof RegistrationI");
	}

	/**
	 * create an inbound port instance.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * uri != null && !uri.isEmpty()
	 * }
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof CoffeeMachineImplementationI
	 * }
	 * post	{@code
	 * true
	 * }	// no more postcondition.
	 * </pre>
	 *
	 * @param uri   URI of the port.
	 * @param owner component owning the port.
	 * @throws Exception <i>to do</i>.
	 */
	public RegistrationInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RegistrationCI.class, owner);
		//assert owner instanceof RegistrationI : new PreconditionException("owner instanceof RegistrationI");
	}

	@Override
	public boolean registered(String uid) throws Exception {
		return this.getOwner().handleRequest(o -> ((RegistrationI) o).registered(uid));
	}

	@Override
	public boolean register(String uid, String controlPortURI, String xmlControlAdapter) throws Exception {
		return this.getOwner().handleRequest(o -> ((RegistrationI) o).register(uid, controlPortURI, xmlControlAdapter));
	}

	@Override
	public void unregister(String uid) throws Exception {
		this.getOwner().handleRequest(o -> {
			((RegistrationI) o).unregister(uid);
			return null;
		});

	}

}
