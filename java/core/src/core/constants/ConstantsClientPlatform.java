package core.constants;

public class ConstantsClientPlatform
{
	public static final boolean DEBUG = false;
	
	public static final String HOST, AUTH_HOST, TOMCAT_HOST, WEB_HOST;
	
	static 
	{
		if (DEBUG)
		{
			HOST = "mailiverse.com";
			AUTH_HOST = "red";
			TOMCAT_HOST = "YOUR_DEV_TOMCAT:8080";
			WEB_HOST = "YOUR_DEV_WEB:8000";
		}
		else
		{
			HOST = "mailiverse.com";
			AUTH_HOST = "mail.mailiverse.com";
			TOMCAT_HOST = "mail.mailiverse.com";
			WEB_HOST = "www.mailiverse.com";
		}
	}
}
