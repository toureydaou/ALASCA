package etape1.equipements.laundry.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>LaundryUserCI</code> defines the user-level
 * services as a BCM4Java component interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface is used to declare ports (offered and required) for the
 * user services of the laundry machine.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface LaundryUserCI extends LaundryUserI, OfferedCI, RequiredCI {

	@Override
	public void			turnOn() throws Exception;

	@Override
	public void			turnOff() throws Exception;

	@Override
	public void			startWash() throws Exception;

	@Override
	public void			cancelWash() throws Exception;

	@Override
	public void			setWhiteMode() throws Exception;

	@Override
	public void			setColorMode() throws Exception;

	@Override
	public void			setDelicateMode() throws Exception;

	@Override
	public void			setIntensiveMode() throws Exception;

	@Override
	public void			setWashTemperature(Measure<Double>  temp) throws Exception;

	@Override
	public void			setSpinSpeed(SpinSpeed speed) throws Exception;

	@Override
	public void			setDelayedStart(long delayInSeconds) throws Exception;

	@Override
	public void			cancelDelayedStart() throws Exception;
}
// -----------------------------------------------------------------------------
