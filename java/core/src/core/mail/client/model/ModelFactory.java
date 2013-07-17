/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import mail.client.CacheManager;
import mail.client.cache.Item;
import mail.client.cache.ItemFactory;
import mail.client.cache.Type;

public class ModelFactory implements ItemFactory
{
	CacheManager manager;
	
	public ModelFactory (CacheManager manager)
	{
		this.manager = manager;
	}
	
	@Override
	public Item instantiate (Type type)
	{
		switch (type)
		{
			case Mail:
				return new Mail(manager);
			case Conversation:
				return new Conversation(manager);
			case FolderPart:
				return new FolderPart(manager);
			case FolderFilterSet:
				return new FolderFilterSet(manager);
			case FolderMaster:
				return new FolderMaster(manager);
			case FolderFilter:
				return new FolderFilterSimple(manager);
			case FolderRepository:
				return new FolderRepository(manager);
		}
		
		return null;
	}
}
