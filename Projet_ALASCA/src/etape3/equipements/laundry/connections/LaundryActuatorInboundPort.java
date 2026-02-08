package etape3.equipements.laundry.connections;

import etape1.equipements.laundry.interfaces.LaundryImplementationI;
import etape1.equipements.laundry.interfaces.LaundryImplementationI.LaundryWashMode;
import etape3.equipements.laundry.LaundryActuatorCI;
import etape3.equipements.laundry.LaundryInternalControlI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>LaundryActuatorInboundPort</code> implements the inbound port
 * for the {@code LaundryActuatorCI} component interface.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * This port receives actuator commands from a controller and forwards them
 * to the laundry component that owns this port.
 * </p>
 *
 * <p><strong>Invariants</strong></p>
 *
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 */
public class LaundryActuatorInboundPort
extends		AbstractInboundPort
implements	LaundryActuatorCI
{
	private static final long serialVersionUID = 1L;

	/**
	 * Create the inbound port.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code owner instanceof LaundryInternalControlI}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public LaundryActuatorInboundPort(ComponentI owner)
	throws Exception
	{
		super(LaundryActuatorCI.class, owner);
		assert owner instanceof LaundryInternalControlI :
			new PreconditionException(
					"owner instanceof LaundryInternalControlI");
	}

	/**
	 * Create the inbound port with a given URI.
	 *
	 * <p><strong>Contract</strong></p>
	 *
	 * <pre>
	 * pre	{@code owner instanceof LaundryInternalControlI}
	 * post	{@code true}
	 * </pre>
	 *
	 * @param uri			unique identifier of the port.
	 * @param owner			component that owns this port.
	 * @throws Exception 	<i>to do</i>.
	 */
	public LaundryActuatorInboundPort(
		String uri,
		ComponentI owner
	) throws Exception
	{
		super(uri, LaundryActuatorCI.class, owner);
		assert owner instanceof LaundryInternalControlI :
			new PreconditionException(
					"owner instanceof LaundryInternalControlI");
	}

	/**
	 * @see etape3.equipements.laundry.LaundryActuatorCI#startWash()
	 */
	@Override
	public void startWash() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {
					((LaundryInternalControlI)o).startWash();
					return null;
				});
	}

	/**
	 * @see etape3.equipements.laundry.LaundryActuatorCI#stopWash()
	 */
	@Override
	public void stopWash() throws Exception
	{
		this.getOwner().handleRequest(
				o -> {
					((LaundryInternalControlI)o).stopWash();
					return null;
				});
	}

	/**
	 * @see etape3.equipements.laundry.LaundryActuatorCI#setWashMode(LaundryWashMode)
	 */
	@Override
	public void setWashMode(LaundryWashMode mode) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {
					((LaundryInternalControlI)o).setWashMode(mode);
					return null;
				});
	}

	/**
	 * @see etape3.equipements.laundry.LaundryActuatorCI#setWashTemperature(double)
	 */
	@Override
	public void setWashTemperature(double temperature) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {
					((LaundryInternalControlI)o).setWashTemperature(temperature);
					return null;
				});
	}

	/**
	 * @see etape3.equipements.laundry.LaundryActuatorCI#setSpinSpeed(int)
	 */
	@Override
	public void setSpinSpeed(int spinSpeed) throws Exception
	{
		this.getOwner().handleRequest(
				o -> {
					// Convert int to SpinSpeed enum
					LaundryImplementationI.SpinSpeed speed =
						convertToSpinSpeed(spinSpeed);
					((LaundryInternalControlI)o).setSpinSpeed(speed);
					return null;
				});
	}

	/**
	 * Convert an integer RPM value to the SpinSpeed enum.
	 *
	 * @param rpm	the RPM value.
	 * @return		the corresponding SpinSpeed enum value.
	 */
	private static LaundryImplementationI.SpinSpeed convertToSpinSpeed(int rpm)
	{
		if (rpm <= 400) return LaundryImplementationI.SpinSpeed.RPM_400;
		if (rpm <= 600) return LaundryImplementationI.SpinSpeed.RPM_600;
		if (rpm <= 800) return LaundryImplementationI.SpinSpeed.RPM_800;
		if (rpm <= 1000) return LaundryImplementationI.SpinSpeed.RPM_1000;
		if (rpm <= 1200) return LaundryImplementationI.SpinSpeed.RPM_1200;
		return LaundryImplementationI.SpinSpeed.RPM_1400;
	}
}
// -----------------------------------------------------------------------------
