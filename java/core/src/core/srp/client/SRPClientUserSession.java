/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp.client;

import core.callbacks.IoOpen;
import core.constants.ConstantsVersion;
import core.crypt.KeyPairFromPassword;
import core.exceptions.CryptoException;
import core.io.IoChain;
import core.srp.SRPPackets;
import core.util.SimpleSerializer;

public class SRPClientUserSession extends IoChain
{
	String user;
	KeyPairFromPassword keyPair;
	SRPClient srp;
	SRPClientListener listener;
	int step;
	
	public SRPClientUserSession (String user, KeyPairFromPassword keyPair, IoChain sender, SRPClientListener listener) throws CryptoException
	{
		super(sender);
		
		this.user = user;
		this.keyPair = keyPair;
		this.listener = listener;
	}
	
	@Override
	public void open () throws Exception
	{
		if (listener != null)
			listener.onSRPStep("H");
		
		step = 0;
		this.srp = new SRPClient(keyPair.getVerifier());
		sender.send(SimpleSerializer.serialize(new SRPPackets.Packet1_ClientSendsHello(user, ConstantsVersion.LOGIN)));
	}
	
	@Override
	protected void onReceive(byte[] bytes) throws Exception 
	{
		switch (step++)
		{
			case 0:			
			{
				if (listener != null)
					listener.onSRPStep("" + step);
				
				SRPPackets.Packet2_ServerSendSalt in = SimpleSerializer.deserialize(bytes);
				srp.step2_generatePublicKey_send_(in, sender).invoke();
			} break;
			
			case 1:
			{
				if (listener != null)
					listener.onSRPStep("" + step);
				
				SRPPackets.Packet4_ServerSendPublicKey in = SimpleSerializer.deserialize(bytes);
				srp.step3_generateEvidenceUsingServerProvidedInputs_send(in, sender).invoke();
			} break;	
			case 2:
			{
				if (listener != null)
					listener.onSRPStep("" + step);
				
				SRPPackets.Packet6_ServerSendEvidenceAndPayload in = SimpleSerializer.deserialize(bytes);
				srp.step4_validateServerEvidence(in).addCallback(new IoOpen(receiver)).invoke();
			} break;
			
			default:
				
				if (listener != null)
					listener.onSRPStep("R");
				
				super.onReceive(srp.streamDecrypt(bytes));
		}
	}

	@Override
	public void send(byte[] packet) throws Exception 
	{
		if (listener != null)
			listener.onSRPStep("S");
		
		sender.send(srp.streamEncrypt(packet));
	}

}
