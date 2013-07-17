/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import key.auth.KeyServerAuthenticatorSync;
import mail.server.util.JavaMailToJSON;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;
import core.util.Streams;
import core.util.Zip;

public class ConvertFileMailToJson
{
	public static void main(String[] args) throws Exception
	{
		String name = args[0];
		String password = args[1];
		
		KeyServerAuthenticatorSync auth = new KeyServerAuthenticatorSync();
		Environment e = auth.get(name, password, null);
		CryptorRSAAES rsa = new CryptorRSAAES(CryptorRSAFactoryEnvironment.create(e.childEnvironment("client")));
		
		JavaMailToJSON converter = new JavaMailToJSON();
		
		
		for (int i=2; i<args.length; ++i)
		{
			String fileName = args[i];
			String fileNameOut = fileName + ".json";
			
			System.out.print(fileName + " ... ");
			
			FileInputStream fis = new FileInputStream(fileName);
			byte[] bytes_ = Streams.readFullyBytes(fis);
			fis.close();
			
			byte[] bytes = Zip.inflate(rsa.decrypt(bytes_));
				
			try
			{
				byte[] json = converter.convertAttemptToShowException(fileName, new Date(), bytes, new JavaMailToJSON.MailDescription());
				byte[] json_ = rsa.encrypt(Zip.deflate(json));
				
				FileOutputStream fos = new FileOutputStream(fileNameOut);
				fos.write(json_);
				fos.close();

				System.out.println("finished.");
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
				System.out.println("Full:\n" + new String(bytes));
			}
		}
	}

}
