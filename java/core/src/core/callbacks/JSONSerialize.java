/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.CallbackDefault;
import core.util.JSONSerializer;

public class JSONSerialize extends CallbackDefault
{
	public void onSuccess (Object... args) throws Exception
	{
		callback.invoke(JSONSerializer.serialize(args[0]));
	}
};
