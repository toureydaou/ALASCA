package etape1.equipements.laundry.interfaces;

public interface LaundryWashModeI {

	public static enum LaundryWashModes {
		WHITE, COLOR
	}
	
	public LaundryWashModes getLaundryWashMode() throws Exception;
	
	public void setLaundryWashModeWhite() throws Exception;
	
	public void setLaundryWashModeColor() throws Exception;
}
