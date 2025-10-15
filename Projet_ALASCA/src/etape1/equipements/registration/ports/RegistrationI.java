package etape1.equipements.registration.ports;

public interface RegistrationI {

	public boolean registered(String uid) throws Exception;

	public boolean register(String uid, String controlPortURI, String xmlControlAdapter) throws Exception;

	public void unregister(String uid) throws Exception;

}
