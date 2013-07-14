/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

public class Characters {

	public static boolean isWhitespace(char c) 
	{
		return " \t\n\r".indexOf(c)!=-1;
	}

	public static boolean isNumber(char c) 
	{
		return "0123456789".indexOf(c)!=-1;
	}

}
