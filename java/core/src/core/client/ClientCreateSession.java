/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.client;

import com.jordanzimmerman.SRPVerifier;

import core.callback.Callback;
import core.callbacks.IoSend;
import core.callbacks.JSONSerialize;
import core.constants.ConstantsVersion;
import core.crypt.Cryptor;
import core.crypt.KeyPairFromPassword;
import core.io.IoChain;
import core.srp.SRPPackets;
import core.srp.client.SRPClient;
import core.util.SimpleSerializer;


public class ClientCreateSession extends IoChain
{
	static { SRPPackets.register(); }
	
	Cryptor cryptor;
	Callback callback;
	String user;
	KeyPairFromPassword keyPair;
	byte[] extra;
	
	public ClientCreateSession (Cryptor cryptor, String user, KeyPairFromPassword keyPair, byte[] extra, Callback callback, IoChain session)
	{
		super(session);
		
		this.user = user;
		this.keyPair = keyPair;
		this.callback = callback;
		this.extra = extra;
		this.cryptor = cryptor;
	}

	@Override
	public void open () throws Exception
	{
		SRPVerifier verifier = SRPClient.createVerifierFromKeyPair(keyPair);

		SRPPackets.PacketInit_ClientPreAutheticationInitialization init = 
			new SRPPackets.PacketInit_ClientPreAutheticationInitialization(
				ConstantsVersion.LOGIN, user, verifier.verifier_v.toByteArray(), verifier.salt_s.toByteArray()
			);
		
		init.extra = extra;

		new JSONSerialize()
			.addCallback(cryptor.encrypt_())
			.addCallback(SRPPackets.PacketInit_EncryptedPacket.wrap_())
			.addCallback(new JSONSerialize())
			.addCallback(new IoSend(sender))
			.invoke(init);
	}
	
	@Override
	public void onReceive (byte[] bytes) throws Exception
	{
		callback.invoke(this, bytes);
	}

	@Override
	public void onException(Exception e) 
	{
		callback.invoke(this, e);
	}
}
