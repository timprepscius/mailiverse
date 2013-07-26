/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_CacheStoreMem_h__
#define __mailiverse_mail_cache_CacheStoreMem_h__

#include "Store.h"
#include "mailiverse/utilities/Monitor.h"
#include "Item.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace mail {
namespace cache {

class StoreMemory : public Store
{
	DECLARE_ITEM(StoreMemory);

protected:
	class Data : public Item 
	{
	public:
		Value bytes;

	public:
		Data (const ID &id)
		{
			setID(id);
		}

		void markLoad (const Value &bytes, const Version &version)
		{
			this->bytes = bytes;
			Item::markLoad(version);
		}

		void markDirty (const Value &bytes, const Version &version)
		{
			this->bytes = bytes;
			setLocalVersion(version);
		}

		void markStore ()
		{
			Item::markStore(getLocalVersion());
		}
	};
	DECLARE_SMARTPTR(Data);

public:
	typedef Map<ID, std::pair<Value, Version>> SKV;

protected:
	int requestedMaxSize;
	bool locked;
	utilities::EmptyMonitor monitor;

	typedef Map<ID, DataPtr> KV;
	KV map;

public:
	StoreMemory(int _requestedMaxSize) :
		requestedMaxSize(_requestedMaxSize),
		locked(false)
	{
		LogDebug(mailiverse::mail::cache::StoreMemory, "constructing " << this);
	}

	virtual ~StoreMemory()
	{
		LogDebug(mailiverse::mail::cache::StoreMemory, "destructing " << this);
	}
	
	bool hasDirtyChildren ()
	{
		utilities::EmptyMonitor::Reader r(monitor);
		for (auto &i : map)
			if (i.second->isDirty())
				return true;
		
		return false;
	}

	void lock ()
	{
		locked = true;
	}

	virtual void onStore () override
	{
		Store::onStore();
		for (auto &i : map)
		{
			if (i.second->isDirty())
				i.second->markStore();
		}
		
		locked = false;
	}

	long getSize ()
	{
		utilities::EmptyMonitor::Reader r(monitor);

		long s = 0;
		for (auto &i : map)
			s += i.first.size() + i.second->bytes.size();

		return s;
	}

	void update(const ID &id, const Version &version, const Value &bytes)
	{
		utilities::EmptyMonitor::Writer r(monitor);
		updateNoLock(id, version, bytes);
	}
	
	void updateNoLock(const ID &id, const Version &version, const Value &bytes)
	{
		LogDebug(mailiverse::mail::cache, this << "update" << id.str() <<  version.str());
		
		DataPtr item = map.getv(id);

		if (item == NULL)
		{
			item = new Data(id);
			map.put(id, item);
		}
		
		if (!item->isDirty())
		{
			LogDebug(mailiverse::mail::cache, "store updating data" << id.str());
			item->markLoad(bytes,version);
		}
		else
		{
			LogDebug(mailiverse::mail::cache, "store not updating data because of conflict" << id.str());
		}
	}

	void put(const ID &id, const Version &version, const Value &bytes)
	{
		utilities::EmptyMonitor::Writer r(monitor);
		assert(isWritable());

		LogDebug(mailiverse::mail::cache, this << "put" << id.str() << version.str());

		DataPtr item = map.getv(id);

		if (item == NULL)
		{
			item = new Data(id);
			map.put(id, item);
		}

		item->markDirty(bytes, version);
	}
	
	const Version &version (const ID &id) override
	{
		utilities::EmptyMonitor::Reader r(monitor);
		return map.getr(id)->getLocalVersion();
	}

	const Value &get (const ID &id) override
	{
		utilities::EmptyMonitor::Reader r(monitor);
		return map[id]->bytes;
	}

	bool isWritable ()
	{
		utilities::EmptyMonitor::Reader r(monitor);
		return ItemCollection::isWritable() && !locked;
	}

	bool has (const ID &id)
	{
		utilities::EmptyMonitor::Reader r(monitor);
		return (map.find(id) != map.end());
	}

	void remove (const ID &id) override
	{
		utilities::EmptyMonitor::Writer r(monitor);
		put(id, Version::DELETED, Value::DELETED);
	}

	bool isFull ()
	{
		utilities::EmptyMonitor::Reader r(monitor);
		return requestedMaxSize!=-1 && getSize() > requestedMaxSize;
	}

	const void putKeyValues (const SKV &_kv)
	{
		utilities::EmptyMonitor::Writer r(monitor);
		
		for (auto &i : _kv)
			updateNoLock(i.first, i.second.second, i.second.first);
	}

	SKV getKeyValues () const
	{
		utilities::EmptyMonitor::Reader r(monitor);
		SKV result;
		for (auto &i : map)
			result[i.first] = std::make_pair(i.second->bytes, i.second->getLocalVersion());
			
		return result;
	}
	
	String debug ()
	{
		String s = "";
		for (auto &i : map)
			s += i.first.debug() + " ";
			
		return s;
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHESTORE_H_ */
