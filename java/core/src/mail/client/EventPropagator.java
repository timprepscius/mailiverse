/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;

public class EventPropagator 
{
	static LogNull log = new LogNull(EventPropagator.class);
	
	public static final String INVOKE = "__INVOKE__";
	
	Map<String, List< Pair<Object,Callback> > > listeners = new HashMap<String, List<Pair<Object, Callback>> >();

	public void add (String event, Object tag, Callback callback)
	{
		if (!listeners.containsKey(event))
			listeners.put(event, new ArrayList<Pair<Object,Callback>>());
		
		listeners.get(event).add(new Pair<Object,Callback>(tag, callback));
	}
	
	public void remove (String event, Object tag)
	{
		List<Pair<Object,Callback>> callbacks = listeners.get(event);
		if (callbacks != null)
		{
			ArrayList<Pair<Object,Callback>> remove = new ArrayList<Pair<Object,Callback>>();
			
			for (Pair<Object,Callback> callback : callbacks)
			{
				if (callback.first.equals(tag))
					remove.add(callback);
			}
			
			for (Pair<Object,Callback> callback : remove)
			{
				callbacks.remove(callback);
			}
		}
	}

	public void signalOnce (String event, Object...parameters)
	{
		signal(event, parameters);
	}

	public void signal (String event, Object...parameters)
	{
		log.debug("signal",event, parameters);
		
		doSignal(event, parameters);
	}
	
	public Callback signal_ (String event, Object...parameters)
	{
		log.debug("signal_",event, parameters);
		
		return new CallbackDefault(event, parameters) {
			public void onSuccess(Object... arguments) throws Exception {
				String event = V(0);
				Object[] parameters = V(1);
				signal(event, parameters);
				
				next();
			}
		};
	}
	
	protected void doSignal (String event, Object...parameters)
	{
		log.debug("doSignal",event, parameters);
		
		if (event.equals(INVOKE))
		{
			Callback c = (Callback)parameters[0];
			Object[] params = new Object[parameters.length-1];
			System.arraycopy(parameters, 1, params, 0, parameters.length-1);
			c.invoke(params);
		}
		
		List<Pair<Object,Callback>> callbacks = listeners.get(event);
		if (callbacks != null)
		{
			for (Pair<Object,Callback> callback : callbacks)
			{
				callback.second.invoke(parameters);
			}
		}
	}
}
