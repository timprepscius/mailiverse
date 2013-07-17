/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.auth;

import java.math.BigInteger;
import java.util.Random;


import core.constants.ConstantsEnvironmentKeys;
import core.util.Environment;


public class MailServerAuthTest 
{
	public static void main (String[] args) throws Exception
	{
		Random random = new Random();
		MailServerAuthenticatorSync auth = new MailServerAuthenticatorSync();
		
		String user = "test-" + BigInteger.valueOf(Math.abs(random.nextLong())).toString(32);
		user = user + "@mailiverse.com";
		
		String password = "password-" + BigInteger.valueOf(Math.abs(random.nextLong())).toString(32);
		String token = "3ee1vpbih0cl";

		System.out.println("user " + user);
		
		System.out.println("testCreate");
		auth.test(user);
		
		System.out.println("create");
		auth.create(user, password, token);
		
		Environment e = new Environment();
		e.put(ConstantsEnvironmentKeys.SMTP_PASSWORD, "abcdef");

		System.out.println("put");
		e.put("hi", "there");
		auth.put(user, password, e);
		
		System.out.println("get");
		e = auth.get(user, password);
		for (String k : e.keySet())
		{
			System.out.format("%s -> %s\n", k, e.get(k));
		}
		
		System.out.println("delete");
		auth.delete(user, password);
	}
}
