package org.bc.crypto.tls;

import java.security.SecureRandom;

import org.bc.crypto.CryptoException;
import org.bc.crypto.Signer;
import org.bc.crypto.params.AsymmetricKeyParameter;

interface TlsSigner
{
    byte[] calculateRawSignature(SecureRandom random, AsymmetricKeyParameter privateKey, byte[] md5andsha1)
        throws CryptoException;

    Signer createVerifyer(AsymmetricKeyParameter publicKey);

    boolean isValidPublicKey(AsymmetricKeyParameter publicKey);
}
