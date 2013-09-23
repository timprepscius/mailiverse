/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package store.server.db.sql;

import core.constants.ConstantsServer;
import core.util.SqlCatalog;

public class Catalog extends SqlCatalog
{
	public static final String
		ENSURE_TABLES = "ensure_tables.sql",
		
		ADD_USER = "add_user.sql",
		ADD_USER_KEY_PAIR = "add_user_key_pair.sql",
		GET_USER_ID = "get_user_id.sql",
		REMOVE_USER = "remove_user.sql",
		GET_USER_ID_AND_SECRET_KEY = "get_user_id_and_secret_key.sql",
		
		LIST_KEY_VALUES = "list_keys.sql",
		REMOVE_KEY_VALUE = "remove_key_value.sql",
		PUT_KEY_VALUE = "put_key_value.sql",
		GET_KEY_VALUE = "get_key_value.sql";
	
	public String CONNECTION_STRING = ConstantsServer.DBCONNECTION_PREFIX + "store";
	public String USER = "store";
}
