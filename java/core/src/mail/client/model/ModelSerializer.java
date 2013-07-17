/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.JSON_;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Strings;
import core.util.Zip;
import mail.client.cache.Item;
import mail.client.cache.ItemSerializer;
import mail.client.cache.JSON;

public class ModelSerializer implements ItemSerializer 
{
	static LogNull log = new LogNull(ModelSerializer.class);
	
	JSON json;
	
	public ModelSerializer(JSON json)
	{
		this.json = json;
	}
	
	public byte[] serialize (Item item) throws Exception
	{
		log.debug("serialize", item);
		
		item.markPreStore();
		
		if (item instanceof Mail)
			return Strings.toBytes(json.toJSON((Mail)item).toString());
		if (item instanceof Conversation)
			return Strings.toBytes(json.toJSON((Conversation)item).toString());
		if (item instanceof Folder)
			return Strings.toBytes(json.toJSON((Folder)item).toString());
		if (item instanceof Settings)
			return Strings.toBytes(json.toJSON((Settings)item).toString());
		
		return null;
	}
	
	public void deserialize (Item item, byte[] bytes) throws Exception
	{
		log.debug("deserialize", item);
		
		item.markPreLoad();
		
		if (item instanceof Mail)
			json.fromJSON((Mail)item, JSON_.parse(Strings.toString(bytes)));
		if (item instanceof Conversation)
			json.fromJSON((Conversation)item, JSON_.parse(Strings.toString(bytes)));
		if (item instanceof Folder)
			json.fromJSON((Folder)item, JSON_.parse(Strings.toString(bytes)));
		if (item instanceof Settings)
			json.fromJSON((Settings)item, JSON_.parse(Strings.toString(bytes)));
	}
	
	@Override
	public Callback serialize_(Item item) 
	{
		return new CallbackDefault(item) {
			public void onSuccess(Object... arguments) throws Exception {
				next(serialize((Item)V(0)));
			}
		}.addCallback(Zip.deflate_());
	}

	@Override
	public Callback deserialize_(Item item) 
	{
		return Zip.inflate_().addCallback(
			new CallbackDefault(item) {
				public void onSuccess(Object... arguments) throws Exception {
					deserialize((Item)V(0), (byte[])arguments[0]);
					next();
				}
			}
		);
	}

}
