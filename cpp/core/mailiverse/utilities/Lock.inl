/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


inline void Mutex::lock () const
{
	pthread_mutex_lock ((pthread_mutex_t *)&handle);
}

inline void Mutex::unlock () const
{
	pthread_mutex_unlock ((pthread_mutex_t *)&handle);
}

inline Mutex::Mutex ()
{
	pthread_mutexattr_init(&attribute);
	pthread_mutexattr_settype(&attribute, PTHREAD_MUTEX_RECURSIVE);
	pthread_mutex_init(&handle, &attribute);
}

inline Mutex::~Mutex ()
{
	pthread_mutex_destroy(&handle);
	pthread_mutexattr_destroy(&attribute);
}
