/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.streamserver;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Random;

import mail.server.db.MailUserDb;

import org.apache.james.cli.probe.impl.JmxServerProbe;

import core.constants.ConstantsEnvironmentKeys;
import core.exceptions.InternalException;
import core.exceptions.PublicMessageException;
import core.server.captcha.Captcha;
import core.server.mailextra.MailExtraDb;
import core.srp.server.SRPServerUserSessionDb;
import core.util.Environment;
import core.util.JSONSerializer;
import core.util.LogOut;
import core.util.SimpleSerializer;
import core.util.Strings;
import core.util.Triple;


public class MailServerSessionDb implements SRPServerUserSessionDb
{
	static final boolean USE_CAPTCHA = true;
	
	
	static LogOut log = new LogOut(MailServerSessionDb.class);
	MailUserDb db;
	MailExtraDb payment;
	Captcha captcha;
	
	public MailServerSessionDb (MailUserDb db) throws SQLException, IOException
	{
		this.db = db;
		this.captcha = new Captcha();
		this.payment = new MailExtraDb();
		payment.ensureTables();
	}

	public void setBlock (String userName, byte[] block) throws Exception
	{
		log.debug("setBlock", userName, Strings.toString(block));
		Environment e = JSONSerializer.deserialize(block);
		
		String newPassword = e.get(ConstantsEnvironmentKeys.SMTP_PASSWORD);

		JmxServerProbe jamesConnection = new JmxServerProbe("localhost");	
		if (newPassword != null)
			jamesConnection.setPassword(userName, newPassword);
		
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
			JmxServerProbe jamesConnection = new JmxServerProbe("localhost");	

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
			
			jamesConnection.addUser(userName, randomLong);
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
			JmxServerProbe jamesConnection = new JmxServerProbe("localhost");	

			db.deleteUser(userName);
			jamesConnection.removeUser(userName);
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
