/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef INDEXEDCACHESERIALIZER_H_
#define INDEXEDCACHESERIALIZER_H_

#include "IndexedCache.h"

namespace mailiverse {
namespace mail {
namespace cache {

class IndexedCacheSerializer : public ItemSerializer
{
protected:
	static const s8 VERSION;

	ItemSerializerPtr serializer;

public:
	IndexedCacheSerializer(ItemSerializer *_serializer) :
		serializer(_serializer)
	{

	}
	virtual ~IndexedCacheSerializer() {}

	virtual Value serialize (Item *cacheable) override
	{
		assert(dynamic_cast<IndexedCache *>(cacheable));
		IndexedCache *cache = static_cast<IndexedCache *>(cacheable);
		
		core::BlockWriter writer;
		writer.write(VERSION);
		writer.write(cache->workCacheID.serialize());

		return Value(writer.getBlock());
	}

	virtual void deserialize (Item *cacheable, const Value &value) override
	{
		assert(dynamic_cast<IndexedCache *>(cacheable));
		IndexedCache *cache = static_cast<IndexedCache *>(cacheable);

		core::BlockReader reader(value.block());

		s8 version;
		core::Block block;

		reader.read(version);
		reader.read(block);

		cache->workCacheID = ID::deserialize(block);
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* INDEXEDCACHESERIALIZER_H_ */
