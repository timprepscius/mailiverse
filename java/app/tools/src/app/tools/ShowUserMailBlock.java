/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import core.exceptions.CryptoException;
import core.util.Environment;
import core.util.JSONSerializer;

import mail.server.db.MailUserDb;

public class ShowUserMailBlock
{
	public static void main (String[] _args) throws CryptoException, IOException, SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		
		Map<String,String> args = Arguments.map(_args,0);
		String user = args.get("user");
		if (user == null)
			throw new IllegalArgumentException();
		
		MailUserDb db = new MailUserDb();
		byte[] block = db.getBlock(user);
		Environment e = JSONSerializer.deserialize(block);
		for (Entry<String, String> i : e.entrySet())
		{
			System.out.println(i.getKey() + ":" + i.getValue());
		}
	}
}
