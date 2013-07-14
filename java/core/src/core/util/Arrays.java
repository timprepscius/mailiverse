/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.ArrayList;
import java.util.List;

public class Arrays 
{
	public static <T> T firstOrNull (T[] t)
	{
		if (t != null && t.length > 0)
			return t[0];
					
		return null;
	}
	
	public static byte[] generate (int length, int j)
	{
		byte[] v = new byte[length];
		for (int i=0; i<length; ++i)
			v[i]=(byte)j;
		
		return v;
	}
	
	public static byte[] concat (byte[]... arrays)
	{
		int length = 0;
		for (byte[] array : arrays)
			length += array.length;
		
		byte[] result = new byte[length];
		
		int i=0;
		for (byte[] array : arrays)
		{
			for (byte b : array)
				result[i++] = b;
		}
		
		return result;
	}
	
	public static byte[] copyOf(byte[] src, int l)
	{
		return copyOf(src, 0, l);
	}

	public static void copyFromTo(byte[] src, int srcOff, byte[] dst, int dstOff, int l)
	{
		for (int i=0; i<l; ++i)
			dst[i+dstOff] = src[i+srcOff];
	}
	
	public static void copyFromTo(byte[] src, byte[] dst, int l)
	{
		copyFromTo(src,0, dst,0, l);
	}
	
	public static byte[] copyOf(byte[] src, int offset, int l)
	{
		byte[] result = new byte[l];
		
		for (int i=0; i<l; ++i)
			result[i] = src[offset + i];
		
		return result;
	}
}
