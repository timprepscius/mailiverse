/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "MasterCacheSerializer.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::mail;
using namespace mailiverse;

MasterCacheSerializer::MasterCacheSerializer (cache::ItemSerializer *storeSerializer)
{
	indexedCacheSerializer = new cache::IndexedCacheSerializer(storeSerializer);
	settingsSerializer = new serializers::ZipSerializer(new serializers::JsonSerializerStandard<model::Settings>());
}

MasterCacheSerializer::~MasterCacheSerializer()
{
	delete indexedCacheSerializer;
	delete settingsSerializer;
}

cache::Value MasterCacheSerializer::serialize(cache::Item *item) 
{
	if (dynamic_cast<model::Settings *>(item))
		return settingsSerializer->serialize(item);
	
	return indexedCacheSerializer->serialize(item);
}

void MasterCacheSerializer::deserialize(cache::Item *item, const cache::Value &value) 
{
	if (dynamic_cast<model::Settings *>(item))
		return settingsSerializer->deserialize(item, value);
	
	return indexedCacheSerializer->deserialize(item, value);
}

