/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.server.mailextra.sql;

import java.io.IOException;

import core.util.SqlCatalog;
import core.util.Streams;

public class Catalog extends SqlCatalog
{
	static public final String CONNECTION_STRING = "jdbc:mysql://localhost/mail_extra";
	public static final String USER = "mail_extra";
	
	static public final String GET_NOTIFICATIONS_FOR = "get_notifications_for.sql";
	static public final String SET_NOTIFICATIONS_FOR = "set_notifications_for.sql";
	static public final String PRUNE_DEVICES = "prune_devices.sql";
	
	static public final String CREATE_TABLES = "create_tables";
	static public final String ADD_DAYS_TO = "add_days_to";
	static public final String GET_DAYS_LEFT = "get_days_left";
}
