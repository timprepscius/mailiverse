/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import core.callback.Callback;

public class HttpDelegateJava extends HttpDelegate
{
	@Override
	public void execute(String action, String url, String[][] headers, boolean binaryInput, boolean binaryOutput, byte[] contents, Callback callback)
	{
		try
		{
			URLConnection urlConnection = new URL(url).openConnection();

			HttpURLConnection httpCon = (HttpURLConnection) urlConnection;
			
			httpCon.setRequestMethod(action);
			if (headers != null)
			{
				for (String[] s : headers)
				{
					httpCon.setRequestProperty(s[0], s[1]);
				}
			}
			
			if (contents != null)
			{
				httpCon.setDoOutput(true);
				httpCon.setRequestMethod("PUT");		
				httpCon.setRequestProperty("CONTENT-LENGTH", "" + contents.length);

				BufferedOutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
				os.write(contents);
				os.close();
			}				

			byte[] bytes = Streams.readFullyBytes(urlConnection.getInputStream());
		
			ArrayList<String[]> responseHeaders = new ArrayList<String[]>();
			for (Entry<String, List<String>> i : httpCon.getHeaderFields().entrySet())
			{
				for (String j : i.getValue())
				{
					String key = i.getKey();
					String value = j;

					if (key == null)
					{
						key = value;
						value = null;
					}
					
					responseHeaders.add(new String[] { key, value });
				}
			}
			
			String[][] rh = new String[responseHeaders.size()][];
			int j=0;
			for (String[] i : responseHeaders)
				rh[j++] = i;
			
			if (binaryOutput)
				callback.invoke(bytes, rh);
			else
			{
				String s = new String(bytes);
				callback.invoke(s, rh);
			}
			
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}
}
