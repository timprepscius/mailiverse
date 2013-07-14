/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.async;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.Base64;
import core.util.Zip;

public abstract class AsyncStoreConnectorAdapter implements AsyncStoreConnector 
{
	AsyncStoreConnector connector;
	
	AsyncStoreConnectorAdapter (AsyncStoreConnector connector)
	{
		this.connector = connector;
	}
	
	@Override
	public Callback list_(String path)
	{
		return connector.list_(path);
	}

	@Override
	public Callback createDirectory_(String path)
	{
		return connector.createDirectory_(path);
	}

	@Override
	public Callback ensureDirectories_(String[] directories)
	{
		return connector.ensureDirectories_(directories);
	}
	
	public Callback get_(String path)
	{
		return 
			new CallbackDefault(path) {
				public void onSuccess(Object... arguments) throws Exception {
					String path = V(0);
					get_().setReturn(callback).invoke((String)path);
				}
			};
		
	}
	public Callback get_()
	{
		return connector.get_();
	}
	
	public Callback put_(String path, byte[] bytes)
	{
		return 
			new CallbackDefault(path, bytes) {
				public void onSuccess(Object... arguments) throws Exception {
					String path = V(0);
					byte[] bytes = V(1);
					put_(path).setReturn(callback).invoke(bytes);
				}
			};
	}

	public Callback put_(String path)
	{
		return connector.put_(path);
	}
	
	@Override
	public Callback move_(String from, String to)
	{
		return connector.move_(from, to);
	}

	@Override
	public Callback delete_(String path)
	{
		return connector.delete_(path);
	}
}
