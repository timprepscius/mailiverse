/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;

import java.io.IOException;
import java.io.InputStream;

import core.util.Environment;


public class UserInformation
{
	public Environment environment;
	public MailetHandler handler;
	public String toAddress;
	
	public UserInformation (String toAddress, Environment environment, MailetHandler handler)
	{
		this.environment = environment;
		this.handler = handler;
		this.toAddress = toAddress;
	}
	
	public void handleIn (InputStream m) throws Exception
	{
		handler.handleIn(toAddress, environment, m);
	}

	public void handleOut (InputStream m) throws Exception
	{
		handler.handleOut(toAddress, environment, m);
	}
}
