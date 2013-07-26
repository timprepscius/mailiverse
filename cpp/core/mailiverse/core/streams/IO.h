/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_streams_IO_h__
#define __mailiverse_core_streams_IO_h__

#include <iostream>
#include "mailiverse/core/Types.h"
#include <vector>
#include <zlib.h>

#include <list>
#include <vector>
#include <map>
#include <set>

namespace mailiverse {
namespace core {
namespace streams {

inline std::string singleToDoubleQuote (const std::string &s)
{
	std::string copy = s;
	std::replace( copy.begin(), copy.end(), '\'', '"');
	return copy;
}

inline void swap (char *buffer, int size)
{
	int i, j;
	for (i = 0, j = size-1 ; i < size/2; ++i, --j)
	{
		char switcheroo = buffer[i];
		buffer[i] = buffer[j];
		buffer[j] = switcheroo;
	}
}

template<typename T>
inline int read (std::istream &is, T &v)
{
	is.read((char *)&v, sizeof(T));
	if (is.gcount() != sizeof(T))
		throw Exception("Read failure");
		
	swap((char *)&v, sizeof(T));
	return sizeof(T);
}

template<typename T>
inline int write (std::ostream &os, const T &v)
{
	char buffer[sizeof(T)];
	memcpy (buffer, (char *)&v, sizeof (T));
	swap (buffer, sizeof (T));
	
	os.write(buffer, sizeof(T));
	return sizeof(T);
}

template<>
inline int read (std::istream &is, std::string &v)
{
	int readSize = 0;
	int size;
	readSize += read(is, size);
	
	v.resize(size);
	is.read((char *)v.data(), size);
	readSize += size;
	
	return readSize;
}

template<>
inline int write (std::ostream &os, const std::string &v)
{
	int writeSize = 0;
	
	int size = v.size();
	writeSize += write(os, size);
	
	os.write ((char *)v.data(), size);
	writeSize += size;
	
	return writeSize;
}

inline int write (std::ostream &os, const char *data, int size)
{
	os.write(data, size);
	return size;
}

//---------------------------------------------------------

template<typename F, typename S>
inline int read (std::istream &is, std::pair<F,S> &p)
{
	int readSize = 0;
	readSize += read(is, p.first);
	readSize += read(is, p.second);
	
	return readSize;
}

template<typename F, typename S>
inline int write (std::ostream &is, const std::pair<F,S> &p)
{
	int writeSize = 0;
	writeSize += write(is, p.first);
	writeSize += write(is, p.second);
	
	return writeSize;
}

//---------------------------------------------------------

template<typename C>
inline int readCollection (std::istream &is, C &c);

template<typename V>
inline int read (std::istream &is, std::list<V> &c)
{
	return readCollection(is, c);
}

template<typename V>
inline int read (std::istream &is, std::vector<V> &c)
{
	int readSize=0;
	
	int num;
	readSize += read(is, num);
	c.reserve(num);
	
	for (int i=0; i<num; ++i)
	{
		V t;
		readSize += read(is, t);
		c.push_back(t);
	}
	
	return readSize;
}

template<>
inline int read (std::istream &is, Block &v)
{
	int readSize = 0;
	int size;
	readSize += read(is, size);
	
	if (size > BlockMaxReadSize)
		throw Exception("Read Block Size failure");
	
	v.resize(size);
	is.read((char *)v.data(), size);
	if (is.gcount() != size)
		throw Exception("Read failure");
		
	readSize += size;
	
	return readSize;
}


template<typename V>
inline int read (std::istream &is, std::set<V> &c)
{
	int readSize = 0;
	
	int num;
	readSize += read(is, num);
	
	for (int i=0; i<num; ++i)
	{
		V t;
		readSize += read(is, t);
		c.insert(t);
	}
	
	return readSize;
}

template<typename K, typename V>
inline int read (std::istream &is, std::map<K, V> &c)
{
	int readSize = 0;
	int num;
	readSize += read(is, num);
	
	for (int i=0; i<num; ++i)
	{
		K k;
		V v;
		readSize += read(is, k);
		readSize += read(is, v);
		c[k]=v;
	}
	
	return readSize;
}

template<typename C>
inline int writeCollection (std::ostream &os, const C &c);

template<typename V>
inline int write (std::ostream &os, const std::list<V> &c)
{
	return writeCollection(os, c);
}

template<typename V>
inline int write (std::ostream &os, const std::set<V> &c)
{
	return writeCollection(os, c);
}

template<typename V>
inline int write (std::ostream &os, const std::vector<V> &c)
{
	return writeCollection(os, c);
}

template<>
inline int write (std::ostream &os, const Block &v)
{
	int writeSize = 0;
	int size = v.size();
	writeSize += write(os, size);
	
	os.write ((char *)v.data(), size);
	writeSize += size;
	return writeSize;
}


template<typename K, typename V>
inline int write (std::ostream &os, const std::map<K,V> &c)
{
	int writeSize = 0;
	int num = c.size();
	writeSize += write(os, num);
	
	for (typename std::map<K,V>::const_iterator i=c.begin(); i!=c.end(); ++i)
	{
		writeSize += write(os, i->first);
		writeSize += write(os, i->second);
	}
	
	return writeSize;
}	

//--------------------------------------------------------------------
	
template<typename C>
inline int readCollection (std::istream &is, C &c)
{
	int readSize = 0;
	int num;
	readSize += read(is, num);
	
	for (int i=0; i<num; ++i)
	{
		typename C::value_type t;
		readSize += read(is, t);
		c.push_back(t);
	}
	
	return readSize;
}

template<typename C>
inline int writeCollection (std::ostream &os, const C &c)
{
	int writeSize = 0;
	int num = c.size();
	writeSize += write(os, num);
	
	for (typename C::const_iterator i=c.begin(); i!=c.end(); ++i)
		writeSize += write(os, *i);
		
	return writeSize;
}


} // namespace
} // namespace
} // namespace

#endif