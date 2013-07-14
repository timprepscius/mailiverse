/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.client;

import core.callback.Callback;
import core.callbacks.IoSend;
import core.callbacks.JSONSerialize;
import core.constants.ConstantsVersion;
import core.crypt.Cryptor;
import core.io.IoChain;
import core.srp.SRPPackets;
import core.srp.SRPPackets.PacketInit_ClientTestCreate;
import core.util.SimpleSerializer;


public class ClientTestCreateSession extends IoChain
{
	static { SRPPackets.register(); }

	Cryptor cryptor;
	Callback callback;
	String user;
	
	public ClientTestCreateSession (Cryptor cryptor, String user, Callback callback, IoChain session)
	{
		super(session);
		
		this.user = user;
		this.callback = callback;
		this.cryptor = cryptor;
	}

	@Override
	public void open () throws Exception
	{
		SRPPackets.PacketInit_ClientTestCreate test = 
			new PacketInit_ClientTestCreate(ConstantsVersion.LOGIN, user);
		
		new JSONSerialize()
			.addCallback(cryptor.encrypt_())
			.addCallback(SRPPackets.PacketInit_EncryptedPacket.wrap_())
			.addCallback(new JSONSerialize())
			.addCallback(new IoSend(sender))
			.invoke(test);
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
