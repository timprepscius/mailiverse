/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_base_BlockReader_h__
#define __mailiverse_core_base_BlockReader_h__

#include "Types.h"
#include "streams/IO.h"

#include <boost/iostreams/stream.hpp>
#include <boost/iostreams/device/array.hpp>

namespace mailiverse {
namespace core {

class BlockReader
{
protected:

	int _size;
	const Block::Atom *_begin, *_end;

	boost::iostreams::basic_array_source<char> source;
	boost::iostreams::stream<boost::iostreams::basic_array_source<char> > iss;
	
public:
	inline
	BlockReader (const Block &block) :
		_size(block.size()),
		_begin(block.data()),
		_end(_begin + _size),
		source((char *)_begin, _size),
		iss(source)
	{
	}
	
	inline
	BlockReader (const Block::Atom *begin, const Block::Atom *end):
		_size(end-begin),
		_begin(begin),
		_end(end),
		source((char *)_begin, _size),
		iss(source)
	{
		
	}
	
	~BlockReader ()
	{
	}
	
	template<typename T>
	int read (T &v)
	{
		return core::streams::read(iss, v);
	}
	
	inline int pos ()
	{
		int _pos = iss.tellg();
		return _pos;
	}
	
	inline const Block::Atom *begin () const
	{
		return _begin;
	}
	
	inline const Block::Atom *end () const
	{
		return _end;
	}
	
	inline int size () const
	{
		return _size;
	}
	
	inline int remaining ()
	{
		return size() - pos();
	}
	
	inline
	int read (char *data, int size)
	{
		iss.read(data, size);
		return iss.gcount();
	}	

	inline
	bool eof () const
	{
		int available = iss.rdbuf()->in_avail();
		return available == 0;
	}
} ;

class BlockWriter
{
protected:
	Block data;
	Block &block;
	
public:
	BlockWriter (Block &_block) :
		block(_block)
	{
	}
	
	BlockWriter () :
		block(data)
	{
	}
	
	template<typename T>
	int write (const T &v)
	{
		std::ostringstream oss(std::istringstream::binary);
		streams::write(oss, v);
		
		std::string str = oss.str();
		block.insert(block.end(), str.begin(), str.end());
		return str.size();
	}
	
	int write (char *data, int size)
	{
		block.insert(block.end(), data, data+size);
		return size;
	}
	
	const Block &getBlock () const
	{
		return block;
	}
} ;

} // namespace core
} // namespace mailiverse

#endif