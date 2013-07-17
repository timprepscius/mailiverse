/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.ArrayList;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import mail.client.Master;

import core.util.Strings;

@Export()
public class Recipients implements Exportable
{
	public static final String To = "to", Cc = "cc", Bcc = "bcc", ReplyTo = "reply-to";
	
	protected List<Identity> to, cc, bcc, replyTo;

	List<Identity> toIdentityList (Master master, String is)
	{
		AddressBook addressBook = master.getAddressBook();
	
		String[] parts = is.split(",");
		List<Identity> identities = new ArrayList<Identity>(parts.length);

		for (String part : parts)
		{
			String trimmed = part.trim();
			if (trimmed.isEmpty())
				continue;
			
			Identity identity = addressBook.getIdentity(new UnregisteredIdentity(trimmed));
			if (!identities.contains(identity))
				identities.add(identity);
		}
		
		return identities;
	}
	
	public Recipients ()
	{
		this.to = new ArrayList<Identity>();
		this.cc = new ArrayList<Identity>();
		this.bcc = new ArrayList<Identity>();
		this.replyTo = new ArrayList<Identity>();
	}
	
	public Recipients (Identity[] to, Identity[] cc, Identity[] bcc, Identity[] replyTo)
	{
		this.to = new ArrayList<Identity>();
		this.cc = new ArrayList<Identity>();
		this.bcc = new ArrayList<Identity>();
		this.replyTo = new ArrayList<Identity>();

		if (to != null)
			for (Identity i : to)
				this.to.add(i);

		if (cc != null)
			for (Identity i : cc)
				this.cc.add(i);

		if (bcc != null)
			for (Identity i : bcc)
				this.bcc.add(i);
		
		if (replyTo != null)
			for (Identity i : replyTo)
				this.replyTo.add(i);

	}
	
	public void add (List<Identity> from, List<Identity> to)
	{
		for (Identity i : from)
			if (!to.contains(i))
				to.add(i);
	}
	
	public void add (Recipients r)
	{
		add (r.to, to);
		add (r.cc, cc);
		add (r.bcc, bcc);
		add (r.replyTo, replyTo);
	}
	
	public List<Identity> get(String key)
	{
		if (key.equals(To))
			return to;
		else
		if (key.equals(Cc))
			return cc;
		else
		if (key.equals(Bcc))
			return bcc;
		else
		if (key.equals(ReplyTo))
			return replyTo;
		
		return null;
	}
	
	public List<Identity> getTo()
	{
		return to;
	}
	
	public void setTo (List<Identity> to)
	{
		this.to = to;
	}
	
	public List<Identity> getCc ()
	{
		return cc;
	}
	
	public void setCc (List<Identity> cc)
	{
		this.cc = cc;
	}

	public List<Identity> getBcc ()
	{
		return bcc;
	}
	
	public void setBcc (List<Identity> bcc)
	{
		this.bcc = bcc;
	}

	public List<Identity> getReplyTo ()
	{
		return replyTo;
	}

	public void setReplyTo (List<Identity> replyTo)
	{
		this.replyTo = replyTo;
	}
	
	public List<Identity> getAll ()
	{
		ArrayList<Identity> all = new ArrayList<Identity>();
		all.addAll(to);
		all.addAll(cc);
		all.addAll(bcc);
		all.addAll(replyTo);
		
		ArrayList<Identity> once = new ArrayList<Identity>();
		for (Identity identity : all)
		{
			if (!once.contains(all))
				once.add(identity);
		}
		
		return once;
	}
	
	protected void registerRecipients (AddressBook addressBook, List<Identity> to)
	{
		Identity[] save = to.toArray(new Identity[0]);
		to.clear();
		
		for (Identity i : save)
		{
			if (i instanceof UnregisteredIdentity)
				to.add(addressBook.getIdentity((UnregisteredIdentity)i));
			else
				to.add(i);
		}
	}
	
	public void registerRecipients (AddressBook addressBook)
	{
		registerRecipients (addressBook, to);
		registerRecipients (addressBook, cc);
		registerRecipients (addressBook, bcc);
		registerRecipients (addressBook, replyTo);
	}
	
	public boolean contains (Identity identity)
	{
		return 
			to.contains(identity) ||
			cc.contains(identity) ||
			bcc.contains(identity) ||
			replyTo.contains(identity);
	}
	
	public String shortList ()
	{
		List<String> shorts = new ArrayList<String>();
		for (Identity i : getAll())
			shorts.add(i.getShortName());
		
		return Strings.concat(shorts.iterator(), ", ");
	}
}
