/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import core.util.Streams;


public class UserUtil
{
	static public String getConfigurationDirectory () throws Exception
	{
		File redirect = new File(AppConstants.DOCUMENTS_CLIENT_REDIRECT);
		if (redirect.exists() && redirect.isFile())
			return Streams.readFullyString(new FileInputStream(redirect.getPath()), "UTF-8").trim();
		
		return AppConstants.DOCUMENTS_CLIENT;
	}
	
	static public boolean hasConfigurationDirectory () throws Exception
	{
		File configurationDirectory = new File(getConfigurationDirectory());
		return configurationDirectory.exists();
	}
	
	/*
	static public void writeConfiguration (String user, String password, Environment e) throws Exception
	{
		PBE pbe = new PBE(password.toCharArray(), PBE.DEFAULT_SALT_2, PBE.DEFAULT_ITERATIONS, PBE.DEFAULT_KEYLENGTH);

		File configurationDirectory = new File(getConfigurationDirectory());
		
		if (!configurationDirectory.exists())
		{
			if (!configurationDirectory.mkdirs())
				throw new Exception ("Unable to create directory for Client configuration.");
		}
		
		Environment.toStore(new EncryptedZipFileConnector(pbe, getConfigurationDirectory() + "/" + user + ".mv1"), e);
	}
	
	static public Environment readConfiguration (String user, String password)
	{
		try
		{
			PBE pbe = new PBE(password.toCharArray(), PBE.DEFAULT_SALT_2, PBE.DEFAULT_ITERATIONS, PBE.DEFAULT_KEYLENGTH);

			Environment e =  Environment.fromStore(
				new EncryptedZipFileConnector(pbe, getConfigurationDirectory() + "/" + user + ".mv1")
			);
			
			if (!e.containsKey(ConstantsEnvironmentKeys.VERSION))
				return null;
			
			return e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	*/
	
	public static void openPageInDefaultBrowser(String url)
	{
		try
		{
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		}
		catch (java.io.IOException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	public static void startClientFromSetup ()
	{
		try
		{
			if (AppConstants.OS.startsWith("Mac"))
			{
				String app = AppConstants.toOS(AppConstants.RUNNING_APP_PARENT);
				System.out.println("Attempting to start " + app);
				Runtime.getRuntime().exec( new String[] { "open", app, "--args", "--install-autorun" } );
			}
			else
			{
				String app = AppConstants.toOS(AppConstants.RUNNING_JAR_DIRECTORY + "/" + AppConstants.CLIENT_JAR);
				System.out.println("Attempting to start " + app);
				Runtime.getRuntime().exec( new String[] { "java", "-jar", app, "--install-autorun" } );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void startSetupFromClient ()
	{
		try
		{
			if (AppConstants.OS.startsWith("Mac"))
			{
				String app = AppConstants.toOS(AppConstants.RUNNING_APP +  "/Contents/Setup.app");
				System.out.println("Attempting to start " + app);
				Runtime.getRuntime().exec( new String[] { "open", app } );
			}
			else
			{
				String app = AppConstants.toOS(AppConstants.RUNNING_JAR_DIRECTORY + "/" + AppConstants.SETUP_JAR);
				System.out.println("Attempting to start " + app);
				Runtime.getRuntime().exec( new String[] { "java", "-jar", app } );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void enableAutoLaunch (String app, boolean enable) throws IOException
	{
		if (AppConstants.OS.startsWith("Linux"))
			return;
		
		String launchFileName = AppConstants.AUTORUNS + "/" + AppConstants.AUTORUN_SCRIPT;

		if (enable)
		{
			System.out.println("Attempting to create autorun " + launchFileName + " pointing to " + app);

			File directory = new File(AppConstants.AUTORUNS);
			if (!directory.exists())
				directory.mkdirs();
		
			String launchString = 
				Streams.readFullyString(
					UserUtil.class.getResourceAsStream(AppConstants.AUTORUN_SCRIPT),
					"UTF-8"
				);
			
			if (AppConstants.OS.startsWith("Mac"))
				app = app + "/Contents/MacOS/JavaApplicationStub";
			if (AppConstants.OS.startsWith("Windows"))
				app = app.replace("\\", "\\\\").replace("\"", "\"\"");
			
			launchString = launchString.replace("#TARGET#", app);
				
			FileOutputStream fos = new FileOutputStream(launchFileName);
			PrintWriter writer = new PrintWriter(fos);
			writer.write(launchString);
			writer.close();
		}
		else
		{
			System.out.println("Attempting to delete autorun " + launchFileName);
				
				File f = new File (launchFileName);
				if (f.exists())
					f.delete();
		}
	}	
	
	public static boolean isAutoLaunchEnabled ()
	{
		if (AppConstants.OS.startsWith("Linux"))
			return false;
		
		String launchFileName = AppConstants.AUTORUNS + "/" + AppConstants.AUTORUN_SCRIPT;
	
		File f = new File (launchFileName);
		return f.exists();
	}
	
	public static void bringApplicationToFront ()
	{
		if (AppConstants.OS.startsWith("Mac"))
		{
			try
			{
				String application = "Mailiverse";
				System.out.println("Attempting to bring application to front via applescript.");
				
				Runtime.getRuntime().exec (
					new String[] {
						"osascript",
						"-e",
						"tell application \"" + application + "\" to activate"
					}
				);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
