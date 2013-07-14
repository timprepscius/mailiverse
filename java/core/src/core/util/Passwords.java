/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.IOException;

public class Passwords
{
	public static String getPasswordFor (String key) throws IOException
	{
		try
		{
			return ExternalResource.getTrimmedString("passwords/" + key);
		}
		catch (Exception e)
		{
			throw new IOException("Internal error. #9203 " + key);
		}
	}
}
