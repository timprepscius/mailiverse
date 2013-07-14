/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import core.exceptions.CryptoException;


public class CryptorAESJCE extends Cryptor
{
	SecretKeySpec secretKey;
	IvParameterSpec iv;
	
	public CryptorAESJCE (SecretKeySpec secretKey, IvParameterSpec iv)
	{
		this.secretKey = secretKey;
		this.iv = iv;
	}
	
    public byte[] encrypt(byte[] clearText) throws CryptoException 
    {
    	try
    	{
    		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
	        return cipher.doFinal(clearText);
    	}
    	catch (Exception e)
    	{
    		throw new CryptoException(e);
    	}
    }
	
    public byte[] decrypt(byte[] encrypted) throws CryptoException
    {
    	try
    	{
    		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
	        return cipher.doFinal(encrypted);
    	}
    	catch (Exception e)
    	{
    		throw new CryptoException (e);
    	}
    }
}
