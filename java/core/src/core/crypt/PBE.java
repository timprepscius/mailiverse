/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import org.bc.crypto.PBEParametersGenerator;
import org.bc.crypto.digests.SHA256Digest;
import org.bc.crypto.generators.PKCS5S2ParametersGenerator;
import org.bc.crypto.params.KeyParameter;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.crypt.Cryptor;
import core.crypt.CryptorAES;
import core.exceptions.CryptoException;
import core.util.Arrays;
import core.util.Base64;
import core.util.LogNull;
import core.util.LogOut;

/**
 * http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
 */
public class PBE extends Cryptor
{
	LogNull log = new LogNull(PBE.class);
	
	public byte[] key = null;
	Cryptor cryptorAES;
	
    public static final int DEFAULT_ITERATIONS = 131072;
    public static final int SHORT_ITERATIONS = 32768;
    public static final int DEFAULT_KEYLENGTH = 256;
    
    /**
     * bytes used to salt the key (set before making an instance)
     */
    public static final byte[] DEFAULT_SALT_0 = {
        (byte)0xc8, (byte)0x73, (byte)0x41, (byte)0x8c,
        (byte)0x7e, (byte)0xd8, (byte)0xee, (byte)0x89
    };
    
    /**
     * bytes used to salt the key (set before making an instance)
     */
    public static final byte[] DEFAULT_SALT_1 = {
        (byte)0x12, (byte)0x53, (byte)0x14, (byte)0xbb,
        (byte)0x7e, (byte)0x97, (byte)0xce, (byte)0x55
    };
    
    /**
     * bytes used to salt the key (set before making an instance)
     */
    public static final byte[] DEFAULT_SALT_2 = {
        (byte)0x0a, (byte)0x48, (byte)0x33, (byte)0xfe,
        (byte)0xa7, (byte)0xc2, (byte)0x2c, (byte)0x71
    };

    public static final byte[][] DEFAULT_SALT = { DEFAULT_SALT_0, DEFAULT_SALT_1, DEFAULT_SALT_2 };

    public PBE ()
    {
    	
    }
    public PBE (String password, byte[] salt, int iterationCount, int keyLength) throws CryptoException
    {
    	generate(password,salt, iterationCount, keyLength);
    }
    
    public void generate (String password, byte[] salt, int iterationCount, int keyLength)
    	throws CryptoException 
	{
    	key = PBEPlatform.generate(password,  salt, iterationCount, keyLength);
		cryptorAES = new CryptorAES (key);
		
		log.debug("computed", new String(Base64.encode(key)), " using ", new String(password),"salt",new String(Base64.encode(salt)), iterationCount, keyLength);
	}
	
    public Callback generate_ (String password, byte[] salt, int iterationCount, int keyLength)
    {
    	return 
	    	new CallbackDefault(password, salt, iterationCount, keyLength) {
				public void onSuccess(Object... arguments) throws Exception {
					String password = V(0);
					byte[] salt = V(1);
					int iterationCount = (Integer)V(2);
					int keyLength = (Integer)V(3);
					
					generate(password, salt, iterationCount, keyLength);
			}
		};
	}
    
    public byte[] encrypt(byte[] clearText) throws CryptoException 
    {
    	return cryptorAES.encrypt(clearText);
    }
    	
    public byte[] decrypt(byte[] encrypted) throws CryptoException
    {
    	return cryptorAES.decrypt(encrypted);
    }
}
