/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorSeed_h__
#define __mailiverse_core_crypt_CryptorSeed_h__

#include "mailiverse/core/Types.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptorSeed 
{
protected:
	core::Block seed;
public:
	CryptorSeed (const core::Block &_seed) :
		seed(_seed)
	{}

	const core::Block &getSeed() const
	{
		return seed;
	}
} ;

DECLARE_SMARTPTR(CryptorSeed);

} // namespace crypt
} // namespace core
} // namespace mailiverse

#endif
