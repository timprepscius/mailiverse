package mail.server.postfix;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import core.util.LogOut;
import core.util.Passwords;
import mail.server.db.ExternalData;
import mail.server.postfix.sql.Catalog;

public class ExternalDataPostfix implements ExternalData 
{
	Catalog catalog = new Catalog();
	LogOut log = new LogOut (ExternalDataPostfix.class);
	
	public ExternalDataPostfix() throws Exception
	{
		ensureTables();
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
	
	@Override
	public void addUser(String name, String password) throws Exception 
	{
		Connection connection = openConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.ADD_USER));
			statement.setString(1, name);
			statement.setString(2, password);
			
			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
	}
	
	@Override
	public void removeUser (String name) throws Exception
	{
		Connection connection = openConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.REMOVE_USER));
			statement.setString(1, name);
			
			statement.executeUpdate();
		}
		finally
		{
			closeConnection(connection);
		}
		
	}

	@Override
	public void setUserPassword(String name, String password) throws Exception
	{
		Connection connection = openConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(catalog.CHANGE_PASSWORD));
			statement.setString(1, password);
			statement.setString(2, name);
			
			statement.executeUpdate();
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
		log.debug (sql);
	}
}
