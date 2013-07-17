/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.NoExport;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import core.callbacks.Single;
import core.util.LogNull;
import core.util.Pair;

import mail.client.CacheManager;
import mail.client.Events;
import mail.client.cache.ID;
import mail.client.cache.Type;

@Export()
public class FolderSet extends Folder
{
	static LogNull log = new LogNull(FolderSet.class);
	List<ID> parts;
	int numConversations;
	Type childType;
	
	@NoExport
	public FolderSet(CacheManager manager, Type childType)
	{
		super(manager);
		this.childType = childType;
		reset();
		
		getLoadCallbacks()
			.addCallback(manager.getMaster().getEventPropagator().signal_(Events.LoadFolder, this));
	}
	
	public void preCacheMostRecentFolder ()
	{
		if (!parts.isEmpty())
		{
			CacheManager cache = getManager();
			cache.getFolder(getChildType(), parts.get(0));
		}
	}

	public void reset ()
	{
		parts = new ArrayList<ID>();
		this.numConversations = 0;
	}
	
	public void addFolderId (ID id)
	{
		parts.add(0,id);
	}
	
	protected void addFolderIdEnd (ID id)
	{
		parts.add(id);
	}

	public List<ID> getFolderIds()
	{
		return parts;
	}
	
	public void addFolder (Folder folder)
	{
		addFolderIdEnd(folder.getId());
		markDirty();
	}
	
	public void removeFolder(Folder folder) 
	{
		if (!parts.contains(folder.getId()))
			return;
		
		parts.remove(folder.getId());
		markDirty();
		
		if (folder.isLoaded())
			folder.markDeleted();
		else
			folder.getLoadCallbacks().addCallback(new Single(markDeleted_()));		
	}
	
	protected void onDeleting ()
	{
		List<Folder> parts = getFolders();
		for(Folder part : parts)
			removeFolder(part);
	}
	
	public List<Folder> getFolders ()
	{
		List<Folder> f = new ArrayList<Folder>(parts.size());
		for (ID id : parts)
			f.add(getManager().getFolder(getChildType(), id));
		
		return f;
	}
	
	@Override
	public List<Conversation> getConversations(int from, int length, String filter)
	{
		log.debug("filter: ", filter);
		
		int totalLength = from+length;
		List<Conversation> result = new ArrayList<Conversation>(totalLength);
		
		for (ID id : parts)
		{
			Folder f = getManager().getFolder(getChildType(), id);

			if (f.isLoaded())
			{
				result.addAll(f.getConversations(0, totalLength - result.size(), filter));
				
				if (result.size() >= totalLength)
					break;
			}
			else
				break;
		}
		
		totalLength = Math.min(totalLength, result.size());
		return result.subList(from, totalLength);
	}

	@Override
	public boolean hasConversation(Conversation conversation)
	{
		for (ID id : parts)
		{
			Folder f = getManager().getFolder(getChildType(), id);

			if (f.isLoaded())
				if (f.hasConversation(conversation))
					return true;
		}
			
		return false;
	}
	
	public Type getChildType ()
	{
		return childType;
	}
	
	@Override
	public void conversationAdded(Conversation conversation)
	{
		CacheManager cache = getManager();
		
		Folder first = !parts.isEmpty() ? cache.getFolder(getChildType(), parts.get(0)) : null;
		
		log.debug ("FolderSet.conversationAdded ", first);
					
		if (parts.isEmpty() || !first.isLoaded() || first.isFull())
		{
			first = cache.newFolder(getChildType(), new FolderDefinition(getId().toFileSystemSafe() + ":part"));
			parts.add(0, first.getId());
		}	
		
		first.conversationAdded(conversation);

		numConversations++;
		markDirty();
	}

	@Override
	public void conversationDeleted(Conversation conversation)
	{
		for (ID id : parts)
		{
			Folder f = getManager().getFolder(getChildType(), id);

			if (f.isLoaded())
			{
				if (f.hasConversation(conversation))
				{
					f.conversationDeleted(conversation);
					numConversations--;
					markDirty();
					
					break;
				}
			}
		}
		
	}
	
	public int getNumConversations ()
	{
		return numConversations;
	}

	public void setNumConversations (int numConversations)
	{
		this.numConversations = numConversations;
	}
	
	@Override
	public List<Pair<ID,Date>> getConversationIds()
	{
		assert(false);
		return null;
	}

	@Override
	public void addConversationId(ID id, Date date)
	{
		assert(false);
	}

	@Override
	public boolean isFull()
	{
		assert(false);
		return false;
	}
	
	@Override
	public Conversation getMatchingConversation (Header header)
	{
		for (ID id : parts)
		{
			Folder f = getManager().getFolder(getChildType(), id);

			if (f.isLoaded())
			{
				Conversation c = f.getMatchingConversation(header);
				if (c != null)
					return c;
			}
		}
		
		return null;
	}
}
