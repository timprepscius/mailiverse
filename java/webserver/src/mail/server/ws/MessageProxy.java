/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.ws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import core.util.Arrays;
import core.util.LogNull;
import core.util.LogOut;

public class MessageProxy extends WebSocketServlet
{
	static LogOut log = new LogOut(MessageProxy.class);
	private static final long serialVersionUID = 1L;

	String host;
	int port;
	
	/**
	 * @throws ClassNotFoundException 
	 * @see WebSocketServlet#WebSocketServlet()
	 */
	public MessageProxy(String host, int port) throws ClassNotFoundException
	{
		super();
		this.host = host;
		this.port = port;
	}

	@Override
	public void init() throws ServletException
	{
		super.init();
	}

	@Override
	protected StreamInbound createWebSocketInbound(String arg, HttpServletRequest request)
	{
		log.debug("web socket connection");
		
		KeySessionHandler result = null;
		try
		{
			result = new KeySessionHandler(host, port);
			log.debug("instantiated handler");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		
		return result;
	}

	private static final class KeySessionHandler extends MessageInbound
	{
		class MinaIoAdapter extends IoHandlerAdapter
		{
			List<Object> queue = new ArrayList<Object>();
			IoSession session = null;

			public void write (Object writable)
			{
				//log.debug("client->server(Q)", writable);
				
				if (writable != null)
				{
					synchronized(queue)
					{
						queue.add(writable);
					}
				}
				
				if (session != null)
				{
					while(true)
					{
						Object next = null;
		
						synchronized(queue)
						{
							if (queue.isEmpty())
								return;
								
							next = queue.get(0);
							queue.remove(0);
						}
						
						// log.debug("client->server(W):", next);
						session.write(next);
					} 
				}
			}

			public void sessionOpened(IoSession session) 
			{
				log.debug("sessionOpened");
				this.session = session;
				session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 5);
				write(null);
			}			
			
			@Override
			public void sessionClosed(IoSession session) throws IOException 
			{
				KeySessionHandler.this.getWsOutbound().close(1000, null);
				log.debug("MinaIoAdapter.sessionClosed: " + session.getReadBytes() + " byte(s)");
			}			
			
			@Override
			public void sessionIdle(IoSession session, IdleStatus status) {
				// Close the connection if reader is idle.
				if (status == IdleStatus.READER_IDLE) {
					session.close(true);
				}
			}
			
			@Override
			public void messageReceived(IoSession session, Object message) throws IOException 
			{
				// the new line gets stripped
				String str = message.toString() + "\n";
				
				log.debug("server->client", str.length());
				KeySessionHandler.this.getWsOutbound().writeTextMessage(
					CharBuffer.wrap(str.toCharArray())
				);
				
				KeySessionHandler.this.getWsOutbound().flush();
			}			
		} ;
		
		// Create TCP/IP connector.
		NioSocketConnector connector = new NioSocketConnector();
		MinaIoAdapter minaAdapter;
		
		public KeySessionHandler(String host, int port) throws UnknownHostException, IOException 
		{
			super();
			
			log.debug("construct");

			setByteBufferMaxSize(10 * 1024);
			setCharBufferMaxSize(10 * 1024);
			
			connector.setConnectTimeoutMillis(30*1000L);
			TextLineCodecFactory textLineCodec = new TextLineCodecFactory(Charset.forName("UTF-8"));
			textLineCodec.setDecoderMaxLineLength(50 * 1000);
			textLineCodec.setEncoderMaxLineLength(50 * 1000);
			connector.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(textLineCodec)
			);
			
			minaAdapter = new MinaIoAdapter();
			connector.setHandler(minaAdapter);
			connector.connect(new InetSocketAddress(host, port));
			
			log.debug("construct finished");
		}
	
		@Override
		protected void onBinaryMessage(ByteBuffer message) throws IOException
		{
			message.flip();
			message.compact();
			log.debug(message,message.limit());
			byte[] copy = Arrays.copyOf(message.array(), message.limit());
			message.clear();
			//log.debug("flip/compact/clear");
			log.debug("client->server(B)", copy.length);
			minaAdapter.write(IoBuffer.wrap(copy));
			message.clear();
		}

		@Override
		protected void onTextMessage(CharBuffer message) throws IOException
		{
			message.flip();
			message.compact();
			log.debug(message,message.limit());
			String str = new String(message.array(), 0, message.limit());
			message.clear();

			
			// there is a bug in the apache websocket, it doesn't seem to be clearing the buffers or something
			int firstNewLine = str.indexOf('\n');
			if (firstNewLine != -1)
				str = str.substring(0, firstNewLine+1);
			
			//log.debug("flip/compact/clear");
			log.debug("client->server(T):", str.length());

			minaAdapter.write(str);
		}
		
		@Override
		protected void onClose(int status)
		{
			log.debug("onClose");
			// TODO Auto-generated method stub
			super.onClose(status);
			minaAdapter.session.close(true);
			minaAdapter = null;
			connector.dispose();
			log.debug("onClose finisehd");
		}
	}
}
