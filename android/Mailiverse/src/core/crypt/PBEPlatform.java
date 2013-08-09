package core.crypt;

import core.exceptions.CryptoException;

public class PBEPlatform
{
	static byte[] generate (String password, byte[] salt, int iterationCount, int keyLength) throws CryptoException
	{
		return PBEPlatformNative.generate(password, salt, iterationCount, keyLength);
	}
}
