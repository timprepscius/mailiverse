/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.util.LogNull;
import core.util.LogOut;

public class Cache extends ItemCollection
{
	static LogNull log = new LogNull(Cache.class);
	static LogNull logState = new LogNull("");

	Store store;
	Version storeVersion;
	
	ItemFactory factory;
	ItemSerializer serializer;
	
	Map<ID, Item> items = new HashMap<ID, Item>();
	
	public Cache(ItemFactory factory, ItemSerializer serializer, Store store)
	{
		this.factory = factory;
		this.serializer = serializer;
		this.store = store;
		
		if (store != null)
		{
			storeVersion = store.getLocalVersion();
			store.getLoadCallbacks().addCallback(update_());
		}
	}
	
	public void onCreate ()
	{
		store.markCreate();
	}

    public void update ()
    {
    	log.debug(this, "update");
    	if (store!=null && !store.getLocalVersion().equals(storeVersion))
	    	for (Item item : items.values())
	            populate(item);
    	
    	markLoad(store.getLocalVersion());
    }
    
    public Callback update_()
    {
    	return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				update();
				next(arguments);
			}
    	};
    }
    
    public boolean has (ID id)
    {
    	log.trace(this, "has", id);
    	return items.containsKey(id);
    }

    public Item getItem (Type type, ID id)
    {
    	log.trace(this, "getItem", type, id);
    	return getAndAcquire(type, id);
    }
    
    public Item getAndAcquire(Type type, ID id)
    {
    	log.trace(this, "getAndAcquire", type, id);
    	Item item = get(type, id);
    	acquire(item);
    	
    	return item;
    }
    
    public Item get (Type type, ID id)
    {
    	log.trace(this, "get", type, id);
    	Item item = items.get(id);
    	if (item != null)
    		return item;
    	
    	item = factory.instantiate(type);
    	item.setId(id);
    	item.onExisting();
    	
    	link(item);
    	
    	return item;
    }
    
    public void acquire (Item item)
    {
    	log.trace(this, "acquire", item);
    	if (!item.isLoaded())
    		populate(item);
    }
    
    public void populate (Item item)
    {
    	log.debug(this, "possibly populate", item);
		if (store != null)
		{
			log.debug(this, "store is not null");
			if (store.has(item.getId()))
			{
				log.debug(this, "store has", item);
                Version version = store.version(item.getId());
                if (!version.equals(item.getCacheVersion()))
                {
    				log.debug(this, "version is different", item);
                	if (!item.isDirty())
                	{
        				log.debug(this, "item is not dirty", item);
	                	if (serializer != null)
	                	{
	        				log.debug(this, "serializer is not null", item);
	                		if (!storeVersion.equals(Version.DELETED))
	                		{
		        				log.debug(this, "version is not deleted", item);
	                	    	log.debug(this, "populating", item);
	                			
	                			store.get_(item.getId())
	                				.addCallback(serializer.deserialize_(item))
	                				.addCallback(item.markLoad_(version))
	                				.invoke();
	                		}
	                	}
                	}
                	else
                	{
        				log.debug(this, "item is dirty, marking no load", item);
                		item.markNoLoad(version);
                	}
                }
			}
		}
    }
    
    public void put (Item item)
    {
    	log.debug(this, "put", item);
    	
    	link (item);
    	item.markCreate();
    }
    
    public void unlink (Item item)
    {
    	log.debug(this, "unlink", item);
    	
    	items.remove(item.getId());
    	itemRemoved(item);
    }
    
    public Callback unlink_ (Item item)
    {
		return new CallbackDefault(item) {
			public void onSuccess(Object... arguments) throws Exception {
				Item item = V(0);
				unlink(item);
				next(arguments);
			}
		};
    }
    
    public void link (Item item)
    {
    	log.debug(this, "link", item);
    	
    	items.put(item.getId(), item);
    	itemAdded(item);
    }
	
    public Callback flush_ ()
    {
    	log.debug(this, "flush_");
    	CallbackChain chain = new CallbackChain();
    	
    	for (Item item : items.values())
    	{
    		if (item.isDirty())
    		{
    	    	log.debug(this, "flush_ isDirty",item);
    	    	if (store.isWritable())
    	    	{
    	    		log.debug(this, "flush store isWritable", item);
    	    		if (item.isDeleted())
    	    		{
    	    			CallbackChain itemChain = new CallbackChain()
    	    				.addCallback(log.debug_(this, "actually removing", item))
    	    				.addCallback(store.remove_(item.getId()))
    	    				.addCallback(unlink_(item));
    	    			
    	    			chain.addCallback(itemChain);
    	    		}
    	    		else
    	    		{
		    	    	CallbackChain itemChain = new CallbackChain()
		    	    		.addCallback(log.debug_(this, "actually flushing", item))
		    	    		.addCallback(item.flush_())
		    				.addCallback(serializer.serialize_(item))
							.addCallback(store.put_(item.getId(), item.getLocalVersion()))
							.addCallback(item.markStore_(item.getLocalVersion()));
		    				
		    			chain.addCallback(itemChain);
    	    		}
    	    	}
    		}
    		else
    		if (item.hasDirtyChildren())
    		{
    	    	log.debug(this, "flush_ hasDirtyChildren",item);
    	    	
    	    	CallbackChain itemChain = new CallbackChain()
    	    		.addCallback(log.debug_(this, "actually flushing", item))
    	    		.addCallback(item.flush_());
    				
    			chain.addCallback(itemChain);
    		}
    	}
    	
    	chain.addCallback(onFlush_());
    	chain.addCallback(this.markStore_(this.getLocalVersion()));
    	
    	return chain;
    }
    
	public Callback checkClean_() {
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				if (isDirty() || hasDirtyChildren())
					throw new Exception("Still dirty after flush");
				
				next(arguments);
			}
		};
	}
	
    
	public Map<?, ? extends Item> getItemMap ()
	{
		return items;
	}
	
	public boolean isFull ()
	{
    	log.debug(this, "isFull");
		return store.isFull();
	}
	
	public void debug (LogOut log, String prefix)
	{
		final String PREFIX = "  |--";
		log.debug (prefix,this);
		store.debug(log, prefix+PREFIX);
		for (Entry<ID, Item> entry : items.entrySet())
			entry.getValue().debug(log, prefix+PREFIX);
	}
	
	public void debug (LogNull log, String prefix)
	{
		
	}
	public Callback debug_ ()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				debug(logState, "");
				next(arguments);
			}
		};
	}

} ;