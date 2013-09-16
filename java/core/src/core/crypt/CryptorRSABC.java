/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;

import org.bc.asn1.ASN1InputStream;
import org.bc.asn1.ASN1Integer;
import org.bc.asn1.ASN1ObjectIdentifier;
import org.bc.asn1.ASN1OctetString;
import org.bc.asn1.ASN1Primitive;
import org.bc.asn1.ASN1Sequence;
import org.bc.asn1.DERBitString;
import org.bc.asn1.pkcs.RSAPrivateKey;
import org.bc.asn1.pkcs.RSAPublicKey;
import org.bc.crypto.encodings.PKCS1Encoding;
import org.bc.crypto.engines.RSAEngine;
import org.bc.crypto.params.RSAKeyParameters;

import core.exceptions.CryptoException;
import core.util.Arrays;


public class CryptorRSABC extends CryptorRSA
{
	SecureRandom random = new SecureRandom();
	
	public final int MAX_RSA_BLOCK_SIZE = 117;
	
	public RSAPublicKey publicKey;
	public RSAPrivateKey privateKey;
	
    public static final byte[] iv = Arrays.generate(16, 0);
    
	public CryptorRSABC (byte[] publicKey, byte[] privateKey) throws CryptoException
	{
		initialize(publicKey, privateKey);
		
		try
		{
			if (privateKey != null)
			{
				ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(privateKey)); 
				ASN1Primitive keyObject = asn1InputStream.readObject(); 
				asn1InputStream.close();
				
				ASN1Sequence keySequence = (ASN1Sequence)keyObject;
				ASN1Integer pkcs8Version = (ASN1Integer) keySequence.getObjectAt(0);
				ASN1Sequence algorithmSequence = (ASN1Sequence) keySequence.getObjectAt(1);
				ASN1ObjectIdentifier algorithmIdentifier = (ASN1ObjectIdentifier) algorithmSequence.getObjectAt(0);

				String algorithm = algorithmIdentifier.getId();
				if (!algorithm.equals("1.2.840.113549.1.1.1"))
					throw new CryptoException("Unknown RSA algorithm");
				
				ASN1OctetString privateKeyOctets = (ASN1OctetString) keySequence.getObjectAt(2);
				
				ASN1InputStream asn1PrivateKeyStream = new ASN1InputStream(privateKeyOctets.getOctetStream());
				ASN1Primitive privateKeyObject = asn1PrivateKeyStream.readObject(); 
				asn1PrivateKeyStream.close();
				ASN1Sequence privateKeySequence = (ASN1Sequence)privateKeyObject;
				
				int I=0;
				ASN1Integer 
					s = (ASN1Integer)privateKeySequence.getObjectAt(I++),
					n = (ASN1Integer)privateKeySequence.getObjectAt(I++), 
					e = (ASN1Integer)privateKeySequence.getObjectAt(I++),
					d = (ASN1Integer)privateKeySequence.getObjectAt(I++),
					p = (ASN1Integer)privateKeySequence.getObjectAt(I++),
					q = (ASN1Integer)privateKeySequence.getObjectAt(I++),
					d1 = (ASN1Integer)privateKeySequence.getObjectAt(I++),
					d2 = (ASN1Integer)privateKeySequence.getObjectAt(I++),
					c = (ASN1Integer)privateKeySequence.getObjectAt(I++)
					;
				
				/*
				org.bouncycastle.asn1.pkcs.RSAPrivateKey.RSAPrivateKey(
						BigInteger modulus, 
						BigInteger publicExponent, 
						BigInteger privateExponent, 
						BigInteger prime1, 
						BigInteger prime2, 
						BigInteger exponent1, 
						BigInteger exponent2, 
						BigInteger coefficient
					)
				*/
				this.privateKey = 
					new RSAPrivateKey(
						n.getValue(), 
						e.getValue(), 
						d.getValue(), 
						p.getValue(), 
						q.getValue(), 
						d1.getValue(), 
						d2.getValue(), 
						c.getValue()
					);
			}
			
			if (publicKey != null)
			{
				ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(publicKey)); 
				ASN1Primitive keyObject = asn1InputStream.readObject();
				asn1InputStream.close();
				
				ASN1Sequence keySequence = (ASN1Sequence)keyObject;
				ASN1Sequence algorithmSequence = (ASN1Sequence) keySequence.getObjectAt(0);
				ASN1ObjectIdentifier algorithmIdentifier = (ASN1ObjectIdentifier) algorithmSequence.getObjectAt(0);

				String algorithm = algorithmIdentifier.getId();
				if (!algorithm.equals("1.2.840.113549.1.1.1"))
					throw new CryptoException("Unknown RSA algorithm");
				
				DERBitString keyOctets = (DERBitString) keySequence.getObjectAt(1);
				ASN1InputStream asn1PublicKeyStream = new ASN1InputStream(new ByteArrayInputStream(keyOctets.getBytes()));
				ASN1Primitive publicKeyObject = asn1PublicKeyStream.readObject(); 
				asn1PublicKeyStream.close();
				
				ASN1Sequence publicKeySequence = (ASN1Sequence)publicKeyObject;
				
				int I=0;
				ASN1Integer 
					n = (ASN1Integer)publicKeySequence.getObjectAt(I++),
					e = (ASN1Integer)publicKeySequence.getObjectAt(I++); 
	
				this.publicKey = new RSAPublicKey(n.getValue(), e.getValue());
			}
		}
		catch (IOException e)
		{
			throw new CryptoException(e);
		}
	}
	
	public byte[] encrypt (byte[] block) throws CryptoException
	{
		try
		{
			PKCS1Encoding e = new PKCS1Encoding(new RSAEngine());
			RSAKeyParameters key = new RSAKeyParameters(false, publicKey.getModulus(), publicKey.getPublicExponent());
			e.init(true, key);
	
			return e.processBlock(block, 0, block.length);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
	
	public byte[] decrypt (byte[] block) throws CryptoException
	{
		try
		{
			PKCS1Encoding e = new PKCS1Encoding(new RSAEngine());
			RSAKeyParameters key = new RSAKeyParameters(false, privateKey.getModulus(), privateKey.getPrivateExponent());
			e.init(false, key);
				
			return e.processBlock(block, 0, block.length);
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
}
