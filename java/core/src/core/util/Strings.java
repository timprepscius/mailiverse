/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

public class Strings 
{
	static public String concat (Collection<?> c, String delimiter)
	{
		return concat(c.iterator(), delimiter);
	}
	
	static public String toString (byte[] bytes, String encoding) throws UnsupportedEncodingException
	{
		if ("UTF-8".equals(encoding))
			return toString(bytes);
		
		return new String(bytes, encoding);
	}

	static public String toString (byte[] bytes)
	{
		return StringsPlatform.toString(bytes);
	}
	
	static public String toString (char[] chars)
	{
		return new String(chars);
	}
	
	static public byte[] toBytes (String s)
	{
		return StringsPlatform.toBytes(s);
	}
	
	@SuppressWarnings("rawtypes")
	static public String concat (Iterator i, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		while (i.hasNext())
		{
			String string = i.next().toString();
			if (!first)
				sb.append(delimiter);
			
			sb.append(string);
			
			first = false;
		}
		
		return sb.toString();
	}

	static public String concat (Object[] args, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object string : args)
		{
			if (!first)
				sb.append(delimiter);
			
			sb.append(string.toString());
			
			first = false;
		}
		
		return sb.toString();
	}

	static public String concatExcudingNull (Object[] objects, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object i : objects)
		{
			if (i == null)
				continue;
			
			String string = i.toString();
			if (!first)
				sb.append(delimiter);
			
			sb.append(string);
			
			first = false;
		}
		
		return sb.toString();
	}
	
	static public String[] splitLines (String lines)
	{
		return lines.split("\\r?\\n");
	}
	
	public static String trimQuotes( String value )
	{
		if (value == null)
			return value;

		value = value.trim();
		boolean b = value.startsWith("\"") || value.startsWith("\'");
		boolean e = value.endsWith("\"") || value.endsWith("\'");
		
		if (b || e)
			return value.substring(0 + (b ? 1 : 0), value.length() - (e ? 1 : 0));

		return value;
	}
}
