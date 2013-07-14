/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

public abstract class IoChain 
{
	protected boolean finished = false;
	protected IoChain sender, receiver;
	
	public IoChain (IoChain sender)
	{
		if (sender != null)
			setSender(sender);
	}
	
	public void setSender (IoChain sender)
	{
		this.sender = sender;
		sender.setReceiver (this);
	}
	
	public void setReceiver (IoChain receiver)
	{
		this.receiver = receiver;
	}

	public void send (byte[] packet) throws Exception
	{
		this.sender.send(packet);
	}

	protected void onReceive (byte[] object) throws Exception
	{
		receiver.onReceive(object);
	}
	
	public void receive(byte[] object)
	{
		try
		{
			onReceive(object);
		}
		catch (Exception e)
		{
			onException(e);
		}
	}
	
	public void onException (Exception e)
	{
		if (receiver != null)
			receiver.onException(e);
	}
	
	public IoChain getFinalSender ()
	{
		if (sender != null)
			return sender.getFinalSender();
		
		return this;
	}
	
	public void open () throws Exception
	{
		if (receiver != null)
			receiver.open();
	}
	
	protected void close () throws Exception
	{
		if (sender != null)
			sender.close();
	}
	
	public void stop () throws Exception
	{
		if (receiver != null)
			receiver.stop();
		else
			close();
	}
	
	public void run () throws Exception
	{
		if (sender != null)
			sender.run();
		else
			open();
	}
	
	public boolean isFinished ()
	{
		return finished;
	}
}
