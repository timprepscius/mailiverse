/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.streamserver;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import mail.server.db.MailUserDb;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import core.constants.ConstantsServer;


/**
 * (<b>Entry point</b>) NetCat client. NetCat client connects to the specified
 * endpoint and prints out received data. NetCat client disconnects
 * automatically when no data is read for 10 seconds.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class MailStreamServerMain
{
	public static void main(String[] args) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");
		MailUserDb userDb = new MailUserDb();
		userDb.ensureTables();

		// Create TCP/IP connector.
		NioSocketAcceptor acceptor = new NioSocketAcceptor();

		// Start communication.
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		TextLineCodecFactory textLineCodec = new TextLineCodecFactory(Charset.forName("UTF-8"));
		textLineCodec.setDecoderMaxLineLength(100 * 1000);
		textLineCodec.setEncoderMaxLineLength(100 * 1000);
		acceptor.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(textLineCodec)
			);
		
		acceptor.setHandler(new SRPProtocolHandler());
		acceptor.bind(new InetSocketAddress(ConstantsServer.MAIL_AUTH_PORT));

		System.out.println("Listening on port " + ConstantsServer.MAIL_AUTH_PORT);
	}
}
