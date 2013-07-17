/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.auth;

import java.io.FileWriter;

import core.util.Environment;
import core.util.JSONSerializer;

public class GetKeyServerEnvironment 
{
	public static void main (String[] args) throws Exception
	{
		KeyServerAuthenticatorSync auth = new KeyServerAuthenticatorSync();
		
		if (args.length != 3)
		{
			System.out.println("Arguments: name password outFile");
			throw new IllegalArgumentException();
		}
		
		Environment e = auth.get(args[0], args[1], null);
		
		FileWriter writer = new FileWriter(args[2]);
		writer.write(new String(JSONSerializer.serialize(e)));
		writer.flush();
		writer.close();
	}
}
