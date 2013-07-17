/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.timepedia.exporter.client.NoExport;

import core.util.Collectionz;
import core.util.Comparators;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;

import mail.client.CacheManager;
import mail.client.Events;
import mail.client.cache.ID;

public class FolderPart extends Folder
{
	static LogNull log = new LogNull(FolderPart.class);
	static final int MAX_FOLDER_CONVERSATIONS = 1000;
	
	List<Pair<ID,Date>> conversations;
	
	@NoExport
	public FolderPart (CacheManager manager)
	{
		super(manager);
		reset();

		getLoadCallbacks()
			.addCallback(manager.getMaster().getEventPropagator().signal_(Events.LoadFolderPart, this));
	}
	
	public void reset ()
	{
		conversations = new ArrayList<Pair<ID,Date>>();
	}
	
	@Override
	public List<Conversation> getConversations (int from, int length, String filter)
	{
		CacheManager cache = getManager();

		log.debug("getConversations ", from, ":", length);
		
		Dictionary filterDictionary = null;
		if (filter != null)
			filterDictionary = new Dictionary(filter);
		
		log.debug("getConversations using filter dictionary", filterDictionary);
		
		int min = Math.min(from+length, conversations.size());
		List<Conversation> result = new ArrayList<Conversation>(min);

		for (int i=from; i<conversations.size() && result.size() < length; ++i)
		{
			Conversation c = 
				cache.getConversation(
					conversations.get(i).first
				);
			
			boolean matches = true;
			if (filter != null)
			{
				// we have to only exclude if we can actually match if things are loaded.
				// else we need to put in more logic not to load all of the parts of a folder on a search.
				if (c.getHeader()!=null && c.getHeader().getDictionary()!=null)
					matches = c.getHeader().getDictionary().matches(filterDictionary);
			}
			
			if (matches)
				result.add(c);
		}		
		return result;
	}
	
	@Override
	public boolean hasConversation (Conversation conversation)
	{
		return Collectionz.containsByFirst(conversations, conversation.getId());
	}
	
	@Override
	public synchronized void addConversationId (ID id, Date date)
	{
		conversations.add(new Pair<ID,Date>(id, date));
		Collections.sort(conversations, new Comparators.SortBySecondNaturalOpposite<Date>());
	}
	
	protected synchronized void removeConversationId (Object id)
	{
		Collectionz.removeByFirst(conversations, id);
	}
	
	@Override
	public List<Pair<ID,Date>> getConversationIds()
	{
		return conversations;
	}
	
	@Override
	public boolean isFull ()
	{
		return conversations.size() > MAX_FOLDER_CONVERSATIONS;
	}
	
	@Override
	public synchronized void conversationAdded (Conversation conversation)
	{
		addConversationId(conversation.getId(), conversation.getHeader().getDate());
		markDirty();
	}

	@Override
	public synchronized void conversationDeleted (Conversation conversation)
	{
		removeConversationId(conversation.getId());
		markDirty();
	}

	@Override
	public Conversation getMatchingConversation (Header header)
	{
		if (header.getSubject() == null)
			return null;
		
		String headerSubject = header.getSubjectExcludingReplyPrefix ();

		for (Pair<ID,Date> pair : conversations)
		{
			ID id = pair.first;
			Conversation conversation = getManager().getConversation(id);
			
			if (conversation.isLoaded())
			{
				Header compare = conversation.getHeader();
				String compareSubject = compare.getSubjectExcludingReplyPrefix();
				
				if (compareSubject.toLowerCase().equals(headerSubject.toLowerCase()))
				{
					return conversation;
				}
			}
		}
		
		return null;
	}
}
