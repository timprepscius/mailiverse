/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

public class Base16 
{
	static String decoder = "0123456789abcdef";
	static byte[] encoder = null;
	
	static void initialize ()
	{
		if (encoder != null)
			return;
		
		encoder = new byte[0xFF];
		for (int i=0; i<decoder.length(); ++i)
			encoder[decoder.charAt(i)] = (byte)i;
	}
	
	public static byte[] decode (String value)
	{
		initialize();
		
		byte[] bytes = new byte[value.length() / 2];
		
		int i=0, j=0;
		while (i<value.length())
		{
			int nibbleHi = (int)encoder[(int)value.charAt(i++)];
			int nibbleLo = (int)encoder[(int)value.charAt(i++)];
			
			bytes[j++] = (byte) ((nibbleHi << 4) | (nibbleLo));
		}
		
		return bytes;
	}

	public static String encode (byte[] bytes)
	{
		String result = "";
		
		int i=0;
		while (i<bytes.length)
		{
			int b = bytes[i++] & 0xFF;
			int nibbleLo = b & 0x0F;
			int nibbleHi = b >> 4;
		
			result += decoder.charAt(nibbleHi);
			result += decoder.charAt(nibbleLo);
		}
		
		return result;
	}
}
