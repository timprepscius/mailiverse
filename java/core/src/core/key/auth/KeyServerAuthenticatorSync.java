/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.auth;

import core.callback.Callback;
import core.srp.client.SRPClientListener;
import core.util.Environment;


public class KeyServerAuthenticatorSync 
{
	KeyServerAuthenticator authenticator;
	Object[] result;
	
	private class ResultSetter extends Callback
	{
		public void invoke (Object... args)
		{
			result = args;
		}
	}
	
	public KeyServerAuthenticatorSync (String server, int port)
	{
		authenticator = new KeyServerAuthenticator(server, port);
	}

	public KeyServerAuthenticatorSync ()
	{
		authenticator = new KeyServerAuthenticator();
	}
	
	public Environment get (String user, String password, SRPClientListener listener) throws Exception
	{
		result = null;
		Thread thread = authenticator.get(user, password, new ResultSetter(), listener);
		thread.join();
		
		if (result[0] instanceof Exception)
			throw (Exception)result[0];
		
		return (Environment)result[0];
	}
	
	public Environment put (String user, String password, Environment environment, SRPClientListener listener) throws Exception
	{
		result = null;
		Thread thread = authenticator.put(user, password, environment, new ResultSetter(), listener);
		thread.join();
		
		if (result[0] instanceof Exception)
			throw (Exception)result[0];
		
		return (Environment)result[0];
	}
}
