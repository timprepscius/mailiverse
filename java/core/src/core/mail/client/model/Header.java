/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mail.client.Master;

import core.util.DateFormat;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Strings;

@Export()
public class Header implements Exportable
{
	static LogNull log = new LogNull (Header.class);
	
	protected String externalKey;
	protected String originalKey;
	protected Identity author;
	protected Set<Identity> authors;
	protected String subject;
	protected Date date;
	protected Recipients recipients;
	protected Dictionary dictionary;
	protected TransportState state;
	protected String brief;
	protected String uidl;
	
	public Header (String externalKey, String originalKey, String uidl, Identity author, Recipients recipients, String subject, Date date, TransportState state, String brief)
	{
		this.externalKey = externalKey;
		this.originalKey = originalKey;
		this.uidl = uidl;
		this.author = author;
		this.subject = subject;
		this.date = date;		
		this.recipients = recipients;
		this.state = state;
		this.brief = brief;
	}
	
	public Header ()
	{
		
	}
	
	public String getExternalKey ()
	{
		return externalKey;
	}
	
	public void setOriginalKey (String originalKey)
	{
		this.originalKey = originalKey;
	}
	
	public String getOriginalKey ()
	{
		return originalKey;
	}

	public void setExternalKey (String externalKey)
	{
		this.externalKey = externalKey;
	}
	
	public Identity getAuthor ()
	{
		return author;
	}
	
	public Identity[] filterMe (List<Identity> identities, Identity me)
	{
		if (identities == null)
			return null;
		
		List<Identity> filtered = new ArrayList<Identity>();
		for (Identity identity : identities)
		{
			if (identity != me)
			{
				log.debug("filterMe adding", identity.debug(), "is not",me.debug());
				filtered.add(identity);
			}
		}
		
		if (filtered.isEmpty())
			return null;
		
		return filtered.toArray(new Identity[0]);
	}
	
	public Identity[] calculateReplyTo (Master master)
	{
		log.debug("calculate replyTo");
		Identity me = master.getIdentity();
		
		Identity[] results = null;
		if (getRecipients()!=null)
		{
			log.debug("recipients not null");
			
			results = filterMe(getRecipients().getReplyTo(), me);
			if (results != null)
				return results;
			
			if (getAuthor() == me)
			{
				results = filterMe(getRecipients().getAll(), me);
				if (results != null)
					return results;
			}
		}
		
		if (getAuthor() != null)
			return new Identity[] { getAuthor() };
		
		return new Identity[0];
	}
		
	public Identity[] calculateReplyAll (Master master)
	{
		Identity me = master.getIdentity();
		ArrayList<Identity> results = new ArrayList<Identity>();
		
		if (getRecipients()!=null)
		{
			Identity[] filtered = filterMe(getRecipients().getAll(), me);
			if (filtered != null)
				for (Identity identity : filtered)
					results.add(identity);
		}
		
		Identity author = getAuthor();
		if (author!=null && author != me && !results.contains(author))
			results.add(0, author);

		return results.toArray(new Identity[0]);
	}

	public void setAuthor (Identity author)
	{
		this.author = author;
	}
	
	public Set<Identity> getAuthors ()
	{
		return authors;
	}
	
	public void setAuthors (List<Identity> authors)
	{
		this.authors = new HashSet<Identity>();
		this.authors.addAll(authors);
	}
	
	public String getAuthorsShortList ()
	{
		String[] shorts = new String[authors.size()];
		int j=0;
		for (Identity i : authors)
			shorts[j++] = i.getShortName();
		
		return Strings.concat(shorts, ", ");
	}
	
	public String getSubject ()
	{
		return subject;
	}
	
	public String getSubjectExcludingReplyPrefix ()
	{
		String subject = this.subject;
		
		if (subject == null)
			subject = "";
		
		while (subject.toLowerCase().startsWith(ConstantsMisc.REPLY_PREFIX.toLowerCase()))
		{
			subject = subject.substring(ConstantsMisc.REPLY_PREFIX.length()).trim();
		}
		
		return subject;
	}
	
	public Dictionary getDictionary ()
	{
		return dictionary;
	}
	
	public void setDictionary (Dictionary dictionary)
	{
		this.dictionary = dictionary;
	}
	
	public Date getDate ()
	{
		return date;
	}
	
	public void setDate (Date date)
	{
		this.date = date;
	}
	
	public Recipients getRecipients ()
	{
		return recipients;
	}

	public void setRecipients (Recipients recipients)
	{
		this.recipients = recipients;
	}

	public void setSubject(String subject) 
	{
		this.subject = subject;
	}
	
	public void setTransportState (TransportState state)
	{
		this.state = state;
	}
	
	public TransportState getTransportState ()
	{
		return state;
	}	
	
	public boolean hasState(String flag)
	{
		return state.has(flag);
	}
		
	public void markState (String flag)
	{
		state.mark(flag);
	}
	
	public void unmarkState(String flag)
	{
		state.unmark(flag);
	}
	
	public void markModification ()
	{
		this.date = new Date();
	}
	
	public String getBrief()
	{
		return brief;
	}

	public void setBrief(String brief)
	{
		this.brief = brief;
	}
	
	public String getRelativeDate ()
	{
		String result = "Unknown";
		
		if (date == null)
			result = "Infinity + 1";
		else
		{
			DateFormat std = new DateFormat("yyyyMMddhhmmss");
			String now = std.format(new Date());
			String then = std.format(date);
			
			if (then.startsWith(now.substring(0,10)))
			{
				long diff = (new Date().getTime()/(60 * 1000)) - (date.getTime()/(60 * 1000));
				result = diff + " min";
			}
			else
			if (then.startsWith(now.substring(0,8)))
			{
				DateFormat sdf = new DateFormat("h:mm a");
				result = sdf.format(date).toLowerCase();
			}
			else
			{
				if (then.startsWith(now.substring(0,4)))
				{
					DateFormat sdf = new DateFormat("MMM d");
					result = sdf.format(date);
				}
				else
				{
					DateFormat sdf = new DateFormat("MM/dd/yy");
					result = sdf.format(date);
				}
			}
		}
		
		log.debug("Header.getRelativeDate ", result);
		return result;
	}

	public String getUIDL()
	{
		return uidl;
	}
	
	public void setUIDL (String uidl)
	{
		this.uidl = uidl;
	}
}
