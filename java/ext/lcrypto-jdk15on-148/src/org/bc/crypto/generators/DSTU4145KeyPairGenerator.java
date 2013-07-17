package org.bc.crypto.generators;

import org.bc.crypto.AsymmetricCipherKeyPair;
import org.bc.crypto.params.ECPrivateKeyParameters;
import org.bc.crypto.params.ECPublicKeyParameters;

public class DSTU4145KeyPairGenerator
    extends ECKeyPairGenerator
{
    public AsymmetricCipherKeyPair generateKeyPair()
    {
        AsymmetricCipherKeyPair pair = super.generateKeyPair();

        ECPublicKeyParameters pub = (ECPublicKeyParameters)pair.getPublic();
        ECPrivateKeyParameters priv = (ECPrivateKeyParameters)pair.getPrivate();

        pub = new ECPublicKeyParameters(pub.getQ().negate(), pub.getParameters());

        return new AsymmetricCipherKeyPair(pub, priv);
    }
}
