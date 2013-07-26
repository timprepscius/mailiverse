/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CACHEABLEFACTORY_H_
#define CACHEABLEFACTORY_H_

#include "mailiverse/utilities/SmartPtr.h"
#include "Type.h"
#include "Item.h"

namespace mailiverse {
namespace mail {
namespace cache {

struct ItemFactory
{
	virtual ~ItemFactory () {}

	virtual Item *instantiate (const Type &type) = 0;
};

typedef utilities::SmartPtr<ItemFactory> ItemFactoryPtr;

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHEABLEFACTORY_H_ */
