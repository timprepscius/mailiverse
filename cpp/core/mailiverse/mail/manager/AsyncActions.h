/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_AsyncActions_h__
#define __mailiverse_mail_manager_AsyncActions_h__

#include "Actions.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "../model/Conversation.h"
#include "mailiverse/Types.h"
#include "mailiverse/utilities/Time.h"

namespace mailiverse {
namespace mail {
namespace manager {

class AsyncActions : public Actions
{
public:
	typedef Actions Super;

protected:
	bool finished;
	typedef Pair<core::util::Callback<>,core::util::Callback<>> Act;
	utilities::Thread thread;

	typedef utilities::Monitor<List<Act>> ActMonitor;
	ActMonitor actMonitor;

protected:

	void __thread__ ()
	{
		while (true)
		{
			actMonitor.wait();
			actMonitor.reset();
			if(finished)
				break;
				
			while (true)
			{
				if(finished)
					break;
					
				Act act;
				// scope lock
				{
					ActMonitor::Writer writer(actMonitor);
					if (writer->empty())
						break;
						
					act = writer->front();
					writer->pop_front();
				}
					
				try
				{
					act.first.invoke();
					act.second.invoke();
				}
				catch (Exception &e)
				{
					LogDebug(mailiverse::mail::cache, "update caught exception " << e.what());
					act.second.invoke(e);
				}
				catch (...)
				{
					LogDebug(mailiverse::mail::cache, "update caught unknown exception");
					act.second.invoke(Exception("unknown"));
				}
			}
		
		}
	}

	static void __threadDispatch__ (void *self)
	{
		((AsyncActions*)self)->__thread__();
	}
	
protected:

	utilities::Thread flushThread;

	void __flushThread__ ()
	{
		while (!finished)
		{
			utilities::sleepSeconds(0.25f);
			possiblyFlush();
		}
	}
	
	static void __flushThreadDispatch__ (void *self)
	{
		((AsyncActions*)self)->__flushThread__();
	}
	
	void possiblyFlush ();

public:
	AsyncActions() :
		finished(false),
		thread(NULL),
		flushThread(NULL)
	{
		assert(!thread);
		thread = utilities::create(__threadDispatch__, this);
	}
	
	virtual ~AsyncActions()
	{
		finished = true;
		actMonitor.signal();
		utilities::wait(thread);
		utilities::wait(flushThread);
	}
	
	virtual void onInitialized () override
	{
		startFlushMechanism	();
	}
	
	void startFlushMechanism ()
	{
		assert(!flushThread);
		flushThread = utilities::create(__flushThreadDispatch__, this);
	}
	
	void queueAction (Action action, core::util::Callback<> callback)
	{
		Act act(
			core::util::Callback<>(utilities::newbindC(this, action)),
			callback
		);
		
		ActMonitor::Writer writer(actMonitor);
		writer->push_back(act);
		actMonitor.signal();
	}
	
	virtual void flush (core::util::Callback<> callback) override
	{
		queueAction (&Actions::doFlush, callback);
	}
	
	virtual void update (core::util::Callback<> callback) override
	{
		queueAction (&Actions::doUpdate, callback);
	}
	
	virtual void partialUpdate (core::util::Callback<> callback) override
	{
		queueAction (&Actions::doPartialUpdate, callback);
	}
	
	virtual void checkMail (core::util::Callback<> callback) override
	{
		queueAction (&Actions::doCheckMail, callback);
	}
	
	virtual void processSendQueue (core::util::Callback<> callback) override
	{
		queueAction (&Actions::doProcessSendQueue, callback);
	}
	
	virtual void enableNotifications (const String &deviceId, core::util::Callback<> callback);
};

DECLARE_SMARTPTR(AsyncActions);

} /* namespace manager */
} /* namespace model::Mail */
} /* namespace model::Mailiverse */
#endif /* ACTIONS_H_ */
