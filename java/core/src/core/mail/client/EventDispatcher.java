/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import core.util.LogNull;
import core.util.Pair;


public class EventDispatcher extends EventPropagator
{
	static LogNull log = new LogNull(EventDispatcher.class);
	List<Pair<String,Object[]>> eventQueue = new LinkedList<Pair<String,Object[]>>();
//		Collections.synchronizedList(new LinkedList<Pair<String,Object[]>>()
//	);
	
	Set<String> onced = new HashSet<String>();

	public synchronized void prepareForDispatch ()
	{
		eventQueue.add(null);
		onced.clear();
	}
	
	public void dispatchEvents ()
	{
		prepareForDispatch();
		
		Pair<String, Object[]> next = null;
		while ((next = eventQueue.get(0))!=null)
		{
			eventQueue.remove(0);
			doSignal(next.first, next.second);
		}
		
		eventQueue.remove(0);
	}
	
	@Override
	public void signalOnce (String event, Object...parameters)
	{
		log.debug("signalOnce", event);
		if (onced.contains(event))
			return;
		
		onced.add(event);

		signal(event, parameters);
	}
	
	@Override
	public void signal (String event, Object... parameters)
	{
		log.debug("signal",event,parameters);
		eventQueue.add(new Pair<String,Object[]>(event, parameters));
	}
}
