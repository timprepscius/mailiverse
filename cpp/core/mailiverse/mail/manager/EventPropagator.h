/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_EventPropagator_h__
#define __mailiverse_mail_manager_EventPropagator_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "Servent.h"
#include "mailiverse/Types.h"
#include "mailiverse/core/util/Callback.h"

namespace mailiverse {
namespace mail {
namespace manager {

class EventPropagator : public Servent
{
public:
	typedef void *Tag;
	typedef core::util::CallbackGeneric Callback;
	typedef utilities::Argument Argument;

protected:
	utilities::EmptyMonitor listenerMonitor;
	typedef Map<Tag,core::util::CallbackGeneric> ListenerMap;
	typedef Map<String,ListenerMap> Listeners;
	Listeners listeners;

public:
	EventPropagator() {}
	virtual ~EventPropagator() {}

	void add (const String &event, Tag tag, const Callback &callback)
	{
		utilities::EmptyMonitor::Writer lock(listenerMonitor);

		if (!listeners.containsKey(event))
			listeners.put(event, Map<Tag,Callback>());

		listeners.getr(event).put(tag, callback);
	}

	void remove (const String &event, Tag tag)
	{
		utilities::EmptyMonitor::Writer lock(listenerMonitor);

		if (listeners.containsKey(event))
		{
			listeners.getr(event).remove(tag);
		}
	}
	
	void remove (Tag tag)
	{
		utilities::EmptyMonitor::Writer lock(listenerMonitor);

		Listeners::iterator i;
		
		for (auto &i : listeners)
		{
			if (i.second.containsKey(tag))
				i.second.remove(tag);
		}
	}

	virtual void signalOnce (const String &event, Argument *parameters)
	{
		signal(event, parameters);
	}

	virtual void signal (const String &event, Argument *parameters)
	{
		doSignal(event, parameters);
	}

	virtual void doSignal (const String &event, Argument *parameters)
	{
/*
		if (event == INVOKE)
		{
			Callback c = (Callback)parameters[0];
			Object[] params = new Object[parameters.length-1];
			System.arraycopy(parameters, 1, params, 0, parameters.length-1);
			c.invoke(params);
		}
*/

		Vector<core::util::CallbackGeneric> callbacks;
		{
			utilities::EmptyMonitor::Writer lock(listenerMonitor);
			auto i = listeners.find(event);
			if (i != listeners.end())
			{
				for (auto &j : i->second)
					callbacks.push_back(j.second);
			}
		}
		
		for (auto &i : callbacks)
			i.invoke(parameters);
	}
};

} /* namespace util */
} /* namespace core */
} /* namespace mailiverse */

#endif /* EVENTPROPAGATOR_H_ */
