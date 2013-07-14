/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import core.callback.Callback;
import core.callback.CallbackDefault;

public class Base64
{
	public static String encode(byte[] bytes)
	{
		return Strings.toString(org.bc.util.encoders.Base64.encode(bytes));
	}
	
	public static byte[] decode(String b64)
	{
		return org.bc.util.encoders.Base64.decode(Strings.toBytes(b64));
	}

	public static byte[] encodeBytes(byte[] bytes)
	{
		return org.bc.util.encoders.Base64.encode(bytes);
	}

	public static byte[] decodeBytes(byte[] b64)
	{
		return org.bc.util.encoders.Base64.decode(b64);
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
