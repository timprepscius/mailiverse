/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

public abstract class CryptorRSA extends Cryptor
{
	byte[] publicKeyBytes, privateKeyBytes;

	public byte[] getPublicKey ()
	{
		return publicKeyBytes;
	}

	public byte[] getPrivateKey ()
	{
		return privateKeyBytes;
	}

	protected void initialize (byte[] publicKeyBytes, byte[] privateKeyBytes)
	{
		this.privateKeyBytes = privateKeyBytes;
		this.publicKeyBytes = publicKeyBytes;
	}
}
