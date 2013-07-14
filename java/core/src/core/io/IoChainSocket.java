/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import core.util.CircularByteBuffer;

public class IoChainSocket extends IoChain implements Runnable
{
	String host;
	int port;
	Socket socket;
	
	InputStream in;
	OutputStream out;

	protected IoChainSocket() throws Exception
	{
		super(null);
	}
	
	protected void useSocket (Socket socket) throws Exception
	{
		this.socket = socket;

		in = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	public IoChainSocket(String host, int port)
	{
		super(null);
		
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void open () throws Exception
	{
		if (socket == null)
			useSocket(new Socket(host, port));
		
		super.open();
	}
	
	@Override
	protected void close () throws Exception
	{
		if (socket != null)
		{
			socket.close();
			socket = null;
		}
	}
	
	public boolean pumpBlock () throws Exception
	{
		CircularByteBuffer buffer = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);

		boolean first = true;
		while (socket!= null && (first || in.available()>0))
		{
			first = false;
			int c = in.read();
		
			if (c == -1)
				throw new IoChainFinishedException();

			buffer.getOutputStream().write(c);
		}
		
		if (buffer.getAvailable() > 0)
		{
			byte[] b = new byte[buffer.getAvailable()];
			buffer.getInputStream().read(b);
		
			receive(b);
		}
		
		return socket != null;
	}
	
	@Override
	public void send(byte[] bytes) throws IOException
	{
		out.write(bytes);
	}
	
	@Override
	public void run ()
	{
		try 
		{
			open();
			
			while (pumpBlock()) ;
		}
		catch (Exception e) 
		{
			onException(e);
		} 
	}
}
