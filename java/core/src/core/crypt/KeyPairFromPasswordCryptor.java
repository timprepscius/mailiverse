/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import core.callback.Callback;
import core.exceptions.CryptoException;

public class KeyPairFromPasswordCryptor extends Cryptor
{
	KeyPairFromPassword keyPair;
	
	public KeyPairFromPasswordCryptor (KeyPairFromPassword keyPair)
	{
		this.keyPair = keyPair;
	}
	
	public byte[] encrypt (byte[] clearBlock) throws CryptoException
	{
		return keyPair.getCryptor().encrypt(clearBlock);
	}
	
	public byte[] decrypt (byte[] encryptedBlock) throws CryptoException
	{
		return keyPair.getCryptor().decrypt(encryptedBlock);
	}

	@Override
	public Callback encrypt_() 
	{
		return keyPair.getCryptor().encrypt_();
	}

	@Override
	public Callback decrypt_()
	{
		return keyPair.getCryptor().decrypt_();
	}
}
