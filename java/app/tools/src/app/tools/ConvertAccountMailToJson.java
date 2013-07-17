/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import key.auth.KeyServerAuthenticatorSync;
import mail.server.util.JavaMailToJSON;
import core.connector.FileInfo;
import core.connector.dropbox.ClientInfoDropbox;
import core.connector.dropbox.sync.DropboxConnector;
import core.connector.s3.ClientInfoS3;
import core.connector.s3.sync.S3Connector;
import core.connector.sync.EncryptedStoreConnector;
import core.connector.sync.StoreConnector;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsServer;
import core.constants.ConstantsStorage;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;

public class ConvertAccountMailToJson
{
	public static void convert (String name, String password, boolean overwrite) throws Exception
	{
		KeyServerAuthenticatorSync auth = new KeyServerAuthenticatorSync("YOUR_SERVER", ConstantsServer.KEY_AUTH_PORT);
		Environment environment = auth.get(name, password, null);
		Environment clientEnv = environment.childEnvironment(ConstantsEnvironmentKeys.CLIENT_ENVIRONMENT);

		String handlerName = clientEnv.get(ConstantsEnvironmentKeys.HANDLER);
		StoreConnector storeConnector = null;
		
		if (handlerName.equals(ConstantsStorage.HANDLER_S3))
		{
			storeConnector = 
				new S3Connector(
					new ClientInfoS3(clientEnv.childEnvironment(ConstantsStorage.HANDLER_S3)
				)
			);
		}
		else
		{
			storeConnector = 
				new DropboxConnector(
					new ClientInfoDropbox(clientEnv.childEnvironment(ConstantsStorage.HANDLER_DROPBOX)
				)
			);
		}
		
		StoreConnector connector = new EncryptedStoreConnector(
			new CryptorRSAAES(CryptorRSAFactoryEnvironment.create(clientEnv)),
			storeConnector
		);
		
		connector.open();

		connector.ensureDirectories(ConstantsStorage.NEW_IN_JSON, ConstantsStorage.NEW_OUT_JSON);
		
		List<FileInfo> inList = connector.listDirectory(ConstantsStorage.NEW_IN);
		List<FileInfo> outList = connector.listDirectory(ConstantsStorage.NEW_OUT);
		List<FileInfo> inListJson = connector.listDirectory(ConstantsStorage.NEW_IN_JSON);
		List<FileInfo> outListJson = connector.listDirectory(ConstantsStorage.NEW_OUT_JSON);
		
		/*
		for (FileInfo i : inList)
			i.path = ConstantsStorage.NEW_IN + i.path;
		
		for (FileInfo i : outList)
			i.path = ConstantsStorage.NEW_OUT + i.path;
		*/
		
		ArrayList<FileInfo> all = new ArrayList<FileInfo>();
		all.addAll(inList);
		all.addAll(outList);
		
		Collections.sort(all, new FileInfo.SortByDateAscending());
		
		Set<String> jsons = new HashSet<String>();
		
		for (FileInfo i : inListJson)
			jsons.add(i.relativePath.substring(i.relativePath.indexOf("_")+1));

		for (FileInfo i : outListJson)
			jsons.add(i.relativePath.substring(i.relativePath.indexOf("_")+1));

		JavaMailToJSON converter = new JavaMailToJSON();
		for (FileInfo i : all)
		{
			System.out.print("handling " + i.path);
			
			if (i.path.indexOf(ConstantsStorage.JSON)!=-1)
			{
				System.out.println(" is json, continuing.");
				continue;
			}
			
			String jsonPath = i.relativePath.substring(i.relativePath.indexOf('_') + 1);
			if (jsons.contains(jsonPath) && !overwrite)
			{
				System.out.println(" has json counterpart, specify overwrite to overwrite.");
				continue;
			}				
			
			System.out.print(" reading");
			byte[] raw = connector.get(i.path);
			System.out.print(", converting");
			byte[] json = converter.convert(i.path, i.date, raw, new JavaMailToJSON.MailDescription());
			System.out.print(", writing");
			
			String datePath = 
				(i.relativePath.contains("_")) ?
					i.relativePath :
					i.date.getTime() + "_" + i.relativePath;
					
			String jsonFileName = i.path.contains(ConstantsStorage.IN) ?
				(ConstantsStorage.NEW_IN_JSON + "/" + datePath) :
				(ConstantsStorage.NEW_OUT_JSON + "/" + datePath);
					
			System.out.print(" to " + jsonFileName);
			connector.put(jsonFileName, json);
			System.out.println(" : finished");
		}
		
		System.out.println("closing");
		connector.close();
	}
	
}
