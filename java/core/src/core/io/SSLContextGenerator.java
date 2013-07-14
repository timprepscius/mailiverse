/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLContextGenerator
{
	static public SSLContext getSslContext(InputStream clientKeyStore, InputStream serverKeyStore)
	{
		SSLContext sslContext = null;
		
		try
		{
			KeyStore cks = KeyStore.getInstance("JKS");
			cks.load(clientKeyStore, "password".toCharArray());

			KeyStore sks = KeyStore.getInstance("JKS");
			sks.load(serverKeyStore, "password".toCharArray());
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(cks, "password".toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");  
			tmf.init(sks);
			
			// this might need to be SSL not TLS
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			return sslContext;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return sslContext;
	}
}