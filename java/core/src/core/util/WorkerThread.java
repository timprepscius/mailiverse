/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.LinkedList;

public class WorkerThread extends Thread 
{
	public LinkedList<Runnable> queue;
	boolean running;
	
	public WorkerThread ()
	{
		queue = new LinkedList<Runnable>();
		running = true;
	}
	
	public synchronized void queue (Runnable r)
	{
		synchronized (queue)
		{
			queue.addLast(r);
			queue.notify();
		}
	}
	
	public Runnable dequeue ()
	{
		synchronized (queue)
		{
			return queue.removeFirst();
		}
	}
	
	public boolean waitFor ()
	{
		try 
		{
			synchronized(queue)
			{
				while (queue.isEmpty() && running)
					queue.wait();
			}
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		return running;
	}
	
	public void shutdown ()
	{
		running = false;
		queue.notify();
	}
	
	public void run ()
	{
		while (waitFor())
		{
			Runnable r = dequeue();
			r.run();
		}
	}
}
