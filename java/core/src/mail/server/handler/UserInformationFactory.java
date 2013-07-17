/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;

import mail.server.db.MailUserDb;

import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsStorage;
import core.util.Environment;
import core.util.LogOut;


public class UserInformationFactory
{
	private static UserInformationFactory singleton;
	static LogOut log = new LogOut(UserInformationFactory.class);

	MailUserDb userDb;
	
	public UserInformationFactory () throws Exception
	{
		userDb = new MailUserDb();	
		userDb.ensureTables();
	}
	
	public static UserInformationFactory getInstance () throws Exception
	{
		if (singleton == null)
		{
			singleton = new UserInformationFactory();
		}		
		
		return singleton;
	}
	
	public UserInformation getUserInformation (String toAddress) throws Exception
	{
		System.out.println("looking for user information for " + toAddress);
		
		Environment environment = userDb.getUserEnvironment(toAddress);
			
		String handlerName = environment.get(ConstantsEnvironmentKeys.HANDLER);
		MailetHandler mailetHandler = null;
		
		if (handlerName.equals(ConstantsStorage.HANDLER_S3))
			mailetHandler = new MailetHandlerS3();
		else
		if (handlerName.equals(ConstantsStorage.HANDLER_DROPBOX))
			mailetHandler = new MailetHandlerDropbox();
		else
			throw new Exception("Unknown handler");
		
		return new UserInformation (toAddress, environment, mailetHandler);
	}
	
	public void recordFailure (String toAddress, Throwable t)
	{
		log.debug("Recording delivery failure for (todo encrypt)[" + toAddress + "] with exception " + t);
	}
}
