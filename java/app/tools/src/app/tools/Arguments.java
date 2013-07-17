/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.util.HashMap;
import java.util.Map;

public class Arguments
{
	public static Map<String,String> map (String[] s, int offset)
	{
		Map<String,String> map = new HashMap<String,String>();
		
		for (int i=offset; i<s.length; ++i)
		{
			String kv = s[i];
			String k, v=null;
			if (kv.matches(".*=.*"))
			{
				k = kv.replaceAll("(.*)=(.*)", "$1");
				v = kv.replaceAll("(.*)=(.*)", "$2");
			}
			else
				k = kv;
			
			map.put(k.toLowerCase(),v);
		}
		
		return map;
	}
	
	public static boolean containsAll(Map<String,String> kv, String[] keys)
	{
		for (String k : keys)
		{
			if (!kv.containsKey(k))
				return false;
		}
		
		return true;
	}
	

}
