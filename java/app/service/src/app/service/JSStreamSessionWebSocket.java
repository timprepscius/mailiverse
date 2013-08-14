/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


import java.util.ArrayList;
import java.util.List;

import core.io.IoChain;
import core.io.IoChainFinishedException;
import core.util.LogNull;
import core.util.Strings;

@Export
public class JSStreamSessionWebSocket extends IoChain implements Exportable
{
	static LogNull log = new LogNull(JSStreamSessionWebSocket.class);
	
	boolean isOpen = false;
	Object delegate;
	Object socket;
	List<byte[]> queued = new ArrayList<byte[]>();
	
	public JSStreamSessionWebSocket(String url, Object delegate)
	{
		super(null);

		this.delegate = delegate;
		
		log.debug("JSStreamSessionWebSocket", url, delegate);
		
		try
		{
			socket = JSInvoker.invoke(
				delegate,
				"socketConstruct",
				new Object[] {
					this,
					url
				}
			);
		}
		catch (Exception e)
		{
			// work around a gwt-exporter thing, which I don't feel like fixing now
			log.error(e);
		}
	}

	@Override
	public void send(byte[] data) throws Exception
	{
		log.debug("send:", data, data != null ? Strings.toString(data) : null);
		
		if (data != null)
			queued.add(data);
		
		if (isOpen)
		{
			while (!queued.isEmpty())
			{				
				byte[] bytes = queued.get(0);
				queued.remove(0);

				log.debug("sending:", bytes);
				JSInvoker.invoke(
					delegate,
					"socketSend", 
					new Object[] { 
						socket,
						Strings.toString(bytes)
					}
				);
			}
		}
	}
	
	public void onMessage (String packet)
	{
		try
		{
			log.debug("onMessage",packet);
			receive(packet.getBytes());
			log.debug("after recieve");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	public void onEvent (String event) throws Exception
	{
		log.debug("onEvent", event);
		if (event.equals("onOpen"))
		{
			isOpen = true;
			send(null);
		}
		if (event.equals("onClose"))
		{
			onException(new IoChainFinishedException());
		}
	}
	
	@Override
	public void onException (Exception e)
	{
		super.onException(e);
	}
	
	@Override
	public void close () throws Exception
	{
		if (delegate != null)
			JSInvoker.invoke(
				delegate,
				"socketClose", 
				new Object[] { 
					socket
				}
			);
	}
}
