/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callback;

import core.util.LogNull;

public abstract class CallbackDefault extends CallbackWithVariables
{
	static LogNull log = new LogNull(CallbackDefault.class);
	
	int shouldFireNext=0;
	
	public CallbackDefault(Object...args)
	{
		super(args);
	}
	
	@Override
	public void invoke(Object... arguments) 
	{
		shouldFireNext++;
		
		try
		{
			if (arguments!=null && arguments.length==1 && arguments[0] instanceof Exception)
				throw (Exception)arguments[0];
			
			onSuccess(arguments);
		}
		catch (Exception e)
		{
			onFailure(e);
		}
		
		if (shouldFireNext!=0)
			log.debug("**************************** NEXT NEVER CALLED, IS THIS CORRECT? *************************");
	}

	public abstract void onSuccess (Object... arguments) throws Exception;
	
	public void next(Object...arguments)
	{
		shouldFireNext--;
		super.next(arguments);
	}
	
	public void call(Callback callback, Object...arguments)
	{
		shouldFireNext--;
		super.call(callback, arguments);
	}
	
	public void onFailure (Exception e)
	{
		log.error("propagating", e);
		log.exception(e);
		next(e);
	}
}
