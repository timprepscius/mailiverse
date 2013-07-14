/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import core.exceptions.CryptoException;

public class CryptorRSAJCE extends CryptorRSA
{
	SecureRandom random = new SecureRandom();
	
	public final int MAX_RSA_BLOCK_SIZE = 117;
	
	PublicKey publicKey;
	PrivateKey privateKey;
	
    public CryptorRSAJCE (InputStream keyStore, InputStream trustStore) throws Exception
    {
    	if (keyStore != null)
    	{
			KeyStore kks = KeyStore.getInstance("JKS");
			kks.load(keyStore, "password".toCharArray());
			privateKey = (PrivateKey)kks.getKey(kks.aliases().nextElement(), "password".toCharArray());
			
			if (privateKey == null)
				throw new CryptoException(new Exception("RSA Private Key not found in KeyStore."));
    	}

    	if (trustStore != null)
    	{
			KeyStore tks = KeyStore.getInstance("JKS");
			tks.load(trustStore, "password".toCharArray());
			publicKey = tks.getCertificate(tks.aliases().nextElement()).getPublicKey();

			if (publicKey == null)
				throw new CryptoException(new Exception("RSA Public Key not found in KeyStore."));
    	}
    	
    	initialize(
    		publicKey!=null ? publicKey.getEncoded() : null,
    		privateKey!=null ? privateKey.getEncoded() : null
    	);
    }
    
	public CryptorRSAJCE (PublicKey publicKey, PrivateKey privateKey)
	{
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		
    	initialize(
    		publicKey!=null ? publicKey.getEncoded() : null,
    		privateKey!=null ? privateKey.getEncoded() : null
    	);
	}
	
	public byte[] encrypt (byte[] block) throws CryptoException
	{
		try
		{
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(block, 0, block.length);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
	
	public byte[] decrypt (byte[] block) throws CryptoException
	{
		try
		{
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(block, 0, block.length);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
}
