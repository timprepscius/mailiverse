/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.push;

import java.io.IOException;
import java.sql.SQLException;

import core.server.mailextra.MailExtraDb;
import core.util.LogOut;
import core.util.Pair;
import core.util.Triple;

public class PushDispatcher
{
	static PushDispatcher instance;
	public static PushDispatcher getInstance ()
	{
		if (instance == null)
			instance = new PushDispatcher();
		
		return instance;
	}

	static LogOut log = new LogOut(PushDispatcher.class);
	MailExtraDb mailExtraDb = new MailExtraDb();
	protected ApplePushService applePushService = new ApplePushService();
	
	public void notifyUserOfEmail (String email, String author, String subject, String body) throws SQLException, IOException
	{
		// this should go elsewherez
		applePushService.queryFeedBackService();
		
		Triple<String,String,String> device = mailExtraDb.getDeviceFor(email);
		
		log.debug (device, email, author, subject, body);
		
		if (device != null && device.first != null && device.second != null && device.third != null)
		{
			log.debug (device.first, device.second, device.third);
			
			if (device.first.equals("NONE"))
			{
				return;
			}
			
			if (device.first.equals("SHORT"))
			{
				author = null;
				subject = null;
				body = null;
			}
			
			if (device.second.equals("ios"))
			{
				applePushService.notifyUserOfEmail(device.third, author, subject, body);
			}
			else
			if (device.second.equals("android"))
			{
				
			}
		}
	}

	public void removeDevice(String deviceId)
	{
		log.debug("removeDevice", deviceId);
		mailExtraDb.removeDevice("ios", deviceId);
	}
	
	public void shutdown ()
	{
		applePushService.shutdown();
	}
	
}
