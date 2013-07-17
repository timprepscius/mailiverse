/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.app;

import core.constants.ConstantsClient;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsServer;
import core.crypt.CryptorRSA;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.exceptions.CryptoException;
import core.util.Environment;

public class User
{
	public User (Environment environment) throws Exception
	{
		this.environment = environment;
		this.clientEnvironment = environment.childEnvironment(ConstantsEnvironmentKeys.CLIENT_ENVIRONMENT);
		this.name = clientEnvironment.checkGet(ConstantsEnvironmentKeys.CLIENT_NAME);
		this.email = name + ConstantsClient.ATHOST;
		this.password = clientEnvironment.checkGet(ConstantsEnvironmentKeys.CLIENT_PASSWORD);
		this.cryptor = new CryptorRSAAES(CryptorRSAFactoryEnvironment.create(clientEnvironment));
	}
	
	public String name;
	public String email;
	public String password;
	public Environment environment;
	public Environment clientEnvironment;
	public CryptorRSAAES cryptor;

	public boolean alreadyEnsuredDirectories = false;
	
	public void authenticate (String name, String password) throws Exception
	{
		if (!this.name.equals(name) || !this.password.equals(password) )
			throw new Exception ("Authentication failure");
	}
}
