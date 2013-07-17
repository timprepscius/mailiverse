package app.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import core.connector.FileInfo;
import core.connector.dropbox.ClientInfoDropbox;
import core.connector.dropbox.sync.DropboxConnector;
import core.connector.s3.ClientInfoS3;
import core.connector.s3.sync.S3Connector;
import core.connector.sync.EncryptedStoreConnector;
import core.connector.sync.StoreConnector;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsStorage;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;
import core.util.Pair;

import key.auth.KeyServerAuthenticatorSync;

public class DecryptMailToFileSystem
{
	public static void main (String[] _args) throws Exception
	{
		_args = new String[] { "name=SOMENAME@mailiverse.com", "password=SOMEPASSWORD", "out=." };
		
		Map<String,String> args = Arguments.map(_args, 0);
		String name = args.get("name");
		String password = args.get("password");
		String out = args.get("out");
		
		if (name == null || password == null || out == null)
			throw new IllegalArgumentException();
		
		KeyServerAuthenticatorSync key = new KeyServerAuthenticatorSync();
		Environment environment = key.get(name, password, null);
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
		
		String[] dirs = new String[] { 
			ConstantsStorage.NEW_IN, ConstantsStorage.NEW_OUT, 
			ConstantsStorage.NEW_IN_JSON, ConstantsStorage.NEW_OUT_JSON
		};
		
		List<FileInfo> all = new ArrayList<FileInfo>();
		
		for (String dir : dirs)
		{
			List<FileInfo> list = connector.listDirectory(dir);
			all.addAll(list);
		}
		
		Collections.sort(all, new FileInfo.SortByDateAscending());

		for (FileInfo f : all)
		{
			if (f.relativePath.contains(".lock"))
				continue;
			
			System.out.print("reading " + f.path);
			byte[] block = connector.get(f.path);

			String directory = out + "/" + f.path;
			directory = directory.substring(0, directory.lastIndexOf('/'));
			
			System.out.print(" ["+directory+"]");
			new File(directory).mkdirs();

			String outPath= out + "/" + f.path;
			System.out.print(" writing " + outPath);

			FileOutputStream of = new FileOutputStream (outPath);
			of.write(block);
			of.close();
			
			System.out.println(" done.");
		}
	}
}
