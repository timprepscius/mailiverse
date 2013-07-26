/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __Utilities_Algorithm_h__
#define __Utilities_Algorithm_h__

#include <algorithm>

namespace mailiverse {
namespace utilities {

template <typename T>
struct ComparatorDefault
{
	typedef T compare_type;

	bool operator () (const T &l, const T &r) const
	{
		return l < r;
	}
} ;

template <typename C>
struct ComparatorReverse
{
	typedef typename C::compare_type compare_type;
	C c;
	
	bool operator () (const compare_type &l, const compare_type &r) const
	{
		return c(r, l);
	}
} ;

struct ComparatorStr
{
	typedef char *compare_type;
	
	bool operator () (const compare_type l, const compare_type r) const
	{
		return strcmp(l, r);
	}
} ;

struct ComparatorStrLexicographical
{
	typedef char *compare_type;
	
	bool operator () (const char *l, const char *r) const
	{
		const char *ls = l, *le = ls + strlen(l), *rs = r, *re = rs + strlen(r);
		bool result = std::lexicographical_compare(ls, le, rs, re);
		
		return result;
	}
} ;

template <typename T>
struct ComparatorLexicographical 
{
	typedef T compare_type;
	
	bool operator () (const T &l, const T &r) const
	{
		const char *ls = l.data(), *le = ls + l.size(), *rs = r.data(), *re = rs + r.size();
		bool result = std::lexicographical_compare(ls, le, rs, re);
		
		return result;
	}
} ;

template <typename T>
struct ComparatorLexicographicalCaseInsensitive
{
	typedef T compare_type;

	static bool insensitiveComp (char c1, char c2) { return tolower(c1)<tolower(c2); }
	
	bool operator () (const T &l, const T &r) const
	{
		const char *ls = l.data(), *le = ls + l.size(), *rs = r.data(), *re = rs + r.size();
		bool result = std::lexicographical_compare(ls, le, rs, re, insensitiveComp);
		
		return result;
	}
} ;


template <typename T, typename C=ComparatorDefault<T> >
struct ComparatorPointer
{
	typedef T *compare_type;
	C c;
	
	bool operator () (const T *l, const T *r) const
	{
		return c(*l, *r);
	}
} ;

template <typename T, typename C=ComparatorDefault<typename T::first_type> >
struct ComparatorPairFirst
{
	typedef T compare_type;
	C c;

	bool operator () (const T &l, const T &r) const
	{
		return c(l.first, r.first);
	}
} ;

template <typename T, typename C=ComparatorDefault<typename T::second_type> >
struct ComparatorPairSecond
{
	typedef T compare_type;
	C c;

	bool operator () (const T &l, const T &r) const
	{
		return c(l.second, r.second);
	}
} ;

template <typename T, typename V>
void removeByFirst (T &l, const V &v)
{
	for (typename T::iterator i=l.begin(); i!=l.end(); ++i)
	{
		if (i->first == v)
		{
			l.erase(i);
			return;
		}
	}
}

template <typename T, typename V>
typename T::iterator findByFirst (T &l, const V &v)
{
	for (typename T::iterator i=l.begin(); i!=l.end(); ++i)
	{
		if (i->first == v)
		{
			return i;
		}
	}
	
	return l.end();
}

template <typename T, typename V>
bool containsByFirst (T &l, const V &v)
{
	for (typename T::iterator i=l.begin(); i!=l.end(); ++i)
	{
		if (i->first == v)
		{
			return true;
		}
	}
	return false;
}

std::string simpleDateFormat (const std::string &format, long long date);

} // namespcae
} // namespcae

#endif
