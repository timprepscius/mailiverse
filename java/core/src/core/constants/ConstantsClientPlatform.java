package core.constants;

import core.util.Hosts;

public class ConstantsClientPlatform
{
	public static final boolean DEBUG = false;
	
	public static final String HOST, AUTH_HOST, TOMCAT_HOST, WEB_HOST;
	
	static 
	{
		if (DEBUG)
		{
			HOST = Hosts.getHostFor("primary_dev");
			AUTH_HOST = Hosts.getHostFor("auth_dev");
			TOMCAT_HOST = Hosts.getHostFor("tomcat_dev");
			WEB_HOST = Hosts.getHostFor("web_dev");
		}
		else
		{
			HOST = Hosts.getHostFor("primary_prod");
			AUTH_HOST = Hosts.getHostFor("auth_prod");
			TOMCAT_HOST = Hosts.getHostFor("tomcat_prod");
			WEB_HOST = Hosts.getHostFor("web_prod");
		}
	}
}
