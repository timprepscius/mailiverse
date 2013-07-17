/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.server;

import java.math.BigInteger;

import key.streamserver.SRPProtocolHandler;

import core.exceptions.PublicMessageException;
import core.srp.server.SRPServerUserSessionDb;
import core.util.LogOut;
import core.util.Triple;

import core.server.srp.db.UserDb;

public class KeyServerSessionDb implements SRPServerUserSessionDb
{
	static LogOut log = new LogOut(SRPProtocolHandler.class);
	UserDb db;
	
	public KeyServerSessionDb (UserDb db)
	{
		this.db = db;
	}

	public void setBlock (String userName, byte[] bytes) throws Exception
	{
		log.debug("setBlock", userName);
		db.setBlock(userName, bytes);
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
		throw new PublicMessageException("Not supported");
	}

	@Override
	public void rateLimitFailure(String userName) throws Exception
	{
//		db.rateLimitFailure(userName);
	}

	@Override
	public void markFailure(String userName) throws Exception
	{
		log.debug("markFailure", userName);
		db.markFailure(userName);
	}

	@Override
	public void testCreate(String version, String userName) throws Exception
	{
		log.debug("testCreate", version, userName);
		db.testCreateUser(version, userName);
	}
}
