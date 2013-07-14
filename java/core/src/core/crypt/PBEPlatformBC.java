/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import org.bc.crypto.PBEParametersGenerator;
import org.bc.crypto.digests.SHA256Digest;
import org.bc.crypto.generators.PKCS5S2ParametersGenerator;
import org.bc.crypto.params.KeyParameter;

import core.exceptions.CryptoException;

public class PBEPlatformBC 
{
    public static byte[] generate (String password, byte[] salt, int iterationCount, int keyLength)
        	throws CryptoException 
	{
    	PBEParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
    	generator.init(
    		PBEParametersGenerator.PKCS5PasswordToBytes(password.toCharArray()),
            salt,
            iterationCount
        );
    	
    	return ((KeyParameter)generator.generateDerivedParameters(keyLength)).getKey();
	}
}
