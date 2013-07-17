/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.util.LogNull;
import core.util.LogNull;

@Export
public class FolderDefinition implements Exportable
{
	static LogNull log = new LogNull(FolderDefinition.class);

	String name;
	Identity author;
	Identity recipient;
	String subject;
	TransportState stateEquals, stateDiffers;
	
	boolean autoBayesian = false;
	Dictionary bayesianDictionary;
	
	public FolderDefinition (String name)
	{
		this.name = name;
	}
	
	public String getName ()
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public FolderDefinition setAuthor(Identity author)
	{
		this.author = author;
		return this;
	}
	
	public Identity getAuthor ()
	{
		return author;
	}
	
	public FolderDefinition setRecipient(Identity recipient)
	{
		this.recipient = recipient;
		return this;
	}
	
	public Identity getRecipient ()
	{
		return recipient;
	}
	
	public FolderDefinition setSubject(String subject)
	{
		this.subject = subject;
		return this;
	}
	
	public String getSubject ()
	{
		return subject;
	}
	
	public FolderDefinition setState(TransportState stateEquals, TransportState stateDiffers)
	{
		this.stateEquals = stateEquals;
		this.stateDiffers = stateDiffers;
		return this;
	}
	
	public TransportState getStateEquals ()
	{
		return stateEquals;
	}
	
	public TransportState getStateDiffers ()
	{
		return stateDiffers;
	}

	public boolean matchesFilter (Conversation conversation)
	{
		Header h = conversation.getHeader();
		
		boolean matches = true;
		if (matches && author != null)
		{
			matches = h.getAuthors().contains(author);
		}
		if (matches && recipient != null)
		{
			matches = h.getRecipients().contains(recipient);
		}
		if (matches && subject != null)
		{
			matches = h.getSubject().equals(subject);
		}
		if (matches && (stateEquals != null || stateDiffers != null))
		{
			boolean equalMatch = stateEquals != null ? (h.getTransportState().hasOne(stateEquals)) : true;
			boolean differMatch = stateDiffers != null ? (h.getTransportState().hasNone(stateDiffers)) : true;
			
			matches = equalMatch && differMatch;
			log.debug("matches filter ", stateEquals, ":!", stateDiffers, " : ", h.getTransportState(), " = ("+ equalMatch, "&", differMatch, ") = ",  matches);
		}
		if (matches && bayesianDictionary != null && autoBayesian)
		{
			matches = bayesianMatches(conversation);
		}			
		
		return matches;
	}
	
	public Dictionary getBayesianDictionary ()
	{
		return bayesianDictionary;
	}
	
	public FolderDefinition setBayesianDictionary(Dictionary bayesianDictionary) 
	{
		this.bayesianDictionary = bayesianDictionary;
		return this;
	}
	
	public FolderDefinition setAutoBayesian (boolean autoBayesian)
	{
		this.autoBayesian = autoBayesian;
		return this;
	}
	
	public boolean bayesianMatches (Conversation conversation)
	{
		return bayesianDictionary.bayesianMatches(conversation.getHeader().getDictionary());
	}
	
	public boolean getAutoBayesian ()
	{
		return autoBayesian;
	}

	public void conversationAdded(Conversation conversation) 
	{
		if (bayesianDictionary != null)
		{
			bayesianDictionary.add(conversation.getHeader().getDictionary());
		}
	}
	
	public void conversationDeleted(Conversation conversation) 
	{
		if (bayesianDictionary != null)
		{
			bayesianDictionary.subtract(conversation.getHeader().getDictionary());
		}
	}
}
