/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.server.srp.db;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import core.exceptions.CryptoException;
import core.exceptions.PublicMessageException;
import core.exceptions.UserExistsException;
import core.server.srp.db.sql.Catalog;
import core.util.LogOut;
import core.util.Pair;
import core.util.Passwords;
import core.util.Strings;
import core.util.Triple;
import core.util.Base64;

public abstract class UserDb 
{
	static LogOut log = new LogOut(UserDb.class);
	
	SecureRandom random = new SecureRandom();
	Catalog catalog;
	
	protected UserDb (Catalog catalog)
	{
		this.catalog = catalog;
	}
	
	public void testCreateUser (String version, String userName) throws Exception
	{
		checkRoomForNewUser();
		testIllegalUserName(userName);
		
		if (getUser(userName)!=null)
			throw new UserExistsException();
	}
	
	public void checkRoomForNewUser () throws Exception
	{
		Connection connection = openConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.ROOM_FOR_NEW_USER));
			ResultSet results = statement.executeQuery();
			if (results.next())
			{
				boolean hasRoom = results.getBoolean("room");
				if (hasRoom)
					return;
			}
			
			throw new PublicMessageException("No room for new users");
		}
		finally
		{
			closeConnection(connection);
		}
		
	}
	
	public Integer getUserId (String userName) throws IOException, SQLException
	{
		return getUser(userName).first;
	}
	
	public void createUser(String version, String userName, byte[] v, byte[] s) throws Exception
	{
		checkRoomForNewUser();
		testIllegalUserName(userName);
		
		if (getUser(userName)!=null)
			throw new UserExistsException();
		
		Connection connection = openConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.CREATE_USER));
			statement.setString(1, version);
			statement.setString(2, userName);
			statement.setString(3, Base64.encode(v));
			statement.setString(4, Base64.encode(s));
			log(statement);
			
			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
	}
	
	
	public Pair<Integer, Triple<String, byte[], byte[]> > getUser (String userName) throws IOException, SQLException
	{
		Connection connection = openConnection();

		Pair<Integer, Triple<String, byte[], byte[]>> result = null;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.GET_USER));
			statement.setString(1, userName);
			log(statement);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
			{
				result = 
					new Pair<Integer, Triple<String, byte[], byte[] > >(
						results.getInt("id"),
						new Triple<String, byte[], byte[]> (
							results.getString("version"),
							Base64.decode(results.getString("v")),
							Base64.decode(results.getString("s"))
						)
					);
			}
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;		
	}
	
	public Triple<String, BigInteger, BigInteger> getVVS (String userName) throws IOException, SQLException
	{
		Triple<String, byte[], byte[]> vvs = getUser(userName).second;
		return 
			new Triple<String, BigInteger, BigInteger>(
				vvs.first,
				new BigInteger (vvs.second),
				new BigInteger (vvs.third)
			);
	}
	
	protected byte[] setMailBlock (String userName, byte[] block) throws IOException, SQLException
	{
		Integer id = getUser(userName).first;
		
		Connection connection = openConnection();

		byte[] result = null;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.SET_USER_MAIL_BLOCK));
			statement.setInt(1, id);
			statement.setString (2, Base64.encode(block));
			log(statement);
			
			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;
	}

	abstract public byte[] setBlock (String userName, byte[] block) throws IOException, SQLException, CryptoException;
	abstract public byte[] getBlock (String userName) throws IOException, SQLException, CryptoException;
	
	protected byte[] setKeyBlock (String userName, byte[] block) throws IOException, SQLException
	{
		Integer id = getUser(userName).first;
		
		Connection connection = openConnection();

		byte[] result = null;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.SET_USER_KEY_BLOCK));
			statement.setInt(1, id);
			statement.setString (2, Base64.encode(block));
			log(statement);
			
			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;
	}

	protected byte[] getMailBlock (String userName) throws IOException, SQLException
	{
		Connection connection = openConnection();

		byte[] result = null;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.GET_USER_MAIL_BLOCK));
			statement.setString(1, userName);
			log(statement);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
				result = Base64.decode(results.getString("block"));
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;
	}
	
	public byte[] getDeletedMailBlock(String userName) throws IOException, SQLException
	{
		Connection connection = openConnection();

		byte[] result = null;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.GET_DELETED_USER_MAIL_BLOCK));
			statement.setString(1, userName);
			log(statement);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
				result = Base64.decode(results.getString("block"));
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;
	}
	
	public String getDeletedUser() throws IOException, SQLException
	{
		Connection connection = openConnection();

		String result = null;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.GET_DELETED_USER));
			log(statement);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
				result = results.getString("name");
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;
	}

	protected byte[] getKeyBlock (String userName) throws IOException, SQLException
	{
		Connection connection = openConnection();

		byte[] result = null;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.GET_USER_KEY_BLOCK));
			statement.setString(1, userName);
			log(statement);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
				result = Base64.decode(results.getString("block"));
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;
	}
	
	public void ensureTables() throws SQLException, IOException 
	{
		Connection connection = openConnection();
		try
		{
			for (String sql : catalog.getMulti(catalog.CREATE_TABLES))
			{
				PreparedStatement statement = connection.prepareStatement (sql);
				log(statement);
				statement.executeUpdate();
			}
		}
		finally
		{
			closeConnection(connection);
		}
	}
	
	public void rateLimitFailure (String userName) throws SQLException, IOException
	{
		Connection connection = openConnection();
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.GET_LAST_FAILURE));
			statement.setString(1, userName);
			log(statement);
			
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				Timestamp timeStamp = rs.getTimestamp("mark");
				Date now = new Date();
				
				if (now.getTime() - timeStamp.getTime() < catalog.FAILURE_TIMEOUT_SECONDS * 1000)
					throw new PublicMessageException ("Too many failures, try again later.");
			}
		}
		finally
		{
			closeConnection(connection);
		}		
	}
	
	public void markFailure (String userName) throws SQLException, IOException
	{
		Connection connection = openConnection();
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.MARK_FAILURE));
			statement.setString(1, userName);
			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}		
		
	}
	
	public void deleteUser(String userName) throws IOException, SQLException
	{
		Connection connection = openConnection();
		try
		{
			String[] texts = catalog.getMulti(catalog.DELETE);
			for (String text : texts)
			{
				PreparedStatement statement = connection.prepareStatement (text);
				statement.setString(1, userName);
				
				log(statement);
				statement.executeUpdate();
			}
		}
		finally
		{
			closeConnection(connection);
		}		
	}
	
	public void expungeUser(String userName) throws IOException, SQLException
	{
		Connection connection = openConnection();
		try
		{
			String[] texts = catalog.getMulti(catalog.EXPUNGE);
			for (String text : texts)
			{
				PreparedStatement statement = connection.prepareStatement (text);
				statement.setString(1, userName);
				statement.executeUpdate();
			}
		}
		finally
		{
			closeConnection(connection);
		}		
	}

	public Connection openConnection () throws IOException, SQLException
	{
		log.debug("Connecting to", catalog.CONNECTION_STRING);
		return DriverManager.getConnection(catalog.CONNECTION_STRING, catalog.USER, Passwords.getPasswordFor(catalog.USER));		
	}
	
	public void closeConnection (Connection connection)
	{
		try
		{
			if (connection != null)
				connection.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	public void log (Statement sql)
	{
		System.out.println (sql);
	}

	protected void testIllegalUserName(String userName) throws Exception
	{
		// http://www.ietf.org/rfc/rfc2142.txt
		final String[] illegalStartsWith = {
			"info",
			"marketing",
			"sales",
			"support",
			
			"abuse",
			"noc",
			"security",

			"postmaster",
			"hostmaster",
			"usenet",
			"news",
			"webmaster",
			"www",
			"uucp",
			"ftp",
			
			"admin",
			"system",
			"root",
			"test",
			"root",
			"hostma",
			"web",
			"post",
			"mail",
		};

		final String[] illegalParts = {
			"postmaster",
			"webmaster",
			"root",
			"admin",
			"system",
		};
		
		String username = userName.toLowerCase();
		for (String illegal : illegalParts)
		{
			if (username.indexOf(illegal) != -1)
				throw new Exception("Illegal username");
		}
		
		for (String illegal : illegalStartsWith)
		{
			if (username.startsWith(illegal))
				throw new Exception("Illegal username");
		}
	}
}
