/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "EventDispatcherThreaded.h"

using namespace mailiverse;
using namespace mailiverse::mail::manager;

void EventDispatcherThreaded::__thread__ (void *_self)
{
	EventDispatcherThreaded *self = (EventDispatcherThreaded *)_self;
	while (true)
	{
		self->monitor.wait();
		self->monitor.reset();
		
		if (self->finished)
			break;

		self->dispatchEvents();
	}
}

EventDispatcherThreaded::EventDispatcherThreaded() :
	finished(false)
{
	thread = utilities::create(&EventDispatcherThreaded::__thread__, this);
}

EventDispatcherThreaded::~EventDispatcherThreaded()
{
	finished = true;
	monitor.signal();
	utilities::wait(thread);
}

void EventDispatcherThreaded::signal (const String &event, Argument *parameters)
{
	EventDispatcher::signal(event, parameters);
	monitor.signal();
}

