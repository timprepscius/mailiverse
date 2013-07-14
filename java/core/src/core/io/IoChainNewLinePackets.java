/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import core.util.LogNull;
import core.util.Strings;

public class IoChainNewLinePackets extends IoChain
{
	LogNull log = new LogNull(IoChainNewLinePackets.class);
	List<byte[]> packets = new ArrayList<byte[]>();
	ByteArrayOutputStream unfinished = new ByteArrayOutputStream();
	
	public IoChainNewLinePackets(IoChain sender)
	{
		super(sender);
	}

	@Override
	protected void onReceive(byte[] bytes) throws Exception
	{
		for (byte b: bytes)
		{
			if (b == '\n' || b == '\r')
			{
				log.debug("found newline");
				if (unfinished.size()>0)
				{
					packets.add(unfinished.toByteArray());
					unfinished = new ByteArrayOutputStream();
				}
			}
			else
			{
				unfinished.write(b);
			}
		}
		
		while (!packets.isEmpty())
		{
			log.debug("recieving packet");
			
			byte[] packet = packets.get(0);
			packets.remove(0);
			super.onReceive(packet);
		}
	}
	
	@Override
	public void send (byte[] bytes) throws Exception
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(bytes);
		bos.write(Strings.toBytes("\n"));
		
		super.send(bos.toByteArray());
	}
}
