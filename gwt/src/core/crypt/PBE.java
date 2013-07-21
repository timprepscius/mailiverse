package core.crypt;

import com.google.gwt.core.client.JavaScriptObject;

import app.service.JSInvoker;
import core.util.Base64;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Strings;
import core.callback.Callback;
import core.callback.CallbackDefault;
import core.exceptions.CryptoException;


/**
 * http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
 */
public class PBE extends Cryptor
{
	byte[] key;
	Cryptor cryptorAES;
	
	static LogNull log = new LogNull(PBE.class);
	
    public static final int DEFAULT_ITERATIONS = 131072;
    public static final int SHORT_ITERATIONS = 32768;
    public static final int DEFAULT_KEYLENGTH = 256;
    
    /**
     * bytes used to salt the key (set before making an instance)
     */
    public static final byte[] DEFAULT_SALT_0 = {
        (byte)0xc8, (byte)0x73, (byte)0x41, (byte)0x8c,
        (byte)0x7e, (byte)0xd8, (byte)0xee, (byte)0x89
    };
    
    /**
     * bytes used to salt the key (set before making an instance)
     */
    public static final byte[] DEFAULT_SALT_1 = {
        (byte)0x12, (byte)0x53, (byte)0x14, (byte)0xbb,
        (byte)0x7e, (byte)0x97, (byte)0xce, (byte)0x55
    };
    
    /**
     * bytes used to salt the key (set before making an instance)
     */
    public static final byte[] DEFAULT_SALT_2 = {
        (byte)0x0a, (byte)0x48, (byte)0x33, (byte)0xfe,
        (byte)0xa7, (byte)0xc2, (byte)0x2c, (byte)0x71
    };

    public static final byte[][] DEFAULT_SALT = { DEFAULT_SALT_0, DEFAULT_SALT_1, DEFAULT_SALT_2 };

    public PBE (String password, byte[] salt, int iterationCount, int keyLength) throws CryptoException
    {
    	generate(password, salt, iterationCount, keyLength);
    }
    
    public PBE ()
    {
    	
    }
    
    public void generate (String password, byte[] salt, int iterationCount, int keyLength) throws CryptoException 
    {
    	key = Base64.decode(jsGenKey(password, Base64.encode(salt), iterationCount, keyLength));
		cryptorAES = new CryptorAES (key);
		log.debug("computed", Base64.encode(key), " using ", password,"salt",Base64.encode(salt), iterationCount, keyLength);		
    }
    
    public Callback generate_ (String password, byte[] salt, int iterationCount, int keyLength)
    {
    	return 
	    	new CallbackDefault(password, salt, iterationCount, keyLength) {
				public void onSuccess(Object... arguments) throws Exception {
					String password = V(0);
					byte[] salt = V(1);
					int iterationCount = V(2);
					int keyLength = V(3);
					
			    	jsGenKey(
			    		password, Base64.encode(salt), iterationCount, keyLength,
		        		JSInvoker.wrap(
		    	    		new CallbackDefault() {
		    					@Override
		    					public void onSuccess(Object... arguments) throws Exception {
		    						String key64 = (String)arguments[0];
		    						key = Base64.decode(key64);
		    						cryptorAES = new CryptorAES (key);
		    						
		    						callback.invoke();
		    					}
		    				}.setReturn(callback)
		    	    	)
		        	);
					
				}
			};
    }

    protected native String jsGenKey (String password, String salt64, int iterationCount, int keyLength) /*-{
    	return $wnd.mSupport.pbe_genKey(password, salt64, iterationCount, keyLength);
    }-*/;
    
    protected native String jsGenKey (String password, String salt64, int iterationCount, int keyLength, JavaScriptObject callback) /*-{
		return $wnd.mAsync.pbe_genKey(callback, password, salt64, iterationCount, keyLength);
	}-*/;

    public byte[] encrypt(byte[] clearText) throws CryptoException 
    {
    	return cryptorAES.encrypt(clearText);
    }
    	
    public byte[] decrypt(byte[] encrypted) throws CryptoException
    {
    	return cryptorAES.decrypt(encrypted);
    }

	@Override
	public Callback encrypt_()
	{
    	return cryptorAES.encrypt_();
	}

	@Override
	public Callback decrypt_()
	{
    	return cryptorAES.decrypt_();
	}
}
