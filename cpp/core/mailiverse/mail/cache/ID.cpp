/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "ID.h"
#include "mailiverse/core/util/Random.h"

namespace mailiverse {
namespace mail {
namespace cache {

core::util::Random ID::rand;
const ID ID::NONE = ID::fromLong(-1);

ID ID::random ()
{
	core::Block b;
	b.push_back(1);
	for (int i=0; i<PartSize; ++i)
	{
		core::Block::Atom a = 0;
		
		while (a==0)
			a = rand.nextByte();
			
		b.push_back(a);
	}
	
	return ID(b);
}

ID ID::fromLong(long value)
{
	core::Block b;
	
	// I really need to do this correctly
	if (value == -1)
		value = 0xFF;
	
	bool foundNonZero = false;
	for (int i=sizeof(long)-1; i>=0; --i)
	{
		core::Block::Atom a = value >> (i*8) & 0xFF;
		foundNonZero = foundNonZero || (a != 0);
		if (foundNonZero)
			b.push_back(a);
	}
	
	return ID(b);
}

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */
