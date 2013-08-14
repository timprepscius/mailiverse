/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JsArray;


import java.io.IOException;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.Base64;

import core.util.HttpDelegate;
import core.util.LogNull;
import core.util.Strings;

@Export()
public class JSHttpDelegate extends HttpDelegate implements Exportable
{
	static LogNull log = new LogNull(JSHttpDelegate.class);

	Object delegate;
	
	public JSHttpDelegate (Object delegate)
	{
		this.delegate = delegate;
	}
	
	public void doExecute (String action, String url, String[][] headers, boolean binaryInput, boolean binaryOutput, byte[] contents, Callback callback) throws Exception
	{
		try
		{
			JSInvoker.invoke (
				delegate,
				"executeURL", 
				new Object[] {
					action, url, headers, binaryInput, binaryOutput, 
					binaryInput ? 
							((contents!=null) ? Base64.encode(contents) : null) :
							((contents!=null) ? Strings.toString(contents) : null),
				
					new CallbackDefault(url, binaryOutput) {
						@Override
						public void onSuccess(Object...arguments) throws Exception
						{
							String data = (String)arguments[0];
							JsArray jsHeaders = (JsArray) arguments[1];
							String[][] headers = new String[jsHeaders.length()][];
							for (int i=0; i<jsHeaders.length(); ++i)
							{
								JsArray header = jsHeaders.get(i).cast();
								headers[i] = new String[] {
									header.get(0).toString(),
									header.get(1).toString()
								} ;
							}
							
							String url = V(0);
							boolean binaryOutput = V(1);
							
							log.debug("doExecute callback ",url,binaryOutput);

							if (data == null)
							{
								log.debug("failed to acquire resource", url);
								next(new IOException("Failed to acquire resource"));
							}
							else
							{
								log.debug("succeeded in acquiring resource", url);
								
								if (binaryOutput)
								{
									byte[] bin = data != null ? Base64.decode(data) : null;
									next(bin, headers);
								}
								else
								{
									next(data, headers);
								}
							}
						}
					}.setReturn(callback)
				}
			);
		}
		catch (Throwable e)
		{
			callback.invoke(e);
		}
	}
	
	
	@Override
	public void execute (String action, String url, String[][] headers, boolean binaryInput, boolean binaryOutput, byte[] contents, Callback callback)
	{
		log.debug("JSHttpDelegate.execute ",url);
		
		try
		{
			doExecute (action, url, headers, binaryInput, binaryOutput, contents, callback);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}
}
