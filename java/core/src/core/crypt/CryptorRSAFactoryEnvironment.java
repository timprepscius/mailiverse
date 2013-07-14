/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bc.util.encoders.Base64;

import core.constants.ConstantsEnvironmentKeys;
import core.exceptions.CryptoException;
import core.util.Environment;

public class CryptorRSAFactoryEnvironment
{
	public static CryptorRSA createJCE (Environment env) throws CryptoException
	{
		try
		{
			PublicKey publicKey = null;
			PrivateKey privateKey = null;
			
			String publicKeyString = env.get(ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY);
			if (publicKeyString != null)
			{
			    publicKey = 
			    	KeyFactory.getInstance("RSA").generatePublic(
			    		new X509EncodedKeySpec((byte[])Base64.decode(publicKeyString))
			    	);
			}
			String privateKeyString = env.get(ConstantsEnvironmentKeys.PRIVATE_DECRYPTION_KEY);
			if (privateKeyString != null)
			{
				privateKey = 
			    	KeyFactory.getInstance("RSA").generatePrivate(
			    		new PKCS8EncodedKeySpec((byte[])Base64.decode(privateKeyString))
			    	);
			}
		
			return new CryptorRSAJCE (publicKey, privateKey);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
/*
	public static CryptorRSA createBC (Environment env) throws CryptoException
	{
		try
		{
			Base64 base64 = new Base64();
			
			String publicKeyString = env.get(ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY);
			byte[] publicKeyBytes = null;
			if (publicKeyString != null)
			    publicKeyBytes = (byte[])base64.decode(publicKeyString);

			String privateKeyString = env.get(ConstantsEnvironmentKeys.PRIVATE_DECRYPTION_KEY);
			byte[] privateKeyBytes = null;
			if (privateKeyString != null)
				privateKeyBytes = base64.decode(privateKeyString);
		
			return new CryptorRSABC (publicKeyBytes, privateKeyBytes);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
*/
	public static CryptorRSA create (Environment env) throws CryptoException
	{
		return createJCE(env);
	}
}
