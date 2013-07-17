/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.util.LogNull;
import core.util.LogOut;

@Export
public class AddressBook implements Exportable
{
	static LogNull log = new LogNull(AddressBook.class);
	
	List<Identity> people;
	Map<String, Identity> indexedByEmail;
	
	public AddressBook ()
	{
		people = new ArrayList<Identity>();
		indexedByEmail = new HashMap<String,Identity>();
	}
	
	public boolean hasIdentity (UnregisteredIdentity identity)
	{
		return indexedByEmail.containsKey(identity.getEmail());
	}
	
	public Identity getIdentity (UnregisteredIdentity identity)
	{
		Identity stored = indexedByEmail.get(identity.getEmail());
		
		if (stored != null)
		{
			log.debug("getIdentity",identity,"exists");
			clearIndices (stored);
			stored.copyFrom (identity);
		}
		else
		{
			log.debug("getIdentity",identity,"new");
			stored = new Identity();
			stored.copyFrom(identity);
			people.add(stored);
		}
		
		index(stored);
		
		return stored;
	}
	
	public List<Identity> parseAddressString (String s)
	{
		List<Identity> list = new ArrayList<Identity>();
		String[] split = s.split(",");
		for (String address : split)
		{
			address = address.trim();
			if (!address.isEmpty())
				list.add(getIdentity (new UnregisteredIdentity(address)));
		}
		
		return list;
	}
	
	public List<Identity> parseUnfinishedAddressString (String s)
	{
		List<Identity> list = new ArrayList<Identity>();
		String[] split = s.split(",");
		for (String address : split)
		{
			address = address.trim();
			if (!address.isEmpty())
			{
				UnregisteredIdentity uri = new UnregisteredIdentity (address);
				if (hasIdentity(uri))
					list.add(getIdentity(uri));
				else
					list.add(uri);
			}
		}
		
		return list;
	}
	
	public void removeIdentity (Identity identity)
	{
		Identity stored = indexedByEmail.get(identity.getEmail());
		
		if (stored != null)
		{
			clearIndices (stored);
			people.remove(stored);
		}
	}
	
	protected void clearIndices (Identity identity)
	{
		indexedByEmail.values().remove(identity);
	}
	
	protected void index (Identity identity)
	{
		if (identity.email != null)
		{
			indexedByEmail.put(identity.email, identity);
		}
	}
	
	public List<Identity> getAddressList ()
	{
		return people;
	}	
}
