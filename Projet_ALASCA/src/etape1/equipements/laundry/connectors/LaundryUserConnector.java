package etape1.equipements.laundry.connectors;

import etape1.equipements.laundry.interfaces.LaundryUserCI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.connectors.AbstractConnector;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryUserConnector</code> implements a connector for
 * the user interface of the laundry machine.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This connector simply delegates all method calls to the offering component
 * through the LaundryUserCI interface.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class LaundryUserConnector
extends		AbstractConnector
implements	LaundryUserCI
{
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#isRunning()
	 */
	@Override
	public boolean		isRunning() throws Exception
	{
		return ((LaundryUserCI)this.offering).isRunning();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getState()
	 */
	@Override
	public LaundryState	getState() throws Exception
	{
		return ((LaundryUserCI)this.offering).getState();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getWashMode()
	 */
	@Override
	public LaundryWashMode	getWashMode() throws Exception
	{
		return ((LaundryUserCI)this.offering).getWashMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getWashTemperature()
	 */
	@Override
	public Measure<Double>	getWashTemperature() throws Exception
	{
		return ((LaundryUserCI)this.offering).getWashTemperature();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryImplementationI#getSpinSpeed()
	 */
	@Override
	public SpinSpeed	getSpinSpeed() throws Exception
	{
		return ((LaundryUserCI)this.offering).getSpinSpeed();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#turnOn()
	 */
	@Override
	public void			turnOn() throws Exception
	{
		((LaundryUserCI)this.offering).turnOn();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		((LaundryUserCI)this.offering).turnOff();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#startWash()
	 */
	@Override
	public void			startWash() throws Exception
	{
		((LaundryUserCI)this.offering).startWash();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#cancelWash()
	 */
	@Override
	public void			cancelWash() throws Exception
	{
		((LaundryUserCI)this.offering).cancelWash();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setWhiteMode()
	 */
	@Override
	public void			setWhiteMode() throws Exception
	{
		((LaundryUserCI)this.offering).setWhiteMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setColorMode()
	 */
	@Override
	public void			setColorMode() throws Exception
	{
		((LaundryUserCI)this.offering).setColorMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setDelicateMode()
	 */
	@Override
	public void			setDelicateMode() throws Exception
	{
		((LaundryUserCI)this.offering).setDelicateMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setIntensiveMode()
	 */
	@Override
	public void			setIntensiveMode() throws Exception
	{
		((LaundryUserCI)this.offering).setIntensiveMode();
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setWashTemperature(etape1.equipements.laundry.interfaces.LaundryImplementationI.WashTemperature)
	 */
	@Override
	public void			setWashTemperature(Measure<Double> temp) throws Exception
	{
		((LaundryUserCI)this.offering).setWashTemperature(temp);
	}

	/**
	 * @see etape1.equipements.laundry.interfaces.LaundryUserI#setSpinSpeed(etape1.equipements.laundry.interfaces.LaundryImplementationI.SpinSpeed)
	 */
	@Override
	public void			setSpinSpeed(SpinSpeed speed) throws Exception
	{
		((LaundryUserCI)this.offering).setSpinSpeed(speed);
	}
}
// -----------------------------------------------------------------------------
