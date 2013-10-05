package com.jordanzimmerman;     

import java.math.BigInteger;

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
public class SRPServerSession
{
	/**
	 * @param constants constants to use
	 * @param verifier the verifier as returned from {@link SRPFactory#makeVerifier(byte[])}
	 */
	public SRPServerSession(SRPConstants constants, SRPVerifier verifier)
	{
		fConstants = constants;
		fVerifier = verifier;
		fRandom_b = SRPUtils.random(fConstants);
		fSRP6_u = null;
		fPublicKey_A = null;
		fCommonValue_S = null;
		fEvidenceValue_M1 = null;
		fSessionKey_K = null;

		// B = 3v + g^b
		fPublicKey_B = fVerifier.verifier_v.multiply(constants.srp6Multiplier_k).add(fConstants.primitiveRoot_g.modPow(fRandom_b, fConstants.largePrime_N));
	}

	/**
	 * When the client sends the public key (value A in the docs) call this method to store the value
	 *
	 * @param publicKey_A A
	 * @throws SRPAuthenticationFailedException if A is invalid
	 */
	public void				setClientPublicKey_A(BigInteger publicKey_A) throws SRPAuthenticationFailedException
	{
		if ( publicKey_A.mod(fConstants.largePrime_N).equals(BigInteger.ZERO) )
		{
			throw new SRPAuthenticationFailedException("A%N == 0");
		}

		fPublicKey_A = publicKey_A;
		fSRP6_u = SRPUtils.calc_u(fPublicKey_A, fPublicKey_B);
		if ( fSRP6_u.mod(fConstants.largePrime_N).equals(BigInteger.ZERO) )
		{
			throw new SRPAuthenticationFailedException("u%N == 0");
		}
	}

	public void setClientPublicKey_A(byte[] publicKey)  throws SRPAuthenticationFailedException
	{ setClientPublicKey_A(new BigInteger(publicKey)); }
	
	/**
	 * Returns the public key that should be sent to the client (value B in the docs).
	 *
	 * @return B
	 */
	public BigInteger		getPublicKey_B()
	{
		return fPublicKey_B;
	}

	/**
	 * Call to calculate the common session key (S/K in the docs)
	 */
	public void				computeCommonValue_S()
	{
		if ( fPublicKey_A == null )
		{
			throw new IllegalStateException("setClientPublicKey_A() has not been called yet.");
		}

		fCommonValue_S = fPublicKey_A.multiply(fVerifier.verifier_v.modPow(fSRP6_u, fConstants.largePrime_N)).modPow(fRandom_b, fConstants.largePrime_N);
		fEvidenceValue_M1 = SRPUtils.calcM1(fPublicKey_A, fPublicKey_B, fCommonValue_S);

		// the MD5 output is the same as the AES key length
		fSessionKey_K = SRPUtils.hashToBytesMD5(fCommonValue_S);
	}

	/**
	 * When M(1) is received from the client, call this method to validate it
	 *
	 * @param evidenceValueFromClient_M1 M(1) as recevied from the client
	 * @throws SRPAuthenticationFailedException if M(1) is incorrect
	 */
	public void			validateClientEvidenceValue_M1(BigInteger evidenceValueFromClient_M1) throws SRPAuthenticationFailedException
	{
		if ( fEvidenceValue_M1 == null )
		{
			throw new IllegalStateException("computeCommonValue_S() has not been called yet.");
		}

		if ( !fEvidenceValue_M1.equals(evidenceValueFromClient_M1) )
		{
			throw new SRPAuthenticationFailedException("M(1) incorrect");
		}
	}

	public void validateClientEvidenceValue_M1(byte[] evidenceValueFromClient_M1)  throws SRPAuthenticationFailedException
	{ validateClientEvidenceValue_M1(new BigInteger(evidenceValueFromClient_M1)); }

	/**
	 * Return the value M(2) that should be sent to the client
	 *
	 * @return M(2)
	 */
	public BigInteger		getEvidenceValue_M2()
	{
		if ( fEvidenceValue_M1 == null )
		{
			throw new IllegalStateException("computeCommonValue_S() has not been called yet.");
		}

		return SRPUtils.calcM2(fPublicKey_A, fEvidenceValue_M1, fCommonValue_S);
	}

	/**
	 * Returns the session common value which is the pre-hashed version of K
	 *
	 * @return common value
	 */
	public BigInteger 	getSessionCommonValue()
	{
		return fCommonValue_S;
	}

	/**
	 * The 16 byte session key suitable for encryption
	 *
	 * @return session key - K
	 */
	public byte[] 		getSessionKey_K()
	{
		return fSessionKey_K;
	}

	SRPConstants		getConstants()
	{
		return fConstants;
	}

	public SRPVerifier			getVerifier()
	{
		return fVerifier;
	}

	private SRPConstants 		fConstants;
	private SRPVerifier 		fVerifier;
	private BigInteger 			fRandom_b;
	private BigInteger 			fSRP6_u;
	private BigInteger 			fPublicKey_A;
	private BigInteger 			fPublicKey_B;
	private BigInteger 			fCommonValue_S;
	private byte[]	 			fSessionKey_K;
	private BigInteger 			fEvidenceValue_M1;
}
