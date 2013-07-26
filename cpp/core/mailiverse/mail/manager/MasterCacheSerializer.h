/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_MasterCacheSerializer_h__
#define __mailiverse_mail_manager_MasterCacheSerializer_h__

#include "../cache/ItemSerializer.h"

namespace mailiverse {
namespace mail {
namespace manager {

class MasterCacheSerializer : public cache::ItemSerializer
{
protected:
	cache::ItemSerializer *indexedCacheSerializer;
	cache::ItemSerializer *settingsSerializer;

public:	
	MasterCacheSerializer (cache::ItemSerializer *storeSerializer);
	virtual ~MasterCacheSerializer();

	cache::Value serialize(cache::Item *item);
	void deserialize(cache::Item *item, const cache::Value &value);
} ;

} // namespce
} // namespce
} // namespce


#endif
