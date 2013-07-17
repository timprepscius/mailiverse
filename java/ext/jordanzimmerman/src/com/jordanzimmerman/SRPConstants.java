package com.jordanzimmerman;     

import java.math.BigInteger;
import java.io.Serializable;

/**
 * POJO for holding the prime number and primitve root.<br>
 *
 * Released into the public domain
 *
 * @author Jordan Zimmerman - jordan@jordanzimmerman.com
 * @see SRPFactory Full Documentation
 * @version 1.3 Updated to use the SRP-6a spec - k = H(N, g) 2/27/07
 * @version 1.2 Updated to use the SRP-6 spec 2/21/07
 * @version 1.1
 */
public class SRPConstants implements Serializable
{
	/**
	 * NOTE: this constructor validates the values passed via {@link SRPUtils#validateConstants(java.math.BigInteger,java.math.BigInteger)}
	 *
	 * @param largePrime a very large prime number
	 * @param primitiveRoot a primitive root that relates to the prime number.
	 */
	public SRPConstants(BigInteger largePrime, BigInteger primitiveRoot)
	{
		SRPUtils.validateConstants(largePrime, primitiveRoot);

		this.largePrime_N = largePrime;
		this.primitiveRoot_g = primitiveRoot;
		this.srp6Multiplier_k = SRPUtils.hash(SRPUtils.combine(this.largePrime_N, this.primitiveRoot_g));
	}

	/**
	 * N
	 */
	public final BigInteger 	largePrime_N;

	/**
	 * g
	 */
	public final BigInteger 	primitiveRoot_g;

	/**
	 * k from SRP-6
	 */
	public final BigInteger		srp6Multiplier_k;
}
