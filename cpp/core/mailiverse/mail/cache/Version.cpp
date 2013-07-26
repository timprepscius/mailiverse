/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Version.h"

namespace mailiverse {
namespace mail {
namespace cache {

core::util::Random Version::rand;
const Version Version::DELETED(Version::fromLong(-1));
const Version Version::NONE(Version::fromLong(0));

Version Version::fromLong(long value)
{
	core::Block b;
	
	bool foundNonZero = false;
	for (int i=sizeof(long)-1; i>=0; --i)
	{
		core::Block::Atom a = value >> (i*8) & 0xFF;
		foundNonZero = foundNonZero || (a != 0);
		if (foundNonZero)
			b.push_back(a);
	}
	
	return Version(b);
}


} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */
