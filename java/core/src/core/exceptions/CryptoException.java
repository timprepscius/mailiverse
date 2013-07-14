/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.exceptions;

public class CryptoException extends Exception 
{
	public CryptoException (Exception e)
	{
		super(e);
	}

	public CryptoException(String s)
	{
		super(s);
	}
}
