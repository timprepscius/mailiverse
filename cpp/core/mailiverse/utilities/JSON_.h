/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailivierse_utilities_JSON__h_
#define __mailivierse_utilities_JSON__h_

#include "Json.h"

namespace mailiverse {
namespace utilities {

class JSON__
{
public:
	JSON__ ()
	{
	}

	json::Object parse(const std::string &s)
	{
		return toJsonObject(s);
	}
	
	json::UnknownElement &getUnknown (json::Object &o, const std::string &key)
	{
		return o[key];
	}
	
	json::String &asString (json::UnknownElement &o)
	{
		return (json::String &)o;
	}
	
	json::String &getString(json::Object &o, const std::string &key)
	{
		return o[key];
	}
	
	json::String &getString(json::Array &o, int key)
	{
		return o[key];
	}
	
	StringPtr getStringPtr(json::Object &o, const std::string &key)
	{
		if (has(o, key))
			return new String(getString(o,key));
			
		return NULL;
	}
	
	StringPtr getStringPtr(json::Array &o, int key)
	{
		if (key < o.Size())
			return new String(getString(o,key));
			
		return NULL;
	}
	
	json::Object &getObject(json::Object &o, const std::string &key)
	{
		return o[key];
	}
	
	json::Object &getObject(json::Array &o, int key)
	{
		return o[key];
	}
	
	json::Array &getArray(json::Object &o, const std::string &key)
	{
		return o[key];
	}

	json::Array &getArray(json::Array &o, int key)
	{
		return o[key];
	}

	int size(const json::Array &o)
	{
		return o.Size();
	}

	bool has (const json::Object &o, const std::string &key)
	{
		return o.Find(key)!=o.End();
	}

} ;

JSON__ JSON_;

} // namespace
} // namespace

#endif
