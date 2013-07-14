/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.async;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callbacks.SaveArguments;
import core.crypt.Cryptor;
import core.util.Base64;
import core.util.Zip;

public class AsyncStoreConnectorEncrypted extends AsyncStoreConnectorAdapter
{
	Cryptor cryptor;
	
	public AsyncStoreConnectorEncrypted(Cryptor cryptor, AsyncStoreConnector connector)
	{
		super(connector);
		this.cryptor = cryptor;
	}
	
	public Callback get_()
	{
		SaveArguments save = new SaveArguments();

		return super.get_()
				.addCallback(save)
				.addCallback(cryptor.decrypt_())
				.addCallback(Zip.inflate_())
				.addCallback(save.restore_(0,101));
		
	}
	
	public Callback put_(String path)
	{
		return Zip.deflate_().addCallback(cryptor.encrypt_()).addCallback(super.put_(path));		
	}
}
