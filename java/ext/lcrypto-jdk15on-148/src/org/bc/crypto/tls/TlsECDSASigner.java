package org.bc.crypto.tls;

import org.bc.crypto.DSA;
import org.bc.crypto.params.AsymmetricKeyParameter;
import org.bc.crypto.params.ECPublicKeyParameters;
import org.bc.crypto.signers.ECDSASigner;

class TlsECDSASigner extends TlsDSASigner
{
    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof ECPublicKeyParameters;
    }

    protected DSA createDSAImpl()
    {
        return new ECDSASigner();
    }
}
