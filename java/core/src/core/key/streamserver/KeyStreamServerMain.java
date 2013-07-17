/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.streamserver;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import key.server.sql.KeyUserDb;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import core.constants.ConstantsServer;


/**
 * (<b>Entry point</b>) NetCat client. NetCat client connects to the specified
 * endpoint and prints out received data. NetCat client disconnects
 * automatically when no data is read for 10 seconds.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class KeyStreamServerMain
{
	public static void main(String[] args) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");
		KeyUserDb userDb = new KeyUserDb();
		userDb.ensureTables();

		// Create TCP/IP connector.
		NioSocketAcceptor acceptor = new NioSocketAcceptor();

		DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

		// Start communication.
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		TextLineCodecFactory textLineCodec = new TextLineCodecFactory(Charset.forName("UTF-8"));
		textLineCodec.setDecoderMaxLineLength(50 * 1000);
		textLineCodec.setEncoderMaxLineLength(50 * 1000);
		acceptor.getFilterChain().addLast(
			"codec",
			new ProtocolCodecFilter(textLineCodec)
		);

		acceptor.setHandler(new SRPProtocolHandler());
		acceptor.bind(new InetSocketAddress(ConstantsServer.KEY_AUTH_PORT));

		System.out.println("Listening on port " + ConstantsServer.KEY_AUTH_PORT);
	}

	private static void addSSLSupport(DefaultIoFilterChainBuilder chain) throws Exception
	{
		SSLContextGenerator sslContextGenerator = new SSLContextGenerator();
		SslFilter sslFilter = new SslFilter(sslContextGenerator.getSslContext());
		sslFilter.setUseClientMode(false);
		sslFilter.setWantClientAuth(true);

		chain.addLast("sslFilter", sslFilter);
		System.out.println("SSL ON");
	}
}
