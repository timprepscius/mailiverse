/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "SRPConstants.h"
#include "SRPUtils.h"

using namespace mailiverse::core::srp;

SRPConstants::SRPConstants(const Botan::BigInt &largePrime, const Botan::BigInt &primitiveRoot)
{
	SRPUtils::validateConstants(largePrime, primitiveRoot);

	largePrime_N = largePrime;
	primitiveRoot_g = primitiveRoot;
	srp6Multiplier_k = SRPUtils::hash(SRPUtils::combine(largePrime_N, primitiveRoot_g));
}

