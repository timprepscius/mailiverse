package core.crypt;

import core.exceptions.CryptoException;

public class CryptorNone extends Cryptor {

	@Override
	public byte[] encrypt(byte[] bytes) throws CryptoException {
		return bytes;
	}

	@Override
	public byte[] decrypt(byte[] bytes) throws CryptoException {
		return bytes;
	}

}
