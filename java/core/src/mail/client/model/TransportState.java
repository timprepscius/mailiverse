/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.util.Strings;

@Export()
public class TransportState implements Exportable
{
	public static final String 
		RECEIVED = "RECEIVED",
		SENT = "SENT",
		DRAFT = "DRAFT",
		SENDING = "SENDING",
		TRASH = "TRASH",
		SPAM = "SPAM",
		READ = "READ";
	
	public static TransportState NONE()
	{
		return new TransportState();
	}
	
	List<String> state = new ArrayList<String>();
	
	public TransportState ()
	{
	}
	
	public void mark(String flag)
	{
		if (flag == null)
			return;
		
		if (state.contains(flag))
			return;
		
		state.add(flag.toUpperCase());
	}
	
	public void unmark (String flag)
	{
		state.remove(flag.toUpperCase());
	}
	
	public void mark(String flag, boolean state)
	{
		if (state)
			mark(flag);
		else
			unmark(flag);
	}
	
	public void mark(TransportState flags)
	{
		if (flags == null)
			return;
		
		for (String flag : flags.state)
		{
			mark(flag);
		}
	}
	
	public boolean has (String flag)
	{
		return state.contains(flag.toUpperCase());
	}
	
	public boolean hasOne (TransportState state)
	{
		for (String flag : state.state)
		{
			if (has(flag))
				return true;
		}
		
		return false;
	}
	
	public boolean hasAll (TransportState state)
	{
		for (String flag : state.state)
		{
			if (has(flag))
				return false;
		}
		
		return true;
	}

	public boolean hasNot (String flag)
	{
		return !has(flag);
	}
	
	public boolean hasNone (TransportState state)
	{
		return !hasOne(state);
	}
	
	public String toString ()
	{
		return Strings.concat(state, ",");
	}
	
	public static TransportState fromString (String flagString)
	{
		TransportState state = new TransportState();
		
		if (!flagString.isEmpty())
		{
			String[] flags = flagString.split(",");
			for (String flag : flags)
			{
				state.mark(flag);
			}
		}		
		return state;
	}
	
	public static TransportState fromList (String... flags)
	{
		TransportState state = new TransportState();
		
		for (String flag : flags)
		{
			state.mark(flag);
		}
		
		return state;
	}
}
