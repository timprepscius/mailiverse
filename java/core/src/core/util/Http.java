/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Http 
{
	public static String INTERNAL_ERROR = "An internal error occurred.";
	
	public static void mapParameters (URLConnection c, String... args)
	{
		for (int i=1; i<args.length;i+=2)
		{
			c.addRequestProperty(args[i-1], args[i]);
		}
	}
	
	public static void writeParameters (URLConnection c, String... args) throws UnsupportedEncodingException, IOException
	{
		boolean first = true;
		
		OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream());
		for (int i=1; i<args.length;i+=2)
		{
			if (!first)
				out.write("&");
			first = false;
			
			out.write(args[i-1]);
			out.write("=");
			out.write(URLEncoder.encode(args[i], "UTF-8"));
		}

		out.close();
		
	}
	
	public static Pair<byte[],Exception> readFully(HttpURLConnection c)
	{
		try
		{
			try
			{
				return new Pair<byte[], Exception>(Streams.readFullyBytes(c.getInputStream()),null);
			}
			catch (IOException e)
			{
				return new Pair<byte[], Exception>(Streams.readFullyBytes(c.getErrorStream()),e);
			}
		}
		catch (IOException e)
		{
			return new Pair<byte[], Exception>(null, e);
		}
	}
}
