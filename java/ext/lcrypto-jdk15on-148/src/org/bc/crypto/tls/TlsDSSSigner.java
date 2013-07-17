package org.bc.crypto.tls;

import org.bc.crypto.DSA;
import org.bc.crypto.params.AsymmetricKeyParameter;
import org.bc.crypto.params.DSAPublicKeyParameters;
import org.bc.crypto.signers.DSASigner;

class TlsDSSSigner extends TlsDSASigner
{
    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof DSAPublicKeyParameters;
    }

    protected DSA createDSAImpl()
    {
        return new DSASigner();
    }
}
