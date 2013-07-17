package org.bc.crypto.generators;

import org.bc.crypto.AsymmetricCipherKeyPair;
import org.bc.crypto.AsymmetricCipherKeyPairGenerator;
import org.bc.crypto.EphemeralKeyPair;
import org.bc.crypto.KeyEncoder;

public class EphemeralKeyPairGenerator
{
    private AsymmetricCipherKeyPairGenerator gen;
    private KeyEncoder keyEncoder;

    public EphemeralKeyPairGenerator(AsymmetricCipherKeyPairGenerator gen, KeyEncoder keyEncoder)
    {
        this.gen = gen;
        this.keyEncoder = keyEncoder;
    }

    public EphemeralKeyPair generate()
    {
        AsymmetricCipherKeyPair eph = gen.generateKeyPair();

        // Encode the ephemeral public key
     	return new EphemeralKeyPair(eph, keyEncoder);
    }
}
