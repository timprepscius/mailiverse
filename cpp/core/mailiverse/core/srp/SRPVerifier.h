/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPVerifier_h__
#define __mailiverse_core_srp_SRPVerifier_h__

#include "mailiverse/core/Types.h"

namespace mailiverse {
namespace core {
namespace srp {

class SRPVerifier
{
public:
	SRPVerifier(const BigInteger &_v, const BigInteger &_s) :
		verifier_v(_v),
		salt_s(_s)
	{
	}

	const BigInteger verifier_v, salt_s;
} ;

} // namespace
} // namespace
} // namespace

#endif