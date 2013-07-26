/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef JSONSERIALIZER_H_
#define JSONSERIALIZER_H_

#include <assert.h>
#include "../cache/ItemSerializer.h"
#include "mailiverse/utilities/Log.h"
#include <json/elements.h>
#include <json/writer.h>
#include <json/reader.h>

namespace mailiverse {
namespace mail {
namespace serializers {

template<typename T>
class JsonSerializer : public cache::ItemSerializer
{
public:
	JsonSerializer() {}

	virtual json::Object serialize (T *) = 0;
	virtual void deserialize (T *, const json::Object &o) = 0;

	virtual cache::Value serialize (cache::Item *cacheable) override
	{
		assert(dynamic_cast<T *>(cacheable));
		T *c = static_cast<T *>(cacheable);

		json::Object o = serialize(c);

		std::ostringstream ss;
		json::Writer::Write(o, ss);

		std::string s = ss.str();
		LogDebug(mailiverse::mail::serializers::JsonSerializer, cacheable->getClassName() << " serialize [" << s << "]");

		return cache::Value(s);
	}

	virtual void deserialize(cache::Item *cacheable, const cache::Value &value) override
	{
		assert(dynamic_cast<T *>(cacheable));
		T *c = static_cast<T *>(cacheable);

		json::Object o;
		json::SimpleMemStream ss((const char *)value.data(), value.size());
		
		LogDebug(mailiverse::mail::serializers::JsonSerializer, cacheable->getClassName() << " deserialize [" << std::string((const char *)value.data(), value.size()) << "]");
		json::Reader<json::SimpleMemStream>::Read(o, ss);

		deserialize (c, o);
	}
};

} /* namespace serializers */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* JSONSERIALIZER_H_ */
