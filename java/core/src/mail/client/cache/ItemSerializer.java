/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import core.callback.Callback;

public interface ItemSerializer 
{
	Callback serialize_ (Item item);
	Callback deserialize_ (Item item);
}
