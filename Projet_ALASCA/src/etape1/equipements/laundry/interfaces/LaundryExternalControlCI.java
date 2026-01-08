package etape1.equipements.laundry.interfaces;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component interface <code>LaundryExternalControlCI</code> defines the
 * external control services as a BCM4Java component interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This interface is used to declare ports (offered and required) for the
 * HEM services of the laundry machine.
 * </p>
 *
 * <p>Created on : 2026-01-08</p>
 *
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface LaundryExternalControlCI
extends LaundryExternalControlI, OfferedCI, RequiredCI {

	@Override
	public void			turnOn() throws Exception;

	@Override
	public void			turnOff() throws Exception;

	@Override
	public void			suspend() throws Exception;

	@Override
	public void			resume() throws Exception;
	
	@Override
	public void setMode(int mode) throws Exception;

	@Override
	public Measure<Double>		getMaxPowerLevel() throws Exception;

	@Override
	public  Measure<Double>			getCurrentPowerLevel() throws Exception;
}
// -----------------------------------------------------------------------------
