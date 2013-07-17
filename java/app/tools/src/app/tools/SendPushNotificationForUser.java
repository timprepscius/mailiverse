/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import mail.server.push.ApplePushService;
import mail.server.push.PushDispatcher;

public class SendPushNotificationForUser
{
	public static void main (String[] _args) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");

		Map<String,String> args = Arguments.map(_args, 0);
		
		String user = args.get("user");
		
		if (user == null)
			throw new IllegalArgumentException();
		
		System.out.println ("Sending push notification to: " + user);
		
		PushDispatcher pusher = new PushDispatcher();
		pusher.notifyUserOfEmail(user, "author@somehow.com", "subject", "body");
		pusher.shutdown();
	}

}
