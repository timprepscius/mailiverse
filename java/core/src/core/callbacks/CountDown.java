/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.LogNull;
import core.util.LogOut;

public class CountDown extends CallbackDefault
{
	static LogNull log = new LogNull(CountDown.class);
	
	int count;
	Callback onFinished;
	
	public CountDown (int from, Callback onFinished)
	{
		this.count = from;
		this.onFinished = onFinished;
	}

	@Override
	public void onSuccess(Object... arguments) throws Exception 
	{
		log.debug("Countdown: ", count);
		
		if (--count == 0)
			onFinished.invoke(arguments);
		
		next();
	}
}
