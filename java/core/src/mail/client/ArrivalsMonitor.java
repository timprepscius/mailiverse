/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

public abstract class ArrivalsMonitor extends Servent<Master>
{
	public ArrivalsMonitor ()
	{
	}
	
	public abstract void check ();
	
	public abstract boolean isChecking ();
}
