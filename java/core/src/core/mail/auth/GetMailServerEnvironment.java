/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.auth;

import java.io.FileWriter;

import core.util.Environment;
import core.util.JSONSerializer;

public class GetMailServerEnvironment 
{
	public static void main (String[] args) throws Exception
	{
		MailServerAuthenticatorSync auth = new MailServerAuthenticatorSync();
		
		if (args.length != 3)
		{
			System.out.println("Arguments: name password outFile");
			throw new IllegalArgumentException();
		}
		
		Environment e = auth.get(args[0], args[1]);
		
		FileWriter writer = new FileWriter(args[2]);
		writer.write(new String(JSONSerializer.serialize(e)));
		writer.flush();
		writer.close();
	}
}
