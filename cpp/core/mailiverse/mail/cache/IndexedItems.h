/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_IndexedItems_h__
#define __mailiverse_mail_cache_IndexedItems_h__

#include "IndexedCache.h"
#include "StoreMemory.h"

namespace mailiverse {
namespace mail {
namespace cache {

class IndexedItems : public IndexedCache
{
public:
	IndexedItems(
		StoreLibrary *storeLibrary, const String &prefix, 
		bool persistCaches,
		ItemFactory *factory, ItemSerializer *serializer, bool persistItems
		) :
		IndexedCache (
			new CacheFactory(
				storeLibrary, prefix,
				factory,
				serializer,
				persistItems
			),
			persistCaches
		)
	{

	}

	virtual ~IndexedItems() {}
};

} // namespace
} // namespace
} // namspace

#endif 