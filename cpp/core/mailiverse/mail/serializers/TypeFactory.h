/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_serializers_TypeFactory_h__
#define __mailiverse_mail_serializers_TypeFactory_h__

#include "../cache/ItemFactory.h"

namespace mailiverse {
namespace mail {
namespace serializers {

class TypeFactory : public cache::ItemFactory
{
protected:
	Map<cache::Type, cache::ItemFactoryPtr> factories;
	cache::ItemFactoryPtr defaultFactory;

public:
	TypeFactory(cache::Type type, cache::ItemFactory *factory, cache::ItemFactory *_default)
	{ 
		factories[type] = factory;
		defaultFactory = _default;
	}

	virtual ~TypeFactory() 
	{ 
	}

	virtual cache::Item *instantiate (const cache::Type &type) override
	{
		auto i = factories.find(type);
		if (i!=factories.end())
			return i->second->instantiate(type);
		
		return defaultFactory->instantiate(type);
	}
};

} /* namespace serializers */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* ZIPSERIALIZER_H_ */
