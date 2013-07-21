package core.crypt;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.exceptions.CryptoException;
import core.util.Base64;

public abstract class CryptorJS extends Cryptor
{
	public abstract Callback jsEncrypt_();
	public abstract Callback jsDecrypt_();
	
	public Callback encrypt_()
	{
		CallbackChain chain = new CallbackChain();
		return chain
			.addCallback(Base64.encode_())
			.addCallback(jsEncrypt_())
			.addCallback(Base64.decode_());
	}

	public Callback decrypt_()
	{
		CallbackChain chain = new CallbackChain();
		return chain
			.addCallback(Base64.encode_())
			.addCallback(jsDecrypt_())
			.addCallback(Base64.decode_());
	}

	public byte[] encrypt(byte[] bytes) throws CryptoException 
	{
		throw new RuntimeException("Use callbacks in JS mode");
	}

	public byte[] decrypt(byte[] bytes) throws CryptoException 
	{
		throw new RuntimeException("Use callbacks in JS mode");
	}
}
