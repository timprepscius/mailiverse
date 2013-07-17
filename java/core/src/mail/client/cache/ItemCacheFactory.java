/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

public class ItemCacheFactory extends IndexedCache.Factory 
{
	String prefix;
	StoreLibrary library;
	ItemFactory factory;
	ItemSerializer serializer;
	
	public ItemCacheFactory(String prefix, StoreLibrary library, ItemFactory factory, ItemSerializer serializer) 
	{
		this.library = library;
		this.prefix = prefix;
		this.factory = factory;
		this.serializer = serializer;
	}

	@Override
	public Cache getCache(ID id, boolean isNew)
	{
		return new Cache(
			factory,
			serializer,
			library.instantiate(prefix, id, isNew)
		);
	}

}
