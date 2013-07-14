/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

public class IoChainThread extends Thread
{
	IoChain session;
	
	public IoChainThread (IoChain session)
	{
		this.session = session;
	}
	
	@Override
	public void run()
	{
		try
		{
			((IoChain)session).run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
