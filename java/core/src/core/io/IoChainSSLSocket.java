/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class IoChainSSLSocket extends IoChainSocket
{
	public IoChainSSLSocket(SSLContext context, String host, int port) throws Exception
	{
		useSocket(createSSLSocket(context, host, port));
	}
	
	public Socket createSSLSocket (SSLContext context, String host, int port) throws UnknownHostException, IOException
	{
		SSLSocketFactory sslsocketfactory = context.getSocketFactory();
		SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(host, port);	
		socket.setUseClientMode(true);
		
		return socket;
	}

}
