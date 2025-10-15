package etape1.equipements.kettle.interfaces;



public interface KettleExternalControlI extends KettleImplementationI  {
	
	
	public void turnOn() throws Exception;
	
	public void turnOff() throws Exception;
	
}
