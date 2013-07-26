/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __MAILIVERSE_MAIL_CACHE_CACHESTORE_H__
#define __MAILIVERSE_MAIL_CACHE_CACHESTORE_H__

#include "mailiverse/utilities/SmartPtr.h"
#include "Item.h"
#include "ID.h"
#include "Value.h"
#include "Version.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace mail {
namespace cache {

class StoreLibrary;

class Store : public ItemCollection
{
protected:
	StoreLibrary *storeLibrary;

public:
	Store () : storeLibrary(0) 
	{
		LogDebug (mailiverse::mail::cache::Store, "construct");
	}
	
	virtual ~Store() 
	{
		LogDebug (mailiverse::mail::cache::Store, "destruct");
	}

	void setStoreLibrary (StoreLibrary *storeLibrary)
	{
		this->storeLibrary = storeLibrary;
	}
	
	virtual bool has (const ID &) = 0;
	virtual const Version &version (const ID &) = 0;
	virtual const Value &get (const ID &) = 0;
	virtual void update (const ID &, const Version &, const Value &) = 0;
	virtual void put (const ID &, const Version &, const Value &) = 0;
	virtual void remove (const ID &) = 0;
	virtual void lock () {};
	
	virtual String debug() { return String(); }

	virtual bool isFull () = 0;
};

typedef utilities::SmartPtr<Store> StorePtr;

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHE_H_ */
