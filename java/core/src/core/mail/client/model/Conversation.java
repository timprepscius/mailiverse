/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.util.Collectionz;
import core.util.Comparators;
import core.util.LogNull;
import core.util.Pair;
import mail.client.CacheManager;
import mail.client.Events;
import mail.client.cache.ID;

@Export
public class Conversation extends Model implements Exportable
{
	static LogNull log = new LogNull(Conversation.class);

	static class SortByDateLatestFirst implements Comparator<Conversation>  {

		@Override
		public int compare(Conversation l, Conversation r)
		{
			Date ld = l.getHeader().getDate(), rd = r.getHeader().getDate();
			return rd.compareTo(ld);
		}
	}
	
	protected Header header;
	protected List<Pair<ID,Date>> items;
	protected Set<ID> itemIds = new HashSet<ID>();
	
	public Conversation (CacheManager manager)
	{
		super(manager);
		reset();
		
		getLoadCallbacks()
			.addCallback(manager.getMaster().getEventPropagator().signal_(Events.LoadConversation, this));
	}
	
	public void reset ()
	{
		items = new ArrayList<Pair<ID, Date>>();
		recomputeHeader();
	}
	
	protected void recomputeHeader ()
	{
		header = new Header();
		header.setDictionary(new Dictionary());
		header.setAuthors(new ArrayList<Identity>());
		header.setRecipients(new Recipients());
		header.setTransportState(TransportState.NONE());
		
		for (Pair<ID,Date> p : items)
		{
			Mail m = getManager().getMail(p.first);
			if (m.isLoaded())
				accumulate(m);
		}		
	}
	
	protected void accumulate(Mail m)
	{
		Header h = m.getHeader();
		
		if (h.getAuthor() != null)
			header.getAuthors().add(h.getAuthor());
		
		if (header.getDate() == null || h.getDate().after(header.getDate()))
		{
			header.setDate(h.getDate());
			header.setBrief(h.getBrief());
			header.setSubject(h.getSubjectExcludingReplyPrefix());
		}	

		if (h.getRecipients() != null)
			header.getRecipients().add(h.getRecipients());
		
		header.getDictionary().add(m);
		header.getTransportState().mark(h.getTransportState());
		header.unmarkState(TransportState.READ);
	}
	
	public List<Mail> getItems () 
	{
		List<Mail> result = new ArrayList<Mail>(items.size());
		for (Pair<ID,Date> p : items)
		{
			Mail m = getManager().getMail(p.first);
			result.add(m);
		}		
		
		return result;
	}
	
	public List<Pair<ID,Date>> getItemIds ()
	{
		return items;
	}
	
	public void addItemId (ID id, Date date)
	{
		items.add(new Pair<ID,Date>(id,date));
		Collections.sort(items, new Comparators.SortBySecondNatural<Date>());
	}
	
	public void removeItemId (ID id)
	{
		Collectionz.removeByFirst(items, id);
	}
	
	public void addItem (Mail mail)
	{
		addItemId (mail.getId(), mail.getHeader().getDate());
		accumulate (mail);
		
		markDirty();
	}
	
	public void removeItem (Mail mail)
	{
		removeItemId (mail.getId());
		recomputeHeader();

		markDirty();
	}
	
	public void itemChanged (Mail mail)
	{
		for (Pair<ID,Date> p : items)
		{
			if (p.first == mail.getId())
				p.second = mail.getHeader().getDate();
		}

		Collections.sort(items, new Comparators.SortBySecondNatural<Date>());
		recomputeHeader();
		
		markDirty();
	}
	
	public Header getHeader ()
	{
		return header;
	}
	
	public void setHeader (Header header)
	{
		this.header = header;
	}
	
	public int getNumItems ()
	{
		return items.size();
	}
	
	public void markState (String state)
	{
		if (!getHeader().hasState(state))
		{
			getHeader().markState(state);
			markDirty();
		}
	}
	
	public void unmarkState (String state)
	{
		if (getHeader().hasState(state))
		{
			getHeader().unmarkState(state);
			markDirty();
		}
	}
}
