/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.IOException;
import java.util.ArrayList;

public class SqlCatalog
{
	static LogOut log = new LogOut(SqlCatalog.class);
	public SqlCatalog ()
	{
		try
		{
	        Class.forName("com.mysql.jdbc.Driver");
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public String getSingle (String name) throws IOException 
	{
		return Streams.readFullyString(getClass().getResourceAsStream(name), "UTF-8");
	}
	
	public String[] getMulti (String name) throws IOException
	{
		String[] sqls = getSingle(name).split(";");
		ArrayList<String> valid = new ArrayList<String>();
		for (String sql : sqls)
			if (!sql.trim().isEmpty())
				valid.add(sql);
		
		return valid.toArray(new String[0]);
	}	

}
