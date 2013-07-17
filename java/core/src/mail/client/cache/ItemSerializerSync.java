/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.io.IOException;

import core.callback.Callback;
import core.callback.CallbackDefault;

public abstract class ItemSerializerSync implements ItemSerializer
{
	public abstract byte[] serialize(Item item) throws Exception;
	public abstract void deserialize(Item item, byte[] bytes) throws Exception;
	
	@Override
	public Callback serialize_(Item item) {
		return new CallbackDefault(item) {

			@Override
			public void onSuccess(Object... arguments) throws Exception {
				Item item = V(0);
				item.onPreStore();
				next(serialize(item));
			}
		};
	}

	@Override
	public Callback deserialize_(Item item) {
		return new CallbackDefault(item) {

			@Override
			public void onSuccess(Object... arguments) throws Exception {
				Item item = V(0);
				item.onPreLoad();
				deserialize(item, (byte[])arguments[0]);
				next((Item)V(0));
			}
		};
	}

}
