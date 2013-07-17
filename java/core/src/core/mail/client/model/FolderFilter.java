/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.NoExport;

import mail.client.CacheManager;
import mail.client.cache.Type;

public class FolderFilter extends FolderSet 
{
	@NoExport
	public FolderFilter (CacheManager manager)
	{
		super(manager, Type.FolderPart);
	}
	
	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		preCacheMostRecentFolder();
	}
	
	public boolean matchesFilter (Conversation conversation)
	{
		return true;
	}
	
	@Override
	public synchronized void conversationAdded (Conversation conversation)
	{
		if (matchesFilter(conversation))
		{
			super.conversationAdded(conversation);
		}
	}
	
	@Override
	public synchronized void conversationDeleted (Conversation conversation)
	{
		if (hasConversation(conversation))
		{
			super.conversationDeleted(conversation);
		}
	}
	
	public synchronized void manuallyAdd (Conversation conversation)
	{
		if (!super.hasConversation(conversation))
		{
			folderDefinition.conversationAdded(conversation);
			super.conversationAdded(conversation);
		}
	}
	
	public synchronized void manuallyRemove (Conversation conversation)
	{
		if (super.hasConversation(conversation))
		{
			folderDefinition.conversationDeleted(conversation);
			super.conversationDeleted(conversation);
		}
	}
}
