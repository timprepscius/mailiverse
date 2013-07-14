/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import core.util.CircularByteBuffer;

public class IoChainSizedPackets extends IoChain
{
	DataInputStream in;
	CircularByteBuffer internalIn;
	
	Integer nextPacket;
	
	public IoChainSizedPackets (IoChain sender) throws IOException
	{
		super(sender);
		
		internalIn = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
		this.in = new DataInputStream(internalIn.getInputStream());
	}
	
	public boolean ready () throws IOException
	{
		if (nextPacket == null)
		{
			if (in.available() >= Integer.SIZE/8)
				nextPacket = in.readInt();
		}
		
		if (nextPacket != null)
			return in.available() >= nextPacket;
			
		return false;
	}
	
	@Override
	protected void onReceive(byte[] bytes) throws Exception
	{
		internalIn.getOutputStream().write(bytes);
		
		while (ready())
			read();
	}
	
	@Override
	public void send(byte[] bytes) throws Exception
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		int size = bytes.length;
		dos.writeInt(size);
		dos.write(bytes);
		dos.flush();
		
		super.send(bos.toByteArray());
	}
	
	public void read () throws Exception
	{
		if (!ready())
			throw new IOException();
		
		byte[] buf = new byte[nextPacket];
		if (in.read(buf, 0, nextPacket) != nextPacket)
			throw new IOException ();
		
		nextPacket = null;

		super.onReceive(buf);
	}
	
	public boolean eof () throws IOException
	{
		return false;
	}
}
