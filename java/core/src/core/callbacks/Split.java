/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.Callback;

public class Split extends Callback {

	Callback split;
	
	public Split(Callback split)
	{
		this.split = split;
	}
	
	@Override
	public void invoke(Object... arguments) {
		split.invoke(arguments);
		
		next(arguments);
	}

}
