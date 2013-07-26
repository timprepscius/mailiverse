/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __mailiverse_utilities_Monitor_h__
#define __mailiverse_utilities_Monitor_h__

#include <boost/thread/recursive_mutex.hpp>
#include <boost/thread/mutex.hpp>
#include <boost/thread/condition.hpp>

namespace mailiverse {
namespace utilities {

/**
 * Monitor gives write/read access to an object.
 *
 * Use:
 * Monitor<int> monitor;
 * Monitor<int>::ReaderPtr reader = monitor->getReader();
 * int i = (*(*reader));
 *
 * Monitor<int>::WriterPtr writer = monitor->getWriter();
 * (*(*reader)) = 5;
 */
template<class T>
class Monitor
{
	public:
		class Reader
		{
			protected:
				const Monitor<T> *parent;

			public:
				Reader (const Monitor<T> &_parent)
				{
					this->parent = &_parent;
					parent->beginReading();
				}

				virtual ~Reader()
				{
					parent->endReading();
					parent = NULL;
				}

				const T *operator ->() const
				{
					return parent->access();
				}

				const T &operator *() const
				{	
					return *(parent->access());
				}
		} ;

		class Writer
		{
			protected:
				Monitor<T> *parent;
				boost::recursive_mutex::scoped_lock writeLock;

			public:
				Writer (Monitor<T> &_parent) :
					writeLock (_parent.writeControl)
				{
					this->parent = &_parent;
					parent->beginWriting();
				}

				virtual ~Writer ()
				{
					parent->endWriting();
					parent = NULL;
				}

				T *operator ->()
				{
					return parent->access();
				}

				T &operator *()
				{
					return *(parent->access());
				}
		} ;

	protected:
		T *object;

	public:
		/**
		 * creates a monitor around a given object, that defaults to a new instance
		 * of the template class parameter.
		 * The given object must be dynamically allocated with "new"
		 */
		Monitor (T *t=new T())
		{
			object = t;
			isReading=0;
			signaled = false;
		}

		virtual ~Monitor ()
		{
			delete object;
		}

		void wait () const
		{
			if (signaled) return;
			boost::mutex localMutex;
			{
				boost::mutex::scoped_lock localLock(localMutex);
				nct()->changed.wait (localLock);
			}
		}

		void signal ()
		{
			signaled = true;
			changed.notify_all();
		}

		void reset ()
		{
			signaled = false;
		}
		
		T *bypass ()
		{
			return access();
		}
		
	//==========================================================================

	protected:
		/**
		 * the mutex surrounding the object for writing.  Only one may
		 * write at one time
		 */
		boost::recursive_mutex writeControl;

		/**
		 * the number of threads currently reading from the object
		 */
		volatile unsigned int isReading;

		boost::mutex readControl;

		/**
		 * pulsed when the number of threads currently reading the object
		 * falls to zero, it signals threads waiting to write unblock
		 */
		boost::condition noneReading;

		/**
		 * 
		 */
		boost::condition changed;

		bool signaled;

		/**
		 * a helper method designed to return a non-const this pointer
		 */
		Monitor<T> *nct() const { return const_cast< Monitor<T> *>(this); }

		virtual T *access () const
		{
			return nct()->object;
		}

	protected:
		friend class Reader;
		friend class Writer;
		friend class EmptyMonitorNoRTTI;

		/**
		 * gains write control of the object
		 */
		void beginWriting ()
		{
			boost::mutex::scoped_lock readLock (readControl);

			while (isReading>0)
			{
				noneReading.wait(readLock);
			}
		}

		/**
		 * release write control of the object
		 */
		void endWriting ()
		{
		}

		/**
		 * gains read access to the object
		 */
		void beginReading () const
		{
			boost::recursive_mutex::scoped_lock writeLock(nct()->writeControl);
			{
				boost::mutex::scoped_lock readLock(nct()->readControl);
				nct()->isReading++;
			}
		}

		/**
		 * release read access, possibly allow another thread to write to the object
		 */
		void endReading () const
		{
			{
				boost::mutex::scoped_lock readLock(nct()->readControl);
				nct()->isReading--;
			}

			nct()->noneReading.notify_all();
		}
} ;

//=============================================================================

class EmptyMonitorType {};

/**
 * a monitor that does not reference a data type, merely providing begin and end
 * read/write methods, useful for monitoring a this pointer
 */
typedef Monitor<EmptyMonitorType> EmptyMonitor;

} // namespace utilities
} // namespace mailiverse

#endif
