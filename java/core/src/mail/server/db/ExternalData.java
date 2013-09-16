package mail.server.db;

public interface ExternalData {

	public void addUser (String name, String password) throws Exception;
	public void removeUser (String name) throws Exception;
	
	public void setUserPassword (String name, String password) throws Exception;
}
