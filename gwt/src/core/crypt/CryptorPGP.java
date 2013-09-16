package core.crypt;

import java.io.IOException;

public abstract class CryptorPGP extends CryptorJS
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
	
	protected void initialize (byte[] publicKeyBytes, byte[] privateKeyBytes) throws IOException 
	{
		this.privateKeyBytes = privateKeyBytes;
		this.publicKeyBytes = publicKeyBytes;
	}
}
