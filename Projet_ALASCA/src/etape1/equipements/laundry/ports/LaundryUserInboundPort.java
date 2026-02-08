package etape1.equipements.laundry.ports;

import etape1.equipements.laundry.interfaces.LaundryUserCI;
import etape1.equipements.laundry.interfaces.LaundryUserI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryUserInboundPort</code> implements an inbound port
 * for the user interface of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This inbound port exposes the user-level services of the laundry machine.
 * It receives calls from remote components and delegates them to the owner
 * component that implements the LaundryUserCI interface.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class LaundryUserInboundPort
extends		AbstractInboundPort
implements	LaundryUserCI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an inbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code owner instanceof LaundryUserCI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				LaundryUserInboundPort(ComponentI owner)
	throws Exception
	{
		super(LaundryUserCI.class, owner);
		assert	owner instanceof LaundryUserI :
				new AssertionError("Precondition violation: "
						+ "owner instanceof LaundryUserCI");
	}

	/**
	 * create an inbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code owner instanceof LaundryUserCI}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				LaundryUserInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, LaundryUserCI.class, owner);
		assert	owner instanceof LaundryUserI :
				new AssertionError("Precondition violation: "
						+ "owner instanceof LaundryUserCI");
	}

	// -------------------------------------------------------------------------
	// Methods from LaundryImplementationI
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#isRunning()
	 */
	@Override
	public boolean		isRunning() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((LaundryUserI)o).isRunning());
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getState()
	 */
	@Override
	public LaundryState	getState() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((LaundryUserI)o).getState());
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getWashMode()
	 */
	@Override
	public LaundryWashMode	getWashMode() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((LaundryUserI)o).getWashMode());
	}


	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getSpinSpeed()
	 */
	@Override
	public SpinSpeed	getSpinSpeed() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((LaundryUserI)o).getSpinSpeed());
	}

	// -------------------------------------------------------------------------
	// Methods from LaundryUserI
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).turnOn();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).turnOff();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#startWash()
	 */
	@Override
	public void			startWash() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).startWash();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#cancelWash()
	 */
	@Override
	public void			cancelWash() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).cancelWash();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setWhiteMode()
	 */
	@Override
	public void			setWhiteMode() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).setWhiteMode();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setColorMode()
	 */
	@Override
	public void			setColorMode() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).setColorMode();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setDelicateMode()
	 */
	@Override
	public void			setDelicateMode() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).setDelicateMode();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setIntensiveMode()
	 */
	@Override
	public void			setIntensiveMode() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).setIntensiveMode();
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setWashTemperature(etape1.equipements.laundry.interfaces.LaundryImplementationI.WashTemperature)
	 */
	@Override
	public void			setWashTemperature(Measure<Double>  temp) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).setWashTemperature(temp);
						return null;
					 });
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setSpinSpeed(etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed)
	 */
	@Override
	public void			setSpinSpeed(SpinSpeed speed) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).setSpinSpeed(speed);
						return null;
					 });
	}

	@Override
	public Measure<Double> getWashTemperature() throws Exception {

		return this.getOwner().handleRequest(
				o -> ((LaundryUserI)o).getWashTemperature());
	}

	@Override
	public boolean		isDelayedStartSet() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((LaundryUserI)o).isDelayedStartSet());
	}

	@Override
	public long			getDelayedStartTime() throws Exception
	{
		return this.getOwner().handleRequest(
				o -> ((LaundryUserI)o).getDelayedStartTime());
	}

	@Override
	public void			setDelayedStart(long delayInSeconds) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).setDelayedStart(delayInSeconds);
						return null;
					 });
	}

	@Override
	public void			cancelDelayedStart() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {	((LaundryUserI)o).cancelDelayedStart();
						return null;
					 });
	}
}
// -----------------------------------------------------------------------------
