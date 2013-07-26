/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __Utilities_SmartPtr_h__
#define __Utilities_SmartPtr_h__

//#include <tr1/unordered_map>
#include "Lock.h"
#include <map>
#include <assert.h>
#include <map>
#include <vector>
#include <algorithm>
#include <iostream>
#include <sstream>
#include "FilledArray.h"
#include "Log.h"

#define LogDebugSmartPtr(x,y) 

namespace mailiverse {
namespace utilities {

class SmartPtrBase {

protected:
	typedef void *K;
	typedef int V;
	typedef void *W;

	typedef std::map<K, V> Items;
//	typedef std::tr1::unordered_map<K, V> Items;
	static Items items;

	typedef std::vector<W> WeakList;
	typedef std::map<K, WeakList > Weaks;
	static Weaks weaks;

	static Mutex m;
	
public:
	virtual ~SmartPtrBase() {}
	
	virtual void *access () const = 0;
	
	static std::string getInfoFor (K k) 
	{
		Lock l(m);
		std::ostringstream ss;
		if (items.find(k) != items.end())
			ss << items[k];
		else
			ss << "0";
		
		return ss.str();
	}
	
	static void printDebug ()
	{
		Lock l(m);
		for (auto &i : items)
		{
			LogDebug(mailiverse::utilities::SmartPtr, i.first << "\t" << i.second);
		}
	}
};

#define SMARTPTR_INFO(k) ::mailiverse::utilities::SmartPtrBase::getInfoFor(k)
#define SMARTPTR_DEBUG() ::mailiverse::utilities::SmartPtrBase::printDebug()

template<typename T>
class WeakRef : public SmartPtrBase
{
private:
	T *t;
	
	WeakRef (const WeakRef &);
	
public:
	WeakRef(T *_t) :
		t(_t)
	{
		Lock lock(m);
		
		weaks[t].push_back(this);
	}
	
	virtual ~WeakRef()
	{
		if (t != 0)
		{
			Lock l(m);
			WeakList &wl = weaks[t];
			wl.erase(std::find(wl.begin(), wl.end(), this));
		}
	}

	virtual void *access () const override
	{
		return t;
	}
		
	operator T *() const
	{
		return t;
	}

	T &operator *() const
	{
		return *t;
	}
	
	static void nullify (T *t)
	{
		if (t == 0)
			return;
			
		Lock lock(m);
			
		auto l = weaks.find(t);
		if (l == weaks.end())
			return;
			
		for (auto &i : l->second)
			((WeakRef<T> *)i)->t = 0;
			
		weaks.erase(l);
	}
} ;

template<typename T>
class SmartPtr : public SmartPtrBase {

private:
	T *t;

public:

	SmartPtr (T *_t=NULL) : t(NULL)
	{
		LogDebugSmartPtr(mailiverse::utilities::SmartPtr, "SmartPtr " << this);

		set(_t);
	}

	SmartPtr (const SmartPtr &rhs) : t(NULL)
	{
		LogDebugSmartPtr(mailiverse::utilities::SmartPtr, "SmartPtr " << this);

		set(rhs.t);
	}

	virtual ~SmartPtr ()
	{
		LogDebugSmartPtr(mailiverse::utilities::SmartPtr, "~SmartPtr " << this);

		set(NULL);
	}

	SmartPtr &operator = (T *_t)
	{
		set(_t);
		return *this;
	}

	SmartPtr &operator = (const SmartPtr &rhs)
	{
		set(rhs.t);
		return *this;
	}

	virtual void *access () const override
	{
		return t;
	}
	
	operator T *() const
	{
		return t;
	}

	T &operator *() const
	{
		return *t;
	}

	T *operator -> () const
	{
		return t;
	}
	
	bool operator == (T *_t) const
	{
		return t == _t;
	}

	bool operator != (T *_t) const
	{
		return t != _t;
	}
	
	bool operator == (const SmartPtr &_t) const
	{
		return t == (T *)_t;
	}

	bool operator != (const SmartPtr &_t) const
	{
		return t != (T *)_t;
	}

	bool operator <(const SmartPtr &rhs) const
	{
		return t < rhs.t;
	}

private:
	void set (T *_t)
	{
		if (t == _t)
			return;

		T *deferredDelete = NULL;

		{
			LogDebugSmartPtr(mailiverse::utilities::SmartPtr, this << " setting t " << t << " to " << _t);

			Lock l(m);

			if (t != 0)
			{

				typename Items::iterator i = items.find(t);

				assert(i!=items.end());
				i->second--;
				if (i->second == 0)
				{
					items.erase (i);
					deferredDelete = t;
				}
			}

			t = _t;

			if (t != 0)
			{
				typename Items::iterator i = items.find(t);
				if (items.find(t) != items.end())
				{
					i->second++;
				}
				else
					items.insert(std::pair<T *, int>(t, 1));
			}
		}

		if (deferredDelete)
		{
			WeakRef<T>::nullify(deferredDelete);
			delete deferredDelete;
		}
	}
};

template<typename T>
class WeakPtr
{
private:
	SmartPtr<WeakRef<T>> t;
	
public:
	WeakPtr(T *_t)
	{
		t = new WeakRef<T>(_t);
	}
	
	WeakPtr()
	{
	}
	
	virtual ~WeakPtr () {}
	
	WeakPtr<T> &operator = (T *_t)
	{
		t = new WeakRef<T>(_t);
		return *this;
	}
	
	operator T *() const
	{
		return *t;
	}

	T &operator *() const
	{
		return **t;
	}

	T *operator -> () const
	{
		return *t;
	}
	
	bool operator == (T *_t) const
	{
		return *t == _t;
	}

	bool operator != (T *_t) const
	{
		return *t != _t;
	}	
} ;

#define DECLARE_SMARTPTR(x) typedef ::mailiverse::utilities::SmartPtr<x> x##Ptr;
#define DECLARE_WEAKPTR(x) typedef ::mailiverse::utilities::WeakPtr<x> Weak##x##Ptr;

} // namespace
} // namespace

#endif
