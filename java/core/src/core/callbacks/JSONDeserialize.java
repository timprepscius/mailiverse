/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.JSONSerializer;
import core.util.Zip;

public class JSONDeserialize extends CallbackDefault
{
	public void onSuccess (Object... args) throws Exception
	{
		callback.invoke(JSONSerializer.deserialize((byte[])args[0]));
	}
};
