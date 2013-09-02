package core.constants;

import core.util.Hosts;

public class ConstantsServer 
{
	public static final boolean DEBUG = System.getenv("PRODUCTION")==null;
	
	public static final String LOCAL_MAIL_SERVER, DBCONNECTION_PREFIX;
	public static final String KEY_SERVER;
	public static final String LOCAL_SMTP_HOST;
	
	static 
	{
		if (DEBUG)
		{
			System.out.println("Running DEBUG Mode");
			
			LOCAL_MAIL_SERVER = Hosts.getHostFor("local_mail_dev");
			DBCONNECTION_PREFIX = "jdbc:mysql://" + Hosts.getHostFor("local_db_dev") + "/";
			KEY_SERVER = Hosts.getHostFor("local_key_dev");
			LOCAL_SMTP_HOST = Hosts.getHostFor("local_smtp_dev");
		}
		else
		{
			System.out.println("Running PRODUCTION Mode");
			
			KEY_SERVER = Hosts.getHostFor("local_key_prod");
			
			// the mail server has to be the full name, or else the SSL certificate fails
			LOCAL_MAIL_SERVER = Hosts.getHostFor("local_mail_prod");
			DBCONNECTION_PREFIX = "jdbc:mysql://" + Hosts.getHostFor("local_db_prod") + "/";
			LOCAL_SMTP_HOST = Hosts.getHostFor("local_smtp_prod");
		}
	}
	
	public static final String SMTP_HOST = LOCAL_MAIL_SERVER;
	public static final int SMTP_PORT = 25;
	
	public static final String LOCAL_SMTP_PORT = "10025";

	public static final String KEY_AUTH_HOST = KEY_SERVER;
	public static final int KEY_AUTH_PORT = 7000;
	
	public static final String MAIL_AUTH_HOST = KEY_SERVER;
	public static final int MAIL_AUTH_PORT = 7001;
	
	public static final int MAXIMUM_MAIL_SIZE = 1024 * 1024 * 1;

	public static final int AUTH_TIMEOUT = 45;
}
