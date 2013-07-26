/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __Util_Json_h__
#define __Util_Json_h__

#include "Strings.h"
#include <json/elements.h>
#include <json/reader.h>
#include <json/writer.h>
#include <sstream>

namespace mailiverse {
namespace utilities {

inline json::Object toJsonObject (const std::string &str)
{
	json::SimpleMemStream iss(str.c_str(), str.size());
	json::Object o;
	json::Reader<json::SimpleMemStream>::Read(o, iss);
	
	return o;
}

template<>
inline std::string toString (const json::Object &o)
{
	std::ostringstream oss;
	json::Writer::Write(o, oss);
	
	return oss.str();
}

inline
std::ostringstream &operator <<(std::ostringstream &oss, const json::Object &o)
{
	json::Writer::Write(o, oss);
	return oss;
}

typedef std::pair<std::string, json::Object> JsonDeserialization;

inline
JsonDeserialization deserializeJson (const std::string &string)
{
	if (string == "null")
		return std::pair<std::string, json::Object>("null", json::Object());
		
	int notPosition = string.find('!');
	if (!notPosition)
		throw std::runtime_error("deserialize failed");
		

	std::string className = string.substr(0, notPosition);
	std::string serialization = string.substr(notPosition+1);
	
	return JsonDeserialization(className, toJsonObject(serialization));
}

inline
std::string serializeJson(const std::string &className, const json::Object &object)
{
	return className + "!" + toString(object);
}

} // namespace
} // namespace

#endif
