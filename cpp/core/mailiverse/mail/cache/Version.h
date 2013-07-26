/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef VERSION_H_
#define VERSION_H_

#include "ID.h"

namespace mailiverse {
namespace mail {
namespace cache {

class Version
{
public:
	static core::util::Random rand;
	static const Version DELETED, NONE;
	typedef core::Block Type;

protected:
	Type value;

	Version(Type _value) : value(_value) {}

public:
	Version() {}

	Version(const Version &rhs) :
		value(rhs.value) 
	{}
	
	String str() const
	{
		return core::crypt::Base64::encode(value);
	}
	
	core::Block serialize()
	{
		return value;
	}
	
	static Version deserialize (const core::Block &block)
	{
		return Version(block);
	}

	bool operator <(const Version &rhs) const
	{
		return value < rhs.value;
	}

	bool operator ==(const Version &rhs) const
	{
		return value == rhs.value;
	}

	bool operator !=(const Version &rhs) const
	{
		return value != rhs.value;
	}
	
	static Version fromLong (long l);
	
	static Version random ()
	{
		Version version;
		do
		{
			version = Version::fromLong(rand.nextLong());
		}
		while (version==DELETED || version==NONE);

		return version;
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* VERSION_H_ */
