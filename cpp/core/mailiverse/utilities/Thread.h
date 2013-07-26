/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __Utilities_Threads_Thread_h__
#define __Utilities_Threads_Thread_h__

#include <boost/thread/thread.hpp>
#include "Functions.h"
#include "Log.h"

namespace mailiverse {
namespace utilities {

typedef ::boost::thread *Thread;

typedef void (*ThreadFunction)(void *);

int __onThreadStart__();
void __onThreadEnd__(int);

template<typename T>
void startThread (ThreadFunction func, const T&param)
{
	int idv = __onThreadStart__();

	try
	{
		func(param);
	}
	catch (Exception &e)
	{
		LogDebug(mailiverse::utilities::thread, "caught exception " << e.what() << " at bottom of thread ");
		throw e;
	}
	catch (...)
	{
		__onThreadEnd__(idv);
		throw;
	}
	
	__onThreadEnd__(idv);
}

template <typename T>
Thread create (ThreadFunction func, const T& param)
{
	return new boost::thread (bind(&startThread<T>, func, param));
}

inline void wait (Thread thread)
{
	if (thread)
	{
		thread->join();
		delete thread;
	}
}

} // namespace Threads 
} // namespace Utilities 

#endif
