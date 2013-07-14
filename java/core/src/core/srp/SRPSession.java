/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp;

import core.crypt.Cryptor;
import core.crypt.CryptorAES;
import core.exceptions.CryptoException;


public class SRPSession 
{
	protected byte[] sessionKey;
	
	public byte[] streamDecrypt (byte[] bytes) throws CryptoException
	{
		try
		{
			Cryptor cryptor = new CryptorAES(sessionKey);
			return cryptor.decrypt(bytes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new CryptoException(e);
		}
	}

	public byte[] streamEncrypt (byte[] bytes) throws CryptoException
	{
		try
		{
			Cryptor cryptor = new CryptorAES(sessionKey);
			return cryptor.encrypt(bytes);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
}
