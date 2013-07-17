/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.Date;
import java.util.List;

import core.callback.Callback;
import core.util.Pair;
import mail.client.CacheManager;
import mail.client.cache.ID;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

@Export()
public abstract class Folder extends Model implements Exportable
{
	FolderDefinition folderDefinition;
	
	@NoExport()
	public Folder (CacheManager manager)
	{
		super(manager);
	}
	
	public FolderDefinition getFolderDefinition()
	{
		return folderDefinition;
	}
	
	public void setFolderDefinition(FolderDefinition folderDefinition)
	{
		this.folderDefinition = folderDefinition;
	}
	
	public String getName()
	{
		return folderDefinition.getName();
	}
	
	public void setName(String name)
	{
		folderDefinition.setName(name);
		markDirty();
	}
	
	public abstract List<Pair<ID,Date>> getConversationIds ();
	public abstract void addConversationId (ID id, Date date);
	public abstract boolean isFull ();
	
	public abstract List<Conversation> getConversations (int from, int length, String filter);
	public abstract boolean hasConversation (Conversation conversation);
	public abstract void conversationAdded (Conversation conversation);
	public abstract void conversationDeleted (Conversation conversation);
	public abstract Conversation getMatchingConversation (Header header);

	public final void conversationChanged (Conversation conversation)
	{
		conversationDeleted(conversation);
		conversationAdded(conversation);
	}
}
