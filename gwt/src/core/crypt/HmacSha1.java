package core.crypt;

import com.google.gwt.core.client.JavaScriptObject;

import app.service.JSInvoker;
import core.callback.Callback;
import core.util.Base64;

public class HmacSha1 
{
	String key64;
	
	public HmacSha1 (byte[] key)
	{
		this.key64 = Base64.encode(key);
	}
	
	protected native String jsHmac(String key64, String bytes64) /*-{
		return $wnd.mSupport.sha1_hmac(key64, bytes64);
	}-*/;

	protected native void jsHmac(String key64, String bytes64, JavaScriptObject callback) /*-{
		return $wnd.mAsync.sha1_hmac(key64, bytes64, callback);
	}-*/;

	public byte[] mac(byte[] bytes) 
	{
		return Base64.decode(jsHmac(key64, Base64.encode(bytes)));
	}

	public void mac(byte[] bytes, Callback callback)
	{
		jsHmac(key64, Base64.encode(bytes), JSInvoker.wrap(callback));
	}
}
