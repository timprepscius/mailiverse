/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.math.BigInteger;
import core.util.Base64;
import core.util.FastRandom;

public class Version
{
	public static final Version
		DELETED = Version.fromLong(-1),
		NONE = Version.fromLong(0);
	
	static FastRandom fastRandom = new FastRandom();
	
	protected String value;

	public Version(byte[] value)
	{
		this.value = Base64.encode(value);
	}
	
	public String toString()
	{
		return value;
	}
	
	protected static Version fromLong (long value)
	{
		return new Version(new BigInteger("" + value).toByteArray());		
	}

	public byte[] toBytes()
	{
		return Base64.decode(value);
	}
	
	static Version random ()
	{
		long i=0;
		while (i == 0)
			i = Math.abs(fastRandom.nextLong());

		return Version.fromLong(i);
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs)
			return true;
		
		if (rhs != null && ((Version)rhs).value.equals(this.value))
			return true;
		
		return false;
	}	
}
