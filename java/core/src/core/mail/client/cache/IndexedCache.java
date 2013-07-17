/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.crypt.Cryptor;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;

public class IndexedCache extends ItemCollection
{
	static LogNull log = new LogNull(IndexedCache.class);

	static abstract class Factory 
	{
		public abstract Cache getCache(ID id, boolean isNew);
	}
	
	ID workCacheID = ID.None;
	Factory factory;

	Map<ID, Cache> caches = new HashMap<ID, Cache>();
	
	public IndexedCache (Factory factory)
	{
		this.factory = factory;
	}
	
	public Item getAndAcquire (Type type, ID iid)
	{
		log.trace(this, "getAndAcquire", type, iid);
		Item t = get(type, iid);
		acquire(t);
		return t;
	}
	
	public Callback flush_ () 
	{
		log.debug(this, "flush_");

		CallbackChain chain = new CallbackChain();
		for (Cache cache : caches.values())
		{
			if (cache.isDirty() || cache.hasDirtyChildren())
				chain.addCallback(cache.flush_());
		}
		
    	chain.addCallback(onFlush_());
		return chain;
	}
	
	public void acquire(Item t)
	{
		log.trace(this, "acquire", t.getId());
		getKnownCache(t.getId().left()).acquire(t);
	}
		
	Cache getKnownCache(ID cid)
	{
		log.trace(this, "getKnownCache",cid);
		if (!caches.containsKey(cid))
		{
			log.debug("getKnownCache creating");
			Cache cache = factory.getCache(cid, false);
			cache.setId(cid);
			caches.put(cid, cache);
			itemAdded(cache);
		}
		
		return caches.get(cid);
	}
	
	public void newCache(ID cid)
	{
		log.debug(this, "newCache", cid);
		
		Cache cache = factory.getCache(cid, true);
		caches.put(cid, cache);
		itemAdded(cache);
	}
	
	ID getWorkCacheID ()
	{
		return workCacheID;
	}
	
	void setWorkCacheID (ID id)
	{
		log.debug(this, "setWorkCacheID");
		workCacheID = id;
		markDirty();
	}
	
	public Item get(Type type, ID iid)
	{
		log.trace(this, "get", type, iid);
		return getKnownCache(iid.left()).get(type, iid);
	}

	public void link(ID id, Item t)
	{
		log.debug(this, "put", id, t);

		t.setId(id);
		getKnownCache(t.getId().left()).link(t);
	}

	public void put(Item t)
	{
		log.debug(this, "put", t);

		t.setId(instantiateID(ID.random()));
		getKnownCache(t.getId().left()).put(t);
	}

	private ID instantiateID(ID id) 
	{
		ID workCache = getWorkCacheID();
		if (workCache.equals(ID.None) || getKnownCache(workCache).isFull())
		{
			workCache = ID.random();
			setWorkCacheID(workCache);
			newCache(workCache);
		}
		
		return ID.combine(workCache, id);
	}
	
	public Map<?, ? extends Item> getItemMap ()
	{
		return caches;
	}
	
	public void debug (LogOut log, String prefix)
	{
		log.debug (prefix,this, "W", workCacheID);
		for (Entry<ID, Cache> entry : caches.entrySet())
			entry.getValue().debug(log, prefix+"  |--");
	}
}
