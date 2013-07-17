package core.constants;

public class ConstantsServer 
{
	public static final boolean DEBUG = System.getenv("PRODUCTION")==null;
	
	public static final String LOCAL_MAIL_SERVER, DBCONNECTION_PREFIX;
	public static final String KEY_SERVER;
	
	static 
	{
		if (DEBUG)
		{
			System.out.println("Running DEBUG Mode");
			
			LOCAL_MAIL_SERVER = "red";
			DBCONNECTION_PREFIX = "jdbc:mysql://red/";
			KEY_SERVER = "red";
		}
		else
		{
			System.out.println("Running PRODUCTION Mode");
			
			KEY_SERVER = "localhost";
			
			// the mail server has to be the full name, or else the SSL certificate fails
			LOCAL_MAIL_SERVER = "mail.mailiverse.com";
			DBCONNECTION_PREFIX =  "jdbc:mysql://localhost/";
		}
	}
	
	public static final String SMTP_HOST = LOCAL_MAIL_SERVER;
	public static final int SMTP_PORT = 25;
	
	public static final String LOCAL_SMTP_HOST = "YOUR_LOCAL_SMTP_HOST";
	public static final String LOCAL_SMTP_PORT = "10025";

	public static final String KEY_AUTH_HOST = KEY_SERVER;
	public static final int KEY_AUTH_PORT = 7000;
	
	public static final String MAIL_AUTH_HOST = KEY_SERVER;
	public static final int MAIL_AUTH_PORT = 7001;
	public static final int MAXIMUM_MAIL_SIZE = 1024 * 1024 * 1;

	public static final int AUTH_TIMEOUT = 45;
}
