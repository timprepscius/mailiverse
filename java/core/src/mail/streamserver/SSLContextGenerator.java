/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.streamserver;

import java.io.File;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;

import core.util.Streams;


public class SSLContextGenerator
{
	public SSLContext getSslContext() throws Exception
	{
		SSLContext sslContext = null;
		
		final KeyStoreFactory keyStoreFactory = new KeyStoreFactory();
		keyStoreFactory.setData(Streams.readFullyBytes(this.getClass().getResourceAsStream("keystore.jks")));
		keyStoreFactory.setPassword("password");

		final KeyStoreFactory trustStoreFactory = new KeyStoreFactory();
		trustStoreFactory.setData(Streams.readFullyBytes(this.getClass().getResourceAsStream("truststore.jks")));
		trustStoreFactory.setPassword("password");

		final SslContextFactory sslContextFactory = new SslContextFactory();
		final KeyStore keyStore = keyStoreFactory.newInstance();
		sslContextFactory.setKeyManagerFactoryKeyStore(keyStore);

		final KeyStore trustStore = trustStoreFactory.newInstance();
		sslContextFactory.setTrustManagerFactoryKeyStore(trustStore);
		sslContextFactory.setKeyManagerFactoryKeyStorePassword("password");
		sslContext = sslContextFactory.newInstance();
		System.out.println("SSL provider is: " + sslContext.getProvider());

		return sslContext;
	}
}