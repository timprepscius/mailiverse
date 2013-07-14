/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.client;

import core.callback.Callback;
import core.client.messages.ClientMessagesSerializerJSON;
import core.client.messages.Message;
import core.client.messages.Response;
import core.io.IoChain;
import core.srp.SRPPackets;
import core.util.SimpleSerializer;

public class ClientUserSession extends IoChain
{
	static { SRPPackets.register(); ClientMessagesSerializerJSON.register(); }

	Callback callback;
	Message message;
	
	public ClientUserSession (Message message, Callback callback, IoChain sender)
	{
		super(sender);
		
		this.message = message;
		this.callback = callback;
	}

	@Override
	public void open () throws Exception
	{
		sender.send(SimpleSerializer.serialize(message));
	}
	
	@Override
	public void onReceive (byte[] bytes) throws Exception
	{
		Response message = SimpleSerializer.deserialize(bytes);
		callback.invoke(this, message.getBlock());
	}

	@Override
	public void onException(Exception e) 
	{
		callback.invoke(this, e);
	}
}
