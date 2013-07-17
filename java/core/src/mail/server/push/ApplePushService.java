/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.push;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import javapns.Push;
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.transmission.PushQueue;

import core.constants.ConstantsServer;
import core.util.Collectionz;
import core.util.ExternalResource;
import core.util.LogOut;
import core.util.Passwords;
import core.util.Strings;

public class ApplePushService 
{
	static
	{
	    Logger logger = Logger.getLogger("javapns.notification.PushNotificationManager");
	    logger.setLevel(Level.ALL);
	    logger.addAppender(new ConsoleAppender(
	               new PatternLayout("%-6r [%p] %c - %m%n")));
	}
	
	static LogOut log = new LogOut(ApplePushService.class);
	
	String environment = "sandbox";
	final int NUM_APNS_THREADS = 1;
	PushQueue queue;
	
	public ApplePushService ()
	{
	}
	
	public InputStream getCertificate () throws Exception
	{
		InputStream stream = ExternalResource.getResourceAsStream(
				getClass(), 
					ConstantsServer.DEBUG ? 
						"ios-push-certificate-dev.p12" :
						"ios-push-certificate-prod.p12"
			);
		
		return stream;
		
	}
	
	public PushQueue getQueue () throws Exception
	{
		if (queue == null)
		{
			queue = Push.queue(
				getCertificate(),
				Passwords.getPasswordFor("push-certificate"), 
				!ConstantsServer.DEBUG, 
				NUM_APNS_THREADS
			);
			queue.start();
		}
		
		return queue;
	}
	
	protected void queryFeedBackService () 
	{
		try
		{
			List<Device> inactiveDevices = 
				Push.feedback(
					getCertificate(),
					Passwords.getPasswordFor("push-certificate"), 
					!ConstantsServer.DEBUG
				);
		
			for (Device device : inactiveDevices)
			{
				log.debug("should remove device ", device.getDeviceId());
				PushDispatcher.getInstance().removeDevice(device.getDeviceId());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.exception(e);
		}
	}
	
	protected void sendMessageToDevices(List<String> recipients, int badgeNumber, String messageKey, String[] arguments)
	{
		log.debug ("sendMessageToDevices");
		
		for (String deviceToken_ : recipients)
		{			
			try
			{
				String deviceToken = deviceToken_.replace("<", "").replace(">", "").replace(" ", "");

				PushNotificationPayload payload = PushNotificationPayload.complex();
				payload.addCustomAlertLocKey(messageKey);
				payload.addCustomAlertLocArgs(Collectionz.toMutableList(arguments));
				payload.addSound("default");
				
				getQueue().add(payload, new BasicDevice(deviceToken));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				log.error("Could not connect to APNs to send the notification ");
			}
		}
		
		log.debug ("~sendMessageToDevices");
	}

	public void notifyUserOfEmail (String deviceId, String author, String subject, String body)
	{
		List<String> devices = new ArrayList<String>();
		devices.add(deviceId);
		
		List<String> keys = new ArrayList<String>();
		
		if (author != null)
			keys.add("%@");

		String text = null;
		if (body != null || subject != null)
		{
			keys.add("%@");
			
			final int MAX_MESSAGE_LENGTH = 128;
			if (subject != null)
				subject = subject.substring(0, Math.min(MAX_MESSAGE_LENGTH, subject.length()));
			
			int subjectLength = subject != null ? subject.length() :0;
			
			if (body != null)
				body = body.substring(0, Math.min(MAX_MESSAGE_LENGTH - subjectLength, body.length()));
			
			text = Strings.concat(Collectionz.filterNull(subject, body), " * ");
		}
		
		sendMessageToDevices(
			devices, 0, 
			Strings.concat(keys, "\n"), 
			Collectionz.filterNull(author, text).toArray(new String[0])
		);
	}
	
	
	public void shutdown ()
	{
		try
		{
			Thread.sleep(5000);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
