/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Types_h__
#define __mailiverse_mail_model_Types_h__

#include "mailiverse/utilities/SmartPtr.h"
#include <set>
#include <list>
#include <vector>
#include <map>

namespace mailiverse {

class Date
{
public:
	typedef long long ValueType;

	static const Date None;

protected:
	ValueType time;
	
	Date () : time(0) {}
	
public:

	explicit
	Date (ValueType _time) :
		time(_time) {}

	static Date now ()
	{
		return Date(((ValueType)::time(NULL)) * 1000);
	}

	static Date fromTimeT(time_t t)
	{
		return Date(((ValueType)t)*1000);
	}

	ValueType getTime () const
	{
		return time;
	}

	bool after(const Date &date)
	{
		return time > date.time;
	}

	bool operator <(const Date &rhs) const
	{
		return time < rhs.time;
	}
	
	bool operator ==(const Date &rhs) const
	{
		return time == rhs.time;
	}
	
	bool operator !=(const Date &rhs) const
	{
		return time != rhs.time;
	}
};

DECLARE_SMARTPTR(Date);

typedef std::string String;
DECLARE_SMARTPTR(String);

template<typename T>
class Set : public std::set<T>
{
public:
	bool contains (const T &t) const
	{
		return this->find(t)!=this->end();
	}
	
	template<typename L>
	void addAll (const L &l)
	{
		for (auto &i : l)
			add(i);
	}

	void add (const T &t)
	{
		this->insert(t);
	}

	template<typename V>
	void remove (const V &t)
	{
		auto i = std::find(this->begin(), this->end(), t);
		if (i != this->end())
			this->erase(i);
	}
	
	template<typename V>
	int getIndexOf (const V &t) const
	{
		int I=0;
		for (auto &i : *this)
		{
			if (i == t)
				return I;
				
			I++;
		}
		
		return -1;
	}
} ;

template<typename T>
class List : public std::list<T>
{
public:
	bool isEmpty () const
	{
		return this->empty();
	}

	bool contains(const T &t) const
	{
		return std::find(this->begin(), this->end(), t)!=this->end();
	}

	void add (const T &t)
	{
		this->push_back(t);
	}

	void remove (const T &t)
	{
		auto i = std::find(this->begin(), this->end(), t);
		if (i != this->end())
			this->erase(i);
	}

	template<typename L>
	void addAll (const L &l)
	{
		for (auto &i : l)
			add(i);
	}

	List<T> subList (int from, int to)
	{
		List<T> l;
		int I = 0;
		for (auto &i : *this)
		{
			if (I >= to)
				break;
				
			if (I >= from)
				l.add(i);
				
			I++;
		}

		return l;
	}

	template<typename S>
	void sort (const S &sorter)
	{
		std::sort(this->begin(), this->end(), sorter);
	}
	
	T &get(int index)
	{
		int I=0;
		typename std::list<T>::iterator i;
		for (i = this->begin(); i!=this->end(); ++i)
			if (I++ == index)
				break;
				
		return *i;
	}

	const T &get(int index) const
	{
		int I=0;
		typename std::list<T>::const_iterator i;
		for (i = this->begin(); i!=this->end(); ++i)
			if (I++ == index)
				break;
				
		return *i;
	}

} ;

template<typename T>
class Vector : public std::vector<T>
{
public:
	bool isEmpty () const
	{
		return this->empty();
	}

	void removeIndex (int i)
	{
		this->erase(this->begin() + i);
	}

	const T &get(int i) const
	{
		return (*this)[i];
	}

	T &get(int i)
	{
		return (*this)[i];
	}

	bool contains(const T &t) const
	{
		return std::find(this->begin(), this->end(), t)!=this->end();
	}

	void add (const T &t)
	{
		this->push_back(t);
	}

	void remove (const T &t)
	{
		auto i = std::find(this->begin(), this->end(), t);
		if (i != this->end())
			this->erase(i);
	}

	template<typename L>
	void addAll (const L &l)
	{
		for (auto &i : l)
			this->add(i);
	}

	Vector<T> subList (int from, int to)
	{
		to = std::min(to,(int)this->size());
		
		Vector<T> l;
		l.assign(this->begin()+from, this->begin()+to);

		return l;
	}
	
	template<typename S>
	void addSorted (const T &t, const S &sorter, bool allowDuplicates=false)
	{
		auto i = std::lower_bound(this->begin(), this->end(), sorter);
		if (i !=this->end())
		{
			if (allowDuplicates || sorter(t,*i))
			{
				this->insert(i, t);
			}
		}
	}

	template<typename S>
	void sort (const S &sorter)
	{
		std::sort(this->begin(), this->end(), sorter);
	}
} ;



template<typename K, typename V>
class Map : public std::map<K,V>
{
public:
	bool isEmpty () const
	{
		return this->empty();
	}

	bool containsKey(const K &k) const
	{
		return this->find(k) != this->end();
	}

	void put (const K &k, const V &v)
	{
		remove(k);
		this->insert(std::make_pair(k,v));
	}

	void remove (const K &k)
	{
		this->erase(k);
	}

	const V &getr(const K &k) const
	{
		auto i = this->find(k);
		return i->second;
	}

	V &getr(const K &k)
	{
		auto i = this->find(k);
		return i->second;
	}

	V getv(const K &k) const
	{
		auto i = this->find(k);
		if (i != this->end())
			return i->second;

		return V();
	}
} ;

template<typename F, typename S>
class Pair : public std::pair<F,S>
{
public:
	Pair() {}
	Pair(const F &f, const S &s) : std::pair<F,S>(f,s) {}
} ;

} /* namespace mailiverse */

#endif /* TYPES_H_ */
