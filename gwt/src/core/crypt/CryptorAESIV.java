package core.crypt;

import com.google.gwt.core.client.JavaScriptObject;

import app.service.JSInvoker;
import core.util.Base64;
import core.util.LogNull;
import core.util.LogOut;
import core.util.SecureRandom;
import core.callback.Callback;
import core.callback.CallbackDefault;
import core.exceptions.CryptoException;

public class CryptorAESIV extends CryptorJS
{
	static LogNull log = new LogNull(CryptorAESIV.class);
	
	public static final int AES_KEYSIZE_BYTES = 32;
	public static final int AES_IVSIZE_BYTES = 16;
	static SecureRandom random = new SecureRandom();

	String key, iv;
	
    public CryptorAESIV (byte[] key, byte[] iv)
    {
    	this.key = Base64.encode(key);
    	this.iv = Base64.encode(iv);
    }
    
	public Callback jsDecrypt_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				log.debug("jsDecrypt_", key, iv);
				
				jsDecrypt (
					key,
					iv,
					(String)arguments[0],
					JSInvoker.wrap(callback)
				);
			}
		};
	}
	
	public Callback jsEncrypt_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				log.debug("jsEncrypt_", key, iv);
				
				jsEncrypt (
					key,
					iv,
					(String)arguments[0],
					JSInvoker.wrap(callback)
				);
			}
		};
	}
	
	public byte[] encrypt(byte[] bytes) throws CryptoException 
	{
		return Base64.decode(jsEncrypt(key, iv, Base64.encode(bytes)));
	}

	public byte[] decrypt(byte[] bytes) throws CryptoException 
	{
		return Base64.decode(jsDecrypt(key, iv, Base64.encode(bytes)));
	}	

	//-----------------------------------------------------------------------
	
	public native String jsEncrypt(String key64, String iv64, String bytes64) /*-{
		return $wnd.mSupport.aes_encrypt(key64, iv64, bytes64);
	}-*/;
	
	public native String jsDecrypt(String key64, String iv64, String bytes64) /*-{
		return $wnd.mSupport.aes_decrypt(key64, iv64, bytes64);
	}-*/;
	
	public native void jsEncrypt(String key64, String iv64, String bytes64, JavaScriptObject callback) /*-{
		$wnd.mAsync.aes_encrypt(callback, key64, iv64, bytes64);
	}-*/;
	
	public native void jsDecrypt(String key64, String iv64, String bytes64, JavaScriptObject callback) /*-{
		$wnd.mAsync.aes_decrypt(callback, key64, iv64, bytes64);
	}-*/;
}
