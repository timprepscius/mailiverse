/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callback;


public class CallbackEmpty extends CallbackDefault
{
	@Override
	public void onSuccess(Object... arguments) throws Exception {
		next(arguments);
	}

}
