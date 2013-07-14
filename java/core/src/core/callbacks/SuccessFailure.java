/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.LogNull;
import core.util.LogOut;

public class SuccessFailure extends CallbackDefault
{
	LogNull log = new LogNull(SuccessFailure.class);
	Callback succeed, fail;
	
	public SuccessFailure (Callback succeed, Callback fail)
	{
		this.succeed = succeed;
		this.fail = fail;
	}
	
	@Override
	public void onSuccess(Object... arguments) throws Exception 
	{
		log.debug("onSuccess");
		
		succeed.setReturn(callback);
		succeed.invoke(arguments);
	}

	@Override
	public void onFailure(Exception e) 
	{
		log.debug("onFailure, transmogrifying to string",e);

		fail.setReturn(callback);
		fail.invoke(e.getMessage());
	}

}
