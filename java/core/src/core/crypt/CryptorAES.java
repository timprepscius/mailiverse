/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.exceptions.CryptoException;
import core.util.Arrays;
import core.util.LogNull;
import core.util.LogOut;
import core.util.SecureRandom;

public class CryptorAES extends Cryptor
{
	static LogNull log = new LogNull(CryptorAES.class);
	
	public static final int AES_KEYSIZE_BYTES = 32;
	public static final int AES_IVSIZE_BYTES = 16;
	static SecureRandom random = new SecureRandom();

	byte[] key;
	
	public CryptorAES (byte[] key)
	{
		this.key = key;
	}
	
    public static final byte[] NullIV = { 
        0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0
	} ;
	
	public static byte[] newIV() 
	{
		byte[] iv = new byte[AES_IVSIZE_BYTES];
		random.nextBytes(iv);
		
		return iv;
	}
	
	public static byte[] newKey() 
	{
		byte[] key = new byte[AES_KEYSIZE_BYTES];
		random.nextBytes(key);
		
		return key;
	}

	@Override
	public byte[] encrypt(byte[] bytes) throws CryptoException
	{
		byte[] iv = newIV();
		Cryptor cryptor = new CryptorAESIV(key, iv);
		return Arrays.concat(iv, cryptor.encrypt(bytes));
	}

	@Override
	public byte[] decrypt(byte[] bytes) throws CryptoException
	{
		byte[] iv = Arrays.copyOf(bytes, 0, AES_IVSIZE_BYTES);
		byte[] data = Arrays.copyOf(bytes, AES_IVSIZE_BYTES, bytes.length - AES_IVSIZE_BYTES);
		
		Cryptor cryptor = new CryptorAESIV(key, iv);
		return cryptor.decrypt(data);
	}

	public Callback decrypt_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				byte[] bytes = (byte[]) arguments[0];
				byte[] iv = Arrays.copyOf(bytes, 0, AES_IVSIZE_BYTES);
				byte[] data = Arrays.copyOf(bytes, AES_IVSIZE_BYTES, bytes.length - AES_IVSIZE_BYTES);
				
				log.debug("decrypt_", bytes.length, iv.length, data.length);
				
				Cryptor cryptor = new CryptorAESIV(key, iv);
				call(cryptor.decrypt_(), data);
			}
		};
	}
	
	public Callback encrypt_()
	{
		byte[] iv = newIV();
		Cryptor cryptor = new CryptorAESIV(key, iv);
		
		return 
			cryptor.encrypt_()
				.addCallback(new CallbackDefault(iv) {
					
					public void onSuccess(Object... arguments) throws Exception {
						byte[] iv = V(0);
						byte[] data = (byte[]) arguments[0];
						log.debug("encrypt_", iv.length, data.length);
						
						next(Arrays.concat(iv, data));
					}
				});
	}
	
}
