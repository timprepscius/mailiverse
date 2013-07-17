/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.deploy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import key.server.sql.KeyUserDb;
import mail.server.db.MailUserDb;
import mail.server.deploy.sql.Catalog;

import core.crypt.CryptorAES;
import core.crypt.CryptorAESIV;
import core.server.srp.db.UserDb;
import core.util.Arrays;
import core.util.Base64;
import core.util.LogOut;

public class AddNullIVsToExistingUserBlocks
{
	static LogOut log = new LogOut (TransferDataToBase64.class);
	
	public static void main (String[] args) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");
		
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

			String[] sqlKeyCreateColumn = catalog.getMulti("ANIV_prepare.sql");
			for (String sql : sqlKeyCreateColumn)
			{
				ps = conn.prepareStatement(String.format(sql, tableName));
				log.debug(ps);
				ps.execute();
				ps.close();
			}
			
			while (true)
			{
				String sqlSelectNext = String.format(catalog.getSingle("ANIV_select_next.sql"), tableName);
				ps = conn.prepareStatement(sqlSelectNext);
				
				log.debug(ps);
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next())
					break;
				
				int id = rs.getInt("user_id");
				byte[] bytes = Base64.decode(rs.getString("block"));
				bytes = Arrays.concat(CryptorAES.NullIV, bytes);
				
				rs.close();
				ps.close();
				
				String sqlSetBlock = String.format(catalog.getSingle("ANIV_set.sql"), tableName);
				ps = conn.prepareStatement(sqlSetBlock);
				ps.setString(1, Base64.encode(bytes));
				ps.setInt(2, id);
				
				log.debug(ps);
				ps.executeUpdate();
				ps.close();
			}
			
			String[] sqlKeyFinishBlocks = catalog.getMulti("ANIV_finish.sql");
			for (String sql : sqlKeyFinishBlocks)
			{
				ps = conn.prepareStatement(String.format(sql, tableName));
				
				log.debug(ps);
				ps.execute();
				ps.close();
			}

			conn.close();
		}
		
	}
}
