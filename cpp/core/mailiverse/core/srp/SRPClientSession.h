/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPClientSession_h__
#define __mailiverse_core_srp_SRPClientSession_h__

#include "mailiverse/core/Types.h"
#include "SRPConstants.h"
#include "SRPUtils.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace core {
namespace srp {

/**
 * Manages a client SRP session
 * <p>
 * Released into the public domain
 *
 * @author Jordan Zimmerman - jordan@jordanzimmerman.com
 * @see SRPFactory Full Documentation
 * @version 1.4 Make sure safeguards are checked: abort if B == 0 (mod N) or u == 0 - 2/27/07
 * @version 1.3 Updated to use the SRP-6 spec - 2/21/07
 * @version 1.2
 */
class SRPClientSession
{
friend class SRPClientSessionSerializer;

private:
	SRPConstants fConstants;
	Key fPassword;
	BigInteger fPrivateKey_x;
	BigInteger fRandom_a;
	BigInteger fPublicKey_A;
	BigInteger fCommonValue_S;
	Block fSessionKey_K;
	BigInteger fEvidenceValue_M1;

public:
	SRPClientSession(const SRPConstants &_fConstants) :
		fConstants(_fConstants)
	{
	}

	/**
	 * @param constants constants to use
	 * @param password password as passed to {@link SRPFactory#makeVerifier(byte[])} 
	 */
	SRPClientSession(const SRPConstants &constants, const Key &password) :
		fConstants(constants),
		fPassword(password)
	{
	}
	
	void setPassword (const Key &password)
	{
		fPassword = password;
	}

	/**
	 * Once the server sends the salt (value s in the docs), call this method to save the value
	 *
	 * @param salt salt from the server
	 */
	void setSalt_s(const BigInteger &salt) throws_ (Exception)
	{
		try
		{
			fPrivateKey_x = SRPUtils::makePrivateKey(fPassword, salt);
			fRandom_a = SRPUtils::random(fConstants);

	//		not needed
	//		fCommonValue_S = null;
	//		fEvidenceValue_M1 = null;
	//		fSessionKey_K = null;

			// A = g^a
			fPublicKey_A = modPow(fConstants.primitiveRoot_g, fRandom_a, fConstants.largePrime_N);
			LogDebug(mailiverse::core::srp::SRPClientSession, 
				"salt " << salt << std::endl <<
				"password " << toBigInteger(fPassword) << std::endl <<
				"fPrivateKey_x " << fPrivateKey_x << std::endl <<
				"fRandom_a " << fRandom_a << std::endl <<
				"fPublicKey_A " << fPublicKey_A
			);
		}
		catch (Exception &e)
		{
			throw e;
		}
		catch (std::exception &e)
		{
			throw Exception(e.what());
		}
	}

	/**
	 * Returns the public key (value A in the docs). This should be passed to the server
	 *
	 * @return A
	 */
	const BigInteger &getPublicKey_A()
	{
		return fPublicKey_A;
	}

	/**
	 * Call to save the public key (value B in the docs) when received from the server
	 *
	 * @param publicKey_B B
	 * @throws SRPAuthenticationFailedException if B is invalid
	 */
	void setServerPublicKey_B(const BigInteger &publicKey_B) throws_ (Exception)
	{
		try
		{
			if ( fPublicKey_A == 0 )
			{
				throw Exception("setSalt_s() has not been called yet.");
			}

			if ( publicKey_B % fConstants.largePrime_N == 0 )
			{
				throw Exception("B%N == 0");
			}

			BigInteger SRP6_u = SRPUtils::calc_u(fPublicKey_A, publicKey_B);
			if ( SRP6_u % fConstants.largePrime_N == 0  )
			{
				throw Exception("u%N == 0");
			}

			// S = (B - 3(g^x))^(a + ux)
			BigInteger three_g_pow_x = 
				fConstants.srp6Multiplier_k * (
					modPow(fConstants.primitiveRoot_g, fPrivateKey_x, fConstants.largePrime_N)
				);
				
			BigInteger B_minus_g_pow_x = publicKey_B - three_g_pow_x;
			BigInteger ux = SRP6_u * fPrivateKey_x;

/*			
			LogDebug(mailiverse::core::srp,
				 " B_minus_g_pow_x " << B_minus_g_pow_x << std::endl
				<< " fPrivateKey_x " << fPrivateKey_x << std::endl
				<< " SRP6_u " << SRP6_u << std::endl
				<< " ux " << ux << std::endl
				<< " fRandom_a + ux " << (fRandom_a + ux)
			); 
*/
				
			fCommonValue_S = 
				modPow(
					B_minus_g_pow_x, 
					fRandom_a + ux, 
					fConstants.largePrime_N
				) % fConstants.largePrime_N;
				
			fEvidenceValue_M1 = SRPUtils::calcM1(fPublicKey_A, publicKey_B, fCommonValue_S);

			// the MD5 output is the same as the AES key length
			fSessionKey_K = SRPUtils::hashToBytesForAES(fCommonValue_S);
		}
		catch (Exception &e)
		{
			throw e;
		}
		catch (std::exception &e)
		{
			throw Exception(e.what());
		}
	}

	/**
	 * After the session key has been computed, use this method to return the evidence value to send to the server (value M[1] in the docs).
	 *
	 * @return M(1)
	 */
	const BigInteger &getEvidenceValue_M1()
	{
		if ( fEvidenceValue_M1 == 0 )
		{
			throw Exception("computeCommonValue_S() has not been called yet.");
		}

		return fEvidenceValue_M1;
	}

	/**
	 * When the server sends M(2), call this method to validate the number.
	 *
	 * @param evidenceValueFromServer_M2 M(2) from the server.
	 * @throws SRPAuthenticationFailedException if M(2) is incorrect
	 */
	void validateServerEvidenceValue_M2(const BigInteger &evidenceValueFromServer_M2) throws_ (Exception)
	{
		if ( fEvidenceValue_M1 == 0 )
		{
			throw Exception("computeCommonValue_S() has not been called yet.");
		}

		BigInteger M2 = SRPUtils::calcM2(fPublicKey_A, fEvidenceValue_M1, fCommonValue_S);
		
		LogDebug(mailiverse::core::srp::SRPClientSession, "calculated " << M2);
		
		if ( evidenceValueFromServer_M2 != M2) 
		{
			throw Exception("M(2) is incorrect");
		}
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
	const Block &getSessionKey_K()
	{
		return fSessionKey_K;
	}

	const SRPConstants &getConstants()
	{
		return fConstants;
	}

} ;

DECLARE_SMARTPTR(SRPClientSession);

} // namespace
} // namespace
} // namespace

#endif
