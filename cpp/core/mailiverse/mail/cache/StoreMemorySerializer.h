/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_StoreMemorySerializer_h__
#define __mailiverse_mail_cache_StoreMemorySerializer_h__

#include <assert.h>
#include "StoreMemory.h"
#include "ItemSerializer.h"
#include "mailiverse/core/BlockIO.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace mail {
namespace cache {

class StoreMemorySerializer : public ItemSerializer
{
public:
	static const int RECORD_OVERHEAD;
	static const int CACHE_OVERHEAD;
	static const s8 VERSION;

	virtual Value serialize (Item *cacheable) override
	{
		assert(dynamic_cast<StoreMemory *>(cacheable));
		StoreMemory *store = static_cast<StoreMemory *>(cacheable);

		StoreMemory::SKV kv = store->getKeyValues();
		core::BlockWriter writer;

		writer.write(VERSION);

		int count = kv.size();		
		writer.write(count);
		for (auto &i : kv)
		{
			LogDebug (mailiverse::mail::cache::StoreMemorySerializer, "serialize key " << i.first.str());

			writer.write(i.first.serialize());
			writer.write(i.second.first.block());
			writer.write(i.second.second.serialize());
		}

		return Value(writer.getBlock());
	}

	virtual void deserialize (Item *cacheable, const Value &value) override
	{
		assert(dynamic_cast<StoreMemory *>(cacheable));
		StoreMemory *store = static_cast<StoreMemory *>(cacheable);

		StoreMemory::SKV kv;

		core::BlockReader reader(value.block());
		s8 version;
		reader.read(version);

		int count;
		reader.read(count);
		
		while (count-- > 0)
		{
			core::Block key;
			core::Block value;
			core::Block version;
			
			reader.read(key);
			reader.read(value);
			reader.read(version);

			ID _key = ID::deserialize(key);
			Value _value(value);
			Version _version = Version::deserialize(version);

			LogDebug (mailiverse::mail::cache::StoreMemorySerializer, "deserialize key " << _key.str() << " value.size() " << value.size());
			kv[_key] = std::pair<Value,Version>(_value, _version);
		}

		store->putKeyValues(kv);
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_cache_StoreMemorySerializer_h__ */
