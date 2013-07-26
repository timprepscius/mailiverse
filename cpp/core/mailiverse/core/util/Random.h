/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_util_Random_h__
#define __mailiverse_core_util_Random_h__

#include "mailiverse/core/Block.h"

namespace mailiverse {
namespace core {
namespace util {

class Random
{
protected:
	void *_rng;
	
	void init ();

public:
	Random ();
	virtual ~Random();

	unsigned char nextByte();
	long nextLong();
	core::Block nextBytes(int size);
};

} // namespace
} // namespace
} // namespace

#endif
