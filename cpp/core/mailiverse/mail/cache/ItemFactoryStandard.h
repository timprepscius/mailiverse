/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_ItemFactoryStandard_h__
#define __mailiverse_mail_cache_ItemFactoryStandard_h__

#include "ItemFactory.h"
#include "Info.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace mail {
namespace cache {

template<typename T>
class ItemFactoryStandard : public ItemFactory
{
public:
	ItemFactoryStandard() {}

	virtual Item *instantiate (const Type &)
	{
		LogDebug(mailiverse::mail::cache::ItemFactoryStandard, "instantiating " << T::className());
		return new T();
	}
};

} // namespace
} /* namespace utilities */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_cache_ItemFactoryStandard_h__ */
