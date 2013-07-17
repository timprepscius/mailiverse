package org.bc.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bc.crypto.AsymmetricCipherKeyPair;
import org.bc.crypto.AsymmetricCipherKeyPairGenerator;
import org.bc.crypto.KeyGenerationParameters;
import org.bc.crypto.params.ECDomainParameters;
import org.bc.crypto.params.ECKeyGenerationParameters;
import org.bc.crypto.params.ECPrivateKeyParameters;
import org.bc.crypto.params.ECPublicKeyParameters;
import org.bc.math.ec.ECConstants;
import org.bc.math.ec.ECPoint;

public class ECKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator, ECConstants
{
    ECDomainParameters  params;
    SecureRandom        random;

    public void init(
        KeyGenerationParameters param)
    {
        ECKeyGenerationParameters  ecP = (ECKeyGenerationParameters)param;

        this.random = ecP.getRandom();
        this.params = ecP.getDomainParameters();
    }

    /**
     * Given the domain parameters this routine generates an EC key
     * pair in accordance with X9.62 section 5.2.1 pages 26, 27.
     */
    public AsymmetricCipherKeyPair generateKeyPair()
    {
        BigInteger n = params.getN();
        int        nBitLength = n.bitLength();
        BigInteger d;

        do
        {
            d = new BigInteger(nBitLength, random);
        }
        while (d.equals(ZERO)  || (d.compareTo(n) >= 0));

        ECPoint Q = params.getG().multiply(d);

        return new AsymmetricCipherKeyPair(
            new ECPublicKeyParameters(Q, params),
            new ECPrivateKeyParameters(d, params));
    }
}
