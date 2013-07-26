/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef ZIPSERIALIZER_H_
#define ZIPSERIALIZER_H_

#include "../cache/ItemSerializer.h"

namespace mailiverse {
namespace mail {
namespace serializers {

class ZipSerializer : public cache::ItemSerializer
{
protected:
	cache::ItemSerializerPtr serializer;

public:
	ZipSerializer(cache::ItemSerializer *_serializer) :
		serializer(_serializer)
	{ }

	virtual ~ZipSerializer() { }

	virtual cache::Value serialize (cache::Item *cacheable) override;
	virtual void deserialize (cache::Item *cacheable, const cache::Value &value) override;
};

} /* namespace serializers */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* ZIPSERIALIZER_H_ */
