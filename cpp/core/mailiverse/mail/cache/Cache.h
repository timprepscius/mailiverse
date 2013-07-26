/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __MAILIVERSE_MAIL_CACHE_CACHE_H__
#define __MAILIVERSE_MAIL_CACHE_CACHE_H__

#include "mailiverse/Types.h"
#include "ItemCollection.h"
#include "Type.h"
#include "Store.h"
#include "ItemFactory.h"
#include "Info.h"
#include "ItemSerializer.h"
#include "mailiverse/utilities/Monitor.h"
#include "mailiverse/utilities/Log.h"

#include <list>

namespace mailiverse {
namespace mail {
namespace cache {

class Cache : public ItemCollection
{
	DECLARE_ITEM(Cache);

public:
	typedef ItemCollection Super;

protected:
	StorePtr store;
	Version storeVersion;

	struct CacheItem
	{
		ItemWeakPtr weak;
		ItemPtr strong;
	} ;
	
	typedef Map<ID, CacheItem> ItemMap;
	typedef utilities::Monitor<ItemMap> Items;
	Items items;

	bool persistItems;
	ItemFactoryPtr factory;
	ItemSerializerPtr serializer;

protected:
	friend class Item;

public:
	Cache(
		ItemFactory *_factory,
		ItemSerializer *_serializer,
		Store *_store,
		bool _persistItems
	) :
		store(_store),
		factory(_factory),
		serializer(_serializer),
		persistItems(_persistItems)
	{
		LogDebug(mailiverse::mail::cache::Cache, "Cache " << this << " persist items " << persistItems);
		
		if (store)
			storeVersion = store->getLocalVersion();
	}

	virtual ~Cache ()
	{
		LogDebug(mailiverse::mail::cache::Cache, "~Cache " << this);
	}

	Store *getCacheStore ()
	{
		return store;
	}

	virtual void onCreate () override
	{
		LogDebug(mailiverse::mail::cache::Cache, this << " onCreate");

		Item::onCreate();
		
		if (store)
			store->markCreate();
	}

	virtual void onExisting () override
	{
		LogDebug(mailiverse::mail::cache::Cache, this << " onExisting");

		Item::onExisting();

		if (store)
			store->onExisting();
	}

	virtual void update () override
	{
		LogDebug(mailiverse::mail::cache::Cache, this << " update");

		// compile a safe list
		Vector<ItemPtr> s;
		{
			Items::Reader r(items);
			for (auto &i: *r)
				s.push_back((Item *)i.second.weak);
		}
		
		if (store)
		{
			if (store->getLocalVersion() != storeVersion)
			{
				LogDebug(mailiverse::mail::cache::Cache, this << " update " << "store->getLocalVersion() != storeVersion");

				// this may need to change to a defer list
				// on deadlock
				for (auto &item : s)
				{
					if (item)
						populate(item);
				}

				storeVersion = store->getLocalVersion();
			}
		}

		for (auto &item : s)
		{
			if (item)
				item->update();
		}
	}

	template<typename T>
	utilities::SmartPtr<T> getItem(const Type &type, const ID &id)
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " getItem " << id.str());
		ItemPtr c = getAndAcquire(type, id);
		assert(dynamic_cast<T *>((Item *)c));
		return static_cast<T*>((Item *)c);
	}

	ItemPtr getAndAcquire (const Type &type, const ID &id)
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " getAndAcquire " << id.str());
		ItemPtr c = get(type, id);
		acquire((Item *)c);

		return c;
	}

	ItemPtr get (const Type &type, const ID &id)
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " get " << id.str());
		{
			Items::Reader r(items);
			
			auto i = r->find(id);
			if (i != r->end())
			{
				ItemPtr item = (Item *)i->second.weak;
				if (item)
					return item;
			}
		}

		ItemPtr item = factory->instantiate(type);
		item->onExisting();
		link(id, (Item *)item);
		return item;
	}

	bool has (const ID &id)
	{
		Items::Reader r(items);
		
		auto i = r->find(id);
		if (i != r->end())
			return (Item *)i->second.weak != NULL;
	
		return false;
	}

	ItemPtr create (const Type &type, const ID &id)
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " create " << id.str());

		ItemPtr item = factory->instantiate(type);
		put(id, item);
		return item;
	}

	void acquire (Item *c)
	{
		if (!c->isLoaded())
			populate (c);
	}

	virtual void populate (Item *item)
	{
		if (store)
		{
			if (store->has(item->getID()))
			{
				const Version &storeVersion = store->version(item->getID());
				if (storeVersion!=item->getCacheVersion())
				{
					if (serializer)
					{
						if (storeVersion != Version::DELETED)
						{
							LogDebug(mailiverse::core::mail::cache::Cache, this << " populate " << item->getID().str());
							serializer->deserialize(item, store->get(item->getID()));
						}
						
						item->markLoad(storeVersion);
					}
				}
			}
			else
			{
				LogDebug(mailiverse::core::mail::cache::Cache, this << " store " << store->debug() << " does not have " << item->getID().debug());
			}
		}
	}

	void put (const ID &id, Item *object)
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " put " << id.str());

		willMarkDirty();
		
		link(id, object);
		object->markCreate();
		
		markDirty();
	}

	void link (const ID &id, Item *object)
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " link " << id.str());

		Items::Writer w(items);
		object->setID(id);
		object->setOwner(this);
		
		CacheItem ci;
		ci.weak = object;
		ci.strong = persistItems ? object : NULL;
		(*w)[object->getID()] = ci;
	}
	
	virtual void onDirty (Item *object)
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " onDirty " << object->getID().str());

		Items::Writer w(items);
		CacheItem &ci = (*w)[object->getID()];
		ci.weak = object;
		ci.strong = object;
	}

	virtual bool hasDirtyChildren () override
	{
		Items::Reader r(items);
		for (auto &i : *r)
		{
			if (i.second.weak)
				if (i.second.weak->isDirty() || i.second.weak->hasDirtyChildren())
					return true;
		}
		
		return false;
	}
	
	virtual void flush () override
	{
		LogDebug(mailiverse::core::mail::cache::Cache, this << " flush ");

		std::list<ID> markRemove;

		{
			Items::Reader r(items);
			for (auto i = r->begin(); i!=r->end(); ++i)
			{
				ItemPtr item = (Item *)i->second.weak;
				if (item)
				{
					if (item->isDirty())
					{
						item->flush();
						
						if (item->isDeleted())
						{
							if (store && serializer)
								store->remove(i->first);
							markRemove.push_back(i->first);
						}
						else
						{
							if (store && serializer)
								store->put(i->first, item->getLocalVersion(), serializer->serialize(item));
							item->markStore(item->getLocalVersion());
						}
					}
					else
					if (item->hasDirtyChildren())
					{
						item->flush();
					}
				}
				else
				{
					markRemove.push_back(i->first);
				}

				if (!persistItems)
				{
					CacheItem &x = *(CacheItem *)(&i->second);
					x.strong = NULL;	
				}
			}
		}

		if (!markRemove.empty())
		{
			Items::Writer w(items);
			for (auto &i : markRemove)
				w->erase(i);
		}
		
		onFlush();
		markStore(getLocalVersion());
	}
	
	virtual void onShutdown () override
	{
		Super::onShutdown();
		
		Items::Reader r(items);
		for (auto i = r->begin(); i!=r->end(); ++i)
		{
			ItemPtr item = (Item *)i->second.weak;
			if (item)
			{
				item->markShutdown();
			}
		}
	}
	
	bool isFull ()
	{
		return store->isFull();
	}
	
	Vector<ItemPtr> getItems ()
	{
		Vector<ItemPtr> results;
		Items::Reader r(items);
		for (auto &i : *r)
		{
			ItemPtr item = (Item *)i.second.weak;
			if (item)
				results.push_back(item);
		}
	
		return results;
	}
};

DECLARE_SMARTPTR(Cache);

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHEOBJECT_H_ */
