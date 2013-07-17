/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.auth;

import core.constants.ConstantsEnvironmentKeys;
import core.crypt.CryptorRSA;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactory;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;
import core.util.Strings;

public class KeyServerAuthTest 
{
	public static void main (String[] args) throws Exception
	{
		KeyServerAuthenticatorSync key = new KeyServerAuthenticatorSync();
		
		Environment e = key.get("USERXXXX@mailiverse.com", "PASSWORD", null);
		for (String k : e.keySet())
		{
			System.out.format("%s -> %s\n", k, e.get(k));
		}
		
		Environment client = e.childEnvironment(ConstantsEnvironmentKeys.CLIENT_ENVIRONMENT);
		CryptorRSA cryptorRSA = CryptorRSAFactoryEnvironment.create (client);
		CryptorRSAAES cryptorRSAAES = new CryptorRSAAES(cryptorRSA);
		
		byte[] bytes = cryptorRSAAES.encrypt(Strings.toBytes("This message was encrypted and decrypted?"));
		System.out.println(Strings.toString(cryptorRSAAES.decrypt(bytes)));
	}
}
