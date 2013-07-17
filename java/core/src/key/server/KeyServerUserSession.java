/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.server;

import org.json.JSONObject;

import core.client.messages.Get;
import core.client.messages.Message;
import core.client.messages.Put;
import core.client.messages.Response;
import core.io.IoChain;
import core.srp.server.SRPServerUserSession;
import core.util.SimpleSerializer;
import core.server.captcha.Captcha;
import core.server.mailextra.MailExtraDb;

public class KeyServerUserSession extends IoChain
{
	String userName;
	KeyServerSessionDb db;
	Captcha captcha;
	
	public KeyServerUserSession (KeyServerSessionDb db, SRPServerUserSession sender) throws Exception
	{
		super(sender);
		
		this.captcha = new Captcha();
		this.db = db;
	}

	@Override
	public void open ()
	{
		userName = ((SRPServerUserSession)sender).getUserName();
	}
	
	@Override
	public void onReceive (byte[] bytes) throws Exception
	{
		Message message = SimpleSerializer.deserialize(bytes);
		
		if (message instanceof Put)
		{
			db.setBlock(userName, ((Put)message).getBlock());
			send(SimpleSerializer.serialize(new Response(db.getBlock(userName))));
		}
		else
		if (message instanceof Get)
		{
			sender.send(SimpleSerializer.serialize(new Response(db.getBlock(userName))));
		}
		else
			throw new Exception("Unknown message type");
	}
	
}
