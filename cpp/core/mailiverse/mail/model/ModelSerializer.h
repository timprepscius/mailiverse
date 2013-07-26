/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_ModelSerializer_h__
#define __mailiverse_mail_model_ModelSerializer_h__

#include "../cache/ItemSerializer.h"

namespace mailiverse {
namespace mail {
namespace model {

class ModelSerializer : public cache::ItemSerializer {

protected:
	cache::ItemSerializer *mailSerializer, *conversationSerializer, *folderSerializer;
	
public:
	ModelSerializer ();
	virtual ~ModelSerializer ();
	
	virtual cache::Value serialize(cache::Item *item);
	virtual void deserialize(cache::Item *item, const cache::Value &value);
} ;

} // namespace
} // namespace
} // namespace


#endif
