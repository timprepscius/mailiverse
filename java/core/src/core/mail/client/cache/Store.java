/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Triple;

/**
 * Store sequence:
 * 		Store exists
 * 				#0 Store.markDirty (new local version A)
 * 			Item is added -> takes on storeVersionWhenModified=Store.getLocalVersion() A
 * 				#1 Store.flush_
 * 				#1 Store.serialize_
 * 				#1 Store.markDirty (new local version B)
 * 			Item is added -> takes on NEW storeVersionWhenModified B
 * 				#1 Store.markStore (local version A)
 * 
 * 
 * @author tprepscius
 *
 */
public class Store extends ItemCollection
{
	public static class Data extends Item 
	{
		byte[] bytes;
		
		public Data (ID id)
		{
			setId(id);
		}

		public void markLoad (byte[] bytes, Version version)
		{
			this.bytes = bytes;
			markLoad(version);
		}
		
		public void markDirty (byte[] bytes, Version version)
		{
			this.bytes = bytes;
			setLocalVersion(version);
		}
		
		public void markStore ()
		{
			markStore(getLocalVersion());
		}
		
		public String toString()
		{
			return super.toString();
		}
	};
	
	LogNull log = new LogNull(Store.class);
	protected Map<ID, Data> map = new HashMap<ID, Data>();
	int requestedMaxSize;
	boolean locked = false;
	
	public Store (int requestedMaxSize)
	{
		this.requestedMaxSize = requestedMaxSize;
	}
	
	byte[] get(ID id)
	{
		return map.get(id).bytes;
	}
	
	public CallbackDefault get_(ID id)
	{
		return new CallbackDefault(id) {
			public void onSuccess(Object... arguments) throws Exception {
				next(get((ID)V(0)));
			}
		};
	}
	
	public CallbackDefault put_(ID id, Version version)
	{
		return new CallbackDefault(id, version) {
			public void onSuccess(Object... arguments) throws Exception {
				put((ID)V(0), (Version)V(1), (byte[])arguments[0]);
				next();
			}
		};
	}

	public CallbackDefault remove_(ID id)
	{
		return new CallbackDefault(id) {
			public void onSuccess(Object... arguments) throws Exception {
				remove((ID)V(0));
				next();
			}
		};
	}

	Version version(ID id)
	{
		return map.get(id).getLocalVersion();
	}
	
	boolean has(ID id)
	{
		if (map.containsKey(id))
		{
			log.debug(this, "has", id);
		}
		else
		{
			log.debug(this, "does not have", id);
			log.debug(map.keySet().toArray());
		}
		
		return map.containsKey(id);
	}
	
	void remove(ID id)
	{
		log.debug(this, "remove", id);
		put(id, Version.DELETED, new byte[0]);
	}
	
	void update(ID id, Version version, byte[] bytes)
	{
		log.debug(this, "update", id, version);
		
		Data item = map.get(id);

		if (item == null)
		{
			item = new Data(id);
			map.put(id, item);
		}
		
		if (!item.isDirty())
		{
			log.debug("store updating data", id);
			item.markLoad(bytes,version);
		}
		else
		{
			log.debug("store not updating data because of conflict", id);
		}
	}
	
	void put(ID id, Version version, byte[] bytes)
	{
		assert(isWritable());
		
		log.debug(this, "put", id, version);
		
		Data item = map.get(id);

		if (item == null)
		{
			item = new Data(id);
			map.put(id, item);
		}
		
		item.markDirty(bytes, version);
	}
	
	int getSize ()
	{
		int size = 0;
		for (Data item : map.values())
			size += item.bytes.length;
		
		return size;
	}
	
	public boolean isWritable ()
	{
		return super.isWritable() && !locked;
	}
	
	boolean isFull ()
	{
		if (requestedMaxSize < 0)
			return false;
		
		return getSize() > requestedMaxSize;
	}
	
	public String toString ()
	{
		return super.toString() + " " + (getSize()/1000) + "k " + (isFull() ? "Full" :"");
	}
	
	public void lock ()
	{
		locked = true;
	}
	
	@Override
	public void onStored ()
	{
		super.onStored();
		
		for (Data item : map.values())
		{
			if (item.isDirty())
			{
				item.markStore();
			}
		}

		locked = false;
	}

	@Override
	public Map<?, ? extends Item> getItemMap() 
	{
		return map;
	}
	
	public void debug (LogOut log, String prefix)
	{
		final String PREFIX = "  |--";
		log.debug (prefix+" store:",this);
		for (Entry<ID, Data> entry : map.entrySet())
			entry.getValue().debug(log, prefix+PREFIX);
	}
	
}
