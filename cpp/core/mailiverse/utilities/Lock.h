/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __Util_Lock_h__
#define __Util_Lock_h__

#include <pthread.h>

namespace mailiverse {
namespace utilities {

class Mutex {

private:
	friend class Lock;

	pthread_mutexattr_t attribute;
	pthread_mutex_t handle;
	void lock () const;
	void unlock () const;

public:
	Mutex ();
	~Mutex ();

};

class Lock
{

public:
	Lock (const Mutex &_m) :
		m(_m)
	{
		m.lock();
	}

	~Lock ()
	{
		m.unlock();
	}

private:
	const Mutex &m;
};

#include "Lock.inl"

} // namespace 
} // namespace 

#endif
