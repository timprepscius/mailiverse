/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_IndexedCache_h__
#define __mailiverse_mail_cache_IndexedCache_h__

#include "Cache.h"
#include "ItemSerializer.h"
#include "CacheFactory.h"
#include "Store.h"
#include "mailiverse/utilities/Log.h"
#include "Type.h"

namespace mailiverse {
namespace mail {
namespace cache {

class IndexedCache : public ItemCollection
{
	DECLARE_ITEM(IndexedCache);

public:
	friend class IndexedCacheSerializer;
	typedef ItemCollection Super;

protected:
	ID workCacheID;
	CachePtr workCache;
	CachePtr caches;
	CacheFactoryPtr cacheFactory;

protected:

	const ID &getWorkCacheID ()
	{
		return workCacheID;
	}

	void setWorkCacheID (const ID &id)
	{
		workCacheID = id;
		markDirty();
	}

	ID instantiateID(const ID &id)
	{
		ID workCacheID = getWorkCacheID();
		
		if (workCacheID == ID::NONE || workCache == NULL || !workCache->isWritable() || workCache->isFull())
		{
			workCacheID = ID::random();
			setWorkCacheID(workCacheID);
			
			workCache = getNewCache(workCacheID);
		}

		return ID::combine(workCacheID, id);
	}
	
	CachePtr getNewCache (const ID &ID)
	{
		CachePtr cache = cacheFactory->instantiate(ID, true);
		caches->put(ID, cache);
		
		return cache;
	}

	CachePtr getKnownCache(const ID &id)
	{
		ID cacheID = id.left();
		LogDebug (mailiverse::mail::cache::IndexedCache, "getCacheForItem " << this << " " << id.str() << " " << cacheID.str());

		if (!caches->has(cacheID))
		{
			CachePtr cache = cacheFactory->instantiate(cacheID, false);
			caches->link(cacheID, cache);
			return cache;
		}

		return caches->getItem<Cache>(0, cacheID);
	}

public:
	IndexedCache(
		CacheFactory *_cacheFactory, 
		bool persistCaches
	) :
		workCacheID(ID::NONE),
		cacheFactory(_cacheFactory)
	{
		LogDebug(mailiverse::mail::cache::IndexedCache, "IndexedCache");
	
		caches = new Cache(
			NULL, NULL, NULL, persistCaches
		);
		
		caches->setOwner(this);
		caches->markCreate();
	}

	virtual ~IndexedCache() 
	{
		LogDebug(mailiverse::mail::cache::IndexedCache, "~IndexedCache");
	}
	
	virtual void onLoaded ()
	{
		Super::onLoaded();
		
		if (workCacheID != ID::NONE && workCache == NULL)
			workCache = getKnownCache(workCacheID);
	}
	
	virtual bool hasDirtyChildren () override
	{
		return caches->hasDirtyChildren();
	}

	CachePtr createCache (const ID &id)
	{
		return getNewCache(id);
	}

	Item *create (const Type &type, const ID &id)
	{
		LogDebug(mailiverse::core::mail::cache::IndexedCache, "create " << id.str());
		return getKnownCache(id)->create(type, id);
	}

	template<typename T>
	utilities::SmartPtr<T> getItem(const Type &type, const ID &id)
	{
		LogDebug (mailiverse::mail::cache::IndexedCache, "getItem " << this << " " << id.str());

		ItemPtr c = getAndAcquire(type, id);
		assert(dynamic_cast<T *>((Item *)c));
		return static_cast<T*>((Item *)c);
	}

	template<typename T>
	utilities::SmartPtr<T> getItemNoAcquire(const Type &type, const ID &id)
	{
		LogDebug (mailiverse::mail::cache::IndexedCache, "getItemNoAcquire " << this << " " << id.str());

		ItemPtr c = get(type, id);
		assert(dynamic_cast<T *>((Item *)c));
		return static_cast<T*>((Item *)c);
	}

	ItemPtr getAndAcquire (const Type &type, const ID &id)
	{
		LogDebug (mailiverse::mail::cache::IndexedCache, "getAndAcquire " << this << " " << id.str());

		return getKnownCache(id)->getAndAcquire(type, id);
	}

	ItemPtr get(const Type &type, const ID &id)
	{
		return getKnownCache(id)->get(type, id);
	}

	void link(const ID &id, Item *c)
	{
		c->setID(id);
		getKnownCache(c->getID())->put(c->getID(), c);
	}

	void put(Item *c)
	{
		c->setID(instantiateID(ID::random()));
		getKnownCache(c->getID())->put(c->getID(), c);
	}

	void acquire(Item *cacheable)
	{
		getKnownCache(cacheable->getID())->acquire(cacheable);
	}

	virtual void onShutdown () override
	{
		Super::onShutdown();
		caches->markShutdown();
	}
	
	virtual void flush () override
	{
		caches->flush();
	}

	virtual void update () override
	{
		caches->update();
	}
};

DECLARE_SMARTPTR(IndexedCache);

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_cache_IndexedCache_h__ */
