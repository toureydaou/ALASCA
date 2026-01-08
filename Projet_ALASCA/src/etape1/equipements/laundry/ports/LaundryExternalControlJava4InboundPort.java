package etape1.equipements.laundry.ports;

import etape1.equipements.laundry.interfaces.LaundryExternalControlJava4CI;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.ComponentI;


public class LaundryExternalControlJava4InboundPort extends LaundryExternalControlInboundPort
		implements LaundryExternalControlJava4CI {

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
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	{@code
	 * owner != null
	 * }
	 * pre	{@code
	 * owner instanceof LaundryUserI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlJava4InboundPort(ComponentI owner) throws Exception {
		super(owner);
	}

	/**
	 * create an inbound port.
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
	 * owner instanceof LaundryUserI
	 * }
	 * post	{@code
	 * true
	 * }	// no postcondition.
	 * </pre>
	 *
	 * @param uri   unique identifier of the port.
	 * @param owner component that owns this port.
	 * @throws Exception <i>to do</i>.
	 */
	public LaundryExternalControlJava4InboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, LaundryExternalControlJava4CI.class, owner);
	}


	@Override
	public void setWashModeJava4(int mode) throws Exception {
		this.setMode(mode);
	}

	@Override
	public int getStateJava4() throws Exception {
		return this.getState().ordinal();
	}

	@Override
	public int getWashModeJava4() throws Exception {
		return this.getState().ordinal();
	}

	@Override
	public double getWashTemperatureJava4() throws Exception {
		return this.getCurrentWashTemperature().getData();
	}

	@Override
	public int getSpinSpeedJava4() throws Exception {
		return this.getSpinSpeed().ordinal();
	}


	@Override
	public double getCurrentPowerJava4() throws Exception {
		return this.getCurrentPowerLevel().getData();
	}


	@Override
	public double getMaxPowerLevelJava4() throws Exception {
		
		return this.getMaxPowerLevel().getData();
	}

	@Override
	public void setCurrentPowerJava4(double power) throws Exception {
		
		 this.setCurrentPowerLevel(new Measure<Double>(power, MeasurementUnit.WATTS));
	}



}
