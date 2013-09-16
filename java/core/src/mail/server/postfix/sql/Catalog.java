/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package mail.server.postfix.sql;

import java.io.IOException;
import core.constants.ConstantsServer;
import core.util.Passwords;
import core.util.SqlCatalog;


public final class Catalog extends SqlCatalog
{
	public String CONNECTION_STRING = ConstantsServer.DBCONNECTION_PREFIX + "postfix";
	public String USER = "postfix";
	
	public int FAILURE_TIMEOUT_SECONDS = 60;
	
	public final String 
		CREATE_TABLES = "create_tables.sql",
		ADD_USER = "add_user.sql",
		REMOVE_USER = "remove_user.sql",
		CHANGE_PASSWORD = "change_password.sql";

	public Catalog ()
	{
		
	}
	
	public String getPassword () throws IOException
	{
		return Passwords.getPasswordFor(USER);
	}
}
