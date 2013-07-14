/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.FileInputStream;
import java.io.InputStream;

public class ExternalResource
{
	static LogOut log = new LogOut(ExternalResource.class);
	
	static protected String prefix;
	
	static {
		prefix = System.getProperty("user.home") + "/resources/";
	}
	
	public static byte[] get(String key) throws Exception
	{
		String path = prefix + key;
		log.debug("get", path);
		return Streams.readFullyBytes(new FileInputStream(path));
	}

	public static InputStream getResourceAsStream(Class<?> c, String key) throws Exception
	{
		String path = prefix + c.getPackage().getName() + "/" + key;
		log.debug("getResourceAsStream", path);
		return new FileInputStream(path);
	}

	public static String getTrimmedString(String key) throws Exception
	{
		return Strings.toString(get(key)).trim();
	}
}
