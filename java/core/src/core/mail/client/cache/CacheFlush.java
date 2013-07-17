/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackWithVariables;
import core.connector.async.AsyncStoreConnector;
import core.util.LogNull;

public class CacheFlush extends CallbackChain
{
	static LogNull log = new LogNull(CacheFlush.class);
	
	Cache cache;
	String file;
	AsyncStoreConnector connector;
	
	CacheFlush (Cache cache, AsyncStoreConnector connector, String file, CallbackChain writeByteArray)
	{
		this.cache = cache;
		this.file = file;
		this.connector = connector;
		
		addCallback(writeByteArray);
		addCallback(connector.put_(file));
	}
}
