/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.google.gwt.core.client.JavaScriptObject;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackSync;

import app.service.JSInvoker;

public class Zip 
{
	static final boolean TEST_ZIP = false;
	
	static LogNull log = new LogNull(Zip.class);
	
	public static native String jsDeflate(String s) /*-{
		return $wnd.mSupport.zip_deflate(s);
	}-*/;
	
	public static native void jsDeflate(String s, JavaScriptObject callback) /*-{
		$wnd.mAsync.zip_deflate(callback, s);
	}-*/;

	public static byte[] deflate (byte[] data) throws IOException
	{
		log.debug("deflateBytes", data.length);
		
		String bytes64 = Base64.encode(data);
		String result64 = jsDeflate(bytes64);
		byte[] result = Base64.decode(result64);
		
		if (TEST_ZIP)
		{
			if (jsInflate(Base64.encode(result)) != bytes64)
			{
				String A = bytes64;
				String B = jsDeflate(A);
				byte[] C = Base64.decode(B);
				String D = Base64.encode(C);
				String E = jsInflate(D);
				boolean X = E == A;
				throw new IOException("Inflation does not yield same string " + X);
			}
		}
		
		return result;
	}
	
	public static void deflate (byte[] data, Callback callback) throws IOException
	{
		String data64 = Base64.encode(data);
		
		log.debug("deflateBytes+callback", data.length);
		jsDeflate(
			data64,
			JSInvoker.wrap(
				new CallbackDefault(data64) {
					
					/* (non-Javadoc)
					 * @see core.util.CallbackExceptionDefaultExceptionHandling#onSuccess(java.lang.Object[])
					 */
					@Override
					public void onSuccess(Object... arguments) throws Exception {
						byte[] result = Base64.decode((String)arguments[0]);
						
						if (TEST_ZIP)
						{
							if (jsInflate(Base64.encode(result)) != V(0))
							{
								String A = V(0);
								String B = jsDeflate(A);
								byte[] C = Base64.decode(B);
								String D = Base64.encode(C);
								String E = jsInflate(D);
								boolean X = E == A;
								throw new IOException("Inflation does not yield same string " + X);
							}
						}
						
						next(result);
					}
				}.setReturn(callback)
			)
		);
	}

	public static native String jsInflate(String s) /*-{
		return $wnd.mSupport.zip_inflate(s);
	}-*/;
	
	public static native String jsInflate(String s, JavaScriptObject callback) /*-{
		return $wnd.mAsync.zip_inflate(callback, s);
	}-*/;
	
	public static Callback jsInflate_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				String s = (String)arguments[0];
				jsInflate(s, JSInvoker.wrap(callback));
			}
		};
	}
	
	public static Callback jsDeflate_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				String s = (String)arguments[0];
				jsDeflate(s, JSInvoker.wrap(callback));
			}
		};
	}

	public static byte[] inflate (byte[] data) throws IOException
	{
		try
		{
			return new CallbackSync(inflate_()).<byte[]>export();
		}
		catch(Exception e)
		{
			throw new IOException(e);
		}
	}
	
	public static Callback inflate_ ()
	{
		CallbackChain chain = new CallbackChain();
		return chain
			.addCallback(log.debug_("inflate"))
			.addCallback(Base64.encode_())
			.addCallback(jsInflate_())
			.addCallback(Base64.decode_());
	}

	public static Callback deflate_ ()
	{
		CallbackChain chain = new CallbackChain();
		return chain
			.addCallback(log.debug_("deflate"))
			.addCallback(Base64.encode_())
			.addCallback(jsDeflate_())
			.addCallback(Base64.decode_())
			;
	}
}
