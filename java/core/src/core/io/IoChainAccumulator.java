/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

import java.util.ArrayList;
import java.util.List;

import core.util.LogNull;
import core.util.LogOut;

public class IoChainAccumulator extends IoChain
{
	LogNull log = new LogNull(IoChainAccumulator.class);
	List<byte[]> out;
	boolean opened, closed;
	Exception exception;
	
	public IoChainAccumulator()
	{
		super(null);
		out = new ArrayList<byte[]>();
	}

	@Override
	public void send(byte[] packet) throws Exception
	{
		log.debug("accumulating packet");
		out.add(packet);
	}

	@Override
	protected void onReceive(byte[] object) throws Exception
	{
		super.onReceive(object);
	}
	
	@Override
	public void open () throws Exception
	{
		opened = true;
		super.open();
	}
	
	@Override
	protected void close () throws Exception
	{
		closed = true;
		super.close();
	}
	
	public void onException(Exception e)
	{
		this.exception = e;
		super.onException(e);
	}
	
	public List<byte[]> getAndClearPackets()
	{
		List<byte[]> result = out;
		out = new ArrayList<byte[]>();
		return result;
	}
	
	public Exception getAndClearException ()
	{
		Exception e = this.exception;
		this.exception = null;
		return e;
	}
	
	public boolean isOpened ()
	{
		return opened;
	}
	
	public boolean isClosed ()
	{
		return closed;
	}
}
