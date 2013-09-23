package store.server.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import core.connector.FileInfo;
import core.util.Base64;
import core.util.FastRandom;
import core.util.LogOut;
import core.util.Pair;
import core.util.Passwords;
import store.server.db.sql.Catalog;

public class DbStore 
{
	FastRandom random = new FastRandom();
	LogOut log = new LogOut(DbStore.class);
	Catalog catalog = new Catalog();
	
	public void ensureTables() throws Exception 
	{
		Connection connection = openConnection();

		try
		{
			for (String sql : catalog.getMulti(Catalog.ENSURE_TABLES))
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
		
	public String putKeyValue (int userId, String key, byte[] value) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.PUT_KEY_VALUE);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			statement.setString(2, key);
			statement.setBytes(3, value);
			
			byte[] randomBytes = new byte[16];
			random.nextBytes(randomBytes);
			String randomB64 = Base64.encode(randomBytes);
			
			statement.setString(4, randomB64);
			statement.execute();
			
			return randomB64;
		}
		finally
		{
			closeConnection (connection);
		}
	}
	
	public Pair<String, byte[]> getKeyValue (int userId, String key) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.GET_KEY_VALUE);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			statement.setString(2, key);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
			{
				return Pair.create(results.getString("version"), results.getBytes("v"));
			}
			
			results.close();
		}
		finally
		{
			closeConnection (connection);
		}
		
		throw new Exception("Unknown key");
	}
	
	public List<FileInfo> listKeys (int userId, String key) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.LIST_KEY_VALUES);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			statement.setString(2, key);
			
			List<FileInfo> retval = new ArrayList<FileInfo>();
			ResultSet results = statement.executeQuery();
			while (results.next())
			{
				String path = results.getString("k");
				int size = results.getInt("size");
				Date date = results.getTimestamp("mark");
				String version = results.getString("version");
				
				FileInfo fileInfo = new FileInfo(path, path.substring(key.length()), size, date, version);
				retval.add(fileInfo);
			}
			
			results.close();
			
			return retval;
		}
		finally
		{
			closeConnection (connection);
		}		
	}
	
	public Pair<Integer, String> getUserIdAndSecretKey (String keyId) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.GET_USER_ID_AND_SECRET_KEY);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, keyId);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
			{
				return Pair.create(results.getInt("user_id"), results.getString("secret_key"));
			}
			
			results.close();
		}
		finally
		{
			closeConnection (connection);
		}
		
		throw new Exception("Unknown KeyId");
	}
	
	public void addUser (String userName) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.ADD_USER);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, userName);
			
			statement.execute();
		}
		finally
		{
			closeConnection (connection);
		}
	}
	
	public void removeUser (String userName) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String[] sqls = catalog.getMulti(Catalog.REMOVE_USER);
			
			boolean first = true;
			for (String sql : sqls)
			{
				PreparedStatement statement = connection.prepareStatement(sql);
				if (first)
					statement.setString(1, userName);
				
				statement.execute();
				first = false;
			}
		}
		finally
		{
			closeConnection (connection);
		}
	}
	
	public void removeKeyValue(int userId, String key) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.REMOVE_KEY_VALUE);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			statement.setString(2, key);
			
			statement.execute();
		}
		finally
		{
			closeConnection (connection);
		}
	}

	public int getUserId(String userName) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.GET_USER_ID);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, userName);
			
			ResultSet results = statement.executeQuery();
			if (results.next())
			{
				return results.getInt("user_id");
			}
			
			results.close();
		}
		finally
		{
			closeConnection (connection);
		}
		
		throw new Exception("Unknown user name");
	}
	

	public void addUserKeyPair (int userId, String keyId, String secretKey) throws Exception
	{
		Connection connection = null;
		
		try
		{
			connection = openConnection();
			String sql = catalog.getSingle(Catalog.ADD_USER_KEY_PAIR);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			statement.setString(2, keyId);
			statement.setString(3, secretKey);
			
			statement.execute();
		}
		finally
		{
			closeConnection (connection);
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
		log.debug (sql);
	}

}
