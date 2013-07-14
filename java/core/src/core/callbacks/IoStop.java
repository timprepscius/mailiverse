/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.CallbackDefault;
import core.io.IoChain;

public class IoStop extends CallbackDefault 
{
	public IoStop() 
	{
	}
	
	public void onSuccess (Object... arguments) throws Exception
	{
		IoChain io = (IoChain)arguments[0];
		io.stop();
		
		next(arguments[1]);
	}
}
