/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import core.util.LogNull;
import core.util.LogOut;
import core.util.Streams;
import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callback.CallbackSync;
import core.callbacks.Memory;
import core.exceptions.CryptoException;

public class CryptorRSAAES extends Cryptor
{
	static LogNull log = new LogNull(CryptorRSAAES.class);
	
	CryptorRSA rsa;
	
	public static class Version {
		public static final int 
			R2012 = 0x00,
			R201303 = 0x01,
			CURRENT = R201303;
	};
	
	public final byte INITIAL = 0x00;
	public final byte VERSION = 0x01;
	public final int MAX_RSA_BLOCK_SIZE = 117;
	
	private final static String M_VERSION = "version";
	
	public CryptorRSAAES(CryptorRSA rsa)
	{
		this.rsa = rsa;
	}
	
	void readVersion (InputStream is, Memory memory) throws Exception
	{
		int version = is.read()&0xFF;
		if (version < 0 || version > Version.CURRENT)
			throw new Exception("Unknown version");
		
		memory.put(M_VERSION, version);
		
		log.debug("readVersion", version);
	}
	
	void writeVersion (OutputStream os) throws IOException
	{
		os.write(Version.CURRENT);
		
		log.debug("writeVersion", Version.CURRENT);
	}

	Callback readEncryptedEmbeddedKey_ (InputStream is, Memory memory)
	{
		return new CallbackDefault(memory, is) {
			public void onSuccess(Object... arguments) throws Exception
			{
				Memory memory = V(0);
				InputStream is = V(1);
				
				int version = (Integer)memory.get(M_VERSION);
				switch (version) {
		
				case Version.R2012:
					Streams.readInt3(is); // ignore the wrapper
					next(Streams.readBoundedArray(is));
					break;
				case Version.R201303:
					next(Streams.readBoundedArray(is));
					break;
				};
			}
		};
	}
	
	Callback writeEncryptedEmbeddedKey_ (OutputStream os)
	{
		return new CallbackDefault(os) {
			public void onSuccess(Object... arguments) throws Exception
			{
				OutputStream os = V(0);
				byte[] bytes = (byte[]) arguments[0];
				Streams.writeBoundedArray(os, bytes);
				next(arguments);
			}
		} ;
	}
	
	Callback decryptMainBlock_(InputStream is, Memory memory)
	{
		return new CallbackDefault (is, memory) {

			@Override
			public void onSuccess(Object... arguments) throws Exception
			{
				InputStream is = V(0);
				Memory memory = V(1);

				byte[] key = (byte[]) arguments[0];
				int version = (Integer)memory.get(M_VERSION);
				
				Cryptor cryptor = null;
				
				switch (version)
				{
				case Version.R2012:
					cryptor = new CryptorAESIV(key, CryptorAES.NullIV);
					break;
				case Version.R201303:
					cryptor = new CryptorAES(key);
					break;
				}
				
				call(cryptor.decrypt_(), Streams.readFullyBytes(is));
			}
		};
	}
	
	Callback encryptMainBlock_ (OutputStream os, Cryptor cryptor, byte[] bytes)
	{
		return new CallbackDefault (os, cryptor, bytes) {
			public void onSuccess(Object... arguments) throws Exception
			{
				OutputStream os = V(0);
				Cryptor cryptor = V(1);
				byte[] bytes = V(2);

				call(cryptor.encrypt_().addCallback(Streams.writeBytes_(os)), bytes);
			}
		};
	}
	
	public Callback decrypt_()
	{
		return new CallbackDefault () {

			@Override
			public void onSuccess(Object... arguments) throws Exception
			{
				ByteArrayInputStream is = new ByteArrayInputStream((byte[])arguments[0]);
				Memory memory = new Memory();
				readVersion(is, memory);
				
				call(
					readEncryptedEmbeddedKey_(is, memory)
						.addCallback(rsa.decrypt_())
						.addCallback(decryptMainBlock_(is, memory))
				);
			}
			
		};
	}
	
	public Callback encrypt_ ()
	{
		return new CallbackDefault () {

			@Override
			public void onSuccess(Object... arguments) throws Exception
			{
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				writeVersion(os);
				
				byte[] key = CryptorAES.newKey();
				CryptorAES aes = new CryptorAES(key);
				
				call(
					rsa.encrypt_()
						.addCallback(writeEncryptedEmbeddedKey_(os))
						.addCallback(encryptMainBlock_(os, aes, (byte[])arguments[0]))
						.addCallback(Streams.toByteArray_(os)),
					key
				);
			}
			
		};
		
	}

	public byte[] encrypt (byte[] bytes) throws CryptoException
	{
		try
		{
			return new CallbackSync<byte[]>(encrypt_()).invoke(bytes).export();
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
	
	
	public byte[] decrypt (byte[] bytes) throws CryptoException
	{
		try
		{
			return new CallbackSync<byte[]>(decrypt_()).invoke(bytes).export();
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
}
