/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callback;

import java.util.ArrayList;

import core.util.LogNull;
import core.util.LogOut;

public class CallbackChain extends Callback
{
	static LogNull log = new LogNull(CallbackChain.class);
	Callback head, tail, execute, next;
	
	boolean fastFail = true;
	boolean propagateOriginalArguments = false;
	
	Object[] originalArguments;
	
	int index = -1;
	
	public CallbackChain()
	{
		
	}
	
	@Override
	public CallbackChain addCallback (Callback callback)
	{
		log.debug(this,"adding callback",callback);
		if (tail != null)
			tail.callback = callback;
		
		if (head == null)
			head = callback;
		
		if (next == null)
			next = head;
		
		tail = callback;
		
		return this;
	}
	
	public CallbackChain setPropagateOriginalArguments ()
	{
		propagateOriginalArguments = true;
		return this;
	}
	
	public CallbackChain setSlowFail ()
	{
		fastFail = false;
		return this;
	}

	@Override
	public void invoke(Object... arguments)
	{
		log.debug("invoke");
		
		if (next == head)
			originalArguments = arguments;
		
		// reset the modifications we made during traversal
		if (execute != null)
			execute.callback = next;
		
		if (tail != null && tail.callback != null)
			log.error(this,"tail callback is not null!",execute,tail);
		
		boolean hasException = 
			arguments != null && 
			arguments.length > 0 && 
			arguments[0] instanceof Exception;
		
		if (hasException)
		{
			log.debug(this,"has exception");
			log.exception((Exception)arguments[0]);
		}
		
		// if we should skip to the end, and we haven't already skipped to the end
		if (hasException && fastFail && execute != tail)
		{
			log.debug(this,"advancing to tail",tail);
			next = tail;
		}		
		
		if (propagateOriginalArguments)
		{
			log.debug(this,"using original arguments");
			arguments = originalArguments;
		}
		
		execute = next;
		if (execute != null)
		{
			next = execute.callback;
			execute.callback = this;
			
			log.debug(this, "execute", execute, next);
			execute.invoke(arguments);
		}
		else
		{
			log.debug(this,"executing self callback");

			// reset
			next = head;
			originalArguments = null;
			next(arguments);
		}
	}
	
	public Callback[] toArray ()
	{
		ArrayList<Callback> array = new ArrayList<Callback>();
		
		Callback i = head;
		while (i!=null)
		{
			array.add(i);
			i = i.callback;
		}
		
		return array.toArray(new Callback[0]);
	}
	
}
