/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.NoExport;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import core.util.Base64;
import core.util.Strings;

import core.crypt.HashSha256;

import mail.client.CacheManager;
import mail.client.cache.Type;

@Export()
public class FolderMaster extends FolderFilterSet
{
	HashSha256 hasher = new HashSha256();
	Map<String, Date> externalKeys = new HashMap<String, Date>();
	Map<String, Date> uidls = new HashMap<String, Date>();
	
	@NoExport
	public FolderMaster(CacheManager manager)
	{
		super(manager);
	}
	
	protected String hash (String key)
	{
		try
		{
			return Base64.encode(hasher.hash(Strings.toBytes(key)));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void addExternalKey (String id, Date date)
	{
		addExternalKeyHash(hash(id), date);
		markDirty();
	}
	
	public void addExternalKeyHash (String hash, Date date)
	{
		externalKeys.put(hash, date);
	}
	
	public boolean containsExternalKey (String id)
	{
		return externalKeys.containsKey(hash(id));
	}
	
	public void addUIDL (String uidl, Date date)
	{
		addUIDLHash(hash(uidl), date);
		markDirty();
	}
	
	public void addUIDLHash (String hash, Date date)
	{
		uidls.put(hash, date);
	}

	public boolean containsUIDL (String uidl)
	{
		return uidls.containsKey(hash(uidl));
	}
	
	public Map<String, Date> getUIDLHashes()
	{
		return uidls;
	}
	
	public Map<String, Date> getExternalKeyHashes ()
	{
		return externalKeys;
	}
}
