package etape1.equipements.laundry.ports;

import etape1.equipements.laundry.interfaces.LaundryExternalControlCI;
import etape1.equipements.laundry.interfaces.LaundryExternalControlI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;


public class LaundryExternalControlInboundPort extends AbstractInboundPort
		implements LaundryExternalControlCI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port instance.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
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
	 * @param owner component owning the port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlInboundPort(ComponentI owner) throws Exception {
		super(LaundryExternalControlCI.class, owner);
		assert owner instanceof LaundryExternalControlI
				: new PreconditionException("owner instanceof CoffeeMachineImplementationI");
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
	public LaundryExternalControlInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, LaundryExternalControlCI.class, owner);
		assert owner instanceof LaundryExternalControlI
				: new PreconditionException("owner instanceof CoffeeMachineImplementationI");
	}

	public LaundryExternalControlInboundPort(String uri, Class<? extends OfferedCI> implementedInterface,
			ComponentI owner) throws Exception {
		super(uri, implementedInterface, owner);

		assert implementedInterface != null
				&& LaundryExternalControlCI.class.isAssignableFrom(implementedInterface)
				: new PreconditionException("implementedInterface != null && "
						+ "FanControlCI.class.isAssignableFrom(" + "implementedInterface)");
		
	}

	

	@Override
	public Measure<Double> getCurrentWashTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).getCurrentWashTemperature();
		});
	}

	@Override
	public boolean isRunning() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).isRunning();
		});
	}

	@Override
	public LaundryState getState() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).getState();
		});
	}

	@Override
	public LaundryWashMode getWashMode() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).getWashMode();
		});
	}

	@Override
	public SpinSpeed getSpinSpeed() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).getSpinSpeed();
		});
	}

	@Override
	public Measure<Double> getWashTemperature() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).getWashTemperature();
		});
	}

	@Override
	public void turnOn() throws Exception {
		 this.getOwner().handleRequest(o -> {
			 ((LaundryExternalControlI) o).turnOn();
			 return null;
		});
	}

	@Override
	public void turnOff() throws Exception {
		this.getOwner().handleRequest(o -> {
			 ((LaundryExternalControlI) o).turnOff();
			 return null;
		});
		
	}

	@Override
	public void suspend() throws Exception {
		this.getOwner().handleRequest(o -> {
			 ((LaundryExternalControlI) o).suspend();
			 return null;
		});
		
	}

	@Override
	public void resume() throws Exception {
		this.getOwner().handleRequest(o -> {
			 ((LaundryExternalControlI) o).resume();
			 return null;
		});
		
	}

	@Override
	public Measure<Double> getCurrentPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).getCurrentPowerLevel();
		});
	}

	@Override
	public Measure<Double> getMaxPowerLevel() throws Exception {
		return this.getOwner().handleRequest(o -> {
			return ((LaundryExternalControlI) o).getMaxPowerLevel();
		});
	}

	@Override
	public void setMode(int mode) throws Exception {
		this.getOwner().handleRequest(o -> {
			 ((LaundryExternalControlI) o).setMode(mode);
			 return null;
		});
		
	}

	@Override
	public void setCurrentPowerLevel(Measure<Double> powerLevel) throws Exception {
		this.getOwner().handleRequest(o -> {
			 ((LaundryExternalControlI) o).setCurrentPowerLevel(powerLevel);
			 return null;
		});
		
	}




}
