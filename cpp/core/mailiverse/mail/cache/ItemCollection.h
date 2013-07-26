/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_client_cache_ItemOwner_h__
#define __mailiverse_mail_client_cache_ItemOwner_h__

#include "Item.h"

namespace mailiverse {
namespace mail {
namespace cache {

class ItemCollection : public Item {

public:
	virtual void onDirty(Item *item) {}
	virtual void onFlush () {}
};

DECLARE_SMARTPTR(ItemCollection);

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CACHEINFO_H_ */
