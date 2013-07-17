/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.util.Map;

import mail.server.push.ApplePushService;

public class SendPushNotificationForDevice
{
	public static void main (String[] _args)
	{
		Map<String,String> args = Arguments.map(_args, 0);
		
		String deviceType = "ios";
		String deviceId = args.get("device");
		
		if (deviceId == null)
			throw new IllegalArgumentException();
		
		System.out.println ("Sending push notification to: " + deviceId);
		
		ApplePushService pusher = new ApplePushService();
		pusher.notifyUserOfEmail(deviceId, "author", "subject", "body");
		pusher.shutdown();
	}
}
