/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.async;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.callbacks.SaveArguments;
import core.util.Base64;


public class AsyncStoreConnectorBase64 extends AsyncStoreConnectorAdapter
{
	public AsyncStoreConnectorBase64(AsyncStoreConnector connector)
	{
		super(connector);
	}

	public Callback get_()
	{
		SaveArguments save = new SaveArguments();
		
		return super.get_()
			.addCallback(save)
			.addCallback(Base64.decodeBytes_())
			.addCallback(save.restore_(0,101));
	}
	
	public Callback put_(String path)
	{
		return Base64.encodeBytes_().addCallback(super.put_(path));
	}
}
