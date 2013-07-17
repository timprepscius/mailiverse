/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.auth;

import java.io.FileInputStream;

import core.util.Environment;
import core.util.JSONSerializer;
import core.util.Streams;

public class PutMailServerEnvironment 
{
	public static void main (String[] args) throws Exception
	{
		MailServerAuthenticatorSync auth = new MailServerAuthenticatorSync();
		
		if (args.length != 3)
		{
			System.out.println("Arguments: name password outFile");
			throw new IllegalArgumentException();
		}

		FileInputStream reader = new FileInputStream(args[2]);
		Environment e = JSONSerializer.deserialize(Streams.readFullyString(reader, "UTF-8").getBytes("UTF-8"));
		auth.put(args[0], args[1], e);
	}
}
