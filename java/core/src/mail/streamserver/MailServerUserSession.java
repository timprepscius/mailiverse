/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.streamserver;

import org.json.JSONObject;

import core.client.messages.Delete;
import core.client.messages.Get;
import core.client.messages.Message;
import core.client.messages.Put;
import core.client.messages.Response;
import core.constants.ConstantsClient;
import core.constants.ConstantsPushNotifications;
import core.io.IoChain;
import core.server.mailextra.MailExtraDb;
import core.srp.server.SRPServerUserSession;
import core.util.Block;
import core.util.SimpleSerializer;


public class MailServerUserSession extends IoChain
{
	String userName;
	MailServerSessionDb db;
	
	public MailServerUserSession (MailServerSessionDb db, SRPServerUserSession sender)
	{
		super(sender);
		
		this.db = db;
	}

	@Override
	public void open ()
	{
		userName = ((SRPServerUserSession)sender).getUserName();
	}
	
	public void doDelete () throws Exception
	{
		db.deleteUser(userName);
		send(SimpleSerializer.serialize(new Response()));
	}
	
	@Override
	public void onReceive (byte[] bytes) throws Exception
	{
		Message message = SimpleSerializer.deserialize(bytes);
		if (message instanceof Delete)
		{
			doDelete();
		}
		else
		if (message instanceof Put)
		{
			db.setBlock(userName, ((Put)message).getBlock());
			send(SimpleSerializer.serialize(new Response(db.getBlock(userName))));
		}
		else
		if (message instanceof Get)
		{
			send(SimpleSerializer.serialize(new Response(db.getBlock(userName))));
		}
		else
			throw new Exception ("Unknown message type");
	}

	public void checkRoomForNewUser() throws Exception
	{
		db.checkRoomForNewUser();
	}
}
