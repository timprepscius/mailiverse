/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.io.IOException;
import java.sql.SQLException;

import core.exceptions.CryptoException;
import mail.server.db.MailUserDb;
import mail.streamserver.MailServerSessionDb;

public class ScheduleUserForDeletion
{
	public static void main (String[] args) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");

		String user = Arguments.map(args, 0).get("user");
		MailServerSessionDb db = new MailServerSessionDb(new MailUserDb());
		db.deleteUser(user);
	}
}
