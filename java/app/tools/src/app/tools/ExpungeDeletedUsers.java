/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.util.Map;

import core.connector.s3.ClientInfoS3;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsStorage;
import core.util.Environment;
import core.util.LogOut;
import mail.server.db.MailUserDb;
import mail.server.storage.AWSStorageDelete;

public class ExpungeDeletedUsers
{
	static LogOut log = new LogOut(ExpungeDeletedUsers.class);
	
	public static void main (String[] _args) throws Exception
	{
		Map<String,String> args = Arguments.map(_args,0);
		
		ExpungeDeletedUsers expunger = new ExpungeDeletedUsers();
		String num = args.get("num");
		if (num == null)
			num = "-1";
		
		expunger.expungeQueue(Integer.parseInt(num), args.containsKey("force"));
	}
	
	public void expungeQueue(int maxToExpunge, boolean force) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");
		
		MailUserDb mailUserDb;
		mailUserDb = new MailUserDb();
		
		String user;
		AWSStorageDelete deletion = new AWSStorageDelete();
		while (maxToExpunge-- != 0 && (user = mailUserDb.getDeletedUser()) != null)
		{
			try
			{
				Environment e = mailUserDb.getDeletedUserEnvironment(user);
				
				if (e.containsKey(ConstantsEnvironmentKeys.HANDLER) && e.get(ConstantsEnvironmentKeys.HANDLER).equals(ConstantsStorage.HANDLER_S3))
				{
					ClientInfoS3 clientInfo = new ClientInfoS3(e.childEnvironment(ConstantsStorage.HANDLER_S3));
					deletion.delete(clientInfo.getBucketName());
				}
			}
			catch (Exception e)
			{
				if (!force)
					throw e;
				
				log.exception(e);
			}
			
			try
			{
				mailUserDb.expungeUser(user);
			}
			catch (Exception e)
			{
				if (!force)
					throw e;
				
				log.exception(e);
			}
		}
	}
}
