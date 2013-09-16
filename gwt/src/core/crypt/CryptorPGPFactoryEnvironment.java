package core.crypt;

import core.constants.ConstantsEnvironmentKeys;
import core.exceptions.CryptoException;
import core.util.Base64;
import core.util.Environment;

public class CryptorPGPFactoryEnvironment {

	public static CryptorPGP create(Environment environment) throws CryptoException 
	{
		return createJS(environment);
	}

    public static CryptorPGP createJS (Environment env) throws CryptoException
    {
        try
        {
            String publicKeyString = env.get(ConstantsEnvironmentKeys.PGP_PUBLIC_KEY);
            byte[] publicKeyBytes = null;
            if (publicKeyString != null)
                publicKeyBytes = Base64.decode(publicKeyString);

            String privateKeyString = env.get(ConstantsEnvironmentKeys.PGP_PRIVATE_KEY);
            byte[] privateKeyBytes = null;
            if (privateKeyString != null)
                    privateKeyBytes = Base64.decode(privateKeyString);
    
        	return new CryptorPGPJS (
        		publicKeyBytes,
            	privateKeyBytes
            );
	    }
        catch (Exception e)
        {
            throw new CryptoException(e);
        }
    }
}
