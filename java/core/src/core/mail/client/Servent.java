/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

public class Servent<T>
{
	protected T master;
	
	public Servent (T master)
	{
		this.master = master;
	}
	
	public Servent ()
	{
		this.master = null;
	}
	
	public void setMaster (T master)
	{
		this.master = master;
	}
	
	public T getMaster ()
	{
		return master;
	}
}
