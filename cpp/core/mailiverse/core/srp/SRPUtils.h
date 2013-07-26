/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPUtils_h__
#define __mailiverse_core_srp_SRPUtils_h__

#include "mailiverse/core/Types.h"
#include <string>
#include <botan/botan.h>
#include <botan/bigint.h>
#include <botan/numthry.h>
#include <botan/auto_rng.h>
#include "SRPConstants.h"
#include "SRPVerifier.h"
#include "mailiverse/core/crypt/Hash.h"
#include <iostream>

#include <cmath>

namespace mailiverse {
namespace core {
namespace srp {

/**
 * Various utilities<br>
 *
 * Released into the public domain
 *
 * @author Jordan Zimmerman - jordan@jordanzimmerman.com
 * @see SRPFactory Full Documentation
 * @version 1.2 Updated to use the SRP-6 spec 2/21/07
 * @version 1.1
 */
class SRPUtils
{
public:
	/**
	 * Validates the given constants. Throws {@link IllegalArgumentException} if the values are not valid.
	 * NOTE: due to prime number calculations, this method can be slow.
	 *
	 * @param N large prime
	 * @param g primitve root of N
	 */
	static void validateConstants(const BigInteger &N, const BigInteger &g)
	{
		// Developed from "SRP JavaScript Demo" from http://srp.stanford.edu<p>

		Botan::AutoSeeded_RNG rng;

		if ( !Botan::check_prime(N, rng) )
		{
			throw Exception("isProbablePrime(10) failed for N");
		}

		BigInteger ONE(1);
		BigInteger TWO(2);
		BigInteger n_minus_one_div_2 = (N - ONE) / TWO;

		if ( !Botan::check_prime(n_minus_one_div_2, rng) )
		{
			throw Exception("(N-1)/2 is not prime for N");
		}

		if( (modPow(g,n_minus_one_div_2, N) + ONE) - N != 0)
		{
			throw Exception("Not a primitive root: g");
		}
	}

	/**
	 * Make a verifier. First, x is generated via x = H(s, P) where H is a hash() function, s is random salt, and P is the password.
	 * The verifier is then v = g^x 
	 *
	 * @param constants the constants to use
	 * @param password the password to process
	 * @return the verifier
	 */
	static SRPVerifier makeVerifier(const SRPConstants &constants, const Key &password)
	{
		BigInteger salt = random(constants);
		BigInteger x = makePrivateKey(password, salt);
		BigInteger v = modPow(constants.primitiveRoot_g, x, constants.largePrime_N);

		return SRPVerifier(v, salt);
	}

	/**
	 * Make a new private key via x = H(s, P) where H is a hash() function, s is random salt, and P is the password.
	 *
	 * @param password the password
	 * @param salt random salt
	 * @return the private key
	 */
	static BigInteger makePrivateKey(const Key &password, const BigInteger &salt)
	{
		BigInteger passwordInt = toBigInteger(password);
		BigInteger result = hash(combine(passwordInt, salt));
		
		return result;
	}

	/**
	 * Combine two integers into one. This method uses a novel combining method rather than simple concatenation. My assumption is
	 * that it will add an additional level of security as a malicious party would not be able to guess this method. The bytes from
	 * each value are interleaved in pairs. If the first value of the pair is odd, two bytes are taken from the second value. Any
	 * remaining bytes are appended at the end.
	 *
	 * @param a first value to combine
	 * @param b second value to combine
	 * @return combined value
	 */
	static BigInteger combine(const BigInteger &a, const BigInteger &b)
	{
		Block ba, bb, br;
		
		ba = toBlock(a);
		bb = toBlock(b);

		Block::iterator ia=ba.begin(); 
		Block::iterator ib=bb.begin();
		
		while ( ia != ba.end() && ib != bb.end() )
		{
			s8 abyte = *ia++;
			br.push_back(abyte);
			
			s8 bbyte = *ib++;
			br.push_back(bbyte);

			if ( ((abyte & 1) == 0) && ib!=bb.end() )
			{
				bbyte = *ib++;
				br.push_back(bbyte);
			}
		}

		while ( ia != ba.end() )
		{
			s8 x = *ia++;
			br.push_back(x);
		}

		while ( ib != bb.end() )
		{
			s8 x = *ib++;
			br.push_back(x);
		}
		
//		std::cerr << "combined " << a << " with " << b << " for " << toBigInteger(br) << std::endl;

		return toBigInteger(br);
	}

	/**
	 * hash a big int. Use SHA 256.
	 *
	 * @param i int to hash
	 * @return the hash
	 */
	static BigInteger hash(const BigInteger &i)
	{
		return toBigInteger(hashToBytes(i));
	}

	/**
	 * hash a big int. Use SHA 256.
	 *
	 * @param i int to hash
	 * @return the hash
	 */
	static Block hashToBytes(const BigInteger &i)
	{
		crypt::HashSha256 hash;
		return hash.generate(toBlock(i));
	}
	
	static Block hashToBytesForAES(const BigInteger &i)
	{
		return hashToBytes(i);
	}

	/**
	 * hash a big int. Use MD5.
	 *
	 * @param i int to hash
	 * @return the hash
	 */
	static Block hashToBytesMD5(BigInteger i)
	{
		crypt::HashMd5 hash;
		return hash.generate(toBlock(i));
	}

	/**
	 * Return a random number that satsifies: 1 < r < n
	 *
	 * @param constants constants to use
	 * @return the random number
	 */
	static BigInteger random(const SRPConstants &constants)
	{
		Botan::AutoSeeded_RNG rng;
		int 		numberOfBytes = (constants.largePrime_N.bits() + (constants.largePrime_N.bits() - 1)) / 8;
		Block b;
		b.resize(numberOfBytes);
		
		rng.randomize(b.data(), b.size());
		BigInteger i = toBigInteger(b);

		// random numbers must be: 1 < r < n
		BigInteger TWO(2);
		BigInteger max = constants.largePrime_N - TWO;
		return (i%max) + TWO;
	}

	/**
	 * Calculate M(1) - H(A, B, K)
	 *
	 * @param publicKey_A generated public key - A
	 * @param publicKey_B generated public key - B
	 * @param commonValue_S the session common value - S
	 * @return M(1)
	 */
	static BigInteger calcM1(
		const BigInteger &publicKey_A, 
		const BigInteger &publicKey_B, 
		const BigInteger &commonValue_S
	)
	{
		return hash(combine(combine(publicKey_A, publicKey_B), commonValue_S));
	}

	/**
	 * Calculate M(1) - H(A, M[1], K)
	 *
	 * @param publicKey_A generated public key - A
	 * @param evidenceValue_M1 generated hash - M(1)
	 * @param commonValue_S the session common value - S
	 * @return M(1)
	 */
	static BigInteger calcM2(
		const BigInteger &publicKey_A, 
		const BigInteger &evidenceValue_M1, 
		const BigInteger &commonValue_S
	)
	{
		return hash(combine(combine(publicKey_A, evidenceValue_M1), commonValue_S));
	}

	/**
	 * Return the SRP-6 version of u - H(A, B)
	 *
	 * @param A Public Key A
	 * @param B Public Key B
	 * @return u
	 */
	static BigInteger calc_u(const BigInteger &A, const BigInteger &B)
	{
		return hash(combine(A, B));
	}
} ;

} // namespace
} // namespace
} // namespace

#endif
