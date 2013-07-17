package com.jordanzimmerman;     

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.ByteBuffer;

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
	/**
	 * Validates the given constants. Throws {@link IllegalArgumentException} if the values are not valid.
	 * NOTE: due to prime number calculations, this method can be slow.
	 *
	 * @param N large prime
	 * @param g primitve root of N
	 */
	static void 		validateConstants(BigInteger N, BigInteger g)
	{
		// Developed from "SRP JavaScript Demo" from http://srp.stanford.edu<p>

		if ( !N.isProbablePrime(10) )
		{
			throw new IllegalArgumentException("isProbablePrime(10) failed for " + N.toString(16));
		}

		BigInteger 		n_minus_one_div_2 = N.subtract(BigInteger.ONE).divide(TWO);

		if ( !n_minus_one_div_2.isProbablePrime(10) )
		{
			throw new IllegalArgumentException("(N-1)/2 is not prime for " + N.toString(16));
		}

		if( g.modPow(n_minus_one_div_2, N).add(BigInteger.ONE).compareTo(N) != 0)
		{
			throw new IllegalArgumentException("Not a primitive root: " + g.toString(16));
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
	static SRPVerifier		makeVerifier(SRPConstants constants, byte[] password)
	{
		BigInteger		salt = random(constants);
		BigInteger 		x = makePrivateKey(password, salt);
		BigInteger		v = constants.primitiveRoot_g.modPow(x, constants.largePrime_N);

		return new SRPVerifier(v, salt);
	}

	/**
	 * Make a new private key via x = H(s, P) where H is a hash() function, s is random salt, and P is the password.
	 *
	 * @param password the password
	 * @param salt random salt
	 * @return the private key
	 */
	static BigInteger makePrivateKey(byte[] password, BigInteger salt)
	{
		BigInteger		passwordInt = new BigInteger(password);
		return hash(combine(passwordInt, salt));
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
	/*
	static BigInteger		combine(BigInteger a, BigInteger b)
	{
		ByteBuffer	abuf = ByteBuffer.wrap(a.toByteArray());
		ByteBuffer	bbuf = ByteBuffer.wrap(b.toByteArray());
		byte[]		combined = new byte[abuf.capacity() + bbuf.capacity()];
		ByteBuffer	combinedbuf = ByteBuffer.wrap(combined);

		abuf.rewind();
		bbuf.rewind();
		combinedbuf.clear();

		while ( abuf.hasRemaining() && bbuf.hasRemaining() )
		{
			byte		abyte = abuf.get();
			combinedbuf.put(abyte);
			byte		bbyte = bbuf.get();
			combinedbuf.put(bbyte);
			if ( ((abyte & 1) == 0) && bbuf.hasRemaining() )
			{
				bbyte = bbuf.get();
				combinedbuf.put(bbyte);
			}
		}

		while ( abuf.hasRemaining() )
		{
			byte		x = abuf.get();
			combinedbuf.put(x);
		}

		while ( bbuf.hasRemaining() )
		{
			byte		x = bbuf.get();
			combinedbuf.put(x);
		}

		return new BigInteger(combined);
	}
	*/
	static BigInteger		combine(BigInteger a, BigInteger b)
	{
		byte[]	abuf = a.toByteArray();
		byte[]	bbuf = b.toByteArray();
		byte[]	cbuf = new byte[abuf.length + bbuf.length];

		int ai = 0;
		int bi = 0;
		int ci = 0;
		int al = abuf.length;
		int bl = bbuf.length;

		while ( ai < al && bi < bl )
		{
			byte		abyte = abuf[ai++];
			cbuf[ci++] = abyte;
			byte		bbyte = bbuf[bi++];
			cbuf[ci++] = bbyte;
			
			if ( ((abyte & 1) == 0) && bi < bl )
			{
				cbuf[ci++] = bbuf[bi++];
			}
		}

		while ( ai < al )
		{
			cbuf[ci++] = abuf[ai++];
		}

		while ( bi < bl )
		{
			cbuf[ci++] = bbuf[bi++];
		}
		
		return new BigInteger(cbuf);
	}
	

	/**
	 * hash a big int. Use SHA 256.
	 *
	 * @param i int to hash
	 * @return the hash
	 */
	static BigInteger		hash(BigInteger i)
	{
		return new BigInteger(hashToBytes(i));
	}

	/**
	 * hash a big int. Use SHA 256.
	 *
	 * @param i int to hash
	 * @return the hash
	 */
	static byte[]		hashToBytes(BigInteger i)
	{
		Hash256i d = new Hash256i();
		byte[] b = i.toByteArray();
		return d.hash(b);
	}

	/**
	 * hash a big int. Use MD5.
	 *
	 * @param i int to hash
	 * @return the hash
	 */
	static byte[]		hashToBytesMD5(BigInteger i)
	{
		/*
		try
		{
			MessageDigest		sha = MessageDigest.getInstance("MD5");
			byte[] 				b = i.toByteArray();
			sha.update(b, 0, b.length);
			return sha.digest();
		}
		catch ( NoSuchAlgorithmException e )
		{
			throw new UnsupportedOperationException(e);
		}
		*/
		return hashToBytesForAES(i);
	}

	static byte[]		hashToBytesForAES(BigInteger i)
	{
		return hashToBytes(i);
	}

	
	/**
	 * Return a random number that satsifies: 1 < r < n
	 *
	 * @param constants constants to use
	 * @return the random number
	 */
	static BigInteger		random(SRPConstants constants)
	{
		int 		numberOfBytes = (constants.largePrime_N.bitLength() + (constants.largePrime_N.bitLength() - 1)) / 8;
		byte[]		b = new byte[numberOfBytes];
		fRandom.nextBytes(b);
		BigInteger	i = new BigInteger(b);

		// random numbers must be: 1 < r < n
		BigInteger	max = constants.largePrime_N.subtract(TWO);
		return i.mod(max).add(TWO);
	}

	/**
	 * Calculate M(1) - H(A, B, K)
	 *
	 * @param publicKey_A generated public key - A
	 * @param publicKey_B generated public key - B
	 * @param commonValue_S the session common value - S
	 * @return M(1)
	 */
	static BigInteger		calcM1(BigInteger publicKey_A, BigInteger publicKey_B, BigInteger commonValue_S)
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
	static BigInteger		calcM2(BigInteger publicKey_A, BigInteger evidenceValue_M1, BigInteger commonValue_S)
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
	static BigInteger 		calc_u(BigInteger A, BigInteger B)
	{
		return hash(combine(A, B));
	}

	private SRPUtils()
	{
	}

	private static final BigInteger 		TWO = BigInteger.valueOf(2);

	private static final SecureRandom 		fRandom = new SecureRandom();
}
