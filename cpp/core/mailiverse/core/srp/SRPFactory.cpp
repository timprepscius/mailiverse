/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "SRPFactory.h"

using namespace mailiverse::core::srp;

SRPFactory *SRPFactory::instance = NULL;

SRPConstants SRPFactory::getDefaultConstants()
{
	return SRPConstants (
		BigInteger("0x115b8b692e0e045692cf280b436735c77a5a9e8a9e7ed56c965f87db5b2a2ece3"),
		BigInteger(2)
	);
}
