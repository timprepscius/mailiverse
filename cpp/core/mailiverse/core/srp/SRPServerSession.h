/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPSessionSession_h__
#define __mailiverse_core_srp_SRPSessionSession_h__

#include "../Types.h"
#include "SRPConstants.h"
#include "SRPUtils.h"
#include "mailiverse/utilities/SmartPtr.h"

namespace mailiverse {
namespace core {
namespace srp {

/**
 * Manages a server SRP session
 * 
 * <p>
 * Released into the public domain
 *
 * @author Jordan Zimmerman - jordan@jordanzimmerman.com
 * @see SRPFactory Full Documentation
 * @version 1.4 Make sure safeguards are checked: abort if A == 0 (mod N) or u == 0 - 2/27/07
 * @version 1.3 Updated to use the SRP-6 spec 2/21/07
 * @version 1.2
 */
class SRPServerSession
{
private:
	SRPConstants 		fConstants;
	SRPVerifier 		fVerifier;
	BigInteger 			fRandom_b;
	BigInteger 			fSRP6_u;
	BigInteger 			fPublicKey_A;
	BigInteger 			fPublicKey_B;
	BigInteger 			fCommonValue_S;
	Key					fSessionKey_K;
	BigInteger 			fEvidenceValue_M1;

public:
	/**
	 * @param constants constants to use
	 * @param verifier the verifier as returned from {@link SRPFactory#makeVerifier(byte[])}
	 */
	SRPServerSession(SRPConstants constants, SRPVerifier verifier) :
		fConstants(constants),
		fVerifier(verifier)
	{
		fRandom_b = SRPUtils::random(fConstants);

//		fSRP6_u = null;
//		fPublicKey_A = null;
//		fCommonValue_S = null;
//		fEvidenceValue_M1 = null;
//		fSessionKey_K = null;

		// B = 3v + g^b
		fPublicKey_B =
			fVerifier.verifier_v * constants.srp6Multiplier_k +  
			modPow(fConstants.primitiveRoot_g, fRandom_b, fConstants.largePrime_N);
	}

	/**
	 * When the client sends the public key (value A in the docs) call this method to store the value
	 *
	 * @param publicKey_A A
	 * @throws SRPAuthenticationFailedException if A is invalid
	 */
	void setClientPublicKey_A(const BigInteger &publicKey_A) throws_ (Exception)
	{
		if ( publicKey_A % fConstants.largePrime_N == 0 )
		{
			throw Exception("A%N == 0");
		}

		fPublicKey_A = publicKey_A;
		fSRP6_u = SRPUtils::calc_u(fPublicKey_A, fPublicKey_B);
		if ( fSRP6_u % fConstants.largePrime_N == 0 )
		{
			throw Exception("u%N == 0");
		}
	}

	/**
	 * Returns the public key that should be sent to the client (value B in the docs).
	 *
	 * @return B
	 */
	const BigInteger &getPublicKey_B()
	{
		return fPublicKey_B;
	}

	/**
	 * Call to calculate the common session key (S/K in the docs)
	 */
	void computeCommonValue_S()
	{
		if ( fPublicKey_A == 0 )
		{
			throw new Exception("setClientPublicKey_A() has not been called yet.");
		}

		// S = (A sum v^u)^b
		fCommonValue_S = 
			fPublicKey_A * (
				modPow(
					modPow(fVerifier.verifier_v, fSRP6_u, fConstants.largePrime_N),
					fRandom_b, 
					fConstants.largePrime_N
				)
			);
			
		fEvidenceValue_M1 = SRPUtils::calcM1(fPublicKey_A, fPublicKey_B, fCommonValue_S);

		// the MD5 output is the same as the AES key length
		fSessionKey_K = SRPUtils::hashToBytesForAES(fCommonValue_S);
	}

	/**
	 * When M(1) is received from the client, call this method to validate it
	 *
	 * @param evidenceValueFromClient_M1 M(1) as recevied from the client
	 * @throws SRPAuthenticationFailedException if M(1) is incorrect
	 */
	void validateClientEvidenceValue_M1(const BigInteger &evidenceValueFromClient_M1) throws_ (Exception)
	{
		if ( fEvidenceValue_M1 == 0 )
		{
			throw Exception("computeCommonValue_S() has not been called yet.");
		}

		if ( fEvidenceValue_M1 != evidenceValueFromClient_M1 )
		{
			throw Exception("M(1) incorrect");
		}
	}

	/**
	 * Return the value M(2) that should be sent to the client
	 *
	 * @return M(2)
	 */
	BigInteger getEvidenceValue_M2()
	{
		if ( fEvidenceValue_M1 == 0 )
		{
			throw Exception("computeCommonValue_S() has not been called yet.");
		}

		return SRPUtils::calcM2(fPublicKey_A, fEvidenceValue_M1, fCommonValue_S);
	}

	/**
	 * Returns the session common value which is the pre-hashed version of K
	 *
	 * @return common value
	 */
	const BigInteger &getSessionCommonValue()
	{
		return fCommonValue_S;
	}

	/**
	 * The 16 byte session key suitable for encryption
	 *
	 * @return session key - K
	 */
	const Key &getSessionKey_K()
	{
		return fSessionKey_K;
	}

	const SRPConstants &getConstants()
	{
		return fConstants;
	}

	const SRPVerifier &getVerifier()
	{
		return fVerifier;
	}
} ;

DECLARE_SMARTPTR(SRPServerSession);

} // namespace
} // namespace
} // namespace

#endif
