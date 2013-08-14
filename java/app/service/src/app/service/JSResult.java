/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


import core.callback.Callback;
import core.exceptions.PublicMessageException;
import core.util.LogNull;
import core.util.LogOut;

@Export()
public class JSResult<T> extends Callback implements Exportable
{
	static LogNull log = new LogNull(JSResult.class);
	
	boolean finished;
	T o;
	Exception e;
	Object callback;
	
	public JSResult ()
	{
		finished = false;
	}
	
	public JSResult (Object callback)
	{
		this();
		this.callback = callback;
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSResultInterface#hasException()
	 */
	public boolean hasException ()
	{
		return e != null;
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSResultInterface#hasObject()
	 */
	public boolean hasObject ()
	{
		return o != null;
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSResultInterface#isFinished()
	 */
	public synchronized boolean isFinished ()
	{
		return finished;
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSResultInterface#getObject()
	 */
	public Object getObject ()
	{
		return JSInvoker.wrap(o);
	}
	
	/* (non-Javadoc)
	 * @see app.service.JSResultInterface#getException()
	 */
	public Object getException ()
	{
		return JSInvoker.wrap(
			e != null ?
				new PublicMessageException(e.getMessage()) :
				null
		);
	}
	
	public synchronized void setObject (T o)
	{
		this.o = o;
		finished = true;
	}
	
	public synchronized void setException (Exception e)
	{
		this.e = e;
		finished = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void invoke(Object... arguments)
	{
		log.debug("invoke",arguments[0]);
		
		if (arguments[0] instanceof Exception)
			setException((Exception)arguments[0]);
		else
			setObject((T)arguments[0]);
		
		if (callback != null)
		{
			try
			{
				JSInvoker.invoke(callback, "invoke", new Object[] {this});
			}
			catch (Exception e)
			{
				log.error(e);
				throw new RuntimeException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see app.service.JSResultInterface#getCallback()
	 */
	public Object getCallback()
	{
		return callback;
	}
}
