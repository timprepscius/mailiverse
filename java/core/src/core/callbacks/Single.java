/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.Callback;

public class Single extends CountDown
{
	public Single(Callback onFinished) {
		super(1, onFinished);
	}
}
