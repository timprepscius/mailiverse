/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.server.captcha;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import core.server.captcha.sql.Catalog;
import core.util.Passwords;


public class Captcha 
{
	Random random = new SecureRandom();
	Catalog catalog = new Catalog();
	
	public static final String 
		SignUp = "SignUp", 
		CreateBucket = "CreateBucket";
	
	public void prune () throws SQLException, IOException
	{
		Connection connection = openConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(Catalog.PRUNE_TOKENS));
			log(statement);

			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
	}
	
	protected void insertCaptchToken (String token, String use) throws SQLException, IOException
	{
		Connection connection = openConnection();
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(Catalog.ADD_TOKEN));
			statement.setString(1, token + "_" + use);
			log(statement);
			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
	}
	
	public String captchaSucceeded () throws SQLException, IOException
	{
		String token = BigInteger.valueOf(Math.abs(random.nextLong())).toString(32);
		insertCaptchToken (token, SignUp);
		insertCaptchToken (token, CreateBucket);
		
		return token;		
	}
	
	public void useToken (String token, String use) throws SQLException, IOException
	{
		prune();
		
		Connection connection = openConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(Catalog.CHECK_TOKEN));
			statement.setString(1, token + "_" + use);
			log(statement);

			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				throw new IOException ("Null captcha");
			rs.close();
			
			statement = connection.prepareStatement (catalog.getSingle(Catalog.USE_TOKEN));
			statement.setString(1, token + "_" + use);
			log(statement);

			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
	}
	
	public void ensureTables() throws SQLException, IOException 
	{
		Connection connection = openConnection();

		try
		{
			for (String sql : catalog.getMulti(Catalog.CREATE_TABLES))
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
	
	public Connection openConnection () throws SQLException, IOException
	{
		return DriverManager.getConnection(Catalog.CONNECTION_STRING, Catalog.USER, Passwords.getPasswordFor(Catalog.USER));		
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
}
