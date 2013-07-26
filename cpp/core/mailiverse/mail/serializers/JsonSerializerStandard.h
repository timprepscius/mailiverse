/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_serializers_JsonSerializerStandard_h__
#define __mailiverse_mail_serializers_JsonSerializerStandard_h__

#include "JSON.h"
#include "JsonSerializer.h"

namespace mailiverse {
namespace mail {
namespace serializers {

template<typename T>
class JsonSerializerStandard : public JsonSerializer<T>
{
protected:
	JSON json;
	
public:
	JsonSerializerStandard() {}
	virtual ~JsonSerializerStandard() {}

	json::Object serialize (T *item)
	{
		 return json.toJSON(item);
	}

	void deserialize (T *item, const json::Object &o)
	{
		json.fromJSON(o, item);
	}

};

} /* namespace serializers */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* JSONSERIALIZERSTANDARD_H_ */
