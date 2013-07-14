/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

public class LogPlatform
{
	final static void println (Object o)
	{
		System.out.println(o);
	}
	
	final static void printException (Object o)
	{
		Exception e = (Exception)o;
		e.printStackTrace();
	}
}
