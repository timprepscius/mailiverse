/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.deploy;

import java.io.IOException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.PreparedStatement;

import core.crypt.PBE;
import core.exceptions.CryptoException;
import core.server.srp.db.UserDb;
import core.util.Base64;
import core.util.LogOut;
import core.util.Passwords;
import core.util.Strings;

import key.server.sql.KeyUserDb;
import mail.server.db.MailUserDb;
import mail.server.deploy.sql.Catalog;

public class TransferDataToBase64
{
	static LogOut log = new LogOut (TransferDataToBase64.class);
	static PBE mailPBEold, mailPBE;
	
	static public void createPBEs () throws CryptoException, IOException
	{
		char[] PASSWORD1 = "REMOVED".toCharArray();
		char[] PASSWORD2 = "1234567890".toCharArray();
		int LENGTH = PASSWORD1.length;

		char[] password = new char[LENGTH];
		for (int i=0; i<LENGTH; ++i)
			password[i] = (char)(PASSWORD1[i] ^ PASSWORD2[i]);
		
		mailPBEold = new PBE(new String(password), PBE.DEFAULT_SALT_0, PBE.DEFAULT_ITERATIONS, 256);
		mailPBE = new PBE(
			Passwords.getPasswordFor("mail-pbe"), 
			PBE.DEFAULT_SALT_0, PBE.DEFAULT_ITERATIONS, 256
		);
	}
	
	public static void main (String[] args) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");

		createPBEs();
		KeyUserDb _keyDb = new KeyUserDb();
		MailUserDb _mailDb = new MailUserDb();
		PreparedStatement ps;
		
		Catalog catalog = new Catalog();
		
		Object[][] blockTables = { {"key_block", _keyDb}, {"mail_block", _mailDb} };
		for (Object[] pair : blockTables)
		{
			String tableName = (String)pair[0];
			UserDb db = (UserDb)pair[1];
			
			Connection conn = db.openConnection();

			String[] sqlKeyCreateColumn = catalog.getMulti("TDB64_create_column.sql");
			for (String sql : sqlKeyCreateColumn)
			{
				ps = conn.prepareStatement(String.format(sql, tableName));
				log.debug(ps);
				ps.execute();
				ps.close();
			}
			
			while (true)
			{
				String sqlSelectNext = String.format(catalog.getSingle("TDB64_select_next.sql"), tableName);
				ps = conn.prepareStatement(sqlSelectNext);
				
				log.debug(ps);
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next())
					break;
				
				int id = rs.getInt("user_id");
				byte[] bytes = rs.getBytes("block");
				
				// transfer the PBE to the new one
				if (tableName.equals("mail_block"))
					bytes = mailPBE.encrypt(mailPBEold.decrypt(bytes));
				
				String b64 = Base64.encode(bytes);
				
				rs.close();
				ps.close();
				
				String sqlSetBlock = String.format(catalog.getSingle("TDB64_set_block.sql"), tableName);
				ps = conn.prepareStatement(sqlSetBlock);
				ps.setString(1, b64);
				ps.setInt(2, id);
				
				log.debug(ps);
				ps.executeUpdate();
				ps.close();
			}
			
			String[] sqlKeyFinishBlocks = catalog.getMulti("TDB64_finish_blocks.sql");
			for (String sql : sqlKeyFinishBlocks)
			{
				ps = conn.prepareStatement(String.format(sql, tableName));
				
				log.debug(ps);
				ps.execute();
				ps.close();
			}

			conn.close();
		}
		
		// do the user
		{
			MailUserDb db = _mailDb;
			Connection conn = db.openConnection();
			String[] sqlKeyCreateColumn = catalog.getMulti("TDB64_user_create_columns.sql");
			for (String sql : sqlKeyCreateColumn)
			{
				ps = conn.prepareStatement(sql);
				log.debug(ps);
				ps.execute();
				ps.close();
			}
			while (true)
			{
				String sqlSelectNext = String.format(catalog.getSingle("TDB64_select_next.sql"), "user");
				ps = conn.prepareStatement(sqlSelectNext);
				
				log.debug(ps);
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next())
					break;
				
				int id = rs.getInt("id");
				byte[] v = rs.getBytes("v");
				byte[] s = rs.getBytes("s");
				String v_b64 = Base64.encode(v);
				String s_b64 = Base64.encode(s);
				
				rs.close();
				ps.close();
				
				String sqlSetBlock = catalog.getSingle("TDB64_set_vs.sql");
				ps = conn.prepareStatement(sqlSetBlock);
				ps.setString(1, v_b64);
				ps.setString(2, s_b64);
				ps.setInt(3, id);
				
				log.debug(ps);
				ps.executeUpdate();
				ps.close();
			}
			
			String[] sqlKeyFinishVS = catalog.getMulti("TDB64_finish_vs.sql");
			for (String sql : sqlKeyFinishVS)
			{
				ps = conn.prepareStatement(sql);
				
				log.debug(ps);
				ps.execute();
				ps.close();
			}
			
			conn.close();
			log.debug("Finished");
		}
	}
	
}
