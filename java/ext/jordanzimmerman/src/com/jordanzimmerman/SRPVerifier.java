package com.jordanzimmerman;     

import java.math.BigInteger;
import java.io.Serializable;

/**
 * POJO for holding the random salt and verifier<br>
 *
 * Released into the public domain
 *
 * @author Jordan Zimmerman - jordan@jordanzimmerman.com
 * @see SRPFactory Full Documentation
 * @version 1.1
 */
public class SRPVerifier implements Serializable
{
	public SRPVerifier(BigInteger verifier, BigInteger salt)
	{
		this.verifier_v = verifier;
		this.salt_s = salt;
	}

	/**
	 * v
	 */
	public final BigInteger 	verifier_v;

	/**
	 * s
	 */
	public final BigInteger 	salt_s;
}
