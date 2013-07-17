/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import core.callback.Callback;
import mail.client.cache.IndexedCacheSerializer;
import mail.client.cache.Item;
import mail.client.cache.ItemSerializer;
import mail.client.cache.JSON;
import mail.client.model.ModelSerializer;
import mail.client.model.Settings;

public class MasterCacheSerializer implements ItemSerializer
{
	IndexedCacheSerializer indexedCacheSerializer;
	ModelSerializer settingsSerializer;
	
	public MasterCacheSerializer (JSON json)
	{
		indexedCacheSerializer = new IndexedCacheSerializer();
		settingsSerializer = new ModelSerializer(json);
	}
	
	@Override
	public Callback serialize_(Item item) 
	{
		if (item instanceof Settings)
			return settingsSerializer.serialize_(item);
		
		return indexedCacheSerializer.serialize_(item);
	}
	@Override
	public Callback deserialize_(Item item) 
	{
		if (item instanceof Settings)
			return settingsSerializer.deserialize_(item);
		
		return indexedCacheSerializer.deserialize_(item);
	}
	
	
}
