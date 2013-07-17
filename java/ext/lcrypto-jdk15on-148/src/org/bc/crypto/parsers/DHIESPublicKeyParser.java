package org.bc.crypto.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.bc.crypto.KeyParser;
import org.bc.crypto.params.AsymmetricKeyParameter;
import org.bc.crypto.params.DHParameters;
import org.bc.crypto.params.DHPublicKeyParameters;

public class DHIESPublicKeyParser
    implements KeyParser
{
    private DHParameters dhParams;

    public DHIESPublicKeyParser(DHParameters dhParams)
    {
        this.dhParams = dhParams;
    }

    public AsymmetricKeyParameter readKey(InputStream stream)
        throws IOException
    {
        byte[] V = new byte[(dhParams.getP().bitLength() + 7) / 8];

        stream.read(V, 0, V.length);

        return new DHPublicKeyParameters(new BigInteger(1, V), dhParams);
    }
}
