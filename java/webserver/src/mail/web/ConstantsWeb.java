/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.web;

import core.constants.ConstantsServer;
import core.util.ExternalResource;

public class ConstantsWeb
{
	public static final String WEB_SERVER_URL;
	
	static 
	{
		String webServerUrl = null;
		try
		{
			if (ConstantsServer.DEBUG)
				webServerUrl = "https://YOUR_DEV_SERVER";
			else
				webServerUrl = "https://" + ExternalResource.getTrimmedString("web-server-url");
		}
		catch (Exception e)
		{
			
		}
		
		WEB_SERVER_URL = webServerUrl;
	}
}
