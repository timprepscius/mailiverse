/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

/*
#ifndef __mailiverse_mail_serializers_TypeSerializer_h__
#define __mailiverse_mail_serializers_TypeSerializer_h__

#include "../cache/ItemSerializer.h"

namespace mailiverse {
namespace mail {
namespace serializers {

class TypeSerializer : public cache::ItemSerializer
{
protected:
	Map<cache::Type, cache::ItemSerializerPtr> serializers;
	cache::ItemSerializerPtr defaultSerializer;

public:
	TypeSerializer(cache::Type type, cache::ItemSerializer *_serializer, cache::ItemSerializer *_default)
	{ 
		serializers[type] = _serializer;
		defaultSerializer = _default;
	}

	virtual ~TypeSerializer() 
	{ 
	}

	virtual cache::Value serialize (cache::Item *item) override
	{
		cache::Type type = item->getKey().;
		auto i = serializers.find(type);
		if (i!=serializers.end())
			return i->second->serialize(item);
		
		return defaultSerializer->serialize(item);
	}
	
	virtual void deserialize (cache::Item *item, const cache::Value &value) override
	{
		cache::Type type = item->getKey().getType();
		auto i = serializers.find(type);
		if (i!=serializers.end())
			return i->second->deserialize(item, value);
		
		return defaultSerializer->deserialize(item, value);
	}
};

} // namespace
} // namespace
} // namespace

#endif

*/
