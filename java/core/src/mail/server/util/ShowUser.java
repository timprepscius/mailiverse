/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.crypto.NoSuchPaddingException;

import core.exceptions.CryptoException;
import core.util.Environment;
import mail.server.db.MailUserDb;

public class ShowUser
{
	public static void main (String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, SQLException, CryptoException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");

		String userName = "";
		
		MailUserDb db = new MailUserDb();
		Environment e = db.getUserEnvironment(userName);
		
		for (String k : e.keySet())
		{
			System.out.println("k " + k + " " + e.get(k));
		}
	}
}
