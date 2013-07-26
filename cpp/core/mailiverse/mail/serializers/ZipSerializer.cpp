/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "ZipSerializer.h"
#include "mailiverse/core/BlockCompression.h"

namespace mailiverse {
namespace mail {
namespace serializers {

cache::Value ZipSerializer::serialize (cache::Item *cacheable)
{
	return cache::Value(
		core::deflate(
			serializer->serialize(cacheable).block()
		)
	);
}

void ZipSerializer::deserialize (cache::Item *cacheable, const cache::Value &value)
{
	serializer->deserialize(
		cacheable,
		cache::Value(core::inflate(value.block()))
	);
}

} /* namespace serializers */
} /* namespace mail */
} /* namespace mailiverse */
