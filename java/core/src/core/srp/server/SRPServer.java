/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp.server;

import java.math.BigInteger;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bc.crypto.params.KeyParameter;


import com.jordanzimmerman.SRPFactory;
import com.jordanzimmerman.SRPServerSession;
import com.jordanzimmerman.SRPVerifier;

import core.exceptions.CryptoException;
import core.srp.SRPPackets;
import core.srp.SRPSession;
import core.srp.SRPPackets.Packet2_ServerSendSalt;
import core.srp.SRPPackets.Packet3_ClientSendPublicKey;
import core.srp.SRPPackets.Packet4_ServerSendPublicKey;
import core.srp.SRPPackets.Packet5_ClientSendEvidence;
import core.srp.SRPPackets.Packet6_ServerSendEvidenceAndPayload;

public class SRPServer extends SRPSession
{
	static { SRPPackets.register(); }

	SRPFactory factory;
	SRPServerSession session;
	
	public SRPServer ()
	{
		factory = SRPFactory.getInstance();
	}
	
	public SRPPackets.Packet2_ServerSendSalt step1_getSalt_send (String version, BigInteger verifier, BigInteger salt)
	{
		session = factory.newServerSession(new SRPVerifier (verifier, salt));
		return new SRPPackets.Packet2_ServerSendSalt(version, session.getVerifier().salt_s.toByteArray());
	}
	
	public SRPPackets.Packet4_ServerSendPublicKey step2_receivePublicKey_generatePublicKey_send (SRPPackets.Packet3_ClientSendPublicKey packet) throws CryptoException
	{
		try
		{
			session.setClientPublicKey_A(packet.getPublicKey());
			return new SRPPackets.Packet4_ServerSendPublicKey(session.getPublicKey_B());
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
	
	public SRPPackets.Packet6_ServerSendEvidenceAndPayload step3_validateClientEvidence_generateEvidence_send (SRPPackets.Packet5_ClientSendEvidence packet) throws CryptoException
	{
		try
		{
			session.computeCommonValue_S();
			session.validateClientEvidenceValue_M1(packet.getEvidence());

			sessionKey = session.getSessionKey_K();

			return new SRPPackets.Packet6_ServerSendEvidenceAndPayload(session.getEvidenceValue_M2());
		}
		catch (Exception e)
		{
			throw new CryptoException(e);
		}
	}
}
