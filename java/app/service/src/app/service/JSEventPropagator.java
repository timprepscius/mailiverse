/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


import core.util.LogNull;
import mail.client.EventDispatcher;
import mail.client.EventPropagator;

@Export()
public class JSEventPropagator extends EventDispatcher implements Exportable
{
	static LogNull log = new LogNull (JSEventPropagator.class);

	Object delegate;
	
	public JSEventPropagator ()
	{
	}
	
	@Override
	protected void doSignal (String event, Object... parameters) 
	{
		super.doSignal(event, parameters);
		
		try
		{
			if (!event.equals(EventPropagator.INVOKE))
				if (delegate != null)
					JSInvoker.invoke(delegate,
						"signal", 
						new Object[] { 
							event, 
							(parameters != null && parameters.length > 0) ?
								parameters[0] : 
								null 
						}
					);
		}
		catch (Exception e)
		{
			log.exception(e);
		}
	}
	
	public void setDelegate (Object delegate)
	{
		this.delegate = delegate;
	}
}
