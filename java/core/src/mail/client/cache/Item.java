/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackEmpty;
import core.util.LogNull;
import core.util.LogOut;

public abstract class Item extends Info
{
	static LogNull log = new LogNull(Item.class);
	
	CallbackChain onLoadCallbacks;
	CallbackChain onLoadOnceCallbacks;
	
	public Item ()
	{
		
	}
	
	protected void onDirty()
	{
		super.onDirty();
		
		if (owner != null)
			owner.onDirty(this);
	}
	
	protected void onLoaded() 
	{
		log.debug("onLoaded", this);
		
		super.onLoaded();
		
		if (onLoadCallbacks != null)
			onLoadCallbacks.invoke(this);

		if (onLoadOnceCallbacks != null)
		{
			CallbackChain chain = onLoadOnceCallbacks;
			onLoadOnceCallbacks = null;
			chain.invoke(this);
		}
	};
	
	public CallbackChain getLoadCallbacks()
	{
		if (onLoadCallbacks == null)
		{
			onLoadCallbacks = new CallbackChain();
			onLoadCallbacks.setPropagateOriginalArguments().setSlowFail();
		}
		return onLoadCallbacks;
	}
	
	public CallbackChain getLoadOnceCallbacks()
	{
		if (onLoadOnceCallbacks == null)
		{
			onLoadOnceCallbacks = new CallbackChain();
			onLoadOnceCallbacks.setPropagateOriginalArguments().setSlowFail();
		}
		
		return onLoadOnceCallbacks;
	}
	
	public void apply (Callback callback)
	{
		if (isLoaded())
			callback.invoke(this);
		else
			getLoadOnceCallbacks().addCallback(callback);
	}
	
	public Callback flush_ ()
	{
		return new CallbackEmpty();
	}
	
	public void debug(LogOut log, String prefix)
	{
		log.debug(prefix,this);
	}
}
