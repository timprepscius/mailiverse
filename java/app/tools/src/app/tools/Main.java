/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mail.auth.MailServerAuthenticatorSync;

import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import core.connector.dropbox.sync.DropboxSignup;
import core.constants.ConstantsClient;
import core.constants.ConstantsDropbox;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsServer;
import core.constants.ConstantsStorage;
import core.util.Environment;
import core.util.JSONSerializer;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Streams;

import key.auth.KeyServerAuthenticatorSync;

public class Main
{
	static LogNull log = new LogNull(Main.class);
	
	public static void usage (Exception e) throws Exception
	{
		System.out.println(
			"Usage: java -jar Tools.jar [action] [args]" + "\n" +
			"	--help this screen" + "\n" +
			"	--get-environment name=x password=Y file=Z" + "\n" +
			"	--put-environment name=x password=Y file=Z" + "\n" +
			"	--dropbox-authorize name=X password=Y" + "\n" +
			"	--change-password name=X password=Y new-password=Z" + "\n" +
			"	--delete name=X password=Y" + "\n" +
			"	--convert-account-mail-to-json name=X password=Y [overwrite]" + "\n" +
			"	--export-pem file=X" + "\n" + 
			"	--expunge-deleted-users [force]" + "\n" +
			"	--expunge-s3-user accessKey=[] secretKey=[] bucketName=[]" + "\n" +
			"	--schedule-user-for-deletion user=name" + "\n" +
			"	--show-user-mail-block user=name" + "\n" +
			"	--show-user-key-block user=name password=password" + "\n" +
			"	--decryptMailToFileSystem name=name password=password out=outpath" + "\n" + 
			"	--sendPushNotificationForDevice device=id" + "\n" + 
			"	--sendPushNotificationForUser user=email" + "\n" + 
			""
		);
		
		if (e != null)
		{
			System.out.println(e);
			throw e;
		}
	}		

	public static void main (String[] args) throws Exception
	{
		System.out.println(new Date());
		try
		{
			if (args.length == 0)
			{
				usage(null);
				return;
			}
			else
			{
				String c = args[0].toLowerCase();
				if (c.equalsIgnoreCase("--help"))
					usage(null);
				else
				if (c.equalsIgnoreCase("--dropbox-authorize"))
					dropboxAuthorize(args);
				else
				if (c.equalsIgnoreCase("--get-environment"))
					getEnvironment(args);
				else
				if (c.equalsIgnoreCase("--put-environment"))
					putEnvironment(args);
				else
				if (c.equalsIgnoreCase("--change-password"))
					changePassword(args);
				else
				if (c.equalsIgnoreCase("--delete"))
					delete(args);
				else
				if (c.equalsIgnoreCase("--convert-account-mail-to-json"))
					convertAccountMailToJson(args);
				else
				if (c.equalsIgnoreCase("--export-pem"))
					ExportPem.main(args);
				else
				if (c.equalsIgnoreCase("--expunge-deleted-users"))
					ExpungeDeletedUsers.main(args);
				else
				if (c.equalsIgnoreCase("--expunge-s3-user"))
					ExpungeS3User.main(args);
				else
				if (c.equalsIgnoreCase("--schedule-user-for-deletion"))
					ScheduleUserForDeletion.main(args);
				else
				if (c.equalsIgnoreCase("--show-user-mail-block"))
					ShowUserMailBlock.main(args);
				else
				if (c.equalsIgnoreCase("--show-user-key-block"))
					ShowUserKeyBlock.main(args);
				else
				if (c.equalsIgnoreCase("--decryptMailToFileSystem"))
					DecryptMailToFileSystem.main(args);
				else
				if (c.equalsIgnoreCase("--sendPushNotificationForDevice"))
					SendPushNotificationForDevice.main(args);
				else
				if (c.equalsIgnoreCase("--sendPushNotificationForUser"))
					SendPushNotificationForUser.main(args);
				else
					throw new IllegalArgumentException("Unknown command " + c);
			}
		}
		catch (IllegalArgumentException e)
		{
			usage(e);
		}
		catch (Exception e)
		{
			System.err.println("Caught " + e);
			throw e;
		}
		System.out.println(new Date());
	}
	
	public static void convertAccountMailToJson(String[] args) throws Exception
	{
		Map<String,String> a = Arguments.map(args, 1);
		if (!Arguments.containsAll(a, new String[] {"name", "password"}))
			throw new IllegalArgumentException();
		
		String name = a.get("name") + ConstantsClient.ATHOST;
		String password = a.get("password");
		
		boolean overwrite = a.containsKey("overwrite");
		
		ConvertAccountMailToJson.convert(name,  password, overwrite);
	}

	public static void dropboxAuthorize (String[] args) throws Exception
	{
		Map<String,String> a = Arguments.map(args, 1);
		if (!Arguments.containsAll(a, new String[] {"name", "password"}))
			throw new IllegalArgumentException();
		
		PrintStream out = System.out;
		
		String name = a.get("name") + ConstantsClient.ATHOST;
		String password = a.get("password");
		
		out.println("Getting existing environment");
		KeyServerAuthenticatorSync keyServer = new KeyServerAuthenticatorSync();
		MailServerAuthenticatorSync mailServer = new MailServerAuthenticatorSync();
		
		Environment e = keyServer.get(name, password, null);
		
		out.println("Requesting access token");
		AppKeyPair appToken = new AppKeyPair(ConstantsClient.DROPBOX_APPKEY, ConstantsClient.DROPBOX_APPSECRET);
		AccessTokenPair requestToken = DropboxSignup.getDropboxRequestToken(appToken);
		
		out.println("Please open in a web browser: " + 
				ConstantsClient.DROPBOX_AUTH_URL.replace("REQUEST_TOKEN_KEY", requestToken.key)
		);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		out.print ("Enter the authorization code recieved: ");
		String authCode = in.readLine();
		
		if (!authCode.equals(requestToken.key))
			throw new Exception("Invalid authentication code");
		
		out.println("Requesting access token");
		AccessTokenPair accessToken = DropboxSignup.getDropboxAccessToken(appToken, requestToken);
		
		out.println("Setting new environment variables");
		
		String handler = ConstantsStorage.HANDLER_DROPBOX;
		String prefix = handler + "/";
		
		String client = ConstantsEnvironmentKeys.CLIENT_ENVIRONMENT + "/";
		e.put(client + prefix + ConstantsDropbox.DropboxUserPrefix, name);
		e.put(client + prefix + ConstantsDropbox.DropboxAppKey, appToken.key);
		e.put(client + prefix + ConstantsDropbox.DropboxAppSecret, appToken.secret);
		e.put(client + prefix + ConstantsDropbox.DropboxTokenKey, accessToken.key);
		e.put(client + prefix + ConstantsDropbox.DropboxTokenSecret, accessToken.secret);
		
		String server = ConstantsEnvironmentKeys.SERVER_ENVIRONMENT + "/";
		e.put(server + prefix + ConstantsDropbox.DropboxUserPrefix, name);
		e.put(server + prefix + ConstantsDropbox.DropboxAppKey, appToken.key);
		e.put(server + prefix + ConstantsDropbox.DropboxAppSecret, appToken.secret);
		e.put(server + prefix + ConstantsDropbox.DropboxTokenKey, accessToken.key);
		e.put(server + prefix + ConstantsDropbox.DropboxTokenSecret, accessToken.secret);
		
		out.println("putting environment into keyserver");
		keyServer.put(name, password, e, null);
		
		out.println("putting server environment into mailserver");
		mailServer.put(name, password, e.childEnvironment(ConstantsEnvironmentKeys.SERVER_ENVIRONMENT));
		
		out.println("finished");
	}
	
	public static void getEnvironment (String args[]) throws Exception
	{
		PrintStream out = System.out;
		out.println("Getting existing environment");
		KeyServerAuthenticatorSync keyServer = new KeyServerAuthenticatorSync();
		
		Map<String,String> a = Arguments.map(args, 1);
		if (!Arguments.containsAll(a, new String[] {"name", "password", "file"}))
			throw new IllegalArgumentException();
		
		String name = a.get("name");
		String password = a.get("password");
		String fileName = a.get("file");
		
		Environment e = keyServer.get(name, password, null);
		FileWriter writer = new FileWriter(fileName);
		writer.write(new String(JSONSerializer.serialize(e)));
		writer.flush();
		writer.close();
		
		out.println("finished");
	}
	
	public static void putEnvironment (String args[]) throws Exception
	{
		PrintStream out = System.out;
		out.println("Putting existing environment");
		KeyServerAuthenticatorSync keyServer = new KeyServerAuthenticatorSync();
		MailServerAuthenticatorSync mailServer = new MailServerAuthenticatorSync();
		
		Map<String,String> a = Arguments.map(args, 1);
		if (!Arguments.containsAll(a, new String[] {"name", "password", "file"}))
			throw new IllegalArgumentException();
		
		String name = a.get("name");
		String password = a.get("password");
		String fileName = a.get("file");
		
		FileInputStream in = new FileInputStream (fileName);
		Environment e = JSONSerializer.deserialize(Streams.readFullyBytes(in));
		in.close();
		
		out.println("putting keyserver");
		keyServer.put(name, password, e, null);
		
		out.println("putting mailserver");
		mailServer.put(name, password, e.childEnvironment(ConstantsEnvironmentKeys.SERVER_ENVIRONMENT));
		
		out.println("finished");
	}

	public static void changePassword (String[] args)
	{
		
	}
	
	public static void delete(String[] args)
	{
		
	}
}
