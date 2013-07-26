/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "CacheFactory.h"
#include "StoreLibrary.h"

using namespace mailiverse::mail::cache;

CachePtr CacheFactory::instantiate(const cache::ID &id, bool isNew)
{
	return new Cache(
		itemFactory,
		itemSerializer,
		library->instantiate(prefix, id, isNew),
		persistItems
	);
}
