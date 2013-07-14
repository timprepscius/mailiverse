/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bc.crypto.util.PrivateKeyFactory;
import org.bc.crypto.util.PublicKeyFactory;

import core.exceptions.CryptoException;
import core.callback.Callback;
import core.util.Base64;
import core.util.Pair;

public class CryptorRSAFactory 
{
	public Pair<byte[], byte[]> generate (int bits) throws CryptoException
	{
		try
		{
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(bits);
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			byte[] privateKey = keyPair.getPrivate().getEncoded();
			byte[] publicKey = keyPair.getPublic().getEncoded();
		
			return new Pair<byte[], byte[]>(publicKey, privateKey);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
	
	public void generate (int bits, Callback callback)
	{
		try
		{
			callback.invoke(generate(bits));
		}
		catch (CryptoException e)
		{
			callback.invoke(e);
		}
	}
	
	public static CryptorRSA fromResources(InputStream publicKey, InputStream privateKey) throws Exception
	{
		return new CryptorRSAJCE(publicKey, privateKey);
	}

	public static CryptorRSA fromString(String publicKey, String privateKey) throws Exception
	{
		return 
			new CryptorRSAJCE(
				publicKey != null ?
					KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
						Base64.decode(publicKey))
					) :
					null,
				privateKey != null ?
					KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(
						Base64.decode(privateKey))
					) :
					null
			);					
	}
	
}
