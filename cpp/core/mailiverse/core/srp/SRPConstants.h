/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPConstants_h__
#define __mailiverse_core_srp_SRPConstants_h__

#include "mailiverse/core/Types.h"

namespace mailiverse {
namespace core {
namespace srp {

class SRPConstants
{
public:
	/**
	 * NOTE: this constructor validates the values passed via {@link SRPUtils#validateConstants(java.math.BigInteger,java.math.BigInteger)}
	 *
	 * @param largePrime a very large prime number
	 * @param primitiveRoot a primitive root that relates to the prime number.
	 */
	SRPConstants(const BigInteger &largePrime, const BigInteger &primitiveRoot);

	/**
	 * N
	 */
	BigInteger largePrime_N, primitiveRoot_g, srp6Multiplier_k;
} ;

} // namespace
} // namespace
} // namespace

#endif