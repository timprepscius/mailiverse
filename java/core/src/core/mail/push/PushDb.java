/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.push;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import core.crypt.Cryptor;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAJCE;
import core.server.mailextra.MailExtraDb;
import core.util.ExternalResource;
import core.util.LogOut;
import core.util.Strings;

public class PushDb
{
	LogOut log = new LogOut(PushDb.class);
    
	Cryptor cryptor; 
	MailExtraDb mailExtra;

	public PushDb () throws Exception
	{
		try
		{
			cryptor = new CryptorRSAAES(new CryptorRSAJCE(ExternalResource.getResourceAsStream(getClass(), "keystore.jks"), null));
			mailExtra = new MailExtraDb();
		}
		catch (Exception e)
		{
			log.exception(e);
			throw e;
		}
	}
	
	public void ensureDb () throws SQLException, IOException
	{
		mailExtra.ensureTables();
	}
	
	public void handleEncryptedBlock (byte[] block) throws Exception
	{
		String json = Strings.toString(cryptor.decrypt(block));
		mailExtra.handlePushNotificationsJson(new JSONObject(json));
	}
}
