/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_EventDispatcher_h__
#define __mailiverse_mail_manager_EventDispatcher_h__

#include "EventPropagator.h"
#include "mailiverse/utilities/Monitor.h"

namespace mailiverse {
namespace mail {
namespace manager {

class EventDispatcher : public EventPropagator
{
protected:
	typedef utilities::SmartPtr<Pair<String,utilities::ArgumentPtr>> Event;
	typedef List<Event> EventQueue;

	utilities::EmptyMonitor monitor;
	EventQueue eventQueue;
	Set<String> onced;

public:
	EventDispatcher() {}
	virtual ~EventDispatcher() {}

	void preDispatch ()
	{
		utilities::EmptyMonitor::Writer w(monitor);
		eventQueue.add(NULL);
		onced.clear();
	}

	Event nextEvent ()
	{
		utilities::EmptyMonitor::Writer w(monitor);
		Event next = eventQueue.front();
		eventQueue.pop_front();
		return next;
	}

	void postDispatch ()
	{
	}

	void dispatchEvents ()
	{
		preDispatch();

		Event next = nextEvent();
		while (next)
		{
			doSignal(next->first, next->second);

			next = nextEvent();
		}

		postDispatch();
	}

	virtual void signalOnce (const String &event, Argument *parameters) override
	{
		utilities::EmptyMonitor::Writer w(monitor);
		if (onced.contains(event))
			return;

		onced.add(event);

		signal(event, parameters);
	}

	virtual void signal (const String &event, Argument *parameters) override
	{
		utilities::EmptyMonitor::Writer w(monitor);
		eventQueue.add(new Pair<String,utilities::ArgumentPtr>(event, parameters));
	}
};

DECLARE_SMARTPTR(EventDispatcher);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* EVENTDISPATCHER_H_ */
