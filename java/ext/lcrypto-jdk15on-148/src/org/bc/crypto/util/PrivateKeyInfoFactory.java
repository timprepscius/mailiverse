package org.bc.crypto.util;

import java.io.IOException;

import org.bc.asn1.ASN1Integer;
import org.bc.asn1.DERNull;
import org.bc.asn1.pkcs.PKCSObjectIdentifiers;
import org.bc.asn1.pkcs.PrivateKeyInfo;
import org.bc.asn1.pkcs.RSAPrivateKey;
import org.bc.asn1.x509.AlgorithmIdentifier;
import org.bc.asn1.x509.DSAParameter;
import org.bc.asn1.x9.X9ObjectIdentifiers;
import org.bc.crypto.params.AsymmetricKeyParameter;
import org.bc.crypto.params.DSAParameters;
import org.bc.crypto.params.DSAPrivateKeyParameters;
import org.bc.crypto.params.RSAKeyParameters;
import org.bc.crypto.params.RSAPrivateCrtKeyParameters;

/**
 * Factory to create ASN.1 private key info objects from lightweight private keys.
 */
public class PrivateKeyInfoFactory
{
    /**
     * Create a PrivateKeyInfo representation of a private key.
     *
     * @param privateKey the SubjectPublicKeyInfo encoding
     * @return the appropriate key parameter
     * @throws java.io.IOException on an error encoding the key
     */
    public static PrivateKeyInfo createPrivateKeyInfo(AsymmetricKeyParameter privateKey) throws IOException
    {
        if (privateKey instanceof RSAKeyParameters)
        {
            RSAPrivateCrtKeyParameters priv = (RSAPrivateCrtKeyParameters)privateKey;

            return new PrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new RSAPrivateKey(priv.getModulus(), priv.getPublicExponent(), priv.getExponent(), priv.getP(), priv.getQ(), priv.getDP(), priv.getDQ(), priv.getQInv()));
        }
        else if (privateKey instanceof DSAPrivateKeyParameters)
        {
            DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters)privateKey;
            DSAParameters params = priv.getParameters();

            return new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(params.getP(), params.getQ(), params.getG())), new ASN1Integer(priv.getX()));
        }
        else
        {
            throw new IOException("key parameters not recognised.");
        }
    }
}
