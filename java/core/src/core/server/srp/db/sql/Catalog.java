/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.server.srp.db.sql;

import java.io.IOException;
import java.sql.SQLException;

import core.constants.ConstantsServer;
import core.util.Passwords;
import core.util.SqlCatalog;
import core.util.Streams;


public final class Catalog extends SqlCatalog
{
	public String CONNECTION_STRING = ConstantsServer.DBCONNECTION_PREFIX + "mail";
	public String USER = "mail";
	
	public int FAILURE_TIMEOUT_SECONDS = 60;
	
	public final String 
		CREATE_TABLES = "create_tables",
		CREATE_USER = "create_user",
		GET_USER = "get_user",
		GET_USER_MAIL_BLOCK = "get_user_mail_block",
		SET_USER_MAIL_BLOCK = "set_user_mail_block",
		GET_USER_KEY_BLOCK = "get_user_key_block",
		SET_USER_KEY_BLOCK = "set_user_key_block",
		GET_LAST_FAILURE = "get_last_failure",
		MARK_FAILURE = "mark_failure",
		
		DELETE = "delete_user.sql",
		EXPUNGE = "expunge_deleted_user.sql",
		GET_DELETED_USER = "get_deleted_user.sql",
		GET_DELETED_USER_MAIL_BLOCK = "get_deleted_user_mail_block.sql",
		
		ROOM_FOR_NEW_USER = "room_for_new_user";

	public Catalog ()
	{
		
	}
	
	public String getPassword () throws IOException
	{
		return Passwords.getPasswordFor(USER);
	}
}
