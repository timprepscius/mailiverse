/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package core.util;

import com.google.gwt.core.client.JsArrayInteger;

import core.callback.Callback;
import core.callback.CallbackDefault;

public class Base64
{
	/*
	public static String encode(byte[] bytes)
	{
		return Strings.toString(Base64Impl.encode(bytes));
	}
	
	public static byte[] decode(String b64)
	{
		return Base64Impl.decode(b64.getBytes());
	}

	public static byte[] encodeBytes(byte[] bytes)
	{
		return Base64Impl.encode(bytes);
	}

	public static byte[] decodeBytes(byte[] b64)
	{
		return Base64Impl.decode(b64);
	}
	*/
	
	public native static String jsEncode(byte[] bytes) /*-{
		return $wnd.Base64.encode(bytes);
	}-*/;
	
	public native static JsArrayInteger jsDecode(String base64) /*-{
		return $wnd.Base64.decode(base64);
	}-*/;
	
	public native static JsArrayInteger jsEncodeBytes(byte[] bytes) /*-{
		return $wnd.Base64.encodeBytes(bytes);
	}-*/;
	
	public native static JsArrayInteger jsDecodeBytes(byte[] base64) /*-{
		return $wnd.Base64.decodeBytes(base64);
	}-*/;

	public static byte[] convert(JsArrayInteger a)
	{
		byte[] bytes = new byte[a.length()];
		for (int i=0; i<a.length(); ++i)
			bytes[i] = (byte)a.get(i);
		
		return bytes;
	}
	
	public static String encode(byte[] bytes)
	{
		if (bytes == null)
			throw new NullPointerException("base64 null bytes");

		return jsEncode(bytes);
	}
	
	public static byte[] decode(String base64)
	{
		if (base64 == null)
			throw new NullPointerException("base64 null string");

		return convert(jsDecode(base64));
	}
	
	public static byte[] decodeBytes(byte[] bytes)
	{
		if (bytes == null)
			throw new NullPointerException("base64 null bytes");
		
		return convert(jsDecodeBytes(bytes));
	}
	
	public static byte[] encodeBytes(byte[] bytes)
	{
		if (bytes == null)
			throw new NullPointerException("base64 null bytes");

		return convert(jsEncodeBytes(bytes));
	}

	public static Callback decode_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				next(Base64.decode((String)arguments[0]));
			}
		};
	}
	
	public static Callback encode_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				next(Base64.encode((byte[])arguments[0]));
			}
		};
	}
	
	public static Callback decodeBytes_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				next(Base64.decodeBytes((byte[])arguments[0]));
			}
		};
	}
	
	public static Callback encodeBytes_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				next(Base64.encodeBytes((byte[])arguments[0]));
			}
		};
	}
}
