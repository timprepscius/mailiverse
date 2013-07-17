/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.math.BigInteger;

import core.util.Arrays;
import core.util.Base16;
import core.util.SecureRandom;

public class ID 
{
	static public final int PartSize=6;
	static public ID None = ID.fromLong(-1);
	static SecureRandom random = new SecureRandom();
	
	public static final byte VERSION = 1;
	final String value;
	
	private ID(byte[] value)
	{
		this.value = Base16.encode(value);
	}
	
	private ID(String value)
	{
		this.value = value;
	}
	
	public static ID combine(ID left, ID right)
	{
		return new ID(left.value + "00" + right.value);
	}

	public int indexOfDoubleNull(String s)
	{
		for (int i=0; i<s.length()-1; i+=2)
		{
			if (s.charAt(i)=='0' && s.charAt(i+1)=='0')
				return i;
		}
		
		return -1;
	}
	
	public ID left ()
	{
		return new ID(value.substring(0, indexOfDoubleNull(value)));
	}

	public ID right ()
	{
		int indexOf = indexOfDoubleNull(value);
		if (indexOf == -1)
			return this;
		
		return new ID(value.substring(indexOf+2));
	}
	
	public static ID deserialize(byte[] bytes)
	{
		byte version = bytes[0];
		return new ID(Arrays.copyOf(bytes,1, bytes.length-1));
	}
	
	public byte[] serialize()
	{
		return Base16.decode( ("0"+VERSION) + value );
	}
	
	public static ID fromLong(long l)
	{
		return new ID(new BigInteger("" + l).toByteArray());
	}
	
	public static ID random ()
	{
		byte[] bytes = new byte[PartSize];
		
		for (int i=0; i<bytes.length; ++i)
		{
			bytes[i]=0;
			
			while (bytes[i]==0)
				bytes[i] = (byte)random.nextInt();
		}
		
		return new ID(bytes);
	}
	
	public static ID fromString (String s)
	{
		return new ID(Base16.decode(s));
	}
	
	public String toString ()
	{
		return value;
	}
	
	@Override
	public boolean equals (Object rhs)
	{
		if (this == rhs)
			return true;
		
		if (rhs != null && ((ID)rhs).value.equals(value))
			return true;
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	public String toFileSystemSafe ()
	{
		return value;
	}
	
}
