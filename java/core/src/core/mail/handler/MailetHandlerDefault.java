/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;



import mail.server.push.PushDispatcher;
import mail.server.util.JavaMailToJSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.connector.sync.StoreConnector;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsStorage;
import core.util.Environment;
import core.util.Streams;

public abstract class MailetHandlerDefault implements MailetHandler
{
	static Logger log = LoggerFactory.getLogger(MailetHandlerDefault.class);
	static SecureRandom random = new SecureRandom();
	
	public abstract StoreConnector createConnector (Environment e) throws Exception;
	
	public byte[] convertToJson (String path, byte[] bytes, JavaMailToJSON.MailDescription description)
	{
		try
		{
			JavaMailToJSON converter = new JavaMailToJSON();
			return converter.convertAttemptToShowException(path, new Date(), bytes, description);
		}
		catch (Throwable e)
		{
			return ("Failed to convert mail to Json: " + e.toString()).getBytes();
		}
	}
	
	public void handle (String toAddress, Environment e, String prefix, InputStream m) throws Exception
	{
		Date start = new Date();
		StoreConnector storeConnector = createConnector(e);
		storeConnector.open();
		
		try
		{
			String keyBase = getWriteKey();
			String originalKey = prefix + "/" + keyBase;
			String jsonKey = prefix + ConstantsStorage.JSON + "/" + keyBase;
			
			log.info("handle using store " + storeConnector + " begin");
			byte[] bytes = Streams.readFullyBytes(m);
			storeConnector.put (originalKey, bytes);
			
			JavaMailToJSON.MailDescription mailDescription = new JavaMailToJSON.MailDescription();
			byte[] json = convertToJson(originalKey, bytes, mailDescription);
			storeConnector.put(jsonKey, json);
			
			PushDispatcher.getInstance().notifyUserOfEmail(
				toAddress, 
				mailDescription.author, 
				mailDescription.subject,
				mailDescription.body
			);
			
			log.info("handle using store " + storeConnector + " end");
		}
		catch (Exception exception)
		{
			log.info("handle using store " + storeConnector + " caught exception " + exception + " propagating.");
			throw exception;
		}
		finally
		{
			storeConnector.close();
			
			Date end = new Date();
			log.info("transaction took " + (end.getTime() - start.getTime()) + " milli seconds");
		}
	}
	
	public String getUserPrefixFor (Environment e)
	{
		if (e.containsKey(ConstantsEnvironmentKeys.IDENTITY))
			return e.get(ConstantsEnvironmentKeys.IDENTITY) + "/";
		
		return "";
	}
	
	public String getWriteKey ()
	{
		return new Date().getTime() + "_" + Math.abs(random.nextLong());
	}
	
	@Override
	public void handleIn(String toAddress, Environment e, InputStream m) throws Exception
	{
		log.info("handleIn");

		String key = getUserPrefixFor(e) + ConstantsStorage.NEW_IN;
		handle(toAddress, e, key, m);
	}
	
	public void handleOut(String toAddress, Environment e, InputStream m) throws Exception
	{
		log.info("handleOut");

		String key = getUserPrefixFor(e) + ConstantsStorage.NEW_OUT;
		handle(toAddress, e, key, m);		
	}
	
}
