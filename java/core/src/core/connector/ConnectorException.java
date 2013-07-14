/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector;

public class ConnectorException extends Exception
{
	public ConnectorException (Exception e)
	{
		super(e);
	}
	
	public ConnectorException (String message)
	{
		super (message);
	}
}
