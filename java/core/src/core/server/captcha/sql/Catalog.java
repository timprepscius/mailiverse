/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.server.captcha.sql;

import core.constants.ConstantsServer;
import core.util.SqlCatalog;


public final class Catalog extends SqlCatalog
{
	static public final String CONNECTION_STRING = ConstantsServer.DBCONNECTION_PREFIX + "captcha";
	public static final String USER = "captcha";
	
	static public final String CREATE_TABLES = "create_tables";
	static public final String USE_TOKEN = "use_token";
	static public final String CHECK_TOKEN = "check_token";
	static public final String ADD_TOKEN = "add_token";
	static public final String PRUNE_TOKENS = "prune_tokens";
}
