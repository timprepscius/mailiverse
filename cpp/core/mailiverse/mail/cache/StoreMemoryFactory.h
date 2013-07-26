/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CACHESTOREMEMFACTORY_H_
#define CACHESTOREMEMFACTORY_H_

#include "Cache.h"
#include "ItemFactory.h"

namespace mailiverse {
namespace mail {
namespace cache {

class StoreMemoryFactory : public ItemFactory
{
protected:
	int maxSize;

public:
	StoreMemoryFactory(int _maxSize) :
		maxSize(_maxSize)
	{
	}

	virtual ~StoreMemoryFactory() {}

	virtual Item *instantiate (const Type &)
	{
		return new StoreMemory(maxSize);
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHEFACTORY_H_ */
