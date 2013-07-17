/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.auth;

import core.callback.Callback;
import core.util.Environment;


public class MailServerAuthenticatorSync 
{
	MailServerAuthenticator authenticator;
	Object[] result;
	
	private class ResultSetter extends Callback
	{
		public void invoke (Object... args)
		{
			result = args;
		}
	}
	
	public MailServerAuthenticatorSync ()
	{
		authenticator = new MailServerAuthenticator();
	}
	
	public void create (String user, String password, String token) throws Exception
	{
		Thread thread = authenticator.create(user, password, token, new ResultSetter());
		thread.join();
		
		if (result[0] instanceof Exception)
			throw (Exception)result[0];
	}
	
	public void test (String user) throws Exception
	{
		Thread thread = authenticator.testCreate(user, new ResultSetter());
		thread.join();
		
		if (result[0] instanceof Exception)
			throw (Exception)result[0];
	}

	public Environment get (String user, String password) throws Exception
	{
		Thread thread = authenticator.get(user, password, new ResultSetter());
		thread.join();
		
		if (result[0] instanceof Exception)
			throw (Exception)result[0];
		
		return (Environment)result[0];
	}
	
	public Environment put (String user, String password, Environment environment) throws Exception
	{
		Thread thread = authenticator.put(user, password, environment, new ResultSetter());
		thread.join();
		
		if (result[0] instanceof Exception)
			throw (Exception)result[0];
		
		return (Environment)result[0];
	}

	public void delete(String user, String password) throws Exception
	{
		Thread thread = authenticator.delete(user, password, new ResultSetter());
		thread.join();
		
		if (result[0] instanceof Exception)
			throw (Exception)result[0];
	}
}
