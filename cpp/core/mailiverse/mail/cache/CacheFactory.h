/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CACHEFACTORY_H_
#define CACHEFACTORY_H_

#include "Cache.h"

namespace mailiverse {
namespace mail {
namespace cache {

class StoreLibrary;

class CacheFactory
{
protected:
	StoreLibrary *library;
	String prefix;
	ItemFactoryPtr itemFactory;
	ItemSerializerPtr itemSerializer;
	bool persistItems;

public:
	CacheFactory(
		StoreLibrary *_library,
		const String &_prefix,
		ItemFactory *_itemFactory, 
		ItemSerializer *_itemSerializer,
		bool _persistItems
	) :
		library(_library),
		prefix(_prefix),
		itemFactory(_itemFactory),
		itemSerializer(_itemSerializer),
		persistItems(_persistItems)
	{
	}

	virtual ~CacheFactory() {}

	CachePtr instantiate(const cache::ID &id, bool isNew);
};

DECLARE_SMARTPTR(CacheFactory);

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHEFACTORY_H_ */
