package core.crypt;

import core.constants.ConstantsEnvironmentKeys;
import core.exceptions.CryptoException;
import core.util.Base64;
import core.util.Environment;

public class CryptorRSAFactoryEnvironment {

	public static CryptorRSA create(Environment environment) throws CryptoException 
	{
		return createJS(environment);
	}

    public static CryptorRSA createJS (Environment env) throws CryptoException
    {
        try
        {
            String publicKeyString = env.get(ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY);
            byte[] publicKeyBytes = null;
            if (publicKeyString != null)
                publicKeyBytes = Base64.decode(publicKeyString);

            String privateKeyString = env.get(ConstantsEnvironmentKeys.PRIVATE_DECRYPTION_KEY);
            byte[] privateKeyBytes = null;
            if (privateKeyString != null)
                    privateKeyBytes = Base64.decode(privateKeyString);
    
        	return new CryptorRSAJS (
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
