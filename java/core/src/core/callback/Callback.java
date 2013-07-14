/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callback;


public abstract class Callback implements CallbackInterface
{
	protected Callback callback;
	
	public Callback ()
	{
		
	}
	
	public void next (Object...arguments)
	{
		if (callback!=null)
			callback.invoke(arguments);
	}
	
	public void call (Callback callback, Object...arguments)
	{
		if (callback != null)
		{
			callback.setReturn(this.callback);
			callback.invoke(arguments);
		}
	}
	
	public CallbackChain addCallback (Callback callback)
	{
		CallbackChain chain = new CallbackChain();
		return chain.addCallback(this).addCallback(callback);
	}
	
	public Callback setReturn (Callback callback)
	{
		this.callback = callback;
		return this;
	}
}
