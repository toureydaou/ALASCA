package etape1.equipements.laundry.ports;

import etape1.equipements.laundry.interfaces.LaundryUserCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryUserOutboundPort</code> implements an outbound port
 * for the user interface of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This outbound port is used by components that need to call the user-level
 * services of the laundry machine. It delegates all calls through the
 * connector to the remote component.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class LaundryUserOutboundPort
extends		AbstractOutboundPort
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
	 * create an outbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				LaundryUserOutboundPort(ComponentI owner)
	throws Exception
	{
		super(LaundryUserCI.class, owner);
	}

	/**
	 * create an outbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception	<i>to do</i>.
	 */
	public				LaundryUserOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, LaundryUserCI.class, owner);
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
		return ((LaundryUserCI)this.getConnector()).isRunning();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getState()
	 */
	@Override
	public LaundryState	getState() throws Exception
	{
		return ((LaundryUserCI)this.getConnector()).getState();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getWashMode()
	 */
	@Override
	public LaundryWashMode	getWashMode() throws Exception
	{
		return ((LaundryUserCI)this.getConnector()).getWashMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getWashTemperature()
	 */
	@Override
	public Measure<Double>	getWashTemperature() throws Exception
	{
		return ((LaundryUserCI)this.getConnector()).getWashTemperature();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getSpinSpeed()
	 */
	@Override
	public SpinSpeed	getSpinSpeed() throws Exception
	{
		return ((LaundryUserCI)this.getConnector()).getSpinSpeed();
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
		((LaundryUserCI)this.getConnector()).turnOn();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		((LaundryUserCI)this.getConnector()).turnOff();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#startWash()
	 */
	@Override
	public void			startWash() throws Exception
	{
		((LaundryUserCI)this.getConnector()).startWash();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#cancelWash()
	 */
	@Override
	public void			cancelWash() throws Exception
	{
		((LaundryUserCI)this.getConnector()).cancelWash();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setWhiteMode()
	 */
	@Override
	public void			setWhiteMode() throws Exception
	{
		((LaundryUserCI)this.getConnector()).setWhiteMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setColorMode()
	 */
	@Override
	public void			setColorMode() throws Exception
	{
		((LaundryUserCI)this.getConnector()).setColorMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setDelicateMode()
	 */
	@Override
	public void			setDelicateMode() throws Exception
	{
		((LaundryUserCI)this.getConnector()).setDelicateMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setIntensiveMode()
	 */
	@Override
	public void			setIntensiveMode() throws Exception
	{
		((LaundryUserCI)this.getConnector()).setIntensiveMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setWashTemperature(etape1.equipements.laundry.interfaces.LaundryImplementationI.WashTemperature)
	 */
	@Override
	public void			setWashTemperature(Measure<Double>  temp) throws Exception
	{
		((LaundryUserCI)this.getConnector()).setWashTemperature(temp);
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setSpinSpeed(etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed)
	 */
	@Override
	public void			setSpinSpeed(SpinSpeed speed) throws Exception
	{
		((LaundryUserCI)this.getConnector()).setSpinSpeed(speed);
	}
}
// -----------------------------------------------------------------------------
