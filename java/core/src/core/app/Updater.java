/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.app;

import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import core.constants.ConstantsClient;
import core.constants.ConstantsVersion;
import core.util.Streams;


public class Updater extends Thread
{
	static public final String VERSION_URL = "http://www.mailiverse.com/version/client";
	static public final String DESCRIPTION_URL = "http://www.mailiverse.com/version/client.description.txt";
	
	public void run ()
	{
		try
		{
			URL url = new URL(VERSION_URL);
			URLConnection c = url.openConnection();
			String response = Streams.readFullyString(c.getInputStream(), "UTF-8");
			
			response = response.replace("\n", " ");
			Pattern pattern = Pattern.compile ("^(.+?),(.*)$");
			Matcher matcher = pattern.matcher(response);
			
			if (matcher.matches())
			{
				String currentVersion = matcher.group(1);
				String downloadURL = matcher.group(2);
				
				if (!currentVersion.equals(ConstantsVersion.CLIENT))
				{
					String description = Streams.readFullyString(new URL(DESCRIPTION_URL).openConnection().getInputStream(), "UTF-8");
					UpdaterGUI.run(description, downloadURL);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static Thread execute ()
	{
		Thread thread = new Updater();
		thread.start();
		return thread;
	}
	
	public static void main (String[] args) throws InterruptedException
	{
		execute().join();
	}
}
