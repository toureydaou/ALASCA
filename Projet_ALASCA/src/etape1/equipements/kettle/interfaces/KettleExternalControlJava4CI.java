package etape1.equipements.kettle.interfaces;

public interface KettleExternalControlJava4CI extends KettleExternalControlCI {
	
	public KettleState	getStateJava4() throws Exception;

	public KettleMode	getKettleModeJava4() throws Exception;
	
	public void turnOnJava4() throws Exception;

	public void turnOffJava4() throws Exception;
}
