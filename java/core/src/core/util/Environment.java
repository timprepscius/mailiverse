/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.connector.FileInfo;
//import core.connector.sync.StoreConnector;


public class Environment extends HashMap<String,String>
{
	private static final long serialVersionUID = 1L;

	public Environment ()
	{
	}

/*
	public static Environment fromStore (StoreConnector connector) throws Exception
	{
		Environment e = new Environment ();
		connector.open();
		e.readFromStore(connector);
		connector.close();
		return e;
	}
	
	public void readFromStore (StoreConnector connector) throws Exception
	{
		List<FileInfo> files = connector.listDirectory("");
		for (FileInfo file : files)
		{
			put(file.relativePath, new String(connector.get(file.relativePath), "UTF-8"));
		}
	}
	
	public static void toStore (StoreConnector connector, Environment e) throws Exception
	{
		connector.open();
		e.writeToStore(connector);
		connector.close();
	}
	
	public void writeToStore (StoreConnector connector) throws Exception
	{
		for (String key : keySet())
			connector.put(key, Strings.toBytes(this.get(key)));
	}
*/
	
	public String checkGet (String key)
	{
		// find a better exception
		if (!containsKey(key))
			throw new NullPointerException("Unknown key: " + key);
		
		return get(key);
	}
	
	public Environment childEnvironment (String key)
	{
		Environment e = new Environment();
		String prefix = key + "/";
		int prefixLength = prefix.length();
		
		for (Map.Entry<String, String> i : entrySet())
		{
			String k = i.getKey();
			if (k.startsWith(prefix))
			{
				e.put(k.substring(prefixLength), i.getValue());
			}
		}
		
		return e;
	}
	
	public void addChildEnvironment (String key, Environment e)
	{
		String prefix = key + "/";
		
		for (Map.Entry<String, String> i : e.entrySet())
		{
			String k = i.getKey();
			this.put(prefix + k, i.getValue());
		}
	}
	
	public boolean hasChildEnvironment (String key)
	{
		String prefix = key + "/";
		int prefixLength = prefix.length();
		
		for (Map.Entry<String, String> i : entrySet())
		{
			String k = i.getKey();
			if (k.startsWith(prefix))
			{
				return true;
			}
		}
		
		return false;
	}
}
