package core.constants;

import core.util.JSON_;

public class ConstantsClientPlatform 
{
	public static final String HOST, AUTH_HOST, TOMCAT_HOST, WEB_HOST;
	
	public static native String jsResolve () /*-{
		return JSON.stringify($wnd.Constants);
	}-*/;
	
	static 
	{
		String host = null, authHost = null, tomcatHost = null, webServer = null;
		try
		{
			Object json = JSON_.parse(jsResolve());
			host = JSON_.getString(json, "HOST");
			authHost = JSON_.getString(json, "AUTH_HOST");
			tomcatHost = JSON_.getString(json, "TOMCAT_HOST");
			webServer = JSON_.getString(json, "WEB_HOST");
		}
		catch (Exception e)
		{
		}
		
		HOST = host;
		AUTH_HOST = authHost;
		TOMCAT_HOST = tomcatHost;
		WEB_HOST = webServer;
	}
}
