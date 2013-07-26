/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_EventDispatcherThreaded_h__
#define __mailiverse_mail_manager_EventDispatcherThreaded_h__

#include "EventDispatcher.h"
#include "mailiverse/utilities/Monitor.h"
#include "mailiverse/utilities/Thread.h"

namespace mailiverse {
namespace mail {
namespace manager {

class EventDispatcherThreaded : public EventDispatcher
{
public:
	typedef EventDispatcher Super;

protected:
	bool finished;
	utilities::EmptyMonitor monitor;
	utilities::Thread thread;

	static void __thread__ (void *);

public:
	EventDispatcherThreaded();
	virtual ~EventDispatcherThreaded();

	virtual void signal (const String &event, Argument *parameters) override;
};

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_EventDispatcherThreaded_h__ */
