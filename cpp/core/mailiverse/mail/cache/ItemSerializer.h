/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CACHEABLESERIALIZER_H_
#define CACHEABLESERIALIZER_H_

#include "Value.h"
#include "mailiverse/utilities/SmartPtr.h"

namespace mailiverse {
namespace mail {
namespace cache {

class Item;

class ItemSerializer
{
public:
	virtual ~ItemSerializer() {}

	virtual Value serialize(Item *) = 0;
	virtual void deserialize(Item *, const Value &value) = 0;
};

typedef utilities::SmartPtr<ItemSerializer> ItemSerializerPtr;

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* CACHEABLESERIALIZER_H_ */
