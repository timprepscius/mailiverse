/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.server.mailextra;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bc.util.Strings;
import org.json.JSONException;
import org.json.JSONObject;

import core.util.Base64;
import core.constants.ConstantsPushNotifications;
import core.crypt.HashSha256;
import core.server.mailextra.sql.Catalog;
import core.util.LogOut;
import core.util.Pair;
import core.util.Passwords;
import core.util.Triple;

public class MailExtraDb
{
	LogOut log = new LogOut(MailExtraDb.class);
	Catalog catalog = new Catalog();
	
	public String getUserHash (String email)
	{
		HashSha256 hasher = new HashSha256();
		return Base64.encode(hasher.hash(Strings.toByteArray(email.toLowerCase())));
	}
	
	public Triple<String,String,String> getDeviceFor (String email) throws SQLException, IOException
	{
		return getDeviceForHash(getUserHash(email));
	}
	
	public Triple<String,String,String> getDeviceForHash (String hash) throws SQLException, IOException
	{

		Connection connection = openConnection();
		Triple<String,String,String> result;
		
		try
		{
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(Catalog.GET_NOTIFICATIONS_FOR));
			statement.setString(1, hash);
			log.debug(statement);

			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				return null;
			
			result = new Triple<String,String,String>(rs.getString("notification_type"), rs.getString("device_type"), rs.getString("device_id"));
		}
		finally
		{
			closeConnection(connection);
		}
		
		return result;
	}

	public void setNotificationsFor (String email, String notificationType, String deviceType, String deviceId) throws SQLException, IOException
	{
		setNotificationsFor (getUserHash(email), deviceType, deviceId, notificationType);
	}
	
	public void setNotificationsForHash (String hash, String notificationType, String deviceType, String deviceId) throws SQLException, IOException
	{
		Connection connection = openConnection();
		
		try
		{
			String statements[] = catalog.getMulti(Catalog.SET_NOTIFICATIONS_FOR);
			
			boolean first = true;
			for (String s : statements)
			{
				PreparedStatement statement = connection.prepareStatement (s);
				
				if (first)
				{
					statement.setString(1, hash);
					statement.setString(2, notificationType);
					statement.setString(3, deviceType);
					statement.setString(4, deviceId);
				}
				
				log.debug(statement);
				statement.executeUpdate();
				first = false;
			}
		}
		finally
		{
			closeConnection(connection);
		}
	}
	
	public void addDaysTo (String email, int days) throws SQLException, IOException
	{
		Connection connection = openConnection();
		
		try
		{
			String statements[] = catalog.getMulti(Catalog.ADD_DAYS_TO);
			
			boolean first = true;
			for (String s : statements)
			{
				PreparedStatement statement = connection.prepareStatement (s);
				
				if (first)
				{
					statement.setString(1, email);
					statement.setInt(2, days);
				}
				
				
				log.debug(statement);
				statement.executeUpdate();
				first = false;
			}
		}
		finally
		{
			closeConnection(connection);
		}
	}

	public int getDaysLeft (String email) throws SQLException, IOException
	{
		Connection connection = openConnection();
		
		try
		{
			log.debug(catalog.getSingle(Catalog.GET_DAYS_LEFT));
			PreparedStatement statement = connection.prepareStatement (catalog.getSingle(Catalog.GET_DAYS_LEFT));
			statement.setString(1, email);
			log.debug(statement);

			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				throw new IOException ("No results");
			
			int days = rs.getInt("days");
			rs.close();
			
			return days;
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
				log.debug(statement);
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

	public void handlePushNotificationsJson(JSONObject json) throws JSONException, SQLException, IOException
	{
		String user = json.getString(ConstantsPushNotifications.USER);
		
		setNotificationsForHash(
			user, 
			json.getString(ConstantsPushNotifications.NOTIFICATION_TYPE),
			json.has(ConstantsPushNotifications.DEVICE_TYPE) ? 
				json.getString(ConstantsPushNotifications.DEVICE_TYPE) : null,
			json.has(ConstantsPushNotifications.DEVICE_ID) ? 
					json.getString(ConstantsPushNotifications.DEVICE_ID) : null
		);
	}

	public void removeDevice(String deviceType, String deviceId)
	{
	}

}
