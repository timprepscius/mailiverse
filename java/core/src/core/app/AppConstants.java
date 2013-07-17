/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.app;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.util.Streams;
import core.util.Strings;


public class AppConstants
{
	public static String 
		USER_HOME,
		DOCUMENTS,
		AUTORUNS,
		RUNNING_JAR,
		RUNNING_JAR_FILE,
		RUNNING_JAR_DIRECTORY,
		RUNNING_APP,
		RUNNING_APP_PARENT,
		PROGRAM_FILES,
		DOCUMENTS_CLIENT,
		DOCUMENTS_CLIENT_REDIRECT,
		AUTORUN_SCRIPT,
		OS;

	static final String CLIENT_JAR = "Mailiverse.jar";
	static final String SETUP_JAR = "Setup.jar";
	
	static final String INTEGRATE_MACMAIL_SCRIPT = "Mailiverse-Integrate-MacMail.script";
	static final String QUERY_DIRECTORIES_VBSCRIPT = "QueryDirectories.vbs";
	
	static final String AUTORUN_MAC = "Mailiverse-AutoLaunch.plist";
	static final String AUTORUN_WINDOWS = "Mailiverse-AutoRun.vbs";
	
	public static final String HELP_PAGE = "http://www.mailiverse.com/help.html";
	public static final String MANUAL_S3 = "http://www.mailiverse.com//manual-s3.html";
	public static final String MANUAL_DROPBOX = "http://www.mailiverse.com/manual-s3.html";
	public static final String MANUAL_RSA = "http://www.mailiverse.com/manual-rsa.html";
	
	
	static {
		queryDirectories();
	}

	public static String toOS (String app)
	{
		try
		{
			if (OS.startsWith("Windows"))
			{
				if (Pattern.matches("^/[a-zA-Z]:.*", app))
					app = app.substring(1);
				
				app = "\"" + URLDecoder.decode(app, "UTF-8").replace("/", "\\") + "\"";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return app;
	}

	static public void queryDirectories ()
	{
		OS = System.getProperty ("os.name");
		
		USER_HOME = System.getProperty("user.home");
		RUNNING_JAR = UserUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		{
			Pattern pattern = Pattern.compile("(.+)/(.+?)");
			Matcher matcher = pattern.matcher(RUNNING_JAR);
			if (matcher.matches())
			{
				RUNNING_JAR_DIRECTORY = matcher.group(1);
				RUNNING_JAR_FILE = matcher.group(2);
			}
		}
			
		if (OS.startsWith("Mac"))
		{
			PROGRAM_FILES = "/Applications";
			DOCUMENTS = USER_HOME + "/Documents";
			AUTORUNS = USER_HOME + "/Library/LaunchAgents";
			
			{
				Pattern pattern = Pattern.compile("(.+)/Contents/Resources/Java/.+?");
				Matcher matcher = pattern.matcher(RUNNING_JAR);
				if (matcher.matches())
					RUNNING_APP = matcher.group(1);
			}
			
			{
				Pattern pattern = Pattern.compile("(.+)/Contents/.+?/Contents/Resources/Java/.+?");
				Matcher matcher = pattern.matcher(RUNNING_JAR);
				if (matcher.matches())
					RUNNING_APP_PARENT = matcher.group(1);
			}
			
			AUTORUN_SCRIPT = AUTORUN_MAC;
		}
		else
		if (OS.startsWith("Windows"))
		{
			try
			{
				Process p = Runtime.getRuntime().exec(
					new String[] { "cscript", "//Nologo", toOS(RUNNING_JAR_DIRECTORY + "/" + QUERY_DIRECTORIES_VBSCRIPT) }
				);
				String output = Streams.readFullyString(p.getInputStream(), "UTF-8");
				String[] lines = Strings.splitLines(output);
				
				DOCUMENTS = lines[0];
				AUTORUNS = lines[1];
				PROGRAM_FILES = lines[2];
				
				RUNNING_APP = RUNNING_JAR;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			AUTORUN_SCRIPT = AUTORUN_WINDOWS;
		}
		else
		{
			// linux
			PROGRAM_FILES = "/usr/local/bin";
			DOCUMENTS = USER_HOME + "/Documents";
			AUTORUNS = null;
			RUNNING_APP = RUNNING_JAR;
			AUTORUN_SCRIPT = null;
		}
		
		DOCUMENTS_CLIENT = DOCUMENTS + "/Mailiverse";
		DOCUMENTS_CLIENT_REDIRECT = DOCUMENTS + "/Mailiverse.redirect";
		
		/*
		String[] strings = {
				USER_HOME,
				DOCUMENTS,
				AUTORUNS,
				RUNNING_JAR,
				RUNNING_JAR_FILE,
				RUNNING_JAR_DIRECTORY,
				RUNNING_APP,
				RUNNING_APP_PARENT,
				PROGRAM_FILES,
				DOCUMENTS_CLIENT,
				DOCUMENTS_CLIENT_REDIRECT,
				AUTORUN_SCRIPT,
				OS
		};
		
		for (String string : strings)
			System.out.println(string);
		*/
	}
}
