/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import core.util.Arrays;
import core.util.SecureRandom;

import org.bc.crypto.BufferedBlockCipher;
import org.bc.crypto.engines.AESEngine;
import org.bc.crypto.modes.CBCBlockCipher;
import org.bc.crypto.paddings.BlockCipherPadding;
import org.bc.crypto.paddings.PKCS7Padding;
import org.bc.crypto.paddings.PaddedBufferedBlockCipher;
import org.bc.crypto.params.KeyParameter;
import org.bc.crypto.params.ParametersWithIV;

import core.exceptions.CryptoException;


public class CryptorAESIV extends Cryptor
{
	static SecureRandom random = new SecureRandom();
	
	byte[] key;
	byte[] iv;
	
	public CryptorAESIV (byte[] key, byte[] iv)
	{
		// setup cipher parameters with key and IV
	    this.key = key;
	    this.iv = iv;
	}
	
	@Override
	public byte[] decrypt(byte[] bytes) throws CryptoException
	{
		try
		{
		    BlockCipherPadding padding = new PKCS7Padding();
		    BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
		            new CBCBlockCipher(new AESEngine()), padding);

		    ParametersWithIV params = new ParametersWithIV(new KeyParameter(key), iv);		    
		    cipher.reset();
		    cipher.init(false, params);		
		    
		    byte[] out = new byte[cipher.getOutputSize(bytes.length)];
		    int len = cipher.processBytes(bytes, 0, bytes.length, out, 0);
		    len += cipher.doFinal(out, len);
		    
		    if (len == out.length)
		    	return out;
		    
		    return Arrays.copyOf(out, len);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}

	@Override
	public byte[] encrypt(byte[] bytes) throws CryptoException
	{
		try
		{
		    BlockCipherPadding padding = new PKCS7Padding();
		    BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
		            new CBCBlockCipher(new AESEngine()), padding);

		    ParametersWithIV params = new ParametersWithIV(new KeyParameter(key), iv);		    
		    cipher.reset();
		    cipher.init(true, params);		
		    
		    byte[] out = new byte[cipher.getOutputSize(bytes.length)];
		    int len = cipher.processBytes(bytes, 0, bytes.length, out, 0);
		    len += cipher.doFinal(out, len);
		    
		    if (len == out.length)
		    	return out;
		    
		    return Arrays.copyOf(out, len);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
	
}
