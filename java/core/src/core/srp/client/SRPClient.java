/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp.client;

import core.callback.Callback;
import com.jordanzimmerman.SRPClientSession;
import com.jordanzimmerman.SRPFactory;
import com.jordanzimmerman.SRPVerifier;

import core.callback.CallbackDefault;
import core.callbacks.JSONSerialize;
import core.callbacks.IoSend;
import core.crypt.KeyPairFromPassword;
import core.exceptions.CryptoException;
import core.io.IoChain;
import core.srp.SRPPackets;
import core.srp.SRPSession;

public class SRPClient extends SRPSession
{
	static { SRPPackets.register(); }
	
	SRPFactory factory;
	SRPClientAsync session;
	
	public SRPClient (byte[] password)
	{
		session = new SRPClientAsync(password);
	}
	
	public static SRPVerifier createVerifierFromKeyPair (KeyPairFromPassword keyPair) throws CryptoException
	{
		SRPFactory factory = SRPFactory.getInstance();
		SRPVerifier verifier = factory.makeVerifier(keyPair.getVerifier());
		
		return verifier;
	}
	
	public SRPPackets.Packet1_ClientSendsHello step1_generateHello (String user, String version)
	{
		return new SRPPackets.Packet1_ClientSendsHello(user, version);
	}

	public Callback step2_generatePublicKey_send_ (SRPPackets.Packet2_ServerSendSalt packet, IoChain sender)
	{
		return 
			session.setSalt_(packet.getSalt())
				.addCallback(
					new CallbackDefault() {
						public void onSuccess(Object...arguments) {
							next(new SRPPackets.Packet3_ClientSendPublicKey(session.getPublicKey()));
						}
					}
				)
				.addCallback(new JSONSerialize())
				.addCallback(new IoSend(sender));
	}
	
	public Callback step3_generateEvidenceUsingServerProvidedInputs_send (SRPPackets.Packet4_ServerSendPublicKey packet, IoChain sender) throws CryptoException
	{
		return 
			session.setServerPublicKey_(packet.getPublicKey())
				.addCallback(
					new CallbackDefault() {
						public void onSuccess(Object...arguments) {
							next(new SRPPackets.Packet5_ClientSendEvidence(session.getEvidenceValue()));
						}
					}
				)
				.addCallback(new JSONSerialize())
				.addCallback(new IoSend(sender));
	}
	
	public Callback step4_validateServerEvidence (SRPPackets.Packet6_ServerSendEvidenceAndPayload packet) throws CryptoException
	{
		return 
			session.validateServerEvidenceValue_M2_(packet.getEvidence())
				.addCallback(
					new CallbackDefault() {
						public void onSuccess(Object...arguments) {
							sessionKey = session.getSessionKey();
								next(sessionKey);
							}
						}
					);
	}
	
}
