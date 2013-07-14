/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.UnsupportedEncodingException;

public class StringsPlatform
{
	static public String toString(byte[] bytes)
	{
		try
		{
			return new String(bytes, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	static public byte[] toBytes(String string)
	{
		try
		{
			return string.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

}
