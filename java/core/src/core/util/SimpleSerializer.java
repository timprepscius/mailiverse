/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.IOException;

public class SimpleSerializer 
{
	public static <T> byte[] serialize (T block) throws IOException
	{
		return JSONSerializer.serialize(block);
	}
	
	public static <T> T deserialize (byte[] bytes) throws IOException, ClassNotFoundException
	{
		return JSONSerializer.deserialize(bytes);
	}
}
