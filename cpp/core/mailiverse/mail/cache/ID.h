/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_ID_h__
#define __mailiverse_mail_cache_ID_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/core/Block.h"
#include "mailiverse/core/util/Random.h"
#include "mailiverse/Types.h"
#include "mailiverse/core/crypt/Base64.h"
#include "mailiverse/core/crypt/Base16.h"
#include <string>
#include <vector>
#include <assert.h>

namespace mailiverse {
namespace mail {
namespace cache {

class ID
{
public:
	static const int PartSize=6;
	static const int VERSION=1;
	
	static const ID NONE;
	static core::util::Random rand;

protected:
	core::Block value;

	ID (const core::Block &_value) :
		value(_value)
	{
	}

public:
	ID () {}
	virtual ~ID() {}
	
	static ID random ();
	static ID fromLong(long value);
	
	String toFileSystemSafe () const
	{
		return core::crypt::Base16::encode(value);
	}
	
	static ID fromFileSystemSafe (const String &value)
	{
		return ID(core::crypt::Base16::decode(value));
	}
	
	core::Block serialize () const
	{
		core::Block b;
		b.push_back(VERSION);
		b.insert(b.end(), value.begin(), value.end());
		return b;
	}
	
	static ID deserialize (const core::Block &b)
	{
		core::Block::Atom version = b[0];
		core::Block a;
		a.assign(b.begin()+1, b.end());
		
		return ID(a);
	}

	static ID combine (const ID &lhs, const ID &rhs)
	{
		core::Block combined;
		combined.insert(combined.end(), lhs.value.begin(), lhs.value.end());
		combined.push_back(0);
		combined.insert(combined.end(), rhs.value.begin(), rhs.value.end());

		return ID(combined);
	}
	
	ID left () const
	{
		core::Block::const_iterator i = std::find(value.begin(), value.end(), (core::Block::Atom)0);
		if (i!=value.end())
			return ID(core::Block(&*value.begin(), &*i));
			
		return *this;
	}

	ID right() const
	{
		core::Block::const_iterator i = std::find(value.begin(), value.end(), 0);
		if (i!=value.end())
			return ID(core::Block(&*(i+1), &*(value.end())));
			
		return *this;
	}

	std::string str () const
	{
		return debug();
	}
	
	std::string debug () const
	{
		return core::crypt::Base16::encode(value);
	}
	
	int size () const
	{
		return value.size();
	}

	bool operator <(const ID &rhs) const
	{
		return value < rhs.value;
	}

	bool operator ==(const ID &rhs) const
	{
		return value == rhs.value;
	}
	
	bool operator !=(const ID &rhs) const
	{
		return value != rhs.value;
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHEKEY_H_ */
