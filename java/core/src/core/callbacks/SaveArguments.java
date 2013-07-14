/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.Callback;
import core.callback.CallbackDefault;

public class SaveArguments extends CallbackDefault{
	
	Object[] saved;

	@Override
	public void onSuccess(Object... save) throws Exception {
		this.saved = save;
		next(save);
	}

	public Callback restore_(int...indexes)
	{
		return new CallbackDefault (new Object[] {indexes}) 
		{
			public void onSuccess(Object...arguments) {
				int[] indexes = V(0);
				Object[] args = new Object[indexes.length];
				
				int j=0;
				for (int i : indexes)
				{
					if (i < 100)
						args[j++] = arguments[i];
					else
						args[j++] = saved[i-100];
				}
				
				next(args);
			}
		};
	}
	
}
