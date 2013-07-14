/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.io.IoChain;

public class IoOpen extends CallbackDefault 
{
	public IoOpen(IoChain chain) 
	{
		super(chain);
	}
	
	public void onSuccess (Object... args) throws Exception
	{
		IoChain chain = (IoChain)V(0);
		if (chain != null)
			chain.open();
		
		next(args);
	}
}
