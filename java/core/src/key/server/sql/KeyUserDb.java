/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.server.sql;

import java.io.IOException;
import java.sql.SQLException;

import core.server.srp.db.UserDb;
import core.server.srp.db.sql.Catalog;

public class KeyUserDb extends UserDb 
{
	public KeyUserDb ()
	{
		super (new Catalog());
	}
	
	public byte[] getBlock (String userName) throws IOException, SQLException
	{
		return super.getKeyBlock(userName);
	}
	
	public byte[] setBlock(String userName, byte[] block) throws IOException, SQLException
	{
		return super.setKeyBlock(userName, block);
	}	
}
