/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.streamserver;

import java.math.BigInteger;
import java.util.Random;

import mail.server.db.ExternalData;
import mail.server.db.ExternalDataFactory;
import mail.server.db.MailUserDb;

import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsServer;
import core.exceptions.InternalException;
import core.exceptions.PublicMessageException;
import core.server.captcha.Captcha;
import core.server.mailextra.MailExtraDb;
import core.srp.server.SRPServerUserSessionDb;
import core.util.Environment;
import core.util.ExternalResource;
import core.util.JSONSerializer;
import core.util.LogOut;
import core.util.SimpleSerializer;
import core.util.Strings;
import core.util.Triple;

public class MailServerSessionDb implements SRPServerUserSessionDb
{
	boolean USE_CAPTCHA = 
		!ExternalResource.
			getTrimmedString(ConstantsServer.RECAPTCHA_PRIVATE_KEY).equals("NONE");
	
	
	static LogOut log = new LogOut(MailServerSessionDb.class);
	MailUserDb db;
	MailExtraDb payment;
	Captcha captcha;
	
	public MailServerSessionDb (MailUserDb db) throws Exception
	{
		this.db = db;
		this.captcha = new Captcha();
		this.payment = new MailExtraDb();
		payment.ensureTables();
	}

	public void setBlock (String userName, byte[] block) throws Exception
	{
		ExternalData externalData = ExternalDataFactory.createInstance();

		log.debug("setBlock", userName, Strings.toString(block));
		Environment e = JSONSerializer.deserialize(block);
		
		String newPassword = e.get(ConstantsEnvironmentKeys.SMTP_PASSWORD);

		externalData.setUserPassword(userName, newPassword);
		db.setBlock(userName, block);
	}

	public byte[] getBlock (String userName) throws Exception
	{
		log.debug("getBlock", userName);
		return db.getBlock(userName);
	}
	
	@Override
	public Triple<String, BigInteger, BigInteger> getUserVVS(String userName) throws Exception 
	{
		return db.getVVS(userName);
	}

	@Override
	public void createUser(String version, String userName, BigInteger v, BigInteger s, byte[] extra) throws Exception
	{
		log.debug("createUser", version, userName);
		try
		{
			ExternalData externalData = ExternalDataFactory.createInstance();

			if (USE_CAPTCHA)
			{
				String token = SimpleSerializer.deserialize(extra);
				captcha.useToken(token, Captcha.SignUp);
			}
			else
				System.out.println("Not checking captcha!");
			
			db.createUser(version, userName, v.toByteArray(), s.toByteArray());
			payment.addDaysTo(userName,0);
			
			Random random = new Random();
			String randomLong = BigInteger.valueOf(Math.abs(random.nextLong())).toString(32);
			
			externalData.addUser(userName, randomLong);
		}
		catch (PublicMessageException e)
		{
			log.debug("create user caught", e);
			throw e;
		} 
		catch (Exception e) 
		{
			log.debug("create user caught", e);
			throw new InternalException(e);
		} 
	}
	
	public void deleteUser(String userName)
	{
		log.debug("deleteUser", userName);
		try
		{
			ExternalData externalData = ExternalDataFactory.createInstance();

			db.deleteUser(userName);
			externalData.removeUser(userName);
		}
		catch (PublicMessageException e)
		{
			log.debug("create user caught", e);
			throw e;
		} 
		catch (Exception e) 
		{
			log.debug("create user caught", e);
			throw new InternalException(e);
		} 
	}
	
	@Override
	public void rateLimitFailure(String userName) throws PublicMessageException
	{
		try
		{
			// db.rateLimitFailure(userName);
		}
		catch (PublicMessageException e)
		{
			throw e;
		} 
		catch (Exception e) 
		{
			throw new InternalException(e);
		} 
	}

	@Override
	public void markFailure(String userName) throws PublicMessageException
	{
		log.debug("markFailure", userName);
		try
		{
			db.markFailure(userName);
		}
		catch (PublicMessageException e)
		{
			throw e;
		} 
		catch (Exception e) 
		{
			throw new InternalException(e);
		} 
	}
	
	public void checkRoomForNewUser() throws Exception
	{
		log.debug("checkRoomForNewUser");
		db.checkRoomForNewUser();
	}

	@Override
	public void testCreate(String version, String userName) throws Exception
	{
		log.debug("testCreate", userName);
		db.testCreateUser(version, userName);
	}
}
